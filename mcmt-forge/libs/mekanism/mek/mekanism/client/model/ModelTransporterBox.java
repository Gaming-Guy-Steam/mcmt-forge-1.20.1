package mekanism.client.model;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import mekanism.api.text.EnumColor;
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

public class ModelTransporterBox extends MekanismJavaModel {
   public static final ModelLayerLocation BOX_LAYER = new ModelLayerLocation(Mekanism.rl("transporter_box"), "main");
   private static final ResourceLocation BOX_TEXTURE = MekanismUtils.getResource(MekanismUtils.ResourceType.RENDER, "transporter_box.png");
   private static final ModelPartData BOX = new ModelPartData(
      "box", CubeListBuilder.m_171558_().m_171481_(0.0F, 0.0F, 0.0F, 7.0F, 7.0F, 7.0F), PartPose.m_171419_(-3.5F, 0.0F, -3.5F)
   );
   private final RenderType RENDER_TYPE = this.m_103119_(BOX_TEXTURE);
   private final ModelPart box;

   public static LayerDefinition createLayerDefinition() {
      return createLayerDefinition(64, 64, new ModelPartData[]{BOX});
   }

   public ModelTransporterBox(EntityModelSet entityModelSet) {
      super(RenderType::m_110458_);
      ModelPart root = entityModelSet.m_171103_(BOX_LAYER);
      this.box = BOX.getFromRoot(root);
   }

   public void render(@NotNull PoseStack matrix, @NotNull MultiBufferSource renderer, int light, int overlayLight, float x, float y, float z, EnumColor color) {
      matrix.m_85836_();
      matrix.m_252880_(x, y, z);
      this.m_7695_(matrix, renderer.m_6299_(this.RENDER_TYPE), 15728880, overlayLight, color.getColor(0), color.getColor(1), color.getColor(2), 1.0F);
      matrix.m_85849_();
   }

   public void m_7695_(
      @NotNull PoseStack matrix, @NotNull VertexConsumer vertexBuilder, int light, int overlayLight, float red, float green, float blue, float alpha
   ) {
      this.box.m_104306_(matrix, vertexBuilder, light, overlayLight, red, green, blue, alpha);
   }
}
