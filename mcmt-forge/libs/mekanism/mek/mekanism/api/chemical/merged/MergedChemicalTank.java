package mekanism.api.chemical.merged;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.function.BooleanSupplier;
import java.util.function.Predicate;
import mekanism.api.annotations.NothingNullByDefault;
import mekanism.api.chemical.Chemical;
import mekanism.api.chemical.ChemicalStack;
import mekanism.api.chemical.ChemicalType;
import mekanism.api.chemical.IChemicalTank;
import mekanism.api.chemical.gas.Gas;
import mekanism.api.chemical.gas.GasStack;
import mekanism.api.chemical.gas.IGasTank;
import mekanism.api.chemical.infuse.IInfusionTank;
import mekanism.api.chemical.infuse.InfuseType;
import mekanism.api.chemical.infuse.InfusionStack;
import mekanism.api.chemical.pigment.IPigmentTank;
import mekanism.api.chemical.pigment.Pigment;
import mekanism.api.chemical.pigment.PigmentStack;
import mekanism.api.chemical.slurry.ISlurryTank;
import mekanism.api.chemical.slurry.Slurry;
import mekanism.api.chemical.slurry.SlurryStack;
import org.jetbrains.annotations.Nullable;

@NothingNullByDefault
public class MergedChemicalTank {
   private final Map<MergedChemicalTank.ChemicalTankType<?, ?, ?>, IChemicalTank<?, ?>> tankMap = new HashMap<>();

   public static MergedChemicalTank create(IGasTank gasTank, IInfusionTank infusionTank, IPigmentTank pigmentTank, ISlurryTank slurryTank) {
      Objects.requireNonNull(gasTank, "Gas tank cannot be null");
      Objects.requireNonNull(infusionTank, "Infusion tank cannot be null");
      Objects.requireNonNull(pigmentTank, "Pigment tank cannot be null");
      Objects.requireNonNull(slurryTank, "Slurry tank cannot be null");
      return new MergedChemicalTank(gasTank, infusionTank, pigmentTank, slurryTank);
   }

   private MergedChemicalTank(IChemicalTank<?, ?>... allTanks) {
      this(null, allTanks);
   }

   protected MergedChemicalTank(@Nullable BooleanSupplier extraCheck, IChemicalTank<?, ?>... allTanks) {
      for (MergedChemicalTank.ChemicalTankType<?, ?, ?> type : MergedChemicalTank.ChemicalTankType.TYPES) {
         boolean handled = false;

         for (IChemicalTank<?, ?> tank : allTanks) {
            if (type.canHandle(tank)) {
               List<IChemicalTank<?, ?>> otherTanks = Arrays.stream(allTanks).filter(otherTank -> tank != otherTank).toList();
               BooleanSupplier insertionCheck;
               if (extraCheck == null) {
                  insertionCheck = () -> otherTanks.stream().allMatch(IChemicalTank::isEmpty);
               } else {
                  insertionCheck = () -> extraCheck.getAsBoolean() && otherTanks.stream().allMatch(IChemicalTank::isEmpty);
               }

               this.tankMap.put(type, type.createWrapper(this, tank, insertionCheck));
               handled = true;
               break;
            }
         }

         if (!handled) {
            throw new IllegalArgumentException("No chemical tank supplied for type: " + type);
         }
      }
   }

   public Collection<IChemicalTank<?, ?>> getAllTanks() {
      return this.tankMap.values();
   }

   public IChemicalTank<?, ?> getTankForType(ChemicalType chemicalType) {
      return (IChemicalTank<?, ?>)(switch (chemicalType) {
         case GAS -> this.getGasTank();
         case INFUSION -> this.getInfusionTank();
         case PIGMENT -> this.getPigmentTank();
         case SLURRY -> this.getSlurryTank();
      });
   }

   public final IGasTank getGasTank() {
      return (IGasTank)this.tankMap.get(MergedChemicalTank.ChemicalTankType.GAS);
   }

   public final IInfusionTank getInfusionTank() {
      return (IInfusionTank)this.tankMap.get(MergedChemicalTank.ChemicalTankType.INFUSE_TYPE);
   }

   public final IPigmentTank getPigmentTank() {
      return (IPigmentTank)this.tankMap.get(MergedChemicalTank.ChemicalTankType.PIGMENT);
   }

   public final ISlurryTank getSlurryTank() {
      return (ISlurryTank)this.tankMap.get(MergedChemicalTank.ChemicalTankType.SLURRY);
   }

   public MergedChemicalTank.Current getCurrent() {
      if (!this.getGasTank().isEmpty()) {
         return MergedChemicalTank.Current.GAS;
      } else if (!this.getInfusionTank().isEmpty()) {
         return MergedChemicalTank.Current.INFUSION;
      } else if (!this.getPigmentTank().isEmpty()) {
         return MergedChemicalTank.Current.PIGMENT;
      } else {
         return !this.getSlurryTank().isEmpty() ? MergedChemicalTank.Current.SLURRY : MergedChemicalTank.Current.EMPTY;
      }
   }

