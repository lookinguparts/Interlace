package art.lookingup.vstrip;

import java.util.List;
import java.util.concurrent.ThreadLocalRandom;

public class DStrip {
    public VTopology vTop;
    public VStrip vStrip;
    public boolean forward;
    public boolean disableRender;


    public DStrip(VTopology vTop, int stripId, boolean forward) {
        this.vTop = vTop;
        vStrip = vTop.getVStrip(stripId);
        this.forward = forward;
        disableRender = false;
    }

    public DStrip chooseNextStrip(int jointSelector) {
        if (forward) {
            return chooseStripFromJoints(vStrip.myEndPointJoints, jointSelector);
        } else {
            return chooseStripFromJoints(vStrip.myStartPointJoints, jointSelector);
        }
    }

    public DStrip choosePrevStrip(int jointSelector) {
        if (forward) {
            return chooseStripFromJoints(vStrip.myStartPointJoints, jointSelector);
        } else {
            return chooseStripFromJoints(vStrip.myEndPointJoints, jointSelector);
        }
    }

    /**
     * Given an array of joints, select the next DStrip.
     * @param joints Array of joints to select from.
     * @param jointSelector Which joint to select the next bar from.  If -1, then choose a random joint.
     */
    public DStrip chooseStripFromJoints(List<VJoint> joints, int jointSelector) {
        int jointNum = jointSelector;
        if (jointNum == -1)
            jointNum = ThreadLocalRandom.current().nextInt(joints.size());
        VStrip nextStrip = joints.get(jointNum).vStrip;
        // TODO(tracy): Should we just precreate all the DStrips and then just return the one we want?
        DStrip ds = new DStrip(vTop, nextStrip.id, joints.get(jointNum).isAdjacentStripAStartPoint);
        return ds;
    }

    /**
     * Finds which joint to take on this directional strip so that the next strip will be our
     * requested strip id.  Note: Tries start point joints first and then end point joints.  For the
     * case where it is just two strips connected end to end, you will need to implement a function that
     * takes into consideration whether you are checking the start point or end point of the current
     * directional strip.
     * @param stripId The ID of the next strip.
     * @return Which joint number to choose to get to the requested next light bar.
     */
    public int findJointNum(int stripId) {
        int whichJoint = findJointNumAtStart(stripId);
        if (whichJoint != -1) return whichJoint;
        return findJointNumAtEnd(stripId);
    }

    public int findJointNumAtStart(int stripId) {
        for (int jointNum = 0; jointNum < vStrip.myStartPointJoints.size(); jointNum++) {
            VJoint j = vStrip.myStartPointJoints.get(jointNum);
            if (j != null && j.vStrip.id == stripId) {
                return jointNum;
            }
        }
        return -1;
    }

    public int findJointNumAtEnd(int stripId) {
        for (int jointNum = 0; jointNum < vStrip.myStartPointJoints.size(); jointNum++) {
            VJoint j = vStrip.myEndPointJoints.get(jointNum);
            if (j != null && j.vStrip.id == stripId) {
                return jointNum;
            }
        }
        return -1;
    }

    // TODO(tracy): Implement the non-normalized version of these methods.  To do so, we will need to account for
    // the strip length.

    /**
     * Given a position on this strip, compute the position on the next strip while accounting for the
     * direction of the strip.
     * @param pos Position based on points along the current strip being located between 0.0 and 1.0. i.e.
     *            normalized position. pos should be positive when calling this method.
     * @param nextStrip The adjacent strip to compute the position on.  We account for the directionality of
     *                  the strip when computing the position on the next strip.
     * @return
     */
    public float computeNextStripPos(float pos, DStrip nextStrip) {
        float distanceToJoint = 1.0f - pos;
        if (!forward) {
            distanceToJoint = pos;
        }
        if (nextStrip.forward) {
            return -distanceToJoint;
        } else {
            return 1.0f + distanceToJoint;
        }
    }

    /**
     * Given a position on this strip, compute the position on the previous strip while accounting for the
     * direction of the strip.
     * @param pos Position based on points along the current strip being located between 0.0 and 1.0. i.e.
     *            normalized position.  pos should be negative when calling this method.
     * @param prevStrip The adjacent strip to compute the position on.  We account for the directionality of
     *                  the strip when computing the position on the previous strip.
     * @return
     */
    public float computePrevStripPos(float pos, DStrip prevStrip) {
        // For the previous strip, in the straightforward case, the distance to this joint will be the current
        // position on the strip since the joint will be at 0.0.  If the current strip is not forward, the position
        // at the joint with the previous strip (strip is to the left) is actually 1.0 so the distance is 1.0 - pos.
        float distanceToJoint = pos;
        if (!forward) {
            distanceToJoint = 1.0f - pos;
        }
        // If the previous strip is oriented normally, then off to the right will be 1.0 + distance.
        // If the previous strip is backwards, then off to the right will be 0 - distance.
        if (prevStrip.forward) {
            return 1.0f + distanceToJoint;
        } else {
            return -distanceToJoint;
        }
    }
}
