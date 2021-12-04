import mido

NoteToNum={55:5,57:6,59:7,60:1,62:2,64:3,65:4,67:5,69:6,71:7,72:1}

f = open("tt.txt", "w")

def send_msg(path):
	mid=mido.MidiFile(path)
	msgl = []
	for i , track in enumerate(mid.tracks):
		#print('Track {}: {}'.format(i, track.name), file=f)
		for msg in track:
			msg=str(msg).split()
			if (msg[0]=="note_on") :
				msg=int(msg[2][5:])
				msgl+=[msg]
	#print(msgl,file=f)
	for i in range(0,len(msgl)):
		while i<len(msgl)-1 and msgl[i]==msgl[i+1]:
			msgl.pop(i+1)
	for i in range(0, len(msgl)):
		msgl[i]=NoteToNum[msgl[i]]
	return msgl[-7:].__str__().replace("[","").replace("]","").replace(",","").replace(" ","")
if __name__ == '__main__':
	l=["test.mid"]
	for i in l:
		send_msg(i)
		#print("\n!!!nxt file!!!\n", file=f)

f.close()