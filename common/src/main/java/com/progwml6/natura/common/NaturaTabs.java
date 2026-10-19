package com.progwml6.natura.common;

import java.util.List;
import java.util.function.Supplier;

import com.progwml6.natura.Natura;

import net.minecraft.core.registries.Registries;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import com.progwml6.natura.platform.Entry;
import com.progwml6.natura.platform.ItemEntry;
import com.progwml6.natura.platform.Registration;
import com.progwml6.natura.platform.Services;

/** The three 1.12 tabs: NaturaGeneral, NaturaWorld, NaturaDecorative. */
public final class NaturaTabs {
    public static final Entry<CreativeModeTab, CreativeModeTab> GENERAL = tab("general", NaturaBlocks.TAB_GENERAL,
            () -> new ItemStack(NaturaBlocks.WOODS.get(com.progwml6.natura.library.NaturaWood.MAPLE).stick().get()));
    public static final Entry<CreativeModeTab, CreativeModeTab> WORLD = tab("world", NaturaBlocks.TAB_WORLD,
            () -> new ItemStack(NaturaBlocks.LOGS.get("maple_log").get()));
    public static final Entry<CreativeModeTab, CreativeModeTab> DECORATIVE = tab("decorative", NaturaBlocks.TAB_DECORATIVE,
            () -> new ItemStack(NaturaBlocks.DOORS.get("redwood_door").get()));

    private NaturaTabs() {}

    public static void init() {}

    private static Entry<CreativeModeTab, CreativeModeTab> tab(String name, List<ItemEntry<? extends Item>> items, Supplier<ItemStack> icon) {
        return Registration.register(Registries.CREATIVE_MODE_TAB, name, () -> Services.PLATFORM.creativeTab(Component.translatable("itemGroup.natura." + name), icon,
                (holders, output) -> items.forEach(item -> {
                    // 1.12 imp armor came enchanted in the creative tab
                    var enchantments = com.progwml6.natura.tools.NaturaTools.ENCHANTED.get(item);
                    if (enchantments == null) {
                        output.accept(new ItemStack(item.get()));
                        return;
                    }
                    ItemStack stack = new ItemStack(item.get());
                    var lookup = holders.lookupOrThrow(Registries.ENCHANTMENT);
                    enchantments.forEach(key -> stack.enchant(lookup.getOrThrow(key), 1));
                    output.accept(stack);
                })));
    }
}
