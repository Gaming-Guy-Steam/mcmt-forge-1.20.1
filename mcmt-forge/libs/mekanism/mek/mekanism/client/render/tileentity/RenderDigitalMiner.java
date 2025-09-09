package mekanism.client.render.tileentity;

import com.mojang.blaze3d.vertex.PoseStack;
import mekanism.api.annotations.NothingNullByDefault;
import mekanism.client.render.MekanismRenderer;
import mekanism.client.render.RenderResizableCuboid;
import mekanism.common.tile.machine.TileEntityDigitalMiner;
import mekanism.common.util.EnumUtils;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.Sheets;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider.Context;
import net.minecraft.core.Direction;
import net.minecraft.util.profiling.ProfilerFiller;
import net.minecraft.world.phys.Vec3;

@NothingNullByDefault
public class RenderDigitalMiner extends MekanismTileEntityRenderer<TileEntityDigitalMiner> {
   private static final MekanismRenderer.LazyModel model = new MekanismRenderer.LazyModel(
      () -> new MekanismRenderer.Model3D().setTexture(MekanismRenderer.whiteIcon).bounds(0.0F, 1.0F)
   );
   private static final int[] colors = new int[EnumUtils.DIRECTIONS.length];

   public static void resetCachedVisuals() {
      model.reset();
   }

   public RenderDigitalMiner(Context context) {
      super(context);
   }

   protected void render(
      TileEntityDigitalMiner miner, float partialTick, PoseStack matrix, MultiBufferSource renderer, int light, int overlayLight, ProfilerFiller profiler
   ) {
      matrix.m_85836_();
      matrix.m_85837_(-miner.getRadius() + 0.01, miner.getMinY() - miner.m_58899_().m_123342_() + 0.01, -miner.getRadius() + 0.01);
      float diameter = miner.getDiameter() - 0.02F;
      matrix.m_85841_(diameter, miner.getMaxY() - miner.getMinY() - 0.02F, diameter);
      RenderResizableCuboid.FaceDisplay faceDisplay = this.isInsideBounds(
            miner.m_58899_().m_123341_() - miner.getRadius(),
            miner.getMinY(),
            miner.m_58899_().m_123343_() - miner.getRadius(),
            miner.m_58899_().m_123341_() + miner.getRadius() + 1,
            miner.getMaxY(),
            miner.m_58899_().m_123343_() + miner.getRadius() + 1
         )
         ? RenderResizableCuboid.FaceDisplay.BACK
         : RenderResizableCuboid.FaceDisplay.BOTH;
      MekanismRenderer.renderObject(model.get(), matrix, renderer.m_6299_(Sheets.m_110792_()), colors, 15728880, overlayLight, faceDisplay, this.getCamera());
      matrix.m_85849_();
   }

   @Override
   protected String getProfilerSection() {
      return "digitalMiner";
   }

   public boolean shouldRenderOffScreen(TileEntityDigitalMiner tile) {
      return true;
   }

   public boolean shouldRender(TileEntityDigitalMiner tile, Vec3 camera) {
      return tile.isClientRendering() && tile.canDisplayVisuals() && super.m_142756_(tile, camera);
   }

   static {
      colors[Direction.DOWN.ordinal()] = MekanismRenderer.getColorARGB(255, 255, 255, 0.82F);
      colors[Direction.UP.ordinal()] = MekanismRenderer.getColorARGB(255, 255, 255, 0.82F);
      colors[Direction.NORTH.ordinal()] = MekanismRenderer.getColorARGB(255, 255, 255, 0.8F);
      colors[Direction.SOUTH.ordinal()] = MekanismRenderer.getColorARGB(255, 255, 255, 0.8F);
      colors[Direction.WEST.ordinal()] = MekanismRenderer.getColorARGB(255, 255, 255, 0.78F);
      colors[Direction.EAST.ordinal()] = MekanismRenderer.getColorARGB(255, 255, 255, 0.78F);
   }
}
