package com.createredline.client;

import com.createredline.tier.TierRules;
import com.simibubi.create.content.kinetics.base.KineticBlockEntityRenderer;
import com.simibubi.create.content.kinetics.base.RotatingInstance;
import com.simibubi.create.content.kinetics.base.SingleAxisRotatingVisual;
import com.simibubi.create.content.kinetics.simpleRelays.BracketedKineticBlockEntity;
import com.simibubi.create.content.kinetics.simpleRelays.BracketedKineticBlockEntityRenderer;
import com.simibubi.create.content.kinetics.simpleRelays.ICogWheel;
import com.simibubi.create.foundation.render.AllInstanceTypes;
import dev.engine_room.flywheel.api.instance.Instance;
import dev.engine_room.flywheel.api.visual.BlockEntityVisual;
import dev.engine_room.flywheel.api.visualization.VisualizationContext;
import dev.engine_room.flywheel.lib.model.Models;
import net.minecraft.core.Direction;
import net.minecraft.world.level.block.state.BlockState;

import java.util.function.Consumer;

/**
 * Mirror of Create's BracketedKineticBlockEntityVisual, which hard-codes AllBlocks.COGWHEEL (so any other small
 * cog fell back to a bare shaft) and Create's wooden partials. This one picks the tier's own partials.
 */
public final class TieredKineticVisual {
    private TieredKineticVisual() {}

    public static BlockEntityVisual<BracketedKineticBlockEntity> create(VisualizationContext context,
                                                                        BracketedKineticBlockEntity be, float partialTick) {
        BlockState state = be.getBlockState();
        TieredPartials.Set partials = TieredPartials.of(TierRules.tierOf(state));
        if (ICogWheel.isLargeCog(state))
            return new LargeCog(context, be, partialTick, partials);
        return new SingleAxisRotatingVisual<>(context, be, partialTick,
                Models.partial(ICogWheel.isSmallCog(state) ? partials.cogwheel() : partials.shaft()));
    }

    /** Large cogs draw a shaftless wheel plus a separate shaft offset by 11.25 degrees so the teeth mesh. */
    public static class LargeCog extends SingleAxisRotatingVisual<BracketedKineticBlockEntity> {
        private final RotatingInstance additionalShaft;

        LargeCog(VisualizationContext context, BracketedKineticBlockEntity be, float partialTick, TieredPartials.Set partials) {
            super(context, be, partialTick, Models.partial(partials.largeCogShaftless()));
            Direction.Axis axis = KineticBlockEntityRenderer.getRotationAxisOf(be);
            additionalShaft = instancerProvider().instancer(AllInstanceTypes.ROTATING, Models.partial(partials.cogShaft()))
                    .createInstance();
            additionalShaft.rotateToFace(axis)
                    .setup(be)
                    .setRotationOffset(BracketedKineticBlockEntityRenderer.getShaftAngleOffset(axis, pos))
                    .setPosition(getVisualPosition())
                    .setChanged();
        }

        @Override
        public void update(float pt) {
            super.update(pt);
            additionalShaft.setup(blockEntity)
                    .setRotationOffset(BracketedKineticBlockEntityRenderer.getShaftAngleOffset(rotationAxis(), pos))
                    .setChanged();
        }

        @Override
        public void updateLight(float partialTick) {
            super.updateLight(partialTick);
            relight(additionalShaft);
        }

        @Override
        protected void _delete() {
            super._delete();
            additionalShaft.delete();
        }

        @Override
        public void collectCrumblingInstances(Consumer<Instance> consumer) {
            super.collectCrumblingInstances(consumer);
            consumer.accept(additionalShaft);
        }
    }
}
