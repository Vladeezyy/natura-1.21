package com.progwml6.natura.fabric;

import java.io.IOException;
import java.io.Reader;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Predicate;
import java.util.stream.Stream;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.progwml6.natura.Natura;
import com.progwml6.natura.common.recipe.ConfigOptionEnabledCondition;

import net.fabricmc.fabric.api.biome.v1.BiomeModificationContext;
import net.fabricmc.fabric.api.biome.v1.BiomeModifications;
import net.fabricmc.fabric.api.biome.v1.BiomeSelectionContext;
import net.fabricmc.fabric.api.biome.v1.BiomeSelectors;
import net.fabricmc.fabric.api.biome.v1.ModificationPhase;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.Identifier;
import net.minecraft.tags.TagKey;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.level.biome.MobSpawnSettings;
import net.minecraft.world.level.levelgen.GenerationStep;

/**
 * The NeoForge biome modifiers (data/natura/neoforge/biome_modifier) applied with the Fabric BiomeModifications: the
 * same files, read from the mod jar. Supports what Natura uses: neoforge:add_features and neoforge:add_spawns, biomes
 * neoforge:any or a tag, and natura:config_option_enabled conditions (checked when a world loads, like NeoForge).
 */
final class FabricBiomeModifiers {
    private static final String DIR = "data/" + Natura.MODID + "/neoforge/biome_modifier";

    private FabricBiomeModifiers() {}

    static void register() {
        Path dir = FabricLoader.getInstance().getModContainer(Natura.MODID).orElseThrow().findPath(DIR).orElseThrow();
        List<Path> files;
        try (Stream<Path> list = Files.list(dir)) {
            files = list.filter(p -> p.toString().endsWith(".json")).sorted().toList();
        } catch (IOException e) {
            throw new IllegalStateException("Could not list " + DIR, e);
        }
        for (Path file : files) {
            JsonObject json;
            try (Reader reader = Files.newBufferedReader(file)) {
                json = JsonParser.parseReader(reader).getAsJsonObject();
            } catch (IOException e) {
                throw new IllegalStateException("Could not read " + file, e);
            }
            String name = file.getFileName().toString().replace(".json", "");
            apply(Identifier.fromNamespaceAndPath(Natura.MODID, name), json);
        }
    }

    private static void apply(Identifier id, JsonObject json) {
        List<String> conditions = new ArrayList<>();
        if (json.has("neoforge:conditions")) {
            for (JsonElement condition : json.getAsJsonArray("neoforge:conditions")) {
                JsonObject object = condition.getAsJsonObject();
                if (!object.get("type").getAsString().equals(Natura.MODID + ":" + ConfigOptionEnabledCondition.ID)) {
                    throw new IllegalStateException("Unsupported condition in " + id + ": " + object);
                }
                conditions.add(object.get(ConfigOptionEnabledCondition.FIELD).getAsString());
            }
        }
        Predicate<BiomeSelectionContext> biomes = biomes(json.get("biomes"));
        String type = json.get("type").getAsString();
        switch (type) {
            case "neoforge:add_features" -> {
                GenerationStep.Decoration step = GenerationStep.Decoration.valueOf(json.get("step").getAsString().toUpperCase(java.util.Locale.ROOT));
                List<ResourceKey<net.minecraft.world.level.levelgen.placement.PlacedFeature>> features = strings(json.get("features")).stream()
                        .map(f -> ResourceKey.create(Registries.PLACED_FEATURE, Identifier.parse(f))).toList();
                BiomeModifications.create(id).add(ModificationPhase.ADDITIONS, biomes, context -> {
                    if (enabled(conditions)) {
                        features.forEach(feature -> context.getGenerationSettings().addFeature(step, feature));
                    }
                });
            }
            case "neoforge:add_spawns" -> {
                List<JsonObject> spawners = new ArrayList<>();
                JsonElement element = json.get("spawners");
                if (element.isJsonArray()) {
                    element.getAsJsonArray().forEach(e -> spawners.add(e.getAsJsonObject()));
                } else {
                    spawners.add(element.getAsJsonObject());
                }
                BiomeModifications.create(id).add(ModificationPhase.ADDITIONS, biomes, context -> {
                    if (enabled(conditions)) {
                        spawners.forEach(spawner -> addSpawn(context, spawner));
                    }
                });
            }
            default -> throw new IllegalStateException("Unsupported biome modifier type in " + id + ": " + type);
        }
    }

    private static void addSpawn(BiomeModificationContext context, JsonObject spawner) {
        EntityType<?> type = BuiltInRegistries.ENTITY_TYPE.getValue(Identifier.parse(spawner.get("type").getAsString()));
        context.getMobSpawnSettings().addSpawn(type.getCategory(), new MobSpawnSettings.SpawnerData(type, count(spawner.get("count"))),
                spawner.get("weight").getAsInt());
    }

    /** 26.3 spawn counts: a number or {"type": "minecraft:uniform", "min_inclusive", "max_inclusive"}. */
    private static net.minecraft.util.valueproviders.IntProvider count(JsonElement count) {
        if (count.isJsonPrimitive()) {
            return net.minecraft.util.valueproviders.ConstantInt.of(count.getAsInt());
        }
        JsonObject uniform = count.getAsJsonObject();
        return net.minecraft.util.valueproviders.UniformInt.of(uniform.get("min_inclusive").getAsInt(), uniform.get("max_inclusive").getAsInt());
    }

    private static boolean enabled(List<String> conditions) {
        return conditions.stream().allMatch(ConfigOptionEnabledCondition::test);
    }

    private static Predicate<BiomeSelectionContext> biomes(JsonElement biomes) {
        if (biomes.isJsonObject() && biomes.getAsJsonObject().get("type").getAsString().equals("neoforge:any")) {
            return BiomeSelectors.all();
        }
        if (biomes.isJsonPrimitive() && biomes.getAsString().startsWith("#")) {
            return BiomeSelectors.tag(TagKey.create(Registries.BIOME, Identifier.parse(biomes.getAsString().substring(1))));
        }
        throw new IllegalStateException("Unsupported biomes: " + biomes);
    }

    private static List<String> strings(JsonElement element) {
        if (element.isJsonArray()) {
            List<String> list = new ArrayList<>();
            for (JsonElement e : (JsonArray) element) {
                list.add(e.getAsString());
            }
            return list;
        }
        return List.of(element.getAsString());
    }
}
