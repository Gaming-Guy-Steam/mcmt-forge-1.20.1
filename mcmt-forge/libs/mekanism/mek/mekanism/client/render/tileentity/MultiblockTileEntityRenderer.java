package mekanism.client.render.tileentity;

import com.mojang.blaze3d.vertex.PoseStack;
import mekanism.api.annotations.NothingNullByDefault;
import mekanism.common.lib.multiblock.MultiblockData;
import mekanism.common.tile.prefab.TileEntityMultiblock;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider.Context;
import net.minecraft.util.profiling.ProfilerFiller;
import net.minecraft.world.phys.Vec3;

@NothingNullByDefault
public abstract class MultiblockTileEntityRenderer<MULTIBLOCK extends MultiblockData, TILE extends TileEntityMultiblock<MULTIBLOCK>>
   extends MekanismTileEntityRenderer<TILE> {
   protected MultiblockTileEntityRenderer(Context context) {
      super(context);
   }

   protected final void render(TILE tile, float partialTick, PoseStack matrix, MultiBufferSource renderer, int light, int overlayLight, ProfilerFiller profiler) {
      this.render(tile, tile.getMultiblock(), partialTick, matrix, renderer, light, overlayLight, profiler);
   }

   protected abstract void render(
      TILE tile, MULTIBLOCK multiblock, float partialTick, PoseStack matrix, MultiBufferSource renderer, int light, int overlayLight, ProfilerFiller profiler
   );

   public final boolean shouldRenderOffScreen(TILE tile) {
      return true;
   }

   public final boolean shouldRender(TILE tile, Vec3 camera) {
      if (!tile.isMaster()) {
         return false;
      } else {
         MULTIBLOCK multiblock = tile.getMultiblock();
         return multiblock.isFormed() && this.shouldRender(tile, multiblock, camera) && super.m_142756_(tile, camera);
      }
   }

   protected boolean shouldRender(TILE tile, MULTIBLOCK multiblock, Vec3 camera) {
      return multiblock.renderLocation != null;
   }
}
