package com.createredline.registry;

import com.createredline.client.ponder.RedlinePonderPlugin;
import com.createredline.content.turbine.SteamRedlinerBlockEntity;
import com.tterrag.registrate.providers.ProviderType;
import com.createredline.tier.KineticTier;

import java.util.Locale;

import static com.createredline.CreateRedline.REGISTRATE;

/** Non-block lang entries. Block names come from Registrate's defaults (e.g. "Vibranium Large Cogwheel"). */
public final class ModLang {
    private ModLang() {}

    public static void register() {
        REGISTRATE.addRawLang("itemGroup.create_redline.main", "Create: Redline");

        String[] rpm = {null, "1,024", "4,096", "16,384"};
        for (KineticTier tier : KineticTier.values()) {
            if (tier == KineticTier.BASE)
                continue;
            String t = tier.getSerializedName();
            String name = Character.toUpperCase(t.charAt(0)) + t.substring(1);
            for (String kind : new String[] {"shaft", "cogwheel", "large_cogwheel", "gearbox"})
                REGISTRATE.addRawLang("block.create_redline." + t + "_" + kind + ".tooltip.summary",
                        "Rated for up to _" + rpm[tier.ordinal()] + " RPM_, glowing brighter past 256. "
                                + "Only links to shafts of _" + name + "_ tier or stronger."
                                + (kind.equals("shaft") ? " _Belts_ built between these shafts carry the same speeds." : ""));
        }

        String turbine = "block.create_redline.steam_redliner.tooltip.";
        REGISTRATE.addRawLang(turbine + "summary", "A high-performance _generator_ that covers a whole face of a "
                + "_3x3x3 boiler_, giving _4x_ the stress capacity of the _9 Steam Engines_ it replaces.");
        REGISTRATE.addRawLang(turbine + "condition1", "When placed on a boiler");
        REGISTRATE.addRawLang(turbine + "behaviour1", "Click the _centre_ of a tank side or its top. Forms a _3x3_ "
                + "housing and needs all _9 Blaze Burners_ superheated with _Blaze Cake_, plus a steady _water_ supply.");
        REGISTRATE.addRawLang(turbine + "condition2", "Limits");
        REGISTRATE.addRawLang(turbine + "behaviour2", "Only _one Redliner_ per boiler, and no _Steam Engines_ on the same tank.");
        REGISTRATE.addRawLang(turbine + "condition3", "Rotor");
        REGISTRATE.addRawLang(turbine + "behaviour3", "Set by the _Large Cogwheel_ it was crafted around. Higher tiers spin faster for "
                + "the _same stress capacity_: _Allthemodium_ 512 RPM, _Vibranium_ 2,048 RPM, _Unobtainium_ 8,192 RPM. "
                + "The output shaft must be the rotor's tier or better.");

        REGISTRATE.addRawLang("create_redline.turbine.rotor", "Rotor: %s (%s RPM)");
        REGISTRATE.addRawLang("create_redline.turbine.one_per_boiler", "This boiler already has a Steam Redliner");
        for (KineticTier tier : KineticTier.values()) {
            String t = tier.getSerializedName();
            REGISTRATE.addRawLang("create_redline.tier." + t, Character.toUpperCase(t.charAt(0)) + t.substring(1));
        }

        status(SteamRedlinerBlockEntity.Status.RUNNING, "Redliner running");
        status(SteamRedlinerBlockEntity.Status.NO_TANK, "No fluid tank behind the Redliner");
        status(SteamRedlinerBlockEntity.Status.WRONG_SIZE, "Tank must be exactly 3x3x3");
        status(SteamRedlinerBlockEntity.Status.MISALIGNED, "The Redliner must cover a full face of the tank");
        status(SteamRedlinerBlockEntity.Status.SHARED_BOILER, "Another Redliner already runs this boiler");

        // Ponder scene text, generated from the default strings in the scenes (client classes; datagen only).
        REGISTRATE.addDataGenerator(ProviderType.LANG, prov -> RedlinePonderPlugin.provideLang(prov::add));
        status(SteamRedlinerBlockEntity.Status.NEEDS_SUPERHEAT, "All 9 Blaze Burners must be superheated");
        status(SteamRedlinerBlockEntity.Status.NO_WATER, "Insufficient water supply");
        status(SteamRedlinerBlockEntity.Status.ENGINES_ATTACHED, "Remove steam engines from this boiler");
    }

    private static void status(SteamRedlinerBlockEntity.Status status, String text) {
        REGISTRATE.addRawLang("create_redline.turbine.status." + status.name().toLowerCase(Locale.ROOT), text);
    }
}
