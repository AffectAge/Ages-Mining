package com.agesmining.agesmining.config;

import net.minecraftforge.common.ForgeConfigSpec;

public class AgesMiningConfig {

    public static final ForgeConfigSpec SPEC;
    public static final AgesMiningConfig INSTANCE;

    // Cave-in general settings
    public final ForgeConfigSpec.BooleanValue CAVE_INS_ENABLED;
    public final ForgeConfigSpec.IntValue CHECK_RADIUS;
    public final ForgeConfigSpec.IntValue SUPPORT_PILLAR_RANGE;
    public final ForgeConfigSpec.IntValue SUPPORT_BEAM_RANGE;
    public final ForgeConfigSpec.DoubleValue BASE_COLLAPSE_CHANCE;
    public final ForgeConfigSpec.DoubleValue COLLAPSE_TRIGGER_CHANCE;
    public final ForgeConfigSpec.DoubleValue COLLAPSE_FAKE_TRIGGER_CHANCE;
    public final ForgeConfigSpec.DoubleValue COLLAPSE_PROPAGATE_CHANCE;
    public final ForgeConfigSpec.DoubleValue COLLAPSE_EXPLOSION_PROPAGATE_CHANCE;
    public final ForgeConfigSpec.IntValue COLLAPSE_MIN_RADIUS;
    public final ForgeConfigSpec.IntValue COLLAPSE_RADIUS_VARIANCE;
    public final ForgeConfigSpec.IntValue MIN_DEPTH_FOR_COLLAPSE;
    public final ForgeConfigSpec.IntValue COLLAPSE_PROPAGATION_RADIUS;
    public final ForgeConfigSpec.IntValue MAX_BLOCKS_PER_COLLAPSE;
    public final ForgeConfigSpec.IntValue COLLAPSE_DELAY_TICKS;

    // Particle & sound settings
    public final ForgeConfigSpec.BooleanValue ENABLE_WARNING_PARTICLES;
    public final ForgeConfigSpec.BooleanValue ENABLE_SOUNDS;
    public final ForgeConfigSpec.BooleanValue ENABLE_SCREEN_SHAKE;

    // Damage settings
    public final ForgeConfigSpec.BooleanValue DAMAGE_PLAYERS;
    public final ForgeConfigSpec.DoubleValue COLLAPSE_DAMAGE_PER_BLOCK;
    public final ForgeConfigSpec.BooleanValue DESTROY_ITEMS_ON_COLLAPSE;

    // Block stability overrides
    public final ForgeConfigSpec.IntValue STONE_STABILITY;
    public final ForgeConfigSpec.IntValue DEEPSLATE_STABILITY;
    public final ForgeConfigSpec.IntValue GRAVEL_STABILITY;
    public final ForgeConfigSpec.IntValue SAND_STABILITY;
    public final ForgeConfigSpec.IntValue DIRT_STABILITY;
    public final ForgeConfigSpec.IntValue ORE_STABILITY;

    static {
        ForgeConfigSpec.Builder builder = new ForgeConfigSpec.Builder();

        INSTANCE = new AgesMiningConfig(builder);
        SPEC = builder.build();
    }

