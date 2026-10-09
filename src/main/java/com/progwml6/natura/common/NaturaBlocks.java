package com.progwml6.natura.common;

import java.util.ArrayList;
import java.util.EnumMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.function.Supplier;

import com.progwml6.natura.Natura;
import com.progwml6.natura.common.block.BlazeRailBlocks;
import com.progwml6.natura.common.block.CloudBlock;
import com.progwml6.natura.common.block.HeatSandBlock;
import com.progwml6.natura.common.block.LargeGlowshroomBlock;
import com.progwml6.natura.common.block.NaturaGlowshroomBlock;
import com.progwml6.natura.common.block.NetherGlassBlock;
import com.progwml6.natura.common.block.NetherrackFurnaceBlock;
import com.progwml6.natura.common.block.NetherrackFurnaceBlockEntity;
import com.progwml6.natura.common.block.RespawnObeliskBlock;
import com.progwml6.natura.common.block.TaintedSoilBlock;
import com.progwml6.natura.common.block.ThornVinesBlock;
import net.minecraft.world.level.block.HopperBlock;
import net.minecraft.world.level.block.LeverBlock;
import net.minecraft.world.level.block.entity.BlockEntityType;
import com.progwml6.natura.common.block.BloodwoodLogBlock;
import com.progwml6.natura.common.block.NaturaBerryBushBlock;
import com.progwml6.natura.common.block.NaturaCropBlock;
import com.progwml6.natura.common.block.NaturaPlantBlock;
import com.progwml6.natura.common.block.SaguaroBabyBlock;
import com.progwml6.natura.common.block.SaguaroBlock;
import com.progwml6.natura.common.block.SaguaroFruitBlock;
import com.progwml6.natura.common.block.FusewoodLogBlock;
import com.progwml6.natura.common.block.NaturaLeavesBlock;
import com.progwml6.natura.common.block.NaturaLogBlock;
import com.progwml6.natura.common.block.NaturaPlainLogBlock;
import com.progwml6.natura.common.block.NaturaSaplingBlock;
import com.progwml6.natura.common.block.NaturaWorkbenchBlock;
import com.progwml6.natura.common.block.RedwoodLeavesBlock;
import com.progwml6.natura.common.item.NaturaBlockItem;
import com.progwml6.natura.common.item.NaturaItem;
import com.progwml6.natura.library.NaturaWood;

import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.TagKey;
import net.minecraft.world.entity.EntityType;
import com.progwml6.natura.common.item.NaturaDoorItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.ButtonBlock;
import net.minecraft.world.level.block.DoorBlock;
import net.minecraft.world.level.block.FenceBlock;
import net.minecraft.world.level.block.FenceGateBlock;
import net.minecraft.world.level.block.FireBlock;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.PressurePlateBlock;
import net.minecraft.world.level.block.SlabBlock;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.block.StairBlock;
import net.minecraft.world.level.block.TrapDoorBlock;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.properties.BlockSetType;
import net.minecraft.world.level.block.state.properties.NoteBlockInstrument;
import net.minecraft.world.level.block.state.properties.WoodType;
import net.minecraft.world.level.material.MapColor;
import net.minecraft.world.level.material.PushReaction;
import net.neoforged.neoforge.registries.DeferredBlock;
import net.neoforged.neoforge.registries.DeferredItem;
import net.neoforged.neoforge.registries.DeferredRegister;

/**
 * Blocks and their items. 1.12 metadata variants are flattened into one block per variant (see PORTLOG.md, "Names").
 * Strength values are the effective 1.12 ones: 1.12 kept resistance x3 and divided by 5, and setHardness raised it to
 * hardness x5, so e.g. planks (hardness 2, no resistance set) resist 2.0, not vanilla's 3.0.
 */
public final class NaturaBlocks {
    public static final DeferredRegister.Blocks BLOCKS = DeferredRegister.createBlocks(Natura.MODID);
    public static final DeferredRegister.Items ITEMS = DeferredRegister.createItems(Natura.MODID);

