package mekanism.common.config;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import mekanism.api.math.FloatingLong;
import mekanism.common.config.value.CachedBooleanValue;
import mekanism.common.config.value.CachedConfigValue;
import mekanism.common.config.value.CachedDoubleValue;
import mekanism.common.config.value.CachedFloatValue;
import mekanism.common.config.value.CachedFloatingLongValue;
import mekanism.common.config.value.CachedIntValue;
import mekanism.common.config.value.CachedLongValue;
import mekanism.common.config.value.CachedOredictionificatorConfigValue;
import mekanism.common.tier.ChemicalTankTier;
import mekanism.common.tier.EnergyCubeTier;
import mekanism.common.tier.FluidTankTier;
import net.minecraft.resources.ResourceLocation;
import net.minecraftforge.common.ForgeConfigSpec;
import net.minecraftforge.common.ForgeConfigSpec.Builder;
import net.minecraftforge.fml.config.ModConfig.Type;

public class GeneralConfig extends BaseMekanismConfig {
   private static final String CONVERSION_CATEGORY = "energy_conversion";
   private static final String EJECT_CATEGORY = "auto_eject";
   private static final String MINER_CATEGORY = "digital_miner";
   private static final String DYNAMIC_TANK = "dynamic_tank";
   private static final String LASER_SETTINGS = "laser";
   private static final String OREDICTIONIFICATOR_CATEGORY = "oredictionificator";
   private static final String PUMP_CATEGORY = "pump";
   private static final String ENTANGLOPORTER_CATEGORY = "quantum_entangloporter";
   private static final String SECURITY_CATEGORY = "security";
   private static final String BOILER_CATEGORY = "boiler";
   private static final String EVAPORATION_CATEGORY = "thermal_evaporation";
   private static final String SPS_CATEGORY = "sps";
   private static final String RADIATION_CATEGORY = "radiation";
   private static final String PREFILLED_CATEGORY = "prefilled";
   private static final String NUTRITIONAL_PASTE_CATEGORY = "nutritional_paste";
   private final ForgeConfigSpec configSpec;
   public final CachedBooleanValue logPackets;
   public final CachedBooleanValue allowChunkloading;
   public final CachedBooleanValue easyMinerFilters;
   public final CachedIntValue blockDeactivationDelay;
   public final CachedConfigValue<List<? extends String>> cardboardModBlacklist;
   public final CachedBooleanValue transmitterAlloyUpgrade;
   public final CachedIntValue maxUpgradeMultiplier;
   public final CachedDoubleValue boilerWaterConductivity;
   public final CachedDoubleValue heatPerFuelTick;
   public final CachedIntValue fuelwoodTickMultiplier;
   public final CachedDoubleValue resistiveHeaterEfficiency;
   public final CachedDoubleValue superheatingHeatTransfer;
   public final CachedIntValue maxSolarNeutronActivatorRate;
   public final CachedIntValue fluidAutoEjectRate;
   public final CachedLongValue chemicalAutoEjectRate;
   public final CachedDoubleValue dumpExcessKeepRatio;
   public final CachedIntValue dynamicTankFluidPerTank;
   public final CachedLongValue dynamicTankChemicalPerTank;
   public final CachedBooleanValue prefilledFluidTanks;
   public final CachedBooleanValue prefilledGasTanks;
   public final CachedBooleanValue prefilledInfusionTanks;
   public final CachedBooleanValue prefilledPigmentTanks;
   public final CachedBooleanValue prefilledSlurryTanks;
   public final CachedBooleanValue blacklistIC2;
   public final CachedFloatingLongValue ic2ConversionRate;
   public final CachedBooleanValue blacklistForge;
   public final CachedFloatingLongValue forgeConversionRate;
   public final CachedBooleanValue blacklistFluxNetworks;
   public final CachedFloatingLongValue FROM_H2;
   public final CachedFloatingLongValue maxEnergyPerSteam;
   public final CachedBooleanValue radiationEnabled;
   public final CachedIntValue radiationChunkCheckRadius;
   public final CachedDoubleValue radiationSourceDecayRate;
   public final CachedDoubleValue radiationTargetDecayRate;
   public final CachedDoubleValue radiationNegativeEffectsMinSeverity;
   public final CachedLongValue radioactiveWasteBarrelMaxGas;
   public final CachedIntValue radioactiveWasteBarrelProcessTicks;
   public final CachedLongValue radioactiveWasteBarrelDecayAmount;
   public final CachedIntValue minerSilkMultiplier;
   public final CachedIntValue minerMaxRadius;
   public final CachedIntValue minerTicksPerMine;
   public final CachedBooleanValue aestheticWorldDamage;
   public final CachedIntValue laserRange;
   public final CachedFloatingLongValue laserEnergyNeededPerHardness;
   public final CachedFloatingLongValue laserEnergyPerDamage;
   public final CachedOredictionificatorConfigValue validOredictionificatorFilters;
   public final CachedIntValue maxPumpRange;
   public final CachedBooleanValue pumpWaterSources;
   public final CachedIntValue pumpHeavyWaterAmount;
   public final CachedIntValue maxPlenisherNodes;
   public final CachedFloatingLongValue entangloporterEnergyBuffer;
   public final CachedIntValue entangloporterFluidBuffer;
   public final CachedLongValue entangloporterChemicalBuffer;
   public final CachedBooleanValue allowProtection;
   public final CachedBooleanValue opsBypassRestrictions;
   public final CachedFloatValue nutritionalPasteSaturation;
   public final CachedIntValue nutritionalPasteMBPerFood;
   public final CachedIntValue boilerWaterPerTank;
   public final CachedLongValue boilerSteamPerTank;
   public final CachedLongValue boilerHeatedCoolantPerTank;
   public final CachedLongValue boilerCooledCoolantPerTank;
   public final CachedDoubleValue evaporationHeatDissipation;
   public final CachedDoubleValue evaporationTempMultiplier;
   public final CachedDoubleValue evaporationSolarMultiplier;
   public final CachedDoubleValue evaporationHeatCapacity;
   public final CachedIntValue evaporationFluidPerTank;
   public final CachedIntValue evaporationOutputTankCapacity;
   public final CachedIntValue spsInputPerAntimatter;
   public final CachedLongValue spsOutputTankCapacity;
   public final CachedFloatingLongValue spsEnergyPerInput;

