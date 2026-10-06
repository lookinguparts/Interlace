import numpy as np
import math
from matplotlib import pyplot as plt

arr = np.arange(-10, 300,.05)
#print(arr)
x=np.cos(arr*math.pi/180)
y=np.sin(arr*math.pi/180)
angs=np.mod(np.arctan2(y,x),2*math.pi)
angvals=np.zeros(360)
for n in range(len(angs)):
    angvals[int(360*(angs[n])/(2*math.pi))%360]+=1
nangs=angvals<0.1
nangs2=nangs
locs=np.where(nangs>0)
rotfix=0
spread=locs[0].max()-locs[0].min()
print('spread = ', spread)
if spread>300:
    rotfix+=180
    nangs2=np.roll(nangs,180)
    locs=np.where(nangs2>0)
spread=locs[0].max()-locs[0].min()
print('spread = ', spread)
print('rotfix = ', rotfix)
meanang=locs[0].mean()-rotfix
maxang=np.mod(locs[0].min()-rotfix,360)
minang=np.mod(locs[0].max()-rotfix,360)
if minang>maxang:
    minang-=360
print('min angle = ', minang)
print('max angle = ', maxang)
print('mean angle = ', meanang)
plt.subplot(2,2,1)
plt.scatter(x,y)
plt.subplot(2,2,2)
plt.plot(angs%(2*math.pi))
plt.subplot(2,2,3)
plt.plot(nangs)
plt.subplot(2,2,4)
plt.plot(angvals)
plt.show()