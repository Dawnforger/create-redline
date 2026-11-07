package com.createredline.content.turbine;

import com.createredline.config.RedlineConfig;
import com.simibubi.create.AllBlocks;
import com.simibubi.create.AllSoundEvents;
import com.simibubi.create.api.stress.BlockStressValues;
import net.createmod.catnip.math.VecHelper;
import net.createmod.ponder.api.level.PonderLevel;
import com.simibubi.create.content.fluids.tank.FluidTankBlockEntity;
import com.simibubi.create.content.kinetics.base.GeneratingKineticBlockEntity;
import com.simibubi.create.content.processing.burner.BlazeBurnerBlock;
import com.simibubi.create.content.processing.burner.BlazeBurnerBlock.HeatLevel;
import net.minecraft.ChatFormatting;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.tags.FluidTags;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import net.minecraft.world.level.block.state.BlockState;
import net.neoforged.neoforge.fluids.FluidStack;
import net.neoforged.neoforge.fluids.capability.IFluidHandler;

import java.util.List;

/**
 * Steam Redliner. Valid when the tank behind it is exactly 3 wide x 3 tall with the 3x3 housing centred on one face, all 9 blaze burners beneath it are
 * SEETHING (blaze cake), no steam engines are attached to the same boiler, and it can drain its water budget.
 * Provides a flat SU capacity = 4 x (what the 9 replaced steam engines would give), at a configurable RPM.
 *
 * Deliberately reads burners and water directly instead of piggy-backing on Create's BoilerData, which only
 * "activates" when steam engines are attached and whose internals change between Create versions.
 */
public class SteamRedlinerBlockEntity extends GeneratingKineticBlockEntity {
    private static final int CHECK_INTERVAL = 20;

    private Status status = Status.NO_TANK;
    private int checkCooldown;

    public SteamRedlinerBlockEntity(BlockEntityType<?> type, BlockPos pos, BlockState state) {
        super(type, pos, state);
    }

    public enum Status { RUNNING, NO_TANK, WRONG_SIZE, MISALIGNED, SHARED_BOILER, NEEDS_SUPERHEAT, NO_WATER, ENGINES_ATTACHED }

    @Override
    public void tick() {
        super.tick();
        if (level == null)
            return;
        // In a Ponder scene the status is set by the scene; never re-validate against the fake world.
        if (level.isClientSide || level instanceof PonderLevel) {
            if (status == Status.RUNNING)
                spawnSteam();
            return;
        }
        if (--checkCooldown > 0)
            return;
        checkCooldown = CHECK_INTERVAL;

        Status next = evaluate();
        if (next != status) {
            status = next;
            updateGeneratedRotation();
            notifyUpdate();
        }
    }

    private Status evaluate() {
        Direction facing = getBlockState().getValue(SteamRedlinerBlock.FACING);
        BlockPos behind = worldPosition.relative(facing.getOpposite());
        if (!(level.getBlockEntity(behind) instanceof FluidTankBlockEntity tank))
            return Status.NO_TANK;
        FluidTankBlockEntity controller = tank.getControllerBE();
        if (controller == null)
            return Status.NO_TANK;
        if (controller.getWidth() != 3 || controller.getHeight() != 3)
            return Status.WRONG_SIZE;
        // The 3x3 housing must cover the tank's face exactly: the core sits on the centre of that face.
        // The tank controller is the bottom min-x/min-z corner, so the tank centre is controller + (1,1,1).
        if (!behind.equals(controller.getBlockPos().offset(1, 1, 1).relative(facing)))
            return Status.MISALIGNED;
        // One turbine per boiler. Placement already refuses a second one; this covers tanks that merge or were
        // edited afterwards. The lowest position keeps the boiler so the outcome is deterministic.
        if (SteamRedlinerBlock.otherTurbines(level, controller, worldPosition).stream()
                .anyMatch(other -> other.asLong() < worldPosition.asLong()))
            return Status.SHARED_BOILER;
        // TODO verify field name on Create 6 BoilerData; the turbine must own the boiler exclusively.
        if (controller.boiler != null && controller.boiler.attachedEngines > 0)
            return Status.ENGINES_ATTACHED;

        BlockPos base = controller.getBlockPos().below();
        for (int x = 0; x < 3; x++)
            for (int z = 0; z < 3; z++) {
                BlockState burner = level.getBlockState(base.offset(x, 0, z));
                if (!burner.hasProperty(BlazeBurnerBlock.HEAT_LEVEL)
                        || burner.getValue(BlazeBurnerBlock.HEAT_LEVEL) != HeatLevel.SEETHING)
                    return Status.NEEDS_SUPERHEAT;
            }

        int needed = RedlineConfig.SERVER.turbineWaterPerTick.get() * CHECK_INTERVAL;
        IFluidHandler tankInv = controller.getTankInventory();
        FluidStack inTank = tankInv.getFluidInTank(0);
        if (needed > 0) {
            if (inTank.isEmpty() || !inTank.getFluid().is(FluidTags.WATER))
                return Status.NO_WATER;
            FluidStack drained = tankInv.drain(needed, IFluidHandler.FluidAction.SIMULATE);
            if (drained.getAmount() < needed)
                return Status.NO_WATER;
            tankInv.drain(needed, IFluidHandler.FluidAction.EXECUTE);
        }
        return Status.RUNNING;
    }

