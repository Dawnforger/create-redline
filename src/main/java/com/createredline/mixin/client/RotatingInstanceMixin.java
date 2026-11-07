package com.createredline.mixin.client;

import com.createredline.client.VisualSpeed;
import com.simibubi.create.content.kinetics.base.RotatingInstance;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyVariable;

/** Every Flywheel rotating visual (shafts, cogs, gearbox halves, ...) funnels its speed through here. */
@Mixin(value = RotatingInstance.class, remap = false)
public abstract class RotatingInstanceMixin {

    @ModifyVariable(method = "setRotationalSpeed", at = @At("HEAD"), argsOnly = true)
    private float redline$clampVisualSpeed(float instanceSpeed) {
        // Instance speed is RPM * SPEED_MULTIPLIER.
        return VisualSpeed.clamp(instanceSpeed / RotatingInstance.SPEED_MULTIPLIER) * RotatingInstance.SPEED_MULTIPLIER;
    }
}
