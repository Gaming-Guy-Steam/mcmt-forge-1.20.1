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

public class ModelJetpack extends MekanismJavaModel {
   public static final ModelLayerLocation JETPACK_LAYER = new ModelLayerLocation(Mekanism.rl("jetpack"), "main");
   private static final ResourceLocation JETPACK_TEXTURE = MekanismUtils.getResource(MekanismUtils.ResourceType.RENDER, "jetpack.png");
   protected static final ModelPartData PACK_TOP = new ModelPartData(
      "packTop", CubeListBuilder.m_171558_().m_171514_(92, 28).m_171481_(-4.0F, 0.0F, 4.0F, 8.0F, 4.0F, 1.0F), PartPose.m_171430_(0.2094395F, 0.0F, 0.0F)
   );
   protected static final ModelPartData PACK_BOTTOM = new ModelPartData(
      "packBottom", CubeListBuilder.m_171558_().m_171514_(92, 42).m_171481_(-4.0F, 4.1F, 1.5F, 8.0F, 4.0F, 4.0F), PartPose.m_171430_(-0.0872665F, 0.0F, 0.0F)
   );
   protected static final ModelPartData PACK_MID = new ModelPartData(
      "packMid", CubeListBuilder.m_171558_().m_171514_(92, 34).m_171481_(-4.0F, 3.3F, 1.5F, 8.0F, 1.0F, 4.0F)
   );
   protected static final ModelPartData PACK_CORE = new ModelPartData(
      "packCore", CubeListBuilder.m_171558_().m_171514_(69, 2).m_171481_(-3.5F, 3.0F, 2.0F, 7.0F, 1.0F, 3.0F)
   );
   protected static final ModelPartData WING_SUPPORT_L = new ModelPartData(
      "wingSupportL", CubeListBuilder.m_171558_().m_171514_(71, 55).m_171481_(3.0F, -1.0F, 2.2F, 7.0F, 2.0F, 2.0F), PartPose.m_171430_(0.0F, 0.0F, 0.2792527F)
   );
   protected static final ModelPartData WING_SUPPORT_R = new ModelPartData(
      "wingSupportR",
      CubeListBuilder.m_171558_().m_171514_(71, 55).m_171481_(-10.0F, -1.0F, 2.2F, 7.0F, 2.0F, 2.0F),
      PartPose.m_171430_(0.0F, 0.0F, -0.2792527F)
   );
   protected static final ModelPartData PACK_TOP_REAR = new ModelPartData(
      "packTopRear", CubeListBuilder.m_171558_().m_171514_(106, 28).m_171481_(-4.0F, 1.0F, 1.0F, 8.0F, 3.0F, 3.0F), PartPose.m_171430_(0.2094395F, 0.0F, 0.0F)
   );
   protected static final ModelPartData EXTENDO_SUPPORT_L = new ModelPartData(
      "extendoSupportL",
      CubeListBuilder.m_171558_().m_171514_(94, 16).m_171481_(8.0F, -0.2F, 2.5F, 9.0F, 1.0F, 1.0F),
      PartPose.m_171430_(0.0F, 0.0F, 0.2792527F)
   );
   protected static final ModelPartData EXTENDO_SUPPORT_R = new ModelPartData(
      "extendoSupportR",
      CubeListBuilder.m_171558_().m_171514_(94, 16).m_171481_(-17.0F, -0.2F, 2.5F, 9.0F, 1.0F, 1.0F),
      PartPose.m_171430_(0.0F, 0.0F, -0.2792527F)
   );
   protected static final ModelPartData WING_BLADE_L = new ModelPartData(
      "wingBladeL", CubeListBuilder.m_171558_().m_171514_(62, 5).m_171481_(3.3F, 1.1F, 3.0F, 14.0F, 2.0F, 0.0F), PartPose.m_171430_(0.0F, 0.0F, 0.2094395F)
   );
   protected static final ModelPartData WING_BLADE_R = new ModelPartData(
      "wingBladeR", CubeListBuilder.m_171558_().m_171514_(62, 5).m_171481_(-17.3F, 1.1F, 3.0F, 14.0F, 2.0F, 0.0F), PartPose.m_171430_(0.0F, 0.0F, -0.2094395F)
   );
   protected static final ModelPartData PACK_DOODAD_2 = new ModelPartData(
      "packDoodad2", CubeListBuilder.m_171558_().m_171514_(116, 0).m_171481_(1.0F, 0.5F, 4.2F, 2.0F, 1.0F, 1.0F), PartPose.m_171430_(0.2094395F, 0.0F, 0.0F)
   );
   protected static final ModelPartData PACK_DOODAD_3 = new ModelPartData(
      "packDoodad3", CubeListBuilder.m_171558_().m_171514_(116, 0).m_171481_(1.0F, 2.0F, 4.2F, 2.0F, 1.0F, 1.0F), PartPose.m_171430_(0.2094395F, 0.0F, 0.0F)
   );
   protected static final ModelPartData BOTTOM_THRUSTER = new ModelPartData(
      "bottomThruster", CubeListBuilder.m_171558_().m_171514_(68, 26).m_171481_(-3.0F, 8.0F, 2.333333F, 6.0F, 1.0F, 2.0F)
   );
   protected static final ModelPartData LIGHT_1 = new ModelPartData(
      "light1", CubeListBuilder.m_171558_().m_171514_(55, 2).m_171481_(2.0F, 6.55F, 4.0F, 1.0F, 1.0F, 1.0F)
   );
   protected static final ModelPartData LIGHT_2 = new ModelPartData(
      "light2", CubeListBuilder.m_171558_().m_171514_(55, 2).m_171481_(0.0F, 6.55F, 4.0F, 1.0F, 1.0F, 1.0F)
   );
   protected static final ModelPartData LIGHT_3 = new ModelPartData(
      "light3", CubeListBuilder.m_171558_().m_171514_(55, 2).m_171481_(-3.0F, 6.55F, 4.0F, 1.0F, 1.0F, 1.0F)
   );
   private static final ModelPartData THRUSTER_LEFT = thrusterLeft(-3.0F);
   private static final ModelPartData THRUSTER_RIGHT = thrusterRight(-3.0F);
   private static final ModelPartData FUEL_TUBE_RIGHT = fuelTubeRight(-3.0F);
   private static final ModelPartData FUEL_TUBE_LEFT = fuelTubeLeft(-3.0F);
   private final RenderType frameRenderType = this.m_103119_(JETPACK_TEXTURE);
   private final RenderType wingRenderType = MekanismRenderType.JETPACK_GLASS.apply(JETPACK_TEXTURE);
   private final List<ModelPart> parts;
   private final List<ModelPart> litParts;
   private final List<ModelPart> wingParts;

