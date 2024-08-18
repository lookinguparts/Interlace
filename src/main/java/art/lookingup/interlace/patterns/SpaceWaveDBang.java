package art.lookingup.interlace.patterns;

import art.lookingup.interlace.modulator.CosPaletteModulator;
import art.lookingup.vstrip.Point3D;
import art.lookingup.wavetable.*;
import heronarts.lx.LX;
import heronarts.lx.color.LXColor;
import heronarts.lx.model.LXPoint;
import heronarts.lx.parameter.*;
import heronarts.lx.pattern.LXPattern;

import java.awt.*;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Given a direction vector and a position in space, move the plane along the direction vector.
 * Use the distance from a point to the plane to render a wavetable.
 */
public class SpaceWaveDBang extends LXPattern {

  protected CompoundParameter dirX = new CompoundParameter("dirX", 0, -1, 1)
    .setDescription("X component of direction vector");
  protected CompoundParameter dirY = new CompoundParameter("dirY", 1, -1, 1)
    .setDescription("Y component of direction vector");
  protected CompoundParameter dirZ = new CompoundParameter("dirZ", 0, -1, 1)
    .setDescription("Z component of direction vector");
  protected CompoundParameter speed = new CompoundParameter("speed", 1, 0, 50)
    .setDescription("Speed of plane movement");
  protected CompoundParameter width = new CompoundParameter("width", 0.2, 0, 100)
    .setDescription("Width of the wave");
  protected CompoundParameter planePos = new CompoundParameter("pos", 0, -2, 2)
    .setDescription("Position of the plane along the direction vector relative to origin.");
  protected DiscreteParameter wave = new DiscreteParameter("wave", 0, 0, WavetableLib.countWavetables())
    .setDescription("Which wave to use");
  protected CompoundParameter paletteDensity = new CompoundParameter("pald", 1, 0, 5)
    .setDescription("Density of the palette");
  protected CompoundParameter paletteOffset = new CompoundParameter("paloff", 0, 0, 1)
    .setDescription("Offset of the palette");
  protected DiscreteParameter whichPal = new DiscreteParameter("pal", 0, 0, CosPaletteModulator.paletteStrings.length)
    .setDescription("Which palette to use");
  protected CompoundParameter brt = new CompoundParameter("brt", 0.01, 0, 1)
    .setDescription("Brightness of the wave");
  protected CompoundParameter pow = new CompoundParameter("pw", 1, 0, 5)
    .setDescription("Pow of the value");
  protected BooleanParameter bang = new BooleanParameter("bang", false)
    .setDescription("Generate new wave");
  protected CompoundParameter alpha = new CompoundParameter("alpha", 0.1, -0.1, 1);

  // For all the above parameters, create another instance with the number 2 appended to the name.
  protected CompoundParameter dirX2 = new CompoundParameter("dirX2", 0, -1, 1)
    .setDescription("X component of direction vector");
  protected CompoundParameter dirY2 = new CompoundParameter("dirY2", 1, -1, 1)
    .setDescription("Y component of direction vector");
  protected CompoundParameter dirZ2 = new CompoundParameter("dirZ2", 0, -1, 1)
    .setDescription("Z component of direction vector");
  protected CompoundParameter speed2 = new CompoundParameter("speed2", 1, 0, 50)
    .setDescription("Speed of plane movement");
  protected CompoundParameter width2 = new CompoundParameter("width2", 0.2, 0, 100)
    .setDescription("Width of the wave");
  protected CompoundParameter planePos2 = new CompoundParameter("pos2", 0, -2, 2)
    .setDescription("Position of the plane along the direction vector relative to origin.");
  protected DiscreteParameter wave2 = new DiscreteParameter("wave2", 0, 0, WavetableLib.countWavetables())
    .setDescription("Which wave to use");
  protected CompoundParameter paletteDensity2 = new CompoundParameter("pald2", 1, 0, 5)
    .setDescription("Density of the palette");
  protected CompoundParameter paletteOffset2 = new CompoundParameter("paloff2", 0, 0, 1)
    .setDescription("Offset of the palette");
  protected DiscreteParameter whichPal2 = new DiscreteParameter("pal2", 0, 0, CosPaletteModulator.paletteStrings.length)
    .setDescription("Which palette to use");
  protected CompoundParameter brt2 = new CompoundParameter("brt2", 0.01, 0, 1)
    .setDescription("Brightness of the wave");
  protected CompoundParameter pow2 = new CompoundParameter("pw2", 1, 0, 5)
    .setDescription("Pow of the value");
  protected BooleanParameter bang2 = new BooleanParameter("bang2", false)
    .setDescription("Generate new wave");
  protected CompoundParameter alpha2 = new CompoundParameter("alpha2", 0.1, -0.1, 1);