   public IChemicalTank<?, ?> getTankFromCurrent(MergedChemicalTank.Current current) {
      return (IChemicalTank<?, ?>)(switch (current) {
         case GAS -> this.getGasTank();
         case INFUSION -> this.getInfusionTank();
         case PIGMENT -> this.getPigmentTank();
         case SLURRY -> this.getSlurryTank();
         case EMPTY -> throw new UnsupportedOperationException("Empty chemical type is unsupported for getting current tank.");
      });
   }

   private record ChemicalTankType<CHEMICAL extends Chemical<CHEMICAL>, STACK extends ChemicalStack<CHEMICAL>, TANK extends IChemicalTank<CHEMICAL, STACK>>(
      String type, MergedChemicalTank.IWrapperCreator<CHEMICAL, STACK, TANK> tankWrapper, Predicate<IChemicalTank<?, ?>> tankValidator
   ) {
      private static final List<MergedChemicalTank.ChemicalTankType<?, ?, ?>> TYPES = new ArrayList<>();
      private static final MergedChemicalTank.ChemicalTankType<Gas, GasStack, IGasTank> GAS = new MergedChemicalTank.ChemicalTankType<>(
         "gas", (MergedChemicalTank.IWrapperCreator<Gas, GasStack, TANK>)(MergedChemicalTank.GasTankWrapper::new), tank -> tank instanceof IGasTank
      );
      private static final MergedChemicalTank.ChemicalTankType<InfuseType, InfusionStack, IInfusionTank> INFUSE_TYPE = new MergedChemicalTank.ChemicalTankType<>(
         "infusion",
         (MergedChemicalTank.IWrapperCreator<InfuseType, InfusionStack, TANK>)(MergedChemicalTank.InfusionTankWrapper::new),
         tank -> tank instanceof IInfusionTank
      );
      private static final MergedChemicalTank.ChemicalTankType<Pigment, PigmentStack, IPigmentTank> PIGMENT = new MergedChemicalTank.ChemicalTankType<>(
         "pigment",
         (MergedChemicalTank.IWrapperCreator<Pigment, PigmentStack, TANK>)(MergedChemicalTank.PigmentTankWrapper::new),
         tank -> tank instanceof IPigmentTank
      );
      private static final MergedChemicalTank.ChemicalTankType<Slurry, SlurryStack, ISlurryTank> SLURRY = new MergedChemicalTank.ChemicalTankType<>(
         "slurry",
         (MergedChemicalTank.IWrapperCreator<Slurry, SlurryStack, TANK>)(MergedChemicalTank.SlurryTankWrapper::new),
         tank -> tank instanceof ISlurryTank
      );

      private ChemicalTankType(String type, MergedChemicalTank.IWrapperCreator<CHEMICAL, STACK, TANK> tankWrapper, Predicate<IChemicalTank<?, ?>> tankValidator) {
         TYPES.add(this);
         this.type = type;
         this.tankWrapper = tankWrapper;
         this.tankValidator = tankValidator;
      }

      private boolean canHandle(IChemicalTank<?, ?> tank) {
         return this.tankValidator.test(tank);
      }

      public TANK createWrapper(MergedChemicalTank mergedTank, IChemicalTank<?, ?> tank, BooleanSupplier insertCheck) {
         return this.tankWrapper.create(mergedTank, (TANK)tank, insertCheck);
      }

      @Override
      public String toString() {
         return this.type;
      }
   }

   public static enum Current {
      EMPTY,
      GAS,
      INFUSION,
      PIGMENT,
      SLURRY;
   }

   private static class GasTankWrapper extends ChemicalTankWrapper<Gas, GasStack> implements IGasTank {
      public GasTankWrapper(MergedChemicalTank mergedTank, IGasTank internal, BooleanSupplier insertCheck) {
         super(mergedTank, internal, insertCheck);
      }
   }

   @FunctionalInterface
   private interface IWrapperCreator<CHEMICAL extends Chemical<CHEMICAL>, STACK extends ChemicalStack<CHEMICAL>, TANK extends IChemicalTank<CHEMICAL, STACK>> {
      TANK create(MergedChemicalTank var1, TANK var2, BooleanSupplier var3);
   }

   private static class InfusionTankWrapper extends ChemicalTankWrapper<InfuseType, InfusionStack> implements IInfusionTank {
      public InfusionTankWrapper(MergedChemicalTank mergedTank, IInfusionTank internal, BooleanSupplier insertCheck) {
         super(mergedTank, internal, insertCheck);
      }
   }

   private static class PigmentTankWrapper extends ChemicalTankWrapper<Pigment, PigmentStack> implements IPigmentTank {
      public PigmentTankWrapper(MergedChemicalTank mergedTank, IPigmentTank internal, BooleanSupplier insertCheck) {
         super(mergedTank, internal, insertCheck);
      }
   }

   private static class SlurryTankWrapper extends ChemicalTankWrapper<Slurry, SlurryStack> implements ISlurryTank {
      public SlurryTankWrapper(MergedChemicalTank mergedTank, ISlurryTank internal, BooleanSupplier insertCheck) {
         super(mergedTank, internal, insertCheck);
      }
   }
}
