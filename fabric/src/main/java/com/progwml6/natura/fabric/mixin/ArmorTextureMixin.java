package com.progwml6.natura.fabric.mixin;

import java.util.HashMap;
import java.util.Map;

import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import net.minecraft.client.renderer.entity.layers.HumanoidArmorLayer;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ArmorItem;

/**
 * Armor materials named "mod:name" (the imp armor: natura:imp) have their layers at mod:textures/models/armor/name_layer_N
 * (Forge reads the namespace itself; vanilla 1.20.1 would build "textures/models/armor/natura:imp_layer_1.png" and fail).
 */
@Mixin(HumanoidArmorLayer.class)
public abstract class ArmorTextureMixin {
    @Unique
    private static final Map<String, ResourceLocation> NATURA_LOCATIONS = new HashMap<>();

    @Inject(method = "getArmorLocation", at = @At("HEAD"), cancellable = true)
    private void natura$namespacedMaterial(ArmorItem item, boolean layer2, @Nullable String suffix, CallbackInfoReturnable<ResourceLocation> cir) {
        String name = item.getMaterial().getName();
        int colon = name.indexOf(':');
        if (colon < 0) {
            return;
        }
        String path = "textures/models/armor/" + name.substring(colon + 1) + "_layer_" + (layer2 ? 2 : 1) + (suffix == null ? "" : "_" + suffix) + ".png";
        cir.setReturnValue(NATURA_LOCATIONS.computeIfAbsent(name.substring(0, colon) + ":" + path, ResourceLocation::new));
    }
}
