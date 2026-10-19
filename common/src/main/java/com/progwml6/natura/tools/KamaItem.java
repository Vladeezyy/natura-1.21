package com.progwml6.natura.tools;

import java.util.function.Consumer;

import com.progwml6.natura.common.item.NaturaItem;

import net.minecraft.network.chat.Component;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ToolMaterial;
import net.minecraft.world.item.component.TooltipDisplay;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.tags.BlockTags;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.entity.Shearable;
import net.minecraft.world.level.gameevent.GameEvent;

/**
 * 1.12 ItemNaturaKama: a sword that is also shears. Cobweb and leaves break fast (15), wool at 5; it shears sheep (and
 * other shearable mobs) and gets the sheared drops of leaves, grass and vines (NeoForge: the shears item abilities, in
 * its subclass; both: the c:tools/shears tag).
 */
public class KamaItem extends Item {
    public KamaItem(ToolMaterial tier, Properties properties) {
        super(properties.sword(tier, 3.0F, -2.4F));
    }

    @Override
    public float getDestroySpeed(ItemStack stack, BlockState state) {
        if (state.is(Blocks.COBWEB) || state.is(BlockTags.LEAVES)) {
            return 15.0F;
        }
        return state.is(BlockTags.WOOL) ? 5.0F : super.getDestroySpeed(stack, state);
    }

    @Override
    public boolean isCorrectToolForDrops(ItemStack stack, BlockState state) {
        return state.is(Blocks.COBWEB) || state.is(Blocks.REDSTONE_WIRE) || state.is(Blocks.TRIPWIRE);
    }

    @Override
    public InteractionResult interactLivingEntity(ItemStack stack, Player player, LivingEntity entity, InteractionHand hand) {
        // vanilla shearable mobs (NeoForge: its IShearable ones, in its subclass)
        if (entity instanceof Shearable target && target.readyForShearing()) {
            if (entity.level() instanceof ServerLevel level) {
                target.shear(level, SoundSource.PLAYERS, stack);
                entity.gameEvent(GameEvent.SHEAR, player);
                stack.hurtAndBreak(1, player, hand.asEquipmentSlot());
            }
            return InteractionResult.SUCCESS;
        }
        return InteractionResult.PASS;
    }

    @Override
    public void appendHoverText(ItemStack stack, TooltipContext context, TooltipDisplay display, Consumer<Component> tooltip, TooltipFlag flag) {
        NaturaItem.addTooltip(this.getDescriptionId(), tooltip);
    }
}