  // For the above parameters, now with 3 instead of 2
  protected CompoundParameter dirX3 = new CompoundParameter("dirX3", 0, -1, 1)
    .setDescription("X component of direction vector");
  protected CompoundParameter dirY3 = new CompoundParameter("dirY3", 1, -1, 1)
    .setDescription("Y component of direction vector");
  protected CompoundParameter dirZ3 = new CompoundParameter("dirZ3", 0, -1, 1)
    .setDescription("Z component of direction vector");
  protected CompoundParameter speed3 = new CompoundParameter("speed3", 1, 0, 50)
    .setDescription("Speed of plane movement");
  protected CompoundParameter width3 = new CompoundParameter("width3", 0.2, 0, 100)
    .setDescription("Width of the wave");
  protected CompoundParameter planePos3 = new CompoundParameter("pos3", 0, -2, 2)
    .setDescription("Position of the plane along the direction vector relative to origin.");
  protected DiscreteParameter wave3 = new DiscreteParameter("wave3", 0, 0, WavetableLib.countWavetables())
    .setDescription("Which wave to use");
  protected CompoundParameter paletteDensity3 = new CompoundParameter("pald3", 1, 0, 5)
    .setDescription("Density of the palette");
  protected CompoundParameter paletteOffset3 = new CompoundParameter("paloff3", 0, 0, 1)
    .setDescription("Offset of the palette");
  protected DiscreteParameter whichPal3 = new DiscreteParameter("pal3", 0, 0, CosPaletteModulator.paletteStrings.length)
    .setDescription("Which palette to use");
  protected CompoundParameter brt3 = new CompoundParameter("brt3", 0.01, 0, 1)
    .setDescription("Brightness of the wave");
  protected CompoundParameter pow3 = new CompoundParameter("pw3", 1, 0, 5)
    .setDescription("Pow of the value");
  protected BooleanParameter bang3 = new BooleanParameter("bang3", false)
    .setDescription("Generate new wave");
  protected CompoundParameter alpha3 = new CompoundParameter("alpha3", 0.1, -0.1, 1);

  Point3D normalVector = new Point3D(0, 1, 0);
  Point3D normalVector2 = new Point3D(0, 1, 0);
  Point3D normalVector3 = new Point3D(0, 1, 0);

  Point3D planePoint = new Point3D(0, 0, 0);
  Point3D planePoint2 = new Point3D(0, 0, 0);
  Point3D planePoint3 = new Point3D(0, 0, 0);

  SpaceWaveGenerator waveGenerator = new SpaceWaveGenerator();
  SpaceWaveGenerator waveGenerator2 = new SpaceWaveGenerator();
  SpaceWaveGenerator waveGenerator3 = new SpaceWaveGenerator();


