package com.createredline.tier;

import net.minecraft.world.level.block.state.BlockState;

/** Implemented by every Redline kinetic block. Anything that doesn't implement it is {@link KineticTier#BASE}. */
public interface ITieredKinetic {
    KineticTier getTier();

    /** For blocks whose tier depends on their state (the Steam Redliner's rotor). */
    default KineticTier getTier(BlockState state) {
        return getTier();
    }
}
