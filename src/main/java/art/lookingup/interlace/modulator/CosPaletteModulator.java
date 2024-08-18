package art.lookingup.interlace.modulator;

import heronarts.lx.LXCategory;
import heronarts.lx.color.LXColor;
import heronarts.lx.color.LinkedColorParameter;
import heronarts.lx.modulator.LXModulator;
import heronarts.lx.osc.LXOscComponent;
import heronarts.lx.parameter.CompoundParameter;
import heronarts.lx.parameter.DiscreteParameter;
import heronarts.lx.parameter.LXNormalizedParameter;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@LXModulator.Global("CosPalette")
@LXModulator.Device("CosPalette")
@LXCategory(LXCategory.CORE)
public class CosPaletteModulator extends LXModulator implements LXOscComponent, LXNormalizedParameter {

  public final CompoundParameter input =
    new CompoundParameter("Input", 0)
      .setUnits(CompoundParameter.Units.PERCENT_NORMALIZED)
      .setDescription("Input value to the palette");

  public final DiscreteParameter which =
    new DiscreteParameter("Palette", 0, 0, paletteStrings.length)
      .setDescription("Which palette to use");

  public final CompoundParameter red =
    new CompoundParameter("Red", 0.0, 0.0, 1.0)
      .setDescription("Red component of the color");

  public final CompoundParameter green =
    new CompoundParameter("Green", 0.0, 0.0, 1.0)
      .setDescription("Green component of the color");

  public final CompoundParameter blue =
    new CompoundParameter("Blue", 0.0, 0.0, 1.0)
      .setDescription("Blue component of the color");

  public final LinkedColorParameter color =
    new LinkedColorParameter("Color", 0xff000000)
      .setDescription("Color of the palette");

  protected int[] rgb;

  public CosPaletteModulator() {
    this("CosPalette");
  }

  public CosPaletteModulator(String label) {
    super(label);
    addParameter("input", this.input);
    addParameter("red", red);
    addParameter("green", green);
    addParameter("blue", blue);
    addParameter("color", color);
  }

  protected double[] tempRGB = new double[3];

  /**
   * NOTE(tracy): This is not thread-safe.  To reduce garbage collection we have a single tempRGB array
   * that we re-use.
   *
   * @param deltaMs Number of milliseconds to advance by
   * @return
   */
  @Override
  protected double computeValue(double deltaMs) {
    paletteN(input.getValuef(), which.getValuei(), tempRGB);
    red.setValue(tempRGB[0]);
    green.setValue(tempRGB[1]);
    blue.setValue(tempRGB[2]);
    int clr = LXColor.rgb((int)(tempRGB[0]*255), (int)(tempRGB[1]*255), (int)(tempRGB[2]*255));
    color.setColor(clr);
    return input.getValuef();
  }

  @Override
  public LXNormalizedParameter setNormalized(double value) {
    input.setValue(value);
    return input;
  }

  @Override
  public double getNormalized() {
    return getValue();
  }


  public static double[] palette(double t, double[] a, double[] b, double[] c, double[] d) {
    double[] result = new double[3];
    for (int i = 0; i < 3; i++) {
      result[i] = Math.max(0, Math.min(1, a[i] + b[i] * Math.cos(6.28318 * (c[i] * t + d[i]))));
    }
    return result;
  }

  public static void paletteN(double t, int whichPalette, double[] result) {
    initialize();
    Double[][] palette = palettes.get(whichPalette);
    double[] a = {palette[0][0], palette[0][1], palette[0][2]};
    double[] b = {palette[1][0], palette[1][1], palette[1][2]};
    double[] c = {palette[2][0], palette[2][1], palette[2][2]};
    double[] d = {palette[3][0], palette[3][1], palette[3][2]};
    for (int i = 0; i < 3; i++) {
      result[i] = Math.max(0, Math.min(1, a[i] + b[i] * Math.cos(6.28318 * (c[i] * t + d[i]))));
    }
    //return palette(t, a, b, c, d);
  }



