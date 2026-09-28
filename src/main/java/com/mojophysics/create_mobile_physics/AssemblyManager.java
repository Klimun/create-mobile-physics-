package com.mojophysics.create_mobile_physics;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Fizik tutkali: BFS ile bagli bloklari bir "arac" sayar.
 * Balon/kanat sayimi bir kez. Motor/isitici kuvveti tum arac AABB icindeki entity'lere.
 * Mobil: max blok limiti, cache, tick basina entity limiti.
 */
public final class AssemblyManager {

    public static final class Assembly {
        public final UUID id;
        public final Set<BlockPos> blocks;
        public final BlockPos min;
        public final BlockPos max;
        public final int balloons;
        public final int wings;
        public final int blockCount;
        public long lastForceTime;

        public Assembly(UUID id, Set<BlockPos> blocks, int balloons, int wings) {
            this.id = id;
            this.blocks = blocks;
            this.balloons = balloons;
            this.wings = wings;
            this.blockCount = blocks.size();
            int minX = Integer.MAX_VALUE, minY = Integer.MAX_VALUE, minZ = Integer.MAX_VALUE;
            int maxX = Integer.MIN_VALUE, maxY = Integer.MIN_VALUE, maxZ = Integer.MIN_VALUE;
            for (BlockPos p : blocks) {
                minX = Math.min(minX, p.getX());
                minY = Math.min(minY, p.getY());
                minZ = Math.min(minZ, p.getZ());
                maxX = Math.max(maxX, p.getX());
                maxY = Math.max(maxY, p.getY());
                maxZ = Math.max(maxZ, p.getZ());
            }
            this.min = new BlockPos(minX, minY, minZ);
            this.max = new BlockPos(maxX, maxY, maxZ);
        }

        public AABB aabb() {
            return new AABB(min.getX(), min.getY(), min.getZ(),
                    max.getX() + 1.0, max.getY() + 1.5, max.getZ() + 1.0).inflate(0.25);
        }

        public boolean contains(BlockPos pos) {
            return blocks.contains(pos);
        }
    }

    /** dimension key -> list of assemblies */
    private static final Map<String, List<Assembly>> BY_DIM = new ConcurrentHashMap<>();

    private AssemblyManager() {}

    private static String dimKey(Level level) {
        return level.dimension().location().toString();
    }

    /** Tutkal: origin'den BFS, max blok. Basariliysa Assembly doner. */
    public static Assembly glue(ServerLevel level, BlockPos origin) {
        int max = Config.MAX_STRUCTURE_BLOCKS.get();
        Set<BlockPos> found = new HashSet<>();
        ArrayDeque<BlockPos> q = new ArrayDeque<>();
        q.add(origin.immutable());
        found.add(origin.immutable());
        int balloons = 0, wings = 0;

        while (!q.isEmpty() && found.size() < max) {
            BlockPos cur = q.poll();
            BlockState st = level.getBlockState(cur);
            Block b = st.getBlock();
            if (b instanceof BalloonBlock) balloons++;
            if (b instanceof WingBlock) wings++;

            for (Direction d : Direction.values()) {
                BlockPos n = cur.relative(d);
                if (found.contains(n)) continue;
                if (!level.isLoaded(n)) continue;
                BlockState ns = level.getBlockState(n);
                if (ns.isAir()) continue;
                // Yapi: herhangi dolu blok (Create tutkali ile birlestirdigin cisim)
                // Cok genis olmasin diye max limit var
                found.add(n.immutable());
                q.add(n.immutable());
                if (found.size() >= max) break;
            }
        }

        Assembly a = new Assembly(UUID.randomUUID(), found, balloons, wings);
        BY_DIM.compute(dimKey(level), (k, list) -> {
            if (list == null) list = new ArrayList<>();
            // Ayni bolgedeki eski montaji kaldir (ust uste binmesin)
            list.removeIf(old -> overlaps(old, a));
            list.add(a);
            // Max 16 arac / dunya (mobil)
            while (list.size() > 16) list.remove(0);
            return list;
        });
        return a;
    }

    private static boolean overlaps(Assembly a, Assembly b) {
        return a.aabb().intersects(b.aabb());
    }

    public static Assembly findAt(Level level, BlockPos pos) {
        List<Assembly> list = BY_DIM.get(dimKey(level));
        if (list == null) return null;
        for (Assembly a : list) {
            if (a.contains(pos.immutable())) return a;
        }
        // Yakin AABB
        for (Assembly a : list) {
            if (a.aabb().contains(pos.getX() + 0.5, pos.getY() + 0.5, pos.getZ() + 0.5)) return a;
        }
        return null;
    }

    public static boolean enoughBalloons(Assembly a) {
        int per = Config.BLOCKS_PER_BALLOON.get();
        int need = Math.max(1, (Math.min(a.blockCount, 80) + per - 1) / per);
        return a.balloons >= need;
    }

    public static boolean enoughWings(Assembly a) {
        return a.wings >= Config.WINGS_PER_MOTOR.get();
    }

    /** Aractaki tum uygun entity'lere ayni hiz ekle (beraber hareket hissi). */
    public static void applyForce(ServerLevel level, Assembly a, Vec3 delta, double maxH, double maxV) {
        AABB box = a.aabb();
        int n = 0;
        int maxE = Config.MAX_ENTITIES_PER_TICK.get();
        for (Entity e : level.getEntities(null, box)) {
            if (n >= maxE) break;
            if (!(e instanceof Player) && !e.getType().getCategory().isFriendly()) continue;
            Vec3 m = e.getDeltaMovement().add(delta);
            double hx = Math.max(-maxH, Math.min(maxH, m.x));
            double hz = Math.max(-maxH, Math.min(maxH, m.z));
            double hy = Math.max(-maxV, Math.min(maxV, m.y));
            e.setDeltaMovement(hx, hy, hz);
            e.hurtMarked = true;
            n++;
        }
        a.lastForceTime = level.getGameTime();
    }

    public static void unglueNear(Level level, BlockPos pos) {
        List<Assembly> list = BY_DIM.get(dimKey(level));
        if (list == null) return;
        list.removeIf(a -> a.contains(pos.immutable()) || a.aabb().inflate(1).contains(pos.getX() + 0.5, pos.getY() + 0.5, pos.getZ() + 0.5));
    }
}
