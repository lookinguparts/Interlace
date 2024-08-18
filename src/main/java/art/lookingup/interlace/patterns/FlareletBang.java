package art.lookingup.interlace.patterns;

import art.lookingup.interlace.FlareletGen;
import art.lookingup.interlace.Topology;
import art.lookingup.util.LXUtil;
import art.lookingup.vstrip.Flarelet;
import art.lookingup.wavetable.*;
import heronarts.lx.LX;
import heronarts.lx.LXCategory;
import heronarts.lx.LXComponentName;
import heronarts.lx.color.ColorParameter;
import heronarts.lx.color.LXColor;
import heronarts.lx.parameter.BooleanParameter;
import heronarts.lx.parameter.CompoundParameter;
import heronarts.lx.parameter.DiscreteParameter;
import heronarts.lx.pattern.LXPattern;

@LXCategory("Form")
@LXComponentName("FlareletBang")
public class FlareletBang extends LXPattern {
  public CompoundParameter speed1 = new CompoundParameter("speed1", 60.0, -1000.0, 1000.0);
  public CompoundParameter fxDepth1 = new CompoundParameter("fxDepth1", 0f, 0.0f, 1.0f);
  public DiscreteParameter waveKnob1 = new DiscreteParameter("wave1", 0, 0, WavetableLib.countWavetables())
    .setDescription("Waveform type");
  public CompoundParameter widthKnob1 = new CompoundParameter("width1", 40f, 0.0f, 420.0f)
    .setDescription("Square wave width");
  public ColorParameter colorKnob1 = new ColorParameter("color1", 0xffffffff);
  public DiscreteParameter swatch1 = new DiscreteParameter("swatch1", -1, -1, 20);
  public CompoundParameter fadeTime1 = new CompoundParameter("fadeT1", 8.0, 0.0, 20.0)
    .setDescription("Fade time in seconds");

  public CompoundParameter brt1 = new CompoundParameter("brt1", 0.1f, 0.0f, 1.0f);
  public CompoundParameter pow1 = new CompoundParameter("pow1", 1.0f, 0.01f, 20.0f);
  public CompoundParameter pos1 = new CompoundParameter("pos1", 0.0, -200.0, 200.0);

  public CompoundParameter speed2 = new CompoundParameter("speed2", 60.0, -1000.0, 1000.0);
  public CompoundParameter fxDepth2 = new CompoundParameter("fxDepth2", 0f, 0.0f, 1.0f);
  public DiscreteParameter waveKnob2 = new DiscreteParameter("wave2", 0, 0, WavetableLib.countWavetables())
    .setDescription("Waveform type");
  public CompoundParameter widthKnob2 = new CompoundParameter("width2", 40f, 0.0f, 420.0f)
    .setDescription("Square wave width");
  public ColorParameter colorKnob2 = new ColorParameter("color2", 0xffffffff);
  public DiscreteParameter swatch2 = new DiscreteParameter("swatch2", -1, -1, 20);
  public CompoundParameter fadeTime2 = new CompoundParameter("fadeT2", 8.0, 0.0, 20.0)
    .setDescription("Fade time in seconds");
  public CompoundParameter brt2 = new CompoundParameter("brt2", 0.1f, 0.0f, 1.0f);
  public CompoundParameter pow2 = new CompoundParameter("pow2", 1.0f, 0.01f, 20.0f);
  public CompoundParameter pos2 = new CompoundParameter("pos2", 0.0, -200.0, 200.0);

