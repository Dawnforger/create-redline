package com.createredline.gametest;

import com.createredline.CreateRedline;
import com.createredline.content.turbine.SteamRedlinerBlock;
import com.createredline.content.turbine.SteamRedlinerBlockEntity;
import com.simibubi.create.api.connectivity.ConnectivityHandler;
import com.simibubi.create.content.fluids.tank.FluidTankBlockEntity;
import com.simibubi.create.content.processing.burner.BlazeBurnerBlock;
import com.simibubi.create.content.processing.burner.BlazeBurnerBlockEntity;
import com.simibubi.create.content.kinetics.belt.BeltBlockEntity;
import com.simibubi.create.content.kinetics.belt.item.BeltConnectorItem;
import net.minecraft.core.Vec3i;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.NbtIo;
import net.minecraft.world.level.levelgen.structure.templatesystem.StructureTemplate;
import net.minecraft.world.level.material.Fluids;
import net.neoforged.neoforge.fluids.FluidStack;
import net.neoforged.neoforge.fluids.capability.IFluidHandler;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import com.createredline.registry.ModBlocks;
import com.createredline.tier.GlowLevels;
import com.createredline.tier.ITieredBelt;
import com.createredline.tier.KineticTier;
import com.createredline.tier.TierRules;
import com.simibubi.create.AllBlocks;
import com.simibubi.create.AllItems;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.GameType;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.Vec3;
import com.simibubi.create.content.kinetics.base.HorizontalKineticBlock;
import com.simibubi.create.content.kinetics.base.KineticBlockEntity;
import com.simibubi.create.content.kinetics.motor.CreativeMotorBlock;
import com.simibubi.create.content.kinetics.motor.CreativeMotorBlockEntity;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.gametest.framework.GameTest;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.RotatedPillarBlock;
import net.neoforged.neoforge.gametest.GameTestHolder;
import net.neoforged.neoforge.gametest.PrefixGameTestTemplate;

/**
 * Run with ./gradlew runGameTestServer. Each test builds a real gear train: a creative motor at 256 RPM feeds an
 * entry shaft, then each stage (large cog meshing a small cog, small cog axially driving the next large cog)
 * doubles the speed. Small cog of stage i spins at 256 * 2^(i+1).
 */
@GameTestHolder(CreateRedline.MOD_ID)
@PrefixGameTestTemplate(false)
public class RedlineGameTests {

    private static BlockPos largeCog(int stage) {
        return new BlockPos(2 + stage, 1 + stage, 1 + stage);
    }

    private static BlockPos smallCog(int stage) {
        return new BlockPos(2 + stage, 2 + stage, 2 + stage);
    }

    private static void axisX(GameTestHelper helper, BlockPos pos, Block block) {
        helper.setBlock(pos, block.defaultBlockState().setValue(RotatedPillarBlock.AXIS, Direction.Axis.X));
    }

    private static void buildTrain(GameTestHelper helper, Block entryShaft, Block large, Block small, int stages) {
        BlockPos motorPos = new BlockPos(0, 1, 1);
        helper.setBlock(motorPos, AllBlocks.CREATIVE_MOTOR.getDefaultState().setValue(CreativeMotorBlock.FACING, Direction.EAST));
        axisX(helper, new BlockPos(1, 1, 1), entryShaft);
        for (int i = 0; i < stages; i++) {
            axisX(helper, largeCog(i), large);
            axisX(helper, smallCog(i), small);
        }
        ((CreativeMotorBlockEntity) helper.getBlockEntity(motorPos)).generatedSpeed.setValue(256);
    }

    private static void buildTrain(GameTestHelper helper, KineticTier tier, int stages) {
        buildTrain(helper, ModBlocks.SHAFTS.get(tier).get(), ModBlocks.LARGE_COGWHEELS.get(tier).get(),
                ModBlocks.COGWHEELS.get(tier).get(), stages);
    }

    private static float speedAt(GameTestHelper helper, BlockPos pos) {
        return Math.abs(((KineticBlockEntity) helper.getBlockEntity(pos)).getSpeed());
    }

