package art.lookingup.interlace.patterns;

import art.lookingup.render2d.Render2D;
import heronarts.lx.LX;
import heronarts.lx.model.LXPoint;
import heronarts.lx.parameter.CompoundParameter;
import heronarts.lx.pattern.LXPattern;

import java.awt.*;
import java.awt.image.BufferedImage;

abstract public class Render2DBase extends LXPattern {
  protected BufferedImage renderImage;
  protected Graphics2D graphics;

  protected int width = 512;
  protected int height = 512;

  protected CompoundParameter theta = new CompoundParameter("theta", 0, 0, 1).setDescription("Angular offset");

  public Render2DBase(LX lx) {
    super(lx);
    renderImage = new BufferedImage(width, height, BufferedImage.TYPE_INT_RGB);
    graphics = renderImage.createGraphics();
    addParameter("theta", theta);
  }

  abstract protected void renderFrame(double deltaMs);

  @Override
  protected void run(double deltaMs) {
    // Clear the image to black
    // TODO(tracy): support fading here if possible.  We would want the fill color to have some percentage of
    // alpha.
    graphics.setColor(Color.BLACK);
    graphics.fillRect(0, 0, width, height);

    renderFrame(deltaMs);

    float st[] = new float[2];
    for (LXPoint p : model.points) {
      Render2D.mapCylinderSTCoords(theta.getValuef(), p.xn, p.yn, p.zn, st);
      int color = Render2D.pixelFromBufferedImageST(renderImage, st[0], st[1]);
      colors[p.index] = color;
    }
  }
}
