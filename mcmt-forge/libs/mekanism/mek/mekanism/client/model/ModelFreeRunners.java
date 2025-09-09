package mekanism.client.model;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import java.util.List;
import mekanism.common.Mekanism;
import mekanism.common.util.MekanismUtils;
import net.minecraft.client.model.geom.EntityModelSet;
import net.minecraft.client.model.geom.ModelLayerLocation;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.model.geom.PartPose;
import net.minecraft.client.model.geom.builders.CubeListBuilder;
import net.minecraft.client.model.geom.builders.LayerDefinition;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.resources.ResourceLocation;
import org.jetbrains.annotations.NotNull;

public class ModelFreeRunners extends MekanismJavaModel {
   public static final ModelLayerLocation FREE_RUNNER_LAYER = new ModelLayerLocation(Mekanism.rl("free_runners"), "main");
   private static final ResourceLocation FREE_RUNNER_TEXTURE = MekanismUtils.getResource(MekanismUtils.ResourceType.RENDER, "free_runners.png");
   protected static final ModelPartData SPRING_L = new ModelPartData(
      "SpringL", CubeListBuilder.m_171558_().m_171514_(8, 0).m_171481_(1.5F, 18.0F, 0.0F, 1.0F, 6.0F, 1.0F), PartPose.m_171430_(0.1047198F, 0.0F, 0.0F)
   );
   protected static final ModelPartData SPRING_R = new ModelPartData(
      "SpringR", CubeListBuilder.m_171558_().m_171514_(8, 0).m_171481_(-2.5F, 18.0F, 0.0F, 1.0F, 6.0F, 1.0F), PartPose.m_171430_(0.1047198F, 0.0F, 0.0F)
   );
   protected static final ModelPartData BRACE_L = new ModelPartData(
      "BraceL", CubeListBuilder.m_171558_().m_171514_(12, 0).m_171481_(0.2F, 18.0F, -0.8F, 4.0F, 2.0F, 3.0F)
   );
   protected static final ModelPartData BRACE_R = new ModelPartData(
      "BraceR", CubeListBuilder.m_171558_().m_171514_(12, 0).m_171481_(-4.2F, 18.0F, -0.8F, 4.0F, 2.0F, 3.0F)
   );
   protected static final ModelPartData SUPPORT_L = new ModelPartData(
      "SupportL", CubeListBuilder.m_171558_().m_171481_(1.0F, 16.5F, -4.2F, 2.0F, 4.0F, 2.0F), PartPose.m_171430_(0.296706F, 0.0F, 0.0F)
   );
   protected static final ModelPartData SUPPORT_R = new ModelPartData(
      "SupportR", CubeListBuilder.m_171558_().m_171481_(-3.0F, 16.5F, -4.2F, 2.0F, 4.0F, 2.0F), PartPose.m_171430_(0.296706F, 0.0F, 0.0F)
   );
   private final RenderType RENDER_TYPE = this.m_103119_(FREE_RUNNER_TEXTURE);
   protected final List<ModelPart> leftParts;
   protected final List<ModelPart> rightParts;

   public static LayerDefinition createLayerDefinition() {
      return createLayerDefinition(64, 32, new ModelPartData[]{SPRING_L, SPRING_R, BRACE_L, BRACE_R, SUPPORT_L, SUPPORT_R});
   }

   public ModelFreeRunners(EntityModelSet entityModelSet) {
      this(entityModelSet.m_171103_(FREE_RUNNER_LAYER));
   }

   protected ModelFreeRunners(ModelPart root) {
      super(RenderType::m_110446_);
      this.leftParts = getRenderableParts(root, new ModelPartData[]{SPRING_L, BRACE_L, SUPPORT_L});
      this.rightParts = getRenderableParts(root, new ModelPartData[]{SPRING_R, BRACE_R, SUPPORT_R});
   }

   public void render(@NotNull PoseStack matrix, @NotNull MultiBufferSource renderer, int light, int overlayLight, boolean hasEffect) {
      this.m_7695_(matrix, getVertexConsumer(renderer, this.RENDER_TYPE, hasEffect), light, overlayLight, 1.0F, 1.0F, 1.0F, 1.0F);
   }

   public void m_7695_(
      @NotNull PoseStack poseStack, @NotNull VertexConsumer vertexConsumer, int light, int overlayLight, float red, float green, float blue, float alpha
   ) {
      this.renderLeg(poseStack, vertexConsumer, light, overlayLight, red, green, blue, alpha, true);
      this.renderLeg(poseStack, vertexConsumer, light, overlayLight, red, green, blue, alpha, false);
   }

   public void renderLeg(@NotNull PoseStack poseStack, @NotNull MultiBufferSource renderer, int light, int overlayLight, boolean hasEffect, boolean left) {
      this.renderLeg(poseStack, getVertexConsumer(renderer, this.RENDER_TYPE, hasEffect), light, overlayLight, 1.0F, 1.0F, 1.0F, 1.0F, left);
   }

   protected void renderLeg(
      @NotNull PoseStack poseStack,
      @NotNull VertexConsumer vertexConsumer,
      int light,
      int overlayLight,
      float red,
      float green,
      float blue,
      float alpha,
      boolean left
   ) {
      if (left) {
         renderPartsToBuffer(this.leftParts, poseStack, vertexConsumer, light, overlayLight, red, green, blue, alpha);
      } else {
         renderPartsToBuffer(this.rightParts, poseStack, vertexConsumer, light, overlayLight, red, green, blue, alpha);
      }
   }
}
