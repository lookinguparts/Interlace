package art.lookingup.vstrip;

import art.lookingup.util.LXUtil;
import art.lookingup.wavetable.Wavetable;
import heronarts.lx.color.LXColor;

import java.util.Random;
import java.util.concurrent.ThreadLocalRandom;
import java.util.logging.Logger;

/**
 * VStripRender implements a variety of 1D rendering functions that
 * are local to the specified VStrip.
 */
public class VStripRender {
    private static final Logger logger = Logger.getLogger(art.lookingup.vstrip.VStripRender.class.getName());

    static public void randomGray(int colors[], VStrip vStrip, LXColor.Blend blend) {
        Random r = new Random();
        for (LVPoint pt : vStrip.points) {
            int randomValue = r.nextInt(256);
            colors[pt.p.index] = LXColor.blend(colors[pt.p.index], LXColor.rgba(randomValue, randomValue, randomValue, 255), blend);
        }
    }

    static public void randomGrayBaseDepth(int colors[], VStrip vStrip, LXColor.Blend blend, int min, int depth) {
        for (LVPoint pt : vStrip.points) {
            if (depth < 0)
                depth = 0;
            int randomDepth = ThreadLocalRandom.current().nextInt(depth);
            int value = min + randomDepth;
            if (value > 255) {
                value = 255;
            }
            colors[pt.p.index] = LXColor.blend(colors[pt.p.index], LXColor.rgba(value, value, value, 255), blend);
        }
    }

    static public void sine(int colors[], VStrip vStrip, float head, float freq, float phase, float min, float depth, LXColor.Blend blend) {
        for (LVPoint pt : vStrip.points) {
            float value = ((float)Math.sin((double)freq * (head - pt.xt) + phase) + 1.0f)/2.0f;
            value = min + depth * value;
            int color = (int)(value * 255f);
            colors[pt.p.index] = LXColor.blend(colors[pt.p.index], LXColor.rgba(color, color, color, 255), blend);
        }
    }

    static public void cosine(int colors[], VStrip vStrip, float head, float freq, float phase, float min, float depth, LXColor.Blend blend) {
        for (LVPoint pt : vStrip.points) {
            float value = ((float)Math.cos((double)freq * (head - pt.xt) + phase) + 1.0f)/2.0f;
            value = min + depth * value;
            int color = (int)(value * 255f);
            colors[pt.p.index] = LXColor.blend(colors[pt.p.index], LXColor.rgba(color, color, color, 255), blend);
        }
    }

    static public void cosine2(int colors[], VStrip vStrip, float head, float freq, float phase, float min, float depth, LXColor.Blend blend) {
        for (LVPoint pt : vStrip.points) {
            float value = ((float)Math.cos((double)freq * (head - pt.xpos) + phase) + 1.0f)/2.0f;
            value = min + depth * value;
            int color = (int)(value * 255f);
            colors[pt.p.index] = LXColor.blend(colors[pt.p.index], LXColor.rgba(color, color, color, 255), blend);
        }
    }

    /**
     * Render a wavetable value at the specified position with the specified width.
     */
    static public float[] renderWavetable(int colors[], VStrip vStrip, Wavetable wt, float pos, float width, int clr, int swatch, float intensity, LXColor.Blend blend) {
        float[] minMax = new float[2];
        minMax[0] = pos - width/2.0f;
        minMax[1] = pos + width/2.0f;
        // LXUtil.lx().log("VStripRender renderWavetable pos=" + pos + " width=" + width + " minMax[0]=" + minMax[0] + " minMax[1]=" + minMax[1] + " intensity=" + intensity);
        for (LVPoint pt : vStrip.points) {
            //float val = wt.getSample((pt.xpos - minMax[0])/(minMax[1] - minMax[0]), width);
            float val = wt.getSample(pt.xpos - pos, width);
            // Palette translation?
            if (swatch != -1) {
                // clr = Colors.getQuantizedPaletteColor(LXUtil.lx(), swatch, val, null);
                clr = Colors.getParameterizedPaletteColor(LXUtil.lx(), swatch, val, null);
            }
            val = val * intensity;
            colors[pt.p.index] = LXColor.blend(colors[pt.p.index],
                    LXColor.rgba((int)(((int)Colors.red(clr))*val),
                            (int)(((int)Colors.green(clr))*val),
                            (int)(((int)Colors.blue(clr))*val), 255), blend);
            //colors[pt.p.index] =
            //  LXColor.rgba((int)(((int)Colors.red(clr))*val),
            //    (int)(((int)Colors.green(clr))*val),
            //    (int)(((int)Colors.blue(clr))*val), 255);
            //colors[pt.p.index] = LXColor.scaleBrightness(clr, val);
        }
        return minMax;
    }

