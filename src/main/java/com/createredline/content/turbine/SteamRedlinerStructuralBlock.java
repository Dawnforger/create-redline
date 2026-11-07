package com.createredline.content.turbine;

import com.createredline.registry.ModBlocks;
import com.mojang.serialization.MapCodec;
import com.simibubi.create.api.equipment.goggles.IProxyHoveringInformation;
import com.simibubi.create.content.equipment.wrench.IWrenchable;
import com.simibubi.create.foundation.block.render.MultiPosDestructionHandler;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.particle.ParticleEngine;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.RandomSource;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.DirectionalBlock;
import net.minecraft.world.level.block.RenderShape;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.material.PushReaction;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.HitResult;
import net.neoforged.neoforge.client.extensions.common.IClientBlockExtensions;
import org.jetbrains.annotations.Nullable;

import java.util.HashSet;
import java.util.Set;

/**
 * The 8 invisible blocks around the turbine core that make up its 3x3 housing (the core renders the whole
 * model). FACING points one step toward the core: edges point at it directly, corners point at an edge.
 * Modelled on Create's WaterWheelStructuralBlock.
 */
public class SteamRedlinerStructuralBlock extends DirectionalBlock implements IWrenchable, IProxyHoveringInformation {
    public static final MapCodec<SteamRedlinerStructuralBlock> CODEC = simpleCodec(SteamRedlinerStructuralBlock::new);

    public SteamRedlinerStructuralBlock(Properties properties) {
        super(properties);
    }

    @Override
    protected MapCodec<? extends DirectionalBlock> codec() {
        return CODEC;
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
        super.createBlockStateDefinition(builder.add(FACING));
    }

    @Override
    public RenderShape getRenderShape(BlockState state) {
        return RenderShape.INVISIBLE;
    }

    @Override
    public PushReaction getPistonPushReaction(BlockState state) {
        return PushReaction.BLOCK;
    }

    @Override
    public InteractionResult onWrenched(BlockState state, UseOnContext context) {
        return InteractionResult.PASS;
    }

    @Override
    public ItemStack getCloneItemStack(BlockState state, HitResult target, LevelReader level, BlockPos pos, Player player) {
        return stillValid(level, pos, state, false)
                ? SteamRedlinerBlock.stackFor(level.getBlockState(getCore(level, pos, state)))
                : ModBlocks.STEAM_REDLINER.asStack();
    }

    @Override
    public BlockPos getInformationSource(Level level, BlockPos pos, BlockState state) {
        return stillValid(level, pos, state, false) ? getCore(level, pos, state) : pos;
    }

    @Override
    public void onRemove(BlockState state, Level level, BlockPos pos, BlockState newState, boolean isMoving) {
        if (stillValid(level, pos, state, false))
            level.destroyBlock(getCore(level, pos, state), true);
    }

    @Override
    public BlockState playerWillDestroy(Level level, BlockPos pos, BlockState state, Player player) {
        if (stillValid(level, pos, state, false)) {
            BlockPos core = getCore(level, pos, state);
            level.destroyBlockProgress(core.hashCode(), core, -1);
            if (!level.isClientSide() && player.isCreative())
                level.destroyBlock(core, false);
        }
        return super.playerWillDestroy(level, pos, state, player);
    }

    @Override
    public BlockState updateShape(BlockState state, Direction direction, BlockState neighbourState, LevelAccessor level,
                                  BlockPos pos, BlockPos neighbourPos) {
        // Orphaned (core gone) -> remove ourselves next tick; this cascades corner <- edge <- core.
        if (!stillValid(level, pos, state, false) && level instanceof Level l && !l.isClientSide()
                && !l.getBlockTicks().hasScheduledTick(pos, this))
            l.scheduleTick(pos, this, 1);
        return state;
    }

    @Override
    public void tick(BlockState state, ServerLevel level, BlockPos pos, RandomSource random) {
        if (!stillValid(level, pos, state, false))
            level.setBlockAndUpdate(pos, Blocks.AIR.defaultBlockState());
    }

    public static BlockPos getCore(BlockGetter level, BlockPos pos, BlockState state) {
        BlockPos target = pos.relative(state.getValue(FACING));
        BlockState targetState = level.getBlockState(target);
        if (targetState.getBlock() instanceof SteamRedlinerStructuralBlock)
            return getCore(level, target, targetState);
        return target;
    }

    public boolean stillValid(BlockGetter level, BlockPos pos, BlockState state, boolean directlyAdjacent) {
        if (!state.is(this))
            return false;
        Direction direction = state.getValue(FACING);
        BlockPos target = pos.relative(direction);
        BlockState targetState = level.getBlockState(target);
        if (!directlyAdjacent && stillValid(level, target, targetState, true))
            return true;
        return targetState.getBlock() instanceof SteamRedlinerBlock
                && targetState.getValue(SteamRedlinerBlock.FACING).getAxis() != direction.getAxis();
    }

    /** Hit/break particles and crack overlay are forwarded to the core so the whole 3x3 cracks together. */
    public static class RenderProperties implements IClientBlockExtensions, MultiPosDestructionHandler {
        @Override
        public boolean addDestroyEffects(BlockState state, Level level, BlockPos pos, ParticleEngine manager) {
            return true;
        }

        @Override
        public boolean addHitEffects(BlockState state, Level level, HitResult target, ParticleEngine manager) {
            if (target instanceof BlockHitResult bhr) {
                BlockPos pos = bhr.getBlockPos();
                if (ModBlocks.STEAM_REDLINER_STRUCTURE.get().stillValid(level, pos, state, false))
                    manager.crack(getCore(level, pos, state), bhr.getDirection());
                return true;
            }
            return IClientBlockExtensions.super.addHitEffects(state, level, target, manager);
        }

        @Override
        @Nullable
        public Set<BlockPos> getExtraPositions(ClientLevel level, BlockPos pos, BlockState state, int progress) {
            if (!ModBlocks.STEAM_REDLINER_STRUCTURE.get().stillValid(level, pos, state, false))
                return null;
            Set<BlockPos> set = new HashSet<>();
            set.add(getCore(level, pos, state));
            return set;
        }
    }
}
