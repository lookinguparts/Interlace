package art.lookingup.interlace.modulator;

import heronarts.lx.studio.LXStudio;
import heronarts.glx.ui.UI2dContainer;
import heronarts.glx.ui.component.UIKnob;
import heronarts.lx.studio.ui.modulation.UIModulator;
import heronarts.lx.studio.ui.modulation.UIModulatorControls;

public class UIEnableDisableModulator implements UIModulatorControls<EnableDisableModulator> {
  private final static int TOP_PADDING = 4;

  public void buildModulatorControls(LXStudio.UI ui, UIModulator uiModulator, EnableDisableModulator modulator) {
    uiModulator.setLayout(UI2dContainer.Layout.VERTICAL);
    uiModulator.setChildSpacing(2);

    uiModulator.addChildren(
      UI2dContainer.newHorizontalContainer(UIKnob.HEIGHT, 2,
        new UIKnob(0, 0, modulator.input)
      ).setTopMargin((float) TOP_PADDING)
    );
  }
}
