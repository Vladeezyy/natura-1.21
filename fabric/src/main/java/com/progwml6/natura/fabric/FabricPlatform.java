package com.progwml6.natura.fabric;

import net.fabricmc.fabric.api.creativetab.v1.FabricCreativeModeTab;

import net.minecraft.world.item.ToolMaterial;

import net.minecraft.network.chat.Component;

import net.minecraft.core.HolderLookup;

import java.util.function.Consumer;

import java.util.function.BiConsumer;

import java.util.function.BiFunction;
import java.util.function.Supplier;

import com.progwml6.natura.Natura;
import com.progwml6.natura.common.block.TaintedSoilBlock;
import com.progwml6.natura.platform.Platform;
import com.progwml6.natura.tools.KamaItem;

import net.fabricmc.fabric.api.entity.FakePlayer;
import net.fabricmc.fabric.api.object.builder.v1.block.entity.FabricBlockEntityTypeBuilder;
import net.fabricmc.fabric.api.registry.FlammableBlockRegistry;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Holder;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.Connection;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.Identifier;
import net.minecraft.tags.TagKey;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.BoneMealItem;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;

/** The Fabric side of {@link Platform}: entries registered right away, Fabric API registries, vanilla behaviour for the NeoForge hooks. */
public class FabricPlatform implements Platform {
    /** c:tools/shear: shears and the kamas (NeoForge: the shears_dig item ability). */
    private static final TagKey<Item> SHEARS = TagKey.create(Registries.ITEM, Identifier.fromNamespaceAndPath("c", "tools/shear"));

    @Override
    public String name() {
        return "fabric";
    }

    @Override
    @SuppressWarnings("unchecked")
    public <R, T extends R> Holder<R> register(ResourceKey<? extends Registry<R>> registry, String name, Supplier<T> factory) {
        Registry<R> target = (Registry<R>) BuiltInRegistries.REGISTRY.getValue(registry.identifier());
        return Registry.registerForHolder(target, Identifier.fromNamespaceAndPath(Natura.MODID, name), factory.get());
    }

    @Override
    public CreativeModeTab creativeTab(Component title, Supplier<ItemStack> icon, BiConsumer<HolderLookup.Provider, Consumer<ItemStack>> contents) {
        // Fabric's transitive access wideners make CreativeModeTab.Output public
        return FabricCreativeModeTab.builder().title(title).icon(icon).displayItems((params, output) -> contents.accept(params.holders(), output::accept)).build();
    }

    @Override
    @SuppressWarnings("unchecked")
    public Item spawnEgg(Supplier<? extends EntityType<? extends Mob>> type, int background, int highlight, Item.Properties properties) {
        // the egg colours are textures now (tools/convert_26.py)
        return new Item(properties.spawnEgg(type.get()));
    }

    @Override
    public Item kama(ToolMaterial tier, Item.Properties properties) {
        return new KamaItem(tier, properties);
    }

    @Override
    public Block taintedSoil(BlockBehaviour.Properties properties) {
        return new TaintedSoilBlock(properties);
    }

    @Override
    public <T extends BlockEntity> BlockEntityType<T> blockEntityType(BiFunction<BlockPos, BlockState, T> factory, Block... blocks) {
        return FabricBlockEntityTypeBuilder.create(factory::apply, blocks).build();
    }

    @Override
    public void setFlammable(Block block, int encouragement, int flammability) {
        // Fabric "burn" / "spread" are the vanilla setFlammable encouragement / flammability, in that order
        FlammableBlockRegistry.getDefaultInstance().add(block, encouragement, flammability);
    }

    @Override
    public boolean canCropGrow(Level level, BlockPos pos, BlockState state, boolean grow) {
        return grow;
    }

    @Override
    public void cropGrown(Level level, BlockPos pos, BlockState state) {}

    @Override
    public boolean isFakePlayer(Player player) {
        return player instanceof FakePlayer;
    }

    @Override
    public boolean isShears(ItemStack stack) {
        return stack.is(Items.SHEARS) || stack.is(SHEARS);
    }

    @Override
    public boolean applyBonemeal(ItemStack stack, Level level, BlockPos pos, Player player) {
        return BoneMealItem.growCrop(stack, level, pos);
    }

    @Override
    public float downfall(Biome biome) {
        return biome.climateSettings.downfall();
    }

    @Override
    public void configureMockConnection(Connection connection) {
        // Fabric sends to any connection
    }
}
