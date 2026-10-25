package com.progwml6.natura.platform;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.function.Supplier;

import com.progwml6.natura.Natura;

import net.minecraft.core.Registry;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.block.Block;

/** Registration helpers over {@link Platform#register}. */
public final class Registration {
    private Registration() {}

    public static <R, T extends R> Entry<R, T> register(ResourceKey<? extends Registry<R>> registry, String name, Supplier<T> factory) {
        return new Entry<>(id(name), Services.PLATFORM.register(registry, name, factory));
    }

    public static <B extends Block> BlockEntry<B> block(String name, Supplier<B> factory) {
        return new BlockEntry<>(id(name), Services.PLATFORM.register(Registries.BLOCK, name, factory));
    }

    public static <I extends Item> ItemEntry<I> item(String name, Supplier<I> factory) {
        return new ItemEntry<>(id(name), Services.PLATFORM.register(Registries.ITEM, name, factory));
    }

    /** DeferredRegister.Blocks-like (as in NeoForge 1.21): {@code BLOCKS.register(id, factory)}. */
    public static final class Blocks {
        private final List<BlockEntry<?>> entries = new ArrayList<>();

        public <B extends Block> BlockEntry<B> register(String name, Supplier<B> factory) {
            BlockEntry<B> entry = block(name, factory);
            this.entries.add(entry);
            return entry;
        }

        public List<BlockEntry<?>> getEntries() {
            return Collections.unmodifiableList(this.entries);
        }
    }

    /** DeferredRegister.Items-like (as in NeoForge 1.21): {@code ITEMS.register(id, factory)}. */
    public static final class Items {
        public <I extends Item> ItemEntry<I> register(String name, Supplier<I> factory) {
            return item(name, factory);
        }
    }

    private static ResourceLocation id(String name) {
        return new ResourceLocation(Natura.MODID, name);
    }
}
