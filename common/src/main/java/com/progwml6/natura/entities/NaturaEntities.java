package com.progwml6.natura.entities;

import com.progwml6.natura.Natura;
import com.progwml6.natura.common.NaturaBlocks;

import net.minecraft.core.registries.Registries;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.MobCategory;
import net.minecraft.world.entity.monster.Monster;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.levelgen.Heightmap;
import java.util.List;
import java.util.function.BiConsumer;

import com.progwml6.natura.platform.Registration;
import com.progwml6.natura.platform.Services;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.SpawnPlacements.Type;
import net.minecraft.world.entity.SpawnPlacements;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import com.progwml6.natura.platform.Entry;
import com.progwml6.natura.platform.ItemEntry;

/**
 * 1.12 NaturaEntities: the imp, the heatscar spider and its baby, the nitro creeper, with the 1.12 sizes and spawn egg
 * colours. They spawn in the nether biomes (data/natura/neoforge/biome_modifier, Fabric: read from there): imps 10 / 8-12 as creatures, heatscar
 * spiders 10 / 4 (only with the config option), nitro creepers 8 / 4-6.
 */
public final class NaturaEntities {
    public static final Entry<EntityType<?>, EntityType<ImpEntity>> IMP = Registration.register(Registries.ENTITY_TYPE, "imp", () -> EntityType.Builder
            .of(ImpEntity::new, MobCategory.CREATURE).sized(0.9F, 0.9F).fireImmune().clientTrackingRange(2).updateInterval(5).build("natura:imp"));
    public static final Entry<EntityType<?>, EntityType<HeatscarSpiderEntity>> HEATSCAR_SPIDER = Registration.register(Registries.ENTITY_TYPE, "heatscarspider",
            () -> EntityType.Builder.<HeatscarSpiderEntity>of((type, level) -> new HeatscarSpiderEntity(type, level, false), MobCategory.MONSTER)
                    .sized(2.7F, 1.9F).fireImmune().clientTrackingRange(2).updateInterval(5).build("natura:heatscarspider"));
    public static final Entry<EntityType<?>, EntityType<HeatscarSpiderEntity>> BABY_HEATSCAR_SPIDER = Registration.register(Registries.ENTITY_TYPE, "babyheatscarspider",
            () -> EntityType.Builder.<HeatscarSpiderEntity>of((type, level) -> new HeatscarSpiderEntity(type, level, true), MobCategory.MONSTER)
                    .sized(1.2F, 0.8F).fireImmune().clientTrackingRange(2).updateInterval(5).build("natura:babyheatscarspider"));
    public static final Entry<EntityType<?>, EntityType<NitroCreeperEntity>> NITRO_CREEPER = Registration.register(Registries.ENTITY_TYPE, "nitrocreeper", () -> EntityType.Builder
            .of(NitroCreeperEntity::new, MobCategory.MONSTER).sized(0.6F, 1.7F).fireImmune().clientTrackingRange(4).updateInterval(5).build("natura:nitrocreeper"));

    public static final ItemEntry<Item> IMP_SPAWN_EGG = NaturaBlocks.ITEMS.register("imp_spawn_egg",
            () -> Services.PLATFORM.spawnEgg(IMP, 0xF29735, 0x2E1F10, new Item.Properties()));
    public static final ItemEntry<Item> HEATSCAR_SPIDER_SPAWN_EGG = NaturaBlocks.ITEMS.register("heatscarspider_spawn_egg",
            () -> Services.PLATFORM.spawnEgg(HEATSCAR_SPIDER, 0xE64D10, 0x57B1BD, new Item.Properties()));
    public static final ItemEntry<Item> BABY_HEATSCAR_SPIDER_SPAWN_EGG = NaturaBlocks.ITEMS.register("babyheatscarspider_spawn_egg",
            () -> Services.PLATFORM.spawnEgg(BABY_HEATSCAR_SPIDER, 0xE64D10, 0x57B1BD, new Item.Properties()));
    public static final ItemEntry<Item> NITRO_CREEPER_SPAWN_EGG = NaturaBlocks.ITEMS.register("nitrocreeper_spawn_egg",
            () -> Services.PLATFORM.spawnEgg(NITRO_CREEPER, 0xF73E6C, 0x9B5004, new Item.Properties()));

    private NaturaEntities() {}

    public static void init() {}

    /** The 1.12 attributes (NeoForge EntityAttributeCreationEvent, Fabric FabricDefaultAttributeRegistry). */
    public static void attributes(BiConsumer<EntityType<? extends LivingEntity>, AttributeSupplier> event) {
        event.accept(IMP.get(), ImpEntity.createAttributes().build());
        event.accept(HEATSCAR_SPIDER.get(), HeatscarSpiderEntity.createAttributes().build());
        event.accept(BABY_HEATSCAR_SPIDER.get(), net.minecraft.world.entity.monster.Spider.createAttributes().build());
        event.accept(NITRO_CREEPER.get(), net.minecraft.world.entity.monster.Creeper.createAttributes().build());
    }

    /** Where they may spawn (NeoForge RegisterSpawnPlacementsEvent, Fabric SpawnPlacements.register). */
    public static void placements(PlacementRegistrar event) {
        event.register(IMP.get(), SpawnPlacements.Type.ON_GROUND, Heightmap.Types.MOTION_BLOCKING_NO_LEAVES, ImpEntity::checkImpSpawnRules);
        event.register(HEATSCAR_SPIDER.get(), SpawnPlacements.Type.ON_GROUND, Heightmap.Types.MOTION_BLOCKING_NO_LEAVES,
                HeatscarSpiderEntity::checkHeatscarSpawnRules);
        event.register(BABY_HEATSCAR_SPIDER.get(), SpawnPlacements.Type.ON_GROUND, Heightmap.Types.MOTION_BLOCKING_NO_LEAVES,
                HeatscarSpiderEntity::checkHeatscarSpawnRules);
        event.register(NITRO_CREEPER.get(), SpawnPlacements.Type.ON_GROUND, Heightmap.Types.MOTION_BLOCKING_NO_LEAVES, Monster::checkMonsterSpawnRules);
    }

    /** 1.12 spawn eggs were in the vanilla eggs tab. */
    public static List<ItemEntry<Item>> spawnEggs() {
        return List.of(IMP_SPAWN_EGG, HEATSCAR_SPIDER_SPAWN_EGG, BABY_HEATSCAR_SPIDER_SPAWN_EGG, NITRO_CREEPER_SPAWN_EGG);
    }

    public interface PlacementRegistrar {
        <T extends Mob> void register(EntityType<T> type, SpawnPlacements.Type placement, Heightmap.Types heightmap, SpawnPlacements.SpawnPredicate<T> predicate);
    }
}