  public CompoundParameter speed3 = new CompoundParameter("speed3", 60.0, -1000.0, 1000.0);
  public CompoundParameter fxDepth3 = new CompoundParameter("fxDepth3", 0f, 0.0f, 1.0f);
  public DiscreteParameter waveKnob3 = new DiscreteParameter("wave3", 0, 0, WavetableLib.countWavetables())
    .setDescription("Waveform type");
  public CompoundParameter widthKnob3 = new CompoundParameter("width3", 40f, 0.0f, 420.0f)
    .setDescription("Square wave width");
  public ColorParameter colorKnob3 = new ColorParameter("color3", 0xffffffff);
  public DiscreteParameter swatch3 = new DiscreteParameter("swatch3", -1, -1, 20);
  public CompoundParameter fadeTime3 = new CompoundParameter("fadeT3", 8.0, 0.0, 20.0)
    .setDescription("Fade time in seconds");
  public CompoundParameter brt3 = new CompoundParameter("brt3", 0.1f, 0.0f, 1.0f);
  public CompoundParameter pow3 = new CompoundParameter("pow3", 1.0f, 0.01f, 20.0f);
  public CompoundParameter pos3 = new CompoundParameter("pos3", 0.0, -200.0, 200.0);

  public final BooleanParameter bang1 = new BooleanParameter("bang1", false);
  public final BooleanParameter bang2 = new BooleanParameter("bang2", false);
  public final BooleanParameter bang3 = new BooleanParameter("bang3", false);

  public final BooleanParameter ring1 = new BooleanParameter("ring1", false);
  public final BooleanParameter ring2 = new BooleanParameter("ring2", false);
  public final BooleanParameter ring3 = new BooleanParameter("ring3", false);

  protected FlareletGen flareletGen1;
  protected FlareletGen flareletGen2;
  protected FlareletGen flareletGen3;

  public FlareletBang(LX lx) {
    super(lx);
    LXUtil.setLX(lx);
    bang1.setMode(BooleanParameter.Mode.MOMENTARY);
    bang1.addListener((p) -> {
      if (((BooleanParameter)p).isOn()) {
        lx.log("Banged 1!");
        banged(1);
      }
    });
    bang2.setMode(BooleanParameter.Mode.MOMENTARY);
    bang2.addListener((p) -> {
      if (((BooleanParameter)p).isOn()) {
        lx.log("Banged 2!");
        banged(2);
      }
    });
    bang3.setMode(BooleanParameter.Mode.MOMENTARY);
    bang3.addListener((p) -> {
      if (((BooleanParameter)p).isOn()) {
        lx.log("Banged 3!");
        banged(3);
      }
    });
    addParameter("bang1", bang1);
    addParameter("ring1", ring1);
    addParameter("pos1", pos1);
    addParameter("fadeT1", fadeTime1);
    addParameter("speed1", speed1);
    addParameter("fxDepth1", fxDepth1);
    addParameter("wave1", waveKnob1);
    addParameter("width1", widthKnob1);
    addParameter("color1", colorKnob1);
    addParameter("swatch1", swatch1);
    addParameter("brt1", brt1);
    addParameter("pow1", pow1);

    addParameter("bang2", bang2);
    addParameter("ring2", ring2);
    addParameter("pos2", pos2);
    addParameter("fadeT2", fadeTime2);
    addParameter("speed2", speed2);
    addParameter("fxDepth2", fxDepth2);
    addParameter("wave2", waveKnob2);
    addParameter("width2", widthKnob2);
    addParameter("color2", colorKnob2);
    addParameter("swatch2", swatch2);
    addParameter("brt2", brt2);
    addParameter("pow2", pow2);

    addParameter("bang3", bang3);
    addParameter("ring3", ring3);
    addParameter("pos3", pos3);
    addParameter("fadeT3", fadeTime3);
    addParameter("speed3", speed3);
    addParameter("fxDepth3", fxDepth3);
    addParameter("wave3", waveKnob3);
    addParameter("width3", widthKnob3);
    addParameter("color3", colorKnob3);
    addParameter("swatch3", swatch3);
    addParameter("brt3", brt3);
    addParameter("pow3", pow3);


    // Create a flarelet generator for each structure
    flareletGen1 = new FlareletGen(lx, Topology.getDefaultTopologies(lx).get(0));
    flareletGen2 = new FlareletGen(lx, Topology.getDefaultTopologies(lx).get(1));
    flareletGen3 = new FlareletGen(lx, Topology.getDefaultTopologies(lx).get(2));
  }

