package mekanism.common.config;

import java.util.Locale;
import mekanism.common.config.value.CachedDoubleValue;
import mekanism.common.config.value.CachedFloatingLongValue;
import mekanism.common.config.value.CachedIntValue;
import mekanism.common.config.value.CachedLongValue;
import mekanism.common.tier.BinTier;
import mekanism.common.tier.CableTier;
import mekanism.common.tier.ChemicalTankTier;
import mekanism.common.tier.ConductorTier;
import mekanism.common.tier.EnergyCubeTier;
import mekanism.common.tier.FluidTankTier;
import mekanism.common.tier.InductionCellTier;
import mekanism.common.tier.InductionProviderTier;
import mekanism.common.tier.PipeTier;
import mekanism.common.tier.TransporterTier;
import mekanism.common.tier.TubeTier;
import mekanism.common.util.EnumUtils;
import net.minecraftforge.common.ForgeConfigSpec;
import net.minecraftforge.common.ForgeConfigSpec.Builder;
import net.minecraftforge.fml.config.ModConfig.Type;

public class TierConfig extends BaseMekanismConfig {
   private static final String ENERGY_CUBE_CATEGORY = "energy_cubes";
   private static final String FLUID_TANK_CATEGORY = "fluid_tanks";
   private static final String CHEMICAL_TANK_CATEGORY = "chemical_tanks";
   private static final String BIN_CATEGORY = "bins";
   private static final String INDUCTION_CATEGORY = "induction";
   private static final String TRANSMITTER_CATEGORY = "transmitters";
   private static final String ENERGY_CATEGORY = "energy";
   private static final String FLUID_CATEGORY = "fluid";
   private static final String CHEMICAL_CATEGORY = "chemical";
   private static final String ITEMS_CATEGORY = "items";
   private static final String HEAT_CATEGORY = "heat";
   private final ForgeConfigSpec configSpec;

   TierConfig() {
      Builder builder = new Builder();
      builder.comment("Tier Config. This config is synced from server to client.").push("tier");
      this.addEnergyCubeCategory(builder);
      this.addFluidTankCategory(builder);
      this.addGasTankCategory(builder);
      this.addBinCategory(builder);
      this.addInductionCategory(builder);
      this.addTransmittersCategory(builder);
      builder.pop();
      this.configSpec = builder.build();
   }

   private void addEnergyCubeCategory(Builder builder) {
      builder.comment("Energy Cubes").push("energy_cubes");

      for (EnergyCubeTier tier : EnumUtils.ENERGY_CUBE_TIERS) {
         String tierName = tier.getBaseTier().getSimpleName();
         CachedFloatingLongValue storageReference = CachedFloatingLongValue.define(
            this,
            builder,
            "Maximum number of Joules " + tierName + " energy cubes can store.",
            tierName.toLowerCase(Locale.ROOT) + "Storage",
            tier.getBaseMaxEnergy(),
            CachedFloatingLongValue.POSITIVE
         );
         CachedFloatingLongValue outputReference = CachedFloatingLongValue.define(
            this,
            builder,
            "Output rate in Joules of " + tierName + " energy cubes.",
            tierName.toLowerCase(Locale.ROOT) + "Output",
            tier.getBaseOutput(),
            CachedFloatingLongValue.POSITIVE
         );
         tier.setConfigReference(storageReference, outputReference);
      }

      builder.pop();
   }

   private void addFluidTankCategory(Builder builder) {
      builder.comment("Fluid Tanks").push("fluid_tanks");

      for (FluidTankTier tier : EnumUtils.FLUID_TANK_TIERS) {
         String tierName = tier.getBaseTier().getSimpleName();
         CachedIntValue storageReference = CachedIntValue.wrap(
            this,
            builder.comment("Storage size of " + tierName + " fluid tanks in mB.")
               .defineInRange(tierName.toLowerCase(Locale.ROOT) + "Storage", tier.getBaseStorage(), 1, Integer.MAX_VALUE)
         );
         CachedIntValue outputReference = CachedIntValue.wrap(
            this,
            builder.comment("Output rate of " + tierName + " fluid tanks in mB.")
               .defineInRange(tierName.toLowerCase(Locale.ROOT) + "Output", tier.getBaseOutput(), 1, Integer.MAX_VALUE)
         );
         tier.setConfigReference(storageReference, outputReference);
      }

      builder.pop();
   }

