package art.lookingup.interlace.modulator;

import heronarts.glx.ui.component.UIColorControl;
import heronarts.lx.studio.LXStudio;
import heronarts.glx.ui.component.UIKnob;
import heronarts.glx.ui.component.UIMeter;
import heronarts.lx.studio.ui.modulation.UIModulator;
import heronarts.lx.studio.ui.modulation.UIModulatorControls;

public class UICosPaletteModulator implements UIModulatorControls<CosPaletteModulator> {

  public void buildModulatorControls(LXStudio.UI ui, UIModulator uiModulator, final CosPaletteModulator cosColor) {
    uiModulator.setContentHeight(UIKnob.HEIGHT);

    uiModulator.addChildren(
      new UIKnob(20, 0, cosColor.which),
      new UIKnob(60, 0, cosColor.input),
      new UIColorControl(-30, 0, cosColor.color), // 145
      new UIKnob(95, 0, cosColor.red),
      new UIKnob(140, 0, cosColor.green),
      new UIKnob(165, 0, cosColor.blue)
    );
  }

}