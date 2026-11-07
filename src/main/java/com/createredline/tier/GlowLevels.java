package com.createredline.tier;

import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.IntegerProperty;

/**
 * Past the base cap, tiered parts stop spinning faster on screen (see client VisualSpeed) and instead emit real
 * block light: one step per speed doubling. With default configs: 512 -> 3, 1024 -> 5, 2048 -> 8, 4096 -> 10,
 * 8192 -> 13, 16384 -> 15.
 */
public final class GlowLevels {
    public static final IntegerProperty GLOW = IntegerProperty.create("glow", 0, 15);

    private GlowLevels() {}

    public static int forSpeed(float speed) {
        float ratio = Math.abs(speed) / KineticTier.BASE.maxRpm();
        if (ratio <= 1)
            return 0;
        int steps = (int) Math.ceil(Math.log(ratio) / Math.log(2));
        return Math.min(15, Math.round(steps * 2.5f));
    }

    public static int lightOf(BlockState state) {
        return state.hasProperty(GLOW) ? state.getValue(GLOW) : 0;
    }
}