    /** Creative tab contents in 1.12 registration order. */
    public static final List<DeferredItem<? extends Item>> TAB_GENERAL = new ArrayList<>();
    public static final List<DeferredItem<? extends Item>> TAB_WORLD = new ArrayList<>();
    public static final List<DeferredItem<? extends Item>> TAB_DECORATIVE = new ArrayList<>();

    /** Fire spread (1.12 setFireInfo: encouragement, flammability), applied in common setup. */
    private static final Map<DeferredBlock<? extends Block>, int[]> FLAMMABLE = new LinkedHashMap<>();

    public static final TagKey<Block> NETHER_SAPLING_SOIL = TagKey.create(Registries.BLOCK, ResourceLocation.fromNamespaceAndPath(Natura.MODID, "nether_sapling_soil"));

    public record WoodSet(DeferredBlock<Block> planks, DeferredBlock<SlabBlock> slab, DeferredBlock<StairBlock> stairs,
                          DeferredBlock<FenceBlock> fence, DeferredBlock<FenceGateBlock> fenceGate, DeferredBlock<TrapDoorBlock> trapdoor,
                          DeferredBlock<ButtonBlock> button, DeferredBlock<PressurePlateBlock> pressurePlate, DeferredBlock<Block> bookshelf,
                          DeferredBlock<NaturaWorkbenchBlock> workbench, DeferredItem<Item> stick) {}

    public static final Map<NaturaWood, WoodSet> WOODS = new EnumMap<>(NaturaWood.class);
    /** Logs (and the axis-less redwood / bloodwood pieces), leaves and saplings by their new id. */
    public static final Map<String, DeferredBlock<? extends Block>> LOGS = new LinkedHashMap<>();
    public static final Map<String, DeferredBlock<? extends Block>> LEAVES = new LinkedHashMap<>();
    public static final Map<String, DeferredBlock<NaturaSaplingBlock>> SAPLINGS = new LinkedHashMap<>();
    /** Doors by their new id (no amaranth or willow door in 1.12, two for redwood). */
    public static final Map<String, DeferredBlock<DoorBlock>> DOORS = new LinkedHashMap<>();

    // NaturaNether blocks and NaturaCommons clouds (phase 4)
    public static final Map<String, DeferredBlock<? extends Block>> NETHER = new LinkedHashMap<>();
    public static final Map<String, DeferredBlock<NaturaGlowshroomBlock>> GLOWSHROOMS = new LinkedHashMap<>();
    public static final Map<String, DeferredBlock<LargeGlowshroomBlock>> LARGE_GLOWSHROOMS = new LinkedHashMap<>();
    public static final Map<String, DeferredBlock<CloudBlock>> CLOUDS = new LinkedHashMap<>();
    public static DeferredBlock<NetherrackFurnaceBlock> NETHERRACK_FURNACE;
    public static DeferredBlock<HopperBlock> BLAZE_HOPPER;
    public static final DeferredRegister<BlockEntityType<?>> BLOCK_ENTITIES = DeferredRegister.create(Registries.BLOCK_ENTITY_TYPE, Natura.MODID);
    public static final Supplier<BlockEntityType<NetherrackFurnaceBlockEntity>> NETHERRACK_FURNACE_BE = BLOCK_ENTITIES.register("netherrack_furnace",
            () -> BlockEntityType.Builder.of(NetherrackFurnaceBlockEntity::new, NETHERRACK_FURNACE.get()).build(null));
    /** 1.12 netherrack button: a stone button (20 ticks, no arrows) with the wooden click sounds. */
    private static final BlockSetType NETHERRACK = new BlockSetType("natura_netherrack", true, true, false, BlockSetType.PressurePlateSensitivity.MOBS,
            SoundType.WOOD, net.minecraft.sounds.SoundEvents.IRON_DOOR_CLOSE, net.minecraft.sounds.SoundEvents.IRON_DOOR_OPEN,
            net.minecraft.sounds.SoundEvents.IRON_TRAPDOOR_CLOSE, net.minecraft.sounds.SoundEvents.IRON_TRAPDOOR_OPEN,
            net.minecraft.sounds.SoundEvents.STONE_PRESSURE_PLATE_CLICK_OFF, net.minecraft.sounds.SoundEvents.STONE_PRESSURE_PLATE_CLICK_ON,
            net.minecraft.sounds.SoundEvents.WOODEN_BUTTON_CLICK_OFF, net.minecraft.sounds.SoundEvents.WOODEN_BUTTON_CLICK_ON);


