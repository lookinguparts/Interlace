package art.lookingup.Interlace.patterns;

import art.lookingup.Interlace.Topology;
import art.lookingup.vstrip.LVPoint;
import art.lookingup.vstrip.VStrip;
import art.lookingup.vstrip.VTopology;
import heronarts.lx.LX;
import heronarts.lx.LXCategory;
import heronarts.lx.LXComponentName;
import heronarts.lx.color.LXColor;
import heronarts.lx.model.LXModel;
import heronarts.lx.model.LXPoint;
import heronarts.lx.parameter.CompoundParameter;
import heronarts.lx.parameter.DiscreteParameter;
import heronarts.lx.pattern.LXPattern;

import java.util.List;

@LXCategory("Test")
@LXComponentName("XYZColor")
public class XYZColor extends LXPattern {


  public XYZColor(LX lx) {
    super(lx);
  }

  @Override
  protected void run(double deltaMs) {
    //lx.log("model points: " + model.points.length);
    LXPoint[] points = model.points;
    float xMin = Float.MAX_VALUE;
    float xMax = Float.MIN_VALUE;
    float yMin = Float.MAX_VALUE;
    float yMax = Float.MIN_VALUE;
    float zMin = Float.MAX_VALUE;
    float zMax = Float.MIN_VALUE;
    for (LXPoint p : points) {
      xMin = Math.min(xMin, p.x);
      xMax = Math.max(xMax, p.x);
      yMin = Math.min(yMin, p.y);
      yMax = Math.max(yMax, p.y);
      zMin = Math.min(zMin, p.z);
      zMax = Math.max(zMax, p.z);
    }

   for (LXPoint p : model.points) {
     float xN = (p.x - xMin) / (xMax - xMin);
     float yN = (p.y - yMin) / (yMax - yMin);
     float zN = (p.z - zMin) / (zMax - zMin);
        colors[p.index] = LXColor.rgb((int)(xN * 255), (int)(yN * 255), (int)(zN * 255));
      }
  }
}
