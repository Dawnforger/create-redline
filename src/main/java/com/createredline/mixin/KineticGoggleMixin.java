package com.createredline.mixin;

import com.createredline.tier.KineticTier;
import com.createredline.tier.TierGoggles;
import com.createredline.tier.TierRules;
import com.simibubi.create.content.kinetics.base.GeneratingKineticBlockEntity;
import com.simibubi.create.content.kinetics.base.KineticBlockEntity;
import com.simibubi.create.foundation.utility.CreateLang;
import net.minecraft.network.chat.Component;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.List;

/**
 * Tiered shafts, cogs, gearboxes and belts reuse Create's BE classes, which show nothing on goggles unless the
 * block has a stress impact. Add their speed against their tier's cap. Generators (the Steam Redliner) add their
 * own line after Create's generator stats instead.
 */
@Mixin(value = KineticBlockEntity.class, remap = false)
public abstract class KineticGoggleMixin {

    @Inject(method = "addToGoggleTooltip", at = @At("RETURN"), cancellable = true)
    private void redline$addTierSpeed(List<Component> tooltip, boolean isPlayerSneaking, CallbackInfoReturnable<Boolean> cir) {
        KineticBlockEntity self = (KineticBlockEntity) (Object) this;
        if (self instanceof GeneratingKineticBlockEntity)
            return;
        KineticTier tier = TierRules.tierOf(self);
        if (tier == KineticTier.BASE)
            return;
        if (!cir.getReturnValue())
            CreateLang.translate("gui.goggles.kinetic_stats").forGoggles(tooltip);
        TierGoggles.addSpeed(tooltip, self.getSpeed(), tier);
        cir.setReturnValue(true);
    }
}
