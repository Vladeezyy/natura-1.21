package com.progwml6.natura.world.gen;

import java.util.Random;
import java.util.concurrent.atomic.AtomicInteger;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import com.progwml6.natura.common.NaturaBlocks;
import com.progwml6.natura.common.NaturaConfig;
import com.progwml6.natura.common.NaturaTrees;
import com.progwml6.natura.common.block.LargeGlowshroomBlock;
import com.progwml6.natura.common.block.NaturaCropBlock;
import com.progwml6.natura.world.tree.BloodwoodTreeGenerator;
import com.progwml6.natura.world.tree.DarkwoodTreeGenerator;
import com.progwml6.natura.world.tree.EucalyptusTreeGenerator;
import com.progwml6.natura.world.tree.FusewoodTreeGenerator;
import com.progwml6.natura.world.tree.GhostwoodTreeGenerator;
import com.progwml6.natura.world.tree.GlowshroomGenerator;
import com.progwml6.natura.world.tree.HopseedTreeGenerator;
import com.progwml6.natura.world.tree.OverworldTreeGenerator;
import com.progwml6.natura.world.tree.RedwoodTreeGenerator;
import com.progwml6.natura.world.tree.SaguaroGenerator;
import com.progwml6.natura.world.tree.SakuraTreeGenerator;
import com.progwml6.natura.world.tree.TreeWorld;
import com.progwml6.natura.world.tree.WillowTreeGenerator;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Holder;
import net.minecraft.tags.TagKey;
import net.minecraft.util.StringRepresentable;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.WorldGenLevel;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.VineBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.BooleanProperty;
import net.minecraft.world.level.levelgen.Heightmap;
import net.minecraft.world.level.levelgen.feature.Feature;
import com.mojang.serialization.MapCodec;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.chunk.ChunkGenerator;

/**
 * The 1.12 IWorldGenerators, one feature each (26.3: a feature record with its kind), run once per chunk with the 1.12 per-chunk logic: biome
 * dictionary types become the c: biome tags, climates the biome's base temperature and downfall, the dimension checks
 * the dimension type (ultrawarm for 1.12 doesWaterVaporize) or the End. Positions are taken in the chunk itself instead
 * of 1.12's chunk + 8 (1.21 features may only write one chunk around theirs; see TreeWorld).
 */
public record NaturaWorldFeature(NaturaWorldFeature.Kind kind) implements Feature {
    public static final MapCodec<NaturaWorldFeature> CODEC = RecordCodecBuilder.mapCodec(i -> i.group(
            Kind.CODEC.fieldOf("kind").forGetter(NaturaWorldFeature::kind)).apply(i, NaturaWorldFeature::new));

    /**
     * 1.12 kept one sakura and one ghostwood generator for the whole game, and their height limit was rolled once and
     * only lowered afterwards (validTreeLocation shortened it to what fitted); the shared value is kept.
     */
    static final AtomicInteger SAKURA_HEIGHT_LIMIT = new AtomicInteger();
    static final AtomicInteger GHOSTWOOD_HEIGHT_LIMIT = new AtomicInteger();

    public enum Kind implements StringRepresentable {
        OVERWORLD_TREES, NETHER_TREES, OVERWORLD_BERRY_BUSHES, NETHER_BERRY_BUSHES, CROPS, CLOUDS, GLOWSHROOMS, THORN_VINES, NETHER_MINABLES;

        public static final Codec<Kind> CODEC = StringRepresentable.fromEnum(Kind::values);

        @Override
        public String getSerializedName() {
            return this.name().toLowerCase(java.util.Locale.ROOT);
        }
    }

    @Override
    public MapCodec<? extends Feature> codec() {
        return CODEC;
    }

