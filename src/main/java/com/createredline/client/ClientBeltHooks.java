package com.createredline.client;

import dev.engine_room.flywheel.api.visualization.VisualizationManager;
import net.minecraft.world.level.block.entity.BlockEntity;

/** Client-only; called from BeltBlockEntityMixin behind an isClientSide check. */
public final class ClientBeltHooks {
    private ClientBeltHooks() {}

    /**
     * The belt visual picks its pulley model once, when it's created, which can happen before the tier arrives
     * from the server. Recreate it so the pulley shows the right tier.
     */
    public static void rebuildVisual(BlockEntity be) {
        VisualizationManager manager = VisualizationManager.get(be.getLevel());
        if (manager == null)
            return;
        manager.blockEntities().queueRemove(be);
        manager.blockEntities().queueAdd(be);
    }
}
