package art.lookingup.interlace;

import art.lookingup.vstrip.VStrip;
import art.lookingup.vstrip.VTopology;
import com.google.gson.JsonObject;
import heronarts.lx.LX;
import heronarts.lx.model.LXModel;
import heronarts.lx.model.LXPoint;

import java.util.ArrayList;
import java.util.List;

/**
 * Topology for Interlace.
 *
 * We will create multiple topologies that can be used by patterns.  The most basic
 * topologies will be three topologies where the strips from our models are mapped
 * to virtual strips.  Each topology will consist of a single Hyperboloid fixture that
 * contains 16 strips.  The strips will be unconnected with no joints.
 *
 * TODO(tracy): For each Hyperboloid, we should also construct a topology such that
 * each strip is attached to itself.  We should also have an additional topology for each
 * Hyperboloid where the top of the strip attaches to the adjacent strip's top and the bottom
 * attaches to the adjacent strip's bottom.
 */
public class Topology {

  static List<VTopology> defaultTopologies = null;

  static public List<VTopology> getDefaultTopologies(LX lx) {
    if (defaultTopologies == null) {
      defaultTopologies = createDefaultTopologies(lx);
    }
    return defaultTopologies;
  }

  static public List<VTopology> createDefaultTopologies(LX lx) {

    List<VTopology> topologies = new ArrayList<>();
    List<VTopology> nullTopologies = new ArrayList<>();

    for (int hNum = 0; hNum < 3; hNum++) {
      // Hyperboloids are tagged with H1, H2, H3
      LXModel hyperboloid = getHyperboloid(lx, hNum + 1);
      VTopology vTop = new VTopology();
      VTopology nullVTop = new VTopology();
      for (int stripNum = 0; stripNum < 16; stripNum++) {
        // Strips in each Hyperboloid ar tagged with strip0, strip1, ..., strip15
        LXModel stripModel = getStripNum(hyperboloid, stripNum);
        if (stripModel != null) {
          lx.log("Creating strip " + stripNum + " for Hyperboloid " + (hNum + 1));
          VStrip vStrip = new VStrip(stripNum);
          addStripToVStrip(vStrip, stripModel);
          lx.log("Added " + stripModel.points.length + " points to strip " + stripNum);
          vStrip.normalize();
          vTop.addStrip(vStrip);
          nullVTop.addStrip(vStrip);
        }
      }
      //createDefaultJoints(vTop);
      topologies.add(vTop);
      nullTopologies.add(nullVTop);
    }
    topologies.addAll(nullTopologies);
    lx.log("Number of topologies: " + topologies.size());
    return topologies;
  }

  static LXModel getHyperboloid(LX lx, int which) {
    List<LXModel> hyperboloids = lx.getModel().sub("H" + which);
    if (hyperboloids.size() == 1) {
      return hyperboloids.get(0);
    }
    return null;
  }

  static LXModel getStripNum(LXModel hyperboloid, int stripNum) {
    List<LXModel> stripModel = hyperboloid.children("strip" + stripNum);
    if (stripModel.size() == 1) {
      return stripModel.get(0);
    }
    return null;
  }

  /**
   * Given a LXModel that is a strip, compute the point spacing.  We will need this to
   * build our virtual strips.
   *
   * @param strip
   * @return
   */
  static public float computeSpacing(LXModel strip) {
    if (strip.points.length < 2) {
      return 1f;
    }
    // Just use the first 2 points since it is expected to be consistent between all points.
    float dist = (float)Math.sqrt(
      (strip.points[0].x - strip.points[1].x) * (strip.points[0].x - strip.points[1].x) +
      (strip.points[0].y - strip.points[1].y) * (strip.points[0].y - strip.points[1].y) +
      (strip.points[0].z - strip.points[1].z) * (strip.points[0].z - strip.points[1].z)
    );

    return dist;
  }

  static public void addStripToVStrip(VStrip vStrip, LXModel strip) {
    float spacing = computeSpacing(strip);
    for (LXPoint p : strip.points) {
      vStrip.addPoint(p, spacing);
    }
  }

  static public void addStripToVStripReverse(VStrip vStrip, LXModel strip) {
    float spacing = computeSpacing(strip);
    for (int i = strip.points.length - 1; i >= 0; i--) {
      vStrip.addPoint(strip.points[i], spacing);
    }
  }

  // TODO(tracy): This could be loaded from a json file.  Each topology should have it's own file.
  // TODO(tracy): This could be auto-generated from the model since it is just self-connected.
  // Mapping between vstrip id's and their joints to other vstrips.  This is a mapping of the form:
  // vstripId1, vstripId2, *ADJACENCY* where ADJACENCY is:
  // 1 if the adjacent vstrip is adjacent at a start point.
  // 2 if the adjacent vstrip is adjacent at an end point.
  static public int[] defaultStartPointJoints =  {
    0, 0, 2,
    1, 1, 2,
    2, 2, 2,
    3, 3, 2,
    4, 4, 2,
    5, 5, 2,
    6, 6, 2,
    7, 7, 2,
    8, 8, 2,
    9, 9, 2,
    10, 10, 2,
    11, 11, 2,
    12, 12, 2,
    13, 13, 2,
    14, 14, 2,
    15, 15, 2
  };

  static public int[] defaultEndPointJoints =  {
    0, 0, 1,
    1, 1, 1,
    2, 2, 1,
    3, 3, 1,
    4, 4, 1,
    5, 5, 1,
    6, 6, 1,
    7, 7, 1,
    8, 8, 1,
    9, 9, 1,
    10, 10, 1,
    11, 11, 1,
    12, 12, 1,
    13, 13, 1,
    14, 14, 1,
    15, 15, 1
  };

  static public JsonObject createStartPointJoints(int[] startPointJoints) {
    JsonObject startJoints = new JsonObject();
    for (int i = 0; i < startPointJoints.length; i+=3) {
      startJoints.addProperty(startPointJoints[i] + "-" + startPointJoints[i+1], startPointJoints[3]);
    }
    return startJoints;
  }

  static public JsonObject createEndPointJoints(int[] endPointJoints) {
    JsonObject endJoints = new JsonObject();
    for (int i = 0; i < endPointJoints.length; i+=3) {
      endJoints.addProperty(endPointJoints[i] + "-" + endPointJoints[i+1], endPointJoints[3]);
    }
    return endJoints;
  }

  static public void createDefaultJoints(VTopology vTop) {
    vTop.buildJoints(createStartPointJoints(defaultStartPointJoints), createEndPointJoints(defaultEndPointJoints));
  }
}
