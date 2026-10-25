package com.progwml6.natura.tools;

import java.util.List;

import com.progwml6.natura.common.item.NaturaItem;

import net.minecraft.network.chat.Component;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.SwordItem;
import net.minecraft.world.item.Tier;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.tags.BlockTags;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.entity.Shearable;
import net.minecraft.world.level.gameevent.GameEvent;

/**
 * 1.12 ItemNaturaKama: a sword that is also shears. Cobweb and leaves break fast (15), wool at 5; it shears sheep (and
 * other shearable mobs) and gets the sheared drops of leaves, grass and vines (Forge: the shears tool actions, in
 * its subclass; both: the c:tools/shears tag).
 */
public class KamaItem extends SwordItem {
    public KamaItem(Tier tier, Properties properties) {
        super(tier, 3, -2.4F, properties);
    }

    @Override
    public float getDestroySpeed(ItemStack stack, BlockState state) {
        if (state.is(Blocks.COBWEB) || state.is(BlockTags.LEAVES)) {
            return 15.0F;
        }
        return state.is(BlockTags.WOOL) ? 5.0F : super.getDestroySpeed(stack, state);
    }

    @Override
    public boolean isCorrectToolForDrops(BlockState state) {
        return state.is(Blocks.COBWEB) || state.is(Blocks.REDSTONE_WIRE) || state.is(Blocks.TRIPWIRE);
    }

    @Override
    public InteractionResult interactLivingEntity(ItemStack stack, Player player, LivingEntity entity, InteractionHand hand) {
        // vanilla shearable mobs (Forge: its IForgeShearable ones, in its subclass)
        if (entity instanceof Shearable target && target.readyForShearing()) {
            if (!entity.level().isClientSide) {
                target.shear(SoundSource.PLAYERS);
                entity.gameEvent(GameEvent.SHEAR, player);
                stack.hurtAndBreak(1, player, e -> e.broadcastBreakEvent(hand));
            }
            return InteractionResult.sidedSuccess(entity.level().isClientSide);
        }
        return InteractionResult.PASS;
    }

    @Override
    public void appendHoverText(ItemStack stack, @javax.annotation.Nullable net.minecraft.world.level.Level context, List<Component> tooltip, TooltipFlag flag) {
        NaturaItem.addTooltip(this.getDescriptionId(), tooltip);
    }
}