    @Override
    public boolean place(WorldGenLevel level, ChunkGenerator generator, RandomSource randomSource, BlockPos origin) {
        int xPos = origin.getX() & ~15;
        int zPos = origin.getZ() & ~15;
        Random random = new Random(randomSource.nextLong());
        int biomeY = level.getHeight(Heightmap.Types.WORLD_SURFACE_WG, xPos + 8, zPos + 8);
        Holder<Biome> biome = level.getBiome(new BlockPos(xPos + 8, biomeY, zPos + 8));
        TreeWorld world = new TreeWorld(level);
        switch (this.kind) {
            case OVERWORLD_TREES -> overworldTrees(random, xPos, zPos, world, biome);
            case NETHER_TREES -> netherTrees(random, xPos, zPos, world, biome);
            case OVERWORLD_BERRY_BUSHES -> overworldBerryBushes(random, xPos, zPos, world, biome);
            case NETHER_BERRY_BUSHES -> netherBerryBushes(random, xPos, zPos, world, level);
            case CROPS -> crops(random, xPos, zPos, world, level, biome);
            case CLOUDS -> clouds(random, xPos, zPos, world, level, biome);
            case GLOWSHROOMS -> glowshrooms(random, xPos, zPos, world, level, biome);
            case THORN_VINES -> thornVines(random, xPos, zPos, world, biome);
            case NETHER_MINABLES -> netherMinables(random, xPos, zPos, world, level, biome);
        }
        world.finish();
        return true;
    }

    private static boolean is(Holder<Biome> biome, TagKey<Biome> tag) {
        return biome.is(tag);
    }

    /** 1.12 goodClimate: biome.getDefaultTemperature() and getRainfall() in the given ranges. */
    static boolean goodClimate(Holder<Biome> biome, float minTemp, float maxTemp, float minRain, float maxRain) {
        float temp = biome.value().getBaseTemperature();
        float rain = com.progwml6.natura.platform.Services.PLATFORM.downfall(biome.value());
        return minTemp <= temp && temp <= maxTemp && minRain <= rain && rain <= maxRain;
    }

    private static BlockPos at(Random random, int xPos, int y, int zPos) {
        int x = xPos + random.nextInt(16);
        int z = zPos + random.nextInt(16);
        return new BlockPos(x, y, z);
    }

    private static BlockPos at(Random random, int xPos, java.util.function.IntSupplier y, int zPos) {
        int x = xPos + random.nextInt(16);
        int yy = y.getAsInt();
        int z = zPos + random.nextInt(16);
        return new BlockPos(x, yy, z);
    }

    private static int sea() {
        return NaturaConfig.SEA_LEVEL.get();
    }

