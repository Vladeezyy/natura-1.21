package com.progwml6.natura.forge;

import java.util.List;
import java.util.Random;

import com.progwml6.natura.tools.KamaItem;

import net.minecraft.core.BlockPos;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Tier;
import net.minecraft.world.item.enchantment.EnchantmentHelper;
import net.minecraft.world.item.enchantment.Enchantments;
import net.minecraftforge.common.IForgeShearable;
import net.minecraftforge.common.ToolAction;
import net.minecraftforge.common.ToolActions;

/** The kama on Forge: the shears tool actions, and every IForgeShearable mob (modded ones too), as the Forge shears do. */
public class ForgeKamaItem extends KamaItem {
    public ForgeKamaItem(Tier tier, Properties properties) {
        super(tier, properties);
    }

    @Override
    public boolean canPerformAction(ItemStack stack, ToolAction action) {
        return ToolActions.DEFAULT_SHEARS_ACTIONS.contains(action) || super.canPerformAction(stack, action);
    }

    @Override
    public InteractionResult interactLivingEntity(ItemStack stack, Player player, LivingEntity entity, InteractionHand hand) {
        if (entity instanceof IForgeShearable target) {
            BlockPos pos = BlockPos.containing(entity.position());
            if (target.isShearable(stack, entity.level(), pos)) {
                if (!entity.level().isClientSide) {
                    List<ItemStack> drops = target.onSheared(player, stack, entity.level(), pos,
                            EnchantmentHelper.getItemEnchantmentLevel(Enchantments.BLOCK_FORTUNE, stack));
                    Random rand = new Random();
                    for (ItemStack drop : drops) {
                        ItemEntity item = entity.spawnAtLocation(drop, 1.0F);
                        if (item != null) {
                            item.setDeltaMovement(item.getDeltaMovement().add((rand.nextFloat() - rand.nextFloat()) * 0.1F, rand.nextFloat() * 0.05F,
                                    (rand.nextFloat() - rand.nextFloat()) * 0.1F));
                        }
                    }
                    stack.hurtAndBreak(1, player, e -> e.broadcastBreakEvent(hand));
                }
                return InteractionResult.sidedSuccess(entity.level().isClientSide);
            }
        }
        return InteractionResult.PASS;
    }
}
