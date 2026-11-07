package com.createredline.client;

import com.createredline.CreateRedline;
import com.createredline.client.ponder.RedlinePonderPlugin;
import com.createredline.registry.ModBlocks;
import com.createredline.registry.ModComponents;
import com.createredline.tier.KineticTier;
import net.createmod.ponder.foundation.PonderIndex;
import net.minecraft.client.renderer.item.ItemProperties;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.event.lifecycle.FMLClientSetupEvent;

/** Client-only bootstrap; only referenced behind a Dist.CLIENT check. */
public final class RedlineClient {
    private RedlineClient() {}

    public static void init(IEventBus modBus) {
        TieredPartials.init();
        PonderIndex.addPlugin(new RedlinePonderPlugin());
        modBus.addListener(RedlineClient::clientSetup);
    }

    private static void clientSetup(FMLClientSetupEvent event) {
        // Selects the coloured item model variant (see the overrides in models/item/steam_redliner.json).
        event.enqueueWork(() -> ItemProperties.register(ModBlocks.STEAM_REDLINER.get().asItem(),
                CreateRedline.id("rotor"),
                (stack, level, entity, seed) -> stack.getOrDefault(ModComponents.ROTOR_TIER.get(), KineticTier.BASE).ordinal()));
    }
}
