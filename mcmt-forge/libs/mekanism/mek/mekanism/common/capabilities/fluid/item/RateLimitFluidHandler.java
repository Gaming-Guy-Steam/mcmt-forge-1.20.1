package mekanism.common.capabilities.fluid.item;

import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.function.BiPredicate;
import java.util.function.Function;
import java.util.function.IntSupplier;
import java.util.function.Predicate;
import mekanism.api.Action;
import mekanism.api.AutomationType;
import mekanism.api.IContentsListener;
import mekanism.api.annotations.NothingNullByDefault;
import mekanism.api.fluid.IExtendedFluidTank;
import mekanism.common.capabilities.fluid.BasicFluidTank;
import mekanism.common.capabilities.fluid.VariableCapacityFluidTank;
import mekanism.common.tier.FluidTankTier;
import net.minecraftforge.fluids.FluidStack;
import org.jetbrains.annotations.Nullable;

@NothingNullByDefault
public class RateLimitFluidHandler extends ItemStackMekanismFluidHandler {
   private final IExtendedFluidTank tank;

   public static RateLimitFluidHandler create(IntSupplier rate, IntSupplier capacity) {
      return create(rate, capacity, BasicFluidTank.alwaysTrueBi, BasicFluidTank.alwaysTrueBi, BasicFluidTank.alwaysTrue);
   }

   public static RateLimitFluidHandler create(
      IntSupplier rate,
      IntSupplier capacity,
      BiPredicate<FluidStack, AutomationType> canExtract,
      BiPredicate<FluidStack, AutomationType> canInsert,
      Predicate<FluidStack> isValid
   ) {
      Objects.requireNonNull(rate, "Rate supplier cannot be null");
      Objects.requireNonNull(capacity, "Capacity supplier cannot be null");
      Objects.requireNonNull(canExtract, "Extraction validity check cannot be null");
      Objects.requireNonNull(canInsert, "Insertion validity check cannot be null");
      Objects.requireNonNull(isValid, "Gas validity check cannot be null");
      return new RateLimitFluidHandler(listener -> new RateLimitFluidHandler.RateLimitFluidTank(rate, capacity, canExtract, canInsert, isValid, listener));
   }

   public static RateLimitFluidHandler create(FluidTankTier tier) {
      Objects.requireNonNull(tier, "Fluid tank tier cannot be null");
      return new RateLimitFluidHandler(listener -> new RateLimitFluidHandler.FluidTankRateLimitFluidTank(tier, listener));
   }

   private RateLimitFluidHandler(Function<IContentsListener, IExtendedFluidTank> tankProvider) {
      this.tank = tankProvider.apply(this);
   }

   @Override
   protected List<IExtendedFluidTank> getInitialTanks() {
      return Collections.singletonList(this.tank);
   }

   private static class FluidTankRateLimitFluidTank extends RateLimitFluidHandler.RateLimitFluidTank {
      private final boolean isCreative;

      private FluidTankRateLimitFluidTank(FluidTankTier tier, @Nullable IContentsListener listener) {
         super(tier::getOutput, tier::getStorage, BasicFluidTank.alwaysTrueBi, BasicFluidTank.alwaysTrueBi, BasicFluidTank.alwaysTrue, listener);
         this.isCreative = tier == FluidTankTier.CREATIVE;
      }

      @Override
      public FluidStack insert(FluidStack stack, Action action, AutomationType automationType) {
         return super.insert(stack, action.combine(!this.isCreative), automationType);
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

   public static class RateLimitFluidTank extends VariableCapacityFluidTank {
      private final IntSupplier rate;

      public RateLimitFluidTank(IntSupplier rate, IntSupplier capacity, @Nullable IContentsListener listener) {
         this(rate, capacity, alwaysTrueBi, alwaysTrueBi, alwaysTrue, listener);
      }

      public RateLimitFluidTank(
         IntSupplier rate,
         IntSupplier capacity,
         BiPredicate<FluidStack, AutomationType> canExtract,
         BiPredicate<FluidStack, AutomationType> canInsert,
         Predicate<FluidStack> isValid,
         @Nullable IContentsListener listener
      ) {
         super(capacity, canExtract, canInsert, isValid, listener);
         this.rate = rate;
      }

      @Override
      protected int getRate(@Nullable AutomationType automationType) {
         return automationType != null && automationType != AutomationType.MANUAL ? this.rate.getAsInt() : super.getRate(automationType);
      }
   }
}
