package com.progwml6.natura.forge;

import java.util.HashMap;
import java.util.Map;
import java.util.function.BiFunction;
import java.util.function.Supplier;

import com.google.common.collect.ImmutableSet;
import com.progwml6.natura.Natura;
import com.progwml6.natura.common.NaturaBlocks;
import com.progwml6.natura.platform.Platform;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Registry;
import net.minecraft.network.Connection;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.BoneMealItem;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Tier;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.FireBlock;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraftforge.common.ForgeHooks;
import net.minecraftforge.common.ForgeSpawnEggItem;
import net.minecraftforge.common.ToolActions;
import net.minecraftforge.common.util.FakePlayer;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.registries.DeferredRegister;

/** The Forge side of {@link Platform}: deferred registers on the mod bus and the Forge hooks. */
public class ForgePlatform implements Platform {
    /** Set by {@link #init} from the mod constructor, before the common registrations. */
    private static IEventBus modBus;
    private static final Map<ResourceKey<?>, DeferredRegister<?>> REGISTERS = new HashMap<>();

    static void init(IEventBus bus) {
        modBus = bus;
    }

    @SuppressWarnings("unchecked")
    static <R> DeferredRegister<R> deferred(ResourceKey<? extends Registry<R>> registry) {
        return (DeferredRegister<R>) REGISTERS.computeIfAbsent(registry, key -> {
            DeferredRegister<R> register = DeferredRegister.create(registry, Natura.MODID);
            register.register(modBus);
            return register;
        });
    }

    /** The blaze hopper is a hopper block entity (Forge 1.20.1 has no event for it: the AT opens validBlocks). */
    static void addHopperBlock() {
        BlockEntityType.HOPPER.validBlocks = ImmutableSet.<Block>builder().addAll(BlockEntityType.HOPPER.validBlocks)
                .add(NaturaBlocks.BLAZE_HOPPER.get()).build();
    }

    @Override
    public String name() {
        return "forge";
    }

    @Override
    @SuppressWarnings("unchecked")
    public <R, T extends R> Supplier<R> register(ResourceKey<? extends Registry<R>> registry, String name, Supplier<T> factory) {
        return (Supplier<R>) deferred(registry).register(name, factory);
    }

    @Override
    public CreativeModeTab.Builder creativeTabBuilder() {
        return CreativeModeTab.builder();
    }

    @Override
    @SuppressWarnings("unchecked")
    public Item spawnEgg(Supplier<? extends EntityType<? extends Mob>> type, int background, int highlight, Item.Properties properties) {
        return new ForgeSpawnEggItem((Supplier<? extends EntityType<? extends Mob>>) type, background, highlight, properties);
    }

    @Override
    public Item kama(Tier tier, Item.Properties properties) {
        return new ForgeKamaItem(tier, properties);
    }

    @Override
    public Block taintedSoil(BlockBehaviour.Properties properties) {
        return new ForgeTaintedSoilBlock(properties);
    }

    @Override
    public <T extends BlockEntity> BlockEntityType<T> blockEntityType(BiFunction<BlockPos, BlockState, T> factory, Block... blocks) {
        return BlockEntityType.Builder.of(factory::apply, blocks).build(null);
    }

    @Override
    public void setFlammable(Block block, int encouragement, int flammability) {
        ((FireBlock) Blocks.FIRE).setFlammable(block, encouragement, flammability);
    }

    @Override
    public boolean canCropGrow(Level level, BlockPos pos, BlockState state, boolean grow) {
        return ForgeHooks.onCropsGrowPre(level, pos, state, grow);
    }

    @Override
    public void cropGrown(Level level, BlockPos pos, BlockState state) {
        ForgeHooks.onCropsGrowPost(level, pos, state);
    }

    @Override
    public boolean isFakePlayer(Player player) {
        return player instanceof FakePlayer;
    }

    @Override
    public boolean isShears(ItemStack stack) {
        return stack.canPerformAction(ToolActions.SHEARS_DIG);
    }

    @Override
    public boolean applyBonemeal(ItemStack stack, Level level, BlockPos pos, Player player) {
        return BoneMealItem.applyBonemeal(stack, level, pos, player);
    }

    @Override
    public float downfall(Biome biome) {
        return biome.getModifiedClimateSettings().downfall();
    }

    @Override
    public void configureMockConnection(Connection connection) {
        // Forge 1.20.1 sends to any connection
    }
}
