package com.progwml6.natura.fabric.mixin;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Invoker;

import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.SpawnPlacements.Type;
import net.minecraft.world.entity.SpawnPlacements;
import net.minecraft.world.level.levelgen.Heightmap;

/** SpawnPlacements.register is private in vanilla (NeoForge: RegisterSpawnPlacementsEvent). */
@Mixin(SpawnPlacements.class)
public interface SpawnPlacementsInvoker {
    @Invoker("register")
    static <T extends Mob> void invokeRegister(EntityType<T> type, SpawnPlacements.Type placement, Heightmap.Types heightmap,
                                               SpawnPlacements.SpawnPredicate<T> predicate) {
        throw new AssertionError();
    }
}
