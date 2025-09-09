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

public class ModelFlamethrower extends MekanismJavaModel {
   public static final ModelLayerLocation FLAMETHROWER_LAYER = new ModelLayerLocation(Mekanism.rl("flamethrower"), "main");
   private static final ResourceLocation FLAMETHROWER_TEXTURE = MekanismUtils.getResource(MekanismUtils.ResourceType.RENDER, "flamethrower.png");
   private static final ModelPartData RING_BOTTOM = new ModelPartData(
      "RingBottom", CubeListBuilder.m_171558_().m_171514_(19, 14).m_171481_(0.0F, 0.0F, 0.0F, 3.0F, 1.0F, 3.0F), PartPose.m_171419_(-2.0F, 19.5F, 1.5F)
   );
   private static final ModelPartData RING_TOP = new ModelPartData(
      "RingTop", CubeListBuilder.m_171558_().m_171514_(19, 14).m_171481_(0.0F, 0.0F, 0.0F, 3.0F, 1.0F, 3.0F), PartPose.m_171419_(-2.0F, 13.5F, 1.466667F)
   );
   private static final ModelPartData RING = new ModelPartData(
      "Ring", CubeListBuilder.m_171558_().m_171514_(0, 14).m_171481_(0.0F, 0.0F, 0.0F, 5.0F, 6.0F, 4.0F), PartPose.m_171419_(-3.0F, 14.0F, 1.0F)
   );
   private static final ModelPartData AXLE = new ModelPartData(
      "Axle", CubeListBuilder.m_171558_().m_171514_(32, 12).m_171481_(0.0F, 0.0F, 0.0F, 4.0F, 4.0F, 7.0F), PartPose.m_171419_(-2.5F, 15.0F, -6.5F)
   );
   private static final ModelPartData AXLE_B_LEFT = new ModelPartData(
      "AxleBLeft",
      CubeListBuilder.m_171558_().m_171514_(0, 25).m_171481_(-0.5F, -0.5F, 0.0F, 1.0F, 1.0F, 8.0F),
      PartPose.m_171423_(-2.0F, 19.0F, -7.0F, 0.0F, 0.0F, 0.2094395F)
   );
   private static final ModelPartData AXLE_B_RIGHT = new ModelPartData(
      "AxleBRight",
      CubeListBuilder.m_171558_().m_171514_(0, 25).m_171481_(-0.5F, -0.5F, 0.0F, 1.0F, 1.0F, 8.0F),
      PartPose.m_171423_(1.0F, 19.0F, -7.0F, 0.0174533F, 0.0F, -0.2094395F)
   );
   private static final ModelPartData AXLE_T_RIGHT = new ModelPartData(
      "AxleTRight",
      CubeListBuilder.m_171558_().m_171514_(0, 25).m_171481_(-0.5F, -0.5F, 0.0F, 1.0F, 1.0F, 8.0F),
      PartPose.m_171423_(1.0F, 15.0F, -7.0F, 0.0F, 0.0F, 0.2094395F)
   );
   private static final ModelPartData AXLE_T_LEFT = new ModelPartData(
      "AxleTLeft",
      CubeListBuilder.m_171558_().m_171514_(0, 25).m_171481_(-0.5F, -0.5F, 0.0F, 1.0F, 1.0F, 8.0F),
      PartPose.m_171423_(-2.0F, 15.0F, -7.0F, 0.0F, 0.0F, -0.2094395F)
   );
   private static final ModelPartData GRASP = new ModelPartData(
      "Grasp",
      CubeListBuilder.m_171558_().m_171514_(24, 19).m_171481_(0.0F, 0.0F, 0.0F, 2.0F, 1.0F, 1.0F),
      PartPose.m_171423_(-1.5F, 13.0F, -1.1F, 0.7807508F, 0.0F, 0.0F)
   );
   private static final ModelPartData GRASP_ROD = new ModelPartData(
      "GraspRod",
      CubeListBuilder.m_171558_().m_171514_(19, 19).m_171481_(0.0F, 0.0F, 0.0F, 1.0F, 3.0F, 1.0F),
      PartPose.m_171423_(-1.0F, 13.0F, -1.0F, 0.2230717F, 0.0F, 0.0F)
   );
   private static final ModelPartData SUPPORT_CENTER = new ModelPartData(
      "SupportCenter",
      CubeListBuilder.m_171558_().m_171514_(0, 40).m_171481_(0.0F, 0.0F, 0.0F, 2.0F, 1.0F, 6.0F),
      PartPose.m_171423_(-1.5F, 12.4F, 6.6F, -0.1115358F, 0.0F, 0.0F)
   );
   private static final ModelPartData SUPPORT_FRONT = new ModelPartData(
      "SupportFront",
      CubeListBuilder.m_171558_().m_171514_(19, 24).m_171481_(0.0F, 0.0F, 0.0F, 1.0F, 1.0F, 4.0F),
      PartPose.m_171423_(-1.0F, 13.1F, 12.5F, -1.226894F, 0.0F, 0.0F)
   );
   private static final ModelPartData SUPPORT_REAR = new ModelPartData(
      "SupportRear",
      CubeListBuilder.m_171558_().m_171514_(0, 35).m_171481_(0.0F, 0.0F, 0.0F, 3.0F, 1.0F, 3.0F),
      PartPose.m_171423_(-2.0F, 14.0F, 4.0F, 0.5424979F, 0.0F, 0.0F)
   );
   private static final ModelPartData LARGE_BARREL = new ModelPartData(
      "LargeBarrel", CubeListBuilder.m_171558_().m_171514_(19, 48).m_171481_(0.0F, 0.0F, 0.0F, 2.0F, 3.0F, 7.0F), PartPose.m_171419_(-1.5F, 16.0F, 4.0F)
   );
   private static final ModelPartData LARGE_BARREL_DECOR = new ModelPartData(
      "LargeBarrelDecor",
      CubeListBuilder.m_171558_().m_171514_(0, 48).m_171481_(0.0F, 0.0F, 0.0F, 3.0F, 3.0F, 6.0F),
      PartPose.m_171423_(-2.0F, 15.0F, 4.0F, -0.1115358F, 0.0F, 0.0F)
   );
   private static final ModelPartData LARGE_BARREL_DECOR_2 = new ModelPartData(
      "LargeBarrelDecor2", CubeListBuilder.m_171558_().m_171514_(17, 41).m_171481_(0.0F, 0.0F, 0.0F, 4.0F, 2.0F, 4.0F), PartPose.m_171419_(-2.5F, 16.0F, 4.0F)
   );
   private static final ModelPartData BARREL = new ModelPartData(
      "Barrel", CubeListBuilder.m_171558_().m_171514_(19, 30).m_171481_(0.0F, 0.0F, 0.0F, 2.0F, 2.0F, 8.0F), PartPose.m_171419_(-1.5F, 16.5F, 11.0F)
   );
   private static final ModelPartData BARREL_RING = new ModelPartData(
      "BarrelRing", CubeListBuilder.m_171558_().m_171514_(30, 25).m_171481_(0.0F, 0.0F, 0.0F, 3.0F, 3.0F, 1.0F), PartPose.m_171419_(-2.0F, 16.0F, 13.0F)
   );
   private static final ModelPartData BARREL_RING_2 = new ModelPartData(
      "BarrelRing2", CubeListBuilder.m_171558_().m_171514_(30, 25).m_171481_(0.0F, 0.0F, 0.0F, 3.0F, 3.0F, 1.0F), PartPose.m_171419_(-2.0F, 16.0F, 17.0F)
   );
   private static final ModelPartData FLAME = new ModelPartData(
      "Flame",
      CubeListBuilder.m_171558_().m_171514_(38, 0).m_171481_(0.0F, 0.0F, 0.0F, 1.0F, 1.0F, 2.0F),
      PartPose.m_171423_(-1.0F, 19.5F, 19.0F, 0.7063936F, 0.0F, 0.0F)
   );
   private static final ModelPartData FLAME_STRUT = new ModelPartData(
      "FlameStrut",
      CubeListBuilder.m_171558_().m_171514_(27, 0).m_171481_(0.0F, 0.0F, 0.0F, 2.0F, 1.0F, 3.0F),
      PartPose.m_171423_(-1.466667F, 18.5F, 17.0F, -0.2602503F, 0.0F, 0.0F)
   );
   private static final ModelPartData HYDROGEN_DECOR = new ModelPartData(
      "HydrogenDecor",
      CubeListBuilder.m_171558_().m_171514_(27, 5).m_171481_(0.0F, 0.0F, 0.0F, 3.0F, 1.0F, 5.0F),
      PartPose.m_171423_(1.5F, 15.66667F, -4.933333F, 0.0F, 0.0F, 0.4438713F)
   );
   private static final ModelPartData HYDROGEN = new ModelPartData(
      "Hydrogen", CubeListBuilder.m_171558_().m_171481_(0.0F, 0.0F, 0.0F, 3.0F, 3.0F, 10.0F), PartPose.m_171423_(1.5F, 16.0F, -5.5F, 0.0F, 0.0F, 0.4438713F)
   );
   private final RenderType RENDER_TYPE = this.m_103119_(FLAMETHROWER_TEXTURE);
   private final List<ModelPart> parts;

