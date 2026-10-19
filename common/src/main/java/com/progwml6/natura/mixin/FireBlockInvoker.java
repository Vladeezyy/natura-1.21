package com.progwml6.natura.mixin;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Invoker;

import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.FireBlock;

/** FireBlock.setFlammable is private (NeoForge 26.x no longer opens it; Fabric uses its FlammableBlockRegistry). */
@Mixin(FireBlock.class)
public interface FireBlockInvoker {
    @Invoker("setFlammable")
    void natura$setFlammable(Block block, int igniteOdds, int burnOdds);
}