    @GameTest(template = "empty")
    public static void vanillaSmallCogBreaksAt512(GameTestHelper helper) {
        buildTrain(helper, AllBlocks.SHAFT.get(), AllBlocks.LARGE_COGWHEEL.get(), AllBlocks.COGWHEEL.get(), 1);
        helper.succeedWhen(() -> {
            helper.assertBlockPresent(Blocks.AIR, smallCog(0));
            helper.assertBlockPresent(AllBlocks.LARGE_COGWHEEL.get(), largeCog(0));
        });
    }

    @GameTest(template = "empty")
    public static void allthemodiumHoldsAndGlowsAt1024(GameTestHelper helper) {
        buildTrain(helper, KineticTier.ALLTHEMODIUM, 2);
        helper.succeedWhen(() -> {
            helper.assertTrue(speedAt(helper, smallCog(1)) == 1024, "expected 1024 rpm at stage 2");
            helper.assertBlockProperty(largeCog(0), GlowLevels.GLOW, 0);
            helper.assertBlockProperty(smallCog(0), GlowLevels.GLOW, 3);
            helper.assertBlockProperty(smallCog(1), GlowLevels.GLOW, 5);
        });
    }

    @GameTest(template = "empty")
    public static void allthemodiumBreaksAt2048(GameTestHelper helper) {
        buildTrain(helper, KineticTier.ALLTHEMODIUM, 3);
        helper.succeedWhen(() -> {
            helper.assertBlockPresent(Blocks.AIR, smallCog(2));
            helper.assertBlockPresent(ModBlocks.COGWHEELS.get(KineticTier.ALLTHEMODIUM).get(), smallCog(1));
        });
    }

    @GameTest(template = "empty")
    public static void unobtainiumFullGlowAt16384(GameTestHelper helper) {
        buildTrain(helper, KineticTier.UNOBTAINIUM, 6);
        helper.succeedWhen(() -> {
            helper.assertTrue(speedAt(helper, smallCog(5)) == 16384, "expected 16384 rpm at stage 6");
            helper.assertBlockProperty(smallCog(5), GlowLevels.GLOW, 15);
            helper.assertTrue(helper.getLevel().getLightEmission(helper.absolutePos(smallCog(5))) == 15,
                    "expected light level 15");
        });
    }

    @GameTest(template = "empty")
    public static void vanillaShaftCannotDriveTieredCog(GameTestHelper helper) {
        Block atmLarge = ModBlocks.LARGE_COGWHEELS.get(KineticTier.ALLTHEMODIUM).get();
        buildTrain(helper, AllBlocks.SHAFT.get(), atmLarge, ModBlocks.COGWHEELS.get(KineticTier.ALLTHEMODIUM).get(), 1);
        helper.runAfterDelay(20, () -> {
            helper.assertTrue(speedAt(helper, new BlockPos(1, 1, 1)) == 256, "vanilla shaft should spin");
            helper.assertTrue(speedAt(helper, largeCog(0)) == 0, "ATM cog must not link to a vanilla shaft");
            helper.succeed();
        });
    }

    @GameTest(template = "empty")
    public static void vanillaMachineRunsAtHighSpeed(GameTestHelper helper) {
        // 1024 rpm out of an Allthemodium train, through an ATM shaft, into a plain Create press.
        buildTrain(helper, KineticTier.ALLTHEMODIUM, 2);
        BlockPos shaft = smallCog(1).east(), press = shaft.east();
        axisX(helper, shaft, ModBlocks.SHAFTS.get(KineticTier.ALLTHEMODIUM).get());
        helper.setBlock(press, AllBlocks.MECHANICAL_PRESS.getDefaultState()
                .setValue(HorizontalKineticBlock.HORIZONTAL_FACING, Direction.EAST)); // press shaft runs along its facing
        helper.runAfterDelay(20, () -> {
            helper.assertBlockPresent(AllBlocks.MECHANICAL_PRESS.get(), press);
            helper.assertTrue(speedAt(helper, press) == 1024, "press should run at 1024 rpm, got " + speedAt(helper, press));
            helper.succeed();
        });
    }