   GeneralConfig() {
      Builder builder = new Builder();
      builder.comment("General Config. This config is synced from server to client.").push("general");
      this.logPackets = CachedBooleanValue.wrap(this, builder.comment("Log Mekanism packet names. Debug setting.").define("logPackets", false));
      this.allowChunkloading = CachedBooleanValue.wrap(
         this, builder.comment("Disable to make the anchor upgrade not do anything.").define("allowChunkloading", true)
      );
      this.easyMinerFilters = CachedBooleanValue.wrap(
         this, builder.comment("Enable this to allow dragging items from JEI into the target slot of Digital Miner filters.").define("easyMinerFilters", false)
      );
      this.blockDeactivationDelay = CachedIntValue.wrap(
         this,
         builder.comment("How many ticks must pass until a block's active state is synced with the client, if it has been rapidly changing.")
            .defineInRange("blockDeactivationDelay", 60, 0, 1200)
      );
      this.cardboardModBlacklist = CachedConfigValue.wrap(
         this,
         builder.comment(
               "Any mod ids added to this list will not be able to have any of their blocks, picked up by the cardboard box. For example: [\"mekanism\"]"
            )
            .defineListAllowEmpty(
               Collections.singletonList("cardboardModBlacklist"), ArrayList::new, e -> e instanceof String modid && ResourceLocation.m_135843_(modid)
            )
      );
      this.transmitterAlloyUpgrade = CachedBooleanValue.wrap(
         this, builder.comment("Allow right clicking on Cables/Pipes/Tubes with alloys to upgrade the tier.").define("transmitterAlloyUpgrade", true)
      );
      this.maxUpgradeMultiplier = CachedIntValue.wrap(
         this,
         builder.comment("Base factor for working out machine performance with upgrades - UpgradeModifier * (UpgradesInstalled/UpgradesPossible).")
            .defineInRange("maxUpgradeMultiplier", 10, 1, Integer.MAX_VALUE)
      );
      this.boilerWaterConductivity = CachedDoubleValue.wrap(
         this,
         builder.comment("How much Boiler heat is immediately usable to convert water to steam.").defineInRange("boilerWaterConductivity", 0.7, 0.01, 1.0)
      );
      this.heatPerFuelTick = CachedDoubleValue.wrap(
         this,
         builder.comment("Amount of heat produced per fuel tick of a fuel's burn time in the Fuelwood Heater.")
            .defineInRange("heatPerFuelTick", 400.0, 0.1, 4000000.0)
      );
      this.fuelwoodTickMultiplier = CachedIntValue.wrap(
         this,
         builder.comment(
               "Number of ticks to burn an item at in a Fuelwood Heater. Use this config option to effectively make Fuelwood Heater's burn faster but produce the same amount of heat per item."
            )
            .defineInRange("fuelwoodTickMultiplier", 1, 1, 1000)
      );
      this.resistiveHeaterEfficiency = CachedDoubleValue.wrap(
         this,
         builder.comment("How much heat energy is created from one Joule of regular energy in the Resistive Heater.")
            .defineInRange("resistiveHeaterEfficiency", 0.6, 0.0, 1.0)
      );
      this.superheatingHeatTransfer = CachedDoubleValue.wrap(
         this, builder.comment("Amount of heat each Boiler heating element produces.").defineInRange("superheatingHeatTransfer", 1.6E7, 0.1, 1.024E9)
      );
      this.maxSolarNeutronActivatorRate = CachedIntValue.wrap(
         this,
         builder.comment("Peak processing rate for the Solar Neutron Activator. Note: It can go higher than this value in some extreme environments.")
            .defineInRange("maxSolarNeutronActivatorRate", 64, 1, 1024)
      );
      builder.comment("Dynamic Tank Settings").push("dynamic_tank");
      int maxVolume = 5832;
      this.dynamicTankFluidPerTank = CachedIntValue.wrap(
         this,
         builder.comment("Amount of fluid (mB) that each block of the dynamic tank contributes to the volume. Max = volume * fluidPerTank")
            .defineInRange("fluidPerTank", 350000, 1, Integer.MAX_VALUE / maxVolume)
      );
      this.dynamicTankChemicalPerTank = CachedLongValue.wrap(
         this,
         builder.comment("Amount of chemical (mB) that each block of the dynamic tank contributes to the volume. Max = volume * chemicalPerTank")
            .defineInRange("chemicalPerTank", 16000000L, 1L, Long.MAX_VALUE / maxVolume)
      );
      builder.pop();
      builder.comment("Auto Eject Settings").push("auto_eject");
      this.fluidAutoEjectRate = CachedIntValue.wrap(
         this, builder.comment("Rate at which fluid gets auto ejected from tiles.").defineInRange("fluid", 1024, 1, Integer.MAX_VALUE)
      );
      this.chemicalAutoEjectRate = CachedLongValue.wrap(
         this, builder.comment("Rate at which chemicals gets auto ejected from tiles.").defineInRange("chemical", 1024L, 1L, Long.MAX_VALUE)
      );
      this.dumpExcessKeepRatio = CachedDoubleValue.wrap(
         this,
         builder.comment("The percentage of a tank's capacity to leave contents in when set to dumping excess.")
            .defineInRange("dumpExcessKeepRatio", 0.9, 0.001, 1.0)
      );
      builder.pop();
      builder.comment("Prefilled Tanks").push("prefilled");
      this.prefilledFluidTanks = CachedBooleanValue.wrap(this, builder.comment("Add filled creative fluid tanks to creative/JEI.").define("fluidTanks", true));
      this.prefilledGasTanks = CachedBooleanValue.wrap(this, builder.comment("Add filled creative gas tanks to creative/JEI.").define("gasTanks", true));
      this.prefilledInfusionTanks = CachedBooleanValue.wrap(
         this, builder.comment("Add filled creative infusion tanks to creative/JEI.").define("infusionTanks", true)
      );
      this.prefilledPigmentTanks = CachedBooleanValue.wrap(
         this, builder.comment("Add filled creative pigment tanks to creative/JEI.").define("pigmentTanks", true)
      );
      this.prefilledSlurryTanks = CachedBooleanValue.wrap(
         this, builder.comment("Add filled creative slurry tanks to creative/JEI.").define("slurryTanks", true)
      );
      builder.pop();
      builder.comment("Energy Conversion Rate Settings").push("energy_conversion");
      this.blacklistIC2 = CachedBooleanValue.wrap(
         this,
         builder.comment("Disables IC2 power integration. Requires world restart (server-side option in SMP).").worldRestart().define("blacklistIC2", false)
      );
      this.ic2ConversionRate = CachedFloatingLongValue.define(
         this,
         builder,
         "Conversion multiplier from EU to Joules (EU * euConversionRate = Joules)",
         "euConversionRate",
         FloatingLong.createConst(10L),
         CachedFloatingLongValue.ENERGY_CONVERSION
      );
      this.blacklistForge = CachedBooleanValue.wrap(
         this,
         builder.comment("Disables Forge Energy (FE,RF,IF,uF,CF) power integration. Requires world restart (server-side option in SMP).")
            .worldRestart()
            .define("blacklistForge", false)
      );
      this.forgeConversionRate = CachedFloatingLongValue.define(
         this,
         builder,
         "Conversion multiplier from Forge Energy to Joules (FE * feConversionRate = Joules)",
         "feConversionRate",
         FloatingLong.createConst(2.5),
         CachedFloatingLongValue.ENERGY_CONVERSION
      );
      this.blacklistFluxNetworks = CachedBooleanValue.wrap(
         this,
         builder.comment(
               "Disables Flux Networks higher throughput Forge Energy (FE,RF,IF,uF,CF) power integration. Requires world restart (server-side option in SMP). Note: Disabling Forge Energy integration also disables this."
            )
            .worldRestart()
            .define("blacklistFluxNetworks", false)
      );
      this.FROM_H2 = CachedFloatingLongValue.define(
         this,
         builder,
         "How much energy is produced per mB of Hydrogen, also affects Electrolytic Separator usage, Ethylene burn rate and Gas generator energy capacity.",
         "HydrogenEnergyDensity",
         FloatingLong.createConst(200L),
         CachedFloatingLongValue.POSITIVE
      );
      this.maxEnergyPerSteam = CachedFloatingLongValue.define(
         this, builder, "Maximum Joules per mB of Steam. Also affects Thermoelectric Boiler.", "maxEnergyPerSteam", FloatingLong.createConst(10L)
      );
      builder.pop();
      builder.comment("Radiation Settings").push("radiation");
      this.radiationEnabled = CachedBooleanValue.wrap(
         this, builder.comment("Enable worldwide radiation effects. Don't be a downer and disable this.").define("radiationEnabled", true)
      );
      this.radiationChunkCheckRadius = CachedIntValue.wrap(
         this,
         builder.comment(
               "The radius of chunks checked when running radiation calculations. The algorithm is efficient, but don't abuse it by making this crazy high."
            )
            .defineInRange("chunkCheckRadius", 5, 1, 100)
      );
      this.radiationSourceDecayRate = CachedDoubleValue.wrap(
         this,
         builder.comment(
               "Radiation sources are multiplied by this constant roughly once per second to represent their emission decay. At the default rate, it takes roughly 10 hours to remove a 1,000 Sv/h (crazy high) source."
            )
            .defineInRange("sourceDecayRate", 0.9995, 0.0, 1.0)
      );
      this.radiationTargetDecayRate = CachedDoubleValue.wrap(
         this,
         builder.comment("Radiated objects and entities are multiplied by this constant roughly once per second to represent their dosage decay.")
            .defineInRange("targetDecayRate", 0.9995, 0.0, 1.0)
      );
      this.radiationNegativeEffectsMinSeverity = CachedDoubleValue.wrap(
         this,
         builder.comment(
               "Defines the minimum severity radiation dosage severity (scale of 0 to 1) for which negative effects can take place. Set to 1 to disable negative effects completely."
            )
            .defineInRange("negativeEffectsMinSeverity", 0.1, 0.0, 1.0)
      );
      this.radioactiveWasteBarrelMaxGas = CachedLongValue.wrap(
         this,
         builder.comment("Amount of gas (mB) that can be stored in a Radioactive Waste Barrel.")
            .defineInRange("radioactiveWasteBarrelMaxGas", 512000L, 1L, Long.MAX_VALUE)
      );
      this.radioactiveWasteBarrelProcessTicks = CachedIntValue.wrap(
         this,
         builder.comment("Number of ticks required for radioactive gas stored in a Radioactive Waste Barrel to decay radioactiveWasteBarrelDecayAmount mB.")
            .defineInRange("radioactiveWasteBarrelProcessTicks", 20, 1, Integer.MAX_VALUE)
      );
      this.radioactiveWasteBarrelDecayAmount = CachedLongValue.wrap(
         this,
         builder.comment(
               "Number of mB of gas that decay every radioactiveWasteBarrelProcessTicks ticks when stored in a Radioactive Waste Barrel. Set to zero to disable decay all together. (Gases in the mekanism:waste_barrel_decay_blacklist tag will not decay)."
            )
            .defineInRange("radioactiveWasteBarrelDecayAmount", 1L, 0L, Long.MAX_VALUE)
      );
      builder.pop();
      builder.comment("Digital Miner Settings").push("digital_miner");
      this.minerSilkMultiplier = CachedIntValue.wrap(
         this, builder.comment("Energy multiplier for using silk touch mode with the Digital Miner.").defineInRange("silkMultiplier", 12, 1, Integer.MAX_VALUE)
      );
      this.minerMaxRadius = CachedIntValue.wrap(
         this,
         builder.comment(
               "Maximum radius in blocks that the Digital Miner can reach. (Increasing this may have negative effects on stability and/or performance. We strongly recommend you leave it at the default value)."
            )
            .defineInRange("maxRadius", 32, 1, Integer.MAX_VALUE)
      );
      this.minerTicksPerMine = CachedIntValue.wrap(
         this,
         builder.comment("Number of ticks required to mine a single block with a Digital Miner (without any upgrades).")
            .defineInRange("ticksPerMine", 80, 1, Integer.MAX_VALUE)
      );
      builder.pop();
      builder.comment("Laser Settings").push("laser");
      this.aestheticWorldDamage = CachedBooleanValue.wrap(
         this, builder.comment("If enabled, lasers can break blocks and the flamethrower starts fires.").define("aestheticWorldDamage", true)
      );
      this.laserRange = CachedIntValue.wrap(this, builder.comment("How far (in blocks) a laser can travel.").defineInRange("range", 64, 1, 1024));
      this.laserEnergyNeededPerHardness = CachedFloatingLongValue.define(
         this,
         builder,
         "Energy needed to destroy or attract blocks with a Laser (per block hardness level).",
         "energyNeededPerHardness",
         FloatingLong.createConst(100000L)
      );
      this.laserEnergyPerDamage = CachedFloatingLongValue.define(
         this,
         builder,
         "Energy used per half heart of damage being transferred to entities.",
         "energyPerDamage",
         FloatingLong.createConst(2500L),
         CachedFloatingLongValue.POSITIVE
      );
      builder.pop();
      builder.comment("Oredictionificator Settings").push("oredictionificator");
      this.validOredictionificatorFilters = CachedOredictionificatorConfigValue.define(
         this,
         builder.comment(
            "The list of valid tag prefixes for the Oredictionificator. Note: It is highly recommended to only include well known/defined tag prefixes otherwise it is very easy to potentially add in accidental conversions of things that are not actually equivalent."
         ),
         "validItemFilters",
         () -> Collections.singletonMap("forge", List.of("ingots/", "ores/", "dusts/", "nuggets/", "storage_blocks/", "raw_materials/"))
      );
      builder.pop();
      builder.comment("Pump Settings").push("pump");
      this.maxPumpRange = CachedIntValue.wrap(
         this, builder.comment("Maximum block distance to pull fluid from for the Electric Pump.").defineInRange("maxPumpRange", 80, 1, 512)
      );
      this.pumpWaterSources = CachedBooleanValue.wrap(
         this, builder.comment("If enabled makes Water and Heavy Water blocks be removed from the world on pump.").define("pumpWaterSources", false)
      );
      this.pumpHeavyWaterAmount = CachedIntValue.wrap(
         this,
         builder.comment("mB of Heavy Water that is extracted per block of Water by the Electric Pump with a Filter Upgrade.")
            .defineInRange("pumpHeavyWaterAmount", 10, 1, 1000)
      );
      this.maxPlenisherNodes = CachedIntValue.wrap(
         this, builder.comment("Fluidic Plenisher stops after this many blocks.").defineInRange("maxPlenisherNodes", 4000, 1, 1000000)
      );
      builder.pop();
      builder.comment("Quantum Entangloporter Settings").push("quantum_entangloporter");
      this.entangloporterEnergyBuffer = CachedFloatingLongValue.define(
         this,
         builder,
         "Maximum energy buffer (Mekanism Joules) of an Entangoloporter frequency - i.e. the maximum transfer per tick per frequency. Default is ultimate tier energy cube capacity.",
         "energyBuffer",
         EnergyCubeTier.ULTIMATE.getBaseMaxEnergy(),
         true,
         CachedFloatingLongValue.POSITIVE
      );
      this.entangloporterFluidBuffer = CachedIntValue.wrap(
         this,
         builder.comment(
               "Maximum fluid buffer (mb) of an Entangoloporter frequency - i.e. the maximum transfer per tick per frequency. Default is ultimate tier tank capacity."
            )
            .worldRestart()
            .defineInRange("fluidBuffer", FluidTankTier.ULTIMATE.getBaseStorage(), 1, Integer.MAX_VALUE)
      );
      this.entangloporterChemicalBuffer = CachedLongValue.wrap(
         this,
         builder.comment(
               "Maximum chemical buffer (mb) of an Entangoloporter frequency - i.e. the maximum transfer per tick per frequency. Default is ultimate tier tank capacity."
            )
            .worldRestart()
            .defineInRange("chemicalBuffer", ChemicalTankTier.ULTIMATE.getBaseStorage(), 1L, Long.MAX_VALUE)
      );
      builder.pop();
      builder.comment("Block security/protection Settings").push("security");
      this.allowProtection = CachedBooleanValue.wrap(
         this,
         builder.comment("Enable the security system for players to prevent others from accessing their machines. Does NOT affect Frequencies.")
            .define("allowProtection", true)
      );
      this.opsBypassRestrictions = CachedBooleanValue.wrap(
         this,
         builder.comment(
               "If this is enabled then players with the 'mekanism.bypass_security' permission (default ops) can bypass the block and item security restrictions."
            )
            .define("opsBypassRestrictions", false)
      );
      builder.pop();
      builder.comment("Nutritional Paste Settings").push("nutritional_paste");
      this.nutritionalPasteSaturation = CachedFloatValue.wrap(
         this, builder.comment("Saturation level of Nutritional Paste when eaten.").defineInRange("saturation", 0.8, 0.0, 100.0)
      );
      this.nutritionalPasteMBPerFood = CachedIntValue.wrap(
         this, builder.comment("How much mB of Nutritional Paste equates to one 'half-food.'").defineInRange("mbPerFood", 50, 1, Integer.MAX_VALUE)
      );
      builder.pop();
      builder.comment("Boiler Settings").push("boiler");
      this.boilerWaterPerTank = CachedIntValue.wrap(
         this,
         builder.comment("Amount of fluid (mB) that each block of the boiler's water portion contributes to the volume. Max = volume * waterPerTank")
            .defineInRange("waterPerTank", 16000, 1, Integer.MAX_VALUE / maxVolume)
      );
      this.boilerSteamPerTank = CachedLongValue.wrap(
         this,
         builder.comment("Amount of steam (mB) that each block of the boiler's steam portion contributes to the volume. Max = volume * steamPerTank")
            .defineInRange("steamPerTank", 160000L, 10L, Long.MAX_VALUE / maxVolume)
      );
      this.boilerHeatedCoolantPerTank = CachedLongValue.wrap(
         this,
         builder.comment(
               "Amount of steam (mB) that each block of the boiler's heated coolant portion contributes to the volume. Max = volume * heatedCoolantPerTank"
            )
            .defineInRange("heatedCoolantPerTank", 256000L, 1L, Long.MAX_VALUE / maxVolume)
      );
      this.boilerCooledCoolantPerTank = CachedLongValue.wrap(
         this,
         builder.comment(
               "Amount of steam (mB) that each block of the boiler's cooled coolant portion contributes to the volume. Max = volume * cooledCoolantPerTank"
            )
            .defineInRange("cooledCoolantPerTank", 256000L, 1L, Long.MAX_VALUE / maxVolume)
      );
      builder.pop();
      builder.comment("Thermal Evaporation Plant Settings").push("thermal_evaporation");
      this.evaporationHeatDissipation = CachedDoubleValue.wrap(
         this, builder.comment("Thermal Evaporation Tower heat loss per tick.").defineInRange("heatDissipation", 0.02, 0.001, 1000.0)
      );
      this.evaporationTempMultiplier = CachedDoubleValue.wrap(
         this, builder.comment("Temperature to amount produced ratio for Thermal Evaporation Tower.").defineInRange("tempMultiplier", 0.4, 0.001, 1000000.0)
      );
      this.evaporationSolarMultiplier = CachedDoubleValue.wrap(
         this, builder.comment("Heat to absorb per Solar Panel array of Thermal Evaporation Tower.").defineInRange("solarMultiplier", 0.2, 0.001, 1000000.0)
      );
      this.evaporationHeatCapacity = CachedDoubleValue.wrap(
         this,
         builder.comment("Heat capacity of Thermal Evaporation Tower layers (increases amount of energy needed to increase temperature).")
            .defineInRange("heatCapacity", 100.0, 1.0, 1000000.0)
      );
      this.evaporationFluidPerTank = CachedIntValue.wrap(
         this,
         builder.comment("Amount of fluid (mB) that each block of the evaporation plant contributes to the input tank capacity. Max = volume * fluidPerTank")
            .defineInRange("fluidPerTank", 64000, 1, 29826161)
      );
      this.evaporationOutputTankCapacity = CachedIntValue.wrap(
         this,
         builder.comment("Amount of output fluid (mB) that the evaporation plant can store.").defineInRange("outputTankCapacity", 10000, 1, Integer.MAX_VALUE)
      );
      builder.pop();
      builder.comment("SPS Settings").push("sps");
      this.spsInputPerAntimatter = CachedIntValue.wrap(
         this,
         builder.comment("How much input gas (polonium) in mB must be processed to make 1 mB of antimatter. Input tank capacity is 2x this value.")
            .defineInRange("inputPerAntimatter", 1000, 1, Integer.MAX_VALUE)
      );
      this.spsOutputTankCapacity = CachedLongValue.wrap(
         this, builder.comment("Amount of output gas (mB, antimatter) that the SPS can store.").defineInRange("outputTankCapacity", 1000L, 1L, Long.MAX_VALUE)
      );
      this.spsEnergyPerInput = CachedFloatingLongValue.define(
         this,
         builder,
         "Energy needed to process 1 mB of input (inputPerAntimatter * energyPerInput = energy to produce 1 mB of antimatter).",
         "energyPerInput",
         FloatingLong.createConst(1000000L)
      );
      builder.pop();
      builder.pop();
      this.configSpec = builder.build();
   }

   @Override
   public String getFileName() {
      return "general";
   }

   @Override
   public ForgeConfigSpec getConfigSpec() {
      return this.configSpec;
   }

   @Override
   public Type getConfigType() {
      return Type.SERVER;
   }
}
