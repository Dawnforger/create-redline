package com.createredline.content.turbine;

import com.createredline.client.TieredPartials;
import com.simibubi.create.content.kinetics.base.KineticBlockEntityRenderer;
import net.createmod.catnip.render.CachedBuffers;
import net.createmod.catnip.render.SuperByteBuffer;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.world.level.block.state.BlockState;

/** Non-Flywheel fallback: spins the rotor partial (authored facing UP) around the turbine axis. */
public class SteamRedlinerRenderer extends KineticBlockEntityRenderer<SteamRedlinerBlockEntity> {

    public SteamRedlinerRenderer(BlockEntityRendererProvider.Context context) {
        super(context);
    }

    @Override
    protected SuperByteBuffer getRotatedModel(SteamRedlinerBlockEntity be, BlockState state) {
        return CachedBuffers.partialFacingVertical(TieredPartials.turbineRotor(state.getValue(SteamRedlinerBlock.ROTOR)), state, state.getValue(SteamRedlinerBlock.FACING));
    }
}
