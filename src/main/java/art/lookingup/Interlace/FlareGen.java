package art.lookingup.Interlace;

import art.lookingup.vstrip.Flarelet;
import art.lookingup.vstrip.VTopology;
import art.lookingup.wavetable.*;

/**
 * Class for generating and managing flarelets.  This class is topology aware.  Each generator should be
 * mapped to a specific Hyperboloid typology.
 */
public class FlareGen {

  protected VTopology vTop;

  /*
  addParameter("fadeT", fadeTime);
    addParameter("speed", speed);
    addParameter("flrlets", numFlarelets);
    addParameter("fx", fxKnob);
    addParameter("fxDepth", fxDepth);
    addParameter("wave", waveKnob);
    addParameter("width", widthKnob);
    addParameter("cfreq", cosineFreq);
    addParameter("color", colorKnob);
   */

  // Parameters for controlling the appearance of the flarelets.
  public float fadeTime = 1.0f;  // seconds
  public float speed  = 100.0f;
  public float intensity = 1.0f;
  public int fx = 0;
  public float fxDepth = 0.1f;
  public float fxFreq = 0.1f;
  public int wave = -1;

  public int color = 0xffffffff;
  public int palette = -1;

  Wavetable wavetable;

  public FlareGen(VTopology vTop) {
    this.vTop = vTop;
  }

  public Flarelet startFlarelet() {
    Flarelet flarelet = new Flarelet();
    flarelet.vTop = this.vTop;
    flarelet.startTime = System.currentTimeMillis();
    flarelet.fadeTime = fadeTime;
    flarelet.intensity = intensity;
    flarelet.fx = fx;
    flarelet.fxDepth = fxDepth;
    flarelet.color = color;
    flarelet.palette = palette;
    if (wave == -1) {
      setWavetable(1, 128, 0, true, 1.0f);
    }
    flarelet.wavetable = wavetable;
    return flarelet;
  }

  /**
   * Builds the wavetable that we will use for rendering the flarelet.  For now, all parameters for all types of
   * wavetables need to be passed in each time even if they are not used for a particular wavetable.  This is mostly
   * just to keep the api smaller for now and those values should always be available via knobs in the pattern even
   * if they are not used for a particular wavetable.
   */
  public void setWavetable(int wave, int samples, int width, boolean forward, float scale) {
    // 0 = sine, 1 = triangle, 2 = step, 3 = step decay, 4 = random, 5 = perlin
    switch (wave) {
      case 0:
        wavetable = new SineWavetable(samples);
        break;
      case 1:
        wavetable = new TriangleWavetable(samples);
        break;
      case 2:
        wavetable = new StepWavetable(samples);
        break;
      case 3:
        wavetable = new StepDecayWavetable(samples, width, samples - width, forward);
        break;
      case 4:
        wavetable = new RandomWavetable(samples);
        break;
      case 5:
        wavetable = new PerlinWavetable(samples, 1, 1f);
        break;
    }
  }

}
