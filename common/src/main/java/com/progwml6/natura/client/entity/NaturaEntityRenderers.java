package com.progwml6.natura.client.entity;

import java.util.function.BiConsumer;
import java.util.function.Supplier;

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
import net.minecraft.client.renderer.entity.state.CreeperRenderState;
import net.minecraft.client.renderer.entity.state.LivingEntityRenderState;
import net.minecraft.resources.Identifier;
import net.minecraft.client.model.geom.builders.LayerDefinition;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;

/** The 1.12 renderers: the imp model, the vanilla spider (x2, baby x0.85) and creeper with the Natura textures. */
public final class NaturaEntityRenderers {
    public static final ModelLayerLocation IMP = new ModelLayerLocation(Identifier.fromNamespaceAndPath(Natura.MODID, "imp"), "main");
    private static final Identifier IMP_TEXTURE = Identifier.fromNamespaceAndPath(Natura.MODID, "textures/entity/imp.png");
    private static final Identifier SPIDER_TEXTURE = Identifier.fromNamespaceAndPath(Natura.MODID, "textures/entity/heatscarspider.png");
    private static final Identifier CREEPER_TEXTURE = Identifier.fromNamespaceAndPath(Natura.MODID, "textures/entity/nitrocreeper.png");

    private NaturaEntityRenderers() {}

    public static void layers(BiConsumer<ModelLayerLocation, Supplier<LayerDefinition>> event) {
        event.accept(IMP, ImpModel::createLayer);
    }

    public static void renderers(Renderers event) {
        event.register(NaturaEntities.IMP.get(), ctx -> new MobRenderer<ImpEntity, LivingEntityRenderState, ImpModel>(ctx, new ImpModel(ctx.bakeLayer(IMP)), 0.0F) {
            @Override
            public Identifier getTextureLocation(LivingEntityRenderState state) {
                return IMP_TEXTURE;
            }

            @Override
            public LivingEntityRenderState createRenderState() {
                return new LivingEntityRenderState();
            }
        });
        event.register(NaturaEntities.HEATSCAR_SPIDER.get(), ctx -> spider(ctx, 2.0F));
        event.register(NaturaEntities.BABY_HEATSCAR_SPIDER.get(), ctx -> spider(ctx, 0.85F));
        event.register(NaturaEntities.NITRO_CREEPER.get(), ctx -> new CreeperRenderer(ctx) {
            @Override
            public Identifier getTextureLocation(CreeperRenderState state) {
                return CREEPER_TEXTURE;
            }
        });
    }

    public interface Renderers {
        <E extends Entity> void register(EntityType<? extends E> type, EntityRendererProvider<E> provider);
    }

    private static SpiderRenderer<HeatscarSpiderEntity> spider(EntityRendererProvider.Context ctx, float scale) {
        return new SpiderRenderer<>(ctx) {
            @Override
            public Identifier getTextureLocation(LivingEntityRenderState state) {
                return SPIDER_TEXTURE;
            }

            @Override
            protected void scale(LivingEntityRenderState state, PoseStack pose) {
                pose.scale(scale, scale, scale);
            }
        };
    }
}
