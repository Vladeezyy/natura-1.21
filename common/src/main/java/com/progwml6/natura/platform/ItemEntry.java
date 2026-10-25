package com.progwml6.natura.platform;

import java.util.function.Supplier;

import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.ItemLike;

public final class ItemEntry<I extends Item> extends Entry<Item, I> implements ItemLike {
    public ItemEntry(ResourceLocation id, Supplier<Item> holder) {
        super(id, holder);
    }

    @Override
    public Item asItem() {
        return this.get();
    }
}
