package mekanism.client.render.tileentity;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import mekanism.api.annotations.NothingNullByDefault;
import mekanism.client.render.data.FluidRenderData;
import mekanism.client.render.data.RenderData;
import mekanism.common.content.evaporation.EvaporationMultiblockData;
import mekanism.common.tile.multiblock.TileEntityThermalEvaporationController;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.Sheets;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider.Context;
import net.minecraft.util.profiling.ProfilerFiller;
import net.minecraft.world.phys.Vec3;

@NothingNullByDefault
public class RenderThermalEvaporationPlant extends MultiblockTileEntityRenderer<EvaporationMultiblockData, TileEntityThermalEvaporationController> {
   public RenderThermalEvaporationPlant(Context context) {
      super(context);
   }

   protected void render(
      TileEntityThermalEvaporationController tile,
      EvaporationMultiblockData multiblock,
      float partialTick,
      PoseStack matrix,
      MultiBufferSource renderer,
      int light,
      int overlayLight,
      ProfilerFiller profiler
   ) {
      VertexConsumer buffer = renderer.m_6299_(Sheets.m_110792_());
      FluidRenderData data = RenderData.Builder.create(multiblock.inputTank.getFluid())
         .location(multiblock.renderLocation.m_7918_(1, 0, 1))
         .dimensions(2, multiblock.height() - 2, 2)
         .build();
      this.renderObject(data, multiblock.valves, tile.m_58899_(), matrix, buffer, overlayLight, Math.min(1.0F, multiblock.prevScale));
   }

   @Override
   protected String getProfilerSection() {
      return "thermalEvaporationController";
   }

   protected boolean shouldRender(TileEntityThermalEvaporationController tile, EvaporationMultiblockData multiblock, Vec3 camera) {
      return super.shouldRender(tile, multiblock, camera) && !multiblock.inputTank.isEmpty();
   }
}