  public SpaceWaveDBang(LX lx) {
    super(lx);
    addParameter("bang", bang);
    addParameter("alpha", alpha);
    addParameter("dirX", dirX);
    addParameter("dirY", dirY);
    addParameter("dirZ", dirZ);
    addParameter("speed", speed);
    addParameter("width", width);
    addParameter("pos", planePos);
    addParameter("pal", whichPal);
    addParameter("pald", paletteDensity);
    addParameter("paloff", paletteOffset);
    addParameter("pw", pow);
    addParameter("brt", brt);
    addParameter("wave", wave);

    // Add new parameters
    addParameter("bang2", bang2);
    addParameter("alpha2", alpha2);
    addParameter("dirX2", dirX2);
    addParameter("dirY2", dirY2);
    addParameter("dirZ2", dirZ2);
    addParameter("speed2", speed2);
    addParameter("width2", width2);
    addParameter("pos2", planePos2);
    addParameter("pal2", whichPal2);
    addParameter("pald2", paletteDensity2);
    addParameter("paloff2", paletteOffset2);
    addParameter("pw2", pow2);
    addParameter("brt2", brt2);
    addParameter("wave2", wave2);

    // Add 3rd set of parameters
    addParameter("bang3", bang3);
    addParameter("alpha3", alpha3);
    addParameter("dirX3", dirX3);
    addParameter("dirY3", dirY3);
    addParameter("dirZ3", dirZ3);
    addParameter("speed3", speed3);
    addParameter("width3", width3);
    addParameter("pos3", planePos3);
    addParameter("pal3", whichPal3);
    addParameter("pald3", paletteDensity3);
    addParameter("paloff3", paletteOffset3);
    addParameter("pw3", pow3);
    addParameter("brt3", brt3);
    addParameter("wave3", wave3);


    addParamListener(bang, new LXParameterListener() {
      public void onParameterChanged(LXParameter p) {
        if (bang.isOn()) {
          updateNormalAndPos();
          waveGenerator.proto.normal = normalVector;
          waveGenerator.proto.position = planePoint;
          waveGenerator.proto.wave = wave.getValuei();
          waveGenerator.proto.palette = whichPal.getValuei();
          waveGenerator.proto.paletteDensity = paletteDensity.getValuef();
          waveGenerator.proto.paletteOffset = paletteOffset.getValuef();
          waveGenerator.proto.brightness = brt.getValuef();
          waveGenerator.proto.pow = pow.getValuef();
          waveGenerator.proto.speed = speed.getValuef();
          waveGenerator.proto.width = width.getValuef();
          waveGenerator.proto.alpha = alpha.getValuef();
          if (waveGenerator.proto.brightness == 0f)
            waveGenerator.proto.brightRender = false;
          else
            waveGenerator.proto.brightRender = true;
          waveGenerator.generate();
          bang.setValue(false);
        }
      }
    });

    // Now for bang2 with waveGenerator2
    addParamListener(bang2, new LXParameterListener() {
      public void onParameterChanged(LXParameter p) {
        if (bang2.isOn()) {
          updateNormalAndPos();
          waveGenerator2.proto.normal = normalVector2;
          waveGenerator2.proto.position = planePoint2;
          waveGenerator2.proto.wave = wave2.getValuei();
          waveGenerator2.proto.palette = whichPal2.getValuei();
          waveGenerator2.proto.paletteDensity = paletteDensity2.getValuef();
          waveGenerator2.proto.paletteOffset = paletteOffset2.getValuef();
          waveGenerator2.proto.brightness = brt2.getValuef();
          waveGenerator2.proto.pow = pow2.getValuef();
          waveGenerator2.proto.speed = speed2.getValuef();
          waveGenerator2.proto.width = width2.getValuef();
          waveGenerator2.proto.alpha = alpha2.getValuef();
          if (waveGenerator2.proto.brightness == 0f)
            waveGenerator2.proto.brightRender = false;
          else
            waveGenerator2.proto.brightRender = true;
          waveGenerator2.generate();
          bang2.setValue(false);
        }
      }
    });
    // Now for 3 with waveGenerator3
    addParamListener(bang3, new LXParameterListener() {
      public void onParameterChanged(LXParameter p) {
        if (bang3.isOn()) {
          updateNormalAndPos();
          waveGenerator3.proto.normal = normalVector3;
          waveGenerator3.proto.position = planePoint3;
          waveGenerator3.proto.wave = wave3.getValuei();
          waveGenerator3.proto.palette = whichPal3.getValuei();
          waveGenerator3.proto.paletteDensity = paletteDensity3.getValuef();
          waveGenerator3.proto.paletteOffset = paletteOffset3.getValuef();
          waveGenerator3.proto.brightness = brt3.getValuef();
          waveGenerator3.proto.pow = pow3.getValuef();
          waveGenerator3.proto.speed = speed3.getValuef();
          waveGenerator3.proto.width = width3.getValuef();
          waveGenerator3.proto.alpha = alpha3.getValuef();
          if (waveGenerator3.proto.brightness == 0f)
            waveGenerator3.proto.brightRender = false;
          else
            waveGenerator3.proto.brightRender = true;
          waveGenerator3.generate();
          bang3.setValue(false);
        }
      }
    });
  }

