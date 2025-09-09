package mekanism.client.render.transmitter;

import com.mojang.blaze3d.vertex.PoseStack;
import mekanism.api.annotations.NothingNullByDefault;
import mekanism.api.chemical.Chemical;
import mekanism.client.render.MekanismRenderer;
import mekanism.common.content.network.BoxedChemicalNetwork;
import mekanism.common.content.network.transmitter.BoxedPressurizedTube;
import mekanism.common.tile.transmitter.TileEntityPressurizedTube;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.Sheets;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider.Context;
import net.minecraft.util.profiling.ProfilerFiller;
import net.minecraft.world.phys.Vec3;

@NothingNullByDefault
public class RenderPressurizedTube extends RenderTransmitterBase<TileEntityPressurizedTube> {
   public RenderPressurizedTube(Context context) {
      super(context);
   }

   protected void render(
      TileEntityPressurizedTube tile, float partialTick, PoseStack matrix, MultiBufferSource renderer, int light, int overlayLight, ProfilerFiller profiler
   ) {
      BoxedChemicalNetwork network = tile.getTransmitter().getTransmitterNetwork();
      if (network != null) {
         matrix.m_85836_();
         matrix.m_85837_(0.5, 0.5, 0.5);
         Chemical<?> chemical = network.lastChemical.getChemical();
         this.renderModel(
            tile,
            matrix,
            renderer.m_6299_(Sheets.m_110792_()),
            chemical.getTint(),
            Math.max(0.2F, network.currentScale),
            15728880,
            overlayLight,
            MekanismRenderer.getChemicalTexture(chemical)
         );
         matrix.m_85849_();
      }
   }

   @Override
   protected String getProfilerSection() {
      return "pressurizedTube";
   }

   protected boolean shouldRenderTransmitter(TileEntityPressurizedTube tile, Vec3 camera) {
      if (super.shouldRenderTransmitter(tile, camera)) {
         BoxedPressurizedTube tube = tile.getTransmitter();
         if (tube.hasTransmitterNetwork()) {
            BoxedChemicalNetwork network = tube.getTransmitterNetwork();
            return !network.lastChemical.isEmpty() && !network.isTankEmpty() && network.currentScale > 0.0F;
         }
      }

      return false;
   }
}
