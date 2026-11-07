package com.createredline;

import com.createredline.client.RedlineClient;
import com.createredline.config.RedlineConfig;
import com.createredline.registry.ModBlockEntities;
import com.createredline.registry.ModBlocks;
import com.createredline.registry.ModComponents;
import com.createredline.registry.ModCreativeTab;
import com.createredline.registry.ModItems;
import com.createredline.registry.ModLang;
import com.simibubi.create.foundation.data.CreateRegistrate;
import com.simibubi.create.foundation.item.ItemDescription;
import com.simibubi.create.foundation.item.KineticStats;
import com.simibubi.create.foundation.item.TooltipModifier;
import net.createmod.catnip.lang.FontHelper;
import net.minecraft.resources.ResourceLocation;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.ModContainer;
import net.neoforged.fml.common.Mod;
import net.neoforged.fml.config.ModConfig;
import net.neoforged.fml.loading.FMLEnvironment;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Mod(CreateRedline.MOD_ID)
public class CreateRedline {
    public static final String MOD_ID = "create_redline";
    public static final Logger LOGGER = LoggerFactory.getLogger(MOD_ID);

    // Registrate is how Create (and nearly every Create addon) registers blocks, BEs, renderers and Flywheel visuals.
    public static final CreateRegistrate REGISTRATE = CreateRegistrate.create(MOD_ID)
            // Create-style tooltips: shift for the .tooltip.summary/.condition/.behaviour lang entries.
            .setTooltipModifierFactory(item -> new ItemDescription.Modifier(item, FontHelper.Palette.STANDARD_CREATE)
                    .andThen(TooltipModifier.mapNull(KineticStats.create(item))));

    public CreateRedline(IEventBus modBus, ModContainer container) {
        REGISTRATE.registerEventListeners(modBus);

        ModCreativeTab.register(modBus);
        ModComponents.register(modBus);
        ModBlocks.register();
        ModItems.register();
        ModBlockEntities.register();
        ModLang.register();

        container.registerConfig(ModConfig.Type.SERVER, RedlineConfig.SERVER_SPEC);
        container.registerConfig(ModConfig.Type.CLIENT, RedlineConfig.CLIENT_SPEC);

        if (FMLEnvironment.dist == Dist.CLIENT)
            RedlineClient.init(modBus);
    }

    public static ResourceLocation id(String path) {
        return ResourceLocation.fromNamespaceAndPath(MOD_ID, path);
    }
}
