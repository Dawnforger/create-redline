package com.createredline.registry;

import com.createredline.client.TieredKineticRenderer;
import com.createredline.client.TieredKineticVisual;
import com.createredline.client.TieredPartials;
import com.createredline.content.turbine.SteamRedlinerBlock;
import com.createredline.content.turbine.SteamRedlinerBlockEntity;
import com.createredline.content.turbine.SteamRedlinerRenderer;
import com.simibubi.create.content.kinetics.base.OrientedRotatingVisual;
import com.simibubi.create.content.kinetics.gearbox.GearboxBlockEntity;
import com.simibubi.create.content.kinetics.gearbox.GearboxRenderer;
import com.simibubi.create.content.kinetics.gearbox.GearboxVisual;
import com.simibubi.create.content.kinetics.simpleRelays.BracketedKineticBlockEntity;
import com.tterrag.registrate.util.entry.BlockEntityEntry;
import com.tterrag.registrate.util.nullness.NonNullSupplier;
import dev.engine_room.flywheel.lib.model.Models;
import net.minecraft.core.Direction;
import net.minecraft.world.level.block.Block;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import static com.createredline.CreateRedline.REGISTRATE;

/**
 * We reuse Create's own BE classes, renderers and Flywheel visuals; we only need our own BlockEntityTypes so
 * our blocks are valid for them. Tier logic lives on the Block (ITieredKinetic), not the BE.
 *
 * Shafts/cogs use our own visual + renderer because Create's hard-code AllBlocks.COGWHEEL and its wooden partials.
 */
public final class ModBlockEntities {
    public static BlockEntityEntry<BracketedKineticBlockEntity> TIERED_BRACKETED;
    public static BlockEntityEntry<GearboxBlockEntity> TIERED_GEARBOX;
    public static BlockEntityEntry<SteamRedlinerBlockEntity> STEAM_REDLINER;

    private ModBlockEntities() {}

    // A raw NonNullSupplier[] would make the whole builder chain raw and break the renderer lambdas.
    @SuppressWarnings("unchecked")
    private static NonNullSupplier<? extends Block>[] blocks(Collection<? extends NonNullSupplier<? extends Block>> entries) {
        return entries.toArray(NonNullSupplier[]::new);
    }

    public static void register() {
        List<NonNullSupplier<? extends Block>> bracketed = new ArrayList<>();
        ModBlocks.SHAFTS.values().forEach(bracketed::add);
        ModBlocks.COGWHEELS.values().forEach(bracketed::add);
        ModBlocks.LARGE_COGWHEELS.values().forEach(bracketed::add);

        TIERED_BRACKETED = REGISTRATE.blockEntity("tiered_bracketed_kinetic", BracketedKineticBlockEntity::new)
                .visual(() -> TieredKineticVisual::create, false)
                .validBlocks(blocks(bracketed))
                .renderer(() -> TieredKineticRenderer::new)
                .register();

        TIERED_GEARBOX = REGISTRATE.blockEntity("tiered_gearbox", GearboxBlockEntity::new)
                .visual(() -> GearboxVisual::new, false)
                .validBlocks(blocks(ModBlocks.GEARBOXES.values()))
                .renderer(() -> GearboxRenderer::new)
                .register();

        STEAM_REDLINER = REGISTRATE.blockEntity("steam_redliner", SteamRedlinerBlockEntity::new)
                .visual(() -> (context, be, partialTick) -> new OrientedRotatingVisual<>(context, be, partialTick, Direction.UP,
                        be.getBlockState().getValue(SteamRedlinerBlock.FACING), Models.partial(
                        TieredPartials.turbineRotor(be.getBlockState().getValue(SteamRedlinerBlock.ROTOR)))), false)
                .validBlocks(ModBlocks.STEAM_REDLINER)
                .renderer(() -> SteamRedlinerRenderer::new)
                .register();
    }
}
