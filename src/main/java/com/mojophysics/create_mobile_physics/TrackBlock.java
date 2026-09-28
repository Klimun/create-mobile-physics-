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

public class TrackBlock extends Block {
    public TrackBlock(BlockBehaviour.Properties p) { super(p); }

    @Override
    public void onPlace(BlockState s, Level l, BlockPos pos, BlockState o, boolean m) {
        super.onPlace(s, l, pos, o, m);
        if (!l.isClientSide) l.scheduleTick(pos, this, 4);
    }

    @Override
    public void tick(BlockState state, ServerLevel level, BlockPos pos, RandomSource random) {
        AABB box = new AABB(pos).inflate(0.15, 0.6, 0.15).move(0, 0.5, 0);
        double damp = Config.SUSPENSION_DAMPING.get();
        double fr = Config.TRACK_FRICTION.get();
        int n = 0, max = Config.MAX_ENTITIES_PER_TICK.get();
        for (Entity e : level.getEntities(null, box)) {
            if (n >= max) break;
            if (!(e instanceof Player) && !e.getType().getCategory().isFriendly()) continue;
            Vec3 m = e.getDeltaMovement();
            e.setDeltaMovement(m.x * (1 - fr * 0.5), m.y * damp, m.z * (1 - fr * 0.5));
            e.hurtMarked = true;
            n++;
        }
        level.scheduleTick(pos, this, 4);
    }
}
