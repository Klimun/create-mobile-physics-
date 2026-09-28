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

/** Jet motoru - kendi fizigi. Redstone ile yonlu itki. Balon/kanat sarti yok. */
public class ThrusterBlock extends DirectionalBlock {
    public static final MapCodec<ThrusterBlock> CODEC = simpleCodec(ThrusterBlock::new);

    public ThrusterBlock(BlockBehaviour.Properties properties) {
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
        if (!level.isClientSide) level.scheduleTick(pos, this, Config.JET_TICK.get());
    }

    @Override
    public void neighborChanged(BlockState state, Level level, BlockPos pos, Block block, BlockPos fromPos, boolean isMoving) {
        if (!level.isClientSide) level.scheduleTick(pos, this, 2);
    }

    @Override
    public void tick(BlockState state, ServerLevel level, BlockPos pos, RandomSource random) {
        if (level.hasNeighborSignal(pos)) {
            Direction f = state.getValue(FACING);
            Vec3 dir = Vec3.atLowerCornerOf(f.getNormal()).normalize().scale(Config.JET_FORCE.get() * 0.08);
            AABB box = new AABB(pos).inflate(2.5);
            int n = 0, max = Config.MAX_ENTITIES_PER_TICK.get();
            for (Entity e : level.getEntities(null, box)) {
                if (n >= max) break;
                if (!(e instanceof Player) && !e.getType().getCategory().isFriendly()) continue;
                Vec3 m = e.getDeltaMovement().add(dir);
                double maxH = Config.MAX_HORIZONTAL_SPEED.get();
                e.setDeltaMovement(
                        Math.max(-maxH, Math.min(maxH, m.x)),
                        m.y,
                        Math.max(-maxH, Math.min(maxH, m.z)));
                e.hurtMarked = true;
                n++;
            }
        }
        level.scheduleTick(pos, this, Config.JET_TICK.get());
    }
}