    @GameTest(template = "empty")
    public static void shaftUpgradeRecipesLoad(GameTestHelper helper) {
        for (String t : new String[] {"allthemodium", "vibranium", "unobtainium"})
            helper.assertTrue(helper.getLevel().getRecipeManager().byKey(CreateRedline.id("deploying/" + t + "_shaft")).isPresent(),
                    "missing deploying recipe for " + t + " shaft");
        helper.succeed();
    }

    /** Belt along Z between two X-axis shafts at x, y, z0..z1 (relative), built through Create's own connector code. */
    private static void buildBelt(GameTestHelper helper, Block shaft, int x, int y, int z0, int z1) {
        axisX(helper, new BlockPos(x, y, z0), shaft);
        axisX(helper, new BlockPos(x, y, z1), shaft);
        BeltConnectorItem.createBelts(helper.getLevel(), helper.absolutePos(new BlockPos(x, y, z0)),
                helper.absolutePos(new BlockPos(x, y, z1)));
    }

    @GameTest(template = "empty")
    public static void tieredBeltCarriesTierSpeed(GameTestHelper helper) {
        // 1024 rpm Allthemodium train; its last small cog drives the belt's start pulley axially.
        buildTrain(helper, KineticTier.ALLTHEMODIUM, 2);
        BlockPos start = smallCog(1).east();
        buildBelt(helper, ModBlocks.SHAFTS.get(KineticTier.ALLTHEMODIUM).get(), start.getX(), start.getY(), start.getZ(), start.getZ() + 3);
        BlockPos middle = start.south(2);
        helper.runAfterDelay(20, () -> {
            helper.assertBlockPresent(AllBlocks.BELT.get(), middle);
            var belt = (BeltBlockEntity) helper.getBlockEntity(middle);
            helper.assertTrue(TierRules.tierOf(belt) == KineticTier.ALLTHEMODIUM, "belt should be Allthemodium tier");
            helper.assertTrue(Math.abs(belt.getSpeed()) == 1024, "belt should carry 1024 rpm, got " + belt.getSpeed());
            helper.assertTrue(Math.abs(belt.getBeltMovementSpeed()) <= 256 / 480f + 1e-6, "item speed must stay capped");
            helper.succeed();
        });
    }

    @GameTest(template = "empty", timeoutTicks = 200)
    public static void tieredBeltOnRunningNetwork(GameTestHelper helper) {
        // Belt built onto shafts that are already spinning at 8192 rpm (the in-game failure case).
        buildTrain(helper, KineticTier.UNOBTAINIUM, 5);
        BlockPos start = smallCog(4).east();
        Block shaft = ModBlocks.SHAFTS.get(KineticTier.UNOBTAINIUM).get();
        axisX(helper, start, shaft);
        axisX(helper, start.south(3), shaft);
        helper.runAfterDelay(20, () -> {
            helper.assertTrue(speedAt(helper, start) == 8192, "start shaft should already spin at 8192, got " + speedAt(helper, start));
            BeltConnectorItem.createBelts(helper.getLevel(), helper.absolutePos(start), helper.absolutePos(start.south(3)));
            helper.runAfterDelay(20, () -> {
                for (int i = 0; i <= 3; i++)
                    helper.assertBlockPresent(AllBlocks.BELT.get(), start.south(i));
                helper.assertTrue(speedAt(helper, start.south(3)) == 8192, "belt end should spin at 8192, got " + speedAt(helper, start.south(3)));
                for (int i : new int[] {0, 3})
                    helper.assertTrue(((ITieredBelt) helper.getBlockEntity(start.south(i))).redline$getPulleyTier() == KineticTier.UNOBTAINIUM,
                            "pulley " + i + " should remember its Unobtainium shaft");
                helper.succeed();
            });
        });
    }