    // NaturaOverworld plants (phase 3)
    public static DeferredBlock<Block> COLORED_GRASS_TOPIARY, COLORED_GRASS_BLUEGRASS, COLORED_GRASS_AUTUMNAL;
    public static final Map<String, DeferredBlock<? extends Block>> COLORED_GRASS = new LinkedHashMap<>();
    public static final Map<String, DeferredBlock<NaturaBerryBushBlock>> BUSHES = new LinkedHashMap<>();
    public static DeferredBlock<NaturaPlantBlock> BLUEBELLS;
    public static DeferredBlock<NaturaCropBlock> BARLEY_CROP, COTTON_CROP;
    public static DeferredBlock<SaguaroBlock> SAGUARO;
    public static DeferredBlock<SaguaroBabyBlock> SAGUARO_BABY;
    public static DeferredBlock<SaguaroFruitBlock> SAGUARO_FRUIT;


    static {
        // NaturaCommons: clouds
        for (String cloud : new String[] {"white", "dark", "ash", "sulfur"}) {
            CLOUDS.put(cloud, block(cloud + "_cloud", () -> new CloudBlock(cloud.equals("sulfur"), BlockBehaviour.Properties.of()
                    .mapColor(MapColor.SNOW).strength(0.3F).sound(SoundType.WOOL).noOcclusion().dynamicShape().isSuffocating((s, l, p) -> false)
                    .isViewBlocking((s, l, p) -> false).isRedstoneConductor((s, l, p) -> false).pushReaction(PushReaction.BLOCK)), TAB_WORLD));
        }

        // NaturaOverworld: logs, leaves, saplings, planks, slabs, stairs, doors
        for (NaturaWood wood : new NaturaWood[] {NaturaWood.MAPLE, NaturaWood.SILVERBELL, NaturaWood.AMARANTH, NaturaWood.TIGER,
                NaturaWood.WILLOW, NaturaWood.EUCALYPTUS, NaturaWood.HOPSEED, NaturaWood.SAKURA}) {
            log(wood.getName() + "_log", () -> new NaturaLogBlock(logProps(1.5F, 3.0F, SoundType.WOOD)), 5, 20);
        }
        plainLog("redwood_bark", 2.0F, 2.0F, SoundType.WOOD, 5, 20);
        plainLog("redwood_heart", 2.0F, 2.0F, SoundType.WOOD, 5, 20);
        plainLog("redwood_root", 2.0F, 2.0F, SoundType.WOOD, 5, 20);
        for (NaturaWood wood : new NaturaWood[] {NaturaWood.MAPLE, NaturaWood.SILVERBELL, NaturaWood.AMARANTH, NaturaWood.TIGER,
                NaturaWood.WILLOW, NaturaWood.EUCALYPTUS, NaturaWood.HOPSEED, NaturaWood.SAKURA}) {
            leaves(wood.getName() + "_leaves", props -> new NaturaLeavesBlock(props, false), 30, 60);
        }
        leaves("redwood_leaves", RedwoodLeavesBlock::new, 5, 20);
        for (NaturaWood wood : new NaturaWood[] {NaturaWood.MAPLE, NaturaWood.SILVERBELL, NaturaWood.AMARANTH, NaturaWood.TIGER,
                NaturaWood.WILLOW, NaturaWood.EUCALYPTUS, NaturaWood.HOPSEED, NaturaWood.SAKURA, NaturaWood.REDWOOD}) {
            sapling(wood.getName(), null, false);
        }

        overworldPlants();

        // NaturaNether: logs, leaves, saplings
        log("ghostwood_log", () -> new NaturaLogBlock(logProps(3.5F, 24.0F, SoundType.WOOD)), 0, 0);
        log("darkwood_log", () -> new NaturaLogBlock(logProps(3.5F, 24.0F, SoundType.WOOD)), 0, 0);
        log("fusewood_log", () -> new FusewoodLogBlock(logProps(3.5F, 24.0F, SoundType.WOOD)), 0, 0);
        log("bloodwood_log", () -> new BloodwoodLogBlock(logProps(8.0F, 8.0F, SoundType.METAL)), 0, 0);
        log("bloodwood_bark", () -> new NaturaPlainLogBlock(logProps(8.0F, 8.0F, SoundType.METAL)), 0, 0);
        leaves("ghostwood_leaves", props -> new NaturaLeavesBlock(props, false), 0, 0);
        leaves("bloodwood_leaves", props -> new NaturaLeavesBlock(props, true), 0, 0);
        leaves("fusewood_leaves", props -> new NaturaLeavesBlock(props, false), 0, 0);
        leaves("darkwood_leaves", props -> new NaturaLeavesBlock(props, false), 0, 0);
        leaves("darkwood_flowering_leaves", props -> new NaturaLeavesBlock(props, false), 0, 0);
        leaves("darkwood_fruit_leaves", props -> new NaturaLeavesBlock(props, false), 0, 0);
        sapling("ghostwood", NETHER_SAPLING_SOIL, false);
        sapling("fusewood", NETHER_SAPLING_SOIL, false);
        sapling("darkwood", NETHER_SAPLING_SOIL, false);
        sapling("bloodwood", NETHER_SAPLING_SOIL, true);
        netherBlocks();
        for (String berry : new String[] {"blightberry", "duskberry", "skyberry", "stingberry"}) {
            bush(berry, true);
        }
        netherDevices();

        // planks + the decorative set of every wood
        for (NaturaWood wood : NaturaWood.values()) {
            WOODS.put(wood, woodSet(wood));
        }
        for (String door : new String[] {"eucalyptus", "hopseed", "sakura", "redwood", "redwood_bark", "tiger", "maple", "silverbell",
                "ghostwood", "bloodwood", "darkwood", "fusewood"}) {
            String id = door + "_door";
            DeferredBlock<DoorBlock> block = BLOCKS.register(id, () -> new DoorBlock(BlockSetType.OAK, wood(3.0F, 3.0F).noOcclusion().pushReaction(PushReaction.DESTROY)));
            TAB_DECORATIVE.add(ITEMS.register(id, () -> new NaturaDoorItem(block.get(), new Item.Properties())));
            DOORS.put(id, block);
        }
    }

