package com.createredline.registry;

import com.createredline.CreateRedline;
import com.createredline.content.turbine.SteamRedlinerBlock;
import com.createredline.tier.KineticTier;
import com.simibubi.create.foundation.data.CreateRegistrate;
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

    /**
     * CreateRegistrate#setCreativeTab only records which entries belong here; the tab still has to list them.
     * Recipe viewers (JEI, EMI, REI) build their item index from creative tab contents, so anything left out
     * of every tab is invisible to them too.
     */
    public static final DeferredHolder<CreativeModeTab, CreativeModeTab> MAIN = TABS.register("main",
            () -> CreativeModeTab.builder()
                    .title(Component.translatable("itemGroup.create_redline.main"))
                    .icon(() -> new ItemStack(ModBlocks.LARGE_COGWHEELS.get(KineticTier.UNOBTAINIUM).get()))
                    .displayItems((params, output) -> {
                        for (var entry : CreateRedline.REGISTRATE.getAll(Registries.ITEM)) {
                            if (!CreateRegistrate.isInCreativeTab(entry, ModCreativeTab.MAIN))
                                continue;
                            // The Steam Redliner is only ever crafted with a tiered rotor; list one per tier.
                            if (entry.get() == ModBlocks.STEAM_REDLINER.asItem()) {
                                for (KineticTier tier : KineticTier.values())
                                    if (tier != KineticTier.BASE)
                                        output.accept(SteamRedlinerBlock.stackFor(
                                                ModBlocks.STEAM_REDLINER.getDefaultState().setValue(SteamRedlinerBlock.ROTOR, tier)));
                                continue;
                            }
                            output.accept(entry.get());
                        }
                    })
                    .build());

    private ModCreativeTab() {}

    public static void register(IEventBus modBus) {
        TABS.register(modBus);
    }
}
