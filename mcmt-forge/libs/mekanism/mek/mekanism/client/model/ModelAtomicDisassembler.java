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
import net.minecraft.client.model.geom.builders.CubeDeformation;
import net.minecraft.client.model.geom.builders.CubeListBuilder;
import net.minecraft.client.model.geom.builders.LayerDefinition;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.resources.ResourceLocation;
import org.jetbrains.annotations.NotNull;

public class ModelAtomicDisassembler extends MekanismJavaModel {
   public static final ModelLayerLocation DISASSEMBLER_LAYER = new ModelLayerLocation(Mekanism.rl("atomic_disassembler"), "main");
   private static final ResourceLocation DISASSEMBLER_TEXTURE = MekanismUtils.getResource(MekanismUtils.ResourceType.RENDER, "atomic_disassembler.png");
   private static final ModelPartData HANDLE = new ModelPartData(
      "handle", CubeListBuilder.m_171558_().m_171514_(0, 10).m_171481_(0.0F, -1.0F, -3.0F, 1.0F, 16.0F, 1.0F)
   );
   private static final ModelPartData HANDLE_TOP = new ModelPartData(
      "handleTop", CubeListBuilder.m_171558_().m_171514_(34, 9).m_171481_(-0.5F, -3.5F, -3.5F, 2.0F, 5.0F, 2.0F)
   );
   private static final ModelPartData BLADE_BACK = new ModelPartData(
      "bladeBack", CubeListBuilder.m_171558_().m_171514_(42, 0).m_171481_(0.0F, -4.0F, -4.0F, 1.0F, 2.0F, 10.0F)
   );
   private static final ModelPartData HEAD = new ModelPartData(
      "head",
      CubeListBuilder.m_171558_().m_171514_(24, 0).m_171481_(-5.0F, -5.7F, -5.5F, 3.0F, 3.0F, 6.0F),
      PartPose.m_171430_(0.0F, 0.0F, (float) (Math.PI / 4))
   );
   private static final ModelPartData NECK = new ModelPartData("neck", CubeListBuilder.m_171558_().m_171481_(-0.5F, -6.0F, -7.0F, 2.0F, 2.0F, 8.0F));
   private static final ModelPartData BLADE_FRONT_UPPER = new ModelPartData(
      "bladeFrontUpper",
      CubeListBuilder.m_171558_().m_171514_(60, 0).m_171481_(0.0F, -0.5333334F, -9.6F, 1.0F, 3.0F, 1.0F),
      PartPose.m_171430_((float) (-Math.PI / 4), 0.0F, 0.0F)
   );
   private static final ModelPartData BLADE_FRONT_LOWER = new ModelPartData(
      "bladeFrontLower",
      CubeListBuilder.m_171558_().m_171514_(58, 0).m_171488_(0.0F, -9.58F, -4.0F, 1.0F, 5.0F, 2.0F, new CubeDeformation(0.01F)),
      PartPose.m_171430_((float) (Math.PI / 4), 0.0F, 0.0F)
   );
   private static final ModelPartData NECK_ANGLED = new ModelPartData(
      "neckAngled",
      CubeListBuilder.m_171558_().m_171514_(12, 0).m_171481_(-0.5F, -8.2F, -2.5F, 2.0F, 1.0F, 1.0F),
      PartPose.m_171430_((float) (Math.PI / 4), 0.0F, 0.0F)
   );
   private static final ModelPartData BLADE_FRONT_CONNECTOR = new ModelPartData(
      "bladeFrontConnector", CubeListBuilder.m_171558_().m_171514_(56, 0).m_171481_(0.0F, -2.44F, -6.07F, 1.0F, 2.0F, 3.0F)
   );
   private static final ModelPartData BLADE_HOLDER_BACK = new ModelPartData(
      "bladeHolderBack", CubeListBuilder.m_171558_().m_171514_(42, 14).m_171481_(-0.5F, -4.5F, 3.5F, 2.0F, 1.0F, 1.0F)
   );
   private static final ModelPartData BLADE_HOLDER_MAIN = new ModelPartData(
      "bladeHolderMain", CubeListBuilder.m_171558_().m_171514_(30, 16).m_171481_(-0.5F, -3.5F, -1.5F, 2.0F, 1.0F, 4.0F)
   );
   private static final ModelPartData BLADE_HOLDER_FRONT = new ModelPartData(
      "bladeHolderFront", CubeListBuilder.m_171558_().m_171514_(42, 12).m_171481_(-0.5F, -4.5F, 1.5F, 2.0F, 1.0F, 1.0F)
   );
   private static final ModelPartData REAR_BAR = new ModelPartData(
      "rearBar", CubeListBuilder.m_171558_().m_171514_(4, 10).m_171481_(0.0F, -5.3F, 0.0F, 1.0F, 1.0F, 7.0F)
   );
   private static final ModelPartData BLADE_BACK_SMALL = new ModelPartData(
      "bladeBackSmall", CubeListBuilder.m_171558_().m_171514_(60, 0).m_171481_(0.0F, -4.0F, 6.0F, 1.0F, 1.0F, 1.0F)
   );
   private static final ModelPartData HANDLE_BASE = new ModelPartData(
      "handleBase", CubeListBuilder.m_171558_().m_171514_(26, 9).m_171481_(-0.5F, 15.0F, -3.5F, 2.0F, 4.0F, 2.0F)
   );
   private static final ModelPartData HANDLE_TOP_BACK = new ModelPartData(
      "handleTopBack", CubeListBuilder.m_171558_().m_171514_(37, 0).m_171481_(0.0F, -2.0F, -2.0F, 1.0F, 4.0F, 1.0F)
   );
   private final RenderType BLADE_RENDER_TYPE = MekanismRenderType.BLADE.apply(DISASSEMBLER_TEXTURE);
   private final RenderType RENDER_TYPE = this.m_103119_(DISASSEMBLER_TEXTURE);
   private final List<ModelPart> parts;
   private final List<ModelPart> bladeParts;