  /**
   * Called when 'bang' is triggered.  We want to start a new Flarelet.
   */
  public void banged(int which) {
    switch (which) {
      case 1:
        if (ring1.isOn()) {
          flareletGen1.startFlareletAll();
        } else {
          flareletGen1.startFlareletIncrement();
        }
        break;
      case 2:
        if (ring2.isOn()) {
          flareletGen2.startFlareletAll();
        } else {
          flareletGen2.startFlareletIncrement();
        }
        break;
      case 3:
        if (ring3.isOn()) {
          flareletGen3.startFlareletAll();
        } else {
          flareletGen3.startFlareletIncrement();
        }
        break;
    }
  }


  /**
   * onActive is called when the pattern starts playing and becomes the active pattern.  Here we re-assigning
   * our speeds to generate some randomness in the speeds.
   */
  @Override
  public void onActive() {

  }

  @Override
  public void run(double deltaMs) {
    flareletGen1.speed = speed1.getValuef();
    flareletGen1.fadeTime = fadeTime1.getValuef() * 1000f;
    flareletGen1.fx = (fxDepth1.getValuef() > 0.05)?1:0;
    flareletGen1.fxDepth = fxDepth1.getValuef();
    flareletGen1.waveWidth = widthKnob1.getValuef();
    flareletGen1.color = colorKnob1.getColor();
    flareletGen1.swatch = swatch1.getValuei();
    flareletGen1.pow = pow1.getValuef();
    flareletGen1.brt = brt1.getValuef();
    flareletGen1.bright = brt1.getValuef() > 0.001;
    flareletGen1.position = pos1.getValuef();
    flareletGen1.setWavetable(waveKnob1.getValuei());

    flareletGen2.speed = speed2.getValuef();
    flareletGen2.fadeTime = fadeTime2.getValuef() * 1000f;
    flareletGen2.fx = (fxDepth2.getValuef() > 0.05)?1:0;
    flareletGen2.fxDepth = fxDepth2.getValuef();
    flareletGen2.waveWidth = widthKnob2.getValuef();
    flareletGen2.color = colorKnob2.getColor();
    flareletGen2.swatch = swatch2.getValuei();
    flareletGen2.pow = pow2.getValuef();
    flareletGen2.brt = brt2.getValuef();
    flareletGen2.bright = brt2.getValuef() > 0.001;
    flareletGen2.position = pos2.getValuef();
    flareletGen2.setWavetable(waveKnob2.getValuei());

    flareletGen3.speed = speed3.getValuef();
    flareletGen3.fadeTime = fadeTime3.getValuef() * 1000f;
    flareletGen3.fx = (fxDepth3.getValuef() > 0.05)?1:0;
    flareletGen3.fxDepth = fxDepth3.getValuef();
    flareletGen3.waveWidth = widthKnob3.getValuef();
    flareletGen3.color = colorKnob3.getColor();
    flareletGen3.swatch = swatch3.getValuei();
    flareletGen3.pow = pow3.getValuef();
    flareletGen3.brt = brt3.getValuef();
    flareletGen3.bright = brt3.getValuef() > 0.001;
    flareletGen3.position = pos3.getValuef();
    flareletGen3.setWavetable(waveKnob3.getValuei());

    flareletGen1.disposeExpiredFlarelets();
    flareletGen2.disposeExpiredFlarelets();
    flareletGen3.disposeExpiredFlarelets();

    for (Flarelet f : flareletGen1.runningFlarelets) {
      //lx.log("Rendering flare on 1.");
      f.waveOnTop(colors, LXColor.Blend.ADD, -1);
      f.pos += speed1.getValuef() * (float)deltaMs / 1000f;
    }
    for (Flarelet f : flareletGen2.runningFlarelets) {
      //lx.log("Rendering flare on 2.");
      f.waveOnTop(colors, LXColor.Blend.ADD, -1);
      f.pos += speed2.getValuef() * (float)deltaMs / 1000f;
    }
    for (Flarelet f : flareletGen3.runningFlarelets) {
      //lx.log("Rendering flare on 3.");
      f.waveOnTop(colors, LXColor.Blend.ADD, -1);
      f.pos += speed3.getValuef() * (float)deltaMs / 1000f;
    }
  }
}
