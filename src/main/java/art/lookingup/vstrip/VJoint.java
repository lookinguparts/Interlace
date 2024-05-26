package art.lookingup.vstrip;

public class VJoint {
    public VJoint(VStrip v, boolean isStartPoint) {
        vStrip = v;
        isAdjacentStripAStartPoint = isStartPoint;
    }

    public Point3D getJointPt() {
        if (isAdjacentStripAStartPoint)
            return vStrip.a;
        else
            return vStrip.b;
    }

    public Point3D getFarPt() {
        if (isAdjacentStripAStartPoint)
            return vStrip.b;
        else
            return vStrip.a;
    }

    public VStrip vStrip;
    public boolean isAdjacentStripAStartPoint;
}
