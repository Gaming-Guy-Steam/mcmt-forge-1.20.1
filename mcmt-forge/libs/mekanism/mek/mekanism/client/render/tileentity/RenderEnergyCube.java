package mekanism.client.render.tileentity;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.mojang.math.Axis;
import mekanism.api.annotations.NothingNullByDefault;
import mekanism.api.tier.BaseTier;
import mekanism.client.model.ModelEnergyCore;
import mekanism.client.render.RenderTickHandler;
import mekanism.common.tile.TileEntityEnergyCube;
import mekanism.common.util.MekanismUtils;
import net.minecraft.client.Camera;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider.Context;
import net.minecraft.util.profiling.ProfilerFiller;
import net.minecraft.world.phys.Vec3;
import org.joml.Vector3f;

@NothingNullByDefault
public class RenderEnergyCube extends ModelTileEntityRenderer<TileEntityEnergyCube, ModelEnergyCore> {
   public static final Axis coreVec = Axis.m_253057_(new Vector3f(0.0F, MekanismUtils.ONE_OVER_ROOT_TWO, MekanismUtils.ONE_OVER_ROOT_TWO));

   public RenderEnergyCube(Context context) {
      super(context, ModelEnergyCore::new);
   }

   protected void render(
      TileEntityEnergyCube tile, float partialTicks, PoseStack matrix, MultiBufferSource renderer, int light, int overlayLight, ProfilerFiller profiler
   ) {
      final float energyScale = tile.getEnergyScale();
      final Vec3 renderPos = Vec3.m_82512_(tile.m_58899_());
      final BaseTier baseTier = tile.getTier().getBaseTier();
      RenderTickHandler.addTransparentRenderer(ModelEnergyCore.BATCHED_RENDER_TYPE, new RenderTickHandler.LazyRender() {
         @Override
         public void render(Camera camera, VertexConsumer buffer, PoseStack poseStack, int renderTick, float partialTick, ProfilerFiller profilerx) {
            float ticks = renderTick + partialTick;
            float scaledTicks = 4.0F * ticks;
            poseStack.m_85836_();
            poseStack.m_85837_(renderPos.f_82479_, renderPos.f_82480_, renderPos.f_82481_);
            poseStack.m_85841_(0.4F, 0.4F, 0.4F);
            poseStack.m_85837_(0.0, Math.sin(Math.toRadians(3.0F * ticks)) / 7.0, 0.0);
            poseStack.m_252781_(Axis.f_252436_.m_252977_(scaledTicks));
            poseStack.m_252781_(RenderEnergyCube.coreVec.m_252977_(36.0F + scaledTicks));
            RenderEnergyCube.this.model.render(poseStack, buffer, 15728880, overlayLight, baseTier, energyScale);
            poseStack.m_85849_();
         }

         @Override
         public Vec3 getCenterPos(float partialTick) {
            return renderPos;
         }

         @Override
         public String getProfilerSection() {
            return "energyCube.core";
         }
      });
   }

   @Override
   protected String getProfilerSection() {
      return "energyCube";
   }

   public boolean shouldRender(TileEntityEnergyCube tile, Vec3 camera) {
      return tile.getEnergyScale() > 0.0F && super.m_142756_(tile, camera);
   }
}
