package com.progwml6.natura.tools;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import com.progwml6.natura.Natura;
import com.progwml6.natura.common.NaturaBlocks;
import com.progwml6.natura.platform.ItemEntry;
import com.progwml6.natura.platform.Registration;
import com.progwml6.natura.platform.Services;

import net.minecraft.core.registries.Registries;
import net.minecraft.resources.Identifier;
import net.minecraft.resources.ResourceKey;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.tags.BlockTags;
import net.minecraft.tags.TagKey;
import net.minecraft.world.item.BowItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ToolMaterial;
import net.minecraft.world.item.enchantment.Enchantment;
import net.minecraft.world.item.enchantment.Enchantments;
import net.minecraft.world.item.equipment.ArmorMaterial;
import net.minecraft.world.item.equipment.ArmorType;
import net.minecraft.world.item.equipment.EquipmentAssets;

/**
 * 1.12 NaturaTools: ghostwood (wood), bloodwood (its own: mines like iron, 350 uses, speed 7, damage +3, enchantability
 * 24, no repair item), darkwood and nether quartz (stone), fusewood (iron) tools, kamas and bows (ghostwood 384,
 * bloodwood 1501, darkwood 162, fusewood 28 uses), flint and blaze, and imp armor.
 */
public final class NaturaTools {
    /** 1.12 bloodwood: mines like iron, 350 uses, speed 7, damage +3, enchantability 24, no repair item (an empty tag). */
    public static final ToolMaterial BLOODWOOD = new ToolMaterial(BlockTags.INCORRECT_FOR_IRON_TOOL, 350, 7.0F, 3.0F, 24,
            TagKey.create(Registries.ITEM, Identifier.fromNamespaceAndPath(Natura.MODID, "bloodwood_tool_materials")));

    /** 1.12 "Imp": durability factor 33, 1 / 3 / 2 / 1 (boots, leggings, chestplate, helmet), enchantability 15, no repair item. */
    public static final ArmorMaterial IMP = new ArmorMaterial(33, Map.of(ArmorType.BOOTS, 1, ArmorType.LEGGINGS, 3, ArmorType.CHESTPLATE, 2,
            ArmorType.HELMET, 1, ArmorType.BODY, 2), 15, SoundEvents.ARMOR_EQUIP_GENERIC, 0.0F, 0.0F,
            TagKey.create(Registries.ITEM, Identifier.fromNamespaceAndPath(Natura.MODID, "imp_armor_materials")),
            ResourceKey.create(EquipmentAssets.ROOT_ID, Identifier.fromNamespaceAndPath(Natura.MODID, "imp")));

    public static final Map<String, ItemEntry<Item>> TOOLS = new LinkedHashMap<>();
    /** The imp armor with the enchantments its 1.12 creative tab stacks had. */
    public static final Map<ItemEntry<Item>, List<ResourceKey<Enchantment>>> ENCHANTED = new LinkedHashMap<>();

    static {
        // 1.12 order: imp armor (NaturaEntities loaded), then the nether tools
        armor("imp_armor_helmet", ArmorType.HELMET, Enchantments.PROTECTION);
        armor("imp_armor_chestplate", ArmorType.CHESTPLATE, Enchantments.BLAST_PROTECTION);
        armor("imp_armor_leggings", ArmorType.LEGGINGS, Enchantments.PROJECTILE_PROTECTION);
        armor("imp_armor_boots", ArmorType.BOOTS, Enchantments.FEATHER_FALLING);
        woodTools("ghostwood", ToolMaterial.WOOD, 384);
        woodTools("bloodwood", BLOODWOOD, 1501);
        woodTools("darkwood", ToolMaterial.STONE, 162);
        woodTools("fusewood", ToolMaterial.IRON, 28);
        tools("netherquartz", ToolMaterial.STONE);
        register("flint_and_blaze", p -> new FlintAndBlazeItem(p.stacksTo(1)));
    }

    private NaturaTools() {}

    public static void init() {}

    private static ItemEntry<Item> register(String id, java.util.function.Function<Item.Properties, Item> item) {
        ItemEntry<Item> holder = NaturaBlocks.ITEMS.register(id, () -> item.apply(Registration.itemProps()));
        NaturaBlocks.TAB_GENERAL.add(holder);
        TOOLS.put(id, holder);
        return holder;
    }

    private static void armor(String id, ArmorType type, ResourceKey<Enchantment> enchantment) {
        ItemEntry<Item> holder = register(id, p -> new Item(p.humanoidArmor(IMP, type)));
        ENCHANTED.put(holder, List.of(enchantment, Enchantments.FIRE_PROTECTION));
    }

    private static void tools(String wood, ToolMaterial tier) {
        register(wood + "_sword", p -> new Item(p.sword(tier, 3.0F, -2.4F)));
        register(wood + "_pickaxe", p -> new Item(p.pickaxe(tier, 1.0F, -2.8F)));
        register(wood + "_shovel", p -> new Item(p.shovel(tier, 1.5F, -3.0F)));
        // 1.12 ItemAxe damage / speed per vanilla material, bloodwood its own 3 / -3.0
        float damage = tier == ToolMaterial.WOOD ? 6.0F : tier == BLOODWOOD ? 3.0F : tier == ToolMaterial.STONE ? 7.0F : 6.0F;
        float speed = tier == ToolMaterial.WOOD ? -3.2F : tier == BLOODWOOD ? -3.0F : tier == ToolMaterial.STONE ? -3.2F : -3.1F;
        register(wood + "_axe", p -> new Item(p.axe(tier, damage, speed)));
        register(wood + "_kama", p -> Services.PLATFORM.kama(tier, p));
    }

    private static void woodTools(String wood, ToolMaterial tier, int bowDurability) {
        tools(wood, tier);
        // vanilla bows: enchantability 1
        register(wood + "_bow", p -> new BowItem(p.durability(bowDurability).enchantable(1)));
    }
}
