package mekanism.common.capabilities.energy.item;

import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.function.Function;
import java.util.function.Predicate;
import mekanism.api.Action;
import mekanism.api.AutomationType;
import mekanism.api.IContentsListener;
import mekanism.api.annotations.NothingNullByDefault;
import mekanism.api.energy.IEnergyContainer;
import mekanism.api.energy.IMekanismStrictEnergyHandler;
import mekanism.api.math.FloatingLong;
import mekanism.api.math.FloatingLongSupplier;
import mekanism.common.capabilities.energy.BasicEnergyContainer;
import mekanism.common.capabilities.energy.VariableCapacityEnergyContainer;
import mekanism.common.tier.EnergyCubeTier;
import org.jetbrains.annotations.Nullable;

@NothingNullByDefault
public class RateLimitEnergyHandler extends ItemStackEnergyHandler {
   private final IEnergyContainer energyContainer;

   public static RateLimitEnergyHandler create(EnergyCubeTier tier) {
      Objects.requireNonNull(tier, "Energy cube tier cannot be null");
      return new RateLimitEnergyHandler(handler -> new RateLimitEnergyHandler.EnergyCubeRateLimitEnergyContainer(tier, handler));
   }

   public static RateLimitEnergyHandler create(FloatingLongSupplier capacity, Predicate<AutomationType> canExtract, Predicate<AutomationType> canInsert) {
      return create(() -> capacity.get().multiply(0.005), capacity, canExtract, canInsert);
   }

   public static RateLimitEnergyHandler create(
      FloatingLongSupplier rate, FloatingLongSupplier capacity, Predicate<AutomationType> canExtract, Predicate<AutomationType> canInsert
   ) {
      Objects.requireNonNull(rate, "Rate supplier cannot be null");
      Objects.requireNonNull(capacity, "Capacity supplier cannot be null");
      Objects.requireNonNull(canExtract, "Extraction validity check cannot be null");
      Objects.requireNonNull(canInsert, "Insertion validity check cannot be null");
      return new RateLimitEnergyHandler(handler -> new RateLimitEnergyHandler.RateLimitEnergyContainer(rate, capacity, canExtract, canInsert, handler));
   }

   private RateLimitEnergyHandler(Function<IMekanismStrictEnergyHandler, IEnergyContainer> energyContainerProvider) {
      this.energyContainer = energyContainerProvider.apply(this);
   }

   @Override
   protected List<IEnergyContainer> getInitialContainers() {
      return Collections.singletonList(this.energyContainer);
   }

   private static class EnergyCubeRateLimitEnergyContainer extends RateLimitEnergyHandler.RateLimitEnergyContainer {
      private final boolean isCreative;

      private EnergyCubeRateLimitEnergyContainer(EnergyCubeTier tier, @Nullable IContentsListener listener) {
         super(tier::getOutput, tier::getMaxEnergy, BasicEnergyContainer.alwaysTrue, BasicEnergyContainer.alwaysTrue, listener);
         this.isCreative = tier == EnergyCubeTier.CREATIVE;
      }

      @Override
      public FloatingLong insert(FloatingLong amount, Action action, AutomationType automationType) {
         return super.insert(amount, action.combine(!this.isCreative), automationType);
      }

      @Override
      public FloatingLong extract(FloatingLong amount, Action action, AutomationType automationType) {
         return super.extract(amount, action.combine(!this.isCreative), automationType);
      }
   }

   private static class RateLimitEnergyContainer extends VariableCapacityEnergyContainer {
      private final FloatingLongSupplier rate;

      private RateLimitEnergyContainer(
         FloatingLongSupplier rate,
         FloatingLongSupplier capacity,
         Predicate<AutomationType> canExtract,
         Predicate<AutomationType> canInsert,
         @Nullable IContentsListener listener
      ) {
         super(capacity, canExtract, canInsert, listener);
         this.rate = rate;
      }

      @Override
      protected FloatingLong getRate(@Nullable AutomationType automationType) {
         return automationType != null && automationType != AutomationType.MANUAL ? this.rate.get() : super.getRate(automationType);
      }
   }
}
