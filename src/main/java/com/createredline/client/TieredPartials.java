package com.createredline.client;

import com.createredline.CreateRedline;
import com.createredline.tier.KineticTier;
import dev.engine_room.flywheel.lib.model.baked.PartialModel;

import java.util.EnumMap;
import java.util.Map;

/** Our partial models: tier-textured counterparts of the Create partials its visuals hard-code, plus the turbine rotor. */
public final class TieredPartials {
    public record Set(PartialModel shaft, PartialModel cogwheel, PartialModel largeCogShaftless, PartialModel cogShaft,
                      PartialModel turbineRotor, PartialModel beltPulley) {}

    private static final Map<KineticTier, Set> BY_TIER = new EnumMap<>(KineticTier.class);

    /** Plain iron Steam Redliner rotor (turbines without a rotor tier), authored facing UP. */
    public static PartialModel TURBINE_ROTOR;

    private TieredPartials() {}

    public static void init() {
        TURBINE_ROTOR = block("steam_redliner/rotor");
        for (KineticTier tier : KineticTier.values()) {
            if (tier == KineticTier.BASE)
                continue;
            String t = tier.getSerializedName();
            BY_TIER.put(tier, new Set(
                    block(t + "_shaft"),
                    block(t + "_cogwheel"),
                    block(t + "_large_cogwheel_shaftless"),
                    block(t + "_cogwheel_shaft"),
                    block("steam_redliner/rotor_" + t),
                    block(t + "_belt_pulley")));
        }
    }

    public static Set of(KineticTier tier) {
        return BY_TIER.get(tier);
    }

    /** Null for untiered pulleys, so callers keep Create's own model. */
    public static PartialModel beltPulley(KineticTier tier) {
        return tier == KineticTier.BASE ? null : BY_TIER.get(tier).beltPulley();
    }

    public static PartialModel turbineRotor(KineticTier tier) {
        return tier == KineticTier.BASE ? TURBINE_ROTOR : BY_TIER.get(tier).turbineRotor();
    }

    private static PartialModel block(String path) {
        return PartialModel.of(CreateRedline.id("block/" + path));
    }
}
