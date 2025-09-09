package mekanism.client.model;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import java.util.List;
import mekanism.client.render.MekanismRenderType;
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

public class ModelScubaMask extends MekanismJavaModel {
   public static final ModelLayerLocation MASK_LAYER = new ModelLayerLocation(Mekanism.rl("scuba_mask"), "main");
   private static final ResourceLocation MASK_TEXTURE = MekanismUtils.getResource(MekanismUtils.ResourceType.RENDER, "scuba_set.png");
   private static final ModelPartData HELMET_FEED = new ModelPartData(
      "helmetFeed", CubeListBuilder.m_171558_().m_171514_(88, 43).m_171481_(-2.0F, -2.0F, 2.0F, 4.0F, 3.0F, 4.0F)
   );
   private static final ModelPartData TUBE_BACK = new ModelPartData(
      "tubeBack", CubeListBuilder.m_171558_().m_171514_(106, 50).m_171481_(-4.5F, -1.0F, 4.5F, 9.0F, 1.0F, 1.0F)
   );
   private static final ModelPartData TUBE_L = new ModelPartData(
      "tubeL", CubeListBuilder.m_171558_().m_171514_(106, 54).m_171481_(4.5F, -1.0F, -4.5F, 1.0F, 1.0F, 9.0F)
   );
   private static final ModelPartData TUBE_R = new ModelPartData(
      "tubeR", CubeListBuilder.m_171558_().m_171514_(106, 54).m_171481_(-5.5F, -1.0F, -4.5F, 1.0F, 1.0F, 9.0F)
   );
   private static final ModelPartData TUBE_FRONT = new ModelPartData(
      "tubeFront", CubeListBuilder.m_171558_().m_171514_(106, 50).m_171481_(-4.5F, -1.0F, -5.5F, 9.0F, 1.0F, 1.0F)
   );
   private static final ModelPartData MOUTH_INTAKE = new ModelPartData(
      "mouthIntake",
      CubeListBuilder.m_171558_().m_171514_(118, 42).m_171481_(-1.5F, -0.7F, -6.0F, 3.0F, 2.0F, 3.0F),
      PartPose.m_171423_(0.0F, -2.0F, 0.0F, 0.2094395F, 0.0F, 0.0F)
   );
   private static final ModelPartData FIN_UPPER_R = new ModelPartData(
      "finUpperR", CubeListBuilder.m_171558_().m_171514_(78, 50).m_171481_(-6.0F, -7.5F, -3.3F, 1.0F, 2.0F, 12.0F), PartPose.m_171430_(0.0698132F, 0.0F, 0.0F)
   );
   private static final ModelPartData FIN_UPPER_L = new ModelPartData(
      "finUpperL", CubeListBuilder.m_171558_().m_171514_(78, 50).m_171481_(5.0F, -7.5F, -3.3F, 1.0F, 2.0F, 12.0F), PartPose.m_171430_(0.0698132F, 0.0F, 0.0F)
   );
   private static final ModelPartData FIN_MID_R = new ModelPartData(
      "finMidR", CubeListBuilder.m_171558_().m_171514_(72, 34).m_171481_(-7.5F, -6.0F, -1.0F, 2.0F, 2.0F, 5.0F)
   );
   private static final ModelPartData FIN_MID_L = new ModelPartData(
      "finMidL", CubeListBuilder.m_171558_().m_171514_(72, 34).m_171481_(5.5F, -6.0F, -1.0F, 2.0F, 2.0F, 5.0F)
   );
   private static final ModelPartData FIN_BACK = new ModelPartData(
      "finBack", CubeListBuilder.m_171558_().m_171514_(80, 0).m_171481_(-1.0F, -9.6F, 2.5F, 2.0F, 10.0F, 3.0F)
   );
   private static final ModelPartData TOP_PLATE = new ModelPartData(
      "topPlate", CubeListBuilder.m_171558_().m_171514_(104, 34).m_171481_(-3.0F, -10.0F, -2.0F, 6.0F, 2.0F, 6.0F), PartPose.m_171430_(0.1396263F, 0.0F, 0.0F)
   );
   private static final ModelPartData FILTER_L = new ModelPartData(
      "filterL",
      CubeListBuilder.m_171558_().m_171514_(108, 42).m_171481_(3.4F, -1.8F, -5.0F, 2.0F, 3.0F, 3.0F),
      PartPose.m_171430_(0.0F, 0.3839724F, 0.5061455F)
   );
   private static final ModelPartData FILTER_R = new ModelPartData(
      "filterR",
      CubeListBuilder.m_171558_().m_171514_(108, 42).m_171481_(-5.4F, -1.8F, -5.0F, 2.0F, 3.0F, 3.0F),
      PartPose.m_171430_(0.0F, -0.3839724F, -0.5061455F)
   );
   private static final ModelPartData FILTER_PIPE_LOWER = new ModelPartData(
      "filterPipeLower", CubeListBuilder.m_171558_().m_171514_(92, 41).m_171481_(-3.0F, 1.0F, -5.0F, 5.0F, 1.0F, 1.0F)
   );
   private static final ModelPartData FILTER_PIPE_UPPER = new ModelPartData(
      "filterPipeUpper", CubeListBuilder.m_171558_().m_171514_(104, 42).m_171481_(-0.5F, 0.0F, -5.0F, 1.0F, 1.0F, 1.0F)
   );
   private static final ModelPartData GLASS_TOP = new ModelPartData("glassTop", CubeListBuilder.m_171558_().m_171481_(-4.0F, -9.0F, -4.0F, 8.0F, 1.0F, 8.0F));
   private static final ModelPartData GLASS_FRONT = new ModelPartData(
      "glassFront", CubeListBuilder.m_171558_().m_171481_(-4.0F, -8.0F, -5.0F, 8.0F, 7.0F, 1.0F)
   );
   private static final ModelPartData GLASS_R = new ModelPartData("glassR", CubeListBuilder.m_171558_().m_171481_(-5.0F, -8.0F, -4.0F, 1.0F, 7.0F, 8.0F));
   private static final ModelPartData GLASS_L = new ModelPartData("glassL", CubeListBuilder.m_171558_().m_171481_(4.0F, -8.0F, -4.0F, 1.0F, 7.0F, 8.0F));
   private static final ModelPartData GLASS_BACK_R = new ModelPartData(
      "glassBackR", CubeListBuilder.m_171558_().m_171481_(-4.0F, -8.0F, 4.0F, 3.0F, 7.0F, 1.0F)
   );
   private static final ModelPartData GLASS_BACK_L = new ModelPartData("glassBackL", CubeListBuilder.m_171558_().m_171481_(1.0F, -8.0F, 4.0F, 3.0F, 7.0F, 1.0F));
   private static final ModelPartData PIPE_CORNER_F_L = new ModelPartData(
      "pipeCornerFL", CubeListBuilder.m_171558_().m_171514_(109, 50).m_171481_(3.5F, -1.0F, -4.5F, 1.0F, 1.0F, 1.0F)
   );
   private static final ModelPartData PIPE_CORNER_F_R = new ModelPartData(
      "pipeCornerFR", CubeListBuilder.m_171558_().m_171514_(109, 50).m_171481_(-4.5F, -1.0F, -4.5F, 1.0F, 1.0F, 1.0F)
   );
   private static final ModelPartData PIPE_CORNER_B_R = new ModelPartData(
      "pipeCornerBR", CubeListBuilder.m_171558_().m_171514_(109, 50).m_171481_(-4.5F, -1.0F, 3.5F, 1.0F, 1.0F, 1.0F)
   );
   private static final ModelPartData PIPE_CORNER_B_L = new ModelPartData(
      "pipeCornerBL", CubeListBuilder.m_171558_().m_171514_(109, 50).m_171481_(3.5F, -1.0F, 4.5F, 1.0F, 1.0F, 1.0F), PartPose.m_171419_(0.0F, 0.0F, -1.0F)
   );
   private static final ModelPartData LIGHT_L = new ModelPartData(
      "lightL", CubeListBuilder.m_171558_().m_171514_(89, 37).m_171481_(5.5F, -6.0F, -2.0F, 2.0F, 2.0F, 1.0F)
   );
   private static final ModelPartData LIGHT_R = new ModelPartData(
      "lightR", CubeListBuilder.m_171558_().m_171514_(89, 37).m_171481_(-7.5F, -6.0F, -2.0F, 2.0F, 2.0F, 1.0F)
   );
   private final RenderType GLASS_RENDER_TYPE = MekanismRenderType.STANDARD.apply(MASK_TEXTURE);
   private final RenderType RENDER_TYPE = this.m_103119_(MASK_TEXTURE);
   private final List<ModelPart> parts;
   private final List<ModelPart> litParts;
   private final List<ModelPart> glass;

