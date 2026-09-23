package com.mojophysics.create_mobile_physics;

import com.mojang.serialization.MapCodec;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.RandomSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.DirectionalBlock;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;

public class ThrusterBlock extends DirectionalBlock {

    public static final MapCodec<ThrusterBlock> CODEC = simpleCodec(ThrusterBlock::new);

    public ThrusterBlock(BlockBehaviour.Properties properties) {
        super(properties);
        this.registerDefaultState(this.stateDefinition.any().setValue(FACING, Direction.NORTH));
    }

    @Override
    protected MapCodec<? extends DirectionalBlock> codec() {
        return CODEC;
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
        builder.add(FACING);
    }

    @Override
    public BlockState getStateForPlacement(BlockPlaceContext context) {
        return this.defaultBlockState().setValue(FACING, context.getNearestLookingDirection().getOpposite());
    }

    @Override
    public void onPlace(BlockState state, Level level, BlockPos pos, BlockState oldState, boolean isMoving) {
        super.onPlace(state, level, pos, oldState, isMoving);
        if (!level.isClientSide) {
            level.scheduleTick(pos, this, 4);
        }
    }

    @Override
    public void neighborChanged(BlockState state, Level level, BlockPos pos, Block block, BlockPos fromPos, boolean isMoving) {
        if (!level.isClientSide) {
            level.scheduleTick(pos, this, 2);
        }
    }

    @Override
    public void tick(BlockState state, ServerLevel level, BlockPos pos, RandomSource random) {
        if (level.hasNeighborSignal(pos)) {
            applyThrust(level, pos, state.getValue(FACING));
        }
        level.scheduleTick(pos, this, Config.THRUSTER_TICK_INTERVAL.get());
    }

    private void applyThrust(ServerLevel level, BlockPos pos, Direction facing) {
        double force = Config.THRUSTER_FORCE.get();
        Vec3 dir = Vec3.atLowerCornerOf(facing.getNormal()).normalize().scale(force * 0.08);

        AABB box = new AABB(pos).inflate(Config.THRUSTER_RADIUS.get());
        for (Entity entity : level.getEntities(null, box)) {
            if (entity instanceof Player || entity.getType().getCategory().isFriendly()) {
                Vec3 motion = entity.getDeltaMovement().add(dir);
                double max = Config.MAX_HORIZONTAL_SPEED.get();
                double hx = Math.max(-max, Math.min(max, motion.x));
                double hz = Math.max(-max, Math.min(max, motion.z));
                entity.setDeltaMovement(hx, motion.y, hz);
                entity.hurtMarked = true;
            }
        }
    }
                }
