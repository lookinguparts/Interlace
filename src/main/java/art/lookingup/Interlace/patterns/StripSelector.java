/**
 * Copyright 2022- Mark C. Slee, Heron Arts LLC
 *
 * This file is part of the LX Studio software library. By using
 * LX, you agree to the terms of the LX Studio Software License
 * and Distribution Agreement, available at: http://lx.studio/license
 *
 * Please note that the LX license is not open-source. The license
 * allows for free, non-commercial use.
 *
 * HERON ARTS MAKES NO WARRANTY, EXPRESS, IMPLIED, STATUTORY, OR
 * OTHERWISE, AND SPECIFICALLY DISCLAIMS ANY WARRANTY OF
 * MERCHANTABILITY, NON-INFRINGEMENT, OR FITNESS FOR A PARTICULAR
 * PURPOSE, WITH RESPECT TO THE SOFTWARE.
 *
 * @author Mark C. Slee <mark@heronarts.com>
 */

package art.lookingup.Interlace.patterns;

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
@LXComponentName("StripSelector")
public class StripSelector extends LXPattern {

  public final DiscreteParameter hyperboloid =
    new DiscreteParameter("Hyperbld", 0, -1, 3)
      .setDescription("Which Hyperboloid. -1 == All");

  public final DiscreteParameter strip =
    new DiscreteParameter("Strip", 0, -1, 16)
      .setDescription("Which Strip. 0 == All");

  public StripSelector(LX lx) {
    super(lx);
    addParameter("Hyperbld", this.hyperboloid);
    addParameter("Strip", this.strip);
  }

  @Override
  protected void run(double deltaMs) {
    final int whichHyperboloid = this.hyperboloid.getValuei();
    final int whichStrip = this.strip.getValuei();
    List<LXModel> strips = lx.getModel().sub("strip");

    for (int hNum = 0; hNum < 3; hNum++) {
      // Hyperboloids are tagged with H1, H2, H3
      List<LXModel> hyperboloids = lx.getModel().sub("H" + (hNum + 1));
      if (hyperboloids.size() == 1) {
        if (whichHyperboloid == hNum || whichHyperboloid == -1) {
          for (int stripNum = 0; stripNum < 16; stripNum++) {
            // Strips in each Hyperboloid ar tagged with strip0, strip1, ..., strip15
            List<LXModel> stripModel = hyperboloids.get(0).sub("strip" + stripNum);
            if (stripModel.size() == 1) {
              if (whichStrip == stripNum || whichStrip == -1) {
                for (LXPoint p : stripModel.get(0).points) {
                  colors[p.index] = LXColor.gray(100);
                }
              }
            }
          }
        }
      }
    }
  }
}
