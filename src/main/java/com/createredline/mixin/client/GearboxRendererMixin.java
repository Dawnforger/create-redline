package com.createredline.mixin.client;

import com.createredline.client.VisualSpeed;
import com.llamalad7.mixinextras.injector.ModifyExpressionValue;
import com.simibubi.create.content.kinetics.gearbox.GearboxRenderer;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

/** Fallback (non-Flywheel) renderer path: clamp the speed used for the angle calculation. */
@Mixin(value = GearboxRenderer.class, remap = false)
public abstract class GearboxRendererMixin {

    @ModifyExpressionValue(method = "renderSafe", at = @At(value = "INVOKE", target = "Lcom/simibubi/create/content/kinetics/gearbox/GearboxBlockEntity;getSpeed()F"))
    private float redline$clamp(float speed) {
        return VisualSpeed.clamp(speed);
    }
}