   public static LayerDefinition createLayerDefinition() {
      return createLayerDefinition(
         64,
         64,
         new ModelPartData[]{
            RING_BOTTOM,
            RING_TOP,
            RING,
            AXLE,
            AXLE_B_LEFT,
            AXLE_B_RIGHT,
            AXLE_T_RIGHT,
            AXLE_T_LEFT,
            GRASP,
            GRASP_ROD,
            SUPPORT_CENTER,
            SUPPORT_FRONT,
            SUPPORT_REAR,
            LARGE_BARREL,
            LARGE_BARREL_DECOR,
            LARGE_BARREL_DECOR_2,
            BARREL,
            BARREL_RING,
            BARREL_RING_2,
            FLAME,
            FLAME_STRUT,
            HYDROGEN_DECOR,
            HYDROGEN
         }
      );
   }

   public ModelFlamethrower(EntityModelSet entityModelSet) {
      super(RenderType::m_110446_);
      ModelPart root = entityModelSet.m_171103_(FLAMETHROWER_LAYER);
      this.parts = getRenderableParts(
         root,
         new ModelPartData[]{
            RING_BOTTOM,
            RING_TOP,
            RING,
            AXLE,
            AXLE_B_LEFT,
            AXLE_B_RIGHT,
            AXLE_T_RIGHT,
            AXLE_T_LEFT,
            GRASP,
            GRASP_ROD,
            SUPPORT_CENTER,
            SUPPORT_FRONT,
            SUPPORT_REAR,
            LARGE_BARREL,
            LARGE_BARREL_DECOR,
            LARGE_BARREL_DECOR_2,
            BARREL,
            BARREL_RING,
            BARREL_RING_2,
            FLAME,
            FLAME_STRUT,
            HYDROGEN_DECOR,
            HYDROGEN
         }
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
