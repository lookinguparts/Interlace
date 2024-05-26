package art.lookingup.wavetable;

import java.util.Random;

public class PerlinNoise1D {
    private static final int PERMUTATION_SIZE = 256;
    private static final int PERMUTATION_MASK = PERMUTATION_SIZE - 1;
    private static final double SCALE = 1.0 / PERMUTATION_SIZE;

    private final int[] permutation;

    public PerlinNoise1D(long seed) {
        permutation = new int[PERMUTATION_SIZE * 2];
        int[] p = new int[PERMUTATION_SIZE];

        // Initialize permutation array with values from 0 to 255
        for (int i = 0; i < PERMUTATION_SIZE; i++) {
            p[i] = i;
        }

        // Shuffle permutation array using the seed value
        Random random = new Random(seed);
        for (int i = PERMUTATION_SIZE - 1; i > 0; i--) {
            int j = random.nextInt(i + 1);
            int temp = p[i];
            p[i] = p[j];
            p[j] = temp;
        }

        // Duplicate permutation array
        System.arraycopy(p, 0, permutation, 0, PERMUTATION_SIZE);
        System.arraycopy(p, 0, permutation, PERMUTATION_SIZE, PERMUTATION_SIZE);
    }

    public double noise(double x) {
        int xi = (int) Math.floor(x) & PERMUTATION_MASK;
        double xf = x - Math.floor(x);

        double u = fade(xf);

        int a = permutation[xi];
        int b = permutation[xi + 1];

        double x1 = lerp(u, grad(a, xf), grad(b, xf - 1));

        return (x1 + 1) / 2;
    }

    private static double fade(double t) {
        return t * t * t * (t * (t * 6 - 15) + 10);
    }

    private static double lerp(double t, double a, double b) {
        return a + t * (b - a);
    }

    private static double grad(int hash, double x) {
        return (hash & 1) == 0 ? x : -x;
    }
}
