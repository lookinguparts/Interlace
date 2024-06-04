package art.lookingup.vstrip;

import art.lookingup.wavetable.Wavetable;
import heronarts.lx.LX;
import heronarts.lx.color.LXColor;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class Flarelet {
    public static final int WAVEFORM_TRIANGLE = 0;
    public static final int WAVEFORM_SQUARE = 1;
    public static final int WAVEFORM_STEPDECAY = 2;

    public VTopology vTop;

    public DStrip dStrip;
    public DStrip initDStrip;
    public float pos = 0f;
    public float initPos = 0f;
    public float speed = 1f;
    public float initSpeed = 1f;
    public List<DStrip> prevDStrips = new ArrayList<>();
    public List<DStrip> nextDStrips = new ArrayList<>();
    public int color = LXColor.rgba(255, 255, 255, 255);
    // Whether the flarelet is enabled or not.  If not enabled, it will not render.
    public boolean enabled = true;
    // The intensity of the flarelet.  This is a multiplier that can be used to adjust the intensity of the flarelet.
    public float intensity = 1.0f;
    // The intensity at which we simply skip the rendering.
    public float cutoutIntensity = 0.05f;
    public float flareWidth = -1.0f;

    // Time the flarelet was started.  Used for tracking fade-outs per flarelet.
    public double startTime;
    public double fadeTime; // milliseconds

    // Application of FX
    public int fx;
    public float fxDepth;
    public float fxFreq;


    public Wavetable wavetable;

    public float waveWidth;

    // Which palette swatch to use for the flarelet.
    public int swatch = -1;

    public LX lx;


    // When rendering position parametrically from 0 to 1, we need a pre-computed set of dStrips
    // that we intend to render on.
    protected List<DStrip> pathDStrips;
    public Map<Integer, Integer> pathJoints = new HashMap<>();

    /**
     * When intending for blobs to travel along a pre-computed set of path dStrips, you should call this
     * method for setting the target dStrips.
     * @param pDStrips The directional dStrips that the flarelet can travel along.
     */
    public void setPathDStrips(List<DStrip> pDStrips) {
        pathDStrips = pDStrips;
        pathJoints.clear();
        // For a given dStrip, we need to figure out which joint leads to the next dStrip.
        for (int pDStripNum = 0; pDStripNum < pDStrips.size(); pDStripNum++) {
            DStrip pDStrip = pDStrips.get(pDStripNum);
            // The next dStrip, or wrap around to the first when reaching the end.
            DStrip nextDStrip = pDStrips.get((pDStripNum < pDStrips.size() - 1)?pDStripNum+1:0);
            int whichJoint = pDStrip.findJointNum(nextDStrip.vStrip.id);
            pathJoints.put(pDStrip.vStrip.id, whichJoint);
        }
    }

    public List<DStrip> getPathDStrips() {
        return pathDStrips;
    }

    public void updateCurrentDStrip(int dStripSelector) {
        // First, lets transfer the current dStrip into our
        // previous dStrips list.  The list will be trimmed in our draw loop.
        prevDStrips.add(0, dStrip);
        // Next the current dStrip should come from the beginning of nextDStrips
        // The nextDStrips list will be filled out in the draw loop if necessary.
        if (!nextDStrips.isEmpty())
            dStrip = nextDStrips.remove(0);
        else {
            if (dStripSelector == -2) {
                // When we select a set of dStrips for a path, it might be possible that at the end of the path
                // there are no valid joint selections.  In that case, the dStripSelector/whichJoint will be -2 and
                // we should just choose the first dStrip in the pathDStrips list.
                resetToInit();
            } else {
                dStrip = dStrip.chooseNextStrip(dStripSelector);
            }
        }

        // Set or position based on the directionality of the current dStrip.
        if (dStrip.forward) {
            pos = 0.0f;
        } else {
            pos = 1.0f;
        }

    }

    public void reset(VTopology vTop, int dStripNum, float initialPos, float randomSpeed, boolean forward) {
        this.vTop = vTop;
        pos = initialPos;
        dStrip = new DStrip(vTop, dStripNum, forward);
        speed = randomSpeed * (float)Math.random();
        initSpeed = speed;
        initDStrip = dStrip;
        initPos = pos;
        nextDStrips = new ArrayList<DStrip>();
        prevDStrips = new ArrayList<DStrip>();
    }

    public void resetToInit() {
        dStrip = initDStrip;
        pos = initPos;
        speed = initSpeed;
        nextDStrips.clear();
        prevDStrips.clear();
    }

    public boolean isRenderable() {
        if ((!enabled) || (getFadeLevel() < cutoutIntensity)) return false;
        return true;
    }

    /**
     * For flarelets that will be fading out, compute the intensity level based on the current time, the flare start
     * time, and the flare fade time.
     * @return
     */
    public float getFadeLevel() {
        if (Math.abs(fadeTime) < 0.001f) return intensity;
        float fadeLevel = 1f - (float) ((System.currentTimeMillis() - startTime)/fadeTime);
        if (fadeLevel < 0f)
            fadeLevel = 0f;
        return fadeLevel * intensity;
    }

    public void waveOnTop(int[] colors, LXColor.Blend blend, int whichJoint) {
        waveOnTop(colors, blend, whichJoint, true);
    }

    public void waveOnTop(int[] colors, LXColor.Blend blend, int whichJoint, boolean initialTail) {
        // Render on the current strip.  Compute the amount of the waveform on previous strips and the amount of
        // the waveform on the next strip. This needs to be done iteratively until there is no previous amount
        // or extra end amount. The waveform with have some width.
        float currentStripLength = dStrip.vStrip.length();
        float waveStart = pos - waveWidth/2f;
        float waveEnd = pos + waveWidth/2f;
        float curWtPos = pos;

        if (!isRenderable()) return;
        if (waveStart < 0f && initialTail) {
            float prevAmount = -waveStart;
            while (prevAmount > 0f) {
                DStrip prevDStrip = dStrip.choosePrevStrip(whichJoint);
                if (prevDStrip != null) {
                    prevAmount = prevAmount - prevDStrip.vStrip.length();
                    // To render on a previous strip, move the position of the wavetable center to the right by the
                    // length of the strip.
                    curWtPos = curWtPos + prevDStrip.vStrip.length();
                    renderWavetable(colors, prevDStrip, curWtPos, color, blend);

                    if (fx == 1) {
                        VStripRender.randomGrayBaseDepth(colors, prevDStrip.vStrip, LXColor.Blend.MULTIPLY, (int) (255 * (1f - fxDepth)),
                          (int) (255 * fxDepth));
                    } else if (fx == 2) {
                        VStripRender.cosine2(colors, prevDStrip.vStrip, pos, fxFreq, 0f, 1f - fxDepth, fxDepth, LXColor.Blend.MULTIPLY);
                    }
                } else {
                    prevAmount = 0f;
                }
            }
        }

        // Render on our current target strip.
        renderWavetable(colors, dStrip, pos, color, blend);

        if (fx == 1) {
            VStripRender.randomGrayBaseDepth(colors, dStrip.vStrip, LXColor.Blend.MULTIPLY, (int)(255*(1f - fxDepth)),
                    (int)(255*fxDepth));
        } else if (fx == 2) {
            VStripRender.cosine(colors, dStrip.vStrip, pos, fxFreq, 0f, 1f - fxDepth, fxDepth, LXColor.Blend.MULTIPLY);
        }

        // If the wavetable extends past the current strip, render on the next set of strips.
        if (waveEnd > currentStripLength) {
            float nextAmount = waveEnd - currentStripLength;
            //curWtPos = pos - currentStripLength;
            curWtPos = pos;
            while (nextAmount > 0f) {
                DStrip nextDStrip = dStrip.chooseNextStrip(whichJoint);
                if (nextDStrip != null) {
                    nextAmount = nextAmount - nextDStrip.vStrip.length();
                    // To render on a next strip, move the position of the wavetable center to the left by the
                    // length of the strip.
                    curWtPos = curWtPos - nextDStrip.vStrip.length();
                    renderWavetable(colors, nextDStrip, curWtPos, color, blend);
                    if (fx == 1) {
                        VStripRender.randomGrayBaseDepth(colors, nextDStrip.vStrip, LXColor.Blend.MULTIPLY, (int) (255 * (1f - fxDepth)),
                          (int) (255 * fxDepth));
                    } else if (fx == 2) {
                        VStripRender.cosine(colors, nextDStrip.vStrip, pos, fxFreq, 0f, 1f - fxDepth, fxDepth, LXColor.Blend.MULTIPLY);
                    }
                } else {
                    nextAmount = 0f;
                }
            }
        }
    }

    public void waveOnStrip(int[] colors, int color, LXColor.Blend blend) {
        if (!isRenderable()) return;
        renderWavetable(colors, dStrip, pos, color, blend);
    }

    public float[] renderWavetable(int[] colors, DStrip targetDStrip, float pos, int color, LXColor.Blend blend) {
        //lx.log("Flarelet renderWavetable at pos: " + pos + " width=" + waveWidth + " color=" + color + " dStrip=" + targetDStrip.vStrip.id);
        return VStripRender.renderWavetable(colors, targetDStrip.vStrip, wavetable, pos, waveWidth, color, swatch, getFadeLevel(), blend);
    }
}
