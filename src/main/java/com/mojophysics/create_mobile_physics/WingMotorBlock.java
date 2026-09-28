package com.mojophysics.create_mobile_physics;

import com.mojang.serialization.MapCodec;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.RandomSource;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.DirectionalBlock;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.phys.Vec3;

public class WingMotorBlock extends DirectionalBlock {
    public static final MapCodec<WingMotorBlock> CODEC = simpleCodec(WingMotorBlock::new);

    public WingMotorBlock(BlockBehaviour.Properties properties) {
        super(properties);
        this.registerDefaultState(this.stateDefinition.any().setValue(FACING, Direction.NORTH));
    }

    @Override
    protected MapCodec<? extends DirectionalBlock> codec() { return CODEC; }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> b) { b.add(FACING); }

    @Override
    public BlockState getStateForPlacement(BlockPlaceContext ctx) {
        return defaultBlockState().setValue(FACING, ctx.getNearestLookingDirection().getOpposite());
    }

    @Override
    public void onPlace(BlockState state, Level level, BlockPos pos, BlockState oldState, boolean isMoving) {
        super.onPlace(state, level, pos, oldState, isMoving);
        if (!level.isClientSide) level.scheduleTick(pos, this, Config.MOTOR_TICK.get());
    }

    @Override
    public void neighborChanged(BlockState state, Level level, BlockPos pos, Block block, BlockPos fromPos, boolean isMoving) {
        if (!level.isClientSide) level.scheduleTick(pos, this, 2);
    }

    @Override
    public void tick(BlockState state, ServerLevel level, BlockPos pos, RandomSource random) {
        if (level.hasNeighborSignal(pos)) {
            AssemblyManager.Assembly a = AssemblyManager.findAt(level, pos);
            boolean okWings = a != null ? AssemblyManager.enoughWings(a)
                    : StructureCounts.hasEnoughWingsForMotor(StructureCounts.get(level, pos), 1);
            if (okWings) {
                Direction f = state.getValue(FACING);
                double force = Config.WING_MOTOR_FORCE.get() * 0.07;
                Vec3 forward = Vec3.atLowerCornerOf(f.getNormal()).normalize().scale(force);
                if (a != null) {
                    AssemblyManager.applyForce(level, a, forward,
                            Config.MAX_HORIZONTAL_SPEED.get(), Config.MAX_UPWARD_SPEED.get());
                } else {
                    AssemblyManager.Assembly temp = AssemblyManager.glue(level, pos);
                    AssemblyManager.applyForce(level, temp, forward,
                            Config.MAX_HORIZONTAL_SPEED.get(), Config.MAX_UPWARD_SPEED.get());
                }
            }
        }
        level.scheduleTick(pos, this, Config.MOTOR_TICK.get());
    }
}
