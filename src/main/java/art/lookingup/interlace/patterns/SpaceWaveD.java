package art.lookingup.interlace.patterns;

import art.lookingup.interlace.modulator.CosPaletteModulator;
import art.lookingup.vstrip.Point3D;
import art.lookingup.wavetable.*;
import heronarts.lx.LX;
import heronarts.lx.color.LXColor;
import heronarts.lx.model.LXPoint;
import heronarts.lx.parameter.CompoundParameter;
import heronarts.lx.parameter.DiscreteParameter;
import heronarts.lx.pattern.LXPattern;

/**
 * Given a direction vector and a position in space, move the plane along the direction vector.
 * Use the distance from a point to the plane to render a wavetable.
 */
public class SpaceWaveD extends LXPattern {

  protected CompoundParameter dirX = new CompoundParameter("dirX", 0, -1, 1)
    .setDescription("X component of direction vector");
  protected CompoundParameter dirY = new CompoundParameter("dirY", 1, -1, 1)
    .setDescription("Y component of direction vector");
  protected CompoundParameter dirZ = new CompoundParameter("dirZ", 0, -1, 1)
    .setDescription("Z component of direction vector");
  protected CompoundParameter speed = new CompoundParameter("speed", 1, 0, 10)
    .setDescription("Speed of plane movement");
  protected CompoundParameter width = new CompoundParameter("width", 0.2, 0, 2)
    .setDescription("Width of the wave");

  protected CompoundParameter planePos = new CompoundParameter("pos", 0, -2, 2)
    .setDescription("Position of the plane along the direction vector relative to origin.");

  protected DiscreteParameter wave = new DiscreteParameter("wave", 0, 0, WavetableLib.countWavetables())
    .setDescription("Which wave to use");

  protected CompoundParameter paletteDensity = new CompoundParameter("pald", 1, 0, 5)
    .setDescription("Density of the palette");
  protected DiscreteParameter whichPal = new DiscreteParameter("pal", 0, 0, CosPaletteModulator.paletteStrings.length)
    .setDescription("Which palette to use");

  protected CompoundParameter brt = new CompoundParameter("brt", 0.01, 0, 1)
    .setDescription("Brightness of the wave");

  protected CompoundParameter pow = new CompoundParameter("pw", 1, 0, 5)
    .setDescription("Pow of the value");

  Point3D normalVector = new Point3D(0, 1, 0);
  Point3D planePoint = new Point3D(0, 0, 0);
  Point3D lightPoint = new Point3D(0, 0, 0);

  public SpaceWaveD(LX lx) {
    super(lx);
    addParameter("dirX", dirX);
    addParameter("dirY", dirY);
    addParameter("dirZ", dirZ);
    addParameter("speed", speed);
    addParameter("width", width);
    addParameter("pos", planePos);
    addParameter("pal", whichPal);
    addParameter("pald", paletteDensity);
    addParameter("pw", pow);
    addParameter("brt", brt);
    addParameter("wave", wave);
  }

  protected double[] tempRGB = new double[3];

  public void run(double deltaMs) {
    normalVector.x = dirX.getValuef();
    normalVector.y = dirY.getValuef();
    normalVector.z = dirZ.getValuef();
    if (normalVector.length() < 0.01f) {
      return;
    }
    normalVector.normalize();
    planePoint.x = normalVector.x * planePos.getValuef();
    planePoint.y = normalVector.y * planePos.getValuef();
    planePoint.z = normalVector.z * planePos.getValuef();
    for (LXPoint p : model.points) {
      lightPoint.x = p.xn;
      lightPoint.y = p.yn;
      lightPoint.z = p.zn;
      double distance = PointToPlaneDistance.distanceFromPointToPlane(planePoint, normalVector, lightPoint);
      //distance = Math.abs(distance);
      //distance = 1f - distance;
      float val = WavetableLib.getLibraryWavetable(wave.getValuei()).getSample((float)distance, width.getValuef());
      float palInputVal = val * paletteDensity.getValuef();
      CosPaletteModulator.paletteN(palInputVal, whichPal.getValuei(), tempRGB);
      val = 1f - val;
      if (val != 0f) {
        val = brt.getValuef()/val;
      } else {
        val = 1000f;
      }
      //val += 0.5f;
      //if (val > 1f) {
      //  val = 1f;
      //}
      val = (float)Math.pow(val, pow.getValuef());
      tempRGB[0] = val * tempRGB[0];
      if (tempRGB[0] > 1f) {
        tempRGB[0] = 1f;
      }
      tempRGB[1] = val * tempRGB[1];
      if (tempRGB[1] > 1f) {
        tempRGB[1] = 1f;
      }
      tempRGB[2] = val * tempRGB[2];
      if (tempRGB[2] > 1f) {
        tempRGB[2] = 1f;
      }

      int color = LXColor.rgb((int)(tempRGB[0]*255), (int)(tempRGB[1]*255), (int)(tempRGB[2]*255));
      //color = LXColor.scaleBrightness(color, val);
      colors[p.index] = color;
    }
  }

  public class PointToPlaneDistance {

    // Method to calculate the distance from a point to a plane
    public static double distanceFromPointToPlane(Point3D planePoint, Point3D normalVector, Point3D point) {

      // Extract coordinates for readability
      double x1 = planePoint.x;
      double y1 = planePoint.y;
      double z1 = planePoint.z;
      double a = normalVector.x;
      double b = normalVector.y;
      double c = normalVector.z;
      double x2 = point.x;
      double y2 = point.y;
      double z2 = point.z;

      // Compute the numerator of the distance formula
      double numerator = a * (x2 - x1) + b * (y2 - y1) + c * (z2 - z1);

      // Compute the denominator of the distance formula
      double denominator = Math.sqrt(a * a + b * b + c * c);

      // Compute and return the distance
      return numerator / denominator;
    }
  }

}
