package mekanism.common.capabilities.chemical.variable;

import java.util.Objects;
import java.util.function.BiPredicate;
import java.util.function.LongSupplier;
import java.util.function.Predicate;
import mekanism.api.AutomationType;
import mekanism.api.IContentsListener;
import mekanism.api.annotations.NothingNullByDefault;
import mekanism.api.chemical.Chemical;
import mekanism.api.chemical.ChemicalStack;
import mekanism.api.chemical.ChemicalTankBuilder;
import mekanism.api.chemical.IChemicalTank;
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
public class VariableCapacityChemicalTankBuilder<CHEMICAL extends Chemical<CHEMICAL>, STACK extends ChemicalStack<CHEMICAL>, TANK extends IChemicalTank<CHEMICAL, STACK>> {
   public static final VariableCapacityChemicalTankBuilder<Gas, GasStack, IGasTank> GAS = new VariableCapacityChemicalTankBuilder<>(
      (ChemicalTankBuilder<Gas, GasStack, TANK>)ChemicalTankBuilder.GAS,
      (VariableCapacityChemicalTankBuilder.VariableCapacityTankCreator<Gas, GasStack, TANK>)(VariableCapacityChemicalTankBuilder.VariableCapacityGasTank::new)
   );
   public static final VariableCapacityChemicalTankBuilder<InfuseType, InfusionStack, IInfusionTank> INFUSION = new VariableCapacityChemicalTankBuilder<>(
      (ChemicalTankBuilder<InfuseType, InfusionStack, TANK>)ChemicalTankBuilder.INFUSION,
      (VariableCapacityChemicalTankBuilder.VariableCapacityTankCreator<InfuseType, InfusionStack, TANK>)(VariableCapacityChemicalTankBuilder.VariableCapacityInfusionTank::new)
   );
   public static final VariableCapacityChemicalTankBuilder<Pigment, PigmentStack, IPigmentTank> PIGMENT = new VariableCapacityChemicalTankBuilder<>(
      (ChemicalTankBuilder<Pigment, PigmentStack, TANK>)ChemicalTankBuilder.PIGMENT,
      (VariableCapacityChemicalTankBuilder.VariableCapacityTankCreator<Pigment, PigmentStack, TANK>)(VariableCapacityChemicalTankBuilder.VariableCapacityPigmentTank::new)
   );
   public static final VariableCapacityChemicalTankBuilder<Slurry, SlurryStack, ISlurryTank> SLURRY = new VariableCapacityChemicalTankBuilder<>(
      (ChemicalTankBuilder<Slurry, SlurryStack, TANK>)ChemicalTankBuilder.SLURRY,
      (VariableCapacityChemicalTankBuilder.VariableCapacityTankCreator<Slurry, SlurryStack, TANK>)(VariableCapacityChemicalTankBuilder.VariableCapacitySlurryTank::new)
   );
   private final VariableCapacityChemicalTankBuilder.VariableCapacityTankCreator<CHEMICAL, STACK, TANK> tankCreator;
   private final ChemicalTankBuilder<CHEMICAL, STACK, TANK> tankBuilder;

   private VariableCapacityChemicalTankBuilder(
      ChemicalTankBuilder<CHEMICAL, STACK, TANK> tankBuilder,
      VariableCapacityChemicalTankBuilder.VariableCapacityTankCreator<CHEMICAL, STACK, TANK> tankCreator
   ) {
      this.tankBuilder = tankBuilder;
      this.tankCreator = tankCreator;
   }

   public TANK createAllValid(LongSupplier capacity, @Nullable IContentsListener listener) {
      Objects.requireNonNull(capacity, "Capacity supplier cannot be null");
      return this.tankCreator
         .create(
            capacity,
            this.tankBuilder.alwaysTrueBi,
            this.tankBuilder.alwaysTrueBi,
            this.tankBuilder.alwaysTrue,
            ChemicalAttributeValidator.ALWAYS_ALLOW,
            listener
         );
   }

