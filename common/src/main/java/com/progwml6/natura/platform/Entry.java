package com.progwml6.natura.platform;

import java.util.function.Supplier;

import net.minecraft.core.Holder;
import net.minecraft.resources.Identifier;

/** A registered entry (NeoForge: a deferred holder, Fabric: the holder of an entry registered right away). */
public class Entry<R, T extends R> implements Supplier<T> {
    private final Identifier id;
    private final Holder<R> holder;

    public Entry(Identifier id, Holder<R> holder) {
        this.id = id;
        this.holder = holder;
    }

    @Override
    @SuppressWarnings("unchecked")
    public T get() {
        return (T) this.holder.value();
    }

    public Identifier getId() {
        return this.id;
    }

    public Holder<R> holder() {
        return this.holder;
    }
}
