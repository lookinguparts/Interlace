package art.lookingup.wavetable;

public abstract class Wavetable {

    public float[] samples;
    int numSamples;

    // The x position of the wavetable.
    public float pos;


    public Wavetable(int numSamples) {
        samples = new float[numSamples+1];
        this.numSamples = numSamples;
    }
    abstract public void generateWavetable(float max, float offset);


    public void ease(Ease ease) {
        for (int i = 0; i < numSamples; i++) {
            samples[i] = ease.ease(samples[i]);
        }
    }

    public float getSample(float position, float physicalWidth) {
        int index = (int) ((position+physicalWidth/2f - this.pos)/ physicalWidth * numSamples);
        if (index < 0 || index >= numSamples)
            return 0;
        return samples[index];
    }

    void reverse() {
        int n = samples.length;
        for (int i = 0; i < n / 2; i++) {
            float temp = samples[i];
            samples[i] = samples[n - i - 1];
            samples[n - i - 1] = temp;
        }
    }
}
