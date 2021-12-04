import wave
import numpy as np
import matplotlib.pyplot as plt
import sys

from numpy.core.records import fromrecords
def find_freq(file):
    f = wave.open(file, 'rb' )
    params = f.getparams()
    # 声道数   量化位数或量化深度   采样频率    采样点数
    nchannels, sampwidth, framerate, nframes = params[:4]
    Data_str = f.readframes(nframes)
    Data_num = list(np.frombuffer(Data_str,dtype=np.int16))
    Data_list = [Data_num[i] for i in range(0,len(Data_num),nchannels)]
    if sum(map(abs,Data_list))/nframes < 300:
        return 8
    volume=np.fft.rfft(Data_list)
    freqs = np.linspace(0, framerate/2, nframes//2+1)


    ans=freqs[np.where(volume==np.max(volume))]
    print(ans)
    pitchs=[15.434,16.352,18.354,20.602,21.927,24.500,27.501,30.868,32.704]
    i=0
    while(i<=9):
        for k in range(1,8):
            if (pitchs[k-1]*2**i+pitchs[k]*2**i)/2<ans<(pitchs[k]*2**i+pitchs[k+1]*2**i)/2:
                return k
        i+=1

    # plt.plot(freqs[1:],np.abs(volume.real)[1:])
    # plt.show()