   private void addGasTankCategory(Builder builder) {
      builder.comment("Chemical Tanks").push("chemical_tanks");

      for (ChemicalTankTier tier : EnumUtils.CHEMICAL_TANK_TIERS) {
         String tierName = tier.getBaseTier().getSimpleName();
         CachedLongValue storageReference = CachedLongValue.wrap(
            this,
            builder.comment("Storage size of " + tierName + " chemical tanks in mB.")
               .defineInRange(tierName.toLowerCase(Locale.ROOT) + "Storage", tier.getBaseStorage(), 1L, Long.MAX_VALUE)
         );
         CachedLongValue outputReference = CachedLongValue.wrap(
            this,
            builder.comment("Output rate of " + tierName + " chemical tanks in mB.")
               .defineInRange(tierName.toLowerCase(Locale.ROOT) + "Output", tier.getBaseOutput(), 1L, Long.MAX_VALUE)
         );
         tier.setConfigReference(storageReference, outputReference);
      }

      builder.pop();
   }

   private void addBinCategory(Builder builder) {
      builder.comment("Bins").push("bins");

      for (BinTier tier : EnumUtils.BIN_TIERS) {
         String tierName = tier.getBaseTier().getSimpleName();
         CachedIntValue storageReference = CachedIntValue.wrap(
            this,
            builder.comment("The number of items " + tierName + " bins can store.")
               .defineInRange(tierName.toLowerCase(Locale.ROOT) + "Storage", tier.getBaseStorage(), 1, Integer.MAX_VALUE)
         );
         tier.setConfigReference(storageReference);
      }

      builder.pop();
   }

   private void addInductionCategory(Builder builder) {
      builder.comment("Induction").push("induction");

      for (InductionCellTier tier : EnumUtils.INDUCTION_CELL_TIERS) {
         String tierName = tier.getBaseTier().getSimpleName();
         CachedFloatingLongValue storageReference = CachedFloatingLongValue.define(
            this,
            builder,
            "Maximum number of Joules " + tierName + " induction cells can store.",
            tierName.toLowerCase(Locale.ROOT) + "Storage",
            tier.getBaseMaxEnergy(),
            CachedFloatingLongValue.POSITIVE
         );
         tier.setConfigReference(storageReference);
      }

      for (InductionProviderTier tier : EnumUtils.INDUCTION_PROVIDER_TIERS) {
         String tierName = tier.getBaseTier().getSimpleName();
         CachedFloatingLongValue outputReference = CachedFloatingLongValue.define(
            this,
            builder,
            "Maximum number of Joules " + tierName + " induction providers can output or accept.",
            tierName.toLowerCase(Locale.ROOT) + "Output",
            tier.getBaseOutput(),
            CachedFloatingLongValue.POSITIVE
         );
         tier.setConfigReference(outputReference);
      }

      builder.pop();
   }

   private void addTransmittersCategory(Builder builder) {
      builder.comment("Transmitters").push("transmitters");
      this.addUniversalCableCategory(builder);
      this.addMechanicalPipeCategory(builder);
      this.addPressurizedTubesCategory(builder);
      this.addLogisticalTransportersCategory(builder);
      this.addThermodynamicConductorsCategory(builder);
      builder.pop();
   }

   private void addUniversalCableCategory(Builder builder) {
      builder.comment("Universal Cables").push("energy");

      for (CableTier tier : EnumUtils.CABLE_TIERS) {
         String tierName = tier.getBaseTier().getSimpleName();
         CachedFloatingLongValue capacityReference = CachedFloatingLongValue.define(
            this,
            builder,
            "Internal buffer in Joules of each " + tierName + " universal cable.",
            tierName.toLowerCase(Locale.ROOT) + "Capacity",
            tier.getBaseCapacity(),
            CachedFloatingLongValue.POSITIVE
         );
         tier.setConfigReference(capacityReference);
      }

      builder.pop();
   }