   public TANK create(
      LongSupplier capacity,
      BiPredicate<CHEMICAL, AutomationType> canExtract,
      BiPredicate<CHEMICAL, AutomationType> canInsert,
      Predicate<CHEMICAL> validator,
      @Nullable IContentsListener listener
   ) {
      return this.create(capacity, canExtract, canInsert, validator, null, listener);
   }

   public TANK create(
      LongSupplier capacity,
      BiPredicate<CHEMICAL, AutomationType> canExtract,
      BiPredicate<CHEMICAL, AutomationType> canInsert,
      Predicate<CHEMICAL> validator,
      @Nullable ChemicalAttributeValidator attributeValidator,
      @Nullable IContentsListener listener
   ) {
      Objects.requireNonNull(capacity, "Capacity supplier cannot be null");
      Objects.requireNonNull(canExtract, "Extraction validity check cannot be null");
      Objects.requireNonNull(canInsert, "Insertion validity check cannot be null");
      Objects.requireNonNull(validator, "Chemical validity check cannot be null");
      return this.tankCreator.create(capacity, canExtract, canInsert, validator, attributeValidator, listener);
   }

   public TANK output(LongSupplier capacity, Predicate<CHEMICAL> validator, @Nullable IContentsListener listener) {
      Objects.requireNonNull(capacity, "Capacity supplier cannot be null");
      Objects.requireNonNull(validator, "Chemical validity check cannot be null");
      return this.tankCreator.create(capacity, this.tankBuilder.alwaysTrueBi, this.tankBuilder.internalOnly, validator, null, listener);
   }

   public static class VariableCapacityGasTank extends VariableCapacityChemicalTank<Gas, GasStack> implements IGasHandler, IGasTank {
      protected VariableCapacityGasTank(
         LongSupplier capacity,
         BiPredicate<Gas, AutomationType> canExtract,
         BiPredicate<Gas, AutomationType> canInsert,
         Predicate<Gas> validator,
         @Nullable ChemicalAttributeValidator attributeValidator,
         @Nullable IContentsListener listener
      ) {
         super(capacity, canExtract, canInsert, validator, attributeValidator, listener);
      }
   }

   public static class VariableCapacityInfusionTank extends VariableCapacityChemicalTank<InfuseType, InfusionStack> implements IInfusionHandler, IInfusionTank {
      protected VariableCapacityInfusionTank(
         LongSupplier capacity,
         BiPredicate<InfuseType, AutomationType> canExtract,
         BiPredicate<InfuseType, AutomationType> canInsert,
         Predicate<InfuseType> validator,
         @Nullable ChemicalAttributeValidator attributeValidator,
         @Nullable IContentsListener listener
      ) {
         super(capacity, canExtract, canInsert, validator, attributeValidator, listener);
      }
   }

   public static class VariableCapacityPigmentTank extends VariableCapacityChemicalTank<Pigment, PigmentStack> implements IPigmentHandler, IPigmentTank {
      protected VariableCapacityPigmentTank(
         LongSupplier capacity,
         BiPredicate<Pigment, AutomationType> canExtract,
         BiPredicate<Pigment, AutomationType> canInsert,
         Predicate<Pigment> validator,
         @Nullable ChemicalAttributeValidator attributeValidator,
         @Nullable IContentsListener listener
      ) {
         super(capacity, canExtract, canInsert, validator, attributeValidator, listener);
      }
   }

   public static class VariableCapacitySlurryTank extends VariableCapacityChemicalTank<Slurry, SlurryStack> implements ISlurryHandler, ISlurryTank {
      protected VariableCapacitySlurryTank(
         LongSupplier capacity,
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
   private interface VariableCapacityTankCreator<CHEMICAL extends Chemical<CHEMICAL>, STACK extends ChemicalStack<CHEMICAL>, TANK extends IChemicalTank<CHEMICAL, STACK>> {
      TANK create(
         LongSupplier capacity,
         BiPredicate<CHEMICAL, AutomationType> canExtract,
         BiPredicate<CHEMICAL, AutomationType> canInsert,
         Predicate<CHEMICAL> validator,
         @Nullable ChemicalAttributeValidator attributeValidator,
         @Nullable IContentsListener listener
      );
   }
}
