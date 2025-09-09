package mekanism.client.model;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import mekanism.api.SupportsColorMap;
import mekanism.api.tier.BaseTier;
import mekanism.client.render.MekanismRenderType;
import mekanism.common.Mekanism;
import mekanism.common.util.MekanismUtils;
import net.minecraft.client.model.geom.EntityModelSet;
import net.minecraft.client.model.geom.ModelLayerLocation;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.model.geom.builders.CubeListBuilder;
import net.minecraft.client.model.geom.builders.LayerDefinition;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.resources.ResourceLocation;
import org.jetbrains.annotations.NotNull;

public class ModelEnergyCore extends MekanismJavaModel {
   public static final ModelLayerLocation CORE_LAYER = new ModelLayerLocation(Mekanism.rl("energy_core"), "main");
   private static final ResourceLocation CORE_TEXTURE = MekanismUtils.getResource(MekanismUtils.ResourceType.RENDER, "energy_core.png");
   private static final ModelPartData CUBE = new ModelPartData(
      "cube", CubeListBuilder.m_171558_().m_171514_(0, 0).m_171481_(-8.0F, -8.0F, -8.0F, 16.0F, 16.0F, 16.0F)
   );
   public static final RenderType BATCHED_RENDER_TYPE = MekanismRenderType.STANDARD_TRANSLUCENT_TARGET.apply(CORE_TEXTURE);
   public final RenderType RENDER_TYPE = this.m_103119_(CORE_TEXTURE);
   private final ModelPart cube;

   public static LayerDefinition createLayerDefinition() {
      return createLayerDefinition(32, 32, new ModelPartData[]{CUBE});
   }

   public ModelEnergyCore(EntityModelSet entityModelSet) {
      super(MekanismRenderType.STANDARD);
      ModelPart root = entityModelSet.m_171103_(CORE_LAYER);
      this.cube = CUBE.getFromRoot(root);
   }

   public void render(@NotNull PoseStack matrix, @NotNull MultiBufferSource renderer, int light, int overlayLight, BaseTier baseTier, float energyPercentage) {
      this.render(matrix, renderer.m_6299_(this.RENDER_TYPE), light, overlayLight, baseTier, energyPercentage);
   }

   public void render(@NotNull PoseStack matrix, @NotNull VertexConsumer buffer, int light, int overlayLight, SupportsColorMap color, float energyPercentage) {
      this.m_7695_(matrix, buffer, light, overlayLight, color.getColor(0), color.getColor(1), color.getColor(2), energyPercentage);
   }

   public void m_7695_(
      @NotNull PoseStack matrix, @NotNull VertexConsumer vertexBuilder, int light, int overlayLight, float red, float green, float blue, float alpha
   ) {
      this.cube.m_104306_(matrix, vertexBuilder, light, overlayLight, red, green, blue, alpha);
   }
}
