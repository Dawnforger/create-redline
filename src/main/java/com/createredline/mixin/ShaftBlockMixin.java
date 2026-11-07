package com.createredline.mixin;

import com.createredline.content.shaft.TieredShaftBlock;
import com.simibubi.create.content.kinetics.simpleRelays.ShaftBlock;
import net.minecraft.world.level.block.state.BlockState;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

/** Create only accepts its own shaft as a belt pulley (ShaftBlock.isShaft is used solely by belt code). */
@Mixin(value = ShaftBlock.class, remap = false)
public abstract class ShaftBlockMixin {
    @Inject(method = "isShaft", at = @At("HEAD"), cancellable = true)
    private static void redline$acceptTieredShafts(BlockState state, CallbackInfoReturnable<Boolean> cir) {
        if (state.getBlock() instanceof TieredShaftBlock)
            cir.setReturnValue(true);
    }
}