   private void addMechanicalPipeCategory(Builder builder) {
      builder.comment("Mechanical Pipes").push("fluid");

      for (PipeTier tier : EnumUtils.PIPE_TIERS) {
         String tierName = tier.getBaseTier().getSimpleName();
         CachedIntValue capacityReference = CachedIntValue.wrap(
            this,
            builder.comment("Capacity of " + tierName + " mechanical pipes in mB.")
               .defineInRange(tierName.toLowerCase(Locale.ROOT) + "Capacity", tier.getBaseCapacity(), 1, Integer.MAX_VALUE)
         );
         CachedIntValue pullReference = CachedIntValue.wrap(
            this,
            builder.comment("Pump rate of " + tierName + " mechanical pipes in mB/t.")
               .defineInRange(tierName.toLowerCase(Locale.ROOT) + "PullAmount", tier.getBasePull(), 1, Integer.MAX_VALUE)
         );
         tier.setConfigReference(capacityReference, pullReference);
      }

      builder.pop();
   }

   private void addPressurizedTubesCategory(Builder builder) {
      builder.comment("Pressurized Tubes").push("chemical");

      for (TubeTier tier : EnumUtils.TUBE_TIERS) {
         String tierName = tier.getBaseTier().getSimpleName();
         CachedLongValue capacityReference = CachedLongValue.wrap(
            this,
            builder.comment("Capacity of " + tierName + " pressurized tubes in mB.")
               .defineInRange(tierName.toLowerCase(Locale.ROOT) + "Capacity", tier.getBaseCapacity(), 1L, Long.MAX_VALUE)
         );
         CachedLongValue pullReference = CachedLongValue.wrap(
            this,
            builder.comment("Pump rate of " + tierName + " pressurized tubes in mB/t.")
               .defineInRange(tierName.toLowerCase(Locale.ROOT) + "PullAmount", tier.getBasePull(), 1L, Long.MAX_VALUE)
         );
         tier.setConfigReference(capacityReference, pullReference);
      }

      builder.pop();
   }

   private void addLogisticalTransportersCategory(Builder builder) {
      builder.comment("Logistical Transporters").push("items");

      for (TransporterTier tier : EnumUtils.TRANSPORTER_TIERS) {
         String tierName = tier.getBaseTier().getSimpleName();
         CachedIntValue pullReference = CachedIntValue.wrap(
            this,
            builder.comment("Item throughput rate of " + tierName + " logistical transporters in items/half second.")
               .defineInRange(tierName.toLowerCase(Locale.ROOT) + "PullAmount", tier.getBasePull(), 1, Integer.MAX_VALUE)
         );
         CachedIntValue speedReference = CachedIntValue.wrap(
            this,
            builder.comment("Five times the travel speed in m/s of " + tierName + " logistical transporter.")
               .defineInRange(tierName.toLowerCase(Locale.ROOT) + "Speed", tier.getBaseSpeed(), 1, Integer.MAX_VALUE)
         );
         tier.setConfigReference(pullReference, speedReference);
      }

      builder.pop();
   }

   private void addThermodynamicConductorsCategory(Builder builder) {
      builder.comment("Thermodynamic Conductors").push("heat");

      for (ConductorTier tier : EnumUtils.CONDUCTOR_TIERS) {
         String tierName = tier.getBaseTier().getSimpleName();
         CachedDoubleValue conductionReference = CachedDoubleValue.wrap(
            this,
            builder.comment("Conduction value of " + tierName + " thermodynamic conductors.")
               .defineInRange(tierName.toLowerCase(Locale.ROOT) + "InverseConduction", tier.getBaseConduction(), 1.0, Double.MAX_VALUE)
         );
         CachedDoubleValue capacityReference = CachedDoubleValue.wrap(
            this,
            builder.comment("Heat capacity of " + tierName + " thermodynamic conductors.")
               .defineInRange(tierName.toLowerCase(Locale.ROOT) + "HeatCapacity", tier.getBaseHeatCapacity(), 1.0, Double.MAX_VALUE)
         );
         CachedDoubleValue insulationReference = CachedDoubleValue.wrap(
            this,
            builder.comment("Insulation value of " + tierName + " thermodynamic conductor.")
               .defineInRange(tierName.toLowerCase(Locale.ROOT) + "Insulation", tier.getBaseConductionInsulation(), 0.0, Double.MAX_VALUE)
         );
         tier.setConfigReference(conductionReference, capacityReference, insulationReference);
      }

      builder.pop();
   }

   @Override
   public String getFileName() {
      return "tiers";
   }

   @Override
   public ForgeConfigSpec getConfigSpec() {
      return this.configSpec;
   }

   @Override
   public Type getConfigType() {
      return Type.SERVER;
   }

   @Override
   public boolean addToContainer() {
      return false;
   }
}
