package com.createredline.mixin.client;

import com.createredline.client.TieredPartials;
import com.createredline.client.VisualSpeed;
import com.createredline.tier.ITieredBelt;
import com.llamalad7.mixinextras.injector.ModifyExpressionValue;
import com.llamalad7.mixinextras.sugar.Local;
import com.simibubi.create.content.kinetics.belt.BeltBlockEntity;
import com.simibubi.create.content.kinetics.belt.BeltRenderer;
import dev.engine_room.flywheel.lib.model.baked.PartialModel;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

/** Non-Flywheel fallback for {@link BeltVisualMixin}. */
@Mixin(value = BeltRenderer.class, remap = false)
public abstract class BeltRendererMixin {
    private static final String RENDER_SAFE = "renderSafe(Lcom/simibubi/create/content/kinetics/belt/BeltBlockEntity;"
            + "FLcom/mojang/blaze3d/vertex/PoseStack;Lnet/minecraft/client/renderer/MultiBufferSource;II)V";

    @ModifyExpressionValue(method = RENDER_SAFE, at = @At(value = "FIELD",
            target = "Lcom/simibubi/create/AllPartialModels;BELT_PULLEY:Ldev/engine_room/flywheel/lib/model/baked/PartialModel;"))
    private PartialModel redline$tieredPulley(PartialModel original, @Local(argsOnly = true) BeltBlockEntity be) {
        PartialModel tiered = be instanceof ITieredBelt belt ? TieredPartials.beltPulley(belt.redline$getPulleyTier()) : null;
        return tiered != null ? tiered : original;
    }

    @ModifyExpressionValue(method = RENDER_SAFE, at = @At(value = "INVOKE",
            target = "Lcom/simibubi/create/content/kinetics/belt/BeltBlockEntity;getSpeed()F"))
    private float redline$clampScroll(float speed) {
        return VisualSpeed.clampBelt(speed);
    }
}
