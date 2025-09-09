package mekanism.common.capabilities.chemical.variable;

import java.util.function.BiPredicate;
import java.util.function.LongSupplier;
import java.util.function.Predicate;
import mekanism.api.AutomationType;
import mekanism.api.IContentsListener;
import mekanism.api.annotations.NothingNullByDefault;
import mekanism.api.chemical.Chemical;
import mekanism.api.chemical.ChemicalStack;
import mekanism.api.chemical.attribute.ChemicalAttributeValidator;
import mekanism.api.chemical.gas.Gas;
import mekanism.api.chemical.gas.GasStack;
import mekanism.api.chemical.gas.IGasHandler;
import mekanism.api.chemical.gas.IGasTank;
import mekanism.api.chemical.infuse.IInfusionHandler;
import mekanism.api.chemical.infuse.IInfusionTank;
import mekanism.api.chemical.infuse.InfuseType;
import mekanism.api.chemical.infuse.InfusionStack;
import mekanism.api.chemical.pigment.IPigmentHandler;
import mekanism.api.chemical.pigment.IPigmentTank;
import mekanism.api.chemical.pigment.Pigment;
import mekanism.api.chemical.pigment.PigmentStack;
import mekanism.api.chemical.slurry.ISlurryHandler;
import mekanism.api.chemical.slurry.ISlurryTank;
import mekanism.api.chemical.slurry.Slurry;
import mekanism.api.chemical.slurry.SlurryStack;
import org.jetbrains.annotations.Nullable;

@NothingNullByDefault
public abstract class RateLimitChemicalTank<CHEMICAL extends Chemical<CHEMICAL>, STACK extends ChemicalStack<CHEMICAL>>
   extends VariableCapacityChemicalTank<CHEMICAL, STACK> {
   private final LongSupplier rate;

   public RateLimitChemicalTank(
      LongSupplier rate,
      LongSupplier capacity,
      BiPredicate<CHEMICAL, AutomationType> canExtract,
      BiPredicate<CHEMICAL, AutomationType> canInsert,
      Predicate<CHEMICAL> isValid,
      @Nullable ChemicalAttributeValidator attributeValidator,
      @Nullable IContentsListener listener
   ) {
      super(capacity, canExtract, canInsert, isValid, attributeValidator, listener);
      this.rate = rate;
   }

   @Override
   protected long getRate(@Nullable AutomationType automationType) {
      return automationType != null && automationType != AutomationType.MANUAL ? this.rate.getAsLong() : super.getRate(automationType);
   }

   public static class RateLimitGasTank extends RateLimitChemicalTank<Gas, GasStack> implements IGasHandler, IGasTank {
      public RateLimitGasTank(
         LongSupplier rate,
         LongSupplier capacity,
         BiPredicate<Gas, AutomationType> canExtract,
         BiPredicate<Gas, AutomationType> canInsert,
         Predicate<Gas> isValid,
         @Nullable ChemicalAttributeValidator attributeValidator,
         @Nullable IContentsListener listener
      ) {
         super(rate, capacity, canExtract, canInsert, isValid, attributeValidator, listener);
      }
   }

   public static class RateLimitInfusionTank extends RateLimitChemicalTank<InfuseType, InfusionStack> implements IInfusionHandler, IInfusionTank {
      public RateLimitInfusionTank(
         LongSupplier rate,
         LongSupplier capacity,
         BiPredicate<InfuseType, AutomationType> canExtract,
         BiPredicate<InfuseType, AutomationType> canInsert,
         Predicate<InfuseType> isValid,
         @Nullable IContentsListener listener
      ) {
         super(rate, capacity, canExtract, canInsert, isValid, null, listener);
      }
   }

   public static class RateLimitPigmentTank extends RateLimitChemicalTank<Pigment, PigmentStack> implements IPigmentHandler, IPigmentTank {
      public RateLimitPigmentTank(
         LongSupplier rate,
         LongSupplier capacity,
         BiPredicate<Pigment, AutomationType> canExtract,
         BiPredicate<Pigment, AutomationType> canInsert,
         Predicate<Pigment> isValid,
         @Nullable IContentsListener listener
      ) {
         super(rate, capacity, canExtract, canInsert, isValid, null, listener);
      }
   }

   public static class RateLimitSlurryTank extends RateLimitChemicalTank<Slurry, SlurryStack> implements ISlurryHandler, ISlurryTank {
      public RateLimitSlurryTank(
         LongSupplier rate,
         LongSupplier capacity,
         BiPredicate<Slurry, AutomationType> canExtract,
         BiPredicate<Slurry, AutomationType> canInsert,
         Predicate<Slurry> isValid,
         @Nullable IContentsListener listener
      ) {
         super(rate, capacity, canExtract, canInsert, isValid, null, listener);
      }
   }
}
