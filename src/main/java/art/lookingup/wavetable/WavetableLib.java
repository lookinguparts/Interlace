package art.lookingup.wavetable;

public class WavetableLib {

  static public boolean wavetablesBuilt = false;
  static public Wavetable[] sharedWavetables = new Wavetable[10];

  static private void buildWavetables(int samples, int width, float pscale, boolean forward) {
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

    wavetable = new PerlinWavetable(samples, 1, pscale * 20f);
    wavetable.generateWavetable(1f, 0.6f);
    sharedWavetables[5] = wavetable;

    wavetable = new StepDecayWavetable(samples, 2, samples - 2, forward);
    wavetable.generateWavetable(1f, 0f);
    wavetable.multiply(wavetable);
    sharedWavetables[6] = wavetable;

    wavetable = new UnityWavetable(samples);
    wavetable.generateWavetable(1f, 0f);
    sharedWavetables[7] = wavetable;

    wavetable = new BeveledUnityWavetable(samples, samples/8);
    wavetable.generateWavetable(1f, 0f);
    sharedWavetables[8] = wavetable;

    wavetable = new TriangleWavetable(samples);
    wavetable.generateWavetable(1f, 0f);
    wavetable.multiply(wavetable);
    sharedWavetables[9] = wavetable;
  }

  static public void buildDefaultTables() {
    int samples = 1024;
    buildWavetables(samples, samples/2, 1.0f, true);
  }

  static public Wavetable getLibraryWavetable(int which) {
    buildDefaultTables();
    if (which >= sharedWavetables.length) {
      which = 0;
    }
    return sharedWavetables[which];
  }

  static public int countWavetables() {
    buildDefaultTables();
    return sharedWavetables.length;
  }
}
