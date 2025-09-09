package mekanism.client.render.data;

import java.util.Objects;
import mekanism.api.annotations.NothingNullByDefault;
import mekanism.common.lib.multiblock.IValveHandler;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;

@NothingNullByDefault
public class ValveRenderData extends FluidRenderData {
   private final Direction side;
   private final int valveFluidHeight;

   private ValveRenderData(FluidRenderData renderData, Direction side, BlockPos valveLocation) {
      super(renderData.location, renderData.width, renderData.height, renderData.length, renderData.fluidType);
      this.side = side;
      this.valveFluidHeight = valveLocation.m_123342_() - this.location.m_123342_();
   }

   public static ValveRenderData get(FluidRenderData renderData, IValveHandler.ValveData valveData) {
      return new ValveRenderData(renderData, valveData.side, valveData.location);
   }

   public int getValveFluidHeight() {
      return this.valveFluidHeight;
   }

   public Direction getSide() {
      return this.side;
   }

   @Override
   public boolean equals(Object data) {
      return data instanceof ValveRenderData other && super.equals(data) && this.side == other.side && this.valveFluidHeight == other.valveFluidHeight;
   }

   @Override
   public int hashCode() {
      return Objects.hash(super.hashCode(), this.side, this.valveFluidHeight);
   }
}
