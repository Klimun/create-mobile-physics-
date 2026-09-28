package com.mojophysics.create_mobile_physics;

import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.Vec3;

/**
 * Sicak hava: redstone + (montajda yeterli balon VEYA yerel sayim) => arac AABB icinde kaldirma.
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
            AssemblyManager.Assembly a = AssemblyManager.findAt(level, pos);
            boolean ok;
            if (a != null) {
                ok = AssemblyManager.enoughBalloons(a);
            } else {
                StructureCounts.Counts c = StructureCounts.get(level, pos);
                ok = StructureCounts.hasEnoughBalloons(c);
            }
            if (ok) {
                double f = Config.BALLOON_LIFT.get() * Config.HEATER_BOOST.get() * 0.06;
                Vec3 delta = new Vec3(0, f, 0);
                if (a != null) {
                    AssemblyManager.applyForce(level, a, delta,
                            Config.MAX_HORIZONTAL_SPEED.get(), Config.MAX_UPWARD_SPEED.get());
                } else {
                    // Montajsiz: eski yerel davranis
                    StructureCounts.Counts c = StructureCounts.get(level, pos);
                    AssemblyManager.Assembly temp = AssemblyManager.glue(level, pos);
                    AssemblyManager.applyForce(level, temp, delta,
                            Config.MAX_HORIZONTAL_SPEED.get(), Config.MAX_UPWARD_SPEED.get());
                }
            }
        }
        level.scheduleTick(pos, this, Config.HEATER_TICK.get());
    }
}
