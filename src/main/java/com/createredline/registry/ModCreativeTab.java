package com.createredline.registry;

import com.createredline.CreateRedline;
import com.createredline.tier.KineticTier;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.ItemStack;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;

public final class ModCreativeTab {
    private static final DeferredRegister<CreativeModeTab> TABS =
            DeferredRegister.create(Registries.CREATIVE_MODE_TAB, CreateRedline.MOD_ID);

    public static final DeferredHolder<CreativeModeTab, CreativeModeTab> MAIN = TABS.register("main",
            () -> CreativeModeTab.builder()
                    .title(Component.translatable("itemGroup.create_redline.main"))
                    .icon(() -> new ItemStack(ModBlocks.LARGE_COGWHEELS.get(KineticTier.UNOBTAINIUM).get()))
                    .build());

    private ModCreativeTab() {}

    public static void register(IEventBus modBus) {
        TABS.register(modBus);
    }
}
