package mekanism.common.capabilities.holder.chemical;

import java.util.function.BiPredicate;
import java.util.function.Supplier;
import mekanism.api.AutomationType;
import mekanism.api.RelativeSide;
import mekanism.api.chemical.Chemical;
import mekanism.api.chemical.ChemicalStack;
import mekanism.api.chemical.IChemicalTank;
import mekanism.api.chemical.gas.Gas;
import mekanism.api.chemical.gas.GasStack;
import mekanism.api.chemical.gas.IGasTank;
import mekanism.api.chemical.gas.attribute.GasAttributes;
import mekanism.api.chemical.infuse.IInfusionTank;
import mekanism.api.chemical.infuse.InfuseType;
import mekanism.api.chemical.infuse.InfusionStack;
import mekanism.api.chemical.pigment.IPigmentTank;
import mekanism.api.chemical.pigment.Pigment;
import mekanism.api.chemical.pigment.PigmentStack;
import mekanism.api.chemical.slurry.ISlurryTank;
import mekanism.api.chemical.slurry.Slurry;
import mekanism.api.chemical.slurry.SlurryStack;
import mekanism.api.radiation.IRadiationManager;
import mekanism.common.tile.component.TileComponentConfig;
import net.minecraft.core.Direction;
import net.minecraftforge.common.util.NonNullSupplier;
import org.jetbrains.annotations.NotNull;

public class ChemicalTankHelper<CHEMICAL extends Chemical<CHEMICAL>, STACK extends ChemicalStack<CHEMICAL>, TANK extends IChemicalTank<CHEMICAL, STACK>> {
   private final IChemicalTankHolder<CHEMICAL, STACK, TANK> slotHolder;
   private boolean built;

   private ChemicalTankHelper(IChemicalTankHolder<CHEMICAL, STACK, TANK> slotHolder) {
      this.slotHolder = slotHolder;
   }

   public static BiPredicate<Gas, AutomationType> radioactiveInputTankPredicate(NonNullSupplier<IGasTank> outputTank) {
      return (type, automationType) -> automationType != AutomationType.EXTERNAL
         || ((IGasTank)outputTank.get()).isEmpty() && type.has(GasAttributes.Radiation.class) && IRadiationManager.INSTANCE.isRadiationEnabled();
   }

   public static <CHEMICAL extends Chemical<CHEMICAL>, STACK extends ChemicalStack<CHEMICAL>, TANK extends IChemicalTank<CHEMICAL, STACK>> ChemicalTankHelper<CHEMICAL, STACK, TANK> forSide(
      Supplier<Direction> facingSupplier
   ) {
      return new ChemicalTankHelper<>(new ChemicalTankHolder<>(facingSupplier));
   }

   public static ChemicalTankHelper<Gas, GasStack, IGasTank> forSideGasWithConfig(
      Supplier<Direction> facingSupplier, Supplier<TileComponentConfig> configSupplier
   ) {
      return new ChemicalTankHelper<>(new ConfigChemicalTankHolder.ConfigGasTankHolder(facingSupplier, configSupplier));
   }

   public static ChemicalTankHelper<InfuseType, InfusionStack, IInfusionTank> forSideInfusionWithConfig(
      Supplier<Direction> facingSupplier, Supplier<TileComponentConfig> configSupplier
   ) {
      return new ChemicalTankHelper<>(new ConfigChemicalTankHolder.ConfigInfusionTankHolder(facingSupplier, configSupplier));
   }

   public static ChemicalTankHelper<Pigment, PigmentStack, IPigmentTank> forSidePigmentWithConfig(
      Supplier<Direction> facingSupplier, Supplier<TileComponentConfig> configSupplier
   ) {
      return new ChemicalTankHelper<>(new ConfigChemicalTankHolder.ConfigPigmentTankHolder(facingSupplier, configSupplier));
   }

   public static ChemicalTankHelper<Slurry, SlurryStack, ISlurryTank> forSideSlurryWithConfig(
      Supplier<Direction> facingSupplier, Supplier<TileComponentConfig> configSupplier
   ) {
      return new ChemicalTankHelper<>(new ConfigChemicalTankHolder.ConfigSlurryTankHolder(facingSupplier, configSupplier));
   }

   public TANK addTank(@NotNull TANK tank) {
      if (this.built) {
         throw new IllegalStateException("Builder has already built.");
      } else {
         if (this.slotHolder instanceof ChemicalTankHolder<CHEMICAL, STACK, TANK> slotHolder) {
            slotHolder.addTank(tank);
         } else {
            if (!(this.slotHolder instanceof ConfigChemicalTankHolder<CHEMICAL, STACK, TANK> slotHolder)) {
               throw new IllegalArgumentException("Holder does not know how to add tanks");
            }

            slotHolder.addTank(tank);
         }

         return tank;
      }
   }

   public TANK addTank(@NotNull TANK tank, RelativeSide... sides) {
      if (this.built) {
         throw new IllegalStateException("Builder has already built.");
      } else if (this.slotHolder instanceof ChemicalTankHolder<CHEMICAL, STACK, TANK> slotHolder) {
         slotHolder.addTank(tank, sides);
         return tank;
      } else {
         throw new IllegalArgumentException("Holder does not know how to add tanks on specific sides");
      }
   }

   public IChemicalTankHolder<CHEMICAL, STACK, TANK> build() {
      this.built = true;
      return this.slotHolder;
   }
}
