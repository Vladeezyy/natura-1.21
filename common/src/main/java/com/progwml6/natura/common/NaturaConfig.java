package com.progwml6.natura.common;

import com.progwml6.natura.world.tree.TreeWorld;

import com.progwml6.natura.platform.ConfigSpec;

/**
 * The 1.12 Natura config (natura.cfg), with the same options, sections and defaults. A common config,
 * config/natura-common.toml on both loaders (NeoForge: a ModConfigSpec built from {@link #SPEC}, Fabric: the same file
 * read and written by the Fabric project).
 */
public final class NaturaConfig {
    private static final ConfigSpec.Builder BUILDER = new ConfigSpec.Builder();

    static {
        BUILDER.push("ENABLE-DISABLE");
    }

    public static final ConfigSpec.BoolValue CAN_RESPAWN_IN_NETHER = BUILDER.define("Obelisks let players respawn in the Nether", true);
    public static final ConfigSpec.BoolValue GENERATE_REDWOOD = BUILDER.define("Generate Redwood Trees", false);
    public static final ConfigSpec.BoolValue GENERATE_MAPLE = BUILDER.define("Generate Maple Trees", true);
    public static final ConfigSpec.BoolValue GENERATE_SILVERBELL = BUILDER.define("Generate Silverbell Trees", true);
    public static final ConfigSpec.BoolValue GENERATE_AMARANTH = BUILDER.define("Generate Amaranth Trees", true);
    public static final ConfigSpec.BoolValue GENERATE_TIGER = BUILDER.define("Generate Tigerwood Trees", true);
    public static final ConfigSpec.BoolValue GENERATE_WILLOW = BUILDER.define("Generate Willow Trees", true);
    public static final ConfigSpec.BoolValue GENERATE_EUCALYPTUS = BUILDER.define("Generate Small Eucalyptus Trees", true);
    public static final ConfigSpec.BoolValue GENERATE_HOPSEED = BUILDER.define("Generate Hopseed Trees", true);
    public static final ConfigSpec.BoolValue GENERATE_SAKURA = BUILDER.define("Generate Sakura Trees", true);
    public static final ConfigSpec.BoolValue GENERATE_SAGUARO = BUILDER.define("Generate Saguaro Cactus", true);
    public static final ConfigSpec.BoolValue GENERATE_BLOODWOOD = BUILDER.define("Generate Bloodwood Trees", true);
    public static final ConfigSpec.BoolValue GENERATE_DARKWOOD = BUILDER.define("Generate Darkwood Trees", true);
    public static final ConfigSpec.BoolValue GENERATE_FUSEWOOD = BUILDER.define("Generate Fusewood Trees", true);
    public static final ConfigSpec.BoolValue GENERATE_GHOSTWOOD = BUILDER.define("Generate Ghostwood Trees", true);
    public static final ConfigSpec.BoolValue GENERATE_RASPBERRIES = BUILDER.define("Generate Raspberry Bushes", true);
    public static final ConfigSpec.BoolValue GENERATE_BLUEBERRIES = BUILDER.define("Generate Blueberry Bushes", true);
    public static final ConfigSpec.BoolValue GENERATE_BLACKBERRIES = BUILDER.define("Generate Blackberry Bushes", true);
    public static final ConfigSpec.BoolValue GENERATE_MALOBERRIES = BUILDER.define("Generate Maloberry Bushes", true);
    public static final ConfigSpec.BoolValue GENERATE_BLIGHTBERRIES = BUILDER.define("Generate Blightberry Bushes", true);
    public static final ConfigSpec.BoolValue GENERATE_DUSKBERRIES = BUILDER.define("Generate Duskberry Bushes", true);
    public static final ConfigSpec.BoolValue GENERATE_SKYBERRIES = BUILDER.define("Generate Skyberry Bushes", true);
    public static final ConfigSpec.BoolValue GENERATE_STINGBERRIES = BUILDER.define("Generate Stingberry Bushes", true);
    public static final ConfigSpec.BoolValue GENERATE_THORNVINES = BUILDER.define("Generate Thornvines", true);
    public static final ConfigSpec.BoolValue GENERATE_OVERWORLD_CLOUDS = BUILDER.define("Generate Overworld Clouds", true);
    public static final ConfigSpec.BoolValue GENERATE_SULFUR_CLOUDS = BUILDER.define("Generate Sulfur Clouds", true);
    public static final ConfigSpec.BoolValue GENERATE_ASH_CLOUDS = BUILDER.define("Generate Ash Clouds", true);
    public static final ConfigSpec.BoolValue GENERATE_DARK_CLOUDS = BUILDER.define("Generate Dark Clouds", true);
    public static final ConfigSpec.BoolValue ENABLE_CLOUD_BLOCKS = BUILDER.define("Enable Clouds", false);
    public static final ConfigSpec.BoolValue GENERATE_BARLEY = BUILDER.define("Generate Barley Crops", true);
    public static final ConfigSpec.BoolValue GENERATE_COTTON = BUILDER.define("Generate Cotton Crops", true);
    public static final ConfigSpec.BoolValue GENERATE_BLUEBELLS = BUILDER.define("Generate Bluebell Flowers", true);
    public static final ConfigSpec.BoolValue GENERATE_GREEN_GLOWSHROOM = BUILDER.define("Generate Green Glowshroom", true);
    public static final ConfigSpec.BoolValue GENERATE_PURPLE_GLOWSHROOM = BUILDER.define("Generate Purple Glowshroom", true);
    public static final ConfigSpec.BoolValue GENERATE_BLUE_GLOWSHROOM = BUILDER.define("Generate Blue Glowshroom", true);
    public static final ConfigSpec.BoolValue GENERATE_GLOWSHROOM_TREE = BUILDER.define("Generate Glowshroom Trees", true);
    public static final ConfigSpec.BoolValue DROP_COTTON = BUILDER.define("Drop cotton seeds from grass", true);
    public static final ConfigSpec.BoolValue DROP_BARLEY = BUILDER.define("Drop barley seeds from grass", true);
    public static final ConfigSpec.BoolValue ENABLE_WHEAT_RECIPE = BUILDER.define("Enable wheat to flour recipe", true);
    public static final ConfigSpec.BoolValue GENERATE_TAINTED_SOIL = BUILDER.define("Generate Tained Soil", true);
    public static final ConfigSpec.BoolValue GENERATE_HEAT_SAND = BUILDER.define("Generate Heat Sand", true);

