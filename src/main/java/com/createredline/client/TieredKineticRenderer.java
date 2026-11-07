package com.createredline.client;

import com.createredline.tier.TierRules;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.simibubi.create.content.kinetics.simpleRelays.BracketedKineticBlockEntity;
import com.simibubi.create.content.kinetics.simpleRelays.BracketedKineticBlockEntityRenderer;
import com.simibubi.create.content.kinetics.simpleRelays.ICogWheel;
import dev.engine_room.flywheel.api.visualization.VisualizationManager;
import net.createmod.catnip.render.CachedBuffers;
import net.createmod.catnip.render.SuperByteBuffer;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.core.Direction;
import net.minecraft.world.level.block.state.BlockState;

/** Non-Flywheel fallback; same tier-partial selection as {@link TieredKineticVisual}. */
public class TieredKineticRenderer extends BracketedKineticBlockEntityRenderer {

    public TieredKineticRenderer(BlockEntityRendererProvider.Context context) {
        super(context);
    }

    @Override
    protected void renderSafe(BracketedKineticBlockEntity be, float partialTicks, PoseStack ms,
                              MultiBufferSource buffer, int light, int overlay) {
        if (VisualizationManager.supportsVisualization(be.getLevel()))
            return;

        BlockState state = be.getBlockState();
        TieredPartials.Set partials = TieredPartials.of(TierRules.tierOf(state));
        VertexConsumer vc = buffer.getBuffer(RenderType.solid());
        Direction.Axis axis = getRotationAxisOf(be);
        Direction facing = Direction.fromAxisAndDirection(axis, Direction.AxisDirection.POSITIVE);

        if (!ICogWheel.isLargeCog(state)) {
            var model = ICogWheel.isSmallCog(state) ? partials.cogwheel() : partials.shaft();
            renderRotatingBuffer(be, CachedBuffers.partialFacingVertical(model, state, facing), ms, vc, light);
            return;
        }

        renderRotatingBuffer(be, CachedBuffers.partialFacingVertical(partials.largeCogShaftless(), state, facing),
                ms, vc, light);
        SuperByteBuffer shaft = CachedBuffers.partialFacingVertical(partials.cogShaft(), state, facing);
        kineticRotationTransform(shaft, be, axis, getAngleForLargeCogShaft(be, axis), light);
        shaft.renderInto(ms, vc);
    }
}