    // 1.12 OverworldTreesGenerator
    private static void overworldTrees(Random random, int xPos, int zPos, TreeWorld world, Holder<Biome> biome) {
        if (is(biome, NaturaBiomeTags.IS_FOREST)) {
            if (NaturaConfig.GENERATE_SAKURA.get() && random.nextInt(NaturaConfig.SAKURA_RARITY.get() * 5) == 0) {
                for (int iter = 0; iter < 3; iter++) {
                    sakura(random, world, at(random, xPos, () -> random.nextInt(NaturaConfig.SAKURA_RANGE.get()) + sea(), zPos));
                }
            }
            if (NaturaConfig.GENERATE_EUCALYPTUS.get() && random.nextInt(NaturaConfig.EUCALYPTUS_RARITY.get()) == 0) {
                eucalyptus(random, world, at(random, xPos, () -> random.nextInt(NaturaConfig.EUCALYPTUS_RANGE.get()) + sea(), zPos));
            }
        }
        if (is(biome, NaturaBiomeTags.IS_PLAINS)) {
            if (NaturaConfig.GENERATE_REDWOOD.get() && random.nextInt(NaturaConfig.REDWOOD_RARITY.get()) == 0) {
                new RedwoodTreeGenerator(NaturaTrees.log("redwood_bark"), NaturaTrees.log("redwood_heart"), NaturaTrees.log("redwood_root"),
                        NaturaTrees.leaves("redwood_leaves"), false, false).generateTree(random, world, at(random, xPos, sea() + 16, zPos));
            }
            if (NaturaConfig.GENERATE_EUCALYPTUS.get() && random.nextInt((int) (NaturaConfig.EUCALYPTUS_RARITY.get() * 1.5)) == 0) {
                eucalyptus(random, world, at(random, xPos, () -> random.nextInt(NaturaConfig.EUCALYPTUS_RANGE.get()) + sea(), zPos));
            }
        }
        if (is(biome, NaturaBiomeTags.IS_MOUNTAIN) || is(biome, NaturaBiomeTags.IS_HILL)) {
            if (NaturaConfig.GENERATE_HOPSEED.get() && random.nextInt(NaturaConfig.HOPSEED_RARITY.get()) == 0) {
                new HopseedTreeGenerator(2, 3, NaturaTrees.log("hopseed_log"), NaturaTrees.leaves("hopseed_leaves"), true, false)
                        .generateTree(random, world, at(random, xPos, () -> random.nextInt(NaturaConfig.HOPSEED_RANGE.get()) + sea(), zPos));
            }
            // 1.12: "< 10", so nearly every hill chunk with the default rarity
            if (NaturaConfig.GENERATE_EUCALYPTUS.get() && random.nextInt(NaturaConfig.EUCALYPTUS_RARITY.get()) < 10) {
                eucalyptus(random, world, at(random, xPos, () -> random.nextInt(NaturaConfig.EUCALYPTUS_RANGE.get()) + sea(), zPos));
            }
        }
        if (is(biome, NaturaBiomeTags.IS_RIVER)) {
            if (NaturaConfig.GENERATE_SAKURA.get() && random.nextInt(NaturaConfig.SAKURA_RARITY.get()) == 0) {
                for (int iter = 0; iter < 3; iter++) {
                    sakura(random, world, at(random, xPos, () -> random.nextInt(NaturaConfig.SAKURA_RANGE.get()) + sea(), zPos));
                }
            }
            if (NaturaConfig.GENERATE_WILLOW.get() && random.nextInt(NaturaConfig.WILLOW_RARITY.get()) == 0) {
                willow(random, world, at(random, xPos, sea() + 16, zPos));
            }
        }
        if (is(biome, NaturaBiomeTags.IS_JUNGLE)) {
            if (NaturaConfig.GENERATE_AMARANTH.get() && random.nextInt(NaturaConfig.AMARANTH_RARITY.get()) == 0) {
                overworld(9, 8, "amaranth", random, world, at(random, xPos, sea() + 48, zPos));
            }
        }
        if (is(biome, NaturaBiomeTags.IS_FOREST)) {
            if (NaturaConfig.GENERATE_MAPLE.get() && random.nextInt(NaturaConfig.MAPLE_RARITY.get()) == 0) {
                overworld(4, 2, "maple", random, world, at(random, xPos, sea() + 48, zPos));
            }
            if (NaturaConfig.GENERATE_SILVERBELL.get() && random.nextInt(NaturaConfig.SILVERBELL_RARITY.get()) == 0) {
                overworld(4, 2, "silverbell", random, world, at(random, xPos, sea() + 48, zPos));
            }
            if (NaturaConfig.GENERATE_TIGER.get() && random.nextInt(NaturaConfig.TIGER_RARITY.get()) == 0) {
                overworld(6, 4, "tiger", random, world, at(random, xPos, sea() + 48, zPos));
            }
        }
        if (is(biome, NaturaBiomeTags.IS_SWAMP)) {
            if (NaturaConfig.GENERATE_WILLOW.get() && random.nextInt(NaturaConfig.WILLOW_RARITY.get()) == 0) {
                for (int i = 0; i < 3; i++) {
                    willow(random, world, at(random, xPos, sea() + 16, zPos));
                }
            }
        }
        // 1.12 SANDY was the deserts and mesas, not the beaches
        if (is(biome, NaturaBiomeTags.IS_SANDY) && !is(biome, NaturaBiomeTags.IS_BEACH)) {
            if (NaturaConfig.GENERATE_SAGUARO.get() && random.nextInt(NaturaConfig.SAGUARO_RARITY.get()) == 0) {
                new SaguaroGenerator(NaturaBlocks.SAGUARO.get().defaultBlockState(), true, false)
                        .generateSaguaro(random, world, at(random, xPos, () -> sea() + random.nextInt(16), zPos));
            }
        }
    }

    private static void overworld(int height, int range, String wood, Random random, TreeWorld world, BlockPos pos) {
        new OverworldTreeGenerator(height, range, NaturaTrees.log(wood + "_log"), NaturaTrees.leaves(wood + "_leaves"), true, false).generateTree(random, world, pos);
    }

