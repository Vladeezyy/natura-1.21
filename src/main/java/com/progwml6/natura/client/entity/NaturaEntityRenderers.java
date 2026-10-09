package com.progwml6.natura.client.entity;

import com.mojang.blaze3d.vertex.PoseStack;
import com.progwml6.natura.Natura;
import com.progwml6.natura.entities.HeatscarSpiderEntity;
import com.progwml6.natura.entities.ImpEntity;
import com.progwml6.natura.entities.NaturaEntities;

import net.minecraft.client.model.geom.ModelLayerLocation;
import net.minecraft.client.renderer.entity.CreeperRenderer;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.MobRenderer;
import net.minecraft.client.renderer.entity.SpiderRenderer;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.monster.Creeper;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.EntityRenderersEvent;

/** The 1.12 renderers: the imp model, the vanilla spider (x2, baby x0.85) and creeper with the Natura textures. */
@EventBusSubscriber(modid = Natura.MODID, bus = EventBusSubscriber.Bus.MOD, value = Dist.CLIENT)
public final class NaturaEntityRenderers {
    public static final ModelLayerLocation IMP = new ModelLayerLocation(ResourceLocation.fromNamespaceAndPath(Natura.MODID, "imp"), "main");
    private static final ResourceLocation IMP_TEXTURE = ResourceLocation.fromNamespaceAndPath(Natura.MODID, "textures/entity/imp.png");
    private static final ResourceLocation SPIDER_TEXTURE = ResourceLocation.fromNamespaceAndPath(Natura.MODID, "textures/entity/heatscarspider.png");
    private static final ResourceLocation CREEPER_TEXTURE = ResourceLocation.fromNamespaceAndPath(Natura.MODID, "textures/entity/nitrocreeper.png");

    private NaturaEntityRenderers() {}

    @SubscribeEvent
    static void layers(EntityRenderersEvent.RegisterLayerDefinitions event) {
        event.registerLayerDefinition(IMP, ImpModel::createLayer);
    }

    @SubscribeEvent
    static void renderers(EntityRenderersEvent.RegisterRenderers event) {
        event.registerEntityRenderer(NaturaEntities.IMP.get(), ctx -> new MobRenderer<ImpEntity, ImpModel>(ctx, new ImpModel(ctx.bakeLayer(IMP)), 0.0F) {
            @Override
            public ResourceLocation getTextureLocation(ImpEntity entity) {
                return IMP_TEXTURE;
            }
        });
        event.registerEntityRenderer(NaturaEntities.HEATSCAR_SPIDER.get(), ctx -> spider(ctx, 2.0F));
        event.registerEntityRenderer(NaturaEntities.BABY_HEATSCAR_SPIDER.get(), ctx -> spider(ctx, 0.85F));
        event.registerEntityRenderer(NaturaEntities.NITRO_CREEPER.get(), ctx -> new CreeperRenderer(ctx) {
            @Override
            public ResourceLocation getTextureLocation(Creeper entity) {
                return CREEPER_TEXTURE;
            }
        });
    }

    private static SpiderRenderer<HeatscarSpiderEntity> spider(EntityRendererProvider.Context ctx, float scale) {
        return new SpiderRenderer<>(ctx) {
            @Override
            public ResourceLocation getTextureLocation(HeatscarSpiderEntity entity) {
                return SPIDER_TEXTURE;
            }

            @Override
            protected void scale(HeatscarSpiderEntity entity, PoseStack pose, float partialTick) {
                pose.scale(scale, scale, scale);
            }
        };
    }
}
