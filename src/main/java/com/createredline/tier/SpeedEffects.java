package com.createredline.tier;

import com.createredline.CreateRedline;
import com.simibubi.create.content.kinetics.base.KineticBlockEntity;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.tick.LevelTickEvent;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

/**
 * World changes triggered by a speed change (overspeed breaks, glow updates) are applied at the end of the level
 * tick rather than mid-propagation, so we never touch a block while Create's RotationPropagator is still
 * recursing through it. Both re-read the speed first, so a transient spike within the tick does nothing.
 *
 * Glow changes are safe for the kinetic network: same block + same axis counts as "kinetically equivalent" in
 * KineticBlock#onPlace, so Create skips re-propagation (the same path it uses for waterlogging).
 */
@EventBusSubscriber(modid = CreateRedline.MOD_ID)
public final class SpeedEffects {
    private static final Map<Level, Set<BlockPos>> PENDING = new HashMap<>();

    private SpeedEffects() {}

    public static void onSpeedChanged(KineticBlockEntity be, float speed) {
        BlockState state = be.getBlockState();
        boolean overspeed = Math.abs(speed) > TierRules.maxRpmFor(be);
        boolean glowStale = state.hasProperty(GlowLevels.GLOW)
                && state.getValue(GlowLevels.GLOW) != GlowLevels.forSpeed(speed);
        if (overspeed || glowStale)
            PENDING.computeIfAbsent(be.getLevel(), l -> new HashSet<>()).add(be.getBlockPos().immutable());
    }

    @SubscribeEvent
    public static void onLevelTick(LevelTickEvent.Post event) {
        Level level = event.getLevel();
        Set<BlockPos> pending = PENDING.remove(level);
        if (pending == null)
            return;
        for (BlockPos pos : pending) {
            if (!(level.getBlockEntity(pos) instanceof KineticBlockEntity be))
                continue;
            float speed = be.getSpeed();
            if (Math.abs(speed) > TierRules.maxRpmFor(be)) {
                level.destroyBlock(pos, true);
                continue;
            }
            BlockState state = be.getBlockState();
            if (state.hasProperty(GlowLevels.GLOW)) {
                int glow = GlowLevels.forSpeed(speed);
                if (state.getValue(GlowLevels.GLOW) != glow)
                    level.setBlock(pos, state.setValue(GlowLevels.GLOW, glow), Block.UPDATE_ALL);
            }
        }
    }
}
