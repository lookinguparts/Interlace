package art.lookingup.wavetable;

import art.lookingup.interlace.modulator.CosPaletteModulator;
import art.lookingup.vstrip.Point3D;
import heronarts.lx.LX;
import heronarts.lx.color.LXColor;
import heronarts.lx.model.LXModel;
import heronarts.lx.model.LXPoint;

import java.util.ArrayList;
import java.util.List;

/**
 * Configure this class with a set of plane normals and plane starting positions.
 * The pattern will have a series of bangs that correspond to each normal.  When banged,
 * the generator will generate a SpaceWave3D object that moves through space at the
 * configured speed and direction.  For each normal, we should also have available all
 * of the configuration parameters for a SpaceWaveD instance.
 */
public class SpaceWaveGenerator {

  public List<SpaceWaveD> currentSpaceWaves = new ArrayList<>();
  // Newly generated spacewaves will clone this prototype.  Patterns should reconfigure this prototype before
  // generating new waves.
  public SpaceWaveD proto;

  public SpaceWaveGenerator() {
    proto = new SpaceWaveD();
  }

  public SpaceWaveGenerator.SpaceWaveD generate() {
    SpaceWaveD swd = proto.clone();
    currentSpaceWaves.add(swd);
    swd.logDetails();
    return swd;
  }

  public void render(double deltaMs, int[] colors, LXModel model, LXColor.Blend blend) {
    for (int i = 0; i < currentSpaceWaves.size(); i++) {
      SpaceWaveD swd = currentSpaceWaves.get(i);
      if (swd.isRenderable) {
        swd.render(deltaMs, colors, model, blend);
        // We want to render waves that are off the edge of the model but just haven't entered
        // the model yet.
        if (!swd.isVisible() && swd.hasRenderedYet) {
          swd.isRenderable = false;
        }
      }
    }
    disposeUnrenderables();
  }

  public void disposeUnrenderables() {
    for (int i = currentSpaceWaves.size()-1; i >= 0; i--) {
      SpaceWaveD swd = currentSpaceWaves.get(i);
      if (!swd.isRenderable) {
        currentSpaceWaves.remove(i);
      }
    }
  }

  public void reset() {
    currentSpaceWaves.clear();
  }


  static public class SpaceWaveD {
    public Point3D normal;
    public Point3D position;
    public int wave;
    public float speed;
    public int palette;
    public float paletteDensity;
    public float paletteOffset;
    public float brightness;
    public float pow;
    public float width;
    public boolean visible = true;
    public boolean isRenderable = true;
    public boolean hasRenderedYet = false;
    // If this is true, we will use 1/d calculation to give nice hot spots.  Otherwise, use the standard
    // spacewave rendering.
    public boolean brightRender = true;
    public float alpha = 1f;

    public SpaceWaveD clone() {
      SpaceWaveD swd = new SpaceWaveD();
      swd.normal = new Point3D(normal.x, normal.y, normal.z);
      swd.position = new Point3D(position.x, position.y, position.z);
      swd.wave = wave;
      swd.speed = speed;
      swd.palette = palette;
      swd.paletteDensity = paletteDensity;
      swd.paletteOffset = paletteOffset;
      swd.brightness = brightness;
      swd.pow = pow;
      swd.width = width;
      swd.brightRender = brightRender;
      return swd;
    }

    // Re-use these objects to avoid creating new ones every frame and generating work for GC.
    double[] tempRGB = new double[3];
    Point3D lightPoint = new Point3D(0, 0, 0);

    /**
     * Based on position and width determine if the wave is visible.  We need to find the intersection point
     * of the ray along the normal with the unit cube.  If that distance is greater than width, then the wave
     * is not visible.
     * @return
     */
    public boolean isVisible() {
      // Add a little extra to the width to make sure we don't cut off the wave too early.
      // TODO(tracy): This should be compared against the actual normalized bounding box of the model
      // but this will work fine.
      if (distanceToUnitCube(position.x, position.y, position.z) > width*2f) {
        visible = false;
        return false;
      }
      // Make sure it renders before we garbage collect it.
      hasRenderedYet = true;
      return true;
    }

    /**
     * For manually turning them off for example before they go off the edge of the screen.
     * TODO(tracy): Should we have a separate notRenderable and only clear them out of the list
     * oncce that is true?  That way we can strobe them.
     * @param visible
     */
    public void setVisible(boolean visible) {
      this.visible = visible;
    }