    private NaturaBlocks() {}

    /** 1.12 NaturaNether: heat sand, tainted soil, thorn vines, nether glass. */
    private static void netherBlocks() {
        NETHER.put("heat_sand", block("heat_sand", () -> new HeatSandBlock(BlockBehaviour.Properties.of().mapColor(MapColor.NETHER).strength(3.0F)
                .sound(SoundType.SAND)), TAB_WORLD));
        for (String soil : new String[] {"tainted_soil", "tainted_farmland_dry", "tainted_farmland_heated"}) {
            NETHER.put(soil, block(soil, () -> new TaintedSoilBlock(BlockBehaviour.Properties.of().mapColor(MapColor.DIRT).strength(2.2F, 15.0F)
                    .sound(SoundType.GRAVEL)), TAB_WORLD));
        }
        NETHER.put("thorn_vines", block("thorn_vines", () -> new ThornVinesBlock(BlockBehaviour.Properties.of().mapColor(MapColor.PLANT).replaceable()
                .noCollission().randomTicks().strength(1.0F).sound(SoundType.VINE).ignitedByLava().pushReaction(PushReaction.DESTROY)), TAB_WORLD));
        for (String glass : new String[] {"soul", "heat"}) {
            NETHER.put(glass + "_glass", block(glass + "_glass", () -> new NetherGlassBlock(glass.equals("soul"), BlockBehaviour.Properties.of()
                    .mapColor(MapColor.NONE).strength(1.0F, 1800.0F).sound(SoundType.GLASS).noOcclusion().isValidSpawn((s, l, p, type) -> false)
                    .isRedstoneConductor((s, l, p) -> false).isSuffocating((s, l, p) -> false).isViewBlocking((s, l, p) -> false)), TAB_WORLD));
        }
    }

