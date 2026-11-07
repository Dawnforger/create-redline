package com.createredline.config;

import net.neoforged.neoforge.common.ModConfigSpec;
import org.apache.commons.lang3.tuple.Pair;

/** Server config so pack devs (ATM team) can rebalance per pack without a rebuild. */
public final class RedlineConfig {
    public static final Server SERVER;
    public static final ModConfigSpec SERVER_SPEC;
    public static final Client CLIENT;
    public static final ModConfigSpec CLIENT_SPEC;

    static {
        Pair<Server, ModConfigSpec> pair = new ModConfigSpec.Builder().configure(Server::new);
        SERVER = pair.getLeft();
        SERVER_SPEC = pair.getRight();
        Pair<Client, ModConfigSpec> client = new ModConfigSpec.Builder().configure(Client::new);
        CLIENT = client.getLeft();
        CLIENT_SPEC = client.getRight();
    }

    private RedlineConfig() {}

    /** Safe outside a world (e.g. Ponder opened from the title screen), where server config isn't loaded. */
    public static int beltMaxItemRpm() {
        return SERVER_SPEC.isLoaded() ? SERVER.beltMaxItemRpm.get() : 256;
    }

    public static final class Server {
        public final ModConfigSpec.IntValue tierMultiplier;
        public final ModConfigSpec.BooleanValue strictAttachment;

        public final ModConfigSpec.IntValue turbineBaselineEngines;
        public final ModConfigSpec.IntValue steamEngineReferenceSU;
        public final ModConfigSpec.DoubleValue turbineMultiplier;
        public final ModConfigSpec.IntValue turbineOutputRpm;
        public final ModConfigSpec.IntValue turbineWaterPerTick;
        public final ModConfigSpec.IntValue beltMaxItemRpm;

        Server(ModConfigSpec.Builder b) {
            b.push("tiers");
            tierMultiplier = b
                    .comment("Each tier's RPM cap = Create's maxRotationSpeed * tierMultiplier^tierIndex.",
                            "Default 4 gives 256 -> 1024 -> 4096 -> 16384.")
                    .defineInRange("tierMultiplier", 4, 2, 16);
            strictAttachment = b
                    .comment("If true, a gear/gearbox only links axially to shafts of its own tier or higher.",
                            "Cog-to-cog meshing is never tier-gated (speed caps still apply).")
                    .define("strictAttachment", true);
            b.pop();

            b.push("belts");
            beltMaxItemRpm = b
                    .comment("Belts built on tiered shafts carry rotation at their tier's speed, but items on any belt move",
                            "at most this fast (RPM-equivalent). Above 480 items move >1 block/tick and can skip funnels,",
                            "tunnels and belt processing, so that is the hard limit.")
                    .defineInRange("maxItemRpm", 256, 16, 480);
            b.pop();

            b.push("steamRedliner");
            turbineBaselineEngines = b
                    .comment("How many steam engines the 3x3x3 / 9-burner boiler is considered to replace.")
                    .defineInRange("baselineEngines", 9, 1, 64);
            steamEngineReferenceSU = b
                    .comment("SU one Create steam engine provides at the boiler level this build would reach.",
                            "VERIFY against your Create version/config with goggles on a real 9-engine build.")
                    .defineInRange("steamEngineReferenceSU", 16384, 1, Integer.MAX_VALUE);
            turbineMultiplier = b
                    .comment("Steam Redliner capacity = baselineEngines * steamEngineReferenceSU * this.")
                    .defineInRange("multiplier", 4.0, 0.1, 64.0);
            turbineOutputRpm = b
                    .comment("Output speed of a Steam Redliner with a plain (untiered) rotor. Tiered rotors output at the first",
                            "speed step their cog is needed for (512 / 2048 / 8192 by default). Capacity in SU is the same for all.")
                    .defineInRange("outputRpm", 256, 16, 16384);
            turbineWaterPerTick = b
                    .comment("Water (mB) the Steam Redliner consumes from the tank per tick while running.")
                    .defineInRange("waterPerTick", 200, 0, 100000);
            b.pop();
        }

        public long turbineCapacitySU() {
            return (long) (turbineBaselineEngines.get() * (long) steamEngineReferenceSU.get() * turbineMultiplier.get());
        }
    }

    public static final class Client {
        public final ModConfigSpec.IntValue maxVisualRpm;

        Client(ModConfigSpec.Builder b) {
            maxVisualRpm = b
                    .comment("Parts spinning faster than this are drawn at this speed (they glow instead).",
                            "Cosmetic only. 0 disables the clamp.")
                    .defineInRange("maxVisualRpm", 256, 0, 65536);
        }
    }
}
