package mekanism.common.capabilities.energy;

import java.util.List;
import java.util.function.Function;
import mekanism.api.Action;
import mekanism.api.IContentsListener;
import mekanism.api.annotations.NothingNullByDefault;
import mekanism.api.energy.IEnergyContainer;
import mekanism.api.energy.IMekanismStrictEnergyHandler;
import mekanism.api.math.FloatingLong;
import mekanism.common.capabilities.DynamicHandler;
import net.minecraft.core.Direction;
import org.jetbrains.annotations.Nullable;

@NothingNullByDefault
public class DynamicStrictEnergyHandler extends DynamicHandler<IEnergyContainer> implements IMekanismStrictEnergyHandler {
   public DynamicStrictEnergyHandler(
      Function<Direction, List<IEnergyContainer>> tankSupplier,
      DynamicHandler.InteractPredicate canExtract,
      DynamicHandler.InteractPredicate canInsert,
      @Nullable IContentsListener listener
   ) {
      super(tankSupplier, canExtract, canInsert, listener);
   }

   @Override
   public List<IEnergyContainer> getEnergyContainers(@Nullable Direction side) {
      return this.containerSupplier.apply(side);
   }

   @Override
   public FloatingLong insertEnergy(int container, FloatingLong amount, @Nullable Direction side, Action action) {
      return this.canInsert.test(container, side) ? IMekanismStrictEnergyHandler.super.insertEnergy(container, amount, side, action) : amount;
   }

   @Override
   public FloatingLong extractEnergy(int container, FloatingLong amount, @Nullable Direction side, Action action) {
      return this.canExtract.test(container, side) ? IMekanismStrictEnergyHandler.super.extractEnergy(container, amount, side, action) : FloatingLong.ZERO;
   }
}