   public static LayerDefinition createLayerDefinition() {
      return createLayerDefinition(
         128,
         64,
         new ModelPartData[]{
            HELMET_FEED,
            TUBE_BACK,
            TUBE_L,
            TUBE_R,
            TUBE_FRONT,
            MOUTH_INTAKE,
            FIN_UPPER_R,
            FIN_UPPER_L,
            FIN_MID_R,
            FIN_MID_L,
            FIN_BACK,
            TOP_PLATE,
            FILTER_L,
            FILTER_R,
            FILTER_PIPE_LOWER,
            FILTER_PIPE_UPPER,
            GLASS_TOP,
            GLASS_FRONT,
            GLASS_R,
            GLASS_L,
            GLASS_BACK_R,
            GLASS_BACK_L,
            PIPE_CORNER_F_L,
            PIPE_CORNER_F_R,
            PIPE_CORNER_B_R,
            PIPE_CORNER_B_L,
            LIGHT_L,
            LIGHT_R
         }
      );
   }

   public ModelScubaMask(EntityModelSet entityModelSet) {
      super(RenderType::m_110446_);
      ModelPart root = entityModelSet.m_171103_(MASK_LAYER);
      this.parts = getRenderableParts(
         root,
         new ModelPartData[]{
            HELMET_FEED,
            TUBE_BACK,
            TUBE_L,
            TUBE_R,
            TUBE_FRONT,
            MOUTH_INTAKE,
            FIN_UPPER_R,
            FIN_UPPER_L,
            FIN_MID_R,
            FIN_MID_L,
            FIN_BACK,
            TOP_PLATE,
            FILTER_L,
            FILTER_R,
            FILTER_PIPE_LOWER,
            FILTER_PIPE_UPPER,
            PIPE_CORNER_F_L,
            PIPE_CORNER_F_R,
            PIPE_CORNER_B_R,
            PIPE_CORNER_B_L
         }
      );
      this.litParts = getRenderableParts(root, new ModelPartData[]{LIGHT_L, LIGHT_R});
      this.glass = getRenderableParts(root, new ModelPartData[]{GLASS_TOP, GLASS_FRONT, GLASS_R, GLASS_L, GLASS_BACK_R, GLASS_BACK_L});
   }

   public void render(@NotNull PoseStack matrix, @NotNull MultiBufferSource renderer, int light, int overlayLight, boolean hasEffect) {
      this.m_7695_(matrix, getVertexConsumer(renderer, this.RENDER_TYPE, hasEffect), light, overlayLight, 1.0F, 1.0F, 1.0F, 1.0F);
      renderPartsToBuffer(this.glass, matrix, getVertexConsumer(renderer, this.GLASS_RENDER_TYPE, hasEffect), 15728880, overlayLight, 1.0F, 1.0F, 1.0F, 0.3F);
   }

   public void m_7695_(
      @NotNull PoseStack poseStack, @NotNull VertexConsumer vertexConsumer, int light, int overlayLight, float red, float green, float blue, float alpha
   ) {
      renderPartsToBuffer(this.parts, poseStack, vertexConsumer, light, overlayLight, red, green, blue, alpha);
      renderPartsToBuffer(this.litParts, poseStack, vertexConsumer, 15728880, overlayLight, red, green, blue, alpha);
   }
}
