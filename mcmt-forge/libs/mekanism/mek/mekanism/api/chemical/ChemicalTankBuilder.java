package mekanism.api.chemical;

import java.util.Objects;
import java.util.function.BiPredicate;
import java.util.function.Predicate;
import mekanism.api.AutomationType;
import mekanism.api.IContentsListener;
import mekanism.api.annotations.NothingNullByDefault;
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
import mekanism.api.functions.ConstantPredicates;
import org.jetbrains.annotations.Nullable;

@NothingNullByDefault
public class ChemicalTankBuilder<CHEMICAL extends Chemical<CHEMICAL>, STACK extends ChemicalStack<CHEMICAL>, TANK extends IChemicalTank<CHEMICAL, STACK>> {
   public static final ChemicalTankBuilder<Gas, GasStack, IGasTank> GAS = new ChemicalTankBuilder<>(
      (ChemicalTankBuilder.BasicTankCreator<Gas, GasStack, TANK>)(ChemicalTankBuilder.BasicGasTank::new)
   );
   public static final ChemicalTankBuilder<InfuseType, InfusionStack, IInfusionTank> INFUSION = new ChemicalTankBuilder<>(
      (ChemicalTankBuilder.BasicTankCreator<InfuseType, InfusionStack, TANK>)(ChemicalTankBuilder.BasicInfusionTank::new)
   );
   public static final ChemicalTankBuilder<Pigment, PigmentStack, IPigmentTank> PIGMENT = new ChemicalTankBuilder<>(
      (ChemicalTankBuilder.BasicTankCreator<Pigment, PigmentStack, TANK>)(ChemicalTankBuilder.BasicPigmentTank::new)
   );
   public static final ChemicalTankBuilder<Slurry, SlurryStack, ISlurryTank> SLURRY = new ChemicalTankBuilder<>(
      (ChemicalTankBuilder.BasicTankCreator<Slurry, SlurryStack, TANK>)(ChemicalTankBuilder.BasicSlurryTank::new)
   );
   public final Predicate<CHEMICAL> alwaysTrue = ConstantPredicates.alwaysTrue();
   public final Predicate<CHEMICAL> alwaysFalse = ConstantPredicates.alwaysFalse();
   public final BiPredicate<CHEMICAL, AutomationType> alwaysTrueBi = ConstantPredicates.alwaysTrueBi();
   public final BiPredicate<CHEMICAL, AutomationType> internalOnly = ConstantPredicates.internalOnly();
   public final BiPredicate<CHEMICAL, AutomationType> notExternal = ConstantPredicates.notExternal();
   private final ChemicalTankBuilder.BasicTankCreator<CHEMICAL, STACK, TANK> tankCreator;

   private ChemicalTankBuilder(ChemicalTankBuilder.BasicTankCreator<CHEMICAL, STACK, TANK> tankCreator) {
      this.tankCreator = tankCreator;
   }

   public TANK createDummy(long capacity) {
      return this.createAllValid(capacity, null);
   }

   public TANK create(long capacity, @Nullable IContentsListener listener) {
      return this.createWithValidator(capacity, null, listener);
   }

   public TANK createWithValidator(long capacity, @Nullable ChemicalAttributeValidator attributeValidator, @Nullable IContentsListener listener) {
      if (capacity < 0L) {
         throw new IllegalArgumentException("Capacity must be at least zero");
      } else {
         return this.tankCreator.create(capacity, this.alwaysTrueBi, this.alwaysTrueBi, this.alwaysTrue, attributeValidator, listener);
      }
   }

   public TANK createAllValid(long capacity, @Nullable IContentsListener listener) {
      return this.createWithValidator(capacity, ChemicalAttributeValidator.ALWAYS_ALLOW, listener);
   }

   public TANK create(long capacity, Predicate<CHEMICAL> canExtract, Predicate<CHEMICAL> canInsert, @Nullable IContentsListener listener) {
      return this.create(capacity, canExtract, canInsert, this.alwaysTrue, listener);
   }

   public TANK create(long capacity, Predicate<CHEMICAL> validator, @Nullable IContentsListener listener) {
      if (capacity < 0L) {
         throw new IllegalArgumentException("Capacity must be at least zero");
      } else {
         Objects.requireNonNull(validator, "Chemical validity check cannot be null");
         return this.tankCreator.create(capacity, this.alwaysTrueBi, this.alwaysTrueBi, validator, null, listener);
      }
   }

   public TANK input(long capacity, Predicate<CHEMICAL> validator, @Nullable IContentsListener listener) {
      if (capacity < 0L) {
         throw new IllegalArgumentException("Capacity must be at least zero");
      } else {
         Objects.requireNonNull(validator, "Chemical validity check cannot be null");
         return this.tankCreator.create(capacity, this.notExternal, this.alwaysTrueBi, validator, null, listener);
      }
   }

   public TANK input(long capacity, Predicate<CHEMICAL> canInsert, Predicate<CHEMICAL> validator, @Nullable IContentsListener listener) {
      if (capacity < 0L) {
         throw new IllegalArgumentException("Capacity must be at least zero");
      } else {
         Objects.requireNonNull(canInsert, "Insertion validity check cannot be null");
         Objects.requireNonNull(validator, "Chemical validity check cannot be null");
         return this.tankCreator.create(capacity, this.notExternal, (stack, automationType) -> canInsert.test(stack), validator, null, listener);
      }
   }

   public TANK output(long capacity, @Nullable IContentsListener listener) {
      if (capacity < 0L) {
         throw new IllegalArgumentException("Capacity must be at least zero");
      } else {
         return this.tankCreator.create(capacity, this.alwaysTrueBi, this.internalOnly, this.alwaysTrue, ChemicalAttributeValidator.ALWAYS_ALLOW, listener);
      }
   }

