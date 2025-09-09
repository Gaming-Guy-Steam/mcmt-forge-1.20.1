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

public class ModelScubaTank extends MekanismJavaModel {
   public static final ModelLayerLocation TANK_LAYER = new ModelLayerLocation(Mekanism.rl("scuba_tank"), "main");
   private static final ResourceLocation TANK_TEXTURE = MekanismUtils.getResource(MekanismUtils.ResourceType.RENDER, "scuba_set.png");
   private static final ModelPartData TANK_L = new ModelPartData(
      "tankL",
      CubeListBuilder.m_171558_().m_171514_(23, 54).m_171481_(-1.0F, 2.0F, 4.0F, 3.0F, 7.0F, 3.0F),
      PartPose.m_171430_(-0.2443461F, (float) (Math.PI / 6), 0.0F)
   );
   private static final ModelPartData TANK_R = new ModelPartData(
      "tankR",
      CubeListBuilder.m_171558_().m_171514_(23, 54).m_171481_(-2.0F, 2.0F, 4.0F, 3.0F, 7.0F, 3.0F),
      PartPose.m_171430_(-0.2443461F, (float) (-Math.PI / 6), 0.0F)
   );
   private static final ModelPartData TANK_DOCK = new ModelPartData(
      "tankDock", CubeListBuilder.m_171558_().m_171514_(0, 55).m_171481_(-2.0F, 5.0F, 1.0F, 4.0F, 4.0F, 5.0F)
   );
   private static final ModelPartData CAP_L = new ModelPartData(
      "capL",
      CubeListBuilder.m_171558_().m_171514_(23, 51).m_171481_(-0.5F, 1.0F, 4.5F, 2.0F, 1.0F, 2.0F),
      PartPose.m_171430_(-0.2443461F, (float) (Math.PI / 6), 0.0F)
   );
   private static final ModelPartData CAP_R = new ModelPartData(
      "capR",
      CubeListBuilder.m_171558_().m_171514_(23, 51).m_171481_(-1.5F, 1.0F, 4.5F, 2.0F, 1.0F, 2.0F),
      PartPose.m_171430_(-0.2443461F, (float) (-Math.PI / 6), 0.0F)
   );
   private static final ModelPartData TANK_BRIDGE = new ModelPartData(
      "tankBridge", CubeListBuilder.m_171558_().m_171514_(0, 47).m_171481_(-1.0F, 3.0F, -1.5F, 2.0F, 5.0F, 3.0F), PartPose.m_171430_(0.5934119F, 0.0F, 0.0F)
   );
   private static final ModelPartData TANK_PIPE_LOWER = new ModelPartData(
      "tankPipeLower", CubeListBuilder.m_171558_().m_171514_(0, 37).m_171481_(-0.5F, 2.0F, 3.0F, 1.0F, 4.0F, 1.0F), PartPose.m_171430_(0.2094395F, 0.0F, 0.0F)
   );
   private static final ModelPartData TANK_PIPE_UPPER = new ModelPartData(
      "tankPipeUpper", CubeListBuilder.m_171558_().m_171514_(4, 38).m_171481_(-0.5F, 1.0F, 1.5F, 1.0F, 1.0F, 3.0F)
   );
   private static final ModelPartData TANK_BACK_BRACE = new ModelPartData(
      "tankBackBrace", CubeListBuilder.m_171558_().m_171514_(0, 42).m_171481_(-3.0F, 2.0F, 0.5F, 6.0F, 3.0F, 2.0F), PartPose.m_171430_(0.2443461F, 0.0F, 0.0F)
   );
   private final RenderType RENDER_TYPE = this.m_103119_(TANK_TEXTURE);
   private final List<ModelPart> parts;

   public static LayerDefinition createLayerDefinition() {
      return createLayerDefinition(
         128, 64, new ModelPartData[]{TANK_L, TANK_R, TANK_DOCK, CAP_L, CAP_R, TANK_BRIDGE, TANK_PIPE_LOWER, TANK_PIPE_UPPER, TANK_BACK_BRACE}
      );
   }

   public ModelScubaTank(EntityModelSet entityModelSet) {
      super(RenderType::m_110446_);
      ModelPart root = entityModelSet.m_171103_(TANK_LAYER);
      this.parts = getRenderableParts(
         root, new ModelPartData[]{TANK_L, TANK_R, TANK_DOCK, CAP_L, CAP_R, TANK_BRIDGE, TANK_PIPE_LOWER, TANK_PIPE_UPPER, TANK_BACK_BRACE}
      );
   }

   public void render(@NotNull PoseStack matrix, @NotNull MultiBufferSource renderer, int light, int overlayLight, boolean hasEffect) {
      this.m_7695_(matrix, getVertexConsumer(renderer, this.RENDER_TYPE, hasEffect), light, overlayLight, 1.0F, 1.0F, 1.0F, 1.0F);
   }

   public void m_7695_(
      @NotNull PoseStack poseStack, @NotNull VertexConsumer vertexConsumer, int light, int overlayLight, float red, float green, float blue, float alpha
   ) {
      renderPartsToBuffer(this.parts, poseStack, vertexConsumer, light, overlayLight, red, green, blue, alpha);
   }
}
