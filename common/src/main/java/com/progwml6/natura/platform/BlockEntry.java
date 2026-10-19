package com.progwml6.natura.platform;

import net.minecraft.core.Holder;
import net.minecraft.resources.Identifier;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.ItemLike;
import net.minecraft.world.level.block.Block;

public final class BlockEntry<B extends Block> extends Entry<Block, B> implements ItemLike {
    public BlockEntry(Identifier id, Holder<Block> holder) {
        super(id, holder);
    }

    @Override
    public Item asItem() {
        return this.get().asItem();
    }
}
