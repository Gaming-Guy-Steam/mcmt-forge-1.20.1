package mekanism.client.render.tileentity;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import mekanism.api.annotations.NothingNullByDefault;
import mekanism.client.render.data.RenderData;
import mekanism.common.capabilities.merged.MergedTank;
import mekanism.common.content.tank.TankMultiblockData;
import mekanism.common.tile.multiblock.TileEntityDynamicTank;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.Sheets;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider.Context;
import net.minecraft.util.profiling.ProfilerFiller;
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.Nullable;

@NothingNullByDefault
public class RenderDynamicTank extends MultiblockTileEntityRenderer<TankMultiblockData, TileEntityDynamicTank> {
   public RenderDynamicTank(Context context) {
      super(context);
   }

   protected void render(
      TileEntityDynamicTank tile,
      TankMultiblockData multiblock,
      float partialTick,
      PoseStack matrix,
      MultiBufferSource renderer,
      int light,
      int overlayLight,
      ProfilerFiller profiler
   ) {
      RenderData data = this.getRenderData(multiblock);
      if (data != null) {
         VertexConsumer buffer = renderer.m_6299_(Sheets.m_110792_());
         this.renderObject(data, multiblock.valves, tile.m_58899_(), matrix, buffer, overlayLight, multiblock.prevScale);
      }
   }

   @Nullable
   private RenderData getRenderData(TankMultiblockData multiblock) {
      MergedTank.CurrentType currentType = multiblock.mergedTank.getCurrentType();
      if (currentType == MergedTank.CurrentType.EMPTY) {
         return null;
      } else {
         return (switch (currentType) {
            case FLUID -> RenderData.Builder.create(multiblock.getFluidTank().getFluid());
            case GAS -> RenderData.Builder.create(multiblock.getGasTank().getStack());
            case INFUSION -> RenderData.Builder.create(multiblock.getInfusionTank().getStack());
            case PIGMENT -> RenderData.Builder.create(multiblock.getPigmentTank().getStack());
            case SLURRY -> RenderData.Builder.create(multiblock.getSlurryTank().getStack());
            default -> throw new IllegalStateException("Unknown current type.");
         }).of(multiblock).build();
      }
   }

   @Override
   protected String getProfilerSection() {
      return "dynamicTank";
   }

   protected boolean shouldRender(TileEntityDynamicTank tile, TankMultiblockData multiblock, Vec3 camera) {
      return super.shouldRender(tile, multiblock, camera) && !multiblock.isEmpty();
   }
}
