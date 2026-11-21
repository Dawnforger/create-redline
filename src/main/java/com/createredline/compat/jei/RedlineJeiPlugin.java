package com.createredline.compat.jei;

import com.createredline.CreateRedline;
import com.createredline.registry.ModBlocks;
import com.createredline.registry.ModComponents;
import com.createredline.tier.KineticTier;
import mezz.jei.api.IModPlugin;
import mezz.jei.api.JeiPlugin;
import mezz.jei.api.ingredients.subtypes.ISubtypeInterpreter;
import mezz.jei.api.ingredients.subtypes.UidContext;
import mezz.jei.api.registration.ISubtypeRegistration;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;

/**
 * Only loaded by JEI's own plugin scan. Items themselves reach JEI through the creative tab; this just keeps the
 * three Steam Redliner rotors apart, since they're one item that differs only by its rotor component.
 */
@JeiPlugin
public class RedlineJeiPlugin implements IModPlugin {
    @Override
    public ResourceLocation getPluginUid() {
        return CreateRedline.id("jei_plugin");
    }

    @Override
    public void registerItemSubtypes(ISubtypeRegistration registration) {
        registration.registerSubtypeInterpreter(ModBlocks.STEAM_REDLINER.asItem(), new ISubtypeInterpreter<>() {
            @Override
            public Object getSubtypeData(ItemStack stack, UidContext context) {
                return stack.get(ModComponents.ROTOR_TIER.get());
            }

            @Override
            public String getLegacyStringSubtypeInfo(ItemStack stack, UidContext context) {
                return stack.getOrDefault(ModComponents.ROTOR_TIER.get(), KineticTier.BASE).getSerializedName();
            }
        });
    }
}
