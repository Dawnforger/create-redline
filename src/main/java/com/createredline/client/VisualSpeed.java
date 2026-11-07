package com.createredline.client;

import com.createredline.config.RedlineConfig;

/**
 * Caps the speed parts are *drawn* at, so 16k RPM gears don't strobe. Purely cosmetic: the real speed (goggles,
 * stress, processing) is untouched. Past the cap, tiered parts glow instead (see GlowLevels).
 */
public final class VisualSpeed {
    private VisualSpeed() {}

    /** Belt texture scroll follows item movement, which is capped separately (see BeltBlockEntityMixin). */
    public static float clampBelt(float rpm) {
        int max = RedlineConfig.beltMaxItemRpm();
        return clamp(Math.abs(rpm) > max ? Math.copySign(max, rpm) : rpm);
    }

    public static float clamp(float rpm) {
        int max = RedlineConfig.CLIENT.maxVisualRpm.get();
        if (max <= 0 || Math.abs(rpm) <= max)
            return rpm;
        return Math.copySign(max, rpm);
    }
}
