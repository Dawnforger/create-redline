package com.createredline.client.ponder;

import com.createredline.CreateRedline;
import com.simibubi.create.infrastructure.ponder.AllCreatePonderTags;
import net.createmod.ponder.api.registration.PonderPlugin;
import net.createmod.ponder.api.registration.PonderSceneRegistrationHelper;
import net.createmod.ponder.api.registration.PonderTagRegistrationHelper;
import net.createmod.ponder.foundation.PonderIndex;
import net.minecraft.resources.ResourceLocation;

import java.util.function.BiConsumer;

public class RedlinePonderPlugin implements PonderPlugin {
    private static final ResourceLocation STEAM_REDLINER = CreateRedline.id("steam_redliner");

    @Override
    public String getModId() {
        return CreateRedline.MOD_ID;
    }

    @Override
    public void registerScenes(PonderSceneRegistrationHelper<ResourceLocation> helper) {
        // Schematic: assets/create_redline/ponder/steam_redliner.nbt (exported by RedlineGameTests).
        helper.forComponents(STEAM_REDLINER)
                .addStoryBoard("steam_redliner", TurbineScenes::turbine);
    }

    @Override
    public void registerTags(PonderTagRegistrationHelper<ResourceLocation> helper) {
        helper.addToTag(AllCreatePonderTags.KINETIC_SOURCES).add(STEAM_REDLINER);
    }

    /** Datagen hook: Ponder scene text lives in lang, generated from the default strings in the scenes. */
    public static void provideLang(BiConsumer<String, String> consumer) {
        PonderIndex.addPlugin(new RedlinePonderPlugin());
        PonderIndex.getLangAccess().provideLang(CreateRedline.MOD_ID, consumer);
    }
}