    /** 1.12 NaturaNether after the berry bushes: obelisk, glowshrooms, hopper, redstone parts, rails, furnace. */
    private static void netherDevices() {
        NETHER.put("respawn_obelisk", block("respawn_obelisk", () -> new RespawnObeliskBlock(BlockBehaviour.Properties.of().mapColor(MapColor.WOOD)
                .strength(2.0F, 600000.0F).sound(SoundType.WOOD).ignitedByLava()
                .lightLevel(s -> s.getValue(RespawnObeliskBlock.ACTIVE) ? 7 : 0)), TAB_WORLD));
        for (String color : new String[] {"green", "blue", "purple"}) {
            GLOWSHROOMS.put(color, block(color + "_glowshroom", () -> new NaturaGlowshroomBlock(color, BlockBehaviour.Properties.of()
                    .mapColor(MapColor.PLANT).noCollission().randomTicks().instabreak().sound(SoundType.GRASS).lightLevel(s -> 9)
                    .pushReaction(PushReaction.DESTROY)), TAB_WORLD));
        }
        for (String color : new String[] {"green", "blue", "purple"}) {
            DeferredBlock<LargeGlowshroomBlock> large = BLOCKS.register(color + "_large_glowshroom", () -> new LargeGlowshroomBlock(
                    () -> GLOWSHROOMS.get(color).get(), BlockBehaviour.Properties.of().mapColor(MapColor.WOOD).instabreak().sound(SoundType.WOOD)
                    .lightLevel(s -> 9).noOcclusion().dynamicShape().ignitedByLava()));
            ITEMS.register(color + "_large_glowshroom", () -> new NaturaBlockItem(large.get(), new Item.Properties()));
            LARGE_GLOWSHROOMS.put(color, large);
        }
        BLAZE_HOPPER = block("blaze_hopper", () -> new HopperBlock(BlockBehaviour.Properties.of().mapColor(MapColor.NETHER).requiresCorrectToolForDrops()
                .strength(3.0F, 8.0F).sound(SoundType.METAL).noOcclusion()), TAB_WORLD);
        NETHER.put("nether_lever", block("nether_lever", () -> new LeverBlock(BlockBehaviour.Properties.of().noCollission().strength(0.5F)
                .sound(SoundType.WOOD).pushReaction(PushReaction.DESTROY)), TAB_WORLD));
        NETHER.put("nether_button", block("nether_button", () -> new ButtonBlock(NETHERRACK, 20, BlockBehaviour.Properties.of().noCollission()
                .strength(0.5F).pushReaction(PushReaction.DESTROY)), TAB_WORLD));
        NETHER.put("nether_pressure_plate", block("nether_pressure_plate", () -> new PressurePlateBlock(BlockSetType.STONE, BlockBehaviour.Properties.of()
                .mapColor(MapColor.NETHER).forceSolidOn().requiresCorrectToolForDrops().noCollission().strength(0.5F).pushReaction(PushReaction.DESTROY)),
                TAB_WORLD));
        NETHER.put("blaze_rail", block("blaze_rail", () -> new BlazeRailBlocks.Normal(rail()), TAB_WORLD));
        NETHER.put("blaze_powered_rail", block("blaze_powered_rail", () -> new BlazeRailBlocks.Powered(rail(), false), TAB_WORLD));
        NETHER.put("blaze_activator_rail", block("blaze_activator_rail", () -> new BlazeRailBlocks.Powered(rail(), true), TAB_WORLD));
        NETHER.put("blaze_detector_rail", block("blaze_detector_rail", () -> new BlazeRailBlocks.Detector(rail()), TAB_WORLD));
        NETHERRACK_FURNACE = block("netherrack_furnace", () -> new NetherrackFurnaceBlock(BlockBehaviour.Properties.of().mapColor(MapColor.NETHER)
                .requiresCorrectToolForDrops().strength(3.5F, 3.5F).sound(SoundType.STONE).lightLevel(s -> s.getValue(NetherrackFurnaceBlock.LIT) ? 13 : 0)), TAB_WORLD);
    }

