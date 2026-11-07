package com.createredline.client.ponder;

import com.createredline.content.turbine.SteamRedlinerBlockEntity;
import com.createredline.tier.GlowLevels;
import com.simibubi.create.AllItems;
import com.simibubi.create.content.processing.burner.BlazeBurnerBlock;
import com.simibubi.create.content.processing.burner.BlazeBurnerBlock.HeatLevel;
import com.simibubi.create.foundation.ponder.CreateSceneBuilder;
import net.createmod.catnip.math.Pointing;
import net.createmod.ponder.api.PonderPalette;
import net.createmod.ponder.api.scene.SceneBuilder;
import net.createmod.ponder.api.scene.SceneBuildingUtil;
import net.createmod.ponder.api.scene.Selection;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.phys.Vec3;

public final class TurbineScenes {
    private TurbineScenes() {}

    public static void turbine(SceneBuilder builder, SceneBuildingUtil util) {
        CreateSceneBuilder scene = new CreateSceneBuilder(builder);
        scene.title("steam_redliner", "Building a Steam Redliner");
        scene.configureBasePlate(0, 0, 7);
        scene.scaleSceneView(.85f);
        scene.showBasePlate();
        scene.idle(10);

        Selection burners = util.select().fromTo(1, 1, 2, 3, 1, 4);
        Selection tank = util.select().fromTo(1, 2, 2, 3, 4, 4);
        Selection turbine = util.select().fromTo(4, 2, 2, 4, 4, 4);
        Selection shafts = util.select().fromTo(5, 3, 3, 6, 3, 3);
        BlockPos core = util.grid().at(4, 3, 3);
        Vec3 tankFace = util.vector().blockSurface(util.grid().at(3, 3, 3), Direction.EAST);

        scene.world().showSection(burners, Direction.DOWN);
        scene.idle(10);
        scene.world().showSection(tank, Direction.DOWN);
        scene.idle(15);

        scene.overlay().showText(70)
                .attachKeyFrame()
                .text("The Steam Redliner runs on a 3x3x3 Fluid Tank over 9 Blaze Burners")
                .pointAt(util.vector().blockSurface(util.grid().at(2, 4, 2), Direction.NORTH))
                .placeNearTarget();
        scene.idle(80);

        scene.overlay().showControls(util.vector().blockSurface(util.grid().at(2, 1, 2), Direction.NORTH), Pointing.RIGHT, 50)
                .withItem(AllItems.BLAZE_CAKE.asStack())
                .rightClick();
        scene.idle(10);
        for (int x = 1; x <= 3; x++)
            for (int z = 2; z <= 4; z++)
                scene.world().modifyBlock(util.grid().at(x, 1, z),
                        s -> s.setValue(BlazeBurnerBlock.HEAT_LEVEL, HeatLevel.SEETHING), false);
        scene.overlay().showText(70)
                .attachKeyFrame()
                .colored(PonderPalette.RED)
                .text("Every burner must be superheated with Blaze Cake, and the tank kept supplied with water")
                .pointAt(util.vector().blockSurface(util.grid().at(2, 1, 2), Direction.NORTH))
                .placeNearTarget();
        scene.idle(80);

        scene.overlay().showControls(tankFace, Pointing.DOWN, 40)
                .withItem(com.createredline.registry.ModBlocks.STEAM_REDLINER.asStack())
                .rightClick();
        scene.idle(10);
        scene.world().showSection(turbine, Direction.WEST);
        scene.idle(15);
        scene.overlay().showOutline(PonderPalette.GREEN, "face", turbine, 70);
        scene.overlay().showText(70)
                .attachKeyFrame()
                .text("Place the Steam Redliner on the centre of a side or the top. It covers the whole 3x3 face")
                .pointAt(util.vector().blockSurface(core, Direction.EAST))
                .placeNearTarget();
        scene.idle(80);

        scene.world().showSection(shafts, Direction.WEST);
        scene.idle(5);
        scene.world().modifyBlockEntityNBT(util.select().position(core), SteamRedlinerBlockEntity.class,
                nbt -> nbt.putString("Status", SteamRedlinerBlockEntity.Status.RUNNING.name()));
        // Vibranium rotor -> 2048 RPM, so the output shafts are Vibranium and glow at that speed.
        scene.world().setKineticSpeed(turbine.add(shafts), 2048);
        for (int x = 5; x <= 6; x++)
            scene.world().modifyBlock(util.grid().at(x, 3, 3), s -> s.setValue(GlowLevels.GLOW, GlowLevels.forSpeed(2048)), false);
        scene.effects().indicateSuccess(core);
        scene.overlay().showText(80)
                .attachKeyFrame()
                .text("It produces 4x the stress capacity of the 9 Steam Engines this boiler could run")
                .pointAt(util.vector().blockSurface(util.grid().at(6, 3, 3), Direction.UP))
                .placeNearTarget();
        scene.idle(90);

        scene.overlay().showText(90)
                .attachKeyFrame()
                .colored(PonderPalette.BLUE)
                .text("The Large Cogwheel it was crafted with sets the rotor: Allthemodium 512 RPM, Vibranium 2048 RPM, Unobtainium 8192 RPM")
                .pointAt(util.vector().blockSurface(core, Direction.EAST))
                .placeNearTarget();
        scene.idle(100);
        scene.overlay().showText(80)
                .text("Stress capacity stays the same, but the output shaft must match the rotor's tier")
                .pointAt(util.vector().blockSurface(util.grid().at(5, 3, 3), Direction.UP))
                .placeNearTarget();
        scene.idle(90);

        scene.overlay().showText(80)
                .attachKeyFrame()
                .colored(PonderPalette.RED)
                .text("Only one Redliner fits on each boiler, and no Steam Engines can share its tank")
                .pointAt(util.vector().blockSurface(util.grid().at(2, 4, 3), Direction.UP))
                .placeNearTarget();
        scene.idle(90);
        scene.markAsFinished();
    }
}
