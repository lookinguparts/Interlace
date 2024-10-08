package art.lookingup.interlace.modulator;

import art.lookingup.vstrip.Point3D;
import heronarts.lx.LXCategory;
import heronarts.lx.modulator.LXModulator;
import heronarts.lx.osc.LXOscComponent;
import heronarts.lx.parameter.CompoundParameter;

@LXModulator.Global("MagnetXYZ")
@LXCategory(LXCategory.MACRO)
public class MagnetXYZ extends LXModulator implements LXOscComponent {

    public final CompoundParameter magX =
            new CompoundParameter("magx")
                    .setDescription("Magnometer X");

    public final CompoundParameter magY =
            new CompoundParameter("magy")
                    .setDescription("Magnometer Y");

    public final CompoundParameter magZ =
            new CompoundParameter("magz")
                    .setDescription("Magnometer Z");

    public final CompoundParameter[] knobs = {
            magX, magY, magZ
    };

    protected float minMagX = Float.MAX_VALUE;
    protected float maxMagX;
    protected float minMagY;
    protected float maxMagY;
    protected float minMagZ;
    protected float maxMagZ;

    protected float rangeMagX = 1f;
    protected float rangeMagY = 1f;
    protected float rangeMagZ = 1f;

    protected float lastMagX;
    protected float lastMagY;
    protected float lastMagZ;


    public MagnetXYZ() {
        this("MagnetXYZ");
    }

    public MagnetXYZ(String label) {
        super(label);
        addParameter("magx", this.magX);
        addParameter("magy", this.magY);
        addParameter("magz", this.magZ);
    }

    @Override
    public void onStart() {
        super.onStart();
        magX.addListener((p) -> {
            //updatePointPositions(0, paramValueToAngle(p.getValuef()));
        });
        magY.addListener((p) -> {
            //updatePointPositions(1, paramValueToAngle(p.getValuef()));
        });
        magZ.addListener((p) -> {
            //updatePointPositions(2, paramValueToAngle(p.getValuef()));
        });
    }


    protected float updateMagX(float mgX) {
        lastMagX = mgX;
        if (mgX < minMagX) {
            minMagX = mgX;
        } else if (mgX > maxMagX) {
            maxMagX = mgX;
        }
        rangeMagX = maxMagX - minMagX;
        return 0f;
    }

    protected float updateMagY(float mgY) {
        lastMagY = mgY;
        if (mgY < minMagY) {
            minMagY = mgY;
        } else if (mgY > maxMagY) {
            maxMagY = mgY;
        }
        rangeMagY = maxMagY - minMagY;
        return 0f;
    }

    protected float updateMagZ(float mgZ) {
        lastMagZ = mgZ;
        if (mgZ < minMagZ) {
            minMagZ = mgZ;
        } else if (mgZ > maxMagZ) {
            maxMagZ = mgZ;
        }
        rangeMagZ = maxMagZ - minMagZ;
        return 0f;
    }

    /**
     * Compute a normalized value based on the current magnetometer values.  Currently, just average the normalized
     * X, Y, and Z values.
     * @return Normalized value.
     */
    protected float computeNormalized() {
        float normX = (lastMagX - minMagX) / rangeMagX;
        float normY = (lastMagY - minMagY) / rangeMagY;
        float normZ = (lastMagZ - minMagZ) / rangeMagZ;
        return (normX + normY + normZ) / 3;
    }

    @Override
    protected double computeValue(double deltaMs) {
        // TODO(tracy): Attempt to generate some value from 0 to 1 that is continuous
        // based on the received values.  We should monitor values for awhile to
        // find the bounds.  Every once in awhile, we will recompute the bounds
        // for computing our normalization.
        return computeNormalized();
    }
}