    private static BlockBehaviour.Properties rail() {
        return BlockBehaviour.Properties.of().noCollission().strength(0.7F).sound(SoundType.METAL);
    }

    /** 1.12 NaturaOverworld registration order: colored grass, bluebells, berry bushes, crops, saguaro. */
    private static void overworldPlants() {
        for (String type : new String[] {"topiary", "bluegrass", "autumnal"}) {
            String id = type.equals("bluegrass") ? "bluegrass" : type + "_grass";
            DeferredBlock<Block> grass = block(id, () -> new Block(BlockBehaviour.Properties.of().mapColor(MapColor.GRASS).strength(0.6F)
                    .sound(SoundType.GRASS)), TAB_WORLD);
            COLORED_GRASS.put(id, grass);
            switch (type) {
                case "topiary" -> COLORED_GRASS_TOPIARY = grass;
                case "bluegrass" -> COLORED_GRASS_BLUEGRASS = grass;
                default -> COLORED_GRASS_AUTUMNAL = grass;
            }
        }
        for (var grass : new java.util.ArrayList<>(COLORED_GRASS.entrySet())) {
            String id = grass.getKey();
            COLORED_GRASS.put(id + "_slab", block(id + "_slab", () -> new SlabBlock(BlockBehaviour.Properties.of().mapColor(MapColor.DIRT).strength(0.6F)
                    .sound(SoundType.GRASS)), TAB_DECORATIVE));
        }
        for (var grass : new String[] {"topiary_grass", "bluegrass", "autumnal_grass"}) {
            DeferredBlock<? extends Block> full = COLORED_GRASS.get(grass);
            COLORED_GRASS.put(grass + "_stairs", block(grass + "_stairs", () -> new StairBlock(full.get().defaultBlockState(),
                    BlockBehaviour.Properties.of().mapColor(MapColor.GRASS).strength(0.6F).sound(SoundType.GRASS)), TAB_DECORATIVE));
        }
        BLUEBELLS = block("bluebells", () -> new NaturaPlantBlock(BlockBehaviour.Properties.of().mapColor(MapColor.PLANT).noCollission().instabreak()
                .sound(SoundType.GRASS).pushReaction(PushReaction.DESTROY)), TAB_WORLD);
        for (String berry : new String[] {"raspberry", "blueberry", "blackberry", "maloberry"}) {
            bush(berry, false);
        }
        BARLEY_CROP = BLOCKS.register("barley_crop", () -> new NaturaCropBlock.Barley(() -> NaturaItems.BARLEY.get(), () -> NaturaItems.BARLEY_SEEDS.get(), cropProps()));
        COTTON_CROP = BLOCKS.register("cotton_crop", () -> new NaturaCropBlock.Cotton(() -> NaturaItems.COTTON.get(), () -> NaturaItems.COTTON_SEEDS.get(), cropProps()));
        SAGUARO = block("saguaro", () -> new SaguaroBlock(cactus()), TAB_WORLD);
        SAGUARO_BABY = block("saguaro_baby", () -> new SaguaroBabyBlock(cactus()), TAB_WORLD);
        SAGUARO_FRUIT = block("saguaro_fruit", () -> new SaguaroFruitBlock(cactus()), TAB_WORLD);
    }

    private static BlockBehaviour.Properties cropProps() {
        return BlockBehaviour.Properties.of().mapColor(MapColor.PLANT).noCollission().randomTicks().instabreak().sound(SoundType.CROP)
                .pushReaction(PushReaction.DESTROY);
    }

