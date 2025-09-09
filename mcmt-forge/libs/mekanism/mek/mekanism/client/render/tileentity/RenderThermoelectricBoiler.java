package mekanism.client.render.tileentity;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import mekanism.api.annotations.NothingNullByDefault;
import mekanism.client.render.data.FluidRenderData;
import mekanism.client.render.data.RenderData;
import mekanism.common.content.boiler.BoilerMultiblockData;
import mekanism.common.tile.multiblock.TileEntityBoilerCasing;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.Sheets;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider.Context;
import net.minecraft.core.BlockPos;
import net.minecraft.util.profiling.ProfilerFiller;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.common.util.Lazy;

@NothingNullByDefault
public class RenderThermoelectricBoiler extends MultiblockTileEntityRenderer<BoilerMultiblockData, TileEntityBoilerCasing> {
   public RenderThermoelectricBoiler(Context context) {
      super(context);
   }

   protected void render(
      TileEntityBoilerCasing tile,
      BoilerMultiblockData multiblock,
      float partialTick,
      PoseStack matrix,
      MultiBufferSource renderer,
      int light,
      int overlayLight,
      ProfilerFiller profiler
   ) {
      BlockPos pos = tile.m_58899_();
      Lazy<VertexConsumer> buffer = Lazy.of(() -> renderer.m_6299_(Sheets.m_110792_()));
      if (!multiblock.waterTank.isEmpty()) {
         int height = multiblock.upperRenderLocation.m_123342_() - 1 - multiblock.renderLocation.m_123342_();
         if (height > 0) {
            FluidRenderData data = RenderData.Builder.create(multiblock.waterTank.getFluid()).of(multiblock).height(height).build();
            this.renderObject(data, multiblock.valves, pos, matrix, (VertexConsumer)buffer.get(), overlayLight, multiblock.prevWaterScale);
         }
      }

      if (!multiblock.steamTank.isEmpty()) {
         int height = multiblock.renderLocation.m_123342_() + multiblock.height() - 2 - multiblock.upperRenderLocation.m_123342_();
         if (height > 0) {
            RenderData data = RenderData.Builder.create(multiblock.steamTank.getStack())
               .of(multiblock)
               .location(multiblock.upperRenderLocation)
               .height(height)
               .build();
            this.renderObject(data, pos, matrix, (VertexConsumer)buffer.get(), overlayLight, multiblock.prevSteamScale);
         }
      }
   }

   @Override
   protected String getProfilerSection() {
      return "thermoelectricBoiler";
   }

   protected boolean shouldRender(TileEntityBoilerCasing tile, BoilerMultiblockData multiblock, Vec3 camera) {
      return super.shouldRender(tile, multiblock, camera) && multiblock.upperRenderLocation != null;
   }
}
