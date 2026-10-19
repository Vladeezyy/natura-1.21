package com.progwml6.natura.platform;

import net.minecraft.network.chat.Component;

import net.minecraft.core.HolderLookup;

import java.util.function.Consumer;

import java.util.function.BiConsumer;

import java.util.function.BiFunction;
import java.util.function.Supplier;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Holder;
import net.minecraft.core.Registry;
import net.minecraft.network.Connection;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.ToolMaterial;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;

/** What the common code needs from the mod loader (NeoForge or Fabric); found with {@link Services}. */
public interface Platform {

    /** "neoforge" or "fabric". */
    String name();

    // Registration

    /** Registers an entry (NeoForge: through a deferred register, Fabric: right away). */
    <R, T extends R> Holder<R> register(ResourceKey<? extends Registry<R>> registry, String name, Supplier<T> factory);

    /** A creative tab; the contents go to the consumer (CreativeModeTab.Output is not public in vanilla). */
    CreativeModeTab creativeTab(Component title, Supplier<ItemStack> icon, BiConsumer<HolderLookup.Provider, Consumer<ItemStack>> contents);

    /** A spawn egg for an entity type that may not exist yet when the item is made. */
    Item spawnEgg(Supplier<? extends EntityType<? extends Mob>> type, int background, int highlight, Item.Properties properties);

    /** The 1.12 kama: a sword that is also shears (NeoForge: the shears item abilities). */
    Item kama(ToolMaterial tier, Item.Properties properties);

    /** The 1.12 tainted soil (NeoForge: nether plants are sustained by it). */
    Block taintedSoil(BlockBehaviour.Properties properties);

    /** BlockEntityType.Builder.of (its factory interface is not public in vanilla). */
    <T extends BlockEntity> BlockEntityType<T> blockEntityType(BiFunction<BlockPos, BlockState, T> factory, Block... blocks);

    /** Sets how a block catches fire (1.12 Blocks.FIRE.setFireInfo). */
    void setFlammable(Block block, int encouragement, int flammability);

    // Hooks

    /** NeoForge CommonHooks.canCropGrow: other mods may allow or deny a growth step. */
    boolean canCropGrow(Level level, BlockPos pos, BlockState state, boolean grow);

    /** NeoForge CommonHooks.fireCropGrowPost. */
    void cropGrown(Level level, BlockPos pos, BlockState state);

    boolean isFakePlayer(Player player);

    /** Bone meal on a block (NeoForge: BoneMealItem.applyBonemeal with its event, Fabric: BoneMealItem.growCrop). */
    boolean applyBonemeal(ItemStack stack, Level level, BlockPos pos, Player player);

    /** A biome's downfall (NeoForge: after the biome modifiers' climate changes). */
    float downfall(Biome biome);

    /** Whether a tool shears (NeoForge: the shears_dig ability, Fabric: shears and the c:tools/shears tag). */
    boolean isShears(ItemStack stack);

    // GameTests

    /** Lets a GameTest mock player's connection carry every mod's packets. */
    void configureMockConnection(Connection connection);
}
