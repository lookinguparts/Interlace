package art.lookingup.wavetable;

public class StepDecayWavetable extends Wavetable {
    int width;
    int decay;
    boolean forward;

    public StepDecayWavetable(int numSamples, int width, int decay, boolean forward) {
        super(numSamples);
        this.width = width;
        this.decay = decay;
        this.forward = forward;
    }

    public void generateWavetable(float max, float offset) {
        for (int i = 0; i < decay; i++) {
            samples[i] = offset + (max - offset) * ((float)i / (float)(decay-1));
        }
        for (int i = decay; i < decay + width; i++) {
            samples[i] = (max - offset);
        }
        for (int i = decay + width; i < numSamples; i++) {
            samples[i] = offset;
        }
        samples[numSamples] = samples[0];
        if (!forward) {
           reverse();
        }
    }
}
