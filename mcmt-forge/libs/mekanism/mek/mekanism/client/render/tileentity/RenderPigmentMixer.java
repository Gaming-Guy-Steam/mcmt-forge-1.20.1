package mekanism.client.render.tileentity;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.mojang.blaze3d.vertex.PoseStack.Pose;
import com.mojang.math.Axis;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;
import mekanism.api.annotations.NothingNullByDefault;
import mekanism.client.model.MekanismModelCache;
import mekanism.client.render.RenderTickHandler;
import mekanism.client.render.lib.Vertex;
import mekanism.common.block.attribute.Attribute;
import mekanism.common.tile.machine.TileEntityPigmentMixer;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.Sheets;
import net.minecraft.client.renderer.block.model.BakedQuad;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider.Context;
import net.minecraft.util.profiling.ProfilerFiller;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.Vec3;

@NothingNullByDefault
public class RenderPigmentMixer extends MekanismTileEntityRenderer<TileEntityPigmentMixer> implements IWireFrameRenderer {
   private static final List<Vertex[]> vertices = new ArrayList<>();
   private static final float SHAFT_SPEED = 5.0F;

   public static void resetCached() {
      vertices.clear();
   }

   public RenderPigmentMixer(Context context) {
      super(context);
   }

   protected void render(
      TileEntityPigmentMixer tile, float partialTick, PoseStack matrix, MultiBufferSource renderer, int light, int overlayLight, ProfilerFiller profiler
   ) {
      this.renderTranslated(tile, partialTick, matrix, poseStack -> {
         Pose entry = poseStack.m_85850_();
         VertexConsumer buffer = renderer.m_6299_(Sheets.m_110789_());

         for (BakedQuad quad : MekanismModelCache.INSTANCE.PIGMENT_MIXER_SHAFT.getQuads(tile.m_58904_().f_46441_)) {
            buffer.m_85987_(entry, quad, 1.0F, 1.0F, 1.0F, light, overlayLight);
         }
      });
   }

   @Override
   protected String getProfilerSection() {
      return "pigmentMixer";
   }

   public boolean shouldRender(TileEntityPigmentMixer tile, Vec3 camera) {
      return tile.getActive() && super.m_142756_(tile, camera);
   }

   @Override
   public boolean hasSelectionBox(BlockState state) {
      return Attribute.isActive(state);
   }

   @Override
   public boolean isCombined() {
      return true;
   }

   @Override
   public void renderWireFrame(BlockEntity tile, float partialTick, PoseStack matrix, VertexConsumer buffer, int red, int green, int blue, int alpha) {
      if (tile instanceof TileEntityPigmentMixer mixer) {
         if (vertices.isEmpty()) {
            MekanismModelCache.INSTANCE.PIGMENT_MIXER_SHAFT.collectQuadVertices(vertices, tile.m_58904_().f_46441_);
         }

         this.renderTranslated(
            mixer,
            partialTick,
            matrix,
            poseStack -> RenderTickHandler.renderVertexWireFrame(vertices, buffer, poseStack.m_85850_().m_252922_(), red, green, blue, alpha)
         );
      }
   }

   private void renderTranslated(TileEntityPigmentMixer tile, float partialTick, PoseStack matrix, Consumer<PoseStack> renderer) {
      matrix.m_85836_();
      switch (tile.getDirection()) {
         case NORTH:
            matrix.m_252880_(0.4375F, 0.0F, 0.375F);
            break;
         case SOUTH:
            matrix.m_252880_(0.4375F, 0.0F, 0.5F);
            break;
         case WEST:
            matrix.m_252880_(0.375F, 0.0F, 0.4375F);
            break;
         case EAST:
            matrix.m_252880_(0.5F, 0.0F, 0.4375F);
      }

      float shift = 0.0625F;
      matrix.m_252880_(shift, 0.0F, shift);
      matrix.m_252781_(Axis.f_252392_.m_252977_(((float)tile.m_58904_().m_46467_() + partialTick) * 5.0F % 360.0F));
      matrix.m_252880_(-shift, 0.0F, -shift);
      renderer.accept(matrix);
      matrix.m_85849_();
   }
}
