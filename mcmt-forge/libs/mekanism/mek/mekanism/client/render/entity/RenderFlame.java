package mekanism.client.render.entity;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.mojang.math.Axis;
import mekanism.client.render.MekanismRenderType;
import mekanism.common.entity.EntityFlame;
import mekanism.common.util.MekanismUtils;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.culling.Frustum;
import net.minecraft.client.renderer.entity.EntityRenderer;
import net.minecraft.client.renderer.entity.EntityRendererProvider.Context;
import net.minecraft.resources.ResourceLocation;
import org.jetbrains.annotations.NotNull;
import org.joml.Matrix4f;

public class RenderFlame extends EntityRenderer<EntityFlame> {
   public RenderFlame(Context context) {
      super(context);
   }

   public boolean shouldRender(EntityFlame flame, @NotNull Frustum camera, double camX, double camY, double camZ) {
      return flame.f_19797_ > 0 && super.m_5523_(flame, camera, camX, camY, camZ);
   }

   public void render(@NotNull EntityFlame flame, float entityYaw, float partialTick, @NotNull PoseStack matrix, @NotNull MultiBufferSource renderer, int light) {
      float alpha = (flame.f_19797_ + partialTick) / 80.0F;
      float actualAlpha = 1.0F - alpha;
      if (!(actualAlpha <= 0.0F)) {
         float size = (float)Math.pow(2.0F * alpha, 2.0);
         float f5 = 0.15625F;
         float scale = 0.05625F * (0.8F + size);
         int alphaColor = (int)(actualAlpha * 255.0F);
         matrix.m_85836_();
         matrix.m_252781_(Axis.f_252436_.m_252977_(flame.f_19859_ + (flame.m_146908_() - flame.f_19859_) * partialTick - 90.0F));
         matrix.m_252781_(Axis.f_252403_.m_252977_(flame.f_19860_ + (flame.m_146909_() - flame.f_19860_) * partialTick));
         matrix.m_252781_(Axis.f_252529_.m_252977_(45.0F));
         matrix.m_85841_(scale, scale, scale);
         matrix.m_252880_(-4.0F, 0.0F, 0.0F);
         VertexConsumer builder = renderer.m_6299_(MekanismRenderType.FLAME.apply(this.getTextureLocation(flame)));

         for (int j = 0; j < 4; j++) {
            matrix.m_252781_(Axis.f_252529_.m_252977_(90.0F));
            builder.m_252939_(matrix.m_85850_().m_252943_(), 0.0F, 0.0F, scale);
            Matrix4f matrix4f = matrix.m_85850_().m_252922_();
            builder.m_252986_(matrix4f, -8.0F, -2.0F, 0.0F).m_6122_(255, 255, 255, alphaColor).m_7421_(0.0F, 0.0F).m_5752_();
            builder.m_252986_(matrix4f, 8.0F, -2.0F, 0.0F).m_6122_(255, 255, 255, alphaColor).m_7421_(0.5F, 0.0F).m_5752_();
            builder.m_252986_(matrix4f, 8.0F, 2.0F, 0.0F).m_6122_(255, 255, 255, alphaColor).m_7421_(0.5F, f5).m_5752_();
            builder.m_252986_(matrix4f, -8.0F, 2.0F, 0.0F).m_6122_(255, 255, 255, alphaColor).m_7421_(0.0F, f5).m_5752_();
         }

         matrix.m_85849_();
      }
   }

   @NotNull
   public ResourceLocation getTextureLocation(@NotNull EntityFlame entity) {
      return MekanismUtils.getResource(MekanismUtils.ResourceType.RENDER, "flame.png");
   }
}