    private static void willow(Random random, TreeWorld world, BlockPos pos) {
        new WillowTreeGenerator(4, 5, NaturaTrees.log("willow_log"), NaturaTrees.leaves("willow_leaves"), true, false).generateTree(random, world, pos);
    }

    private static void eucalyptus(Random random, TreeWorld world, BlockPos pos) {
        new EucalyptusTreeGenerator(6, 3, NaturaTrees.log("eucalyptus_log"), NaturaTrees.leaves("eucalyptus_leaves"), true).generateTree(random, world, pos);
    }

    private static void sakura(Random random, TreeWorld world, BlockPos pos) {
        new SakuraTreeGenerator(NaturaTrees.log("sakura_log"), NaturaTrees.leaves("sakura_leaves"), true, false).shareHeightLimit(SAKURA_HEIGHT_LIMIT)
                .generateTree(random, world, pos);
    }

    // 1.12 NetherTreesGenerator
    private static void netherTrees(Random random, int xPos, int zPos, TreeWorld world, Holder<Biome> biome) {
        if (!is(biome, NaturaBiomeTags.IS_NETHER)) {
            return;
        }
        if (NaturaConfig.GENERATE_BLOODWOOD.get() && random.nextInt(NaturaConfig.BLOODWOOD_RARITY.get()) == 0) {
            new BloodwoodTreeGenerator(NaturaTrees.log("bloodwood_bark"), NaturaTrees.bloodwood(0), NaturaTrees.bloodwood(1), NaturaTrees.bloodwood(2),
                    NaturaTrees.bloodwood(3), NaturaTrees.leaves("bloodwood_leaves")).generateTree(random, world, at(random, xPos, world.bottom() + 72, zPos));
        }
        if (NaturaConfig.GENERATE_DARKWOOD.get() && random.nextInt(NaturaConfig.DARKWOOD_RARITY.get()) == 0) {
            new DarkwoodTreeGenerator(3, NaturaTrees.log("darkwood_log"), NaturaTrees.leaves("darkwood_leaves"), NaturaTrees.leaves("darkwood_flowering_leaves"),
                    NaturaTrees.leaves("darkwood_fruit_leaves"), true).generateTree(random, world, at(random, xPos, () -> world.bottom() + random.nextInt(64) + 32, zPos));
        }
        if (NaturaConfig.GENERATE_FUSEWOOD.get() && random.nextInt(NaturaConfig.FUSEWOOD_RARITY.get()) == 0) {
            new FusewoodTreeGenerator(3, NaturaTrees.log("fusewood_log"), NaturaTrees.leaves("fusewood_leaves"), true)
                    .generateTree(random, world, at(random, xPos, () -> world.bottom() + random.nextInt(64) + 32, zPos));
        }
        if (NaturaConfig.GENERATE_GHOSTWOOD.get() && random.nextInt(NaturaConfig.GHOSTWOOD_RARITY.get()) == 0) {
            for (int iter = 0; iter < 3; iter++) {
                BlockPos pos = at(random, xPos, () -> world.bottom() + random.nextInt(80) + 16, zPos);
                ((GhostwoodTreeGenerator) new GhostwoodTreeGenerator(NaturaTrees.log("ghostwood_log"), NaturaTrees.leaves("ghostwood_leaves"), true)
                        .shareHeightLimit(GHOSTWOOD_HEIGHT_LIMIT)).generateTree(random, world, pos);
            }
        }
    }

    private static BlockState bush(String id) {
        return NaturaBlocks.BUSHES.get(id).get().defaultBlockState();
    }