    private AgesMiningConfig(ForgeConfigSpec.Builder builder) {

        builder.comment("Ages Mining - Cave-In Configuration").push("cave_ins");

        CAVE_INS_ENABLED = builder
            .comment("Enable the cave-in mechanic entirely")
            .define("enabled", true);

        MIN_DEPTH_FOR_COLLAPSE = builder
            .comment("Minimum Y level below which cave-ins can occur (sea level = 63)")
            .defineInRange("minDepthForCollapse", 55, -64, 320);

        BASE_COLLAPSE_CHANCE = builder
            .comment("Base probability that a newly unsupported area will collapse (0.0 - 1.0)")
            .defineInRange("baseCollapseChance", 0.35, 0.0, 1.0);

        COLLAPSE_TRIGGER_CHANCE = builder
            .comment("Chance for a real collapse to be triggered by mining")
            .defineInRange("collapseTriggerChance", 0.10, 0.0, 1.0);

        COLLAPSE_FAKE_TRIGGER_CHANCE = builder
            .comment("Chance for a fake collapse warning (sound/particles only) when mining")
            .defineInRange("collapseFakeTriggerChance", 0.35, 0.0, 1.0);

        COLLAPSE_PROPAGATE_CHANCE = builder
            .comment("Chance for each candidate block to propagate an active collapse wave")
            .defineInRange("collapsePropagateChance", 0.55, 0.0, 1.0);

        COLLAPSE_EXPLOSION_PROPAGATE_CHANCE = builder
            .comment("Chance for explosion-affected blocks to seed collapse waves")
            .defineInRange("collapseExplosionPropagateChance", 0.30, 0.0, 1.0);

        COLLAPSE_MIN_RADIUS = builder
            .comment("Minimum radius for a new collapse")
            .defineInRange("collapseMinRadius", 3, 1, 32);

        COLLAPSE_RADIUS_VARIANCE = builder
            .comment("Random radius variance; total radius is minRadius + random(variance)")
            .defineInRange("collapseRadiusVariance", 16, 1, 32);

        CHECK_RADIUS = builder
            .comment("Radius (in blocks) around a broken block to check for structural support")
            .defineInRange("checkRadius", 6, 1, 16);

        SUPPORT_PILLAR_RANGE = builder
            .comment("Vertical range of a Mine Support Pillar block")
            .defineInRange("supportPillarRange", 5, 1, 16);

        SUPPORT_BEAM_RANGE = builder
            .comment("Horizontal range of a Mine Support Beam block")
            .defineInRange("supportBeamRange", 7, 1, 20);

        COLLAPSE_PROPAGATION_RADIUS = builder
            .comment("How far (in blocks) a collapse can chain-propagate to neighboring unsupported blocks")
            .defineInRange("collapsePropagationRadius", 3, 0, 8);

        MAX_BLOCKS_PER_COLLAPSE = builder
            .comment("Maximum number of blocks that can fall in a single collapse event")
            .defineInRange("maxBlocksPerCollapse", 64, 1, 256);

        COLLAPSE_DELAY_TICKS = builder
            .comment("Ticks between a block being flagged as unstable and actually collapsing (20 ticks = 1 sec)")
            .defineInRange("collapseDelayTicks", 40, 5, 200);

        builder.pop();

        builder.comment("Visual and Sound Effects").push("effects");

        ENABLE_WARNING_PARTICLES = builder
            .comment("Show dust particle warnings before a collapse")
            .define("warningParticles", true);

        ENABLE_SOUNDS = builder
            .comment("Play cracking/rumble sounds near unstable areas")
            .define("enableSounds", true);

        ENABLE_SCREEN_SHAKE = builder
            .comment("Apply screen shake effect during collapses (client side)")
            .define("screenShake", true);

        builder.pop();

        builder.comment("Damage and Gameplay").push("gameplay");

        DAMAGE_PLAYERS = builder
            .comment("Whether falling blocks from a collapse deal damage to players")
            .define("damageplayers", true);

        COLLAPSE_DAMAGE_PER_BLOCK = builder
            .comment("Damage dealt per collapsed block falling on a player")
            .defineInRange("damagePerBlock", 1.5, 0.0, 20.0);

        DESTROY_ITEMS_ON_COLLAPSE = builder
            .comment("Whether items on the ground are destroyed by a collapse")
            .define("destroyItems", false);

        builder.pop();

        builder.comment("Block Stability Values (higher = more stable, 0 = non-structural)").push("stability");

        STONE_STABILITY = builder.defineInRange("stone", 10, 0, 100);
        DEEPSLATE_STABILITY = builder.defineInRange("deepslate", 14, 0, 100);
        GRAVEL_STABILITY = builder.defineInRange("gravel", 3, 0, 100);
        SAND_STABILITY = builder.defineInRange("sand", 2, 0, 100);
        DIRT_STABILITY = builder.defineInRange("dirt", 5, 0, 100);
        ORE_STABILITY = builder.defineInRange("ores", 8, 0, 100);

        builder.pop();
    }
}
