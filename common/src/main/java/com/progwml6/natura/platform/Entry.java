package com.progwml6.natura.platform;

import java.util.function.Supplier;

import net.minecraft.core.Holder;
import net.minecraft.resources.ResourceLocation;

/** A registered entry (NeoForge: a deferred holder, Fabric: the holder of an entry registered right away). */
public class Entry<R, T extends R> implements Supplier<T> {
    private final ResourceLocation id;
    private final Holder<R> holder;

    public Entry(ResourceLocation id, Holder<R> holder) {
        this.id = id;
        this.holder = holder;
    }

    @Override
    @SuppressWarnings("unchecked")
    public T get() {
        return (T) this.holder.value();
    }

    public ResourceLocation getId() {
        return this.id;
    }

    public Holder<R> holder() {
        return this.holder;
    }
}
