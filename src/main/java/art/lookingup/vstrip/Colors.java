package art.lookingup.vstrip;

import heronarts.lx.color.LXColor;

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

  public static int HSBtoRGB(float[] hsb) {
    return Color.HSBtoRGB(hsb[0], hsb[1], hsb[2]);
  }
}
