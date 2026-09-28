package com.mojophysics.create_mobile_physics;

import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.RandomSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;

/**
 * Sicak hava ufleyicisi. Redstone + yeterli balon => dikey fizik.
 * Balonlar sadece sayilir; kuvvet burada.
 */
public class HeaterBlock extends Block {
    public HeaterBlock(BlockBehaviour.Properties properties) {
        super(properties);
    }

    @Override
    public void onPlace(BlockState state, Level level, BlockPos pos, BlockState oldState, boolean isMoving) {
        super.onPlace(state, level, pos, oldState, isMoving);
        if (!level.isClientSide) level.scheduleTick(pos, this, Config.HEATER_TICK.get());
    }

    @Override
    public void neighborChanged(BlockState state, Level level, BlockPos pos, Block block, BlockPos fromPos, boolean isMoving) {
        if (!level.isClientSide) level.scheduleTick(pos, this, 2);
    }

    @Override
    public void tick(BlockState state, ServerLevel level, BlockPos pos, RandomSource random) {
        if (level.hasNeighborSignal(pos)) {
            StructureCounts.Counts c = StructureCounts.get(level, pos);
            if (StructureCounts.hasEnoughBalloons(c)) {
                applyLift(level, pos);
            }
        }
        level.scheduleTick(pos, this, Config.HEATER_TICK.get());
    }

    private void applyLift(ServerLevel level, BlockPos pos) {
        double force = Config.BALLOON_LIFT.get() * Config.HEATER_BOOST.get();
        AABB box = new AABB(pos).inflate(4.0);
        int n = 0;
        int max = Config.MAX_ENTITIES_PER_TICK.get();
        for (Entity entity : level.getEntities(null, box)) {
            if (n >= max) break;
            if (!(entity instanceof Player) && !entity.getType().getCategory().isFriendly()) continue;
            Vec3 m = entity.getDeltaMovement();
            double ny = Math.min(m.y + force * 0.06, Config.MAX_UPWARD_SPEED.get());
            entity.setDeltaMovement(m.x * 0.98, ny, m.z * 0.98);
            entity.hurtMarked = true;
            n++;
        }
    }
}