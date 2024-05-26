package art.lookingup.wavetable;

public class SineWavetable extends Wavetable {

    public SineWavetable(int numSamples) {
        super(numSamples);
    }

    public void generateWavetable(float max, float offset) {
        for (int i = 0; i < numSamples; i++) {
            samples[i] = offset + (max - offset) * (0.5f * (float) Math.sin(1.5f * Math.PI + 2 * Math.PI * i / numSamples) + 0.5f);
        }
        samples[numSamples] = samples[0];
    }
}
