package art.lookingup.util;

import heronarts.lx.LX;

public class LXUtil {

  // Instead of passing LX around to every class that needs it, we can just set it here and then
  // reference it.  There is only a single LX instance per project, so this is safe.
  public static LX lx;

  public static void setLX(LX lx) {
    LXUtil.lx = lx;
  }

  public static LX lx() {
    return lx;
  }
}
