package mekanism.common.capabilities.chemical.multiblock;

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
import mekanism.common.capabilities.chemical.variable.VariableCapacityChemicalTank;
import mekanism.common.lib.multiblock.MultiblockData;
import org.jetbrains.annotations.Nullable;

@NothingNullByDefault
public class MultiblockChemicalTankBuilder<CHEMICAL extends Chemical<CHEMICAL>, STACK extends ChemicalStack<CHEMICAL>, TANK extends IChemicalTank<CHEMICAL, STACK>> {
   public static final MultiblockChemicalTankBuilder<Gas, GasStack, IGasTank> GAS = new MultiblockChemicalTankBuilder<>(
      (ChemicalTankBuilder<Gas, GasStack, TANK>)ChemicalTankBuilder.GAS,
      (MultiblockChemicalTankBuilder.MultiblockTankCreator<Gas, GasStack, TANK>)(MultiblockChemicalTankBuilder.MultiblockGasTank::new)
   );
   public static final MultiblockChemicalTankBuilder<InfuseType, InfusionStack, IInfusionTank> INFUSION = new MultiblockChemicalTankBuilder<>(
      (ChemicalTankBuilder<InfuseType, InfusionStack, TANK>)ChemicalTankBuilder.INFUSION,
      (MultiblockChemicalTankBuilder.MultiblockTankCreator<InfuseType, InfusionStack, TANK>)(MultiblockChemicalTankBuilder.MultiblockInfusionTank::new)
   );
   public static final MultiblockChemicalTankBuilder<Pigment, PigmentStack, IPigmentTank> PIGMENT = new MultiblockChemicalTankBuilder<>(
      (ChemicalTankBuilder<Pigment, PigmentStack, TANK>)ChemicalTankBuilder.PIGMENT,
      (MultiblockChemicalTankBuilder.MultiblockTankCreator<Pigment, PigmentStack, TANK>)(MultiblockChemicalTankBuilder.MultiblockPigmentTank::new)
   );
   public static final MultiblockChemicalTankBuilder<Slurry, SlurryStack, ISlurryTank> SLURRY = new MultiblockChemicalTankBuilder<>(
      (ChemicalTankBuilder<Slurry, SlurryStack, TANK>)ChemicalTankBuilder.SLURRY,
      (MultiblockChemicalTankBuilder.MultiblockTankCreator<Slurry, SlurryStack, TANK>)(MultiblockChemicalTankBuilder.MultiblockSlurryTank::new)
   );
   private final MultiblockChemicalTankBuilder.MultiblockTankCreator<CHEMICAL, STACK, TANK> tankCreator;
   private final ChemicalTankBuilder<CHEMICAL, STACK, TANK> tankBuilder;

   private MultiblockChemicalTankBuilder(
      ChemicalTankBuilder<CHEMICAL, STACK, TANK> tankBuilder, MultiblockChemicalTankBuilder.MultiblockTankCreator<CHEMICAL, STACK, TANK> tankCreator
   ) {
      this.tankBuilder = tankBuilder;
      this.tankCreator = tankCreator;
   }

   public TANK create(MultiblockData multiblock, LongSupplier capacity, Predicate<CHEMICAL> validator, @Nullable IContentsListener listener) {
      Objects.requireNonNull(capacity, "Capacity supplier cannot be null");
      Objects.requireNonNull(validator, "Chemical validity check cannot be null");
      return this.createUnchecked(multiblock, capacity, validator, listener);
   }

   private TANK createUnchecked(MultiblockData multiblock, LongSupplier capacity, Predicate<CHEMICAL> validator, @Nullable IContentsListener listener) {
      return this.tankCreator.create(capacity, multiblock.formedBiPred(), multiblock.formedBiPred(), validator, null, listener);
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

   public TANK input(MultiblockData multiblock, LongSupplier capacity, Predicate<CHEMICAL> validator, @Nullable IContentsListener listener) {
      return this.input(multiblock, capacity, validator, null, listener);
   }

   public TANK input(
      MultiblockData multiblock,
      LongSupplier capacity,
      Predicate<CHEMICAL> validator,
      @Nullable ChemicalAttributeValidator attributeValidator,
      @Nullable IContentsListener listener
   ) {
      return this.create(capacity, multiblock.notExternalFormedBiPred(), multiblock.formedBiPred(), validator, attributeValidator, listener);
   }

   public TANK output(MultiblockData multiblock, LongSupplier capacity, Predicate<CHEMICAL> validator, @Nullable IContentsListener listener) {
      return this.output(multiblock, capacity, validator, null, listener);
   }

   public TANK output(
      MultiblockData multiblock,
      LongSupplier capacity,
      Predicate<CHEMICAL> validator,
      @Nullable ChemicalAttributeValidator attributeValidator,
      @Nullable IContentsListener listener
   ) {
      return this.create(capacity, multiblock.formedBiPred(), multiblock.notExternalFormedBiPred(), validator, attributeValidator, listener);
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
      Objects.requireNonNull(validator, "Chemical validity check cannot be null");
      Objects.requireNonNull(canExtract, "Extraction validity check cannot be null");
      Objects.requireNonNull(canInsert, "Insertion validity check cannot be null");
      return this.tankCreator.create(capacity, canExtract, canInsert, validator, attributeValidator, listener);
   }

   public static class MultiblockGasTank extends VariableCapacityChemicalTank<Gas, GasStack> implements IGasHandler, IGasTank {
      protected MultiblockGasTank(
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

   public static class MultiblockInfusionTank extends VariableCapacityChemicalTank<InfuseType, InfusionStack> implements IInfusionHandler, IInfusionTank {
      protected MultiblockInfusionTank(
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

   public static class MultiblockPigmentTank extends VariableCapacityChemicalTank<Pigment, PigmentStack> implements IPigmentHandler, IPigmentTank {
      protected MultiblockPigmentTank(
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

   public static class MultiblockSlurryTank extends VariableCapacityChemicalTank<Slurry, SlurryStack> implements ISlurryHandler, ISlurryTank {
      protected MultiblockSlurryTank(
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
   private interface MultiblockTankCreator<CHEMICAL extends Chemical<CHEMICAL>, STACK extends ChemicalStack<CHEMICAL>, TANK extends IChemicalTank<CHEMICAL, STACK>> {
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