   public static LayerDefinition createLayerDefinition() {
      return createLayerDefinition(
         128,
         64,
         new ModelPartData[]{
            PACK_TOP,
            PACK_BOTTOM,
            THRUSTER_LEFT,
            THRUSTER_RIGHT,
            FUEL_TUBE_RIGHT,
            FUEL_TUBE_LEFT,
            PACK_MID,
            PACK_CORE,
            WING_SUPPORT_L,
            WING_SUPPORT_R,
            PACK_TOP_REAR,
            EXTENDO_SUPPORT_L,
            EXTENDO_SUPPORT_R,
            WING_BLADE_L,
            WING_BLADE_R,
            PACK_DOODAD_2,
            PACK_DOODAD_3,
            BOTTOM_THRUSTER,
            LIGHT_1,
            LIGHT_2,
            LIGHT_3
         }
      );
   }

   public ModelJetpack(EntityModelSet entityModelSet) {
      this(entityModelSet.m_171103_(JETPACK_LAYER));
   }

   protected ModelJetpack(ModelPart root) {
      super(RenderType::m_110446_);
      this.parts = getRenderableParts(
         root,
         new ModelPartData[]{
            PACK_TOP,
            PACK_BOTTOM,
            THRUSTER_LEFT,
            THRUSTER_RIGHT,
            FUEL_TUBE_RIGHT,
            FUEL_TUBE_LEFT,
            PACK_MID,
            WING_SUPPORT_L,
            WING_SUPPORT_R,
            PACK_TOP_REAR,
            EXTENDO_SUPPORT_L,
            EXTENDO_SUPPORT_R,
            PACK_DOODAD_2,
            PACK_DOODAD_3,
            BOTTOM_THRUSTER
         }
      );
      this.litParts = getRenderableParts(root, new ModelPartData[]{LIGHT_1, LIGHT_2, LIGHT_3, PACK_CORE});
      this.wingParts = getRenderableParts(root, new ModelPartData[]{WING_BLADE_L, WING_BLADE_R});
   }

   public void render(@NotNull PoseStack poseStack, @NotNull MultiBufferSource renderer, int light, int overlayLight, boolean hasEffect) {
      this.m_7695_(poseStack, getVertexConsumer(renderer, this.frameRenderType, hasEffect), light, overlayLight, 1.0F, 1.0F, 1.0F, 1.0F);
      renderPartsToBuffer(
         this.wingParts, poseStack, getVertexConsumer(renderer, this.wingRenderType, hasEffect), 15728880, overlayLight, 1.0F, 1.0F, 1.0F, 0.2F
      );
   }

   public void m_7695_(
      @NotNull PoseStack poseStack, @NotNull VertexConsumer vertexConsumer, int light, int overlayLight, float red, float green, float blue, float alpha
   ) {
      renderPartsToBuffer(this.parts, poseStack, vertexConsumer, light, overlayLight, red, green, blue, alpha);
      renderPartsToBuffer(this.litParts, poseStack, vertexConsumer, 15728880, overlayLight, red, green, blue, alpha);
   }

   protected static ModelPartData thrusterLeft(float fuelZ) {
      return new ModelPartData(
         "thrusterLeft",
         CubeListBuilder.m_171558_().m_171514_(69, 30).m_171481_(7.8F, 1.5F, fuelZ - 0.5F, 3.0F, 3.0F, 3.0F),
         PartPose.m_171430_((float) (Math.PI / 4), -0.715585F, 0.3490659F)
      );
   }

   protected static ModelPartData thrusterRight(float fuelZ) {
      return new ModelPartData(
         "thrusterRight",
         CubeListBuilder.m_171558_().m_171514_(69, 30).m_171481_(-10.8F, 1.5F, fuelZ - 0.5F, 3.0F, 3.0F, 3.0F),
         PartPose.m_171430_((float) (Math.PI / 4), 0.715585F, -0.3490659F)
      );
   }

   protected static ModelPartData fuelTubeRight(float fuelZ) {
      return new ModelPartData(
         "fuelTubeRight",
         CubeListBuilder.m_171558_().m_171514_(92, 23).m_171481_(-11.2F, 2.0F, fuelZ, 8.0F, 2.0F, 2.0F),
         PartPose.m_171430_((float) (Math.PI / 4), 0.715585F, -0.3490659F)
      );
   }

   protected static ModelPartData fuelTubeLeft(float fuelZ) {
      return new ModelPartData(
         "fuelTubeLeft",
         CubeListBuilder.m_171558_().m_171514_(92, 23).m_171481_(3.2F, 2.0F, fuelZ, 8.0F, 2.0F, 2.0F),
         PartPose.m_171430_((float) (Math.PI / 4), -0.715585F, 0.3490659F)
      );
   }
}