    // 1.12 OverworldBerryBushesGenerator
    private static void overworldBerryBushes(Random random, int xPos, int zPos, TreeWorld world, Holder<Biome> biome) {
        if (NaturaConfig.GENERATE_RASPBERRIES.get() && random.nextInt(NaturaConfig.RASPBERRY_RARITY.get()) == 0 && goodClimate(biome, 0.6f, 2.0f, 0.2f, 0.93f)) {
            overworldBush("raspberry", NaturaConfig.RASPBERRY_RANGE.get(), random, xPos, zPos, world);
        }
        if (NaturaConfig.GENERATE_BLUEBERRIES.get() && random.nextInt(NaturaConfig.BLUEBERRY_RARITY.get()) == 0 && goodClimate(biome, 0.3f, 0.81f, 0.3f, 0.8f)) {
            overworldBush("blueberry", NaturaConfig.BLUEBERRY_RANGE.get(), random, xPos, zPos, world);
        }
        if (NaturaConfig.GENERATE_BLACKBERRIES.get() && random.nextInt(NaturaConfig.BLACKBERRY_RARITY.get()) == 0 && goodClimate(biome, 0.5f, 5.0f, 0.6f, 3.0f)) {
            overworldBush("blackberry", NaturaConfig.BLACKBERRY_RANGE.get(), random, xPos, zPos, world);
        }
        if (NaturaConfig.GENERATE_BLACKBERRIES.get() && random.nextInt(NaturaConfig.BLACKBERRY_RARITY.get() / 3) == 0 && is(biome, NaturaBiomeTags.IS_SWAMP)) {
            overworldBush("blackberry", NaturaConfig.BLACKBERRY_RANGE.get(), random, xPos, zPos, world);
        }
        if (NaturaConfig.GENERATE_MALOBERRIES.get() && random.nextInt(NaturaConfig.MALOBERRY_RARITY.get()) == 0 && goodClimate(biome, 0.0f, 0.3f, 0.0f, 5.0f)) {
            overworldBush("maloberry", NaturaConfig.MALOBERRY_RANGE.get(), random, xPos, zPos, world);
        }
    }

    private static void overworldBush(String berry, int range, Random random, int xPos, int zPos, TreeWorld world) {
        BlockPos pos = at(random, xPos, () -> random.nextInt(range) + sea(), zPos);
        new BerryBushGenerator(sea() + range, bush(berry + "_bush"), false).generateBush(random, world, pos);
    }

    // 1.12 NetherBerryBushesGenerator
    private static void netherBerryBushes(Random random, int xPos, int zPos, TreeWorld world, WorldGenLevel level) {
        if (!waterEvaporates(level)) {
            return;
        }
        netherBush(NaturaConfig.GENERATE_BLIGHTBERRIES.get(), NaturaConfig.BLIGHTBERRY_RARITY.get(), NaturaConfig.BLIGHTBERRY_RANGE.get(), "blightberry", random, xPos, zPos, world);
        netherBush(NaturaConfig.GENERATE_DUSKBERRIES.get(), NaturaConfig.DUSKBERRY_RARITY.get(), NaturaConfig.DUSKBERRY_RANGE.get(), "duskberry", random, xPos, zPos, world);
        netherBush(NaturaConfig.GENERATE_SKYBERRIES.get(), NaturaConfig.SKYBERRY_RARITY.get(), NaturaConfig.SKYBERRY_RANGE.get(), "skyberry", random, xPos, zPos, world);
        netherBush(NaturaConfig.GENERATE_STINGBERRIES.get(), NaturaConfig.STINGBERRY_RARITY.get(), NaturaConfig.STINGBERRY_RANGE.get(), "stingberry", random, xPos, zPos, world);
    }

    private static void netherBush(boolean enabled, int rarity, int range, String berry, Random random, int xPos, int zPos, TreeWorld world) {
        if (enabled && random.nextInt(rarity) == 0) {
            BlockPos pos = at(random, xPos, () -> world.bottom() + random.nextInt(range) + 16, zPos);
            new BerryBushGenerator(0, bush(berry + "_bush"), true).generateBush(random, world, pos);
        }
    }

