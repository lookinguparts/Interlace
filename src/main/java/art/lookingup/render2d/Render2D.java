package art.lookingup.render2d;

import java.awt.image.BufferedImage;

public class Render2D {
  static public void mapCylinderSTCoords(float thetaOffset, float x, float y, float z, float[] st) {
    x = (x - 0.5f) * 2.0f;
    z = (z - 0.5f) * 2.0f;
    thetaOffset = thetaOffset - (int)thetaOffset;
    float theta = (float)Math.atan2(z, x);
    theta = theta + thetaOffset * 2.0f * (float)Math.PI;
    if (theta > 2.0f * (float)Math.PI) {
      theta = theta - 2.0f * (float)Math.PI;
    }
    if (theta < 0) {
      theta = theta + 2.0f * (float)Math.PI;
    }
    theta = theta/(2.0f * (float)Math.PI);
    st[0] = theta;
    st[1] = y;
  }

  static public int pixelFromBufferedImageST(BufferedImage image, float s, float t) {
    return image.getRGB((int)(s*(image.getWidth()-1)), (int)(t * (image.getHeight()-1)));
  }
}
