package com.createredline.content.shaft;

import com.createredline.tier.GlowLevels;
import com.createredline.registry.ModBlockEntities;
import com.createredline.tier.KineticTier;
import com.createredline.tier.TierRules;
import com.simibubi.create.content.kinetics.base.KineticBlockEntity;
import com.simibubi.create.content.kinetics.simpleRelays.ShaftBlock;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;

/**
 * Same behaviour as Create's shaft. Not registered in Create's EncasingRegistry, so casings can't
 * silently downgrade it to a vanilla encased shaft (encased variants are a Phase 3 item).
 */
public class TieredShaftBlock extends ShaftBlock implements TierRules.ITieredShaft {
    private final KineticTier tier;

    public TieredShaftBlock(Properties properties, KineticTier tier) {
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
    public BlockEntityType<? extends KineticBlockEntity> getBlockEntityType() {
        return ModBlockEntities.TIERED_BRACKETED.get();
    }
}
