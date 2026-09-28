package com.mojophysics.create_mobile_physics;

import net.neoforged.neoforge.common.ModConfigSpec;

/** Mojo / 2GB RAM odakli ayarlar */
public class Config {
    private static final ModConfigSpec.Builder B = new ModConfigSpec.Builder();

    public static final ModConfigSpec.IntValue BLOCKS_PER_BALLOON = B
            .comment("Kac blok icin 1 balon gerekir (dekoratif sayac)")
            .defineInRange("blocksPerBalloon", 10, 5, 50);

    public static final ModConfigSpec.IntValue WINGS_PER_MOTOR = B
            .comment("1 kanat motoru icin gereken kanat sayisi")
            .defineInRange("wingsPerMotor", 5, 2, 20);

    public static final ModConfigSpec.IntValue COUNT_RADIUS = B
            .comment("Balon/kanat sayim yaricapi")
            .defineInRange("countRadius", 8, 4, 16);

    public static final ModConfigSpec.IntValue COUNT_CACHE_TICKS = B
            .comment("Sayim cache suresi (tick). Surekli sayma yok.")
            .defineInRange("countCacheTicks", 40, 20, 200);

    public static final ModConfigSpec.DoubleValue BALLOON_LIFT = B
            .comment("Isitici + yeterli balon iken dikey kuvvet")
            .defineInRange("balloonLift", 0.12, 0.01, 1.0);

    public static final ModConfigSpec.DoubleValue MAX_UPWARD_SPEED = B
            .defineInRange("maxUpwardSpeed", 0.55, 0.1, 2.0);

    public static final ModConfigSpec.DoubleValue HEATER_BOOST = B
            .defineInRange("heaterBoost", 1.0, 0.5, 3.0);

    public static final ModConfigSpec.IntValue HEATER_TICK = B
            .defineInRange("heaterTickInterval", 6, 2, 20);

    public static final ModConfigSpec.DoubleValue WING_MOTOR_FORCE = B
            .comment("Kanat motoru kuvveti (yonlu)")
            .defineInRange("wingMotorForce", 0.18, 0.01, 1.0);

    public static final ModConfigSpec.DoubleValue WING_MOTOR_TURN = B
            .comment("Kanat motoru donus (yaw) etkisi - hafif")
            .defineInRange("wingMotorTurn", 0.04, 0.0, 0.3);

    public static final ModConfigSpec.IntValue MOTOR_TICK = B
            .defineInRange("wingMotorTickInterval", 4, 2, 20);

    public static final ModConfigSpec.DoubleValue JET_FORCE = B
            .defineInRange("jetForce", 0.24, 0.01, 2.0);

    public static final ModConfigSpec.IntValue JET_TICK = B
            .defineInRange("jetTickInterval", 4, 2, 20);

    public static final ModConfigSpec.DoubleValue MAX_HORIZONTAL_SPEED = B
            .defineInRange("maxHorizontalSpeed", 0.8, 0.1, 3.0);

    public static final ModConfigSpec.DoubleValue SUSPENSION_DAMPING = B
            .defineInRange("suspensionDamping", 0.65, 0.1, 0.95);

    public static final ModConfigSpec.DoubleValue TRACK_FRICTION = B
            .defineInRange("trackFriction", 0.15, 0.0, 0.5);

    public static final ModConfigSpec.IntValue MAX_ENTITIES_PER_TICK = B
            .comment("Tick basina max etkilenen entity (2GB guvenlik)")
            .defineInRange("maxEntitiesPerTick", 6, 2, 20);

    static final ModConfigSpec SPEC = B.build();
}
