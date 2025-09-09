package mekanism.common.capabilities.fluid;

import java.util.Objects;
import java.util.function.IntSupplier;
import mekanism.api.Action;
import mekanism.api.AutomationType;
import mekanism.api.IContentsListener;
import mekanism.api.annotations.NothingNullByDefault;
import mekanism.common.tier.FluidTankTier;
import mekanism.common.tile.TileEntityFluidTank;
import mekanism.common.util.WorldUtils;
import net.minecraftforge.fluids.FluidStack;
import org.jetbrains.annotations.Nullable;

@NothingNullByDefault
public class FluidTankFluidTank extends BasicFluidTank {
   private final TileEntityFluidTank tile;
   private final boolean isCreative;
   private final IntSupplier rate;

   public static FluidTankFluidTank create(TileEntityFluidTank tile, @Nullable IContentsListener listener) {
      Objects.requireNonNull(tile, "Fluid tank tile entity cannot be null");
      return new FluidTankFluidTank(tile, listener);
   }

   private FluidTankFluidTank(TileEntityFluidTank tile, @Nullable IContentsListener listener) {
      super(tile.tier.getStorage(), alwaysTrueBi, alwaysTrueBi, alwaysTrue, listener);
      this.tile = tile;
      this.rate = tile.tier::getOutput;
      this.isCreative = tile.tier == FluidTankTier.CREATIVE;
   }

   @Override
   protected int getRate(@Nullable AutomationType automationType) {
      return automationType == AutomationType.INTERNAL ? this.rate.getAsInt() : super.getRate(automationType);
   }

   @Override
   public FluidStack insert(FluidStack stack, Action action, AutomationType automationType) {
      FluidStack remainder;
      if (this.isCreative && this.isEmpty() && action.execute() && automationType != AutomationType.EXTERNAL) {
         remainder = super.insert(stack, Action.SIMULATE, automationType);
         if (remainder.isEmpty()) {
            this.setStackUnchecked(new FluidStack(stack, this.getCapacity()));
         }
      } else {
         remainder = super.insert(stack, action.combine(!this.isCreative), automationType);
      }

      if (!remainder.isEmpty()) {
         TileEntityFluidTank tileAbove = WorldUtils.getTileEntity(TileEntityFluidTank.class, this.tile.m_58904_(), this.tile.m_58899_().m_7494_());
         if (tileAbove != null) {
            remainder = tileAbove.fluidTank.insert(remainder, action, AutomationType.EXTERNAL);
         }
      }

      return remainder;
   }

   @Override
   public int growStack(int amount, Action action) {
      int grownAmount = super.growStack(amount, action);
      if (amount > 0 && grownAmount < amount && !this.tile.getActive()) {
         TileEntityFluidTank tileAbove = WorldUtils.getTileEntity(TileEntityFluidTank.class, this.tile.m_58904_(), this.tile.m_58899_().m_7494_());
         if (tileAbove != null) {
            int leftOverToInsert = amount - grownAmount;
            FluidStack remainder = tileAbove.fluidTank.insert(new FluidStack(this.stored, leftOverToInsert), action, AutomationType.EXTERNAL);
            grownAmount += leftOverToInsert - remainder.getAmount();
         }
      }

      return grownAmount;
   }

   @Override
   public FluidStack extract(int amount, Action action, AutomationType automationType) {
      return super.extract(amount, action.combine(!this.isCreative), automationType);
   }

   @Override
   public int setStackSize(int amount, Action action) {
      return super.setStackSize(amount, action.combine(!this.isCreative));
   }
}
