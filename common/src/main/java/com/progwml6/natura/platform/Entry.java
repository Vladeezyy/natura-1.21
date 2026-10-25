package com.progwml6.natura.platform;

import java.util.function.Supplier;

import net.minecraft.resources.ResourceLocation;

/** A registered entry (Forge: a registry object, Fabric: the entry registered right away). */
public class Entry<R, T extends R> implements Supplier<T> {
    private final ResourceLocation id;
    private final Supplier<R> holder;

    public Entry(ResourceLocation id, Supplier<R> holder) {
        this.id = id;
        this.holder = holder;
    }

    @Override
    @SuppressWarnings("unchecked")
    public T get() {
        return (T) this.holder.get();
    }

    public ResourceLocation getId() {
        return this.id;
    }

}
