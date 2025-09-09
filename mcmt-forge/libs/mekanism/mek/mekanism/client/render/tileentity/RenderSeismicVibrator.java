package mekanism.client.render.tileentity;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.mojang.blaze3d.vertex.PoseStack.Pose;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;
import mekanism.api.annotations.NothingNullByDefault;
import mekanism.client.model.MekanismModelCache;
import mekanism.client.render.RenderTickHandler;
import mekanism.client.render.lib.Vertex;
import mekanism.common.tile.machine.TileEntitySeismicVibrator;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.Sheets;
import net.minecraft.client.renderer.block.model.BakedQuad;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider.Context;
import net.minecraft.util.profiling.ProfilerFiller;
import net.minecraft.world.level.block.entity.BlockEntity;

@NothingNullByDefault
public class RenderSeismicVibrator extends MekanismTileEntityRenderer<TileEntitySeismicVibrator> implements IWireFrameRenderer {
   private static final List<Vertex[]> vertices = new ArrayList<>();

   public static void resetCached() {
      vertices.clear();
   }

   public RenderSeismicVibrator(Context context) {
      super(context);
   }

   protected void render(
      TileEntitySeismicVibrator tile, float partialTick, PoseStack matrix, MultiBufferSource renderer, int light, int overlayLight, ProfilerFiller profiler
   ) {
      this.renderTranslated(tile, partialTick, matrix, poseStack -> {
         Pose entry = poseStack.m_85850_();
         VertexConsumer buffer = renderer.m_6299_(Sheets.m_110789_());

         for (BakedQuad quad : MekanismModelCache.INSTANCE.VIBRATOR_SHAFT.getQuads(tile.m_58904_().f_46441_)) {
            buffer.m_85987_(entry, quad, 1.0F, 1.0F, 1.0F, light, overlayLight);
         }
      });
   }

   @Override
   protected String getProfilerSection() {
      return "seismicVibrator";
   }

   @Override
   public boolean isCombined() {
      return true;
   }

   @Override
   public void renderWireFrame(BlockEntity tile, float partialTick, PoseStack matrix, VertexConsumer buffer, int red, int green, int blue, int alpha) {
      if (tile instanceof TileEntitySeismicVibrator vibrator) {
         if (vertices.isEmpty()) {
            MekanismModelCache.INSTANCE.VIBRATOR_SHAFT.collectQuadVertices(vertices, tile.m_58904_().f_46441_);
         }

         this.renderTranslated(
            vibrator,
            partialTick,
            matrix,
            poseStack -> RenderTickHandler.renderVertexWireFrame(vertices, buffer, poseStack.m_85850_().m_252922_(), red, green, blue, alpha)
         );
      }
   }

   private void renderTranslated(TileEntitySeismicVibrator tile, float partialTick, PoseStack matrix, Consumer<PoseStack> renderer) {
      matrix.m_85836_();
      float piston = Math.max(0.0F, (float)Math.sin((tile.clientPiston + (tile.getActive() ? partialTick : 0.0F)) / 5.0F));
      matrix.m_85837_(0.0, piston * 0.625, 0.0);
      renderer.accept(matrix);
      matrix.m_85849_();
   }
}
