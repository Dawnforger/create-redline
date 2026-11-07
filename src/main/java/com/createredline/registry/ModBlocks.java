package com.createredline.registry;

import com.createredline.content.cog.TieredCogWheelBlock;
import com.createredline.content.gearbox.TieredGearboxBlock;
import com.createredline.content.shaft.TieredShaftBlock;
import com.createredline.content.turbine.SteamRedlinerBlock;
import com.createredline.content.turbine.SteamRedlinerStructuralBlock;
import com.createredline.tier.GlowLevels;
import com.createredline.tier.KineticTier;
import com.simibubi.create.content.kinetics.simpleRelays.BracketedKineticBlockModel;
import com.simibubi.create.content.kinetics.simpleRelays.CogwheelBlockItem;
import com.simibubi.create.foundation.data.CreateRegistrate;
import com.simibubi.create.foundation.data.SharedProperties;
import com.tterrag.registrate.util.entry.BlockEntry;
import com.tterrag.registrate.providers.loot.RegistrateBlockLootTables;
import net.minecraft.advancements.critereon.StatePropertiesPredicate;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.storage.loot.LootPool;
import net.minecraft.world.level.storage.loot.LootTable;
import net.minecraft.world.level.storage.loot.entries.LootItem;
import net.minecraft.world.level.storage.loot.functions.SetComponentsFunction;
import net.minecraft.world.level.storage.loot.predicates.ExplosionCondition;
import net.minecraft.world.level.storage.loot.predicates.LootItemBlockStatePropertyCondition;
import net.minecraft.world.level.storage.loot.providers.number.ConstantValue;
import net.minecraft.world.level.material.MapColor;

import java.util.EnumMap;
import java.util.Map;

import static com.createredline.CreateRedline.REGISTRATE;
import static com.simibubi.create.foundation.data.TagGen.pickaxeOnly;

/**
 * Block/item models and blockstates are hand-written under src/main/resources (they currently parent Create's
 * models as placeholders), so every entry uses a no-op datagen provider.
 */
public final class ModBlocks {
    public static final Map<KineticTier, BlockEntry<TieredShaftBlock>> SHAFTS = new EnumMap<>(KineticTier.class);
    public static final Map<KineticTier, BlockEntry<TieredCogWheelBlock>> COGWHEELS = new EnumMap<>(KineticTier.class);
    public static final Map<KineticTier, BlockEntry<TieredCogWheelBlock>> LARGE_COGWHEELS = new EnumMap<>(KineticTier.class);
    public static final Map<KineticTier, BlockEntry<TieredGearboxBlock>> GEARBOXES = new EnumMap<>(KineticTier.class);

    public static BlockEntry<SteamRedlinerBlock> STEAM_REDLINER;
    public static BlockEntry<SteamRedlinerStructuralBlock> STEAM_REDLINER_STRUCTURE;

    private ModBlocks() {}

    /** Drops one turbine carrying the rotor tier back as an item component. */
    private static void turbineLoot(RegistrateBlockLootTables lt, SteamRedlinerBlock block) {
        var item = LootItem.lootTableItem(block);
        for (KineticTier tier : KineticTier.values()) {
            if (tier == KineticTier.BASE)
                continue;
            item.apply(SetComponentsFunction.setComponent(ModComponents.ROTOR_TIER.get(), tier)
                    .when(LootItemBlockStatePropertyCondition.hasBlockStateProperties(block)
                            .setProperties(StatePropertiesPredicate.Builder.properties()
                                    .hasProperty(SteamRedlinerBlock.ROTOR, tier))));
        }
        lt.add(block, LootTable.lootTable().withPool(LootPool.lootPool()
                .setRolls(ConstantValue.exactly(1))
                .add(item)
                .when(ExplosionCondition.survivesExplosion())));
    }

    public static void register() {
        REGISTRATE.setCreativeTab(ModCreativeTab.MAIN);

        for (KineticTier tier : KineticTier.values()) {
            if (tier == KineticTier.BASE)
                continue;
            String t = tier.getSerializedName();
            MapColor color = switch (tier) {
                case ALLTHEMODIUM -> MapColor.COLOR_YELLOW;
                case VIBRANIUM -> MapColor.COLOR_GREEN;
                default -> MapColor.COLOR_PURPLE;
            };

            SHAFTS.put(tier, REGISTRATE.block(t + "_shaft", p -> new TieredShaftBlock(p, tier))
                    .initialProperties(SharedProperties::stone)
                    .transform(pickaxeOnly())
                    .properties(p -> p.mapColor(color).sound(SoundType.NETHERITE_BLOCK).forceSolidOff().lightLevel(GlowLevels::lightOf))
                    .blockstate((c, p) -> {})
                    .onRegister(CreateRegistrate.blockModel(() -> BracketedKineticBlockModel::new))
                    .item().model((c, p) -> {}).build()
                    .register());

            COGWHEELS.put(tier, REGISTRATE.block(t + "_cogwheel", p -> TieredCogWheelBlock.small(p, tier))
                    .initialProperties(SharedProperties::stone)
                    .transform(pickaxeOnly())
                    .properties(p -> p.mapColor(color).sound(SoundType.NETHERITE_BLOCK).lightLevel(GlowLevels::lightOf))
                    .blockstate((c, p) -> {})
                    .onRegister(CreateRegistrate.blockModel(() -> BracketedKineticBlockModel::new))
                    .item(CogwheelBlockItem::new).model((c, p) -> {}).build()
                    .register());

            LARGE_COGWHEELS.put(tier, REGISTRATE.block(t + "_large_cogwheel", p -> TieredCogWheelBlock.large(p, tier))
                    .initialProperties(SharedProperties::stone)
                    .transform(pickaxeOnly())
                    .properties(p -> p.mapColor(color).sound(SoundType.NETHERITE_BLOCK).lightLevel(GlowLevels::lightOf))
                    .blockstate((c, p) -> {})
                    .onRegister(CreateRegistrate.blockModel(() -> BracketedKineticBlockModel::new))
                    .item(CogwheelBlockItem::new).model((c, p) -> {}).build()
                    .register());

            GEARBOXES.put(tier, REGISTRATE.block(t + "_gearbox", p -> new TieredGearboxBlock(p, tier))
                    .initialProperties(SharedProperties::stone)
                    .transform(pickaxeOnly())
                    .properties(p -> p.mapColor(color).sound(SoundType.NETHERITE_BLOCK).noOcclusion().lightLevel(GlowLevels::lightOf))
                    .blockstate((c, p) -> {})
                    .item().model((c, p) -> {}).build()
                    .register());
        }

        STEAM_REDLINER = REGISTRATE.block("steam_redliner", SteamRedlinerBlock::new)
                .initialProperties(SharedProperties::copperMetal)
                .loot(ModBlocks::turbineLoot)
                .transform(pickaxeOnly())
                .properties(p -> p.mapColor(MapColor.COLOR_ORANGE).sound(SoundType.NETHERITE_BLOCK).noOcclusion())
                .blockstate((c, p) -> {})
                .item().model((c, p) -> {}).build()
                .register();

        STEAM_REDLINER_STRUCTURE = REGISTRATE.block("steam_redliner_structure", SteamRedlinerStructuralBlock::new)
                .initialProperties(SharedProperties::copperMetal)
                .transform(pickaxeOnly())
                .properties(p -> p.mapColor(MapColor.COLOR_ORANGE).sound(SoundType.NETHERITE_BLOCK).noOcclusion())
                .clientExtension(() -> () -> new SteamRedlinerStructuralBlock.RenderProperties())
                .blockstate((c, p) -> {})
                .lang("Steam Redliner")
                .register();
    }
}
