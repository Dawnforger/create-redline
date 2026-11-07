package com.createredline.content.cog;

import com.createredline.tier.GlowLevels;
import com.createredline.registry.ModBlockEntities;
import com.createredline.tier.ITieredKinetic;
import com.createredline.tier.KineticTier;
import com.simibubi.create.content.kinetics.base.KineticBlockEntity;
import com.simibubi.create.content.kinetics.simpleRelays.CogWheelBlock;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;

public class TieredCogWheelBlock extends CogWheelBlock implements ITieredKinetic {
    private final KineticTier tier;

    protected TieredCogWheelBlock(boolean large, Properties properties, KineticTier tier) {
        super(large, properties);
        registerDefaultState(defaultBlockState().setValue(GlowLevels.GLOW, 0));
        this.tier = tier;
    }

    public static TieredCogWheelBlock small(Properties p, KineticTier tier) {
        return new TieredCogWheelBlock(false, p, tier);
    }

    public static TieredCogWheelBlock large(Properties p, KineticTier tier) {
        return new TieredCogWheelBlock(true, p, tier);
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
        super.createBlockStateDefinition(builder);
        builder.add(GlowLevels.GLOW);
    }

    @Override
    public KineticTier getTier() {
        return tier;
    }

    @Override
    public BlockEntityType<? extends KineticBlockEntity> getBlockEntityType() {
        return ModBlockEntities.TIERED_BRACKETED.get();
    }
}