    /**
     * Render a triangle gradient in gray.  t is the 0 to 1 normalized x position.  Slope
     * is the slope of the gradient.
     * TODO(tracy): Slope normalization needs to account for led density? i.e. Max slope should include
     * only one led.  Minimum slope should include all leds.
     * @param colors LED colors array.
     * @param vStrip The VStrip to render on.
     * @param t Normalized (0.0-1.0) x position.
     * @param slope The slope of the gradient.  Not normalized currently.
     * @param maxValue Maximum value of the step function (0.0 - 1.0)
     * @param blend Blend mode for writing into the colors array.
     * @return A float array containing the minimum x intercept and maximum x intercept in that order.
     */
    static public float[] renderTriangle(int colors[], VStrip vStrip, float t, float slope, float maxValue, LXColor.Blend blend) {
        return renderTriangle(colors, vStrip, t, slope, maxValue, blend, LXColor.rgba(255, 255, 255, 255));
    }

    static public float[] renderTriangle(int colors[], VStrip vStrip, float t, float slope, float maxValue, LXColor.Blend blend,
                                         int color) {
        float[] minMax = new float[2];
        minMax[0] = (float)zeroCrossingTriangleWave(t, slope);
        minMax[1] = (float)zeroCrossingTriangleWave(t, -slope);
        for (LVPoint pt : vStrip.points) {
            float val = (float)triangleWave(t, slope, pt.xt)*maxValue;
            //colors[pt.index] = LXColor.blend(colors[pt.index], LXColor.rgba(gray, gray, gray, 255), blend);
            int theColor = color;
            /*
            if (pal != null) {
                if (palTVal == -1f)
                    theColor = pal.getColor(val);
                else
                    theColor = pal.getColor(palTVal);
            }

             */
            colors[pt.p.index] = LXColor.blend(colors[pt.p.index], LXColor.rgba(
                            (int)(Colors.red(theColor) * val), (int)(Colors.green(theColor) * val), (int)(Colors.blue(theColor) * val), 255),
                    blend);
        }
        return minMax;
    }

    static public float[] renderSquare(int colors[], VStrip vStrip, float t, float width, float maxValue, LXColor.Blend blend) {
        return renderSquare(colors, vStrip, t, width, maxValue, blend, LXColor.rgba(255, 255, 255, 255));
    }


    static public float[] renderSquare(int colors[], VStrip vStrip, float t, float width, float maxValue, LXColor.Blend blend,
                                       int color) {
        float[] minMax = new float[2];
        minMax[0] = t - width/2.0f;
        minMax[1] = t + width/2.0f;
        for (LVPoint pt: vStrip.points) {
            //int gray = (int) ((((pt.lbx > minMax[0]*lightBar.length) && (pt.lbx < minMax[1]*lightBar.length))?maxValue:0f)*255.0f);
            float val = (((pt.xt > minMax[0]) && (pt.xt < minMax[1]))?maxValue:0f);
            int theColor = color;
            /*
            if (pal != null) {
                if (palTVal == -1f)
                    theColor = pal.getColor(val);
                else
                    theColor = pal.getColor(palTVal);
            }
             */
            int newColor = LXColor.blend(colors[pt.p.index], LXColor.rgba(
                            (int)(Colors.red(theColor) * val), (int)(Colors.green(theColor) * val), (int)(Colors.blue(theColor) * val), 255),
                    blend);
            colors[pt.p.index] = newColor;
        }
        return minMax;
    }

    /**
     * Render a step function at the given position with the given slope.
     * @param colors Points color array to write into.
     * @param vStrip The VStrip to render on.
     * @param t Normalized (0.0-1.0) x position of the step function on the vstrip.
     * @param slope The slope of edge of the step function.
     * @param maxValue Maximum value of the step function (0.0 - 1.0)
     * @param forward Direction of the step function.
     * @param blend Blend mode for writing into the colors array.
     */
    static public float[] renderStepDecay(int colors[], VStrip vStrip, float t, float width, float slope,
                                          float maxValue, boolean forward, LXColor.Blend blend) {
        return renderStepDecay(colors, vStrip, t, width, slope, maxValue, forward, blend, LXColor.rgba(255, 255, 255, 255));
    }

