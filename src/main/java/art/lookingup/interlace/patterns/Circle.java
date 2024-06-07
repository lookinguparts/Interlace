package art.lookingup.interlace.patterns;

import heronarts.lx.LX;
import heronarts.lx.LXCategory;
import heronarts.lx.LXComponentName;
import heronarts.lx.parameter.CompoundParameter;

import java.awt.*;

@LXCategory("Form")
@LXComponentName("Circle")
public class Circle extends Render2DBase {
  protected CompoundParameter radius = new CompoundParameter("radius", 20, 1, 300);

  public Circle(LX lx) {
    super(lx);
    addParameter("radius", radius);
  }

  @Override
  protected void renderFrame(double deltaMs) {
    // Circle parameters
    int radius = (int)this.radius.getValuef();
    int centerX = width / 2;
    int centerY = height / 2;

    // Draw the circle
    graphics.setColor(Color.WHITE); // Set the color for the circle
    graphics.fillOval(centerX - radius, centerY - radius, 2 * radius, 2 * radius);
  }
}