    // 1.12 CropGenerator
    private static void crops(Random random, int xPos, int zPos, TreeWorld world, WorldGenLevel level, Holder<Biome> biome) {
        if (NaturaConfig.GENERATE_BARLEY.get() && random.nextInt(5) == 0 && goodClimate(biome, 0.11f, 1.0f, 0.11f, 2f)) {
            BlockPos pos = at(random, xPos, () -> random.nextInt(128) + sea(), zPos);
            BlockState state = NaturaBlocks.BARLEY_CROP.get().defaultBlockState().setValue(NaturaCropBlock.AGE_3, 3);
            patch(random, world, level, pos, state, 64, 4);
            patch(random, world, level, pos, state, 64, 4);
        }
        if (NaturaConfig.GENERATE_COTTON.get() && random.nextInt(12) == 0 && goodClimate(biome, 0.11f, 1.0f, 0.11f, 2f)) {
            // 1.12 rolled z before y here
            int x = xPos + random.nextInt(16);
            int z = zPos + random.nextInt(16);
            int y = random.nextInt(128) + sea();
            BlockState state = NaturaBlocks.COTTON_CROP.get().defaultBlockState().setValue(NaturaCropBlock.AGE_4, 4);
            patch(random, world, level, new BlockPos(x, y, z), state, 64, 4);
            patch(random, world, level, new BlockPos(x, y, z), state, 64, 4);
        }
        if (NaturaConfig.GENERATE_BLUEBELLS.get() && random.nextInt(12) == 0) {
            int x = xPos + random.nextInt(16);
            int z = zPos + random.nextInt(16);
            int y = random.nextInt(128) + sea();
            patch(random, world, level, new BlockPos(x, y, z), NaturaBlocks.BLUEBELLS.get().defaultBlockState(), 40, 8);
        }
    }

    /**
     * 1.12 generateBarley / generateCotton / generateBluebells: tries around the spot, into air where the plant can stay.
     * The crops' light check (light 8 or sky) is the sky through the heightmap: no light is known yet while generating.
     */
    private static void patch(Random random, TreeWorld world, WorldGenLevel level, BlockPos position, BlockState state, int tries, int ySpread) {
        boolean crop = state.getBlock() instanceof NaturaCropBlock;
        for (int i = 0; i < tries; i++) {
            BlockPos pos = position.offset(random.nextInt(8) - random.nextInt(8), random.nextInt(ySpread) - random.nextInt(ySpread), random.nextInt(8) - random.nextInt(8));
            if (!world.isAirBlock(pos)) {
                continue;
            }
            boolean stays;
            if (crop) {
                BlockState soil = world.getBlockState(pos.below());
                stays = (soil.is(Blocks.FARMLAND) || TreeWorld.isDirtOrGrass(soil))
                        && pos.getY() >= level.getHeight(Heightmap.Types.WORLD_SURFACE_WG, pos.getX(), pos.getZ());
            } else {
                stays = state.canSurvive(level, pos);
            }
            if (stays) {
                world.setBlockState(pos, state);
            }
        }
    }

    private static BlockState cloud(String type) {
        return NaturaBlocks.CLOUDS.get(type).get().defaultBlockState();
    }

    // 1.12 CloudGenerator (its generators only existed with "Enable Clouds")
    private static void clouds(Random random, int xPos, int zPos, TreeWorld world, WorldGenLevel level, Holder<Biome> biome) {
        if (!NaturaConfig.ENABLE_CLOUD_BLOCKS.get()) {
            return;
        }
        boolean end = level.getLevel().dimension() == Level.END;
        if (NaturaConfig.GENERATE_OVERWORLD_CLOUDS.get() && com.progwml6.natura.platform.Services.PLATFORM.downfall(biome.value()) > 0.15f
                && random.nextInt(NaturaConfig.CLOUD_RARITY.get()) == 0 && !end) {
            BlockPos pos = at(random, xPos, () -> random.nextInt(NaturaConfig.CLOUD_RANGE.get()) + NaturaConfig.CLOUD_HEIGHT.get(), zPos);
            int size = random.nextInt(12);
            int blocks = size < 5 ? 10 : size < 9 ? 20 : size < 11 ? 30 : 40;
            new CloudsGenerator(blocks, cloud("white")).generateCloud(random, world, pos);
        }
        if (waterEvaporates(level)) {
            if (NaturaConfig.GENERATE_ASH_CLOUDS.get() && random.nextInt(NaturaConfig.ASH_RARITY.get()) == 0) {
                BlockPos pos = at(random, xPos, () -> random.nextInt(NaturaConfig.ASH_RANGE.get()) + NaturaConfig.ASH_HEIGHT.get(), zPos);
                int size = random.nextInt(12);
                // 1.12 used the small ash cloud where the medium one belonged
                int blocks = size < 2.5 ? 3 : size < 9 ? 10 : size < 11 ? 27 : 37;
                new CloudsGenerator(blocks, cloud("ash")).generateCloud(random, world, pos);
            }
            if (NaturaConfig.GENERATE_SULFUR_CLOUDS.get() && random.nextInt(NaturaConfig.SULFUR_RARITY.get()) == 0) {
                BlockPos pos = at(random, xPos, () -> random.nextInt(NaturaConfig.SULFUR_RANGE.get()) + NaturaConfig.SULFUR_HEIGHT.get(), zPos);
                int size = random.nextInt(12);
                int blocks = size < 2.5 ? 3 : size < 5 ? 10 : size < 9 ? 18 : size < 11 ? 27 : 37;
                new CloudsGenerator(blocks, cloud("sulfur")).generateCloud(random, world, pos);
            }
        }
        // 1.12: Biomes.SKY in dimension 1, the whole End then
        if (NaturaConfig.GENERATE_DARK_CLOUDS.get() && end && is(biome, NaturaBiomeTags.IS_END) && random.nextInt(4) == 0) {
            int x = xPos + random.nextInt(16);
            int z = zPos + random.nextInt(16);
            CloudsGenerator dark = new CloudsGenerator(10, cloud("dark"));
            for (int iter = 0; iter < NaturaConfig.DARK_CLOUD_RARITY.get(); iter++) {
                int height = random.nextInt(NaturaConfig.DARK_CLOUD_RANGE.get());
                BlockPos pos = new BlockPos(x, height + NaturaConfig.DARK_CLOUD_HEIGHT.get(), z);
                // 1.12 made all four dark cloud sizes 10 blocks
                if (random.nextInt(5) == 0 || random.nextInt(7) == 0 || random.nextInt(9) == 0 || random.nextInt(12) == 0) {
                    dark.generateCloud(random, world, pos);
                }
            }
        }
    }

