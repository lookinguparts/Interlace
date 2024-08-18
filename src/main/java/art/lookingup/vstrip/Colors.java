package art.lookingup.vstrip;

import art.lookingup.util.EaseUtil;
import heronarts.lx.LX;
import heronarts.lx.color.LXColor;
import heronarts.lx.color.LXDynamicColor;
import heronarts.lx.color.LXSwatch;

import java.awt.*;
import java.util.Random;

public final class Colors {
  static final Random rand = new Random();

  private Colors() {
  }

  /**
   * Chooses a random n-bit color. If {@code bits} is not in the range 1-8 then 8 will be assumed.
   *
   * @param bits the top number of random bits in an 8-bit color
   */
  public static int randomColor(int bits) {
    if (bits < 1 || 8 < bits) {
      bits = 8;
    }

    // Choose from an n-bit set of random colors
    int c = 0xff;
    for (int i = 0; i < 3; i++) {
      c = (c << 8) | (rand.nextInt(1 << bits) << (8 - bits));
    }
    return c;
  }

  /**
   * Returns the red part of a 32-bit RGBA color.
   */
  public static int red(int color) {
    return (color >> 16) & 0xff;
  }

  /**
   * Returns the green part of a 32-bit RGBA color.
   */
  public static int green(int color) {
    return (color >> 8) & 0xff;
  }

  /**
   * Returns the blue part of a 32-bit RGBA color.
   */
  public static int blue(int color) {
    return color & 0xff;
  }

  /**
   * Returns the alpha part of a 32-bit RGBA color.
   */
  public static int alpha(int color) {
    return (color >> 24) & 0xff;
  }

  /**
   * Returns a color constructed from the three components. The alpha component is set to 255.
   */
  public static int rgb(int r, int g, int b) {
    return 0xff000000 | ((r & 0xff) << 16) | ((g & 0xff) << 8) | (b & 0xff);
  }

  public static int rgba(int r, int g, int b, int a) {
    return ((a & 0xff) << 24) | ((r & 0xff) << 16) | ((g & 0xff) << 8) | (b & 0xff);
  }

  /**
   * Returns a color constructed from the three components. The alpha component is set to 255.
   */
  public static int hsb(float h, float s, float b) {
    return Color.HSBtoRGB(h, s, b);
  }

  public static float[] RGBtoHSB(int rgb, float[] hsb) {
    int r = (rgb & LXColor.R_MASK) >> LXColor.R_SHIFT;
    int g = (rgb & LXColor.G_MASK) >> LXColor.G_SHIFT;
    int b = rgb & LXColor.B_MASK;
    return Color.RGBtoHSB(r, g, b, hsb);
  }

  public static void colorToRGBArrayNormalized(int color, float[] rgb) {
    rgb[0] = (float)red(color) / 255.0f;
    rgb[1] = (float)green(color) / 255.0f;
    rgb[2] = (float)blue(color) / 255.0f;
  }

  public static int HSBtoRGB(float[] hsb) {
    return Color.HSBtoRGB(hsb[0], hsb[1], hsb[2]);
  }

  public static double quantize(double value, int steps) {
    if (value < 0.0 || value > 1.0)
      return value;

    if (steps < 1)
      return value;

    double stepSize = 1.0 / steps;
    double quantizedValue = Math.round(value / stepSize) * stepSize;
    return quantizedValue;
  }

  /**
   * Given a range from 0 to one, extract a color out of a swatch.  0 is the left most color of
   * the palette and 1 is the rightmost.  A swatch returns a DynamicColor, which can already lerp
   * between the base color and the next color.  So here, we just need to remap 0 to 1 so that
   * it pulls the correct DynamicColor from the correct index of the Swatch and then re-parameterize
   * our 0 to 1 value.  For example with 4 colors, 0.125 is halfway between the first two colors.
   * With 4 colors, we have 0.250 per color. So colorIndexRange = 1.0 / NumSwatchColors;
   * (int)(0.125 / colorIndexRange) -> 0, so we use index 0 on the swatch.  Now to reparameterize
   * we just divide 0.125/0.250 = 0.5;  So we can just LXColor.lerp between DynamicColor.primary and DynamicColor.secondary;
   */
  static public int getParameterizedPaletteColor(LX lx, int swatchIndex, float t, EaseUtil ease) {
    if (swatchIndex >= lx.engine.palette.swatches.size())
      return 0;
    LXSwatch swatch = lx.engine.palette.swatches.get(swatchIndex);
    if (swatch.colors.size() == 0)
      return LXColor.BLACK;
    if (swatch.colors.size() == 1)
      return swatch.getColor(0).primary.getColor();
    float colorIndexRange = 1.0f / (float)((swatch.colors.size()-1));
    int colorIndex = (int) (t / colorIndexRange);
    if (t < 0f)
      t = 0f;

    int nextIndex = colorIndex + 1;
    if (nextIndex >= swatch.colors.size())
      nextIndex -= 1;
    float distanceInRange = (t - colorIndex * colorIndexRange)/colorIndexRange;
    if (ease!= null) distanceInRange = ease.ease(distanceInRange);
    LXDynamicColor color = swatch.getColor(colorIndex);
    LXDynamicColor nextColor = swatch.getColor(nextIndex);
    return LXColor.lerp(color.primary.getColor(), nextColor.primary.getColor(), distanceInRange);
  }

  static public int getQuantizedPaletteColor(LX lx, int swatchIndex, float t, EaseUtil ease) {
    if (swatchIndex >= lx.engine.palette.swatches.size())
      return 0;
    LXSwatch swatch = lx.engine.palette.swatches.get(swatchIndex);
    if (swatch.colors.size() == 0)
      return LXColor.BLACK;
    if (swatch.colors.size() == 1)
      return swatch.getColor(0).primary.getColor();
    t = (float)quantize(t, swatch.colors.size());
    float stepSize = 1.0f / (float)swatch.colors.size();
    int colorIndex = (int)(t / stepSize);
    LXDynamicColor color = swatch.getColor(colorIndex);
    return color.getColor();
  }
}
