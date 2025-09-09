package mekanism.client.render.transmitter;

import com.mojang.blaze3d.vertex.PoseStack;
import mekanism.api.annotations.NothingNullByDefault;
import mekanism.client.render.MekanismRenderer;
import mekanism.common.content.network.transmitter.ThermodynamicConductor;
import mekanism.common.tile.transmitter.TileEntityThermodynamicConductor;
import mekanism.common.util.HeatUtils;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.Sheets;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider.Context;
import net.minecraft.util.profiling.ProfilerFiller;

@NothingNullByDefault
public class RenderThermodynamicConductor extends RenderTransmitterBase<TileEntityThermodynamicConductor> {
   public RenderThermodynamicConductor(Context context) {
      super(context);
   }

   protected void render(
      TileEntityThermodynamicConductor tile,
      float partialTick,
      PoseStack matrix,
      MultiBufferSource renderer,
      int light,
      int overlayLight,
      ProfilerFiller profiler
   ) {
      matrix.m_85836_();
      matrix.m_85837_(0.5, 0.5, 0.5);
      ThermodynamicConductor conductor = tile.getTransmitter();
      int argb = HeatUtils.getColorFromTemp(conductor.getTotalTemperature(), conductor.getBaseColor()).argb();
      this.renderModel(
         tile, matrix, renderer.m_6299_(Sheets.m_110792_()), argb, MekanismRenderer.getAlpha(argb), 15728880, overlayLight, MekanismRenderer.heatIcon
      );
      matrix.m_85849_();
   }

   @Override
   protected String getProfilerSection() {
      return "thermodynamicConductor";
   }
}
