package art.lookingup.wavetable;

public class TriangleWavetable extends Wavetable {

    public TriangleWavetable(int numSamples) {
        super(numSamples);
    }

    public void generateWavetable(float max, float offset) {
        float half = numSamples / 2;
        for (int i = 0; i < numSamples; i++) {
            if (i <= half)
                samples[i] = (max-offset) * (float)i / half;
            else
                samples[i] = (max-offset) * (1f - (float)(i - half) / half);
            samples[i] += offset;
        }
        samples[numSamples] = samples[0];
    }
}
