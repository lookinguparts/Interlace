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
@LXComponentName("VStripSelector")
public class VStripSelector extends LXPattern {

  public final DiscreteParameter hyperboloid =
    new DiscreteParameter("Hyperbld", 0, 0, 3)
      .setDescription("Which Hyperboloid.");

  public final DiscreteParameter strip =
    new DiscreteParameter("Strip", 0, -1, 16)
      .setDescription("Which Strip. -1 == All");

  public VStripSelector(LX lx) {
    super(lx);
    addParameter("Hyperbld", this.hyperboloid);
    addParameter("Strip", this.strip);
  }

  @Override
  protected void run(double deltaMs) {
    final int whichHyperboloid = this.hyperboloid.getValuei();
    final int whichStrip = this.strip.getValuei();

    VTopology vt = Topology.getDefaultTopologies(lx).get(whichHyperboloid);
    for (VStrip vStrip : vt.strips) {
      if (whichStrip == -1 || whichStrip == vStrip.id) {
        for (LVPoint p : vStrip.points) {
          colors[p.p.index] = LXColor.gray(100);
        }
      }
    }
  }
}
