package com.mojophysics.create_mobile_physics;

import net.neoforged.neoforge.common.ModConfigSpec;

/**
 * Performance-focused config for mobile / Mojo Launcher.
 * All values are tunable so users can reduce load on weak phones.
 */
public class Config {
    private static final ModConfigSpec.Builder BUILDER = new ModConfigSpec.Builder();

    // === Structure limits (prevent memory/CPU spikes) ===
    public static final ModConfigSpec.IntValue MAX_STRUCTURE_BLOCKS = BUILDER
            .comment("Maximum number of blocks allowed in a single physics structure (safety limit for phones)")
            .defineInRange("maxStructureBlocks", 48, 8, 256);

    // === Balloon ===
    public static final ModConfigSpec.DoubleValue BALLOON_LIFT = BUILDER
            .comment("Base upward force per balloon")
            .defineInRange("balloonLift", 0.12, 0.01, 1.0);

    public static final ModConfigSpec.IntValue BALLOON_RADIUS = BUILDER
            .comment("Radius (blocks) around balloon that receives lift")
            .defineInRange("balloonRadius", 3, 1, 8);

    public static final ModConfigSpec.IntValue BALLOON_TICK_INTERVAL = BUILDER
            .comment("How often (ticks) balloons recalculate lift. Higher = less CPU")
            .defineInRange("balloonTickInterval", 5, 2, 20);

    public static final ModConfigSpec.DoubleValue MAX_UPWARD_SPEED = BUILDER
            .comment("Hard cap on upward velocity from balloons")
            .defineInRange("maxUpwardSpeed", 0.6, 0.1, 2.0);

    // === Thruster ===
    public static final ModConfigSpec.DoubleValue THRUSTER_FORCE = BUILDER
            .comment("Force applied by powered thruster")
            .defineInRange("thrusterForce", 0.25, 0.01, 2.0);

    public static final ModConfigSpec.IntValue THRUSTER_RADIUS = BUILDER
            .comment("Radius around thruster that receives force")
            .defineInRange("thrusterRadius", 2, 1, 6);

    public static final ModConfigSpec.IntValue THRUSTER_TICK_INTERVAL = BUILDER
            .comment("How often thrusters tick when powered")
            .defineInRange("thrusterTickInterval", 4, 2, 20);

    public static final ModConfigSpec.DoubleValue MAX_HORIZONTAL_SPEED = BUILDER
            .comment("Hard cap on horizontal speed from thrusters")
            .defineInRange("maxHorizontalSpeed", 0.8, 0.1, 3.0);

    static final ModConfigSpec SPEC = BUILDER.build();
}
