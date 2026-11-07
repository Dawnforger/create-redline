package com.createredline.registry;

import com.createredline.tier.KineticTier;
import com.tterrag.registrate.util.entry.ItemEntry;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.ItemTags;
import net.minecraft.world.item.Item;

import java.util.EnumMap;
import java.util.Map;

import static com.createredline.CreateRedline.REGISTRATE;

public final class ModItems {
    /** Pressed from ingots; deployed onto a shaft to upgrade it one tier. Tagged c:plates/<metal> for pack compat. */
    public static final Map<KineticTier, ItemEntry<Item>> SHEETS = new EnumMap<>(KineticTier.class);

    private ModItems() {}

    public static void register() {
        for (KineticTier tier : KineticTier.values()) {
            if (tier == KineticTier.BASE)
                continue;
            String t = tier.getSerializedName();
            SHEETS.put(tier, REGISTRATE.item(t + "_sheet", Item::new)
                    .tag(ItemTags.create(ResourceLocation.fromNamespaceAndPath("c", "plates/" + t)), ItemTags.create(ResourceLocation.fromNamespaceAndPath("c", "plates")))
                    // Hand-written model: its texture is a load-time recolour of Create's iron sheet (see
                    // tools/gen_tier_palettes.py), which datagen's existing-file check can't see.
                    .model((c, p) -> {})
                    .register());
        }
    }
}
