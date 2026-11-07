package com.createredline.content.turbine;

import com.createredline.registry.ModBlockEntities;
import com.createredline.registry.ModBlocks;
import com.createredline.registry.ModComponents;
import com.createredline.tier.ITieredKinetic;
import com.createredline.tier.KineticTier;
import com.simibubi.create.content.fluids.tank.FluidTankBlockEntity;
import com.simibubi.create.content.kinetics.base.DirectionalKineticBlock;
import com.simibubi.create.foundation.block.IBE;
import net.createmod.catnip.data.Iterate;
import net.minecraft.ChatFormatting;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.RandomSource;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.EnumProperty;
import net.minecraft.world.phys.HitResult;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.function.BiConsumer;

/**
 * Core of the 3x3 Steam Redliner. Placed against the centre of a 3x3x3 Create fluid tank's side or top face;
 * FACING points away from the tank and the output shaft comes out of that face. On placement it fills the
 * surrounding 3x3 face with {@link SteamRedlinerStructuralBlock}s and renders the whole housing from this block.
 * Boiler validation lives in the BE. ROTOR (set by the large cog it was crafted with) picks the rotor colour and the
 * output speed, and makes the core count as a part of that tier, so its output shaft must be that tier or better.
 */
public class SteamRedlinerBlock extends DirectionalKineticBlock implements IBE<SteamRedlinerBlockEntity>, ITieredKinetic {
    public static final EnumProperty<KineticTier> ROTOR = EnumProperty.create("rotor", KineticTier.class);

    public SteamRedlinerBlock(Properties properties) {
        super(properties);
        registerDefaultState(defaultBlockState().setValue(ROTOR, KineticTier.BASE));
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
        super.createBlockStateDefinition(builder.add(ROTOR));
    }

    @Override
    public BlockState getStateForPlacement(BlockPlaceContext context) {
        // Face away from the block clicked on, so clicking a tank wall gives the right orientation.
        Direction facing = context.getClickedFace();
        // Burners sit under the tank, so the turbine can't hang from the bottom face.
        if (facing == Direction.DOWN)
            return null;
        Level level = context.getLevel();
        BlockPos pos = context.getClickedPos();

        boolean[] clear = {true};
        forEachHousingPos(pos, facing.getAxis(), (housingPos, toCore) -> {
            if (!level.getBlockState(housingPos).canBeReplaced(context))
                clear[0] = false;
        });
        if (!clear[0])
            return null;

        if (level.getBlockEntity(pos.relative(facing.getOpposite())) instanceof FluidTankBlockEntity tank
                && tank.getControllerBE() != null && !otherTurbines(level, tank.getControllerBE(), pos).isEmpty()) {
            Player player = context.getPlayer();
            if (player != null && level.isClientSide)
                player.displayClientMessage(Component.translatable("create_redline.turbine.one_per_boiler")
                        .withStyle(ChatFormatting.RED), true);
            return null;
        }

        KineticTier rotor = context.getItemInHand().getOrDefault(ModComponents.ROTOR_TIER.get(), KineticTier.BASE);
        return defaultBlockState().setValue(FACING, facing).setValue(ROTOR, rotor);
    }

    /** The 5 spots (4 sides + top) where a turbine core can sit on a 3x3x3 tank, keyed by the face it covers. */
    static void forEachTurbineSlot(FluidTankBlockEntity controller, BiConsumer<BlockPos, Direction> visitor) {
        BlockPos centre = controller.getBlockPos().offset(1, 1, 1);
        for (Direction face : Iterate.directions)
            if (face != Direction.DOWN)
                visitor.accept(centre.relative(face, 2), face);
    }

    /** Turbine cores attached to this tank's face centres, excluding {@code self}. */
    static List<BlockPos> otherTurbines(BlockGetter level, FluidTankBlockEntity controller, BlockPos self) {
        List<BlockPos> found = new ArrayList<>();
        forEachTurbineSlot(controller, (slot, face) -> {
            BlockState state = level.getBlockState(slot);
            if (!slot.equals(self) && state.getBlock() instanceof SteamRedlinerBlock && state.getValue(FACING) == face)
                found.add(slot);
        });
        return found;
    }

