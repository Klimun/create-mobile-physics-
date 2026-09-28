package com.mojophysics.create_mobile_physics;

import net.minecraft.core.BlockPos;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Balon/kanat sayimi BIR KEZ yapilir, cache'lenir.
 * Surekli world taramasi yok -> mobil dostu.
 */
public final class StructureCounts {
    public record Counts(int balloons, int wings, int totalApprox, long gameTime) {}

    private static final Map<Long, Counts> CACHE = new ConcurrentHashMap<>();

    private StructureCounts() {}

    private static long key(Level level, BlockPos pos) {
        // 4x4x4 hucre - yakin motorlar ayni sayimi paylasir
        int cx = pos.getX() >> 2;
        int cy = pos.getY() >> 2;
        int cz = pos.getZ() >> 2;
        return (((long) level.dimension().location().hashCode()) << 32)
                ^ (((long) cx) << 20) ^ (((long) cy) << 10) ^ (cz & 0x3FF);
    }

    public static Counts get(Level level, BlockPos origin) {
        long k = key(level, origin);
        long time = level.getGameTime();
        Counts c = CACHE.get(k);
        int ttl = Config.COUNT_CACHE_TICKS.get();
        if (c != null && time - c.gameTime() < ttl) {
            return c;
        }
        Counts fresh = scan(level, origin, time);
        CACHE.put(k, fresh);
        if (CACHE.size() > 48) {
            CACHE.entrySet().removeIf(e -> time - e.getValue().gameTime() > ttl * 2L);
        }
        return fresh;
    }

    private static Counts scan(Level level, BlockPos origin, long time) {
        int r = Config.COUNT_RADIUS.get();
        int balloons = 0, wings = 0, total = 0;
        BlockPos.MutableBlockPos m = new BlockPos.MutableBlockPos();
        // Adim 2: daha az getBlockState (mobil)
        for (int dx = -r; dx <= r; dx += 1) {
            for (int dy = -2; dy <= 3; dy += 1) {
                for (int dz = -r; dz <= r; dz += 1) {
                    m.set(origin.getX() + dx, origin.getY() + dy, origin.getZ() + dz);
                    if (!level.isLoaded(m)) continue;
                    BlockState st = level.getBlockState(m);
                    if (st.isAir()) continue;
                    total++;
                    Block b = st.getBlock();
                    if (b instanceof BalloonBlock) balloons++;
                    else if (b instanceof WingBlock) wings++;
                }
            }
        }
        return new Counts(balloons, wings, total, time);
    }

    /** Yeterli balon var mi? (blok/10 kurali) */
    public static boolean hasEnoughBalloons(Counts c) {
        int need = Math.max(1, (c.totalApprox() + Config.BLOCKS_PER_BALLOON.get() - 1) / Config.BLOCKS_PER_BALLOON.get());
        // Toplam blok tahmini abartisli olabilir; en az 1 balon + oran
        int softNeed = Math.max(1, c.balloons() > 0 ? Math.min(need, c.balloons() + 2) : need);
        // Basit kural: balon >= ceil(total/10) ama total sadece dolu bloklar
        int required = Math.max(1, (Math.min(c.totalApprox(), 80) + Config.BLOCKS_PER_BALLOON.get() - 1)
                / Config.BLOCKS_PER_BALLOON.get());
        return c.balloons() >= required;
    }

    /** Kanat motoru icin: wings >= motors * wingsPerMotor; tek motor cagrisinda motors=1 */
    public static boolean hasEnoughWingsForMotor(Counts c, int motorCount) {
        return c.wings() >= motorCount * Config.WINGS_PER_MOTOR.get();
    }

    public static void invalidateNear(Level level, BlockPos pos) {
        CACHE.remove(key(level, pos));
    }
}
