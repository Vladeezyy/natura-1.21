package com.progwml6.natura.platform;

import net.minecraft.core.Holder;
import net.minecraft.resources.Identifier;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.ItemLike;

public final class ItemEntry<I extends Item> extends Entry<Item, I> implements ItemLike {
    public ItemEntry(Identifier id, Holder<Item> holder) {
        super(id, holder);
    }

    @Override
    public Item asItem() {
        return this.get();
    }
}
