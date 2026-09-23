package com.mojophysics.create_mobile_physics;

import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.phys.Vec3;

/**
 * Simple balloon block. Applies upward force to nearby entities / players standing on structures.
 * Lightweight: no complex rigid-body. Designed for mobile performance.
 * Multiple balloons in an area share the lift effect.
 */
public class BalloonBlock extends Block {

    public BalloonBlock(BlockBehaviour.Properties properties) {
        super(properties);
    }

    @Override
    public void onPlace(BlockState state, Level level, BlockPos pos, BlockState oldState, boolean isMoving) {
        super.onPlace(state, level, pos, oldState, isMoving);
        if (!level.isClientSide) {
            level.scheduleTick(pos, this, 5); // start ticking
        }
    }

    @Override
    public void tick(BlockState state, ServerLevel level, BlockPos pos, RandomSource random) {
        // Apply lift every few ticks to keep cost low
        applyLift(level, pos);
        level.scheduleTick(pos, this, Config.BALLOON_TICK_INTERVAL.get());
    }

    private void applyLift(ServerLevel level, BlockPos pos) {
        double baseLift = Config.BALLOON_LIFT.get();
        // Simple altitude decay so high balloons give less extra lift (prevents infinite height abuse)
        double y = pos.getY();
        double altitudeFactor = Math.max(0.3, 1.0 - Math.max(0, (y - 64) / 200.0));
        double force = baseLift * altitudeFactor;

        // Affect entities in a small radius around the balloon (cheap AABB check)
        AABB box = new AABB(pos).inflate(Config.BALLOON_RADIUS.get());
        for (Entity entity : level.getEntities(null, box)) {
            if (entity instanceof Player || entity instanceof ItemEntity || entity.getType().getCategory().isFriendly()) {
                // Only apply if entity is roughly "supported" or close
                Vec3 motion = entity.getDeltaMovement();
                // Gentle upward acceleration, capped
                double newY = Math.min(motion.y + force * 0.05, Config.MAX_UPWARD_SPEED.get());
                entity.setDeltaMovement(motion.x * 0.98, newY, motion.z * 0.98);
                entity.hurtMarked = true;
            }
        }
    }

    @Override
    public boolean isRandomlyTicking(BlockState state) {
        return true;
    }
}