    /** Visits the 8 housing positions around the core in the plane perpendicular to the turbine axis. */
    static void forEachHousingPos(BlockPos core, Direction.Axis axis, BiConsumer<BlockPos, Direction> visitor) {
        for (Direction side : Iterate.directions) {
            if (side.getAxis() == axis)
                continue;
            for (boolean corner : Iterate.falseAndTrue) {
                Direction step = corner ? side.getClockWise(axis) : side;
                BlockPos pos = (corner ? core.relative(side) : core).relative(step);
                visitor.accept(pos, step.getOpposite());
            }
        }
    }

    @Override
    public void onPlace(BlockState state, Level level, BlockPos pos, BlockState oldState, boolean isMoving) {
        super.onPlace(state, level, pos, oldState, isMoving);
        if (!level.getBlockTicks().hasScheduledTick(pos, this))
            level.scheduleTick(pos, this, 1);
    }

    @Override
    public void tick(BlockState state, ServerLevel level, BlockPos pos, RandomSource random) {
        Direction.Axis axis = state.getValue(FACING).getAxis();
        boolean[] blocked = {false};
        forEachHousingPos(pos, axis, (housingPos, toCore) -> {
            if (blocked[0])
                return;
            BlockState required = ModBlocks.STEAM_REDLINER_STRUCTURE.getDefaultState()
                    .setValue(SteamRedlinerStructuralBlock.FACING, toCore);
            BlockState occupied = level.getBlockState(housingPos);
            if (occupied == required)
                return;
            if (!occupied.canBeReplaced()) {
                blocked[0] = true;
                return;
            }
            level.setBlockAndUpdate(housingPos, required);
        });
        if (blocked[0])
            level.destroyBlock(pos, true);
    }

    public static ItemStack stackFor(BlockState state) {
        ItemStack stack = ModBlocks.STEAM_REDLINER.asStack();
        if (state.hasProperty(ROTOR) && state.getValue(ROTOR) != KineticTier.BASE)
            stack.set(ModComponents.ROTOR_TIER.get(), state.getValue(ROTOR));
        return stack;
    }

    @Override
    public ItemStack getCloneItemStack(BlockState state, HitResult target, LevelReader level, BlockPos pos, Player player) {
        return stackFor(state);
    }

    @Override
    public void appendHoverText(ItemStack stack, Item.TooltipContext context, List<Component> tooltip, TooltipFlag flag) {
        KineticTier rotor = stack.get(ModComponents.ROTOR_TIER.get());
        if (rotor == null)
            return;
        tooltip.add(Component.translatable("create_redline.turbine.rotor",
                        Component.translatable("create_redline.tier." + rotor.getSerializedName()).withStyle(tierColour(rotor)),
                        rpmText(rotor))
                .withStyle(ChatFormatting.GRAY));
    }

    /** Server config isn't available outside a world (e.g. the main menu), so fall back to the default value. */
    private static String rpmText(KineticTier rotor) {
        int rpm;
        try {
            rpm = rotor.turbineRpm();
        } catch (IllegalStateException | NullPointerException e) {
            rpm = 256 * (int) Math.pow(4, rotor.ordinal() - 1) * 2;
        }
        return String.format(Locale.ROOT, "%,d", rpm);
    }

    private static ChatFormatting tierColour(KineticTier tier) {
        return switch (tier) {
            case ALLTHEMODIUM -> ChatFormatting.GOLD;
            case VIBRANIUM -> ChatFormatting.GREEN;
            case UNOBTAINIUM -> ChatFormatting.LIGHT_PURPLE;
            case BASE -> ChatFormatting.GRAY;
        };
    }

    @Override
    public KineticTier getTier() {
        return KineticTier.BASE;
    }

    @Override
    public KineticTier getTier(BlockState state) {
        return state.getValue(ROTOR);
    }

    @Override
    public boolean hasShaftTowards(LevelReader world, BlockPos pos, BlockState state, Direction face) {
        return face == state.getValue(FACING);
    }

    @Override
    public Direction.Axis getRotationAxis(BlockState state) {
        return state.getValue(FACING).getAxis();
    }

    @Override
    public Class<SteamRedlinerBlockEntity> getBlockEntityClass() {
        return SteamRedlinerBlockEntity.class;
    }

    @Override
    public BlockEntityType<? extends SteamRedlinerBlockEntity> getBlockEntityType() {
        return ModBlockEntities.STEAM_REDLINER.get();
    }
}