    private static BlockBehaviour.Properties cactus() {
        return BlockBehaviour.Properties.of().mapColor(MapColor.PLANT).strength(0.3F).sound(SoundType.WOOL).noOcclusion().randomTicks()
                .pushReaction(PushReaction.DESTROY);
    }

    /** 1.12 BlockOverworldBerryBush / BlockNetherBerryBush (leaves material, hardness 0.3); overworld ones burn (4 / 25). */
    private static void bush(String berry, boolean nether) {
        String id = berry + "_bush";
        DeferredBlock<NaturaBerryBushBlock> holder = block(id, () -> new NaturaBerryBushBlock(nether, () -> NaturaItems.BERRIES.get(berry).get(),
                BlockBehaviour.Properties.of().mapColor(MapColor.PLANT).strength(0.3F).randomTicks().sound(SoundType.GRASS).noOcclusion()
                        .isSuffocating((s, l, p) -> false).isViewBlocking((s, l, p) -> false).isRedstoneConductor((s, l, p) -> false)
                        .pushReaction(PushReaction.DESTROY)), TAB_WORLD);
        BUSHES.put(id, holder);
        if (!nether) {
            FLAMMABLE.put(holder, new int[] {4, 25});
        }
    }

    private static BlockBehaviour.Properties wood(float hardness, float resistance) {
        return BlockBehaviour.Properties.of().mapColor(MapColor.WOOD).instrument(NoteBlockInstrument.BASS).strength(hardness, resistance)
                .sound(SoundType.WOOD).ignitedByLava();
    }

    private static BlockBehaviour.Properties logProps(float hardness, float resistance, SoundType sound) {
        return BlockBehaviour.Properties.of().mapColor(MapColor.WOOD).instrument(NoteBlockInstrument.BASS).strength(hardness, resistance)
                .sound(sound).ignitedByLava();
    }

    private static <B extends Block> DeferredBlock<B> block(String id, Supplier<B> block, List<DeferredItem<? extends Item>> tab) {
        DeferredBlock<B> holder = BLOCKS.register(id, block);
        tab.add(ITEMS.register(id, () -> new NaturaBlockItem(holder.get(), new Item.Properties())));
        return holder;
    }

    private static <B extends Block> void log(String id, Supplier<B> block, int encouragement, int flammability) {
        DeferredBlock<B> holder = block(id, block, TAB_WORLD);
        LOGS.put(id, holder);
        if (flammability > 0) {
            FLAMMABLE.put(holder, new int[] {encouragement, flammability});
        }
    }

    private static void plainLog(String id, float hardness, float resistance, SoundType sound, int encouragement, int flammability) {
        log(id, () -> new NaturaPlainLogBlock(logProps(hardness, resistance, sound)), encouragement, flammability);
    }

    private static <B extends Block> void leaves(String id, Function<BlockBehaviour.Properties, B> block, int encouragement, int flammability) {
        DeferredBlock<B> holder = block(id, () -> block.apply(BlockBehaviour.Properties.of().mapColor(MapColor.PLANT).strength(0.2F).randomTicks()
                .sound(SoundType.GRASS).noOcclusion().isValidSpawn((s, l, p, type) -> type == EntityType.OCELOT || type == EntityType.PARROT)
                .isSuffocating((s, l, p) -> false).isViewBlocking((s, l, p) -> false).ignitedByLava().pushReaction(PushReaction.DESTROY).isRedstoneConductor((s, l, p) -> false)), TAB_WORLD);
        LEAVES.put(id, holder);
        if (flammability > 0) {
            FLAMMABLE.put(holder, new int[] {encouragement, flammability});
        }
    }

    private static void sapling(String name, TagKey<Block> soil, boolean hanging) {
        String id = name + "_sapling";
        DeferredBlock<NaturaSaplingBlock> holder = block(id, () -> new NaturaSaplingBlock(name, NaturaTrees.forSapling(name), soil, hanging,
                BlockBehaviour.Properties.of().mapColor(MapColor.PLANT).noCollission().randomTicks().instabreak().sound(SoundType.GRASS)
                        .pushReaction(PushReaction.DESTROY)), TAB_WORLD);
        SAPLINGS.put(id, holder);
    }

