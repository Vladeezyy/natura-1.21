package com.progwml6.natura.platform;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.function.Supplier;

import com.progwml6.natura.Natura;

import net.minecraft.core.Registry;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.Identifier;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockBehaviour;

/**
 * Registration helpers over {@link Platform#register}. 26.x blocks and items need their id in their properties: while
 * a factory runs, the key it registers is known here, and {@link #blockProps()} / {@link #itemProps()} set it.
 */
public final class Registration {
    private static final ThreadLocal<ResourceKey<?>> CURRENT = new ThreadLocal<>();

    private Registration() {}

    public static <R, T extends R> Entry<R, T> register(ResourceKey<? extends Registry<R>> registry, String name, Supplier<T> factory) {
        return new Entry<>(id(name), Services.PLATFORM.register(registry, name, withKey(registry, name, factory)));
    }

    public static <B extends Block> BlockEntry<B> block(String name, Supplier<B> factory) {
        return new BlockEntry<>(id(name), Services.PLATFORM.register(Registries.BLOCK, name, withKey(Registries.BLOCK, name, factory)));
    }

    public static <I extends Item> ItemEntry<I> item(String name, Supplier<I> factory) {
        return new ItemEntry<>(id(name), Services.PLATFORM.register(Registries.ITEM, name, withKey(Registries.ITEM, name, factory)));
    }

    /** New block properties with the id of the block being made. */
    public static BlockBehaviour.Properties blockProps() {
        return BlockBehaviour.Properties.of().setId(current(Registries.BLOCK));
    }

    /** New item properties with the id of the item being made. */
    public static Item.Properties itemProps() {
        return new Item.Properties().setId(current(Registries.ITEM));
    }

    /** Sets the id of the item being made on properties made before. */
    public static Item.Properties withId(Item.Properties properties) {
        return properties.setId(current(Registries.ITEM));
    }

    @SuppressWarnings("unchecked")
    private static <T> ResourceKey<T> current(ResourceKey<? extends Registry<T>> registry) {
        ResourceKey<?> key = CURRENT.get();
        if (key == null || !key.isFor(registry)) {
            throw new IllegalStateException("Properties for " + registry.identifier() + " made outside its registration (" + key + ")");
        }
        return (ResourceKey<T>) key;
    }

    private static <R, T> Supplier<T> withKey(ResourceKey<? extends Registry<R>> registry, String name, Supplier<T> factory) {
        ResourceKey<R> key = ResourceKey.create(registry, id(name));
        return () -> {
            ResourceKey<?> outer = CURRENT.get();
            CURRENT.set(key);
            try {
                return factory.get();
            } finally {
                CURRENT.set(outer);
            }
        };
    }

    /** NeoForge DeferredRegister.Blocks-like: {@code BLOCKS.register(id, factory)}. */
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

    /** NeoForge DeferredRegister.Items-like: {@code ITEMS.register(id, factory)}. */
    public static final class Items {
        public <I extends Item> ItemEntry<I> register(String name, Supplier<I> factory) {
            return item(name, factory);
        }
    }

    private static Identifier id(String name) {
        return Identifier.fromNamespaceAndPath(Natura.MODID, name);
    }
}