    // 1.12 GlowshroomGenerator (world)
    private static void glowshrooms(Random random, int xPos, int zPos, TreeWorld world, WorldGenLevel level, Holder<Biome> biome) {
        if (!is(biome, NaturaBiomeTags.IS_NETHER)) {
            return;
        }
        if (NaturaConfig.GENERATE_GLOWSHROOM_TREE.get() && random.nextInt(35) == 0) {
            for (int iter = 0; iter < 5; iter++) {
                int x = xPos + random.nextInt(24) - 4;
                int z = zPos + random.nextInt(24) - 4;
                int y = findShroomGround(world, x, world.bottom() + random.nextInt(64) + 32, z);
                BlockPos pos = new BlockPos(x, y, z);
                String color = random.nextInt(3) == 0 ? "purple" : random.nextInt(2) == 0 ? "green" : "blue";
                LargeGlowshroomBlock large = NaturaBlocks.LARGE_GLOWSHROOMS.get(color).get();
                new GlowshroomGenerator(color, large.defaultBlockState()).generateShroom(random, world, pos);
            }
        }
        if (NaturaConfig.GENERATE_GREEN_GLOWSHROOM.get() && random.nextInt(7) == 0) {
            babyShroom("green", random, world, level, at(random, xPos, () -> world.bottom() + random.nextInt(128), zPos));
        }
        if (NaturaConfig.GENERATE_PURPLE_GLOWSHROOM.get() && random.nextInt(8) == 0) {
            babyShroom("purple", random, world, level, at(random, xPos, () -> world.bottom() + random.nextInt(128), zPos));
        }
        if (NaturaConfig.GENERATE_BLUE_GLOWSHROOM.get() && random.nextInt(9) == 0) {
            babyShroom("blue", random, world, level, at(random, xPos, () -> world.bottom() + random.nextInt(128), zPos));
        }
    }

    private static int findShroomGround(TreeWorld world, int x, int y, int z) {
        int height = y;
        while (true) {
            height--;
            BlockState state = world.getBlockState(new BlockPos(x, height, z));
            if (state.is(Blocks.NETHERRACK) || state.is(Blocks.SOUL_SAND) || state.is(NaturaBlocks.NETHER.get("tainted_soil").get()) || height < world.bottom()) {
                return height + 1;
            }
        }
    }

