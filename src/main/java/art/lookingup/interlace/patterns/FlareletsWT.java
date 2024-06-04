package art.lookingup.interlace.patterns;

import art.lookingup.interlace.Topology;
import art.lookingup.vstrip.Flarelet;
import art.lookingup.wavetable.SineWavetable;
import art.lookingup.wavetable.StepWavetable;
import art.lookingup.wavetable.TriangleWavetable;
import art.lookingup.wavetable.Wavetable;
import heronarts.lx.LX;
import heronarts.lx.color.ColorParameter;
import heronarts.lx.color.LXColor;
import heronarts.lx.parameter.CompoundParameter;
import heronarts.lx.parameter.DiscreteParameter;
import heronarts.lx.pattern.LXPattern;

public class FlareletsWT extends LXPattern {
  public static final int MAX_FLARELETS = 48;

  public CompoundParameter slope = new CompoundParameter("slope", 1.0, 0.001, 30.0);
  public CompoundParameter maxValue = new CompoundParameter("maxv", 1.0, 0.0, 1.0);
  public CompoundParameter speed = new CompoundParameter("speed", 40.0, 0.0, 1000.0);
  public CompoundParameter randSpeed = new CompoundParameter("randspd", 1.0, 0.0, 5.0);
  public DiscreteParameter numFlarelets = new DiscreteParameter("flrlets", 10, 1, MAX_FLARELETS+1);
  public DiscreteParameter nextStripKnob = new DiscreteParameter("nxtStrip", 0, 0, 4);
  public DiscreteParameter fxKnob = new DiscreteParameter("fx", 0, 0, 3).setDescription("0=none 1=sparkle 2=cosine");
  public CompoundParameter fxDepth = new CompoundParameter("fxDepth", 1.0f, 0.1f, 1.0f);
  public DiscreteParameter waveKnob = new DiscreteParameter("wave", 0, 0, 3).setDescription("Waveform type");
  public CompoundParameter widthKnob = new CompoundParameter("width", 0.1f, 0.0f, 120.0f).setDescription("Square wave width");
  public CompoundParameter cosineFreq = new CompoundParameter("cfreq", 1.0, 1.0, 400.0);
  public ColorParameter colorKnob = new ColorParameter("color", 0xffffffff);
  public DiscreteParameter swatch = new DiscreteParameter("swatch", -1, -1, 20);


  Wavetable sineTable = new SineWavetable(128);
  Wavetable stepTable = new StepWavetable(16);
  Wavetable triangleTable = new TriangleWavetable(128);



  public Flarelet[] flarelets = new Flarelet[MAX_FLARELETS];

  public FlareletsWT(LX lx) {
    super(lx);
    addParameter("slope", slope);
    addParameter("maxv", maxValue);
    addParameter("speed", speed);
    addParameter("flrlets", numFlarelets);
    addParameter("randspd", randSpeed);
    addParameter("nextStrip", nextStripKnob);
    addParameter("fx", fxKnob);
    addParameter("fxDepth", fxDepth);
    addParameter("wave", waveKnob);
    addParameter("width", widthKnob);
    addParameter("cfreq", cosineFreq);
    addParameter("color", colorKnob);
    addParameter("swatch", swatch);
    triangleTable.generateWavetable(1f, 0f);
    resetBlobs();
  }

  public void resetBlobs() {

    for (int i = 0; i < MAX_FLARELETS; i++) {
      flarelets[i] = new Flarelet();
      // TODO(tracy): Pick an initial random lightBarNum such that we are restricted to a specific fixture.
      int hNum = i / 16;
      int stripNum = i % 16;
      flarelets[i].reset(Topology.getDefaultTopologies(lx).get(hNum), stripNum,0.0f, randSpeed.getValuef(), true);

      flarelets[i].color = colorKnob.getColor();
      flarelets[i].pos = 6.0f;
      flarelets[i].enabled = true;
    }
  }

  /**
   * onActive is called when the pattern starts playing and becomes the active pattern.  Here we re-assigning
   * our speeds to generate some randomness in the speeds.
   */
  @Override
  public void onActive() {
    resetBlobs();
  }

  @Override
  public void run(double deltaMs) {
    for (int i = 0; i < numFlarelets.getValuei(); i++) {
      flarelets[i].color = colorKnob.getColor();
      flarelets[i].wavetable = triangleTable;
      flarelets[i].fx = fxKnob.getValuei();
      flarelets[i].fxDepth = fxDepth.getValuef();
      flarelets[i].fxFreq = cosineFreq.getValuef();
      flarelets[i].waveWidth = widthKnob.getValuef();
      flarelets[i].swatch = swatch.getValuei();
      flarelets[i].waveOnTop(colors, LXColor.Blend.ADD, -1);
      flarelets[i].pos += speed.getValuef() * (float)deltaMs / 1000f;
      if (flarelets[i].pos > flarelets[i].dStrip.vStrip.length()) {
        flarelets[i].pos = -flarelets[i].dStrip.vStrip.length();
      }
    }
  }
}
