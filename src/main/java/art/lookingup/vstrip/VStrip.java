package art.lookingup.vstrip;

import heronarts.lx.model.LXPoint;

import java.util.ArrayList;
import java.util.List;

/**
 * Virtual LED strip.
 *
 * This class provides an abstraction layer for the underlying physical strips.  Typically, it
 * suffices to just render to the physical strips but on some complicated installs the physical
 * strips may be running the wrong way or shorter or longer than ideal.  That happens due to
 * power injection requirements or limits on the number of pixels per output of the pixel controller.
 * We expect a virtual strip to be constructed out of pieces of underlying physical strips.  For each
 * point in a virtual strip, we have an associated LVPoint that tracks virtual-strip-local coordinates
 * to simplify the rendering of patterns.  There are also scenarios where we might want to break up one
 * long physical strip into multiple virtual strips.  For example, imagine the scenario of wiring a cube.
 * Rather than using 12 separate strips we might want to use fewer strips that snake around the cube but
 * for rendering purposes it would be more convenient to reference virtual strips that represent each edge
 * of the cube.
 */
public class VStrip {

    public List<LVPoint> points;
    public int id;
    public Point3D a;
    public Point3D b;
    public List<VJoint> myStartPointJoints;
    public List<VJoint> myEndPointJoints;

    // The current maximum x value.  This is incremented as points are added.
    public float maxX;

    public VStrip(int stripId)
    {
        id = stripId;
        maxX = 0f;
        myStartPointJoints = new ArrayList<>();
        myEndPointJoints = new ArrayList<>();
        points = new ArrayList<LVPoint>();
    }

    public float length() {
        return maxX;
    }

    /**
     * Adds a point to this virtual strip.  When adding a point, the led pitch from the underlying physical
     * strip should be passed in.  This allows strips with differing pitches to be included in the same
     * virtual strip.
     * @param p The point to add.
     * @param pitch
     * @return
     */
    public float addPoint(LXPoint p, float pitch) {
        LVPoint lvPoint = new LVPoint(p);
        maxX += pitch;
        lvPoint.xpos = maxX;
        points.add(lvPoint);
        return maxX;
    }

    /**
     * For all the points, compute their normalized X coordinate.  This should only be done after all points have
     * been added.  This is merely a convenience routine for pre-computing each point's position from 0 to 1 along the
     * strip for any rendering algorithms that rely on normalized coordinates.
     * @return
     */
    public float normalize()
    {
        for (LVPoint p: points)
            p.xt = p.xpos / maxX;
        return maxX;
    }

}
