import os

from flask import Flask, request
from mp4_to_wav import mp4_to_wav
from find_freq import find_freq
from NoteConfirm import send_msg
from gevent import pywsgi

app = Flask(__name__)
app.secret_key = 'xxxxxxx'

goal = []
queue='88888888'
goal_index = 0


@app.route('/', methods=['GET', 'POST'])
def hello_world():  # put application's code here
    return 'Hello World!'


@app.route('/pdf', methods=['POST'])
def pdf():  # put application's code here
    global goal
    request.files['file'].save('/home/ubuntu/lyf/music/pdf-omr-pmerge-master/test.pdf')
    os.system("sudo rm -rf ./music/pdf-omr-pmerge-master/musicxml")
    os.system('sudo sh ./music/pdf-omr-pmerge-master/pmerge.sh "./music/pdf-omr-pmerge-master/test.pdf"')
    i = 1
    while True:
        if os.access(f"/home/ubuntu/lyf/music/pdf-omr-pmerge-master/musicxml/out{i}.xml", os.F_OK):
            os.system(f"sudo ./go/bin/go run main.go ./music/pdf-omr-pmerge-master/musicxml/out{i}.xml")
            goal.append('000' + send_msg(f"music/pdf-omr-pmerge-master/musicxml/out{i}.xml.mid") + '000')
            i += 1
        else:
            break
    print(goal)
    return str(i-1)


@app.route('/mp4', methods=['POST'])
def mp4():  # put application's code here
    global queue, goal, goal_index
    # 接收MP4
    request.files['file'].save('/home/ubuntu/lyf/tmp/test.mp4')
    # 判断是否匹配结束
    if goal_index == len(goal):
        return "sustain"
    # 转换成wav
    mp4_to_wav('/home/ubuntu/lyf/tmp/test.mp4', '/home/ubuntu/lyf/tmp/test.wav')
    # 找到音频对应的频率
    freq = str(find_freq('/home/ubuntu/lyf/tmp/test.wav'))
    print(freq)
    # 将音频添加到序列
    if len(queue) == 0 or freq != queue[-1]:
        queue = queue + freq
    if len(queue) > 7:
        queue = queue[1:]
    # 是否匹配
    maxn=0
    count=0
    for i in range(5):
        f=0
        for j in range(7):
            if queue[j]==goal[goal_index][i+j]:
                if f==1:
                    count+=1
                else:
                    f=1
                    count=1
                maxn=max(maxn,count)
            else:
                f=0
                count=0
    if maxn>=5:
        goal_index+=1
        queue='88888888'
        return "next page"
    return "sustain"


@app.route('/test', methods=['POST'])
def test():  # put application's code here
    request.files['file'].save('/home/ubuntu/lyf/test')
    return "OK already get"


if __name__ == '__main__':
    server = pywsgi.WSGIServer(('0.0.0.0', 5050), app)
    server.serve_forever()
    app.run()
