package com.progwml6.natura.common.item;

import java.util.function.Supplier;

import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.ItemLike;
import net.minecraft.world.level.Level;

/** 1.12 ItemNaturaEdibleSoup: stacks to 1, gives its bowl back (a wooden one for the nether stews) into the inventory. */
public class NaturaSoupItem extends NaturaFoodItem {
    private final Supplier<? extends ItemLike> bowl;

    public NaturaSoupItem(Supplier<? extends ItemLike> bowl, Properties properties) {
        super(properties.stacksTo(1));
        this.bowl = bowl;
    }

    /** The bowl this stew gives back. */
    public ItemStack bowl() {
        return new ItemStack(this.bowl.get());
    }

    @Override
    public ItemStack finishUsingItem(ItemStack stack, Level level, LivingEntity entity) {
        ItemStack result = super.finishUsingItem(stack, level, entity);
        if (entity instanceof Player player) {
            ItemStack bowl = new ItemStack(this.bowl.get());
            if (!player.getInventory().add(bowl)) {
                player.drop(bowl, false, net.minecraft.util.Prediction.SERVER_ONLY);
            }
        }
        return result;
    }
}
