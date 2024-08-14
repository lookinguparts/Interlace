package art.lookingup.wavetable;

public class BeveledUnityWavetable extends Wavetable {

  private int bevelSize;

  public BeveledUnityWavetable(int numSamples) {
    this(numSamples, 16);
  }

  public BeveledUnityWavetable(int numSamples, int bevelSize) {
    super(numSamples);

    this.bevelSize = bevelSize;
  }

  public int getBevelSize() {
    return bevelSize;
  }
  public synchronized BeveledUnityWavetable setBevelSize(int bevelSize) {
    this.bevelSize = bevelSize;

    generateWavetable(1f, 0f);
    return this;
  }

  public synchronized void generateWavetable(float max, float offset) {
    generateWavetableSinExp(max, offset, 0.8);
  }
  public synchronized void generateWavetableLin(float max, float offset) {
    if (numSamples < bevelSize * 2) {
      for (int i = 0; i < numSamples; i++) {
        samples[i] = max;
      }
    }
    else {
      for (int i = 0; i < bevelSize; i++) {
        samples[i] = max * (i + 1) / (float)(bevelSize + 1);
      }
      for (int i = numSamples - bevelSize; i < numSamples; i++) {
        float a = (numSamples - i - 1);
        samples[i] = max * (a + 1) / (float)(bevelSize + 1);
      }

      for (int i = bevelSize; i < numSamples - bevelSize; i++) {
        samples[i] = max;
      }
    }

    samples[numSamples] = samples[0];
  }
  public synchronized void generateWavetableSinExp(float max, float offset, double p) {
    if (numSamples < bevelSize * 2) {
      for (int i = 0; i < numSamples; i++) {
        samples[i] = max;
      }
    }
    else {
      for (int i = 0; i < bevelSize; i++) {
        samples[i] = max * (float)Math.sin(Math.PI * Math.pow((i + 1) / (float)(bevelSize + 1), p) / 2);
        //samples[i] = max * (float)Math.sin(Math.PI * (i + 1) / (float)(bevelSize + 1) / 2);
      }
      for (int i = numSamples - bevelSize; i < numSamples; i++) {
        float a = (numSamples - i - 1);
        samples[i] = max * (float)Math.sin(Math.PI * Math.pow((a + 1) / (float)(bevelSize + 1), p) / 2);
        //samples[i] = max * (float)Math.sin(Math.PI * (a + 1) / (float)(bevelSize + 1) / 2);
      }

      for (int i = bevelSize; i < numSamples - bevelSize; i++) {
        samples[i] = max;
      }
    }

    samples[numSamples] = samples[0];
  }
}
