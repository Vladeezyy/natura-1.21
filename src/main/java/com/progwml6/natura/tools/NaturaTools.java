package com.progwml6.natura.tools;

import java.util.EnumMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import com.progwml6.natura.Natura;
import com.progwml6.natura.common.NaturaBlocks;

import net.minecraft.core.Holder;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.tags.BlockTags;
import net.minecraft.world.item.ArmorItem;
import net.minecraft.world.item.ArmorMaterial;
import net.minecraft.world.item.AxeItem;
import net.minecraft.world.item.BowItem;
import net.minecraft.world.item.DiggerItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.PickaxeItem;
import net.minecraft.world.item.ShovelItem;
import net.neoforged.neoforge.common.SimpleTier;
import net.minecraft.world.item.SwordItem;
import net.minecraft.world.item.Tier;
import net.minecraft.world.item.Tiers;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.item.enchantment.Enchantment;
import net.minecraft.world.item.enchantment.Enchantments;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredItem;
import net.neoforged.neoforge.registries.DeferredRegister;

/**
 * 1.12 NaturaTools: ghostwood (wood), bloodwood (its own: mines like iron, 350 uses, speed 7, damage +3, enchantability
 * 24, no repair item), darkwood and nether quartz (stone), fusewood (iron) tools, kamas and bows (ghostwood 384,
 * bloodwood 1501, darkwood 162, fusewood 28 uses), flint and blaze, and imp armor.
 */
public final class NaturaTools {
    public static final Tier BLOODWOOD = new SimpleTier(BlockTags.INCORRECT_FOR_IRON_TOOL, 350, 7.0F, 3.0F, 24, () -> Ingredient.EMPTY);

    public static final DeferredRegister<ArmorMaterial> ARMOR = DeferredRegister.create(Registries.ARMOR_MATERIAL, Natura.MODID);
    /** 1.12 "Imp": durability factor 33, 1 / 3 / 2 / 1 (boots, leggings, chestplate, helmet), enchantability 15, no repair item. */
    public static final DeferredHolder<ArmorMaterial, ArmorMaterial> IMP = ARMOR.register("imp", () -> {
        Map<ArmorItem.Type, Integer> defense = new EnumMap<>(ArmorItem.Type.class);
        defense.put(ArmorItem.Type.BOOTS, 1);
        defense.put(ArmorItem.Type.LEGGINGS, 3);
        defense.put(ArmorItem.Type.CHESTPLATE, 2);
        defense.put(ArmorItem.Type.HELMET, 1);
        defense.put(ArmorItem.Type.BODY, 2);
        return new ArmorMaterial(defense, 15, SoundEvents.ARMOR_EQUIP_GENERIC, () -> Ingredient.EMPTY,
                List.of(new ArmorMaterial.Layer(ResourceLocation.fromNamespaceAndPath(Natura.MODID, "imp"))), 0.0F, 0.0F);
    });

    public static final Map<String, DeferredItem<Item>> TOOLS = new LinkedHashMap<>();
    /** The imp armor with the enchantments its 1.12 creative tab stacks had. */
    public static final Map<DeferredItem<Item>, List<ResourceKey<Enchantment>>> ENCHANTED = new LinkedHashMap<>();

    static {
        // 1.12 order: imp armor (NaturaEntities loaded), then the nether tools
        armor("imp_armor_helmet", ArmorItem.Type.HELMET, Enchantments.PROTECTION);
        armor("imp_armor_chestplate", ArmorItem.Type.CHESTPLATE, Enchantments.BLAST_PROTECTION);
        armor("imp_armor_leggings", ArmorItem.Type.LEGGINGS, Enchantments.PROJECTILE_PROTECTION);
        armor("imp_armor_boots", ArmorItem.Type.BOOTS, Enchantments.FEATHER_FALLING);
        woodTools("ghostwood", Tiers.WOOD, 384);
        woodTools("bloodwood", BLOODWOOD, 1501);
        woodTools("darkwood", Tiers.STONE, 162);
        woodTools("fusewood", Tiers.IRON, 28);
        tools("netherquartz", Tiers.STONE);
        register("flint_and_blaze", new Item.Properties().stacksTo(1), FlintAndBlazeItem::new);
    }

    private NaturaTools() {}

    public static void init() {}

    private static DeferredItem<Item> register(String id, Item.Properties properties, java.util.function.Function<Item.Properties, Item> item) {
        DeferredItem<Item> holder = NaturaBlocks.ITEMS.register(id, () -> item.apply(properties));
        NaturaBlocks.TAB_GENERAL.add(holder);
        TOOLS.put(id, holder);
        return holder;
    }

    private static void armor(String id, ArmorItem.Type type, ResourceKey<Enchantment> enchantment) {
        DeferredItem<Item> holder = register(id, new Item.Properties().durability(type.getDurability(33)), p -> new ArmorItem(IMP, type, p));
        ENCHANTED.put(holder, List.of(enchantment, Enchantments.FIRE_PROTECTION));
    }

    private static void tools(String wood, Tier tier) {
        register(wood + "_sword", new Item.Properties(), p -> new SwordItem(tier, p.attributes(SwordItem.createAttributes(tier, 3, -2.4F))));
        register(wood + "_pickaxe", new Item.Properties(), p -> new PickaxeItem(tier, p.attributes(DiggerItem.createAttributes(tier, 1.0F, -2.8F))));
        register(wood + "_shovel", new Item.Properties(), p -> new ShovelItem(tier, p.attributes(DiggerItem.createAttributes(tier, 1.5F, -3.0F))));
        // 1.12 ItemAxe damage / speed per vanilla material, bloodwood its own 3 / -3.0
        float damage = tier == Tiers.WOOD ? 6.0F : tier == BLOODWOOD ? 3.0F : tier == Tiers.STONE ? 7.0F : 6.0F;
        float speed = tier == Tiers.WOOD ? -3.2F : tier == BLOODWOOD ? -3.0F : tier == Tiers.STONE ? -3.2F : -3.1F;
        register(wood + "_axe", new Item.Properties(), p -> new AxeItem(tier, p.attributes(DiggerItem.createAttributes(tier, damage, speed))));
        register(wood + "_kama", new Item.Properties(), p -> new KamaItem(tier, p));
    }

    private static void woodTools(String wood, Tier tier, int bowDurability) {
        tools(wood, tier);
        register(wood + "_bow", new Item.Properties().durability(bowDurability), BowItem::new);
    }
}