    /**
     * A running Steam Redliner with the given rotor: 9 creative (non-decaying) superheated burners, a formed
     * 3x3x3 tank full of water, core at (4,3,3) facing EAST. Returns the core position.
     */
    private static BlockPos buildRunningTurbine(GameTestHelper helper, KineticTier rotor) {
        for (int x = 1; x <= 3; x++)
            for (int z = 2; z <= 4; z++) {
                BlockPos pos = new BlockPos(x, 1, z);
                helper.setBlock(pos, AllBlocks.BLAZE_BURNER.getDefaultState()
                        .setValue(BlazeBurnerBlock.HEAT_LEVEL, BlazeBurnerBlock.HeatLevel.SEETHING));
                ((BlazeBurnerBlockEntity) helper.getBlockEntity(pos)).isCreative = true;
            }
        FluidTankBlockEntity tank = buildTank(helper, new BlockPos(1, 2, 2));
        tank.getTankInventory().fill(new FluidStack(Fluids.WATER, 200_000), IFluidHandler.FluidAction.EXECUTE);
        BlockPos core = new BlockPos(4, 3, 3);
        helper.setBlock(core, ModBlocks.STEAM_REDLINER.getDefaultState()
                .setValue(SteamRedlinerBlock.FACING, Direction.EAST).setValue(SteamRedlinerBlock.ROTOR, rotor));
        return core;
    }

    @GameTest(template = "empty", timeoutTicks = 200)
    public static void redlinerGivesFourTimesSteamEngines(GameTestHelper helper) {
        // 9 steam engines on a 3x3x3 boiler: level 6 (size-capped) * 16 rpm * 1024 = 98,304 SU; Redliner = 4x.
        helper.assertTrue(SteamRedlinerBlockEntity.steamEngineBaselineSU() == 98_304,
                "steam engine baseline should be 98,304 SU, got " + SteamRedlinerBlockEntity.steamEngineBaselineSU());
        BlockPos core = buildRunningTurbine(helper, KineticTier.VIBRANIUM);
        helper.succeedWhen(() -> {
            var be = (SteamRedlinerBlockEntity) helper.getBlockEntity(core);
            helper.assertTrue(be.getStatus() == SteamRedlinerBlockEntity.Status.RUNNING, "Redliner should run");
            double su = be.calculateAddedStressCapacity() * Math.abs(be.getGeneratedSpeed());
            helper.assertTrue(Math.abs(su - 393_216) < 1, "Redliner should provide 393,216 SU, got " + su);
        });
    }

    @GameTest(template = "empty", timeoutTicks = 300)
    public static void beltOnTurbineFedShaft(GameTestHelper helper) {
        // The in-game report: belt from a turbine-driven Unobtainium shaft to a loose, stationary one.
        BlockPos core = buildRunningTurbine(helper, KineticTier.UNOBTAINIUM);
        Block shaft = ModBlocks.SHAFTS.get(KineticTier.UNOBTAINIUM).get();
        BlockPos start = core.east(), end = start.south(3);
        axisX(helper, start, shaft);
        axisX(helper, end, shaft);
        helper.runAfterDelay(60, () -> {
            var status = ((SteamRedlinerBlockEntity) helper.getBlockEntity(core)).getStatus();
            helper.assertTrue(status == SteamRedlinerBlockEntity.Status.RUNNING, "turbine should run, status " + status);
            helper.assertTrue(speedAt(helper, start) == 8192, "fed shaft should spin at 8192, got " + speedAt(helper, start));
            // Go through the real item: two right-clicks with a belt connector, as a player would.
            Player player = helper.makeMockPlayer(GameType.SURVIVAL);
            ItemStack belt = AllItems.BELT_CONNECTOR.asStack(4);
            player.setItemInHand(InteractionHand.MAIN_HAND, belt);
            for (BlockPos click : new BlockPos[] {start, end}) {
                BlockPos abs = helper.absolutePos(click);
                var hit = new BlockHitResult(Vec3.atCenterOf(abs), Direction.UP, abs, false);
                var result = belt.useOn(new UseOnContext(player, InteractionHand.MAIN_HAND, hit));
                player.getCooldowns().removeCooldown(belt.getItem());
                helper.assertTrue(result.consumesAction(), "click on " + click + " was rejected: " + result);
            }
            helper.runAfterDelay(40, () -> {
                for (int i = 0; i <= 3; i++)
                    helper.assertBlockPresent(AllBlocks.BELT.get(), start.south(i));
                helper.assertTrue(speedAt(helper, end) == 8192, "belt end should spin at 8192, got " + speedAt(helper, end));
                helper.succeed();
            });
        });
    }