    private static WoodSet woodSet(NaturaWood wood) {
        String name = wood.getName();
        DeferredBlock<Block> planks = block(name + "_planks", () -> new Block(wood(2.0F, 2.0F)), TAB_WORLD);
        // 1.12 nether planks set fire info but returned 0 flammability; nether slabs stayed flammable
        if (!wood.isNether()) {
            FLAMMABLE.put(planks, new int[] {5, 20});
        }
        DeferredBlock<SlabBlock> slab = block(name + "_slab", () -> new SlabBlock(wood(2.0F, 2.0F)), TAB_DECORATIVE);
        FLAMMABLE.put(slab, new int[] {5, 20});
        DeferredBlock<StairBlock> stairs = block(name + "_stairs", () -> new StairBlock(planks.get().defaultBlockState(), wood(2.0F, 2.0F)), TAB_DECORATIVE);
        DeferredBlock<ButtonBlock> button = block(name + "_button", () -> new ButtonBlock(BlockSetType.OAK, 30,
                BlockBehaviour.Properties.of().noCollission().strength(0.5F).pushReaction(PushReaction.DESTROY)), TAB_DECORATIVE);
        DeferredBlock<PressurePlateBlock> plate = block(name + "_pressure_plate", () -> new PressurePlateBlock(BlockSetType.OAK,
                wood(0.5F, 0.5F).forceSolidOn().noCollission().pushReaction(PushReaction.DESTROY)), TAB_DECORATIVE);
        DeferredBlock<TrapDoorBlock> trapdoor = block(name + "_trapdoor", () -> new TrapDoorBlock(BlockSetType.OAK,
                wood(3.0F, 3.0F).noOcclusion().isValidSpawn((s, l, p, type) -> false)), TAB_DECORATIVE);
        DeferredBlock<FenceBlock> fence = block(name + "_fence", () -> new FenceBlock(wood(2.0F, 3.0F).forceSolidOn()), TAB_DECORATIVE);
        DeferredBlock<FenceGateBlock> gate = block(name + "_fence_gate", () -> new FenceGateBlock(WoodType.OAK, wood(2.0F, 3.0F).forceSolidOn()), TAB_DECORATIVE);
        return new WoodSet(planks, slab, stairs, fence, gate, trapdoor, button, plate, null, null, null);
    }

    /** NaturaDecorative bookshelves and workbenches, then the NaturaCommons sticks (1.12 module order). */
    public static void init() {
        for (NaturaWood wood : NaturaWood.values()) {
            String name = wood.getName();
            WoodSet set = WOODS.get(wood);
            DeferredBlock<Block> bookshelf = block(name + "_bookshelf", () -> new Block(wood(1.5F, 1.5F)), TAB_DECORATIVE);
            DeferredBlock<NaturaWorkbenchBlock> workbench = block(name + "_workbench", () -> new NaturaWorkbenchBlock(wood(2.5F, 2.5F)), TAB_DECORATIVE);
            WOODS.put(wood, new WoodSet(set.planks(), set.slab(), set.stairs(), set.fence(), set.fenceGate(), set.trapdoor(), set.button(),
                    set.pressurePlate(), bookshelf, workbench, null));
        }
        for (NaturaWood wood : NaturaWood.values()) {
            WoodSet set = WOODS.get(wood);
            DeferredItem<Item> stick = ITEMS.register(wood.getName() + "_stick", () -> new NaturaItem(new Item.Properties()));
            TAB_GENERAL.add(stick);
            WOODS.put(wood, new WoodSet(set.planks(), set.slab(), set.stairs(), set.fence(), set.fenceGate(), set.trapdoor(), set.button(),
                    set.pressurePlate(), set.bookshelf(), set.workbench(), stick));
        }
    }

    public static void registerFlammability() {
        FireBlock fire = (FireBlock) Blocks.FIRE;
        FLAMMABLE.forEach((block, info) -> fire.setFlammable(block.get(), info[0], info[1]));
    }
}
