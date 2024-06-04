package art.lookingup.interlace;

import art.lookingup.vstrip.Flarelet;
import art.lookingup.vstrip.VTopology;
import art.lookingup.wavetable.*;
import heronarts.lx.LX;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

/**
 * Class for generating and managing flarelets.  This class is topology aware.  Each generator should be
 * mapped to a specific Hyperboloid typology.  The various flarelet parameters should be set on the FlareGen
 * instance and then flarelets can be generated using the startFlarelet methods.
 *
 * TODO(tracy): We should recycle Flarelet instances to reduce excessive garbage collection.
 */
public class FlareletGen {

  protected VTopology vTop;

  // Parameters for controlling the appearance of the flarelets.
  public float fadeTime = 1000.0f;  // milliseconds
  public float speed  = 100.0f;
  public float intensity = 1.0f;
  public int fx = 0;
  public float fxDepth = 0.1f;
  public float fxFreq = 0.1f;
  public int wave = -1;
  public float waveWidth = 12f;

  public int color = 0xffffffff;
  public int swatch = -1;

  public int currentStrip = 0;

  public List<Flarelet> runningFlarelets = new ArrayList<>();


  public Wavetable wavetable;

  static public Wavetable[] sharedWavetables = new Wavetable[6];
  static public boolean wavetablesBuilt = false;


  public LX lx;

  public FlareletGen(LX lx, VTopology vTop) {
    this.vTop = vTop;
    this.lx = lx;
    int samples = 128;
    buildWavetables(samples, samples/2, 1.0f, true);
  }

  /**
   * Since wavetables are static, we should prebuild them and then just share the instances here.  This will be more
   * efficient with large numbers of flarelets.
   */
  static public void buildWavetables(int samples, int width, float pscale, boolean forward) {
    if (wavetablesBuilt) {
      return;
    }
    wavetablesBuilt = true;
    Wavetable wavetable;

    wavetable = new SineWavetable(samples);
    wavetable.generateWavetable(1f, 0f);
    sharedWavetables[0] = wavetable;

    wavetable = new TriangleWavetable(samples);
    wavetable.generateWavetable(1f, 0f);
    sharedWavetables[1] = wavetable;

    wavetable = new StepWavetable(samples);
    wavetable.generateWavetable(1f, 0f);
    sharedWavetables[2] = wavetable;

    wavetable = new StepDecayWavetable(samples, width, samples - width, forward);
    wavetable.generateWavetable(1f, 0f);
    sharedWavetables[3] = wavetable;

    wavetable = new RandomWavetable(samples);
    wavetable.generateWavetable(1f, 0f);
    sharedWavetables[4] = wavetable;

    wavetable = new PerlinWavetable(samples, 1, pscale);
    wavetable.generateWavetable(1f, 0f);
    sharedWavetables[5] = wavetable;
  }

  /**
   * Starts a flarelet on the current strip and increments the strip number for the next flarelet.
   * @return
   */
  public Flarelet startFlareletIncrement() {
    Flarelet flarelet = startFlarelet(currentStrip);
    currentStrip = (currentStrip + 1) % vTop.strips.size();
    runningFlarelets.add(flarelet);
    return flarelet;
  }

  /**
   * Starts a flarelet on the current strip and decrements the strip number for the next flarelet.
   * @return
   */
  public Flarelet startFlareletDecrement() {
    Flarelet flarelet = startFlarelet(currentStrip);
    currentStrip = (currentStrip - 1) % vTop.strips.size();
    if (currentStrip < 0) {
      currentStrip = vTop.strips.size() - 1;
    }
    runningFlarelets.add(flarelet);
    return flarelet;
  }

  /**
   * Starts the flarelet on a random strip.
   * @return
   */
  public Flarelet startFlareletRandom() {
    int stripNum = (int) Math.floor(Math.random() * vTop.strips.size());
    Flarelet flarelet = startFlarelet(stripNum);
    runningFlarelets.add(flarelet);
    return flarelet;
  }

  /**
   * Starts a flarelet on the specified strip.  The various flarelet parameters should be set on this
   * object before calling this method.
   * @param stripNum
   * @return
   */
  public Flarelet startFlarelet(int stripNum) {
    Flarelet flarelet = new Flarelet();
    flarelet.vTop = this.vTop;
    flarelet.startTime = System.currentTimeMillis();
    flarelet.fadeTime = fadeTime;
    flarelet.intensity = intensity;
    flarelet.fx = fx;
    flarelet.fxDepth = fxDepth;
    flarelet.fxFreq = fxFreq;
    flarelet.color = color;
    flarelet.swatch = swatch;
    if (wave == -1) {
      //lx.log("wave==-1, using default wavetable. waveWidth="  + waveWidth + " stripNum=" + stripNum);
      //setWavetable(1, 128, 0, true, 1.0f);
      setWavetable(1);
    } else {
      //lx.log("wave=" + wave + " waveWidth=" + waveWidth);
      //setWavetable(wave, 128, 64, true, 1.0f);
      setWavetable(wave);
    }
    flarelet.wavetable = wavetable;
    flarelet.waveWidth = waveWidth;
    flarelet.reset(vTop, stripNum, -waveWidth, 0f, true);
    flarelet.speed = speed;
    flarelet.lx = lx;
    flarelet.enabled = true;
    return flarelet;
  }

  /**
   * Processes the list of running flarelets and removes any that are no longer renderable.
   * TODO(tracy): These should be recycled instead of being garbage collected.
   */
  public void disposeExpiredFlarelets() {
    Iterator<Flarelet> it = runningFlarelets.iterator();
    while (it.hasNext()) {
      Flarelet flarelet = it.next();
      if (!flarelet.isRenderable()) {
        it.remove();
      }
    }
  }

  /**
   * Sets the current wavetable based on the wave parameter and selects from the pre-built wavetables.  This is
   * much more efficient for many flarelets.
   * @param wave
   */
  public void setWavetable(int wave) {
    wavetable = sharedWavetables[wave];
    this.wave = wave;
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
    wavetable.generateWavetable(1f, 0f);
  }
}