    @GameTest(template = "empty")
    public static void vanillaBeltBreaksPastBaseCap(GameTestHelper helper) {
        // An Allthemodium shaft may feed a weaker belt; the belt then breaks at 1024 rpm.
        buildTrain(helper, KineticTier.ALLTHEMODIUM, 2);
        BlockPos feed = smallCog(1).east(), start = feed.east();
        axisX(helper, feed, ModBlocks.SHAFTS.get(KineticTier.ALLTHEMODIUM).get());
        buildBelt(helper, AllBlocks.SHAFT.get(), start.getX(), start.getY(), start.getZ(), start.getZ() + 3);
        helper.succeedWhen(() -> helper.assertBlockNotPresent(AllBlocks.BELT.get(), start.south(2)));
    }

    @GameTest(template = "empty")
    public static void tieredBeltReturnsItsShafts(GameTestHelper helper) {
        Block vib = ModBlocks.SHAFTS.get(KineticTier.VIBRANIUM).get();
        buildBelt(helper, vib, 4, 2, 2, 6);
        helper.runAfterDelay(2, () -> {
            helper.destroyBlock(new BlockPos(4, 2, 4));
            helper.succeedWhen(() -> {
                helper.assertBlockPresent(vib, new BlockPos(4, 2, 2));
                helper.assertBlockPresent(vib, new BlockPos(4, 2, 6));
            });
        });
    }

    @GameTest(template = "empty")
    public static void glowStepTable(GameTestHelper helper) {
        int[][] expected = {{256, 0}, {512, 3}, {1024, 5}, {2048, 8}, {4096, 10}, {8192, 13}, {16384, 15}};
        for (int[] e : expected)
            helper.assertTrue(GlowLevels.forSpeed(e[0]) == e[1], e[0] + " rpm -> expected glow " + e[1]);
        helper.succeed();
    }

    @GameTest(template = "empty")
    public static void attachmentRule(GameTestHelper helper) {
        BlockPos a = new BlockPos(1, 1, 2), b = new BlockPos(2, 1, 2), c = new BlockPos(3, 1, 2);
        axisX(helper, a, AllBlocks.SHAFT.get());
        axisX(helper, b, ModBlocks.COGWHEELS.get(KineticTier.ALLTHEMODIUM).get());
        axisX(helper, c, ModBlocks.SHAFTS.get(KineticTier.VIBRANIUM).get());
        var vanillaShaft = (KineticBlockEntity) helper.getBlockEntity(a);
        var atmCog = (KineticBlockEntity) helper.getBlockEntity(b);
        var vibShaft = (KineticBlockEntity) helper.getBlockEntity(c);
        helper.assertFalse(TierRules.canLink(vanillaShaft, atmCog), "vanilla shaft must not drive ATM cog");
        helper.assertTrue(TierRules.canLink(vibShaft, atmCog), "vibranium shaft may drive ATM cog");
        helper.succeed();
    }

    // --- Steam Redliner multiblock ---

    private static final BlockPos CORE = new BlockPos(4, 2, 4);

    private static java.util.List<BlockPos> housing() {
        java.util.List<BlockPos> out = new java.util.ArrayList<>();
        for (int dx = -1; dx <= 1; dx++)
            for (int dz = -1; dz <= 1; dz++)
                if (dx != 0 || dz != 0)
                    out.add(CORE.offset(dx, 0, dz));
        return out;
    }

