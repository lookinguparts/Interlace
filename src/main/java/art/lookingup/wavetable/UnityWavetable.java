package art.lookingup.wavetable;

public class UnityWavetable extends Wavetable {

  public UnityWavetable(int numSamples) {
    super(numSamples);
  }

  public void generateWavetable(float max, float offset) {
    for (int i = 0; i < numSamples; i++) {
      samples[i] = max;
    }
    samples[numSamples] = samples[0];
  }
}
