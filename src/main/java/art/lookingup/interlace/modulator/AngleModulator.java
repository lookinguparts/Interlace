package art.lookingup.interlace.modulator;

import art.lookingup.util.LXUtil;
import art.lookingup.vstrip.Point3D;
import heronarts.lx.LX;
import heronarts.lx.LXCategory;
import heronarts.lx.model.LXModel;
import heronarts.lx.model.LXPoint;
import heronarts.lx.modulator.LXModulator;
import heronarts.lx.osc.LXOscComponent;
import heronarts.lx.parameter.CompoundParameter;

import java.util.List;

@LXModulator.Global("TwistAngle")
@LXCategory(LXCategory.MACRO)
public class AngleModulator extends LXModulator implements LXOscComponent {

  public final CompoundParameter angle1 =
    new CompoundParameter("Angle1")
      .setUnits(CompoundParameter.Units.PERCENT_NORMALIZED)
      .setDescription("Hyperboloid twist angle 1");

  public final CompoundParameter angle2 =
    new CompoundParameter("Angle2")
      .setUnits(CompoundParameter.Units.PERCENT_NORMALIZED)
      .setDescription("Hyperboloid twist angle 2");

  public final CompoundParameter angle3 =
    new CompoundParameter("Angle3")
      .setUnits(CompoundParameter.Units.PERCENT_NORMALIZED)
      .setDescription("Hyperboloid twist angle 3");


  protected float[] center1 = new float[2];
  protected float[] center2 = new float[2];
  protected float[] center3 = new float[2];

  protected float[][] hCenters = new float[3][2];

  protected float pointSpacing = 1f;
  protected float radius = 7.5f * 12f;

  // Based on Steel Fabrication Proposal.pdf
  protected final float RADIUS_SPEC = 7.5f * 12f;

  protected final int NUM_STRIPS = 16;
  protected final int NUM_HYPERBOLOIDS = 3;

  // Store original bottom points for a strips for all hyperboloids.
  protected Point3D[][] bottomPoints = new Point3D[NUM_HYPERBOLOIDS][NUM_STRIPS];


  public final CompoundParameter[] knobs = {
    angle1, angle2, angle3
  };

  public AngleModulator() {
    this("Angles");
  }

  public AngleModulator(String label) {
    super(label);
    addParameter("angle1", this.angle1);
    addParameter("angle2", this.angle2);
    addParameter("angle3", this.angle3);


  }

  @Override
  public void onStart() {
    super.onStart();
    angle1.addListener((p) -> {
      updatePointPositions(0, ((float)Math.PI * 2f * p.getValuef()));
    });
    angle2.addListener((p) -> {
      updatePointPositions(1, ((float)Math.PI * 2f * p.getValuef()));
    });
    angle3.addListener((p) -> {
      updatePointPositions(2, ((float)Math.PI * 2f * p.getValuef()));
    });
    computeHyperboloidCenters();
    storeBottomPoints();
    pointSpacing = computePointSpacing();
  }

  protected void storeBottomPoints() {
    for (int hNum = 0; hNum < 3; hNum++) {
      // Hyperboloids are tagged with H1, H2, H3
      List<LXModel> hyperboloids = lx.getModel().sub("H" + (hNum + 1));
      if (hyperboloids.size() == 1) {
        for (int stripNum = 0; stripNum < 16; stripNum++) {
          List<LXModel> stripModel = hyperboloids.get(0).sub("strip" + stripNum);
          Point3D bottomPoint = new Point3D(stripModel.get(0).points[0].x, stripModel.get(0).points[0].y, stripModel.get(0).points[0].z);
          bottomPoints[hNum][stripNum] = bottomPoint;
        }
      }
    }
  }

  protected void computeHyperboloidCenters() {
    computeHyperboloidCenter(0, center1);
    computeHyperboloidCenter(1, center2);
    computeHyperboloidCenter(2, center3);
    hCenters[0] = center1;
    hCenters[1] = center2;
    hCenters[2] = center3;
  }

  protected void computeHyperboloidCenter(int hNum, float[] center) {
    float xv = 0f;
    float zv = 0f;
    int pointCount = 0;
    List<LXModel> hyperboloids = lx.getModel().sub("H" + (hNum + 1));
    if (hyperboloids.size() == 1) {
      for (int stripNum = 0; stripNum < 16; stripNum++) {
        List<LXModel> stripModel = hyperboloids.get(0).sub("strip" + stripNum);
        if (stripModel.size() == 1) {
          for (LXPoint p : stripModel.get(0).points) {
            xv += p.x;
            zv += p.z;
            pointCount += 1;
          }
        }
      }
      xv = xv / pointCount;
      zv = zv / pointCount;
    }
    center[0] = xv;
    center[1] = zv;
  }

  protected float computeRadius() {
    // just compute the distance of a point to the center of the hyperboloid in the XZ plane.
    List<LXModel> hyperboloid1 = lx.getModel().sub("H1");
    if (hyperboloid1.size() == 1) {
      LXPoint pt1 = hyperboloid1.get(0).points[0];
      // Just in the XZ plane so Y is 0.
      Point3D p1 = new Point3D(pt1.x, 0f, pt1.z);
      Point3D center = new Point3D(center1[0], 0f, center1[1]);
      return p1.distanceTo(center);
    } else return RADIUS_SPEC;
  }

