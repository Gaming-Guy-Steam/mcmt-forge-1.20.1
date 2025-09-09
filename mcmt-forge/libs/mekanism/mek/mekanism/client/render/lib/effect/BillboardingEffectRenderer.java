package mekanism.client.render.lib.effect;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import java.util.function.Supplier;
import mekanism.client.render.MekanismRenderType;
import mekanism.client.render.RenderTickHandler;
import mekanism.common.lib.effect.CustomEffect;
import net.minecraft.client.Camera;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.profiling.ProfilerFiller;
import net.minecraft.world.phys.Vec3;
import org.joml.Matrix4f;
import org.joml.Quaternionf;
import org.joml.Vector3f;

public class BillboardingEffectRenderer {
   private BillboardingEffectRenderer() {
   }

   public static void render(CustomEffect effect, String profilerSection) {
      render(effect.getTexture(), profilerSection, () -> effect);
   }

   public static void render(ResourceLocation texture, String profilerSection, Supplier<CustomEffect> lazyEffect) {
      RenderTickHandler.addTransparentRenderer(MekanismRenderType.SPS.apply(texture), new RenderTickHandler.LazyRender() {
         @Override
         public void render(Camera camera, VertexConsumer renderer, PoseStack poseStack, int renderTick, float partialTick, ProfilerFiller profiler) {
            BillboardingEffectRenderer.render(camera, renderer, poseStack, renderTick, partialTick, lazyEffect.get());
         }

         @Override
         public Vec3 getCenterPos(float partialTick) {
            return lazyEffect.get().getPos(partialTick);
         }

         @Override
         public String getProfilerSection() {
            return profilerSection;
         }
      });
   }

   private static void render(Camera camera, VertexConsumer buffer, PoseStack poseStack, int renderTick, float partialTick, CustomEffect effect) {
      int gridSize = effect.getTextureGridSize();
      int tick = renderTick % (gridSize * gridSize);
      int xIndex = tick % gridSize;
      int yIndex = tick / gridSize;
      float spriteSize = 1.0F / gridSize;
      Quaternionf quaternion = camera.m_253121_();
      Vector3f[] vertexPos = new Vector3f[]{
         new Vector3f(-1.0F, 1.0F, 0.0F), new Vector3f(1.0F, 1.0F, 0.0F), new Vector3f(1.0F, -1.0F, 0.0F), new Vector3f(-1.0F, -1.0F, 0.0F)
      };
      Vec3 pos = effect.getPos(partialTick);

      for (Vector3f vector3f : vertexPos) {
         quaternion.transform(vector3f);
         vector3f.mul(effect.getScale());
         vector3f.add((float)pos.m_7096_(), (float)pos.m_7098_(), (float)pos.m_7094_());
      }

      int[] color = effect.getColor().rgbaArray();
      float minU = xIndex * spriteSize;
      float maxU = minU + spriteSize;
      float minV = yIndex * spriteSize;
      float maxV = minV + spriteSize;
      poseStack.m_85836_();
      Matrix4f matrix = poseStack.m_85850_().m_252922_();
      buffer.m_252986_(matrix, vertexPos[0].x(), vertexPos[0].y(), vertexPos[0].z())
         .m_6122_(color[0], color[1], color[2], color[3])
         .m_7421_(minU, maxV)
         .m_5752_();
      buffer.m_252986_(matrix, vertexPos[1].x(), vertexPos[1].y(), vertexPos[1].z())
         .m_6122_(color[0], color[1], color[2], color[3])
         .m_7421_(maxU, maxV)
         .m_5752_();
      buffer.m_252986_(matrix, vertexPos[2].x(), vertexPos[2].y(), vertexPos[2].z())
         .m_6122_(color[0], color[1], color[2], color[3])
         .m_7421_(maxU, minV)
         .m_5752_();
      buffer.m_252986_(matrix, vertexPos[3].x(), vertexPos[3].y(), vertexPos[3].z())
         .m_6122_(color[0], color[1], color[2], color[3])
         .m_7421_(minU, minV)
         .m_5752_();
      poseStack.m_85849_();
   }
}
