import numpy as np
from matplotlib import pyplot as plt
import math
def fit_ellipse(x, y):
    D1 = np.vstack([x**2, x*y, y**2]).T
    D2 = np.vstack([x, y, np.ones(len(x))]).T
    S1 = D1.T @ D1
    S2 = D1.T @ D2
    S3 = D2.T @ D2
    T = -np.linalg.inv(S3) @ S2.T
    M = S1 + S2 @ T
    C = np.array(((0, 0, 2), (0, -1, 0), (2, 0, 0)), dtype=float)
    M = np.linalg.inv(C) @ M
    eigval, eigvec = np.linalg.eig(M)
    con = 4 * eigvec[0]* eigvec[2] - eigvec[1]**2
    ak = eigvec[:, np.nonzero(con > 0)[0]]
    return np.concatenate((ak, T @ ak)).ravel()
def cart_to_pol(coeffs):
    a = coeffs[0]
    b = coeffs[1] / 2
    c = coeffs[2]
    d = coeffs[3] / 2
    f = coeffs[4] / 2
    g = coeffs[5]

    den = b**2 - a*c
    if den > 0:
        raise ValueError('coeffs do not represent an ellipse: b^2 - 4ac must'
                         ' be negative!')

    # The location of the ellipse centre.
    x0, y0 = (c*d - b*f) / den, (a*f - b*d) / den

    num = 2 * (a*f**2 + c*d**2 + g*b**2 - 2*b*d*f - a*c*g)
    fac = np.sqrt((a - c)**2 + 4*b**2)
    # The semi-major and semi-minor axis lengths (these are not sorted).
    ap = np.sqrt(num / den / (fac - a - c))
    bp = np.sqrt(num / den / (-fac - a - c))

    # Sort the semi-major and semi-minor axis lengths but keep track of
    # the original relative magnitudes of width and height.
    width_gt_height = True
    if ap < bp:
        width_gt_height = False
        ap, bp = bp, ap

    # The eccentricity.
    r = (bp/ap)**2
    if r > 1:
        r = 1/r
    e = np.sqrt(1 - r)

    # The angle of anticlockwise rotation of the major-axis from x-axis.
    if b == 0:
        phi = 0 if a < c else np.pi/2
    else:
        phi = np.arctan((2.*b) / (a - c)) / 2
        if a > c:
            phi += np.pi/2
    if not width_gt_height:
        # Ensure that phi is the angle to rotate to the semi-major axis.
        phi += np.pi/2
    phi = phi % np.pi

    return x0, y0, ap, bp, e, phi


a=np.load('calibration.npy')
X=a[:,0]
Y=a[:,1]
coeffs =fit_ellipse(a[:,0],a[:,1])
x0, y0, ap, bp, e, phi = cart_to_pol(coeffs)
print('x0, y0, ap, bp, e, phi = ', x0, y0, ap, bp, e, phi)
print(coeffs)
rat = ap / bp
rot = round(phi / (math.pi / 2.0))
rotation = -(phi - rot * math.pi / 2.0)
a = np.cos(rotation)
b = np.sin(rotation)
c = ap/bp
d = x0
e = y0
xcorr = (X - d) * a - (Y - e) * b
ycorr = ((X - d) * b + (Y - e) * a) / c
xcorr2 = (X - d) * a 
ycorr2 = ((Y - e) * a) / c
angs=np.zeros(100)
offs=0.4
rotfix=0.0
x = np.arange(100)
for n in range(len(xcorr)):
    angs[int(100*(math.atan2(ycorr[n],xcorr[n])+offs)/(2*3.14159))%100]+=1
nangs=1-angs>0.1
nangs2=nangs
locs=np.where(nangs>0)
spread=locs[0].max()-locs[0].min()
print('spread = ', spread)
if spread>80:
    rotfix+=50
    nangs2=np.roll(nangs,50)
    locs=np.where(nangs2>0)
meanang=locs[0].mean()-rotfix
print('mean angle = ', meanang)
print([c,d,e])
plt.subplot(2,1,1)
plt.plot(nangs)
plt.plot(nangs2)
#plt.plot(100*(math.atan2(ycorr[:],xcorr[:])+offs)/(2*3.14159)%100)
plt.subplot(2,1,2)
plt.scatter(X,Y)
#plt.scatter(xcorr,ycorr)
plt.scatter(xcorr2,ycorr2)
plt.show()