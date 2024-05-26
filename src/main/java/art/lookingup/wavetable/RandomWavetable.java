package art.lookingup.wavetable;

public class RandomWavetable extends Wavetable {

    public RandomWavetable(int numSamples) {
        super(numSamples);
    }

    public void generateWavetable(float max, float offset) {
        for (int i = 0; i < numSamples; i++) {
            samples[i] = offset + (max-offset) * (float)Math.random();
        }
        samples[numSamples] = samples[0];
    }
}
