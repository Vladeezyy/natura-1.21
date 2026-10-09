package com.progwml6.natura.entities;

import com.progwml6.natura.Natura;
import com.progwml6.natura.common.NaturaBlocks;

import net.minecraft.core.registries.Registries;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.MobCategory;
import net.minecraft.world.entity.SpawnPlacementTypes;
import net.minecraft.world.entity.monster.Monster;
import net.minecraft.world.item.CreativeModeTabs;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.levelgen.Heightmap;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.common.DeferredSpawnEggItem;
import net.neoforged.neoforge.event.BuildCreativeModeTabContentsEvent;
import net.neoforged.neoforge.event.entity.EntityAttributeCreationEvent;
import net.neoforged.neoforge.event.entity.RegisterSpawnPlacementsEvent;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredItem;
import net.neoforged.neoforge.registries.DeferredRegister;

/**
 * 1.12 NaturaEntities: the imp, the heatscar spider and its baby, the nitro creeper, with the 1.12 sizes and spawn egg
 * colours. They spawn in the nether biomes (data/natura/neoforge/biome_modifier): imps 10 / 8-12 as creatures, heatscar
 * spiders 10 / 4 (only with the config option), nitro creepers 8 / 4-6.
 */
public final class NaturaEntities {
    public static final DeferredRegister<EntityType<?>> ENTITIES = DeferredRegister.create(Registries.ENTITY_TYPE, Natura.MODID);

    public static final DeferredHolder<EntityType<?>, EntityType<ImpEntity>> IMP = ENTITIES.register("imp", () -> EntityType.Builder
            .of(ImpEntity::new, MobCategory.CREATURE).sized(0.9F, 0.9F).fireImmune().clientTrackingRange(2).updateInterval(5).build("natura:imp"));
    public static final DeferredHolder<EntityType<?>, EntityType<HeatscarSpiderEntity>> HEATSCAR_SPIDER = ENTITIES.register("heatscarspider",
            () -> EntityType.Builder.<HeatscarSpiderEntity>of((type, level) -> new HeatscarSpiderEntity(type, level, false), MobCategory.MONSTER)
                    .sized(2.7F, 1.9F).fireImmune().clientTrackingRange(2).updateInterval(5).build("natura:heatscarspider"));
    public static final DeferredHolder<EntityType<?>, EntityType<HeatscarSpiderEntity>> BABY_HEATSCAR_SPIDER = ENTITIES.register("babyheatscarspider",
            () -> EntityType.Builder.<HeatscarSpiderEntity>of((type, level) -> new HeatscarSpiderEntity(type, level, true), MobCategory.MONSTER)
                    .sized(1.2F, 0.8F).fireImmune().clientTrackingRange(2).updateInterval(5).build("natura:babyheatscarspider"));
    public static final DeferredHolder<EntityType<?>, EntityType<NitroCreeperEntity>> NITRO_CREEPER = ENTITIES.register("nitrocreeper", () -> EntityType.Builder
            .of(NitroCreeperEntity::new, MobCategory.MONSTER).sized(0.6F, 1.7F).fireImmune().clientTrackingRange(4).updateInterval(5).build("natura:nitrocreeper"));

    public static final DeferredItem<Item> IMP_SPAWN_EGG = NaturaBlocks.ITEMS.register("imp_spawn_egg",
            () -> new DeferredSpawnEggItem(IMP, 0xF29735, 0x2E1F10, new Item.Properties()));
    public static final DeferredItem<Item> HEATSCAR_SPIDER_SPAWN_EGG = NaturaBlocks.ITEMS.register("heatscarspider_spawn_egg",
            () -> new DeferredSpawnEggItem(HEATSCAR_SPIDER, 0xE64D10, 0x57B1BD, new Item.Properties()));
    public static final DeferredItem<Item> BABY_HEATSCAR_SPIDER_SPAWN_EGG = NaturaBlocks.ITEMS.register("babyheatscarspider_spawn_egg",
            () -> new DeferredSpawnEggItem(BABY_HEATSCAR_SPIDER, 0xE64D10, 0x57B1BD, new Item.Properties()));
    public static final DeferredItem<Item> NITRO_CREEPER_SPAWN_EGG = NaturaBlocks.ITEMS.register("nitrocreeper_spawn_egg",
            () -> new DeferredSpawnEggItem(NITRO_CREEPER, 0xF73E6C, 0x9B5004, new Item.Properties()));

    private NaturaEntities() {}

    public static void register(IEventBus modBus) {
        ENTITIES.register(modBus);
        modBus.addListener(NaturaEntities::attributes);
        modBus.addListener(NaturaEntities::placements);
        modBus.addListener(NaturaEntities::spawnEggs);
    }

    private static void attributes(EntityAttributeCreationEvent event) {
        event.put(IMP.get(), ImpEntity.createAttributes().build());
        event.put(HEATSCAR_SPIDER.get(), HeatscarSpiderEntity.createAttributes().build());
        event.put(BABY_HEATSCAR_SPIDER.get(), net.minecraft.world.entity.monster.Spider.createAttributes().build());
        event.put(NITRO_CREEPER.get(), net.minecraft.world.entity.monster.Creeper.createAttributes().build());
    }

    private static void placements(RegisterSpawnPlacementsEvent event) {
        event.register(IMP.get(), SpawnPlacementTypes.ON_GROUND, Heightmap.Types.MOTION_BLOCKING_NO_LEAVES, ImpEntity::checkImpSpawnRules,
                RegisterSpawnPlacementsEvent.Operation.REPLACE);
        event.register(HEATSCAR_SPIDER.get(), SpawnPlacementTypes.ON_GROUND, Heightmap.Types.MOTION_BLOCKING_NO_LEAVES,
                HeatscarSpiderEntity::checkHeatscarSpawnRules, RegisterSpawnPlacementsEvent.Operation.REPLACE);
        event.register(BABY_HEATSCAR_SPIDER.get(), SpawnPlacementTypes.ON_GROUND, Heightmap.Types.MOTION_BLOCKING_NO_LEAVES,
                HeatscarSpiderEntity::checkHeatscarSpawnRules, RegisterSpawnPlacementsEvent.Operation.REPLACE);
        event.register(NITRO_CREEPER.get(), SpawnPlacementTypes.ON_GROUND, Heightmap.Types.MOTION_BLOCKING_NO_LEAVES,
                Monster::checkMonsterSpawnRules, RegisterSpawnPlacementsEvent.Operation.REPLACE);
    }

    /** 1.12 spawn eggs were in the vanilla eggs tab. */
    private static void spawnEggs(BuildCreativeModeTabContentsEvent event) {
        if (event.getTabKey() == CreativeModeTabs.SPAWN_EGGS) {
            event.accept(IMP_SPAWN_EGG.get());
            event.accept(HEATSCAR_SPIDER_SPAWN_EGG.get());
            event.accept(BABY_HEATSCAR_SPIDER_SPAWN_EGG.get());
            event.accept(NITRO_CREEPER_SPAWN_EGG.get());
        }
    }
}
