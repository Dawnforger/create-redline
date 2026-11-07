package com.createredline.mixin;

import com.createredline.tier.ITieredBelt;
import com.createredline.tier.KineticTier;
import com.createredline.tier.TierRules;
import com.llamalad7.mixinextras.sugar.Local;
import com.simibubi.create.content.kinetics.belt.item.BeltConnectorItem;
import com.simibubi.create.content.kinetics.simpleRelays.ShaftBlock;
import com.simibubi.create.foundation.blockEntity.SmartBlockEntity;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.ModifyVariable;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/** Records which tier of shaft each belt segment replaced, then stamps the tiers onto the new belt. */
@Mixin(value = BeltConnectorItem.class, remap = false)
public abstract class BeltConnectorItemMixin {
    @Unique
    private static final ThreadLocal<List<BlockPos>> redline$segments = new ThreadLocal<>();
    @Unique
    private static final ThreadLocal<Map<BlockPos, KineticTier>> redline$shaftTiers = new ThreadLocal<>();

    // Runs when the chain positions are computed, before any shaft is replaced.
    @ModifyVariable(method = "createBelts", at = @At("STORE"), ordinal = 0)
    private static List<BlockPos> redline$captureShafts(List<BlockPos> segments, @Local(argsOnly = true) Level level) {
        Map<BlockPos, KineticTier> tiers = new HashMap<>();
        for (BlockPos pos : segments) {
            BlockState state = level.getBlockState(pos);
            if (ShaftBlock.isShaft(state))
                tiers.put(pos.immutable(), TierRules.tierOf(state));
        }
        redline$segments.set(segments);
        redline$shaftTiers.set(tiers);
        return segments;
    }

    // RETURN, not TAIL: the success path is an early `if (!failed) return;`.
    @Inject(method = "createBelts", at = @At("RETURN"))
    private static void redline$applyTiers(Level level, BlockPos start, BlockPos end, CallbackInfo ci) {
        List<BlockPos> segments = redline$segments.get();
        Map<BlockPos, KineticTier> tiers = redline$shaftTiers.get();
        redline$segments.remove();
        redline$shaftTiers.remove();
        if (segments == null || tiers == null || tiers.isEmpty())
            return;
        KineticTier beltTier = tiers.values().stream().min(Enum::compareTo).orElse(KineticTier.BASE);
        if (beltTier == KineticTier.BASE && tiers.values().stream().allMatch(t -> t == KineticTier.BASE))
            return;
        for (BlockPos pos : segments) {
            if (!(level.getBlockEntity(pos) instanceof ITieredBelt belt))
                continue;
            belt.redline$setBeltTier(beltTier);
            belt.redline$setPulleyTier(tiers.getOrDefault(pos, KineticTier.BASE));
            if (belt instanceof SmartBlockEntity be) {
                be.setChanged();
                be.sendData();
            }
        }
    }
}
