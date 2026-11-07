package com.createredline.tier;

import com.createredline.config.RedlineConfig;
import com.simibubi.create.infrastructure.config.AllConfigs;
import net.minecraft.util.StringRepresentable;

/**
 * Speed tiers. BASE is every vanilla Create component; it defers to Create's own maxRotationSpeed config
 * so we never change behaviour for blocks we don't own. Each tier above multiplies the previous cap.
 */
public enum KineticTier implements StringRepresentable {
    BASE("base", null),
    ALLTHEMODIUM("allthemodium", "c:ingots/allthemodium"),
    VIBRANIUM("vibranium", "c:ingots/vibranium"),
    UNOBTAINIUM("unobtainium", "c:ingots/unobtainium");

    private final String name;
    private final String ingotTag;

    KineticTier(String name, String ingotTag) {
        this.name = name;
        this.ingotTag = ingotTag;
    }

    /** 256 -> 1024 -> 4096 -> 16384 with default configs. */
    public int maxRpm() {
        int base = AllConfigs.server().kinetics.maxRotationSpeed.get();
        int mult = RedlineConfig.SERVER.tierMultiplier.get();
        return base * (int) Math.pow(mult, ordinal());
    }

    /**
     * The Steam Redliner output speed for a rotor of this tier: the first speed step at which this tier's cog
     * becomes mandatory, i.e. one doubling past the previous tier's cap (512 / 2048 / 8192 by default).
     * An untiered (BASE) rotor uses the configured base output instead.
     */
    public int turbineRpm() {
        if (this == BASE)
            return RedlineConfig.SERVER.turbineOutputRpm.get();
        return values()[ordinal() - 1].maxRpm() * 2;
    }

    public boolean atLeast(KineticTier other) {
        return ordinal() >= other.ordinal();
    }

    public String ingotTag() {
        return ingotTag;
    }

    @Override
    public String getSerializedName() {
        return name;
    }
}