    static public float[] renderStepDecay(int colors[], VStrip vStrip, float t, float width, float slope,
                                          float maxValue, boolean forward, LXColor.Blend blend, int color) {
        float[] minMax = stepDecayZeroCrossing(t, width, slope, forward);
        for (LVPoint pt : vStrip.points) {
            //int gray = (int) (stepDecayWave(t, width, slope, pt.lbx/lightBar.length, forward)*255.0*maxValue);
            float val = stepDecayWave(t, width, slope, pt.xt, forward)*maxValue;
            //colors[pt.index] = LXColor.blend(colors[pt.index], LXColor.rgba(gray, gray, gray, 255), blend);
            int theColor = color;
            /*
            if (pal != null) {
                if (palTVal == -1f)
                    theColor = pal.getColor(val);
                else
                    theColor = pal.getColor(palTVal);
            }

             */
            colors[pt.p.index] = LXColor.blend(colors[pt.p.index], LXColor.rgba(
                            (int)(Colors.red(theColor) * val), (int)(Colors.green(theColor) * val), (int)(Colors.blue(theColor) * val), 255),
                    blend);
        }

        return minMax;
    }

    static public float[] stepDecayZeroCrossing(float stepPos, float width, float slope, boolean forward) {
        float[] minMax = new float[2];
        float max = stepPos + width/2.0f;
        float min = stepPos - width/2.0f - 1.0f/slope;
        // If our orientation traveling along the bar is backwards, swap our min/max computations.

        float tail = 0f;
        if (forward) {
            tail  = - 1.0f/slope + stepPos - width/2.0f;
        } else {
            tail = 1.0f/slope + stepPos + width/2.0f;
        }

        float head = 0;
        if (forward) {
            head = stepPos + width/2.0f;
        } else {
            head = stepPos - width/2.0f;
        }

        if (forward) {
            minMax[0] = tail;
            minMax[1] = head;
        } else {
            minMax[1] = tail;
            minMax[0] = head;
        }
        return minMax;
    }

    /**
     * Step wave with attack slope.
     * Returns value from 0.0f to 1.0f
     */
    static public float stepDecayWave(float stepPos, float width, float slope, float x, boolean forward) {
        float value;
        float halfwidth = width/2.0f;
        if ((x > stepPos - halfwidth) && (x < stepPos + halfwidth))
            return 1.0f;

        if ((x > stepPos + halfwidth) && forward)
            return 0f;
        else if ((x < stepPos - halfwidth && !forward))
            return 0f;

        if (forward) {
            value = 1.0f + slope * (x - (stepPos - halfwidth));
            if (value < 0f) value = 0f;
        } else {
            value = 1.0f - slope * (x - (stepPos + halfwidth));
            if (value < 0f) value = 0f;
        }
        return value;
    }

    static public double zeroCrossingTriangleWave(double peakX, double slope) {
        return peakX - 1.0/slope;
    }

    /**
     * Normalized triangle wave function.  Given position of triangle peak and the
     * slope, return value of function at evalAtX.  If less than 0, clip to zero.
     */
    static public double triangleWave(double peakX, double slope, double evalAtX)
    {
        // If we are to the right of the triangle, the slope is negative
        if (evalAtX > peakX) slope = -slope;
        double y = slope * (evalAtX - peakX) + 1.0f;
        if (y < 0f) y = 0f;
        return y;
    }

    static public void renderColor(int[] colors, VStrip vStrip, int red, int green, int blue, int alpha) {
        renderColor(colors, vStrip, LXColor.rgba(red, green, blue, alpha));
    }

    static public void renderColor(int[] colors, VStrip vStrip, int color) {
        renderColor(colors, vStrip, color, 1.0f);
    }

    static public void renderColor(int[] colors, VStrip vStrip, int color, float maxValue) {
        for (LVPoint point: vStrip.points) {
            colors[point.p.index] = LXColor.rgba(
                    (int)(Colors.red(color) * maxValue), (int)(Colors.green(color) * maxValue), (int)(Colors.blue(color) * maxValue), 255);
        }
    }
}
