package mekanism.client.render.transmitter;

import com.mojang.blaze3d.vertex.PoseStack;
import mekanism.api.annotations.NothingNullByDefault;
import mekanism.client.render.MekanismRenderer;
import mekanism.common.content.network.EnergyNetwork;
import mekanism.common.content.network.transmitter.UniversalCable;
import mekanism.common.tile.transmitter.TileEntityUniversalCable;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.Sheets;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider.Context;
import net.minecraft.util.profiling.ProfilerFiller;
import net.minecraft.world.phys.Vec3;

@NothingNullByDefault
public class RenderUniversalCable extends RenderTransmitterBase<TileEntityUniversalCable> {
   public RenderUniversalCable(Context context) {
      super(context);
   }

   protected void render(
      TileEntityUniversalCable tile, float partialTick, PoseStack matrix, MultiBufferSource renderer, int light, int overlayLight, ProfilerFiller profiler
   ) {
      EnergyNetwork network = tile.getTransmitter().getTransmitterNetwork();
      if (network != null) {
         matrix.m_85836_();
         matrix.m_85837_(0.5, 0.5, 0.5);
         this.renderModel(
            tile, matrix, renderer.m_6299_(Sheets.m_110792_()), 16777215, network.currentScale, 15728880, overlayLight, MekanismRenderer.energyIcon
         );
         matrix.m_85849_();
      }
   }

   @Override
   protected String getProfilerSection() {
      return "universalCable";
   }

   protected boolean shouldRenderTransmitter(TileEntityUniversalCable tile, Vec3 camera) {
      if (super.shouldRenderTransmitter(tile, camera)) {
         UniversalCable cable = tile.getTransmitter();
         if (cable.hasTransmitterNetwork()) {
            EnergyNetwork network = cable.getTransmitterNetwork();
            return network.currentScale > 0.0F;
         }
      }

      return false;
   }
}
