package com.progwml6.natura.common;

import java.util.List;
import java.util.function.Supplier;

import com.progwml6.natura.Natura;

import net.minecraft.core.registries.Registries;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredItem;
import net.neoforged.neoforge.registries.DeferredRegister;

/** The three 1.12 tabs: NaturaGeneral, NaturaWorld, NaturaDecorative. */
public final class NaturaTabs {
    public static final DeferredRegister<CreativeModeTab> TABS = DeferredRegister.create(Registries.CREATIVE_MODE_TAB, Natura.MODID);

    public static final DeferredHolder<CreativeModeTab, CreativeModeTab> GENERAL = tab("general", NaturaBlocks.TAB_GENERAL,
            () -> new ItemStack(NaturaBlocks.WOODS.get(com.progwml6.natura.library.NaturaWood.MAPLE).stick().get()));
    public static final DeferredHolder<CreativeModeTab, CreativeModeTab> WORLD = tab("world", NaturaBlocks.TAB_WORLD,
            () -> new ItemStack(NaturaBlocks.LOGS.get("maple_log").get()));
    public static final DeferredHolder<CreativeModeTab, CreativeModeTab> DECORATIVE = tab("decorative", NaturaBlocks.TAB_DECORATIVE,
            () -> new ItemStack(NaturaBlocks.DOORS.get("redwood_door").get()));

    private NaturaTabs() {}

    private static DeferredHolder<CreativeModeTab, CreativeModeTab> tab(String name, List<DeferredItem<? extends Item>> items, Supplier<ItemStack> icon) {
        return TABS.register(name, () -> CreativeModeTab.builder().title(Component.translatable("itemGroup.natura." + name)).icon(icon)
                .displayItems((params, output) -> items.forEach(item -> output.accept(item.get()))).build());
    }
}
