package com.createredline.registry;

import com.createredline.CreateRedline;
import com.createredline.tier.KineticTier;
import net.minecraft.core.component.DataComponentType;
import net.minecraft.core.registries.Registries;
import net.minecraft.util.StringRepresentable;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.network.codec.NeoForgeStreamCodecs;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;

public final class ModComponents {
    private static final DeferredRegister.DataComponents COMPONENTS =
            DeferredRegister.createDataComponents(Registries.DATA_COMPONENT_TYPE, CreateRedline.MOD_ID);

    /** Which tier's cog a Steam Redliner was crafted with; only changes the rotor colour. */
    public static final DeferredHolder<DataComponentType<?>, DataComponentType<KineticTier>> ROTOR_TIER =
            COMPONENTS.registerComponentType("rotor_tier", b -> b
                    .persistent(StringRepresentable.fromEnum(KineticTier::values))
                    .networkSynchronized(NeoForgeStreamCodecs.enumCodec(KineticTier.class)));

    private ModComponents() {}

    public static void register(IEventBus modBus) {
        COMPONENTS.register(modBus);
    }
}
