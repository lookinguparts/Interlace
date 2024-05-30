package art.lookingup.Interlace.patterns;

import art.lookingup.Interlace.Topology;
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
@LXComponentName("FlareBang")
public class FlareBang extends LXPattern {
  public static final int MAX_FLARELETS = 48;

  public CompoundParameter speed = new CompoundParameter("speed", 1.0, 0.0, 1000.0);
  public DiscreteParameter numFlarelets = new DiscreteParameter("flrlets", 10, 1, MAX_FLARELETS+1);
  public DiscreteParameter fxKnob = new DiscreteParameter("fx", 0, 0, 3).setDescription("0=none 1=sparkle 2=cosine");
  public CompoundParameter fxDepth = new CompoundParameter("fxDepth", 1.0f, 0.1f, 1.0f);
  public DiscreteParameter waveKnob = new DiscreteParameter("wave", 0, 0, 3).setDescription("Waveform type");
  public CompoundParameter widthKnob = new CompoundParameter("width", 0.1f, 0.0f, 120.0f).setDescription("Square wave width");
  public CompoundParameter cosineFreq = new CompoundParameter("cfreq", 1.0, 1.0, 400.0);
  public ColorParameter colorKnob = new ColorParameter("color", 0xffffffff);
  public CompoundParameter fadeTime = new CompoundParameter("fadeT", 1.0, 0.1, 20.0).setDescription("Fade time in seconds");


  Wavetable sineTable = new SineWavetable(128);
  Wavetable stepTable = new StepWavetable(16);
  Wavetable triangleTable = new TriangleWavetable(128);
  Wavetable stepDecayTable = new StepDecayWavetable(32, 6, 8, true);



  public Flarelet[] flarelets = new Flarelet[MAX_FLARELETS];
  public final BooleanParameter bang = new BooleanParameter("bang", false);

  protected double bangTime = 0;

  public FlareBang(LX lx) {
    super(lx);
    bang.setMode(BooleanParameter.Mode.MOMENTARY);
    bang.addListener((p) -> {
      if (((BooleanParameter)p).isOn()) {
        lx.log("Banged!");
        banged();
      }
    });
    addParameter("bang", bang);
    addParameter("fadeT", fadeTime);
    addParameter("speed", speed);
    addParameter("flrlets", numFlarelets);
    addParameter("fx", fxKnob);
    addParameter("fxDepth", fxDepth);
    addParameter("wave", waveKnob);
    addParameter("width", widthKnob);
    addParameter("cfreq", cosineFreq);
    addParameter("color", colorKnob);
    triangleTable.generateWavetable(1f, 0f);
    stepDecayTable.generateWavetable(1f, 0f);
    resetBlobs(false);
  }

  /**
   * Called when 'bang' is triggered.  We want to start a new Flarelet.
   */
  public void banged() {
    resetBlobs(true);
    bangTime = System.currentTimeMillis();
  }

  public void resetBlobs(boolean enabled) {
    for (int i = 0; i < MAX_FLARELETS; i++) {
      flarelets[i] = new Flarelet();
      // TODO(tracy): Pick an initial random lightBarNum such that we are restricted to a specific fixture.
      int hNum = i / 16;
      int stripNum = i % 16;
      flarelets[i].reset(Topology.getDefaultTopologies(lx).get(hNum + 3), stripNum,0.0f, 0f, true);

      flarelets[i].color = colorKnob.getColor();
      flarelets[i].pos = 6.0f;
      flarelets[i].enabled = enabled;
    }
  }

  /**
   * onActive is called when the pattern starts playing and becomes the active pattern.  Here we re-assigning
   * our speeds to generate some randomness in the speeds.
   */
  @Override
  public void onActive() {
    resetBlobs(false);
  }

  @Override
  public void run(double deltaMs) {
    for (int i = 0; i < numFlarelets.getValuei(); i++) {
      flarelets[i].color = colorKnob.getColor();
      flarelets[i].wavetable = stepDecayTable;
      flarelets[i].fx = fxKnob.getValuei();
      flarelets[i].fxDepth = fxDepth.getValuef();
      flarelets[i].fxFreq = cosineFreq.getValuef();
      flarelets[i].fadeTime = fadeTime.getValuef()*1000f;
      flarelets[i].waveWidth = widthKnob.getValuef();
      flarelets[i].waveOnTop(colors, LXColor.Blend.ADD, -1, true);
      flarelets[i].pos += speed.getValuef() * (float)deltaMs / 1000f;
      if (flarelets[i].pos > flarelets[i].dStrip.vStrip.length()) {
        flarelets[i].pos = -flarelets[i].dStrip.vStrip.length();
      }
    }
  }
}