  Map<LXListenableParameter, List<LXParameterListener>> listeners = new HashMap<>();

  public void addParamListener(LXListenableParameter p, LXParameterListener l) {
    p.addListener(l);
    List<LXParameterListener> plisteners = listeners.computeIfAbsent(p, k -> new ArrayList<>());
    plisteners.add(l);
  }

  @Override
  public void dispose() {
    super.dispose();
    for (LXListenableParameter param : listeners.keySet()) {
      for (LXParameterListener listener : listeners.get(param)) {
        param.removeListener(listener);
      }
    }
    listeners.clear();
  }

  public void updateNormalAndPos() {
    planePoint.x = normalVector.x * planePos.getValuef();
    planePoint.y = normalVector.y * planePos.getValuef();
    planePoint.z = normalVector.z * planePos.getValuef();
    normalVector.x = dirX.getValuef();
    normalVector.y = dirY.getValuef();
    normalVector.z = dirZ.getValuef();
    if (Math.abs(normalVector.length()) < 0.01f) {
      //return;
    }
    normalVector.normalize();
    // Now for 2
    planePoint2.x = normalVector2.x * planePos2.getValuef();
    planePoint2.y = normalVector2.y * planePos2.getValuef();
    planePoint2.z = normalVector2.z * planePos2.getValuef();
    normalVector2.x = dirX2.getValuef();
    normalVector2.y = dirY2.getValuef();
    normalVector2.z = dirZ2.getValuef();
    if (Math.abs(normalVector2.length())< 0.01f) {
      //return;
    }
    normalVector2.normalize();

    planePoint3.x = normalVector3.x * planePos3.getValuef();
    planePoint3.y = normalVector3.y * planePos3.getValuef();
    planePoint3.z = normalVector3.z * planePos3.getValuef();
    normalVector3.x = dirX3.getValuef();
    normalVector3.y = dirY3.getValuef();
    normalVector3.z = dirZ3.getValuef();
    if (Math.abs(normalVector3.length()) < 0.01f) {
      //return;
    }
    normalVector3.normalize();
  }

  public void run(double deltaMs) {
    // Run render on the wave generator.
    if (alpha.getValuef() > 0) {
      for (LXPoint p : model.points) {
        colors[p.index] = LXColor.rgba(0, 0, 0, 0);
      }
    } else {
      for (LXPoint p : model.points) {
        colors[p.index] = LXColor.rgba(0, 0, 0, 255);
      }
    }
    waveGenerator.render(deltaMs, colors, model, alpha.getValuef()>0f?LXColor.Blend.SCREEN:LXColor.Blend.ADD);
    waveGenerator2.render(deltaMs, colors, model, alpha2.getValuef()>0f?LXColor.Blend.SCREEN:LXColor.Blend.ADD);
    waveGenerator3.render(deltaMs, colors, model, alpha3.getValuef()>0f?LXColor.Blend.SCREEN:LXColor.Blend.ADD);
  }

  @Override
  public void onActive() {
    super.onActive();
    if (waveGenerator != null) waveGenerator.reset();
    if (waveGenerator2 != null) waveGenerator2.reset();
    if (waveGenerator3 != null) waveGenerator3.reset();
  }

}