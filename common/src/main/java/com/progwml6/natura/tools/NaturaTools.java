package com.progwml6.natura.tools;

import java.util.EnumMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import com.progwml6.natura.Natura;
import com.progwml6.natura.common.NaturaBlocks;

import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.item.ArmorItem;
import net.minecraft.world.item.ArmorMaterial;
import net.minecraft.world.item.AxeItem;
import net.minecraft.world.item.BowItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.PickaxeItem;
import net.minecraft.world.item.ShovelItem;
import net.minecraft.world.item.SwordItem;
import net.minecraft.world.item.Tier;
import net.minecraft.world.item.Tiers;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.item.enchantment.Enchantment;
import net.minecraft.world.item.enchantment.Enchantments;
import com.progwml6.natura.platform.ItemEntry;
import com.progwml6.natura.platform.Services;

/**
 * 1.12 NaturaTools: ghostwood (wood), bloodwood (its own: mines like iron, 350 uses, speed 7, damage +3, enchantability
 * 24, no repair item), darkwood and nether quartz (stone), fusewood (iron) tools, kamas and bows (ghostwood 384,
 * bloodwood 1501, darkwood 162, fusewood 28 uses), flint and blaze, and imp armor.
 */
public final class NaturaTools {
    public static final Tier BLOODWOOD = new Tier() {
        @Override
        public int getUses() {
            return 350;
        }

        @Override
        public float getSpeed() {
            return 7.0F;
        }

        @Override
        public float getAttackDamageBonus() {
            return 3.0F;
        }

        /** Mines like iron. */
        @Override
        public int getLevel() {
            return 2;
        }

        @Override
        public int getEnchantmentValue() {
            return 24;
        }

        @Override
        public Ingredient getRepairIngredient() {
            return Ingredient.EMPTY;
        }
    };

    /** 1.12 "Imp": durability factor 33, 1 / 3 / 2 / 1 (boots, leggings, chestplate, helmet), enchantability 15, no repair item. */
    public static final ArmorMaterial IMP = new ArmorMaterial() {
        /** vanilla ArmorMaterials health per slot (boots 13, leggings 15, chestplate 16, helmet 11) times the factor */
        private static final Map<ArmorItem.Type, Integer> HEALTH = new EnumMap<>(Map.of(ArmorItem.Type.BOOTS, 13,
                ArmorItem.Type.LEGGINGS, 15, ArmorItem.Type.CHESTPLATE, 16, ArmorItem.Type.HELMET, 11));
        private static final Map<ArmorItem.Type, Integer> DEFENSE = new EnumMap<>(Map.of(ArmorItem.Type.BOOTS, 1,
                ArmorItem.Type.LEGGINGS, 3, ArmorItem.Type.CHESTPLATE, 2, ArmorItem.Type.HELMET, 1));

        @Override
        public int getDurabilityForType(ArmorItem.Type type) {
            return HEALTH.get(type) * 33;
        }

        @Override
        public int getDefenseForType(ArmorItem.Type type) {
            return DEFENSE.get(type);
        }

        @Override
        public int getEnchantmentValue() {
            return 15;
        }

        @Override
        public SoundEvent getEquipSound() {
            return SoundEvents.ARMOR_EQUIP_GENERIC;
        }

        @Override
        public Ingredient getRepairIngredient() {
            return Ingredient.EMPTY;
        }

        /** natura:textures/models/armor/imp_layer_1.png (Forge reads the namespace; Fabric: ArmorTextureMixin) */
        @Override
        public String getName() {
            return Natura.MODID + ":imp";
        }

        @Override
        public float getToughness() {
            return 0.0F;
        }

        @Override
        public float getKnockbackResistance() {
            return 0.0F;
        }
    };

    public static final Map<String, ItemEntry<Item>> TOOLS = new LinkedHashMap<>();
    /** The imp armor with the enchantments its 1.12 creative tab stacks had. */
    public static final Map<ItemEntry<Item>, List<Enchantment>> ENCHANTED = new LinkedHashMap<>();

    static {
        // 1.12 order: imp armor (NaturaEntities loaded), then the nether tools
        armor("imp_armor_helmet", ArmorItem.Type.HELMET, Enchantments.ALL_DAMAGE_PROTECTION);
        armor("imp_armor_chestplate", ArmorItem.Type.CHESTPLATE, Enchantments.BLAST_PROTECTION);
        armor("imp_armor_leggings", ArmorItem.Type.LEGGINGS, Enchantments.PROJECTILE_PROTECTION);
        armor("imp_armor_boots", ArmorItem.Type.BOOTS, Enchantments.FALL_PROTECTION);
        woodTools("ghostwood", Tiers.WOOD, 384);
        woodTools("bloodwood", BLOODWOOD, 1501);
        woodTools("darkwood", Tiers.STONE, 162);
        woodTools("fusewood", Tiers.IRON, 28);
        tools("netherquartz", Tiers.STONE);
        register("flint_and_blaze", new Item.Properties().stacksTo(1), FlintAndBlazeItem::new);
    }

    private NaturaTools() {}

    public static void init() {}

    private static ItemEntry<Item> register(String id, Item.Properties properties, java.util.function.Function<Item.Properties, Item> item) {
        ItemEntry<Item> holder = NaturaBlocks.ITEMS.register(id, () -> item.apply(properties));
        NaturaBlocks.TAB_GENERAL.add(holder);
        TOOLS.put(id, holder);
        return holder;
    }

    private static void armor(String id, ArmorItem.Type type, Enchantment enchantment) {
        ItemEntry<Item> holder = register(id, new Item.Properties(), p -> new ArmorItem(IMP, type, p));
        ENCHANTED.put(holder, List.of(enchantment, Enchantments.FIRE_PROTECTION));
    }

    private static void tools(String wood, Tier tier) {
        register(wood + "_sword", new Item.Properties(), p -> new SwordItem(tier, 3, -2.4F, p));
        register(wood + "_pickaxe", new Item.Properties(), p -> new PickaxeItem(tier, 1, -2.8F, p));
        register(wood + "_shovel", new Item.Properties(), p -> new ShovelItem(tier, 1.5F, -3.0F, p));
        // 1.12 ItemAxe damage / speed per vanilla material, bloodwood its own 3 / -3.0
        float damage = tier == Tiers.WOOD ? 6.0F : tier == BLOODWOOD ? 3.0F : tier == Tiers.STONE ? 7.0F : 6.0F;
        float speed = tier == Tiers.WOOD ? -3.2F : tier == BLOODWOOD ? -3.0F : tier == Tiers.STONE ? -3.2F : -3.1F;
        register(wood + "_axe", new Item.Properties(), p -> new AxeItem(tier, damage, speed, p));
        register(wood + "_kama", new Item.Properties(), p -> Services.PLATFORM.kama(tier, p));
    }

    private static void woodTools(String wood, Tier tier, int bowDurability) {
        tools(wood, tier);
        register(wood + "_bow", new Item.Properties().durability(bowDurability), BowItem::new);
    }
}