    private static void placeTurbine(GameTestHelper helper) {
        helper.setBlock(CORE, ModBlocks.STEAM_REDLINER.getDefaultState().setValue(SteamRedlinerBlock.FACING, Direction.UP));
    }

    @GameTest(template = "empty")
    public static void turbineFormsFullHousing(GameTestHelper helper) {
        placeTurbine(helper);
        helper.succeedWhen(() -> {
            for (BlockPos pos : housing())
                helper.assertBlockPresent(ModBlocks.STEAM_REDLINER_STRUCTURE.get(), pos);
        });
    }

    @GameTest(template = "empty")
    public static void breakingHousingRemovesWholeTurbine(GameTestHelper helper) {
        placeTurbine(helper);
        helper.runAfterDelay(3, () -> {
            helper.destroyBlock(CORE.offset(1, 0, 1)); // a corner, two links from the core
            helper.succeedWhen(() -> {
                helper.assertBlockPresent(Blocks.AIR, CORE);
                for (BlockPos pos : housing())
                    helper.assertBlockPresent(Blocks.AIR, pos);
            });
        });
    }

    @GameTest(template = "empty")
    public static void blockedHousingCancelsTurbine(GameTestHelper helper) {
        helper.setBlock(CORE.offset(-1, 0, 1), Blocks.STONE);
        placeTurbine(helper);
        helper.succeedWhen(() -> helper.assertBlockPresent(Blocks.AIR, CORE));
    }

    @GameTest(template = "empty")
    public static void turbineRotorSpeeds(GameTestHelper helper) {
        int[] expected = {256, 512, 2048, 8192};
        for (KineticTier tier : KineticTier.values())
            helper.assertTrue(tier.turbineRpm() == expected[tier.ordinal()],
                    tier + " rotor should output " + expected[tier.ordinal()] + " rpm, got " + tier.turbineRpm());
        // Output must never exceed the rotor's own cap, or the core would break itself.
        for (KineticTier tier : KineticTier.values())
            helper.assertTrue(tier.turbineRpm() <= tier.maxRpm(), tier + " rotor output exceeds its cap");
        helper.succeed();
    }

    @GameTest(template = "empty")
    public static void turbineOutputShaftMustMatchRotor(GameTestHelper helper) {
        BlockPos core = new BlockPos(2, 2, 2), out = new BlockPos(3, 2, 2);
        helper.setBlock(core, ModBlocks.STEAM_REDLINER.getDefaultState()
                .setValue(SteamRedlinerBlock.FACING, Direction.EAST).setValue(SteamRedlinerBlock.ROTOR, KineticTier.VIBRANIUM));
        var turbine = (KineticBlockEntity) helper.getBlockEntity(core);
        Block[][] cases = {
                {AllBlocks.SHAFT.get(), null},
                {ModBlocks.SHAFTS.get(KineticTier.ALLTHEMODIUM).get(), null},
                {ModBlocks.SHAFTS.get(KineticTier.VIBRANIUM).get(), Blocks.STONE},
                {ModBlocks.SHAFTS.get(KineticTier.UNOBTAINIUM).get(), Blocks.STONE},
        };
        for (Block[] c : cases) {
            axisX(helper, out, c[0]);
            boolean links = TierRules.canLink(turbine, (KineticBlockEntity) helper.getBlockEntity(out));
            helper.assertTrue(links == (c[1] != null), c[0].getName().getString() + (links ? " should not" : " should") + " link to a Vibranium turbine");
        }
        helper.succeed();
    }

    /** Builds a merged 3x3x3 tank with its min corner at {@code origin} and returns the controller. */
    private static FluidTankBlockEntity buildTank(GameTestHelper helper, BlockPos origin) {
        for (int x = 0; x < 3; x++)
            for (int y = 0; y < 3; y++)
                for (int z = 0; z < 3; z++)
                    helper.setBlock(origin.offset(x, y, z), AllBlocks.FLUID_TANK.getDefaultState());
        FluidTankBlockEntity controller = (FluidTankBlockEntity) helper.getBlockEntity(origin);
        ConnectivityHandler.formMulti(controller);
        return controller;
    }