  protected float computePointSpacing() {
    for (int hNum = 0; hNum < 3; hNum++) {
      List<LXModel> hyperboloids = lx.getModel().sub("H" + (hNum + 1));
      if (hyperboloids.size() == 1) {
        for (int stripNum = 0; stripNum < 16; stripNum++) {
          List<LXModel> stripModel = hyperboloids.get(0).sub("strip" + stripNum);
          if (stripModel.size() == 1) {
            LXPoint pt1 = stripModel.get(0).points[0];
            LXPoint pt2 = stripModel.get(0).points[1];
            Point3D p1 = new Point3D(pt1.x, pt1.y, pt1.z);
            Point3D p2 = new Point3D(pt2.x, pt2.y, pt2.z);
            return p1.distanceTo(p2);
          }
        }
      }
    }
    // Couldn't process the model, return a default value.
    return 1f;
  }

  /**
   * Updates the selected hyperboloid by rotating the bottom point of each strip by the angle (in radians).
   * TODO(tracy): We need to preserve all of our original point positions so that we can just rotate by the
   * absolute angle here.  Otherwise, attempting to rotate by the angle delta will cause problems because of the
   * extension of the telescoping arm.
   * @param whichHyperboloid Which hyperboloid to update.  -1 means update all hyperboloids.
   * @param angle in radians
   */
  protected void updatePointPositions(int whichHyperboloid, float angle) {
    if (lx == null) return;
    for (int hNum = 0; hNum < 3; hNum++) {
      // Support updating multiple hyperboloids at once just in case the model banging operation
      // becomes a performance issue.  This way we can batch the model updates (which can trigger
      // some normalization recomputation).
      if (hNum != whichHyperboloid && whichHyperboloid != -1)
        continue;
      // Hyperboloids are tagged with H1, H2, H3
      List<LXModel> hyperboloids = lx.getModel().sub("H" + (hNum + 1));
      if (hyperboloids.size() == 1) {
        for (int stripNum = 0; stripNum < 16; stripNum++) {
          // Strips in each Hyperboloid are tagged with strip0, strip1, ..., strip15
          // The points in the strip, based on modelgen.py, start at the bottom and move
          // upwards.  But for the rotation, our top point is fixed and we are rotating the
          // bottom point.  We need to rotate the bottom point, compute the new total
          // distance from bottom point to top point.  The difference between the new distance
          // and the original distance is the amount that the telescoping arm will need to extend.
          // We then need to compute the new direction vector for the new bottom point and original
          // top point.  We then need to travel along that vector the amount of the extension.  From
          // there we need to recompute the new point positions for each led on the strip by incrementing
          // the spacing along the direction vector.  We should also verify that the top point doesn't
          // change position to sanity check our computations.
          List<LXModel> stripModel = hyperboloids.get(0).sub("strip" + stripNum);
          // We will need to rotate the bottom point about the hyperboloid center, not in world coordinates.
          // So we need to translate the bottom point to the origin, rotate it, and then translate it back.
          Point3D bottomPoint = new Point3D(bottomPoints[hNum][stripNum]);
          Point3D topPoint = new Point3D(stripModel.get(0).points[stripModel.get(0).points.length - 1].x,
            stripModel.get(0).points[stripModel.get(0).points.length - 1].y,
            stripModel.get(0).points[stripModel.get(0).points.length - 1].z);
          float originalDistance = bottomPoint.distanceTo(topPoint);
          Point3D hyperboloidCenter = new Point3D(hCenters[hNum][0], 0f, hCenters[hNum][1]);
          // NOTE(tracy): We are only rotating in the XZ plane around the Y axis so we can
          // leave the bottom point's Y coordinate unchanged.
          bottomPoint.translate(-hyperboloidCenter.x, 0f, -hyperboloidCenter.z);
          bottomPoint.rotateYAxis(angle);
          bottomPoint.translate(hyperboloidCenter.x, 0f, hyperboloidCenter.z);
          // Now we need to recompute the new point positions for the strip.
          float newDistance = bottomPoint.distanceTo(topPoint);
          float extension = newDistance - originalDistance;
          Point3D direction = new Point3D(topPoint);
          direction.subtract(bottomPoint);
          direction.normalize();
          bottomPoint.translate(direction.x * extension, direction.y * extension, direction.z * extension);
          for (int ptNum = 0; ptNum < stripModel.get(0).points.length; ptNum++) {
            LXPoint p = stripModel.get(0).points[ptNum];
            Point3D newPoint = new Point3D(bottomPoint);
            newPoint.translate(direction.x * pointSpacing * ptNum, direction.y * pointSpacing * ptNum, direction.z * pointSpacing * ptNum);
            p.x = newPoint.x;
            p.y = newPoint.y;
            p.z = newPoint.z;
          }
          // Now, double check the top point to make sure it hasn't moved.
          Point3D newTopPoint = new Point3D(stripModel.get(0).points[stripModel.get(0).points.length - 1].x,
            stripModel.get(0).points[stripModel.get(0).points.length - 1].y,
            stripModel.get(0).points[stripModel.get(0).points.length - 1].z);
          if (newTopPoint.distanceTo(topPoint) > 0.01f) {
            LX.log("Top point moved during rotation.  Check computations: " + newTopPoint.distanceTo(topPoint));
          }
        }
      }
    }
    if (lx != null)
      lx.getModel().update(true, true);
  }

  @Override
  protected double computeValue(double deltaMs) {
    // Not relevant
    return 0;
  }
}