    private void spawnSteam() {
        RandomSource random = level.random;
        Direction facing = getBlockState().getValue(SteamRedlinerBlock.FACING);
        Vec3 normal = Vec3.atLowerCornerOf(facing.getNormal());
        if (random.nextFloat() < 0.35f) {
            // Wisps escaping round the bezel of the rotor chamber.
            Vec3 offset = new Vec3(random.nextFloat() * 2.4 - 1.2, 0.55, random.nextFloat() * 2.4 - 1.2);
            Vec3 pos = VecHelper.getCenterOf(worldPosition).add(orientFromUp(offset, facing));
            Vec3 motion = normal.scale(0.03 + random.nextFloat() * 0.03);
            level.addParticle(ParticleTypes.CLOUD, pos.x, pos.y, pos.z, motion.x, motion.y, motion.z);
        }
        if (random.nextInt(80) == 0)
            AllSoundEvents.STEAM.playAt(level, worldPosition, 0.35f, 0.7f + random.nextFloat() * 0.2f, false);
    }

    /** Maps an offset authored for an UP-facing turbine onto the real facing (same convention as the models). */
    private static Vec3 orientFromUp(Vec3 v, Direction facing) {
        return switch (facing) {
            case UP -> v;
            case DOWN -> new Vec3(v.x, -v.y, -v.z);
            case NORTH -> new Vec3(v.x, v.z, -v.y);
            case SOUTH -> new Vec3(-v.x, v.z, v.y);
            case EAST -> new Vec3(v.y, v.z, v.x);
            case WEST -> new Vec3(-v.y, v.z, -v.x);
        };
    }

    /** The rotor and housing span the whole 3x3 face, not just this block. */
    @Override
    protected AABB createRenderBoundingBox() {
        return new AABB(worldPosition).inflate(1);
    }

    /** Set by the rotor (crafting cog): higher tiers spin faster for the same total stress capacity. */
    public int outputRpm() {
        return getBlockState().getValue(SteamRedlinerBlock.ROTOR).turbineRpm();
    }

    /**
     * What steam engines on the same boiler would produce, mirroring Create's BoilerData/PoweredShaftBlockEntity:
     * the boiler level is min(heat, water, size); 9 seething burners give 18 heat, but a 3x3x3 tank caps it at
     * 27 / 4 = 6. Each level of engines yields 16 RPM worth of the steam engine's capacity (1,024 by default),
     * however many engines share it, so 9 engines give 6 * 16 * 1024 = 98,304 SU.
     */
    public static double steamEngineBaselineSU() {
        int boilerLevel = Math.min(18, (3 * 3 * 3) / 4);
        return boilerLevel * 16 * BlockStressValues.getCapacity(AllBlocks.STEAM_ENGINE.get());
    }

    public Status getStatus() {
        return status;
    }

    @Override
    public float getGeneratedSpeed() {
        if (status != Status.RUNNING)
            return 0;
        return convertToDirection(outputRpm(),
                getBlockState().getValue(SteamRedlinerBlock.FACING));
    }

    /** Create multiplies this per-RPM figure by |speed|, so divide to keep total SU constant at any output RPM. */
    @Override
    public float calculateAddedStressCapacity() {
        float rpm = outputRpm();
        float capacity = status == Status.RUNNING ? RedlineConfig.SERVER.turbineCapacitySU() / rpm : 0;
        this.lastCapacityProvided = capacity;
        return capacity;
    }

    @Override
    public boolean addToGoggleTooltip(List<Component> tooltip, boolean isPlayerSneaking) {
        super.addToGoggleTooltip(tooltip, isPlayerSneaking);
        tooltip.add(Component.literal("    ")
                .append(Component.translatable("create_redline.turbine.status." + status.name().toLowerCase(java.util.Locale.ROOT)))
                .withStyle(status == Status.RUNNING ? ChatFormatting.GREEN : ChatFormatting.RED));
        return true;
    }

    @Override
    protected void write(CompoundTag tag, HolderLookup.Provider registries, boolean clientPacket) {
        super.write(tag, registries, clientPacket);
        tag.putString("Status", status.name());
    }

    @Override
    protected void read(CompoundTag tag, HolderLookup.Provider registries, boolean clientPacket) {
        super.read(tag, registries, clientPacket);
        try {
            status = Status.valueOf(tag.getString("Status"));
        } catch (IllegalArgumentException e) {
            status = Status.NO_TANK;
        }
    }
}
