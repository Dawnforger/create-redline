package com.createredline.tier;

import com.createredline.CreateRedline;
import com.simibubi.create.foundation.utility.CreateLang;
import net.createmod.catnip.lang.LangBuilder;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;

import java.util.List;

/**
 * Goggle lines for Redline parts. Create shows nothing for plain transmission parts, and the speed is the one
 * number that matters for a tiered one: how close it is to the cap that will break it.
 */
public final class TierGoggles {
    private TierGoggles() {}

    public static void addSpeed(List<Component> tooltip, float speed, KineticTier tier) {
        int max = tier.maxRpm();
        float abs = Math.abs(speed);
        ChatFormatting colour = abs > max ? ChatFormatting.RED : abs > max * 0.75f ? ChatFormatting.GOLD : ChatFormatting.AQUA;

        lang().translate("goggles.speed").style(ChatFormatting.GRAY).forGoggles(tooltip);
        CreateLang.number(abs).add(CreateLang.translate("generic.unit.rpm")).style(colour)
                .add(lang().translate("goggles.rated",
                        CreateLang.number(max).add(CreateLang.translate("generic.unit.rpm")).component(),
                        Component.translatable("create_redline.tier." + tier.getSerializedName())).style(ChatFormatting.DARK_GRAY))
                .forGoggles(tooltip, 1);
    }

    private static LangBuilder lang() {
        return new LangBuilder(CreateRedline.MOD_ID);
    }
}
