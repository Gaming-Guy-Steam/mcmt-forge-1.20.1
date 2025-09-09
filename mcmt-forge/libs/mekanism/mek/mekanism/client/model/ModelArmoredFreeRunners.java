package mekanism.client.model;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import java.util.List;
import mekanism.common.Mekanism;
import net.minecraft.client.model.geom.EntityModelSet;
import net.minecraft.client.model.geom.ModelLayerLocation;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.model.geom.PartPose;
import net.minecraft.client.model.geom.builders.CubeListBuilder;
import net.minecraft.client.model.geom.builders.LayerDefinition;
import org.jetbrains.annotations.NotNull;

public class ModelArmoredFreeRunners extends ModelFreeRunners {
   public static final ModelLayerLocation ARMORED_FREE_RUNNER_LAYER = new ModelLayerLocation(Mekanism.rl("armored_free_runners"), "main");
   private static final ModelPartData PLATE_L = new ModelPartData(
      "PlateL",
      CubeListBuilder.m_171558_()
         .m_171480_()
         .m_171514_(0, 11)
         .m_171481_(0.5F, 21.0F, -3.0F, 3.0F, 2.0F, 1.0F)
         .m_171514_(0, 7)
         .m_171481_(0.5F, 17.0F, -3.0F, 3.0F, 1.0F, 1.0F)
   );
   private static final ModelPartData PLATE_R = new ModelPartData(
      "PlateR",
      CubeListBuilder.m_171558_()
         .m_171514_(0, 11)
         .m_171481_(-3.5F, 21.0F, -3.0F, 3.0F, 2.0F, 1.0F)
         .m_171514_(0, 7)
         .m_171481_(-3.5F, 17.0F, -3.0F, 3.0F, 1.0F, 1.0F)
   );
   private static final ModelPartData TOP_PLATE_L = new ModelPartData(
      "TopPlateL",
      CubeListBuilder.m_171558_().m_171480_().m_171514_(12, 7).m_171481_(0.0F, 0.0F, -0.25F, 2.0F, 2.0F, 1.0F),
      PartPose.m_171423_(1.0F, 16.0F, -2.0F, -0.7854F, 0.0F, 0.0F)
   );
   private static final ModelPartData TOP_PLATE_R = new ModelPartData(
      "TopPlateR",
      CubeListBuilder.m_171558_().m_171514_(12, 7).m_171481_(-2.0F, 0.0F, -0.25F, 2.0F, 2.0F, 1.0F),
      PartPose.m_171423_(-1.0F, 16.0F, -2.0F, -0.7854F, 0.0F, 0.0F)
   );
   private static final ModelPartData CONNECTION_L = new ModelPartData(
      "ConnectionL",
      CubeListBuilder.m_171558_()
         .m_171480_()
         .m_171514_(8, 7)
         .m_171481_(2.5F, 18.0F, -3.0F, 1.0F, 3.0F, 1.0F)
         .m_171514_(8, 7)
         .m_171481_(0.5F, 18.0F, -3.0F, 1.0F, 3.0F, 1.0F)
   );
   private static final ModelPartData CONNECTION_R = new ModelPartData(
      "ConnectionR",
      CubeListBuilder.m_171558_()
         .m_171514_(8, 7)
         .m_171481_(-1.5F, 18.0F, -3.0F, 1.0F, 3.0F, 1.0F)
         .m_171514_(8, 7)
         .m_171481_(-3.5F, 18.0F, -3.0F, 1.0F, 3.0F, 1.0F)
   );
   private static final ModelPartData ARMORED_BRACE_L = new ModelPartData(
      "ArmoredBraceL",
      CubeListBuilder.m_171558_()
         .m_171514_(10, 12)
         .m_171481_(0.2F, 17.0F, -2.3F, 4.0F, 1.0F, 1.0F)
         .m_171514_(8, 10)
         .m_171481_(0.2F, 21.0F, -2.3F, 4.0F, 1.0F, 3.0F)
   );
   private static final ModelPartData ARMORED_BRACE_R = new ModelPartData(
      "ArmoredBraceR",
      CubeListBuilder.m_171558_()
         .m_171480_()
         .m_171514_(10, 12)
         .m_171481_(-4.2F, 17.0F, -2.3F, 4.0F, 1.0F, 1.0F)
         .m_171514_(8, 10)
         .m_171481_(-4.2F, 21.0F, -2.3F, 4.0F, 1.0F, 3.0F)
   );
   private static final ModelPartData BATTERY_L = new ModelPartData(
      "BatteryL", CubeListBuilder.m_171558_().m_171514_(22, 11).m_171481_(1.5F, 18.0F, -3.0F, 1.0F, 2.0F, 1.0F)
   );
   private static final ModelPartData BATTERY_R = new ModelPartData(
      "BatteryR", CubeListBuilder.m_171558_().m_171514_(22, 11).m_171481_(-2.5F, 18.0F, -3.0F, 1.0F, 2.0F, 1.0F)
   );
   private final List<ModelPart> litLeftParts;
   private final List<ModelPart> litRightParts;

   public static LayerDefinition createLayerDefinition() {
      return createLayerDefinition(
         64,
         32,
         new ModelPartData[]{
            SPRING_L,
            SPRING_R,
            BRACE_L,
            BRACE_R,
            SUPPORT_L,
            SUPPORT_R,
            PLATE_L,
            PLATE_R,
            TOP_PLATE_L,
            TOP_PLATE_R,
            CONNECTION_L,
            CONNECTION_R,
            ARMORED_BRACE_L,
            ARMORED_BRACE_R,
            BATTERY_L,
            BATTERY_R
         }
      );
   }

   public ModelArmoredFreeRunners(EntityModelSet entityModelSet) {
      this(entityModelSet.m_171103_(ARMORED_FREE_RUNNER_LAYER));
   }

   private ModelArmoredFreeRunners(ModelPart root) {
      super(root);
      this.leftParts.addAll(getRenderableParts(root, new ModelPartData[]{PLATE_L, TOP_PLATE_L, CONNECTION_L, ARMORED_BRACE_L}));
      this.rightParts.addAll(getRenderableParts(root, new ModelPartData[]{PLATE_R, TOP_PLATE_R, CONNECTION_R, ARMORED_BRACE_R}));
      this.litLeftParts = getRenderableParts(root, new ModelPartData[]{BATTERY_L});
      this.litRightParts = getRenderableParts(root, new ModelPartData[]{BATTERY_R});
   }

   @Override
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
      super.renderLeg(poseStack, vertexConsumer, light, overlayLight, red, green, blue, alpha, left);
      if (left) {
         renderPartsToBuffer(this.litLeftParts, poseStack, vertexConsumer, 15728880, overlayLight, red, green, blue, alpha);
      } else {
         renderPartsToBuffer(this.litRightParts, poseStack, vertexConsumer, 15728880, overlayLight, red, green, blue, alpha);
      }
   }
}
