package com.createredline.compat.emi;

import com.createredline.registry.ModBlocks;
import com.createredline.registry.ModComponents;
import dev.emi.emi.api.EmiEntrypoint;
import dev.emi.emi.api.EmiPlugin;
import dev.emi.emi.api.EmiRegistry;
import dev.emi.emi.api.stack.Comparison;

/** Only loaded by EMI's own entrypoint scan. Same job as the JEI plugin: one index entry per Redliner rotor. */
@EmiEntrypoint
public class RedlineEmiPlugin implements EmiPlugin {
    @Override
    public void register(EmiRegistry registry) {
        registry.setDefaultComparison(ModBlocks.STEAM_REDLINER.asItem(),
                Comparison.compareData(stack -> stack.get(ModComponents.ROTOR_TIER.get())));
    }
}