   public static LayerDefinition createLayerDefinition() {
      return createLayerDefinition(
         64,
         32,
         new ModelPartData[]{
            HANDLE,
            HANDLE_TOP,
            BLADE_BACK,
            HEAD,
            NECK,
            BLADE_FRONT_UPPER,
            BLADE_FRONT_LOWER,
            NECK_ANGLED,
            BLADE_FRONT_CONNECTOR,
            BLADE_HOLDER_BACK,
            BLADE_HOLDER_MAIN,
            BLADE_HOLDER_FRONT,
            REAR_BAR,
            BLADE_BACK_SMALL,
            HANDLE_BASE,
            HANDLE_TOP_BACK
         }
      );
   }

   public ModelAtomicDisassembler(EntityModelSet entityModelSet) {
      super(RenderType::m_110446_);
      ModelPart root = entityModelSet.m_171103_(DISASSEMBLER_LAYER);
      this.parts = getRenderableParts(
         root,
         new ModelPartData[]{
            HANDLE, HANDLE_TOP, HEAD, NECK, REAR_BAR, NECK_ANGLED, BLADE_HOLDER_BACK, BLADE_HOLDER_MAIN, BLADE_HOLDER_FRONT, HANDLE_BASE, HANDLE_TOP_BACK
         }
      );
      this.bladeParts = getRenderableParts(root, new ModelPartData[]{BLADE_FRONT_CONNECTOR, BLADE_BACK, BLADE_FRONT_UPPER, BLADE_FRONT_LOWER, BLADE_BACK_SMALL});
   }

   public void render(@NotNull PoseStack matrix, @NotNull MultiBufferSource renderer, int light, int overlayLight, boolean hasEffect) {
      this.m_7695_(matrix, getVertexConsumer(renderer, this.RENDER_TYPE, hasEffect), light, overlayLight, 1.0F, 1.0F, 1.0F, 1.0F);
      renderPartsToBuffer(
         this.bladeParts, matrix, getVertexConsumer(renderer, this.BLADE_RENDER_TYPE, hasEffect), 15728880, overlayLight, 1.0F, 1.0F, 1.0F, 0.75F
      );
   }

   public void m_7695_(
      @NotNull PoseStack poseStack, @NotNull VertexConsumer vertexConsumer, int light, int overlayLight, float red, float green, float blue, float alpha
   ) {
      renderPartsToBuffer(this.parts, poseStack, vertexConsumer, light, overlayLight, red, green, blue, alpha);
   }
}
