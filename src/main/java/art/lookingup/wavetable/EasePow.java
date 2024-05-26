package art.lookingup.wavetable;

public class EasePow extends Ease {
   public float exponent;

    public EasePow(float exponent) {
        this.exponent = exponent;
    }

    public float ease(float x) {
        return (float) Math.pow(x, exponent);
    }
}
