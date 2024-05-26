package art.lookingup.vstrip;

import heronarts.lx.model.LXPoint;

/**
 * This class represents a virtual point.  A virtual strip consists of a set of virtual points.  These points
 * can come from a set of underlying strips.  This allows the rendering to be more fully uncoupled from the
 * underlying physical wiring.
 */
public class LVPoint {

  public LXPoint p;

  // X position of this virtual point. This will be computed as we add this point to a virtual strip.
  public float xpos;
  // The parameterized t coordinate of this virtual point on the virtual strip.
  public float xt;

  public LVPoint(LXPoint point) {
    this.p = point;
  }
}
