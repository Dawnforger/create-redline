package com.createredline.mixin.client;

import com.createredline.client.VisualSpeed;
import com.llamalad7.mixinextras.injector.ModifyExpressionValue;
import com.simibubi.create.content.kinetics.base.KineticBlockEntityRenderer;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

/** Fallback (non-Flywheel) renderer path: clamp the speed used for the angle calculation. */
@Mixin(value = KineticBlockEntityRenderer.class, remap = false)
public abstract class KineticBlockEntityRendererMixin {

    @ModifyExpressionValue(method = "getAngleForBe", at = @At(value = "INVOKE", target = "Lcom/simibubi/create/content/kinetics/base/KineticBlockEntity;getSpeed()F"))
    private static float redline$clamp(float speed) {
        return VisualSpeed.clamp(speed);
    }
}
