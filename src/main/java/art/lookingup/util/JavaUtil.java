package art.lookingup.util;

import java.awt.geom.AffineTransform;
import java.awt.image.AffineTransformOp;
import java.awt.image.BufferedImage;

public class JavaUtil {

  static public BufferedImage resize(BufferedImage before, int newW, int newH) {
    int w = before.getWidth();
    int h = before.getHeight();
    float sx = (float) newW / (float)w;
    float sy = (float) newH / (float)h;
    BufferedImage after = new BufferedImage(w, h, BufferedImage.TYPE_INT_ARGB);
    AffineTransform at = new AffineTransform();
    at.scale(sx, sy);
    AffineTransformOp scaleOp =
      new AffineTransformOp(at, AffineTransformOp.TYPE_BILINEAR);
    after = scaleOp.filter(before, after);
    return after;
  }
}
