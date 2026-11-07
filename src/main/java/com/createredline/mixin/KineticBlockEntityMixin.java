package com.createredline.mixin;

import com.createredline.tier.SpeedEffects;
import com.simibubi.create.content.kinetics.base.KineticBlockEntity;
import net.minecraft.server.level.ServerLevel;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/** Every kinetic BE (vanilla Create included) checks its new speed against its tier cap and glow level. */
@Mixin(value = KineticBlockEntity.class, remap = false)
public abstract class KineticBlockEntityMixin {

    @Inject(method = "setSpeed", at = @At("TAIL"))
    private void redline$checkTierCap(float speed, CallbackInfo ci) {
        KineticBlockEntity self = (KineticBlockEntity) (Object) this;
        if (!(self.getLevel() instanceof ServerLevel))
            return;
        SpeedEffects.onSpeedChanged(self, speed);
    }
}
