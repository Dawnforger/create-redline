package com.createredline.content.gearbox;

import com.createredline.tier.GlowLevels;
import com.createredline.registry.ModBlockEntities;
import com.createredline.tier.ITieredKinetic;
import com.createredline.tier.KineticTier;
import com.simibubi.create.content.kinetics.gearbox.GearboxBlock;
import com.simibubi.create.content.kinetics.gearbox.GearboxBlockEntity;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;

/** Gearbox that only links to shafts of its own tier or stronger (enforced in TierRules via the propagator mixin). */
public class TieredGearboxBlock extends GearboxBlock implements ITieredKinetic {
    private final KineticTier tier;

    public TieredGearboxBlock(Properties properties, KineticTier tier) {
        super(properties);
        registerDefaultState(defaultBlockState().setValue(GlowLevels.GLOW, 0));
        this.tier = tier;
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
    public BlockEntityType<? extends GearboxBlockEntity> getBlockEntityType() {
        return ModBlockEntities.TIERED_GEARBOX.get();
    }
}
