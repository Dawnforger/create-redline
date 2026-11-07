package com.createredline.mixin;

import com.createredline.tier.KineticTier;
import com.createredline.tier.TierRules;
import com.llamalad7.mixinextras.injector.ModifyExpressionValue;
import com.simibubi.create.content.kinetics.RotationPropagator;
import com.simibubi.create.content.kinetics.base.KineticBlockEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

/**
 * Two hooks into Create's network propagation:
 *
 *  1. Global ceiling: Create's propagator destroys a block when the conveyed speed exceeds
 *     {@code maxRotationSpeed}. We raise *that one check* to the highest tier's cap; per-block caps
 *     (including 256 for every vanilla part) are then enforced by {@link KineticBlockEntityMixin} +
 *     {@link com.createredline.tier.SpeedEffects}. Other users of the config (creative motor,
 *     speed controller range) are untouched.
 *
 *  2. Attachment rule: links that violate {@link TierRules#canLink} get a speed modifier of 0, i.e. they
 *     simply don't connect (same as two shafts on perpendicular axes).
 *
 * VERIFY on first compile: `javap -c -p` RotationPropagator from the Create jar and confirm the config getter
 * descriptor below. If Create changed ConfigInt to override get() with an Integer return, change the target to
 * `...ConfigInt;get()Ljava/lang/Integer;` and the handler types accordingly.
 */
@Mixin(value = RotationPropagator.class, remap = false)
public abstract class RotationPropagatorMixin {

    @ModifyExpressionValue(
            method = "*",
            at = @At(value = "INVOKE", target = "Lnet/createmod/catnip/config/ConfigBase$ConfigInt;get()Ljava/lang/Object;"))
    private static Object redline$raiseGlobalCeiling(Object original) {
        return KineticTier.UNOBTAINIUM.maxRpm();
    }

    @Inject(method = "getRotationSpeedModifier", at = @At("HEAD"), cancellable = true)
    private static void redline$enforceTierAttachment(KineticBlockEntity from, KineticBlockEntity to,
                                                        CallbackInfoReturnable<Float> cir) {
        if (!TierRules.canLink(from, to))
            cir.setReturnValue(0f);
    }
}