  public static String[] paletteStrings = {
    "[[0.5 0.5 0.5][0.5 0.5 0.5][1.0 1.0 1.0][0.263 0.416 0.557]]",
    "[[0.572 0.574 0.518] [0.759 0.171 0.358] [1.022 0.318 0.620] [3.138 5.671 -0.172]]",
    "[[0.846 0.430 0.206] [0.349 0.678 0.651] [0.690 1.319 0.654] [6.205 2.511 3.523]]",
    "[[0.806 0.355 0.693] [0.802 0.464 0.260] [1.514 1.131 1.197] [1.015 0.738 3.202]]",
    "[[0.990 0.520 0.071] [0.063 0.800 0.918] [1.548 0.740 0.062] [1.261 5.091 5.773]]",
    "[[0.481 0.619 0.755] [0.424 0.158 0.810] [3.136 1.650 2.155] [4.963 4.889 4.418]]",
    "[[0.354 -0.322 0.578] [0.321 0.861 0.394] [1.197 1.258 0.758] [0.788 0.368 0.434]]",
    "[[0.768 0.748 0.828] [0.798 0.108 1.048] [3.108 0.798 2.008] [2.808 1.998 4.544]]",
    "[[0.472 0.658 0.577] [0.837 0.606 0.653] [1.025 1.508 0.407] [2.753 4.488 2.828]]",
    "[[0.572 0.574 0.518] [0.759 0.171 0.358] [1.022 0.318 0.620] [3.138 5.671 -0.172]]",
    "[[0.172 0.854 0.888] [0.406 0.606 0.068] [0.055 1.268 0.209] [4.175 2.384 2.380]]",
    "[[0.992 0.070 0.258] [0.880 0.265 0.232] [0.677 0.325 0.802] [4.825 4.254 4.444]]",
    "[[0.774 0.747 0.103] [0.049 0.381 0.939] [1.283 0.442 0.834] [5.031 4.499 2.707]]",
    "[[0.774 0.747 0.103] [-0.592 0.381 0.918] [1.000 2.028 0.834] [3.138 4.499 2.518]]",
    "[[0.221 0.244 0.811] [0.468 0.998 0.518] [1.988 0.637 1.003] [1.481 2.788 3.864]]",
    "[[0.028 1.004 0.417] [0.971 0.737 0.114] [2.338 0.290 0.360] [5.994 4.342 4.865]]",
    "[[0.028 0.918 0.558] [0.971 0.737 0.114] [2.298 2.518 0.360] [5.994 4.342 4.865]]",
    "[[0.410 0.681 1.098] [0.510 0.975 0.478] [1.483 2.918 2.208] [0.942 0.462 1.724]]",
    "[[0.638 0.148 0.153] [0.285 0.693 0.338] [0.678 1.808 2.028] [4.993 3.681 5.919]]",
    "[[0.410 -0.141 0.317] [0.834 0.877 0.244] [1.366 0.947 1.530] [0.150 4.830 5.551]]",
    "[[0.335 0.297 0.094] [0.924 0.647 0.219] [1.295 0.859 0.341] [4.705 5.682 2.882]]"
  };

  /**
   * We should parse our lines of strings into a vector of double, double.
   * @param input
   */
  public static List<Double[][]> palettes = new ArrayList<>();
  public static boolean initialized = false;

  public static void initialize() {
    if (initialized) {
      return;
    }
    initialized = true;
    for (String s : paletteStrings) {
      palettes.add(parseInput(s));
    }
  }

  public static Double[][] parseInput(String input) {
    Pattern pattern = Pattern.compile("\\[([\\d.-]+)\\s+([\\d.-]+)\\s+([\\d.-]+)\\]");
    Matcher matcher = pattern.matcher(input);

    Double[][] result = new Double[4][3];
    int index = 0;

    while (matcher.find() && index < 4) {
      for (int i = 0; i < 3; i++) {
        result[index][i] = Double.parseDouble(matcher.group(i + 1));
      }
      index++;
    }

    return (index == 4) ? result : null;
  }
}