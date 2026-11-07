package com.createredline.mixin;

import com.createredline.client.ClientBeltHooks;
import com.createredline.config.RedlineConfig;
import com.createredline.tier.ITieredBelt;
import com.createredline.tier.KineticTier;
import com.llamalad7.mixinextras.injector.ModifyReturnValue;
import com.simibubi.create.content.kinetics.belt.BeltBlockEntity;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.level.block.entity.BlockEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(value = BeltBlockEntity.class, remap = false)
public abstract class BeltBlockEntityMixin implements ITieredBelt {
    @Unique
    private KineticTier redline$beltTier = KineticTier.BASE;
    @Unique
    private KineticTier redline$pulleyTier = KineticTier.BASE;

    @Override
    public KineticTier redline$getBeltTier() {
        return redline$beltTier;
    }

    @Override
    public void redline$setBeltTier(KineticTier tier) {
        redline$beltTier = tier;
    }

    @Override
    public KineticTier redline$getPulleyTier() {
        return redline$pulleyTier;
    }

    @Override
    public void redline$setPulleyTier(KineticTier tier) {
        redline$pulleyTier = tier;
    }

    @Inject(method = "write", at = @At("TAIL"))
    private void redline$writeTier(CompoundTag tag, HolderLookup.Provider registries, boolean clientPacket, CallbackInfo ci) {
        if (redline$beltTier != KineticTier.BASE)
            tag.putString("RedlineBeltTier", redline$beltTier.getSerializedName());
        if (redline$pulleyTier != KineticTier.BASE)
            tag.putString("RedlinePulleyTier", redline$pulleyTier.getSerializedName());
    }

    // HEAD, not TAIL: Create's read() returns early on several paths.
    @Inject(method = "read", at = @At("HEAD"))
    private void redline$readTier(CompoundTag tag, HolderLookup.Provider registries, boolean clientPacket, CallbackInfo ci) {
        KineticTier pulleyBefore = redline$pulleyTier;
        redline$beltTier = redline$parse(tag.getString("RedlineBeltTier"));
        redline$pulleyTier = redline$parse(tag.getString("RedlinePulleyTier"));
        // The visual chose its pulley model when created, possibly before this data arrived: rebuild it.
        BlockEntity self = (BlockEntity) (Object) this;
        if (pulleyBefore != redline$pulleyTier && self.hasLevel() && self.getLevel().isClientSide)
            ClientBeltHooks.rebuildVisual(self);
    }

    @Unique
    private static KineticTier redline$parse(String name) {
        for (KineticTier tier : KineticTier.values())
            if (tier.getSerializedName().equals(name))
                return tier;
        return KineticTier.BASE;
    }

    /**
     * Items advance speed/480 blocks per tick, and funnel/tunnel/processing checks only look one step ahead, so
     * past 480 RPM items could skip them. A tiered belt may carry rotation far faster; its items stay capped.
     */
    @ModifyReturnValue(method = "getBeltMovementSpeed", at = @At("RETURN"))
    private float redline$capItemSpeed(float speed) {
        float max = RedlineConfig.beltMaxItemRpm() / 480f;
        return Math.max(-max, Math.min(max, speed));
    }
}
