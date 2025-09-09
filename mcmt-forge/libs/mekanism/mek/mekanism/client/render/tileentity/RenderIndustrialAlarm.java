package mekanism.client.render.tileentity;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.mojang.math.Axis;
import mekanism.api.annotations.NothingNullByDefault;
import mekanism.client.model.ModelIndustrialAlarm;
import mekanism.client.render.RenderTickHandler;
import mekanism.common.tile.TileEntityIndustrialAlarm;
import net.minecraft.client.Camera;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider.Context;
import net.minecraft.util.profiling.ProfilerFiller;
import net.minecraft.world.phys.Vec3;

@NothingNullByDefault
public class RenderIndustrialAlarm extends ModelTileEntityRenderer<TileEntityIndustrialAlarm, ModelIndustrialAlarm> {
   private static final float ROTATE_SPEED = 10.0F;

   public RenderIndustrialAlarm(Context context) {
      super(context, ModelIndustrialAlarm::new);
   }

   protected void render(
      TileEntityIndustrialAlarm tile, float partialTicks, PoseStack matrix, MultiBufferSource renderer, int light, int overlayLight, ProfilerFiller profiler
   ) {
      RenderTickHandler.addTransparentRenderer(this.model.getRenderType(), new RenderTickHandler.LazyRender() {
         @Override
         public void render(Camera camera, VertexConsumer buffer, PoseStack poseStack, int renderTick, float partialTick, ProfilerFiller profilerx) {
            float rot = (renderTick + partialTick) * 10.0F % 360.0F;
            Vec3 renderPos = Vec3.m_82539_(tile.m_58899_());
            poseStack.m_85836_();
            poseStack.m_85837_(renderPos.f_82479_, renderPos.f_82480_, renderPos.f_82481_);
            switch (tile.getDirection()) {
               case DOWN:
                  poseStack.m_252880_(0.0F, 1.0F, 0.0F);
                  poseStack.m_252781_(Axis.f_252529_.m_252977_(180.0F));
                  break;
               case NORTH:
                  poseStack.m_85837_(0.0, 0.5, 0.5);
                  poseStack.m_252781_(Axis.f_252495_.m_252977_(90.0F));
                  break;
               case SOUTH:
                  poseStack.m_85837_(0.0, 0.5, -0.5);
                  poseStack.m_252781_(Axis.f_252529_.m_252977_(90.0F));
                  break;
               case EAST:
                  poseStack.m_85837_(-0.5, 0.5, 0.0);
                  poseStack.m_252781_(Axis.f_252393_.m_252977_(90.0F));
                  break;
               case WEST:
                  poseStack.m_85837_(0.5, 0.5, 0.0);
                  poseStack.m_252781_(Axis.f_252403_.m_252977_(90.0F));
            }

            RenderIndustrialAlarm.this.model.render(poseStack, buffer, 15728880, overlayLight, 1.0F, 1.0F, 1.0F, 1.0F, rot);
            poseStack.m_85849_();
         }

         @Override
         public Vec3 getCenterPos(float partialTick) {
            return Vec3.m_82512_(tile.m_58899_());
         }

         @Override
         public String getProfilerSection() {
            return "industrialAlarm";
         }
      });
   }

   @Override
   protected String getProfilerSection() {
      return "industrialAlarm";
   }

   public boolean shouldRender(TileEntityIndustrialAlarm tile, Vec3 camera) {
      return tile.getActive() && super.m_142756_(tile, camera);
   }
}
