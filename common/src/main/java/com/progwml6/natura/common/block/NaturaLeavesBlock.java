package com.progwml6.natura.common.block;

import java.util.List;
import java.util.Random;

import net.minecraft.core.BlockPos;
import net.minecraft.core.registries.Registries;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.enchantment.EnchantmentHelper;
import net.minecraft.world.item.enchantment.Enchantments;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.LeavesBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.storage.loot.LootParams;
import net.minecraft.world.level.storage.loot.parameters.LootContextParams;

/**
 * Natura leaves: vanilla decay, drops from the loot table (sapling 1/25 or 1/20 with the 1.12 fortune steps, no apples).
 * Blood leaves (1.12 BlockNetherLeaves) also drop redstone, rolled with a Random made from a fixed seed every time, so
 * whether it drops depends on the fortune level only; kept as in 1.12.
 */
public class NaturaLeavesBlock extends LeavesBlock {
    private final boolean dropsRedstone;

    public NaturaLeavesBlock(Properties properties, boolean dropsRedstone) {
        super(properties);
        this.dropsRedstone = dropsRedstone;
    }

    @Override
    protected List<ItemStack> getDrops(BlockState state, LootParams.Builder params) {
        List<ItemStack> drops = super.getDrops(state, params);
        if (this.dropsRedstone) {
            ItemStack tool = params.getOptionalParameter(LootContextParams.TOOL);
            ServerLevel level = params.getLevel();
            var enchantments = level.registryAccess().lookupOrThrow(Registries.ENCHANTMENT);
            boolean silk = tool != null && EnchantmentHelper.getItemEnchantmentLevel(enchantments.getOrThrow(Enchantments.SILK_TOUCH), tool) > 0;
            boolean sheared = tool != null && com.progwml6.natura.platform.Services.PLATFORM.isShears(tool);
            if (!silk && !sheared) {
                int fortune = tool == null ? 0 : EnchantmentHelper.getItemEnchantmentLevel(enchantments.getOrThrow(Enchantments.FORTUNE), tool);
                Random rand = new Random();
                rand.setSeed(2 ^ 16 + 2 ^ 8 + (4 * 3 * 271));
                if (fortune > 3 || rand.nextInt(40 - fortune * 10) == 0) {
                    drops.add(new ItemStack(Items.REDSTONE));
                }
            }
        }
        return drops;
    }

    @Override
    protected void onRemove(BlockState state, Level level, BlockPos pos, BlockState newState, boolean movedByPiston) {
        if (!state.is(newState.getBlock())) {
            RedwoodLeavesBlock.beginLeavesDecay(level, pos, 2);
        }
        super.onRemove(state, level, pos, newState, movedByPiston);
    }
}
