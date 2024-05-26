package art.lookingup.wavetable;

public class StepWavetable extends Wavetable {
    public StepWavetable(int numSamples) {
        super(numSamples);
    }

    public void generateWavetable(float max, float offset) {
        for (int i = 0; i < numSamples; i++) {
            samples[i] = offset + (float) (i < numSamples / 2 ? (max-offset) : 0);
        }
        samples[numSamples] = samples[0];
    }
}
