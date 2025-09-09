package mekanism.api.chemical.gas.attribute;

import java.util.List;
import java.util.function.IntSupplier;
import mekanism.api.MekanismAPI;
import mekanism.api.chemical.attribute.ChemicalAttribute;
import mekanism.api.chemical.gas.Gas;
import mekanism.api.math.FloatingLong;
import mekanism.api.math.FloatingLongSupplier;
import mekanism.api.providers.IGasProvider;
import mekanism.api.radiation.IRadiationManager;
import mekanism.api.text.APILang;
import mekanism.api.text.EnumColor;
import mekanism.api.text.ITooltipHelper;
import net.minecraft.network.chat.Component;

public class GasAttributes {
   private GasAttributes() {
   }

   public abstract static class Coolant extends ChemicalAttribute {
      private final double thermalEnthalpy;
      private final double conductivity;

      private Coolant(double thermalEnthalpy, double conductivity) {
         if (thermalEnthalpy <= 0.0) {
            throw new IllegalArgumentException("Coolant attributes must have a thermal enthalpy greater than zero! Thermal Enthalpy: " + thermalEnthalpy);
         } else if (!(conductivity <= 0.0) && !(conductivity > 1.0)) {
            this.thermalEnthalpy = thermalEnthalpy;
            this.conductivity = conductivity;
         } else {
            throw new IllegalArgumentException("Coolant attributes must have a conductivity greater than zero and at most one! Conductivity: " + conductivity);
         }
      }

      public double getThermalEnthalpy() {
         return this.thermalEnthalpy;
      }

      public double getConductivity() {
         return this.conductivity;
      }

      @Override
      public List<Component> addTooltipText(List<Component> list) {
         super.addTooltipText(list);
         ITooltipHelper tooltipHelper = ITooltipHelper.INSTANCE;
         list.add(
            APILang.CHEMICAL_ATTRIBUTE_COOLANT_EFFICIENCY
               .translateColored(EnumColor.GRAY, new Object[]{EnumColor.INDIGO, tooltipHelper.getPercent(this.conductivity)})
         );
         list.add(
            APILang.CHEMICAL_ATTRIBUTE_COOLANT_ENTHALPY
               .translateColored(
                  EnumColor.GRAY, new Object[]{EnumColor.INDIGO, tooltipHelper.getEnergyPerMBDisplayShort(FloatingLong.createConst(this.thermalEnthalpy))}
               )
         );
         return list;
      }
   }

   public static class CooledCoolant extends GasAttributes.Coolant {
      private final IGasProvider heatedGas;

      public CooledCoolant(IGasProvider heatedGas, double thermalEnthalpy, double conductivity) {
         super(thermalEnthalpy, conductivity);
         this.heatedGas = heatedGas;
      }

      public Gas getHeatedGas() {
         return this.heatedGas.getChemical();
      }
   }

   public static class Fuel extends ChemicalAttribute {
      private final IntSupplier burnTicks;
      private final FloatingLongSupplier energyDensity;

      public Fuel(int burnTicks, FloatingLong energyDensity) {
         if (burnTicks <= 0) {
            throw new IllegalArgumentException("Fuel attributes must burn for at least one tick! Burn Ticks: " + burnTicks);
         } else if (energyDensity.isZero()) {
            throw new IllegalArgumentException("Fuel attributes must have an energy density greater than zero!");
         } else {
            this.burnTicks = () -> burnTicks;
            this.energyDensity = () -> energyDensity;
         }
      }

      public Fuel(IntSupplier burnTicks, FloatingLongSupplier energyDensity) {
         this.burnTicks = burnTicks;
         this.energyDensity = energyDensity;
      }

      public int getBurnTicks() {
         return this.burnTicks.getAsInt();
      }

      public FloatingLong getEnergyPerTick() {
         int ticks = this.getBurnTicks();
         if (ticks < 1) {
            MekanismAPI.logger.warn("Invalid tick count ({}) for Fuel attribute, this number should be at least 1.", ticks);
            return FloatingLong.ZERO;
         } else {
            return ticks == 1 ? this.energyDensity.get() : this.energyDensity.get().divide((long)ticks);
         }
      }

      @Override
      public List<Component> addTooltipText(List<Component> list) {
         super.addTooltipText(list);
         ITooltipHelper tooltipHelper = ITooltipHelper.INSTANCE;
         list.add(
            APILang.CHEMICAL_ATTRIBUTE_FUEL_BURN_TICKS
               .translateColored(EnumColor.GRAY, new Object[]{EnumColor.INDIGO, tooltipHelper.getFormattedNumber(this.getBurnTicks())})
         );
         list.add(
            APILang.CHEMICAL_ATTRIBUTE_FUEL_ENERGY_DENSITY
               .translateColored(EnumColor.GRAY, new Object[]{EnumColor.INDIGO, tooltipHelper.getEnergyPerMBDisplayShort(this.energyDensity.get())})
         );
         return list;
      }
   }

   public static class HeatedCoolant extends GasAttributes.Coolant {
      private final IGasProvider cooledGas;

      public HeatedCoolant(IGasProvider cooledGas, double thermalEnthalpy, double conductivity) {
         super(thermalEnthalpy, conductivity);
         this.cooledGas = cooledGas;
      }

      public Gas getCooledGas() {
         return this.cooledGas.getChemical();
      }
   }

   public static class Radiation extends ChemicalAttribute {
      private final double radioactivity;

      public Radiation(double radioactivity) {
         if (radioactivity <= 0.0) {
            throw new IllegalArgumentException("Radiation attribute should only be used when there actually is radiation! Radioactivity: " + radioactivity);
         } else {
            this.radioactivity = radioactivity;
         }
      }

      public double getRadioactivity() {
         return this.radioactivity;
      }

      @Override
      public boolean needsValidation() {
         return IRadiationManager.INSTANCE.isRadiationEnabled();
      }

      @Override
      public List<Component> addTooltipText(List<Component> list) {
         super.addTooltipText(list);
         if (this.needsValidation()) {
            ITooltipHelper tooltipHelper = ITooltipHelper.INSTANCE;
            list.add(
               APILang.CHEMICAL_ATTRIBUTE_RADIATION
                  .translateColored(EnumColor.GRAY, new Object[]{EnumColor.INDIGO, tooltipHelper.getRadioactivityDisplayShort(this.getRadioactivity())})
            );
         }

         return list;
      }
   }
}