    static {
        BUILDER.pop().push("Entities");
    }

    public static final ConfigSpec.BoolValue ENABLE_HEATSCAR_SPIDER = BUILDER.define("Enable Heatscar Spiders", false);

    static {
        BUILDER.pop().push("Mob-Changes");
    }

    public static final ConfigSpec.IntValue BABY_HEATSCAR_MINIMUM = BUILDER.defineInRange("Minimum Baby Heatscar Spiders on Spider Death", 2, 0, Integer.MAX_VALUE);
    public static final ConfigSpec.IntValue BABY_HEATSCAR_MAXIMUM = BUILDER.defineInRange("Maximum Baby Heatscar Spiders on Spider Death", 4, 0, Integer.MAX_VALUE);

    static {
        BUILDER.pop().push("Worldgen");
    }

    public static final ConfigSpec.IntValue SEA_LEVEL = BUILDER.comment("Controls what the lowest Y level trees can grow at, make lower if the tree will not grow")
            .defineInRange("Sea level", 64, -2048, 2048);
    public static final ConfigSpec.IntValue FLAT_SEA_LEVEL = BUILDER.comment(
                    "Controls what the lowest Y level trees can grow at in a flat world, make value lower if the tree will not grow",
                    "Counted from the bottom of the world (1.12 worlds started at y 0)")
            .defineInRange("Flat Sea level", 1, -2048, 2048);
    public static final ConfigSpec.IntValue REDWOOD_RARITY = rarity("Redwood Tree Spawn Rarity", 150);
    public static final ConfigSpec.IntValue MAPLE_RARITY = rarity("Maple Tree Spawn Rarity", 10);
    public static final ConfigSpec.IntValue SILVERBELL_RARITY = rarity("Silverbell Tree Spawn Rarity", 70);
    public static final ConfigSpec.IntValue AMARANTH_RARITY = rarity("Amaranth Tree Spawn Rarity", 1);
    public static final ConfigSpec.IntValue TIGER_RARITY = rarity("Tigerwood Tree Spawn Rarity", 30);
    public static final ConfigSpec.IntValue WILLOW_RARITY = rarity("Willow Tree Spawn Rarity", 10);
    public static final ConfigSpec.IntValue EUCALYPTUS_RARITY = rarity("Eucalyptus Tree Spawn Rarity", 25);
    public static final ConfigSpec.IntValue EUCALYPTUS_RANGE = rarity("Eucalyptus Tree Spawn Range", 32);
    public static final ConfigSpec.IntValue HOPSEED_RARITY = rarity("Hopseed Tree Spawn Rarity", 10);
    public static final ConfigSpec.IntValue HOPSEED_RANGE = rarity("Hopseed Tree Spawn Range", 20);
    public static final ConfigSpec.IntValue SAKURA_RARITY = rarity("Sakura Tree Spawn Rarity", 10);
    public static final ConfigSpec.IntValue SAKURA_RANGE = rarity("Sakura Tree Spawn Range", 32);
    public static final ConfigSpec.IntValue BLOODWOOD_RARITY = rarity("Bloodwood Tree Spawn Rarity", 14);
    public static final ConfigSpec.IntValue DARKWOOD_RARITY = rarity("Darkwood Tree Spawn Rarity", 10);
    public static final ConfigSpec.IntValue FUSEWOOD_RARITY = rarity("Fusewood Tree Spawn Rarity", 50);
    public static final ConfigSpec.IntValue GHOSTWOOD_RARITY = rarity("Ghostwood Tree Spawn Rarity", 10);
    public static final ConfigSpec.IntValue SAGUARO_RARITY = rarity("Saguaro Cactus Spawn Rarity", 5);
    public static final ConfigSpec.IntValue RASPBERRY_RARITY = rarity("Raspberry Spawn Rarity", 30);
    public static final ConfigSpec.IntValue RASPBERRY_RANGE = rarity("Raspberry Spawn Range", 64);
    public static final ConfigSpec.IntValue BLUEBERRY_RARITY = rarity("Blueberry Spawn Rarity", 34);
    public static final ConfigSpec.IntValue BLUEBERRY_RANGE = rarity("Blueberry Spawn Range", 64);
    public static final ConfigSpec.IntValue BLACKBERRY_RARITY = rarity("Blackberry Spawn Rarity", 48);
    public static final ConfigSpec.IntValue BLACKBERRY_RANGE = rarity("Blackberry Spawn Range", 64);
    public static final ConfigSpec.IntValue MALOBERRY_RARITY = rarity("Maloberry Spawn Rarity", 40);
    public static final ConfigSpec.IntValue MALOBERRY_RANGE = rarity("Maloberry Spawn Range", 64);
    public static final ConfigSpec.IntValue BLIGHTBERRY_RARITY = rarity("Blightberry Spawn Rarity", 18);
    public static final ConfigSpec.IntValue BLIGHTBERRY_RANGE = rarity("Blightberry Spawn Range", 100);
    public static final ConfigSpec.IntValue DUSKBERRY_RARITY = rarity("Duskberry Spawn Rarity", 18);
    public static final ConfigSpec.IntValue DUSKBERRY_RANGE = rarity("Duskberry Spawn Range", 100);
    public static final ConfigSpec.IntValue SKYBERRY_RARITY = rarity("Skyberry Spawn Rarity", 18);
    public static final ConfigSpec.IntValue SKYBERRY_RANGE = rarity("Skyberry Spawn Range", 100);
    public static final ConfigSpec.IntValue STINGBERRY_RARITY = rarity("Stingberry Spawn Rarity", 18);
    public static final ConfigSpec.IntValue STINGBERRY_RANGE = rarity("Stingberry Spawn Range", 100);
    public static final ConfigSpec.IntValue CLOUD_RARITY = rarity("Cloud Spawn Rarity", 10);
    public static final ConfigSpec.IntValue CLOUD_HEIGHT = BUILDER.defineInRange("Cloud Spawn Height", 192, -2048, 2048);
    public static final ConfigSpec.IntValue CLOUD_RANGE = rarity("Cloud Spawn Range", 48);
    public static final ConfigSpec.IntValue DARK_CLOUD_RARITY = rarity("Dark Cloud Spawn Density", 10);
    public static final ConfigSpec.IntValue DARK_CLOUD_HEIGHT = BUILDER.defineInRange("Dark Cloud Spawn Height", 64, -2048, 2048);
    public static final ConfigSpec.IntValue DARK_CLOUD_RANGE = rarity("Dark Cloud Spawn Range", 256);
    public static final ConfigSpec.IntValue SULFUR_RARITY = rarity("Sulfur Cloud Spawn Rarity", 8);
    public static final ConfigSpec.IntValue SULFUR_HEIGHT = BUILDER.defineInRange("Sulfur Cloud Spawn Height", 40, -2048, 2048);
    public static final ConfigSpec.IntValue SULFUR_RANGE = rarity("Sulfur Cloud Spawn Range", 78);
    public static final ConfigSpec.IntValue ASH_RARITY = rarity("Ash Cloud Spawn Rarity", 8);
    public static final ConfigSpec.IntValue ASH_HEIGHT = BUILDER.defineInRange("Ash Cloud Spawn Height", 40, -2048, 2048);
    public static final ConfigSpec.IntValue ASH_RANGE = rarity("Ash Cloud Spawn Range", 78);
    public static final ConfigSpec.IntValue TAINTED_SOIL_CLUSTER_COUNT = rarity("Tainted Soil Cluster Count", 4);
    public static final ConfigSpec.IntValue HEAT_SAND_CLUSTER_COUNT = rarity("Heat Sand Cluster Count", 4);
    public static final ConfigSpec.IntValue TAINTED_SOIL_CLUSTER_SIZE = rarity("Tainted Soil Cluster Size", 33);
    public static final ConfigSpec.IntValue HEAT_SAND_CLUSTER_SIZE = rarity("Heat Sand Cluster Size", 33);
    public static final ConfigSpec.IntValue THORN_RARITY = rarity("Thornvines Spawn Rarity", 40);

    static {
        BUILDER.pop();
    }

    public static final ConfigSpec SPEC = BUILDER.build();

    private NaturaConfig() {}

    private static ConfigSpec.IntValue rarity(String name, int value) {
        return BUILDER.defineInRange(name, value, 0, Integer.MAX_VALUE);
    }

    /** The flat world sea level in 1.21 coordinates (1.12 counted it from y 0, the world bottom). */
    public static int flatSeaLevel(TreeWorld world) {
        return world.bottom() + FLAT_SEA_LEVEL.get();
    }
}
