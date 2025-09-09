package mekanism.client.model;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import mekanism.client.render.MekanismRenderType;
import mekanism.common.Mekanism;
import mekanism.common.util.MekanismUtils;
import net.minecraft.client.model.geom.EntityModelSet;
import net.minecraft.client.model.geom.ModelLayerLocation;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.model.geom.builders.CubeDeformation;
import net.minecraft.client.model.geom.builders.CubeListBuilder;
import net.minecraft.client.model.geom.builders.LayerDefinition;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.resources.ResourceLocation;
import org.jetbrains.annotations.NotNull;

public class ModelIndustrialAlarm extends MekanismJavaModel {
   public static final ModelLayerLocation ALARM_LAYER = new ModelLayerLocation(Mekanism.rl("industrial_alarm"), "main");
   private static final ResourceLocation TEXTURE_ACTIVE = MekanismUtils.getResource(MekanismUtils.ResourceType.RENDER, "industrial_alarm_active.png");
   private static final ModelPartData BULB = new ModelPartData(
      "bulb", CubeListBuilder.m_171558_().m_171514_(16, 0).m_171481_(-1.0F, 1.0F, -1.0F, 2.0F, 3.0F, 2.0F)
   );
   private static final ModelPartData LIGHT_BOX = new ModelPartData(
      "light_box", CubeListBuilder.m_171558_().m_171488_(-2.0F, 1.0F, -2.0F, 4.0F, 4.0F, 4.0F, new CubeDeformation(0.01F))
   );
   private static final ModelPartData AURA = new ModelPartData(
      "aura", CubeListBuilder.m_171558_().m_171514_(0, 16).m_171488_(-6.0F, 2.0F, -1.0F, 12.0F, 1.0F, 2.0F, new CubeDeformation(0.01F))
   );
   private final RenderType RENDER_TYPE = this.m_103119_(TEXTURE_ACTIVE);
   private final ModelPart bulb;
   private final ModelPart lightBox;
   private final ModelPart aura;

   public static LayerDefinition createLayerDefinition() {
      return createLayerDefinition(64, 64, new ModelPartData[]{BULB, LIGHT_BOX, AURA});
   }

   public ModelIndustrialAlarm(EntityModelSet entityModelSet) {
      super(MekanismRenderType.ALARM);
      ModelPart root = entityModelSet.m_171103_(ALARM_LAYER);
      this.bulb = BULB.getFromRoot(root);
      this.lightBox = LIGHT_BOX.getFromRoot(root);
      this.aura = AURA.getFromRoot(root);
   }

   public void m_7695_(
      @NotNull PoseStack matrix, @NotNull VertexConsumer vertexBuilder, int light, int overlayLight, float red, float green, float blue, float alpha
   ) {
      this.render(matrix, vertexBuilder, 15728880, overlayLight, red, green, blue, alpha, 0.0F);
   }

   public RenderType getRenderType() {
      return this.RENDER_TYPE;
   }

   public void render(
      @NotNull PoseStack matrix,
      @NotNull VertexConsumer vertexBuilder,
      int light,
      int overlayLight,
      float red,
      float green,
      float blue,
      float alpha,
      float rotation
   ) {
      float yRot = rotation * (float) (Math.PI / 180.0);
      setRotation(this.aura, 0.0F, yRot, 0.0F);
      setRotation(this.bulb, 0.0F, yRot, 0.0F);
      float bulbAlpha = 0.3F + Math.abs(rotation * 2.0F % 360.0F - 180.0F) / 180.0F * 0.7F;
      this.bulb.m_104306_(matrix, vertexBuilder, light, overlayLight, red, green, blue, bulbAlpha);
      this.lightBox.m_104306_(matrix, vertexBuilder, light, overlayLight, red, green, blue, alpha);
      this.aura.m_104306_(matrix, vertexBuilder, light, overlayLight, red, green, blue, bulbAlpha);
   }
}
