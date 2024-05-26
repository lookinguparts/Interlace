package art.lookingup.vstrip;

import art.lookingup.wavetable.Wavetable;
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
    public boolean enabled = true;
    public float intensity = 1.0f;
    public float flareWidth = -1.0f;
    public float palTVal = -1f;

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



    public void renderOnTopAtT(int[] colors, float baseSpeed, float defaultWidth, float slope,
                               float maxValue, int waveform, int whichJoint, boolean initialTail,
                               int whichEffect, float fxDepth, float cosineFreq) {
        renderOnTopAtT(colors, baseSpeed, defaultWidth, slope, maxValue, waveform, whichJoint, initialTail, LXColor.Blend.ADD,
                whichEffect, fxDepth, cosineFreq);
    }

    /**
     * Renders a 'flarelet'. Supports a number of different 'waveforms' centered at the current
     * position.  Position will be incremented by baseSpeed + the flarelet's random speed component.
     * This method will handle making sure there are enough DStrips so that the
     * waveform can be rendered across multiple DStrips.  As the position approaches joints between
     * the DStrips, we will pick additional DStrips to render on based on whichJoint.
     * @param colors
     * @param baseSpeed
     * @param defaultWidth
     * @param slope
     * @param maxValue
     * @param waveform
     * @param whichJoint
     */
    public void renderOnTopAtT(int[] colors, float baseSpeed, float defaultWidth, float slope,
                               float maxValue, int waveform, int whichJoint, boolean initialTail, LXColor.Blend blend,
                               int whichEffect, float fxDepth, float cosineFreq) {
        if (!enabled) return;
        boolean needsCurrentDStripUpdate = false;
        float resolvedWidth = defaultWidth;
        if (flareWidth >= 0f)
            resolvedWidth = flareWidth;

        // Option to force the flarelet to render along a path.  In order to do this, if pathDStrips is set, we will initialize a mapping
        // from a DStrip to a joint value when calling setPathDStrips.  Here we will force whichJoint to
        // be assigned from that mapping.  Once we reach the end of the path, if we don't find a valid joint, we will use
        // the value -2 to denote that we are at the end of the path and that the next dStrip should just be the first
        // dStrip in the pre-defined path.
        if (pathDStrips != null && !pathDStrips.isEmpty()) {
            Integer pJoint = pathJoints.get(dStrip.vStrip.id);
            if (pJoint != null) {
                whichJoint = pJoint;
            } else whichJoint = -2;
            if (whichJoint == -1)
                whichJoint = -2;
        }
        for (VStrip vStrip : vTop.strips) {
            if (dStrip.vStrip.id == vStrip.id) {
                // -- Render on our target light bar --
                float minMax[] = renderWaveform(colors, dStrip, pos, resolvedWidth, slope, intensity * maxValue, waveform, blend);

                if (whichEffect == 1) {
                    VStripRender.randomGrayBaseDepth(colors, vStrip, LXColor.Blend.MULTIPLY, (int)(255*(1f - fxDepth)),
                            (int)(255*fxDepth));
                } else if (whichEffect == 2) {
                    VStripRender.cosine(colors, vStrip, pos, cosineFreq, 0f, 1f - fxDepth, fxDepth, LXColor.Blend.MULTIPLY);
                }

                // If whichJoint == -3, then we are rendering on a single strip and we don't want to make any
                // joint decisions.
                // TODO(tracy): Maybe we should just separate this usecase into a separate method in order to
                // simplify the logic.  It could potentially be handled by just setting the pathDStrips to a single
                // strip and then calling renderFlareletAtT but it would be nicer if there was a more direct way to
                // handle this for the basic case.  Maybe this current method should be called
                // renderOnTopology().  And then renderOnPath() and renderOnStrip().
                if (whichJoint != -3) {
                    // -- Fix up the set of lightbars that we are rendering over.
                    int numPrevBars = -1 * (int) Math.floor(minMax[0]);
                    int numNextBars = (int) Math.ceil(minMax[1] - 1.0f);
                    if (!dStrip.forward) {
                        int oldNumNextBars = numNextBars;
                        numNextBars = numPrevBars;
                        numPrevBars = oldNumNextBars;
                    }
                    // We need to handle the initial case, so we might need to add multiple next bars to our list.
                    int prevWhichJoint = whichJoint;
                    while (nextDStrips.size() < numNextBars) {
                        DStrip nextDStrip;
                        if (whichJoint == -2) {
                            // TODO(tracy): Ugh, we need to prepopulate the look-ahead bars to fit our render but our next
                            // joint is resolving as the end of the path so just select the beginning of the path.  This will
                            // only work in the scenario for a single look-ahead.  To do it properly, we need to compute the whichJoint
                            // value for each dStrip segment.  We already have the vStrip.id to nextJoint mapping computed
                            // so we can use that.
                            nextDStrip = pathDStrips.get(0);
                            Integer pJoint = pathJoints.get(nextDStrip.vStrip.id);
                            if (pJoint != null) {
                                whichJoint = pJoint;
                            }
                            if (whichJoint == -1)
                                whichJoint = -2;
                        } else {
                            if (nextDStrips.isEmpty())
                                nextDStrip = dStrip.chooseNextStrip(whichJoint);
                            else {
                                Integer futureJoint = whichJoint;
                                if (pathDStrips != null && !pathDStrips.isEmpty()) {
                                    // If we a rendering along a preset path, figure out the future joint values.  If there is no
                                    // valid joint value for a strip, then we are at the end of the path so we use the value -2 and
                                    // then choose the next dStrip to be the first bar in the path.
                                    // TODO(tracy): Ideally we would be able to support multiple connecting topologies but for now
                                    // we just have one underlying joint network so we need to work around it for special path
                                    // topologies.
                                    futureJoint = pathJoints.get(nextDStrips.get(nextDStrips.size() - 1).vStrip.id);
                                    if (futureJoint == null || futureJoint == -1)
                                        futureJoint = -2;
                                    whichJoint = futureJoint;
                                }
                                if (whichJoint == -2)
                                    nextDStrip = pathDStrips.get(0);
                                else
                                    nextDStrip = nextDStrips.get(nextDStrips.size() - 1).chooseNextStrip(whichJoint);
                            }
                        }
                        nextDStrips.add(nextDStrip);
                    }

                    // Pre-populate the previous dStrips if we want an initial tail.  Otherwise
                    // these will be populated as we update the current bar to the next bar.
                    while (initialTail && (prevDStrips.size() < numPrevBars)) {
                        DStrip prevDStrip;
                        if (prevDStrips.isEmpty())
                            prevDStrip = dStrip.choosePrevStrip(whichJoint);
                        else
                            prevDStrip = prevDStrips.get(prevDStrips.size() - 1).choosePrevStrip(whichJoint);
                        prevDStrips.add(prevDStrip);
                    }

                    // Garbage collect any old bars.
                    // TODO(tracy): We should trim both nextDStrips and prevDStrips each time so for example if our slope changes
                    // dynamically, we might want to reduce our prevDStrips and nextDStrips list.  It is only an optimization since
                    // we will just render black in ADD mode which should have no effect but is just inefficient.
                    if (prevDStrips.size() > numPrevBars && !prevDStrips.isEmpty()) {
                        prevDStrips.remove(prevDStrips.size() - 1);
                    }

                    whichJoint = prevWhichJoint;

                    // For the number of previous bars, render on each bar
                    for (int j = 0; j < numPrevBars && j < prevDStrips.size(); j++) {
                        DStrip prevDStrip = prevDStrips.get(j);
                        // We need to compute the next bar pos but we need to account for any intermediate bars.
                        float prevDStripPos = dStrip.computePrevStripPos(pos, prevDStrip);
                        // DStrip lengths are normalized to 1.0, so we need to shift our compute distance based on
                        // whether there are any intermediate dStrips.
                        // TODO(tracy): This needs to be fixed for non-normalized rendering.
                        if (prevDStrip.forward) prevDStripPos += j;
                        else prevDStripPos -= j; //
                        renderWaveform(colors, prevDStrip, prevDStripPos, resolvedWidth, slope, intensity * maxValue, waveform, blend);
                        if (whichEffect == 1) {
                            VStripRender.randomGrayBaseDepth(colors, prevDStrip.vStrip, LXColor.Blend.MULTIPLY, (int) (255 * (1f - fxDepth)),
                                    (int) (255 * fxDepth));
                        } else if (whichEffect == 2) {
                            VStripRender.cosine(colors, prevDStrip.vStrip, prevDStripPos, cosineFreq, 0f, 1f - fxDepth, fxDepth, LXColor.Blend.MULTIPLY);
                        }
                    }

                    for (int j = 0; j < numNextBars; j++) {
                        DStrip nextDStrip = nextDStrips.get(j);
                        float nextDStripPos = dStrip.computeNextStripPos(pos, nextDStrip);
                        // TODO(tracy): This needs to be fixed for non-normalized rendering.
                        if (nextDStrip.forward)
                            nextDStripPos -= j; // shift the position to the left by the number of bars away it is actually at.
                        else
                            nextDStripPos += j;
                        renderWaveform(colors, nextDStrip, nextDStripPos, resolvedWidth, slope, intensity * maxValue, waveform, blend);
                        if (whichEffect == 1) {
                            VStripRender.randomGrayBaseDepth(colors, nextDStrip.vStrip, LXColor.Blend.MULTIPLY, (int) (255 * (1f - fxDepth)),
                                    (int) (255 * fxDepth));
                        } else if (whichEffect == 2) {
                            VStripRender.cosine(colors, nextDStrip.vStrip, nextDStripPos, cosineFreq, 0f, 1f - fxDepth, fxDepth,
                                    LXColor.Blend.MULTIPLY);
                        }
                    }
                }
                if (dStrip.forward) {
                    pos += (baseSpeed + speed)/100f;
                } else {
                    pos -= (baseSpeed + speed)/100f;
                }

                if (pos <= 0.0 || pos >= 1.0f) {
                    needsCurrentDStripUpdate = true;
                }
            }
        }

        if (needsCurrentDStripUpdate && whichJoint != -3) {
            updateCurrentDStrip(whichJoint);
        }
    }
    public void renderOnPathAtT(int[] colors, float paramT, float defaultWidth, float slope,
                                float maxValue, int waveform, float maxGlobalPos) {
        renderOnPathAtT(colors, paramT, defaultWidth, slope, maxValue, waveform, 0f, maxGlobalPos);
    }

    /**
     * Renders a waveform on a pre-computed list of dStrips stored in pathBars.  Position is
     * defined parametrically from 0 to 1 where 1 is at the end of the last dStrip.  Automatically
     * adjusts to number of dStrips.
     * @param colors
     * @param paramT
     * @param defaultWidth
     * @param slope
     * @param maxValue
     * @param waveform
     */
    public void renderOnPathAtT(int[] colors, float paramT, float defaultWidth, float slope,
                                float maxValue, int waveform, float startMargin, float maxGlobalPos) {
        if (!enabled) return;
        float resolvedWidth = defaultWidth;
        if (flareWidth >= 0f) resolvedWidth = flareWidth;
        for (VStrip vStrip: vTop.strips) {
            int dStripNum = 0;
            for (DStrip currentDStrip : pathDStrips) {
                if (currentDStrip.vStrip.id == vStrip.id && !currentDStrip.disableRender) {
                    // -- Render on our target dStrip and adjust pos based on which strip number.
                    float localDStripPos = paramT * (maxGlobalPos + startMargin) - startMargin;
                    localDStripPos -= dStripNum;
                    if (!currentDStrip.forward)
                        localDStripPos = 1.0f - localDStripPos;

                    renderWaveform(colors, currentDStrip, localDStripPos, resolvedWidth, slope, intensity * maxValue, waveform, LXColor.Blend.ADD);
                }
                dStripNum++;
            }
        }
    }

    public void renderOnStripAtT(int[] colors, float waveWidth,
                               float maxValue, int waveform, int whichJoint, LXColor.Blend blend,
                               int whichEffect, float fxDepth, float cosineFreq) {
    }


    public void waveOnTop(int[] colors, Wavetable wavetable, float width, LXColor.Blend blend, int whichJoint, int whichEffect, float fxDepth, float fxFreq) {
        // Render on the current strip.  Compute the amount of the waveform on previous strips and the amount of
        // the waveform on the next strip. This needs to be done iteratively until there is no previous amount
        // or extra end amount. The waveform with have some width.
        float currentStripLength = dStrip.vStrip.length();
        float waveStart = pos - width/2f;
        float waveEnd = pos + width/2f;
        float curWtPos = pos;
        if (waveStart < 0f) {
            float prevAmount = -waveStart;
            while (prevAmount > 0f) {
                DStrip prevDStrip = dStrip.choosePrevStrip(whichJoint);
                prevAmount = prevAmount - prevDStrip.vStrip.length();
                // To render on a previous strip, move the position of the wavetable center to the right by the
                // length of the strip.
                curWtPos = curWtPos + prevDStrip.vStrip.length();
                renderWavetable(colors, prevDStrip, wavetable, curWtPos, width, color, blend);

                if (whichEffect == 1) {
                    VStripRender.randomGrayBaseDepth(colors, prevDStrip.vStrip, LXColor.Blend.MULTIPLY, (int)(255*(1f - fxDepth)),
                            (int)(255*fxDepth));
                } else if (whichEffect == 2) {
                    VStripRender.cosine(colors, prevDStrip.vStrip, pos, fxFreq, 0f, 1f - fxDepth, fxDepth, LXColor.Blend.MULTIPLY);
                }
            }
        }

        // Render on our current target strip.
        renderWavetable(colors, dStrip, wavetable, pos, width, color, blend);

        if (whichEffect == 1) {
            VStripRender.randomGrayBaseDepth(colors, dStrip.vStrip, LXColor.Blend.MULTIPLY, (int)(255*(1f - fxDepth)),
                    (int)(255*fxDepth));
        } else if (whichEffect == 2) {
            VStripRender.cosine(colors, dStrip.vStrip, pos, fxFreq, 0f, 1f - fxDepth, fxDepth, LXColor.Blend.MULTIPLY);
        }

        // If the wavetable extends past the current strip, render on the next set of strips.
        if (waveEnd > currentStripLength) {
            float nextAmount = waveEnd - currentStripLength;
            //curWtPos = pos - currentStripLength;
            curWtPos = pos;
            while (nextAmount > 0f) {
                DStrip nextDStrip = dStrip.chooseNextStrip(whichJoint);
                nextAmount = nextAmount - nextDStrip.vStrip.length();
                // To render on a next strip, move the position of the wavetable center to the left by the
                // length of the strip.
                curWtPos = curWtPos - nextDStrip.vStrip.length();
                renderWavetable(colors, nextDStrip, wavetable, curWtPos, width, color, blend);
                if (whichEffect == 1) {
                    VStripRender.randomGrayBaseDepth(colors, nextDStrip.vStrip, LXColor.Blend.MULTIPLY, (int)(255*(1f - fxDepth)),
                            (int)(255*fxDepth));
                } else if (whichEffect == 2) {
                    VStripRender.cosine(colors, nextDStrip.vStrip, pos, fxFreq, 0f, 1f - fxDepth, fxDepth, LXColor.Blend.MULTIPLY);
                }
            }
        }
    }

    public void waveOnStrip(int[] colors, Wavetable wavetable, float width, int color, LXColor.Blend blend, int whichEffect, float fxDepth, float fxFreq) {
        renderWavetable(colors, dStrip, wavetable, pos, width, color, blend);
    }

    public float[] renderWavetable(int[] colors, DStrip targetDStrip, Wavetable wavetable, float pos, float width,
                                   int color, LXColor.Blend blend) {
        return VStripRender.renderWavetable(colors, targetDStrip.vStrip, wavetable, pos, width, color, blend);
    }

    /**
     * Render the specified waveform at the specified position.  maxValue already includes the flarelet intensity override multiplied
     * into it by this point.
     * @param colors
     * @param targetDStrip
     * @param position
     * @param width
     * @param slope
     * @param maxValue
     * @param waveform
     * @param blend
     * @return
     */
    public float[] renderWaveform(int[] colors, DStrip targetDStrip, float position, float width, float slope,
                                  float maxValue, int waveform, LXColor.Blend blend) {
        if (waveform == WAVEFORM_TRIANGLE)
            return VStripRender.renderTriangle(colors, targetDStrip.vStrip, position, slope, maxValue, blend, color);
        else if (waveform == WAVEFORM_SQUARE)
            return VStripRender.renderSquare(colors, targetDStrip.vStrip, position, width, maxValue, blend, color);
        else if (waveform == WAVEFORM_STEPDECAY)
            return VStripRender.renderStepDecay(colors, targetDStrip.vStrip, position, width, slope,
                    maxValue, targetDStrip.forward, LXColor.Blend.ADD, color);
        return new float[] {0f, 0f};
    }
}
