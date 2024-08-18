package art.lookingup.interlace.modulator;

import heronarts.lx.LX;
import heronarts.lx.LXCategory;
import heronarts.lx.LXLoopTask;
import heronarts.lx.mixer.LXAbstractChannel;
import heronarts.lx.modulator.LXModulator;
import heronarts.lx.osc.LXOscComponent;
import heronarts.lx.parameter.*;

@LXModulator.Global("EnableDisable")
@LXModulator.Device("EnableDisable")
@LXCategory(LXCategory.CORE)
public class EnableDisableModulator extends LXModulator implements LXOscComponent, LXNormalizedParameter {

  public final CompoundParameter input =
    new CompoundParameter("Input", 0)
      .setUnits(CompoundParameter.Units.PERCENT_NORMALIZED)
      .setDescription("The fader value to enable/disable channels");

  LXLoopTask loopTask = null;
  LXParameterListener paramListener = null;

  public EnableDisableModulator() {
    this("EnableDisable");
  }

  public EnableDisableModulator(String label) {
    super(label);
    addParameter("input", this.input);
    paramListener = new LXParameterListener() {
      public void onParameterChanged(LXParameter p) {
        if (p == input) {
          if (input.getValuef() > 0 && loopTask == null) {
            // Start engine loop task
            loopTask = new LXLoopTask() {
              public void loop(double deltaMs) {
                autoEnableDisableChannels(lx, input.getValuef());
              }
            };
            lx.engine.addLoopTask(loopTask);
          } else if (input.getValuef() == 0) {
            // Remove engine loop task
            if (loopTask != null) {
              lx.engine.removeLoopTask(loopTask);
              loopTask = null;
            }
          }
        }
      }
    };
    input.addListener(paramListener, true);
  }

  @Override
  public LXNormalizedParameter setNormalized(double value) {
    input.setValue(value);
    return input;
  }

  @Override
  public double getNormalized() {
    return getValue();
  }

  @Override
  protected double computeValue(double deltaMs) {
    return input.getValuef();
  }

  @Override
  public void dispose() {
    if (loopTask != null) {
      lx.engine.removeLoopTask(loopTask);
      loopTask = null;
    }
    if (paramListener != null) {
      input.removeListener(paramListener);
      paramListener = null;
    }
  }

  public static void autoEnableDisableChannels(LX lx, float minValue) {
    for (LXAbstractChannel absChannel : lx.engine.mixer.channels) {
      if (absChannel.fader.getValuef() > minValue) {
        absChannel.enabled.setValue(true);
      } else {
        absChannel.enabled.setValue(false);
      }
    }
  }
}