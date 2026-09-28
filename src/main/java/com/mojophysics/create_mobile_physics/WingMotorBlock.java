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

/**
 * Kanat motoru - uçak kontrolu.
 * Create tutkali yapi ile kullanilmasi beklenir (Create zorunlu).
 * En az wingsPerMotor (5) kanat yoksa kuvvet yok.
 * Redstone: bakis yonune itki; yan sinyaller donus hissi (hafif yatay sapma).
 */
public class WingMotorBlock extends DirectionalBlock {
    public static final MapCodec<WingMotorBlock> CODEC = simpleCodec(WingMotorBlock::new);

    public WingMotorBlock(BlockBehaviour.Properties properties) {
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
            StructureCounts.Counts c = StructureCounts.get(level, pos);
            if (StructureCounts.hasEnoughWingsForMotor(c, 1)) {
                applyControl(level, pos, state.getValue(FACING));
            }
        }
        level.scheduleTick(pos, this, Config.MOTOR_TICK.get());
    }

    private void applyControl(ServerLevel level, BlockPos pos, Direction facing) {
        double force = Config.WING_MOTOR_FORCE.get();
        double turn = Config.WING_MOTOR_TURN.get();
        Vec3 forward = Vec3.atLowerCornerOf(facing.getNormal()).normalize().scale(force * 0.07);
        // Hafif "aileron" - dikey bilesen az, yatay ana
        AABB box = new AABB(pos).inflate(3.5);
        int n = 0;
        int max = Config.MAX_ENTITIES_PER_TICK.get();
        for (Entity entity : level.getEntities(null, box)) {
            if (n >= max) break;
            if (!(entity instanceof Player) && !entity.getType().getCategory().isFriendly()) continue;
            Vec3 m = entity.getDeltaMovement().add(forward);
            // Cok hafif yan sapma (donus hissi) - mobil icin basitleştirilmiş
            Vec3 side = new Vec3(-forward.z, 0, forward.x).scale(turn * 0.5);
            m = m.add(side);
            double maxH = Config.MAX_HORIZONTAL_SPEED.get();
            double hx = Math.max(-maxH, Math.min(maxH, m.x));
            double hz = Math.max(-maxH, Math.min(maxH, m.z));
            double hy = Math.max(-Config.MAX_UPWARD_SPEED.get(), Math.min(Config.MAX_UPWARD_SPEED.get(), m.y));
            entity.setDeltaMovement(hx, hy, hz);
            entity.hurtMarked = true;
            n++;
        }
    }
}