    public static double distanceToUnitCube(double x, double y, double z) {
      // Calculate the distance to the nearest face of the cube in each dimension
      double dx = Math.max(0 - x, Math.max(x - 1, 0));
      double dy = Math.max(0 - y, Math.max(y - 1, 0));
      double dz = Math.max(0 - z, Math.max(z - 1, 0));

      // Return the Euclidean distance
      return Math.sqrt(dx * dx + dy * dy + dz * dz);
    }


    /**
     * Renders the SpaceWaveD pattern into the colors array with the specified blend.
     *
     * @param deltaMs
     * @param colors
     * @param model
     * @param blend
     */
    public void render(double deltaMs, int[] colors, LXModel model, LXColor.Blend blend) {
      for (LXPoint p : model.points) {
        lightPoint.x = p.xn;
        lightPoint.y = p.yn;
        lightPoint.z = p.zn;

        if (brightRender)
          renderBright(deltaMs, colors, p, lightPoint, blend);
        else
          renderStandard(deltaMs, colors, p, lightPoint, blend);
      }
      float deltaS = (float)deltaMs/5000f;
      // Update the position based on speed and direction.
      position.x += normal.x * speed * deltaS;
      position.y += normal.y * speed * deltaS;
      position.z += normal.z * speed * deltaS;

      // Check if the wave is still visible, if not, remove it from the list of renderable waves.
      // TODO(tracy): We should pool and re-use instances of these objects to minimize GC.
    }
    public void renderStandard(double deltaMs, int[] colors, LXPoint p, Point3D lightPoint, LXColor.Blend blend) {
      double distance = distanceFromPointToPlane(position, normal, lightPoint);
      float val = WavetableLib.getLibraryWavetable(wave).getSample((float)distance, width);
      float palInputVal = (val + paletteOffset) * paletteDensity;
      CosPaletteModulator.paletteN(palInputVal, palette, tempRGB);

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
      // If we chose a SCREEN blending method, compute alpha values based on the brightness val.
      if (blend == LXColor.Blend.SCREEN) {
        if (val > 1f)
          val = 1f;
        int alpha = (int)(val * 255);
        color = LXColor.rgba(LXColor.red(color), LXColor.green(color), LXColor.blue(color), alpha);
        colors[p.index] = LXColor.blend(colors[p.index], color, blend);
      } else {
        colors[p.index] = LXColor.blend(colors[p.index], color, blend);
      }
    }

    public void renderBright(double deltaMs, int[] colors, LXPoint p, Point3D lightPoint, LXColor.Blend blend) {
      double distance = distanceFromPointToPlane(position, normal, lightPoint);
      float val = WavetableLib.getLibraryWavetable(wave).getSample((float)distance, width);
      float palInputVal = (val + paletteOffset) * paletteDensity;
      CosPaletteModulator.paletteN(palInputVal, palette, tempRGB);
      float origval = val;
      val = 1f - val;
      if (val != 0f) {
        val = brightness/val;
      } else {
        val = 1000f;
      }
      val = (float)Math.pow(val, pow);
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
      float brightness = (float)(tempRGB[0] + tempRGB[1] + tempRGB[2]) / 3f;
      int color = LXColor.rgb((int)(tempRGB[0]*255f*origval), (int)(tempRGB[1]*255f*origval), (int)(tempRGB[2]*255f*origval));
      if (alpha > 0f) {
        float bright = LXColor.luminosity(color) / 100f;
        // If the brightness is less than alpha, then we want to fade the color to black.
        if (brightness < alpha) {
          float computedAlpha = bright / alpha;
          //if (computedAlpha < .1f)
          //  computedAlpha = 0f;
          //if (computedAlpha > 0.2f)
          //  computedAlpha = 1f;
          //computedAlpha = 0f;
          color = LXColor.rgba(LXColor.red(color), LXColor.green(color), LXColor.blue(color), (int) (255f * computedAlpha));
        }
        colors[p.index] = LXColor.blend(colors[p.index], color, blend);
      } else {
        colors[p.index] = LXColor.blend(colors[p.index], color, blend);
      }
    }

    public void logDetails() {
      LX.log("SpaceWaveD: normal: " + normal.toString() + " position: " + position.toString() + " wave: " + wave + " speed: " + speed + " palette: " + palette + " paletteDensity: " + paletteDensity + " paletteOffset: " + paletteOffset + " brightness: " + brightness + " pow: " + pow + " width: " + width + " visible: " + visible + " isRenderable: " + isRenderable + " hasRenderedYet: " + hasRenderedYet + " brightRender: " + brightRender + " alpha: " + alpha);
    }
  }

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
