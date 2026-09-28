package com.mojophysics.create_mobile_physics;

import net.minecraft.core.BlockPos;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;

/**
 * Dekoratif sayac blogu. Fizik uygulamaz.
 * Isitici, yeterli balon sayisini StructureCounts ile okur.
 */
public class BalloonBlock extends Block {
    public BalloonBlock(BlockBehaviour.Properties properties) {
        super(properties);
    }

    @Override
    public void onPlace(BlockState state, Level level, BlockPos pos, BlockState oldState, boolean isMoving) {
        super.onPlace(state, level, pos, oldState, isMoving);
        if (!level.isClientSide) StructureCounts.invalidateNear(level, pos);
    }

    @Override
    public void onRemove(BlockState state, Level level, BlockPos pos, BlockState newState, boolean isMoving) {
        if (!level.isClientSide) StructureCounts.invalidateNear(level, pos);
        super.onRemove(state, level, pos, newState, isMoving);
    }
}
