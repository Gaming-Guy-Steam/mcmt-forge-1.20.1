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

public class ModelArmoredJetpack extends ModelJetpack {
   public static final ModelLayerLocation ARMORED_JETPACK_LAYER = new ModelLayerLocation(Mekanism.rl("armored_jetpack"), "main");
   private static final ModelPartData THRUSTER_LEFT = thrusterLeft(-1.9F);
   private static final ModelPartData THRUSTER_RIGHT = thrusterRight(-1.9F);
   private static final ModelPartData FUEL_TUBE_RIGHT = fuelTubeRight(-1.9F);
   private static final ModelPartData FUEL_TUBE_LEFT = fuelTubeLeft(-1.9F);
   private static final ModelPartData CHESTPLATE = new ModelPartData(
      "chestplate",
      CubeListBuilder.m_171558_().m_171514_(104, 22).m_171481_(-4.0F, 1.333333F, -3.0F, 8.0F, 4.0F, 3.0F),
      PartPose.m_171430_(-0.3665191F, 0.0F, 0.0F)
   );
   private static final ModelPartData LEFT_GUARD_TOP = new ModelPartData(
      "leftGuardTop", CubeListBuilder.m_171558_().m_171514_(87, 31).m_171481_(0.95F, 3.0F, -5.0F, 3.0F, 4.0F, 2.0F), PartPose.m_171430_(0.2094395F, 0.0F, 0.0F)
   );
   private static final ModelPartData RIGHT_GUARD_TOP = new ModelPartData(
      "rightGuardTop",
      CubeListBuilder.m_171558_().m_171514_(87, 31).m_171481_(-3.95F, 3.0F, -5.0F, 3.0F, 4.0F, 2.0F),
      PartPose.m_171430_(0.2094395F, 0.0F, 0.0F)
   );
   private static final ModelPartData MIDDLE_PLATE = new ModelPartData(
      "middlePlate", CubeListBuilder.m_171558_().m_171514_(93, 20).m_171481_(-1.5F, 3.0F, -6.2F, 3.0F, 5.0F, 3.0F), PartPose.m_171430_(0.2094395F, 0.0F, 0.0F)
   );
   private static final ModelPartData RIGHT_GUARD_BOT = new ModelPartData(
      "rightGuardBot",
      CubeListBuilder.m_171558_().m_171514_(84, 30).m_171481_(-3.5F, 5.5F, -6.5F, 2.0F, 2.0F, 2.0F),
      PartPose.m_171430_(0.4712389F, 0.0F, 0.0F)
   );
   private static final ModelPartData LEFT_GUARD_BOT = new ModelPartData(
      "leftGuardBot", CubeListBuilder.m_171558_().m_171514_(84, 30).m_171481_(1.5F, 5.5F, -6.5F, 2.0F, 2.0F, 2.0F), PartPose.m_171430_(0.4712389F, 0.0F, 0.0F)
   );
   private static final ModelPartData RIGHT_LIGHT = new ModelPartData(
      "rightLight", CubeListBuilder.m_171558_().m_171514_(81, 0).m_171481_(-3.0F, 4.0F, -4.5F, 1.0F, 3.0F, 1.0F)
   );
   private static final ModelPartData LEFT_LIGHT = new ModelPartData(
      "leftLight", CubeListBuilder.m_171558_().m_171514_(81, 0).m_171481_(2.0F, 4.0F, -4.5F, 1.0F, 3.0F, 1.0F)
   );
   private final List<ModelPart> armoredParts;
   private final List<ModelPart> armoredLights;

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
            LIGHT_3,
            CHESTPLATE,
            LEFT_GUARD_TOP,
            RIGHT_GUARD_TOP,
            MIDDLE_PLATE,
            RIGHT_GUARD_BOT,
            LEFT_GUARD_BOT,
            RIGHT_LIGHT,
            LEFT_LIGHT
         }
      );
   }

   public ModelArmoredJetpack(EntityModelSet entityModelSet) {
      this(entityModelSet.m_171103_(ARMORED_JETPACK_LAYER));
   }

   private ModelArmoredJetpack(ModelPart root) {
      super(root);
      this.armoredParts = getRenderableParts(
         root, new ModelPartData[]{CHESTPLATE, LEFT_GUARD_TOP, RIGHT_GUARD_TOP, MIDDLE_PLATE, RIGHT_GUARD_BOT, LEFT_GUARD_BOT}
      );
      this.armoredLights = getRenderableParts(root, new ModelPartData[]{RIGHT_LIGHT, LEFT_LIGHT});
   }

   @Override
   public void m_7695_(
      @NotNull PoseStack poseStack, @NotNull VertexConsumer vertexConsumer, int light, int overlayLight, float red, float green, float blue, float alpha
   ) {
      super.m_7695_(poseStack, vertexConsumer, light, overlayLight, red, green, blue, alpha);
      poseStack.m_85836_();
      poseStack.m_85837_(0.0, 0.0, -0.0625);
      renderPartsToBuffer(this.armoredParts, poseStack, vertexConsumer, light, overlayLight, red, green, blue, alpha);
      renderPartsToBuffer(this.armoredLights, poseStack, vertexConsumer, 15728880, overlayLight, red, green, blue, alpha);
      poseStack.m_85849_();
   }
}
