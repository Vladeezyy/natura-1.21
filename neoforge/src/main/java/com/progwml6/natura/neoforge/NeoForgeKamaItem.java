package com.progwml6.natura.neoforge;

import java.util.List;

import com.progwml6.natura.tools.KamaItem;

import net.minecraft.core.BlockPos;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Tier;
import net.neoforged.neoforge.common.IShearable;
import net.neoforged.neoforge.common.ItemAbilities;
import net.neoforged.neoforge.common.ItemAbility;

/** The kama on NeoForge: the shears item abilities, and every IShearable mob (modded ones too). */
public class NeoForgeKamaItem extends KamaItem {
    public NeoForgeKamaItem(Tier tier, Properties properties) {
        super(tier, properties);
    }

    @Override
    public boolean canPerformAction(ItemStack stack, ItemAbility ability) {
        return ItemAbilities.DEFAULT_SHEARS_ACTIONS.contains(ability) || super.canPerformAction(stack, ability);
    }

    @Override
    public InteractionResult interactLivingEntity(ItemStack stack, Player player, LivingEntity entity, InteractionHand hand) {
        if (entity instanceof IShearable target) {
            BlockPos pos = entity.blockPosition();
            if (target.isShearable(player, stack, entity.level(), pos)) {
                List<ItemStack> drops = target.onSheared(player, stack, entity.level(), pos);
                if (!entity.level().isClientSide) {
                    for (ItemStack drop : drops) {
                        target.spawnShearedDrop(entity.level(), pos, drop);
                    }
                    stack.hurtAndBreak(1, player, LivingEntity.getSlotForHand(hand));
                }
                return InteractionResult.sidedSuccess(entity.level().isClientSide);
            }
        }
        return InteractionResult.PASS;
    }
}
