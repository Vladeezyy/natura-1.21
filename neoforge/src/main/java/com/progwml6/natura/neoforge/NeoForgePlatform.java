package com.progwml6.natura.neoforge;

import net.minecraft.world.item.ToolMaterial;

import net.minecraft.network.chat.Component;

import net.minecraft.core.HolderLookup;

import java.util.function.Consumer;

import java.util.function.BiConsumer;

import java.util.HashMap;
import java.util.Map;
import java.util.function.BiFunction;
import java.util.function.Supplier;

import com.progwml6.natura.Natura;
import com.progwml6.natura.platform.Platform;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Holder;
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
import net.minecraft.world.level.Level;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.common.CommonHooks;
import net.neoforged.neoforge.common.ItemAbilities;
import net.neoforged.neoforge.common.util.FakePlayer;
import net.neoforged.neoforge.network.registration.NetworkRegistry;
import net.neoforged.neoforge.registries.DeferredRegister;

/** The NeoForge side of {@link Platform}: deferred registers on the mod bus and the NeoForge hooks. */
public class NeoForgePlatform implements Platform {
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

    @Override
    public String name() {
        return "neoforge";
    }

    @Override
    public <R, T extends R> Holder<R> register(ResourceKey<? extends Registry<R>> registry, String name, Supplier<T> factory) {
        return deferred(registry).register(name, factory);
    }

    @Override
    public CreativeModeTab creativeTab(Component title, Supplier<ItemStack> icon, BiConsumer<HolderLookup.Provider, Consumer<ItemStack>> contents) {
        // NeoForge makes CreativeModeTab.Output public
        return CreativeModeTab.builder().title(title).icon(icon).displayItems((params, output) -> contents.accept(params.holders(), output::accept)).build();
    }

    @Override
    @SuppressWarnings("unchecked")
    public Item spawnEgg(Supplier<? extends EntityType<? extends Mob>> type, int background, int highlight, Item.Properties properties) {
        // 26.x: entity types are registered before items; the egg colours are textures now (tools/convert_26.py)
        return new Item(properties.spawnEgg(type.get()));
    }

    @Override
    public Item kama(ToolMaterial tier, Item.Properties properties) {
        return new NeoForgeKamaItem(tier, properties);
    }

    @Override
    public Block taintedSoil(BlockBehaviour.Properties properties) {
        return new NeoForgeTaintedSoilBlock(properties);
    }

    @Override
    public <T extends BlockEntity> BlockEntityType<T> blockEntityType(BiFunction<BlockPos, BlockState, T> factory, Block... blocks) {
        return new BlockEntityType<>(factory::apply, blocks);
    }

    @Override
    public void setFlammable(Block block, int encouragement, int flammability) {
        ((com.progwml6.natura.mixin.FireBlockInvoker) Blocks.FIRE).natura$setFlammable(block, encouragement, flammability);
    }

    @Override
    public boolean canCropGrow(Level level, BlockPos pos, BlockState state, boolean grow) {
        return CommonHooks.canCropGrow(level, pos, state, grow);
    }

    @Override
    public void cropGrown(Level level, BlockPos pos, BlockState state) {
        CommonHooks.fireCropGrowPost(level, pos, state);
    }

    @Override
    public boolean isFakePlayer(Player player) {
        return player instanceof FakePlayer;
    }

    @Override
    public boolean isShears(ItemStack stack) {
        return stack.canPerformAction(ItemAbilities.SHEARS_DIG);
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
        NetworkRegistry.configureMockConnection(connection);
    }
}