    /** 1.12 BabyGlowshroomGenerator: 64 tries into air where it can stay (below the top in a dimension with sky light). */
    private static void babyShroom(String color, Random random, TreeWorld world, WorldGenLevel level, BlockPos pos) {
        BlockState shroom = NaturaBlocks.GLOWSHROOMS.get(color).get().defaultBlockState();
        for (int i = 0; i < 64; ++i) {
            BlockPos blockpos = pos.offset(random.nextInt(8) - random.nextInt(8), random.nextInt(4) - random.nextInt(4), random.nextInt(8) - random.nextInt(8));
            if (world.isAirBlock(blockpos) && (!level.dimensionType().hasSkyLight() || blockpos.getY() < world.top() - 1) && shroom.canSurvive(level, blockpos)) {
                world.setBlockState(blockpos, shroom);
            }
        }
    }

    private static final BooleanProperty[] SIDES = {VineBlock.NORTH, VineBlock.EAST, VineBlock.SOUTH, VineBlock.WEST};

    // 1.12 VineGenerator / ThornvinesGenerator
    private static void thornVines(Random random, int xPos, int zPos, TreeWorld world, Holder<Biome> biome) {
        if (!is(biome, NaturaBiomeTags.IS_NETHER) || !NaturaConfig.GENERATE_THORNVINES.get() || random.nextInt(NaturaConfig.THORN_RARITY.get()) != 0) {
            return;
        }
        int ySpawn = world.bottom() + 108;
        for (int i = 0; i < 20; i++) {
            BlockState vine = NaturaBlocks.NETHER.get("thorn_vines").get().defaultBlockState();
            for (BooleanProperty side : SIDES) {
                vine = vine.setValue(side, false);
            }
            for (int s = random.nextInt(3) + 1; s > 0; s--) {
                vine = vine.setValue(SIDES[random.nextInt(SIDES.length)], true);
            }
            int x = xPos + random.nextInt(16);
            int z = zPos + random.nextInt(16);
            int size = random.nextInt(25) + 1;
            int height = ySpawn - (random.nextInt(size) + random.nextInt(size) + random.nextInt(size));
            for (int y = ySpawn; y > height; y--) {
                BlockPos pos = new BlockPos(x, y, z);
                if (world.getBlockState(pos).is(Blocks.AIR)) {
                    world.setBlockState(pos, vine);
                }
            }
        }
    }

    // 1.12 NetherMinableGenerator: the vanilla vein, at the 1.12 positions (chunk corner, the vein adds 8)
    private static void netherMinables(Random random, int xPos, int zPos, TreeWorld world, WorldGenLevel level, Holder<Biome> biome) {
        if (!is(biome, NaturaBiomeTags.IS_NETHER)) {
            return;
        }
        // 1.12 world.getSeaLevel() / 2 + 1 - 5 with the nether's sea level of 63 then (32 now)
        int yPos = world.bottom() + 63 / 2 + 1 - 5;
        if (NaturaConfig.GENERATE_TAINTED_SOIL.get()) {
            MinableGenerator soil = new MinableGenerator(NaturaBlocks.NETHER.get("tainted_soil").get().defaultBlockState(),
                    NaturaConfig.TAINTED_SOIL_CLUSTER_SIZE.get(), s -> s.is(Blocks.NETHERRACK));
            for (int i = 0; i < NaturaConfig.TAINTED_SOIL_CLUSTER_COUNT.get(); i++) {
                soil.generate(world, random, at(random, xPos, () -> yPos + random.nextInt(10), zPos));
            }
        }
        if (NaturaConfig.GENERATE_HEAT_SAND.get()) {
            MinableGenerator sand = new MinableGenerator(NaturaBlocks.NETHER.get("heat_sand").get().defaultBlockState(),
                    NaturaConfig.HEAT_SAND_CLUSTER_SIZE.get(), s -> s.is(Blocks.SOUL_SAND));
            for (int i = 0; i < NaturaConfig.HEAT_SAND_CLUSTER_COUNT.get(); i++) {
                sand.generate(world, random, at(random, xPos, () -> yPos + random.nextInt(10), zPos));
            }
        }
    }

    /** 1.12 doesWaterVaporize (the nether): the dimension's water_evaporates attribute (26.x; was DimensionType.ultraWarm). */
    private static boolean waterEvaporates(WorldGenLevel level) {
        return level.dimensionType().attributes().applyModifier(net.minecraft.world.attribute.EnvironmentAttributes.WATER_EVAPORATES, false);
    }
}
