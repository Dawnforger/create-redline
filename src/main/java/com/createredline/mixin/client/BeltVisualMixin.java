package com.createredline.mixin.client;

import com.createredline.client.TieredPartials;
import com.createredline.client.VisualSpeed;
import com.createredline.tier.ITieredBelt;
import com.llamalad7.mixinextras.injector.ModifyExpressionValue;
import com.simibubi.create.content.kinetics.base.KineticBlockEntityVisual;
import com.simibubi.create.content.kinetics.belt.BeltBlockEntity;
import com.simibubi.create.content.kinetics.belt.BeltVisual;
import dev.engine_room.flywheel.api.visualization.VisualizationContext;
import dev.engine_room.flywheel.lib.model.baked.PartialModel;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

@Mixin(value = BeltVisual.class, remap = false)
public abstract class BeltVisualMixin extends KineticBlockEntityVisual<BeltBlockEntity> {
    private BeltVisualMixin(VisualizationContext context, BeltBlockEntity blockEntity, float partialTick) {
        super(context, blockEntity, partialTick);
    }

    /** Pulleys built from tiered shafts show that tier's colours. */
    @ModifyExpressionValue(method = "getPulleyModel", at = @At(value = "FIELD",
            target = "Lcom/simibubi/create/AllPartialModels;BELT_PULLEY:Ldev/engine_room/flywheel/lib/model/baked/PartialModel;"))
    private PartialModel redline$tieredPulley(PartialModel original) {
        PartialModel tiered = blockEntity instanceof ITieredBelt belt ? TieredPartials.beltPulley(belt.redline$getPulleyTier()) : null;
        return tiered != null ? tiered : original;
    }

    @ModifyExpressionValue(method = "setup", at = @At(value = "INVOKE",
            target = "Lcom/simibubi/create/content/kinetics/belt/BeltBlockEntity;getSpeed()F"))
    private float redline$clampScroll(float speed) {
        return VisualSpeed.clampBelt(speed);
    }
}
