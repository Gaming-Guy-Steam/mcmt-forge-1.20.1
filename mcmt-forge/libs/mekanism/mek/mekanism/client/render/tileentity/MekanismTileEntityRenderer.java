package mekanism.client.render.tileentity;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import java.util.Set;
import mekanism.api.annotations.NothingNullByDefault;
import mekanism.client.render.MekanismRenderer;
import mekanism.client.render.ModelRenderer;
import mekanism.client.render.RenderResizableCuboid;
import mekanism.client.render.data.FluidRenderData;
import mekanism.client.render.data.RenderData;
import mekanism.common.config.MekanismConfig;
import mekanism.common.lib.multiblock.IValveHandler;
import net.minecraft.client.Camera;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.MultiBufferSource.BufferSource;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderer;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider.Context;
import net.minecraft.core.BlockPos;
import net.minecraft.util.profiling.ProfilerFiller;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

@NothingNullByDefault
public abstract class MekanismTileEntityRenderer<TILE extends BlockEntity> implements BlockEntityRenderer<TILE> {
   protected final Context context;

   protected MekanismTileEntityRenderer(Context context) {
      this.context = context;
   }

   public int m_142163_() {
      return MekanismConfig.client.terRange.get();
   }

   public void m_6922_(TILE tile, float partialTick, PoseStack matrix, MultiBufferSource renderer, int light, int overlayLight) {
      if (tile.m_58904_() != null) {
         ProfilerFiller profiler = tile.m_58904_().m_46473_();
         profiler.m_6180_(this.getProfilerSection());
         this.render(tile, partialTick, matrix, renderer, light, overlayLight, profiler);
         profiler.m_7238_();
      }
   }

   protected abstract void render(
      TILE tile, float partialTick, PoseStack matrix, MultiBufferSource renderer, int light, int overlayLight, ProfilerFiller profiler
   );

   protected void endIfNeeded(MultiBufferSource renderer, @Nullable RenderType renderType) {
      if (!Minecraft.m_91085_() && renderer instanceof BufferSource source) {
         if (renderType == null) {
            source.m_173043_();
         } else {
            source.m_109912_(renderType);
         }
      }
   }

   protected abstract String getProfilerSection();

   protected Camera getCamera() {
      return this.context.m_173581_().f_112249_;
   }

   protected final boolean isInsideBounds(double minX, double minY, double minZ, double maxX, double maxY, double maxZ) {
      return this.isInsideBounds(this.getCamera(), minX, minY, minZ, maxX, maxY, maxZ);
   }

   protected final boolean isInsideBounds(Camera camera, double minX, double minY, double minZ, double maxX, double maxY, double maxZ) {
      Vec3 projectedView = camera.m_90583_();
      return minX <= projectedView.f_82479_
         && projectedView.f_82479_ <= maxX
         && minY <= projectedView.f_82480_
         && projectedView.f_82480_ <= maxY
         && minZ <= projectedView.f_82481_
         && projectedView.f_82481_ <= maxZ;
   }

   protected final RenderResizableCuboid.FaceDisplay getFaceDisplay(Camera camera, RenderData data, MekanismRenderer.Model3D model) {
      return this.isInsideBounds(
            camera,
            data.location.m_123341_(),
            data.location.m_123342_(),
            data.location.m_123343_(),
            data.location.m_123341_() + data.length,
            data.location.m_123342_() + ModelRenderer.getActualHeight(model),
            data.location.m_123343_() + data.width
         )
         ? RenderResizableCuboid.FaceDisplay.BACK
         : RenderResizableCuboid.FaceDisplay.FRONT;
   }

   protected void renderObject(
      RenderData data, Set<IValveHandler.ValveData> valves, BlockPos rendererPos, @NotNull PoseStack matrix, VertexConsumer buffer, int overlay, float scale
   ) {
      MekanismRenderer.Model3D model = ModelRenderer.getModel(data, scale);
      int glow = this.renderObject(data, rendererPos, model, matrix, buffer, overlay, scale);
      if (data instanceof FluidRenderData fluidRenderData && !valves.isEmpty()) {
         RenderResizableCuboid.FaceDisplay faceDisplay = this.isInsideBounds(
               data.location.m_123341_(),
               data.location.m_123342_(),
               data.location.m_123343_(),
               data.location.m_123341_() + data.length,
               data.location.m_123342_() + data.height,
               data.location.m_123343_() + data.width
            )
            ? RenderResizableCuboid.FaceDisplay.BOTH
            : RenderResizableCuboid.FaceDisplay.FRONT;
         MekanismRenderer.renderValves(
            matrix, buffer, valves, fluidRenderData, model.maxY - model.minY, rendererPos, glow, overlay, faceDisplay, this.getCamera()
         );
      }
   }

   protected int renderObject(RenderData data, BlockPos rendererPos, @NotNull PoseStack matrix, VertexConsumer buffer, int overlay, float scale) {
      return this.renderObject(data, rendererPos, ModelRenderer.getModel(data, scale), matrix, buffer, overlay, scale);
   }

   protected int renderObject(
      RenderData data, BlockPos rendererPos, MekanismRenderer.Model3D object, @NotNull PoseStack matrix, VertexConsumer buffer, int overlay, float scale
   ) {
      int glow = data.calculateGlowLight(15728640);
      Camera camera = this.getCamera();
      matrix.m_85836_();
      matrix.m_252880_(
         data.location.m_123341_() - rendererPos.m_123341_(),
         data.location.m_123342_() - rendererPos.m_123342_(),
         data.location.m_123343_() - rendererPos.m_123343_()
      );
      MekanismRenderer.renderObject(
         object, matrix, buffer, data.getColorARGB(scale), glow, overlay, this.getFaceDisplay(camera, data, object), camera, data.location
      );
      matrix.m_85849_();
      return glow;
   }
}
