package mekanism.common.capabilities.chemical.item;

import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.function.BiPredicate;
import java.util.function.Function;
import java.util.function.LongSupplier;
import java.util.function.Predicate;
import mekanism.api.AutomationType;
import mekanism.api.IContentsListener;
import mekanism.api.annotations.NothingNullByDefault;
import mekanism.api.chemical.ChemicalTankBuilder;
import mekanism.api.chemical.attribute.ChemicalAttributeValidator;
import mekanism.api.chemical.gas.Gas;
import mekanism.api.chemical.gas.IGasTank;
import mekanism.common.capabilities.chemical.variable.RateLimitChemicalTank;
import org.jetbrains.annotations.Nullable;

@NothingNullByDefault
public class RateLimitGasHandler extends ItemStackMekanismGasHandler {
   private final IGasTank tank;

   public static RateLimitGasHandler create(LongSupplier rate, LongSupplier capacity) {
      return create(rate, capacity, ChemicalTankBuilder.GAS.alwaysTrueBi, ChemicalTankBuilder.GAS.alwaysTrueBi, ChemicalTankBuilder.GAS.alwaysTrue, null);
   }

   public static RateLimitGasHandler create(
      LongSupplier rate, LongSupplier capacity, BiPredicate<Gas, AutomationType> canExtract, BiPredicate<Gas, AutomationType> canInsert, Predicate<Gas> isValid
   ) {
      return create(rate, capacity, canExtract, canInsert, isValid, null);
   }

   public static RateLimitGasHandler create(
      LongSupplier rate,
      LongSupplier capacity,
      BiPredicate<Gas, AutomationType> canExtract,
      BiPredicate<Gas, AutomationType> canInsert,
      Predicate<Gas> isValid,
      @Nullable ChemicalAttributeValidator attributeValidator
   ) {
      Objects.requireNonNull(rate, "Rate supplier cannot be null");
      Objects.requireNonNull(capacity, "Capacity supplier cannot be null");
      Objects.requireNonNull(canExtract, "Extraction validity check cannot be null");
      Objects.requireNonNull(canInsert, "Insertion validity check cannot be null");
      Objects.requireNonNull(isValid, "Gas validity check cannot be null");
      return new RateLimitGasHandler(
         listener -> new RateLimitChemicalTank.RateLimitGasTank(rate, capacity, canExtract, canInsert, isValid, attributeValidator, listener)
      );
   }

   private RateLimitGasHandler(Function<IContentsListener, IGasTank> tankProvider) {
      this.tank = tankProvider.apply(this);
   }

   @Override
   protected List<IGasTank> getInitialTanks() {
      return Collections.singletonList(this.tank);
   }
}
