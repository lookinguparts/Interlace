package art.lookingup.wavetable;

public class PerlinWavetable extends Wavetable {

    long seed;
    PerlinNoise1D perlinNoise;
    float scale;

    public PerlinWavetable(int numSamples, long seed, float scale) {
        super(numSamples);
        this.seed = seed;
        perlinNoise = new PerlinNoise1D(seed);
        this.scale = scale;
    }

    public void generateWavetable(float max, float offset) {
        for (int i = 0; i < numSamples; i++) {
            float x = (float)i / (float)numSamples;
            x = x * scale;
            samples[i] = offset + (max-offset) * (float)perlinNoise.noise(x);
        }
        samples[numSamples] = samples[0];
    }
}