   public TANK create(
      long capacity, Predicate<CHEMICAL> canExtract, Predicate<CHEMICAL> canInsert, Predicate<CHEMICAL> validator, @Nullable IContentsListener listener
   ) {
      return this.create(capacity, canExtract, canInsert, validator, null, listener);
   }

   public TANK create(
      long capacity,
      BiPredicate<CHEMICAL, AutomationType> canExtract,
      BiPredicate<CHEMICAL, AutomationType> canInsert,
      Predicate<CHEMICAL> validator,
      @Nullable IContentsListener listener
   ) {
      return this.create(capacity, canExtract, canInsert, validator, null, listener);
   }

   public TANK create(
      long capacity,
      Predicate<CHEMICAL> canExtract,
      Predicate<CHEMICAL> canInsert,
      Predicate<CHEMICAL> validator,
      @Nullable ChemicalAttributeValidator attributeValidator,
      @Nullable IContentsListener listener
   ) {
      if (capacity < 0L) {
         throw new IllegalArgumentException("Capacity must be at least zero");
      } else {
         Objects.requireNonNull(canExtract, "Extraction validity check cannot be null");
         Objects.requireNonNull(canInsert, "Insertion validity check cannot be null");
         Objects.requireNonNull(validator, "Chemical validity check cannot be null");
         return this.createUnchecked(capacity, canExtract, canInsert, validator, attributeValidator, listener);
      }
   }

   public TANK create(
      long capacity,
      BiPredicate<CHEMICAL, AutomationType> canExtract,
      BiPredicate<CHEMICAL, AutomationType> canInsert,
      Predicate<CHEMICAL> validator,
      @Nullable ChemicalAttributeValidator attributeValidator,
      @Nullable IContentsListener listener
   ) {
      if (capacity < 0L) {
         throw new IllegalArgumentException("Capacity must be at least zero");
      } else {
         Objects.requireNonNull(canExtract, "Extraction validity check cannot be null");
         Objects.requireNonNull(canInsert, "Insertion validity check cannot be null");
         Objects.requireNonNull(validator, "Chemical validity check cannot be null");
         return this.tankCreator.create(capacity, canExtract, canInsert, validator, attributeValidator, listener);
      }
   }

   private TANK createUnchecked(
      long capacity,
      Predicate<CHEMICAL> canExtract,
      Predicate<CHEMICAL> canInsert,
      Predicate<CHEMICAL> validator,
      @Nullable ChemicalAttributeValidator attributeValidator,
      @Nullable IContentsListener listener
   ) {
      return this.tankCreator
         .create(
            capacity,
            (stack, automationType) -> automationType == AutomationType.MANUAL || canExtract.test(stack),
            (stack, automationType) -> canInsert.test(stack),
            validator,
            attributeValidator,
            listener
         );
   }

   public static class BasicGasTank extends BasicChemicalTank<Gas, GasStack> implements IGasHandler, IGasTank {
      protected BasicGasTank(
         long capacity,
         BiPredicate<Gas, AutomationType> canExtract,
         BiPredicate<Gas, AutomationType> canInsert,
         Predicate<Gas> validator,
         @Nullable ChemicalAttributeValidator attributeValidator,
         @Nullable IContentsListener listener
      ) {
         super(capacity, canExtract, canInsert, validator, attributeValidator, listener);
      }
   }

   public static class BasicInfusionTank extends BasicChemicalTank<InfuseType, InfusionStack> implements IInfusionHandler, IInfusionTank {
      protected BasicInfusionTank(
         long capacity,
         BiPredicate<InfuseType, AutomationType> canExtract,
         BiPredicate<InfuseType, AutomationType> canInsert,
         Predicate<InfuseType> validator,
         @Nullable ChemicalAttributeValidator attributeValidator,
         @Nullable IContentsListener listener
      ) {
         super(capacity, canExtract, canInsert, validator, attributeValidator, listener);
      }
   }

   public static class BasicPigmentTank extends BasicChemicalTank<Pigment, PigmentStack> implements IPigmentHandler, IPigmentTank {
      protected BasicPigmentTank(
         long capacity,
         BiPredicate<Pigment, AutomationType> canExtract,
         BiPredicate<Pigment, AutomationType> canInsert,
         Predicate<Pigment> validator,
         @Nullable ChemicalAttributeValidator attributeValidator,
         @Nullable IContentsListener listener
      ) {
         super(capacity, canExtract, canInsert, validator, attributeValidator, listener);
      }
   }

   public static class BasicSlurryTank extends BasicChemicalTank<Slurry, SlurryStack> implements ISlurryHandler, ISlurryTank {
      protected BasicSlurryTank(
         long capacity,
         BiPredicate<Slurry, AutomationType> canExtract,
         BiPredicate<Slurry, AutomationType> canInsert,
         Predicate<Slurry> validator,
         @Nullable ChemicalAttributeValidator attributeValidator,
         @Nullable IContentsListener listener
      ) {
         super(capacity, canExtract, canInsert, validator, attributeValidator, listener);
      }
   }

   @FunctionalInterface
   private interface BasicTankCreator<CHEMICAL extends Chemical<CHEMICAL>, STACK extends ChemicalStack<CHEMICAL>, TANK extends IChemicalTank<CHEMICAL, STACK>> {
      TANK create(
         long var1,
         BiPredicate<CHEMICAL, AutomationType> var3,
         BiPredicate<CHEMICAL, AutomationType> var4,
         Predicate<CHEMICAL> var5,
         @Nullable ChemicalAttributeValidator var6,
         @Nullable IContentsListener var7
      );
   }
}
