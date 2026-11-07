package com.createredline.tier;

import com.createredline.config.RedlineConfig;
import com.simibubi.create.api.stress.BlockStressValues;
import com.simibubi.create.content.kinetics.base.KineticBlockEntity;
import com.simibubi.create.content.kinetics.simpleRelays.ICogWheel;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;

/**
 * The two rules of the mod, in one place. They only govern *transmission* parts (shafts, cogs, gearboxes, belts,
 * clutches...): blocks with neither a stress impact nor a stress capacity. Machines and generators (presses,
 * millstones, fans, steam engines...) run at whatever speed the network gives them and pay stress for it.
 *  1. Speed cap  - a transmission part breaks if its speed exceeds its own tier's cap (vanilla parts keep
 *                  Create's cap).
 *  2. Attachment - a shaft/gearbox connection along an axis only links if the shaft side is >= the tier of the
 *                  gear/gearbox it feeds. Cog-to-cog meshing is NOT tier-gated, so players can always gear
 *                  back down; the speed cap alone protects the lower-tier cog. Machines accept any tier.
 */
public final class TierRules {
    private TierRules() {}

    public static KineticTier tierOf(BlockState state) {
        Block block = state.getBlock();
        return block instanceof ITieredKinetic t ? t.getTier(state) : KineticTier.BASE;
    }

    /** Like {@link #tierOf(BlockState)}, but also knows belt tiers, which live on the block entity. */
    public static KineticTier tierOf(KineticBlockEntity be) {
        if (be instanceof ITieredBelt belt)
            return belt.redline$getBeltTier();
        return tierOf(be.getBlockState());
    }

    public static int maxRpmFor(KineticBlockEntity be) {
        BlockState state = be.getBlockState();
        if (!(state.getBlock() instanceof ITieredKinetic) && isMachine(state.getBlock()))
            return KineticTier.UNOBTAINIUM.maxRpm();
        return tierOf(be).maxRpm();
    }

    /** Consumers and generators, as opposed to pure transmission. Our own blocks are never "machines". */
    public static boolean isMachine(Block block) {
        return !(block instanceof ITieredKinetic)
                && (BlockStressValues.getImpact(block) > 0 || BlockStressValues.getCapacity(block) > 0);
    }

    public static boolean canLink(KineticBlockEntity from, KineticBlockEntity to) {
        BlockState a = from.getBlockState();
        BlockState b = to.getBlockState();
        KineticTier ta = tierOf(from);
        KineticTier tb = tierOf(to);
        if (ta == tb || !RedlineConfig.SERVER.strictAttachment.get())
            return true;

        if (isCogMesh(from.getBlockPos(), a, to.getBlockPos(), b))
            return true;

        BlockState lower = ta.atLeast(tb) ? b : a;
        if (isMachine(lower.getBlock()))
            return true;

        // Axial link: the lower-tier side must not be a gear/gearbox host of the higher tier.
        // Equivalently: the higher-tier piece must be a shaft or belt pulley (they may feed weaker parts).
        KineticBlockEntity higher = ta.atLeast(tb) ? from : to;
        return higher.getBlockState().getBlock() instanceof ITieredShaft || higher instanceof ITieredBelt;
    }

    private static boolean isCogMesh(BlockPos pa, BlockState a, BlockPos pb, BlockState b) {
        if (!ICogWheel.isSmallCog(a) && !ICogWheel.isLargeCog(a))
            return false;
        if (!ICogWheel.isSmallCog(b) && !ICogWheel.isLargeCog(b))
            return false;
        // Two cogs stacked directly along their axis are connected through their shafts, not their teeth.
        BlockPos diff = pb.subtract(pa);
        Direction dir = Direction.fromDelta(diff.getX(), diff.getY(), diff.getZ());
        return dir == null || dir.getAxis() != ((ICogWheel) a.getBlock()).getRotationAxis(a);
    }

    /** Marker for shaft-type pieces (they may feed equal-or-lower tier gears). */
    public interface ITieredShaft extends ITieredKinetic {}
}