    @GameTest(template = "empty")
    public static void onlyOneTurbinePerBoiler(GameTestHelper helper) {
        buildTank(helper, new BlockPos(1, 1, 1));
        BlockPos side = new BlockPos(4, 2, 2), top = new BlockPos(2, 4, 2);
        helper.setBlock(side, ModBlocks.STEAM_REDLINER.getDefaultState().setValue(SteamRedlinerBlock.FACING, Direction.EAST));
        helper.setBlock(top, ModBlocks.STEAM_REDLINER.getDefaultState().setValue(SteamRedlinerBlock.FACING, Direction.UP));
        helper.succeedWhen(() -> {
            var a = ((SteamRedlinerBlockEntity) helper.getBlockEntity(side)).getStatus();
            var b = ((SteamRedlinerBlockEntity) helper.getBlockEntity(top)).getStatus();
            boolean oneShared = (a == SteamRedlinerBlockEntity.Status.SHARED_BOILER) != (b == SteamRedlinerBlockEntity.Status.SHARED_BOILER);
            // The owner gets as far as the burner check (there are no burners here).
            boolean ownerValidated = a == SteamRedlinerBlockEntity.Status.NEEDS_SUPERHEAT || b == SteamRedlinerBlockEntity.Status.NEEDS_SUPERHEAT;
            helper.assertTrue(oneShared && ownerValidated, "expected exactly one turbine to own the boiler, got " + a + "/" + b);
        });
    }

    /**
     * Not a real test: with -Dredline.exportPonder=true it builds the Ponder scene and saves it to
     * run-gametest/ponder_export/steam_redliner.nbt (copy into assets/create_redline/ponder/). Layout in TurbineScenes.
     */
    @GameTest(template = "empty", timeoutTicks = 200)
    public static void exportPonderSchematic(GameTestHelper helper) {
        if (!Boolean.getBoolean("redline.exportPonder")) {
            helper.succeed();
            return;
        }
        BlockPos o = new BlockPos(1, 0, 1); // schematic origin inside the test area
        for (int x = 0; x < 7; x++)
            for (int z = 0; z < 7; z++)
                helper.setBlock(o.offset(x, 0, z), (x + z) % 2 == 0 ? Blocks.SNOW_BLOCK : Blocks.WHITE_CONCRETE);
        for (int x = 1; x <= 3; x++)
            for (int z = 2; z <= 4; z++)
                helper.setBlock(o.offset(x, 1, z), AllBlocks.BLAZE_BURNER.getDefaultState());
        FluidTankBlockEntity tank = buildTank(helper, o.offset(1, 2, 2));
        tank.getTankInventory().fill(new FluidStack(Fluids.WATER, 200_000), IFluidHandler.FluidAction.EXECUTE);
        helper.setBlock(o.offset(4, 3, 3), ModBlocks.STEAM_REDLINER.getDefaultState()
                .setValue(SteamRedlinerBlock.FACING, Direction.EAST).setValue(SteamRedlinerBlock.ROTOR, KineticTier.VIBRANIUM));
        for (int x = 5; x <= 6; x++)
            axisX(helper, o.offset(x, 3, 3), ModBlocks.SHAFTS.get(KineticTier.VIBRANIUM).get());

        helper.runAfterDelay(20, () -> {
            StructureTemplate template = new StructureTemplate();
            template.fillFromWorld(helper.getLevel(), helper.absolutePos(o), new Vec3i(7, 6, 7), false, null);
            try {
                Path out = Path.of("ponder_export", "steam_redliner.nbt");
                Files.createDirectories(out.getParent());
                NbtIo.writeCompressed(template.save(new CompoundTag()), out);
                CreateRedline.LOGGER.info("Exported Ponder schematic to {}", out.toAbsolutePath());
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
            helper.succeed();
        });
    }
}
