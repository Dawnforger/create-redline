package com.createredline.mixin;

import com.createredline.registry.ModBlocks;
import com.createredline.tier.ITieredBelt;
import com.createredline.tier.KineticTier;
import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import com.llamalad7.mixinextras.sugar.Local;
import com.simibubi.create.content.kinetics.belt.BeltBlock;
import com.tterrag.registrate.util.entry.BlockEntry;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;

/**
 * Everywhere Create turns a belt pulley back into a shaft it hard-codes AllBlocks.SHAFT; give back the tier of
 * shaft that pulley was built from instead.
 */
@Mixin(value = BeltBlock.class, remap = false)
public abstract class BeltBlockMixin {
    private static final String SHAFT_STATE = "Lcom/tterrag/registrate/util/entry/BlockEntry;getDefaultState()Lnet/minecraft/world/level/block/state/BlockState;";
    private static final String SHAFT_STACK = "Lcom/tterrag/registrate/util/entry/BlockEntry;asStack()Lnet/minecraft/world/item/ItemStack;";

    /** Neighbouring pulleys revert to shafts when a belt chain is destroyed. */
    @WrapOperation(method = "onRemove", at = @At(value = "INVOKE", target = SHAFT_STATE))
    private BlockState redline$tieredShaftOnRemove(BlockEntry<?> entry, Operation<BlockState> original,
                                                     @Local BlockEntity blockEntity) {
        return redline$shaftState(blockEntity, entry, original);
    }

    /** The broken pulley segment itself drops its shaft. */
    @WrapOperation(method = "getDrops", at = @At(value = "INVOKE", target = SHAFT_STATE))
    private BlockState redline$tieredShaftDrop(BlockEntry<?> entry, Operation<BlockState> original,
                                                 @Local BlockEntity blockEntity) {
        return redline$shaftState(blockEntity, entry, original);
    }

    /** Wrenching a pulley off a belt hands the shaft back. */
    @WrapOperation(method = "onWrenched", at = @At(value = "INVOKE", target = SHAFT_STACK))
    private ItemStack redline$tieredShaftOnWrench(BlockEntry<?> entry, Operation<ItemStack> original,
                                                    @Local(argsOnly = true) UseOnContext context) {
        if (context.getLevel().getBlockEntity(context.getClickedPos()) instanceof ITieredBelt belt
                && belt.redline$getPulleyTier() != KineticTier.BASE) {
            KineticTier tier = belt.redline$getPulleyTier();
            belt.redline$setPulleyTier(KineticTier.BASE); // no longer a pulley
            return ModBlocks.SHAFTS.get(tier).asStack();
        }
        return original.call(entry);
    }

    @Unique
    private static BlockState redline$shaftState(BlockEntity blockEntity, BlockEntry<?> entry, Operation<BlockState> original) {
        if (blockEntity instanceof ITieredBelt belt && belt.redline$getPulleyTier() != KineticTier.BASE)
            return ModBlocks.SHAFTS.get(belt.redline$getPulleyTier()).getDefaultState();
        return original.call(entry);
    }
}
