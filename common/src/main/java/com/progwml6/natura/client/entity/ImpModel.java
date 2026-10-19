package com.progwml6.natura.client.entity;

import com.progwml6.natura.entities.ImpEntity;

import net.minecraft.client.model.HierarchicalModel;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.model.geom.PartPose;
import net.minecraft.client.model.geom.builders.CubeListBuilder;
import net.minecraft.client.model.geom.builders.LayerDefinition;
import net.minecraft.client.model.geom.builders.MeshDefinition;
import net.minecraft.client.model.geom.builders.PartDefinition;
import net.minecraft.util.Mth;

/** 1.12 RenderNaturaImp.ModelImp: body, head, two legs and two arms, with the 1.12 boxes and swing. */
public class ImpModel extends HierarchicalModel<ImpEntity> {
    private final ModelPart root;
    private final ModelPart body, head, leftLeg, rightLeg, leftArm, rightArm;

    public ImpModel(ModelPart root) {
        this.root = root;
        this.body = root.getChild("body");
        this.head = root.getChild("head");
        this.leftLeg = root.getChild("leg0");
        this.rightLeg = root.getChild("leg1");
        this.leftArm = root.getChild("arm0");
        this.rightArm = root.getChild("arm1");
    }

    public static LayerDefinition createLayer() {
        MeshDefinition mesh = new MeshDefinition();
        PartDefinition parts = mesh.getRoot();
        parts.addOrReplaceChild("body", CubeListBuilder.create().texOffs(16, 16).addBox(-5F, 0.0F, -18.5F, 10, 5, 9), PartPose.ZERO);
        parts.addOrReplaceChild("head", CubeListBuilder.create().texOffs(0, 0).addBox(-5F, -6.5F, -4F, 10, 8, 8), PartPose.offset(0.0F, 8.0F, 2.0F));
        parts.addOrReplaceChild("leg0", CubeListBuilder.create().texOffs(0, 16).addBox(-2F, 0.0F, -2F, 4, 6, 4), PartPose.offset(-3F, 18F, 2.0F));
        parts.addOrReplaceChild("leg1", CubeListBuilder.create().texOffs(0, 16).addBox(-2F, 0.0F, -2F, 4, 6, 4), PartPose.offset(3F, 18F, 2.0F));
        parts.addOrReplaceChild("arm0", CubeListBuilder.create().texOffs(32, 0).addBox(-1.5F, 0.0F, 1.0F, 2, 5, 2), PartPose.offset(6F, 10F, 0.0F));
        parts.addOrReplaceChild("arm1", CubeListBuilder.create().texOffs(32, 0).addBox(-0.5F, 0.0F, 1.0F, 2, 5, 2), PartPose.offset(-6F, 10F, 0.0F));
        return LayerDefinition.create(mesh, 64, 32);
    }

    @Override
    public ModelPart root() {
        return this.root;
    }

    @Override
    public void setupAnim(ImpEntity entity, float limbSwing, float limbSwingAmount, float ageInTicks, float netHeadYaw, float headPitch) {
        this.body.xRot = 1.570796F;
        this.head.xRot = headPitch / -57.29578F;
        this.head.yRot = netHeadYaw / 57.29578F;
        this.leftArm.xRot = Mth.cos(limbSwing * 0.6662F) * 1.4F * limbSwingAmount;
        this.rightArm.xRot = Mth.cos(limbSwing * 0.6662F + 3.141593F) * 1.4F * limbSwingAmount;
        this.leftLeg.xRot = Mth.cos(limbSwing * 0.6662F) * 1.4F * limbSwingAmount;
        this.rightLeg.xRot = Mth.cos(limbSwing * 0.6662F + 3.141593F) * 1.4F * limbSwingAmount;
    }
}
