package mekanism.common.config;

import java.util.stream.Collectors;
import mekanism.api.math.FloatingLong;
import mekanism.common.config.value.CachedBooleanValue;
import mekanism.common.config.value.CachedDoubleValue;
import mekanism.common.config.value.CachedFloatValue;
import mekanism.common.config.value.CachedFloatingLongValue;
import mekanism.common.config.value.CachedIntValue;
import mekanism.common.config.value.CachedLongValue;
import mekanism.common.config.value.CachedRL2FloatMapConfigValue;
import mekanism.common.item.gear.ItemMekaSuitArmor;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.item.ArmorMaterials;
import net.minecraft.world.item.ArmorItem.Type;
import net.minecraftforge.common.ForgeConfigSpec;
import net.minecraftforge.common.ForgeConfigSpec.Builder;

public class GearConfig extends BaseMekanismConfig {
   private static final String DISASSEMBLER_CATEGORY = "atomic_disassembler";
   private static final String CONFIGURATOR_CATEGORY = "configurator";
   private static final String ELECTRIC_BOW_CATEGORY = "electric_bow";
   private static final String ENERGY_TABLET_CATEGORY = "energy_tablet";
   private static final String FLAMETHROWER_CATEGORY = "flamethrower";
   private static final String FREE_RUNNER_CATEGORY = "free_runner";
   private static final String ARMORED_SUBCATEGORY = "armored";
   private static final String JETPACK_CATEGORY = "jetpack";
   private static final String NETWORK_READER_CATEGORY = "network_reader";
   private static final String PORTABLE_TELEPORTER_CATEGORY = "portable_teleporter";
   private static final String SCUBA_TANK_CATEGORY = "scuba_tank";
   private static final String SEISMIC_READER_CATEGORY = "seismic_reader";
   private static final String CANTEEN_CATEGORY = "canteen";
   private static final String MEKATOOL_CATEGORY = "mekatool";
   private static final String MEKASUIT_CATEGORY = "mekasuit";
   private static final String MEKASUIT_DAMAGE_CATEGORY = "damage_absorption";
   private final ForgeConfigSpec configSpec;
   public final CachedFloatingLongValue disassemblerEnergyUsage;
   public final CachedFloatingLongValue disassemblerEnergyUsageWeapon;
   public final CachedIntValue disassemblerMiningCount;
   public final CachedBooleanValue disassemblerSlowMode;
   public final CachedBooleanValue disassemblerFastMode;
   public final CachedBooleanValue disassemblerVeinMining;
   public final CachedIntValue disassemblerMinDamage;
   public final CachedIntValue disassemblerMaxDamage;
   public final CachedDoubleValue disassemblerAttackSpeed;
   public final CachedFloatingLongValue disassemblerMaxEnergy;
   public final CachedFloatingLongValue disassemblerChargeRate;
   public final CachedFloatingLongValue configuratorMaxEnergy;
   public final CachedFloatingLongValue configuratorChargeRate;
   public final CachedFloatingLongValue configuratorEnergyPerConfigure;
   public final CachedFloatingLongValue configuratorEnergyPerItem;
   public final CachedFloatingLongValue electricBowMaxEnergy;
   public final CachedFloatingLongValue electricBowChargeRate;
   public final CachedFloatingLongValue electricBowEnergyUsage;
   public final CachedFloatingLongValue electricBowEnergyUsageFire;
   public final CachedFloatingLongValue tabletMaxEnergy;
   public final CachedFloatingLongValue tabletChargeRate;
   public final CachedLongValue flamethrowerMaxGas;
   public final CachedLongValue flamethrowerFillRate;
   public final CachedBooleanValue flamethrowerDestroyItems;
   public final CachedFloatingLongValue freeRunnerFallEnergyCost;
   public final CachedFloatValue freeRunnerFallDamageRatio;
   public final CachedFloatingLongValue freeRunnerMaxEnergy;
   public final CachedFloatingLongValue freeRunnerChargeRate;
   public final CachedIntValue armoredFreeRunnerArmor;
   public final CachedFloatValue armoredFreeRunnerToughness;
   public final CachedFloatValue armoredFreeRunnerKnockbackResistance;
   public final CachedLongValue jetpackMaxGas;
   public final CachedLongValue jetpackFillRate;
   public final CachedIntValue armoredJetpackArmor;
   public final CachedFloatValue armoredJetpackToughness;
   public final CachedFloatValue armoredJetpackKnockbackResistance;
   public final CachedFloatingLongValue portableTeleporterMaxEnergy;
   public final CachedFloatingLongValue portableTeleporterChargeRate;
   public final CachedIntValue portableTeleporterDelay;
   public final CachedFloatingLongValue networkReaderMaxEnergy;
   public final CachedFloatingLongValue networkReaderChargeRate;
   public final CachedFloatingLongValue networkReaderEnergyUsage;
   public final CachedLongValue scubaMaxGas;
   public final CachedLongValue scubaFillRate;
   public final CachedFloatingLongValue seismicReaderMaxEnergy;
   public final CachedFloatingLongValue seismicReaderChargeRate;
   public final CachedFloatingLongValue seismicReaderEnergyUsage;
   public final CachedIntValue canteenMaxStorage;
   public final CachedIntValue canteenTransferRate;
   public final CachedFloatingLongValue mekaToolEnergyUsageWeapon;
   public final CachedFloatingLongValue mekaToolEnergyUsageTeleport;
   public final CachedFloatingLongValue mekaToolEnergyUsage;
   public final CachedFloatingLongValue mekaToolEnergyUsageSilk;
   public final CachedIntValue mekaToolMaxTeleportReach;
   public final CachedIntValue mekaToolBaseDamage;
   public final CachedDoubleValue mekaToolAttackSpeed;
   public final CachedFloatValue mekaToolBaseEfficiency;
   public final CachedFloatingLongValue mekaToolBaseEnergyCapacity;
   public final CachedFloatingLongValue mekaToolBaseChargeRate;
   public final CachedFloatingLongValue mekaToolEnergyUsageHoe;
   public final CachedFloatingLongValue mekaToolEnergyUsageShovel;
   public final CachedFloatingLongValue mekaToolEnergyUsageAxe;
   public final CachedFloatingLongValue mekaToolEnergyUsageShearEntity;
   public final CachedBooleanValue mekaToolExtendedMining;
   public final CachedFloatingLongValue mekaSuitBaseEnergyCapacity;
   public final CachedFloatingLongValue mekaSuitBaseChargeRate;
   public final CachedFloatingLongValue mekaSuitBaseJumpEnergyUsage;
   public final CachedFloatingLongValue mekaSuitElytraEnergyUsage;
   public final CachedFloatingLongValue mekaSuitEnergyUsagePotionTick;
   public final CachedFloatingLongValue mekaSuitEnergyUsageMagicReduce;
   public final CachedFloatingLongValue mekaSuitEnergyUsageFall;
   public final CachedFloatingLongValue mekaSuitEnergyUsageSprintBoost;
   public final CachedFloatingLongValue mekaSuitEnergyUsageGravitationalModulation;
   public final CachedFloatingLongValue mekaSuitInventoryChargeRate;
   public final CachedFloatingLongValue mekaSuitSolarRechargingRate;
   public final CachedFloatingLongValue mekaSuitEnergyUsageVisionEnhancement;
   public final CachedFloatingLongValue mekaSuitEnergyUsageHydrostaticRepulsion;
   public final CachedFloatingLongValue mekaSuitEnergyUsageNutritionalInjection;
   public final CachedFloatingLongValue mekaSuitEnergyUsageDamage;
   public final CachedFloatingLongValue mekaSuitEnergyUsageItemAttraction;
   public final CachedBooleanValue mekaSuitGravitationalVibrations;
   public final CachedIntValue mekaSuitNutritionalMaxStorage;
   public final CachedIntValue mekaSuitNutritionalTransferRate;
   public final CachedLongValue mekaSuitJetpackMaxStorage;
   public final CachedLongValue mekaSuitJetpackTransferRate;
   public final CachedIntValue mekaSuitHelmetArmor;
   public final CachedIntValue mekaSuitBodyArmorArmor;
   public final CachedIntValue mekaSuitPantsArmor;
   public final CachedIntValue mekaSuitBootsArmor;
   public final CachedFloatValue mekaSuitToughness;
   public final CachedFloatValue mekaSuitKnockbackResistance;
   public final CachedRL2FloatMapConfigValue mekaSuitDamageRatios;
   public final CachedFloatValue mekaSuitFallDamageRatio;
   public final CachedFloatValue mekaSuitMagicDamageRatio;
   public final CachedFloatValue mekaSuitUnspecifiedDamageRatio;

   GearConfig() {
      Builder builder = new Builder();
      builder.comment("Gear Config. This config is synced from server to client.").push("gear");
      builder.comment("Atomic Disassembler Settings").push("atomic_disassembler");
      this.disassemblerEnergyUsage = CachedFloatingLongValue.define(
         this,
         builder,
         "Base Energy (Joules) usage of the Atomic Disassembler. (Gets multiplied by speed factor)",
         "energyUsage",
         FloatingLong.createConst(10L)
      );
      this.disassemblerEnergyUsageWeapon = CachedFloatingLongValue.define(
         this, builder, "Cost in Joules of using the Atomic Disassembler as a weapon.", "energyUsageWeapon", FloatingLong.createConst(2000L)
      );
      this.disassemblerMiningCount = CachedIntValue.wrap(
         this, builder.comment("The max Atomic Disassembler Vein Mining Block Count.").defineInRange("miningCount", 128, 2, 1000000)
      );
      this.disassemblerSlowMode = CachedBooleanValue.wrap(this, builder.comment("Enable the 'Slow' mode for the Atomic Disassembler.").define("slowMode", true));
      this.disassemblerFastMode = CachedBooleanValue.wrap(this, builder.comment("Enable the 'Fast' mode for the Atomic Disassembler.").define("fastMode", true));
      this.disassemblerVeinMining = CachedBooleanValue.wrap(
         this, builder.comment("Enable the 'Vein Mining' mode for the Atomic Disassembler.").define("veinMining", false)
      );
      this.disassemblerMinDamage = CachedIntValue.wrap(
         this,
         builder.comment("The bonus attack damage of the Atomic Disassembler when it is out of power. (Value is in number of half hearts)")
            .defineInRange("minDamage", 4, 0, 1000)
      );
      this.disassemblerMaxDamage = CachedIntValue.wrap(
         this,
         builder.comment(
               "The bonus attack damage of the Atomic Disassembler when it has at least energyUsageWeapon power stored. (Value is in number of half hearts)"
            )
            .defineInRange("maxDamage", 20, 1, 10000)
      );
      this.disassemblerAttackSpeed = CachedDoubleValue.wrap(
         this, builder.comment("Attack speed of the Atomic Disassembler.").defineInRange("attackSpeed", -2.4, -Attributes.f_22283_.m_22082_(), 100.0)
      );
      this.disassemblerMaxEnergy = CachedFloatingLongValue.define(
         this, builder, "Maximum amount (joules) of energy the Atomic Disassembler can contain.", "maxEnergy", FloatingLong.createConst(1000000L)
      );
      this.disassemblerChargeRate = CachedFloatingLongValue.define(
         this, builder, "Amount (joules) of energy the Atomic Disassembler can accept per tick.", "chargeRate", FloatingLong.createConst(5000L)
      );
      builder.pop();
      builder.comment("Configurator Settings").push("configurator");
      this.configuratorMaxEnergy = CachedFloatingLongValue.define(
         this, builder, "Maximum amount (joules) of energy the Configurator can contain.", "maxEnergy", FloatingLong.createConst(60000L)
      );
      this.configuratorChargeRate = CachedFloatingLongValue.define(
         this, builder, "Amount (joules) of energy the Configurator can accept per tick.", "chargeRate", FloatingLong.createConst(300L)
      );
      this.configuratorEnergyPerConfigure = CachedFloatingLongValue.define(
         this, builder, "Energy usage in joules of using the configurator to configure machines.", "energyPerConfigure", FloatingLong.createConst(400L)
      );
      this.configuratorEnergyPerItem = CachedFloatingLongValue.define(
         this,
         builder,
         "Energy cost in joules for each item the configurator ejects from a machine on empty mode.",
         "energyPerItem",
         FloatingLong.createConst(8L)
      );
      builder.pop();
      builder.comment("Electric Bow Settings").push("electric_bow");
      this.electricBowMaxEnergy = CachedFloatingLongValue.define(
         this, builder, "Maximum amount (joules) of energy the Electric Bow can contain.", "maxEnergy", FloatingLong.createConst(120000L)
      );
      this.electricBowChargeRate = CachedFloatingLongValue.define(
         this, builder, "Amount (joules) of energy the Electric Bow can accept per tick.", "chargeRate", FloatingLong.createConst(600L)
      );
      this.electricBowEnergyUsage = CachedFloatingLongValue.define(
         this, builder, "Cost in Joules of using the Electric Bow.", "energyUsage", FloatingLong.createConst(120L)
      );
      this.electricBowEnergyUsageFire = CachedFloatingLongValue.define(
         this, builder, "Cost in Joules of using the Electric Bow with flame mode active.", "energyUsageFire", FloatingLong.createConst(1200L)
      );
      builder.pop();
      builder.comment("Energy Tablet Settings").push("energy_tablet");
      this.tabletMaxEnergy = CachedFloatingLongValue.define(
         this, builder, "Maximum amount (joules) of energy the Energy Tablet can contain.", "maxEnergy", FloatingLong.createConst(1000000L)
      );
      this.tabletChargeRate = CachedFloatingLongValue.define(
         this, builder, "Amount (joules) of energy the Energy Tablet can accept per tick.", "chargeRate", FloatingLong.createConst(5000L)
      );
      builder.pop();
      builder.comment("Flamethrower Settings").push("flamethrower");
      this.flamethrowerMaxGas = CachedLongValue.wrap(
         this, builder.comment("Flamethrower Gas Tank capacity in mB.").defineInRange("maxGas", 24000L, 1L, Long.MAX_VALUE)
      );
      this.flamethrowerFillRate = CachedLongValue.wrap(
         this, builder.comment("Amount of hydrogen the Flamethrower can accept per tick.").defineInRange("fillRate", 16L, 1L, Long.MAX_VALUE)
      );
      this.flamethrowerDestroyItems = CachedBooleanValue.wrap(
         this, builder.comment("Determines whether or not the Flamethrower can destroy items if it fails to smelt them.").define("destroyItems", true)
      );
      builder.pop();
      builder.comment("Free Runner Settings").push("free_runner");
      this.freeRunnerFallEnergyCost = CachedFloatingLongValue.define(
         this,
         builder,
         "Energy cost/multiplier in Joules for reducing fall damage with free runners. Energy cost is: FallDamage * freeRunnerFallEnergyCost. (1 FallDamage is 1 half heart)",
         "fallEnergyCost",
         FloatingLong.createConst(50L)
      );
      this.freeRunnerFallDamageRatio = CachedFloatValue.wrap(
         this,
         builder.comment("Percent of damage taken from falling that can be absorbed by Free Runners when they have enough power.")
            .defineInRange("fallDamageReductionRatio", 1.0, 0.0, 1.0)
      );
      this.freeRunnerMaxEnergy = CachedFloatingLongValue.define(
         this, builder, "Maximum amount (joules) of energy Free Runners can contain.", "maxEnergy", FloatingLong.createConst(64000L)
      );
      this.freeRunnerChargeRate = CachedFloatingLongValue.define(
         this, builder, "Amount (joules) of energy the Free Runners can accept per tick.", "chargeRate", FloatingLong.createConst(320L)
      );
      builder.comment("Armored Free Runner Settings").push("armored");
      this.armoredFreeRunnerArmor = CachedIntValue.wrap(
         this, builder.comment("Armor value of the Armored Free Runners").defineInRange("armor", 3, 0, Integer.MAX_VALUE)
      );
      this.armoredFreeRunnerToughness = CachedFloatValue.wrap(
         this, builder.comment("Toughness value of the Armored Free Runners.").defineInRange("toughness", 2.0, 0.0, Float.MAX_VALUE)
      );
      this.armoredFreeRunnerKnockbackResistance = CachedFloatValue.wrap(
         this, builder.comment("Knockback resistance value of the Armored Free Runners.").defineInRange("knockbackResistance", 0.0, 0.0, Float.MAX_VALUE)
      );
      builder.pop(2);
      builder.comment("Jetpack Settings").push("jetpack");
      this.jetpackMaxGas = CachedLongValue.wrap(this, builder.comment("Jetpack Gas Tank capacity in mB.").defineInRange("maxGas", 24000L, 1L, Long.MAX_VALUE));
      this.jetpackFillRate = CachedLongValue.wrap(
         this, builder.comment("Amount of hydrogen the Jetpack can accept per tick.").defineInRange("fillRate", 16L, 1L, Long.MAX_VALUE)
      );
      builder.comment("Armored Jetpack Settings").push("armored");
      this.armoredJetpackArmor = CachedIntValue.wrap(
         this, builder.comment("Armor value of the Armored Jetpack.").defineInRange("armor", 8, 0, Integer.MAX_VALUE)
      );
      this.armoredJetpackToughness = CachedFloatValue.wrap(
         this, builder.comment("Toughness value of the Armored Jetpack.").defineInRange("toughness", 2.0, 0.0, Float.MAX_VALUE)
      );
      this.armoredJetpackKnockbackResistance = CachedFloatValue.wrap(
         this, builder.comment("Knockback resistance value of the Armored Jetpack.").defineInRange("knockbackResistance", 0.0, 0.0, Float.MAX_VALUE)
      );
      builder.pop(2);
      builder.comment("Network Reader Settings").push("network_reader");
      this.networkReaderMaxEnergy = CachedFloatingLongValue.define(
         this, builder, "Maximum amount (joules) of energy the Network Reader can contain.", "maxEnergy", FloatingLong.createConst(60000L)
      );
      this.networkReaderChargeRate = CachedFloatingLongValue.define(
         this, builder, "Amount (joules) of energy the Network Reader can accept per tick.", "chargeRate", FloatingLong.createConst(300L)
      );
      this.networkReaderEnergyUsage = CachedFloatingLongValue.define(
         this, builder, "Energy usage in joules for each network reading.", "energyUsage", FloatingLong.createConst(400L)
      );
      builder.pop();
      builder.comment("Portable Teleporter Settings").push("portable_teleporter");
      this.portableTeleporterMaxEnergy = CachedFloatingLongValue.define(
         this, builder, "Maximum amount (joules) of energy the Portable Teleporter can contain.", "maxEnergy", FloatingLong.createConst(1000000L)
      );
      this.portableTeleporterChargeRate = CachedFloatingLongValue.define(
         this, builder, "Amount (joules) of energy the Portable Teleporter can accept per tick.", "chargeRate", FloatingLong.createConst(5000L)
      );
      this.portableTeleporterDelay = CachedIntValue.wrap(
         this,
         builder.comment("Delay in ticks before a player is teleported after clicking the Teleport button in the portable teleporter.")
            .defineInRange("delay", 0, 0, 6000)
      );
      builder.pop();
      builder.comment("Scuba Tank Settings").push("scuba_tank");
      this.scubaMaxGas = CachedLongValue.wrap(this, builder.comment("Scuba Tank Gas Tank capacity in mB.").defineInRange("maxGas", 24000L, 1L, Long.MAX_VALUE));
      this.scubaFillRate = CachedLongValue.wrap(
         this, builder.comment("Amount of oxygen the Scuba Tank Gas Tank can accept per tick.").defineInRange("fillRate", 16L, 1L, Long.MAX_VALUE)
      );
      builder.pop();
      builder.comment("Seismic Reader Settings").push("seismic_reader");
      this.seismicReaderMaxEnergy = CachedFloatingLongValue.define(
         this, builder, "Maximum amount (joules) of energy the Seismic Reader can contain.", "maxEnergy", FloatingLong.createConst(12000L)
      );
      this.seismicReaderChargeRate = CachedFloatingLongValue.define(
         this, builder, "Amount (joules) of energy the Seismic Reader can accept per tick.", "chargeRate", FloatingLong.createConst(60L)
      );
      this.seismicReaderEnergyUsage = CachedFloatingLongValue.define(
         this, builder, "Energy usage in joules required to use the Seismic Reader.", "energyUsage", FloatingLong.createConst(250L)
      );
      builder.pop();
      builder.comment("Canteen Settings").push("canteen");
      this.canteenMaxStorage = CachedIntValue.wrap(
         this, builder.comment("Maximum amount of Nutritional Paste storable by the Canteen.").defineInRange("maxStorage", 64000, 1, Integer.MAX_VALUE)
      );
      this.canteenTransferRate = CachedIntValue.wrap(
         this, builder.comment("Rate at which Nutritional Paste can be transferred into a Canteen.").defineInRange("transferRate", 128, 1, Integer.MAX_VALUE)
      );
      builder.pop();
      builder.comment("Meka-Tool Settings").push("mekatool");
      this.mekaToolEnergyUsage = CachedFloatingLongValue.define(
         this, builder, "Base energy (Joules) usage of the Meka-Tool. (Gets multiplied by speed factor)", "energyUsage", FloatingLong.createConst(10L)
      );
      this.mekaToolEnergyUsageSilk = CachedFloatingLongValue.define(
         this,
         builder,
         "Silk touch energy (Joules) usage of the Meka-Tool. (Gets multiplied by speed factor)",
         "energyUsageSilk",
         FloatingLong.createConst(100L)
      );
      this.mekaToolEnergyUsageWeapon = CachedFloatingLongValue.define(
         this, builder, "Cost in Joules of using the Meka-Tool to deal 4 units of damage.", "energyUsageWeapon", FloatingLong.createConst(2000L)
      );
      this.mekaToolEnergyUsageTeleport = CachedFloatingLongValue.define(
         this, builder, "Cost in Joules of using the Meka-Tool to teleport 10 blocks.", "energyUsageTeleport", FloatingLong.createConst(1000L)
      );
      this.mekaToolMaxTeleportReach = CachedIntValue.wrap(
         this, builder.comment("Maximum distance a player can teleport with the Meka-Tool.").defineInRange("maxTeleportReach", 100, 3, 1024)
      );
      this.mekaToolBaseDamage = CachedIntValue.wrap(
         this, builder.comment("Base bonus damage applied by the Meka-Tool without using any energy.").defineInRange("baseDamage", 4, 0, 100000)
      );
      this.mekaToolAttackSpeed = CachedDoubleValue.wrap(
         this, builder.comment("Attack speed of the Meka-Tool.").defineInRange("attackSpeed", -2.4, -Attributes.f_22283_.m_22082_(), 100.0)
      );
      this.mekaToolBaseEfficiency = CachedFloatValue.wrap(
         this, builder.comment("Efficiency of the Meka-Tool with energy but without any upgrades.").defineInRange("baseEfficiency", 4.0, 0.1, 100.0)
      );
      this.mekaToolBaseEnergyCapacity = CachedFloatingLongValue.define(
         this,
         builder,
         "Energy capacity (Joules) of the Meka-Tool without any installed upgrades. Quadratically scaled by upgrades.",
         "baseEnergyCapacity",
         FloatingLong.createConst(16000000L)
      );
      this.mekaToolBaseChargeRate = CachedFloatingLongValue.define(
         this,
         builder,
         "Amount (joules) of energy the Meka-Tool can accept per tick. Quadratically scaled by upgrades.",
         "chargeRate",
         FloatingLong.createConst(100000L)
      );
      this.mekaToolEnergyUsageHoe = CachedFloatingLongValue.define(
         this, builder, "Cost in Joules of using the Meka-Tool as a hoe.", "energyUsageHoe", FloatingLong.createConst(10L)
      );
      this.mekaToolEnergyUsageShovel = CachedFloatingLongValue.define(
         this,
         builder,
         "Cost in Joules of using the Meka-Tool as a shovel for making paths and dowsing campfires.",
         "energyUsageShovel",
         FloatingLong.createConst(10L)
      );
      this.mekaToolEnergyUsageAxe = CachedFloatingLongValue.define(
         this,
         builder,
         "Cost in Joules of using the Meka-Tool as an axe for stripping logs, scraping, or removing wax.",
         "energyUsageAxe",
         FloatingLong.createConst(10L)
      );
      this.mekaToolEnergyUsageShearEntity = CachedFloatingLongValue.define(
         this, builder, "Cost in Joules of using the Meka-Tool to shear entities.", "energyUsageShearEntity", FloatingLong.createConst(10L)
      );
      this.mekaToolExtendedMining = CachedBooleanValue.wrap(
         this,
         builder.comment("Enable the 'Extended Vein Mining' mode for the Meka-Tool. (Allows vein mining everything not just ores/logs)")
            .define("extendedMining", true)
      );
      builder.pop();
      builder.comment("MekaSuit Settings").push("mekasuit");
      this.mekaSuitBaseEnergyCapacity = CachedFloatingLongValue.define(
         this,
         builder,
         "Energy capacity (Joules) of MekaSuit items without any installed upgrades. Quadratically scaled by upgrades.",
         "baseEnergyCapacity",
         FloatingLong.createConst(16000000L)
      );
      this.mekaSuitBaseChargeRate = CachedFloatingLongValue.define(
         this,
         builder,
         "Amount (joules) of energy the MekaSuit can accept per tick. Quadratically scaled by upgrades.",
         "chargeRate",
         FloatingLong.createConst(100000L)
      );
      this.mekaSuitBaseJumpEnergyUsage = CachedFloatingLongValue.define(
         this, builder, "Energy usage (Joules) of MekaSuit when adding 0.1 to jump motion.", "baseJumpEnergyUsage", FloatingLong.createConst(1000L)
      );
      this.mekaSuitElytraEnergyUsage = CachedFloatingLongValue.define(
         this,
         builder,
         "Energy usage (Joules) per second of the MekaSuit when flying with the Elytra Unit.",
         "elytraEnergyUsage",
         FloatingLong.createConst(32000L)
      );
      this.mekaSuitEnergyUsagePotionTick = CachedFloatingLongValue.define(
         this, builder, "Energy usage (Joules) of MekaSuit when lessening a potion effect.", "energyUsagePotionTick", FloatingLong.createConst(40000L)
      );
      this.mekaSuitEnergyUsageMagicReduce = CachedFloatingLongValue.define(
         this,
         builder,
         "Energy cost/multiplier in Joules for reducing magic damage via the inhalation purification unit. Energy cost is: MagicDamage * energyUsageMagicPrevent. (1 MagicDamage is 1 half heart).",
         "energyUsageMagicReduce",
         FloatingLong.createConst(1000L)
      );
      this.mekaSuitEnergyUsageFall = CachedFloatingLongValue.define(
         this,
         builder,
         "Energy cost/multiplier in Joules for reducing fall damage with MekaSuit Boots. Energy cost is: FallDamage * freeRunnerFallEnergyCost. (1 FallDamage is 1 half heart)",
         "energyUsageFall",
         FloatingLong.createConst(50L)
      );
      this.mekaSuitEnergyUsageSprintBoost = CachedFloatingLongValue.define(
         this, builder, "Energy usage (Joules) of MekaSuit when adding 0.1 to sprint motion.", "energyUsageSprintBoost", FloatingLong.createConst(100L)
      );
      this.mekaSuitEnergyUsageGravitationalModulation = CachedFloatingLongValue.define(
         this,
         builder,
         "Energy usage (Joules) of MekaSuit per tick when flying via Gravitational Modulation.",
         "energyUsageGravitationalModulation",
         FloatingLong.createConst(1000L)
      );
      this.mekaSuitInventoryChargeRate = CachedFloatingLongValue.define(
         this, builder, "Charge rate of inventory items (Joules) per tick.", "inventoryChargeRate", FloatingLong.createConst(10000L)
      );
      this.mekaSuitSolarRechargingRate = CachedFloatingLongValue.define(
         this, builder, "Solar recharging rate (Joules) of helmet per tick, per upgrade installed.", "solarRechargingRate", FloatingLong.createConst(500L)
      );
      this.mekaSuitEnergyUsageVisionEnhancement = CachedFloatingLongValue.define(
         this,
         builder,
         "Energy usage (Joules) of MekaSuit per tick of using vision enhancement.",
         "energyUsageVisionEnhancement",
         FloatingLong.createConst(500L)
      );
      this.mekaSuitEnergyUsageHydrostaticRepulsion = CachedFloatingLongValue.define(
         this,
         builder,
         "Energy usage (Joules) of MekaSuit per tick of using hydrostatic repulsion.",
         "energyUsageHydrostaticRepulsion",
         FloatingLong.createConst(500L)
      );
      this.mekaSuitEnergyUsageNutritionalInjection = CachedFloatingLongValue.define(
         this,
         builder,
         "Energy usage (Joules) of MekaSuit per half-food of nutritional injection.",
         "energyUsageNutritionalInjection",
         FloatingLong.createConst(20000L)
      );
      this.mekaSuitEnergyUsageDamage = CachedFloatingLongValue.define(
         this, builder, "Energy usage (Joules) of MekaSuit per unit of damage applied.", "energyUsageDamage", FloatingLong.createConst(100000L)
      );
      this.mekaSuitEnergyUsageItemAttraction = CachedFloatingLongValue.define(
         this, builder, "Energy usage (Joules) of MekaSuit per tick of attracting a single item.", "energyUsageItemAttraction", FloatingLong.createConst(250L)
      );
      this.mekaSuitGravitationalVibrations = CachedBooleanValue.wrap(
         this, builder.comment("Should the Gravitational Modulation unit give off vibrations when in use.").define("gravitationalVibrations", true)
      );
      this.mekaSuitNutritionalMaxStorage = CachedIntValue.wrap(
         this,
         builder.comment("Maximum amount of Nutritional Paste storable by the nutritional injection unit.")
            .defineInRange("nutritionalMaxStorage", 128000, 1, Integer.MAX_VALUE)
      );
      this.mekaSuitNutritionalTransferRate = CachedIntValue.wrap(
         this,
         builder.comment("Rate at which Nutritional Paste can be transferred into the nutritional injection unit.")
            .defineInRange("nutritionalTransferRate", 256, 1, Integer.MAX_VALUE)
      );
      this.mekaSuitJetpackMaxStorage = CachedLongValue.wrap(
         this, builder.comment("Maximum amount of Hydrogen storable in the jetpack unit.").defineInRange("jetpackMaxStorage", 48000L, 1L, Long.MAX_VALUE)
      );
      this.mekaSuitJetpackTransferRate = CachedLongValue.wrap(
         this,
         builder.comment("Rate at which Hydrogen can be transferred into the jetpack unit.").defineInRange("jetpackTransferRate", 256L, 1L, Long.MAX_VALUE)
      );
      this.mekaSuitHelmetArmor = CachedIntValue.wrap(
         this,
         builder.comment("Armor value of MekaSuit Helmets.").defineInRange("helmetArmor", ArmorMaterials.NETHERITE.m_7366_(Type.HELMET), 0, Integer.MAX_VALUE)
      );
      this.mekaSuitBodyArmorArmor = CachedIntValue.wrap(
         this,
         builder.comment("Armor value of MekaSuit BodyArmor.")
            .defineInRange("bodyArmorArmor", ArmorMaterials.NETHERITE.m_7366_(Type.CHESTPLATE), 0, Integer.MAX_VALUE)
      );
      this.mekaSuitPantsArmor = CachedIntValue.wrap(
         this,
         builder.comment("Armor value of MekaSuit Pants.").defineInRange("pantsArmor", ArmorMaterials.NETHERITE.m_7366_(Type.LEGGINGS), 0, Integer.MAX_VALUE)
      );
      this.mekaSuitBootsArmor = CachedIntValue.wrap(
         this,
         builder.comment("Armor value of MekaSuit Boots.").defineInRange("bootsArmor", ArmorMaterials.NETHERITE.m_7366_(Type.BOOTS), 0, Integer.MAX_VALUE)
      );
      this.mekaSuitToughness = CachedFloatValue.wrap(
         this, builder.comment("Toughness value of the MekaSuit.").defineInRange("toughness", ArmorMaterials.NETHERITE.m_6651_(), 0.0, Float.MAX_VALUE)
      );
      this.mekaSuitKnockbackResistance = CachedFloatValue.wrap(
         this,
         builder.comment("Knockback resistance value of the MekaSuit.")
            .defineInRange("knockbackResistance", ArmorMaterials.NETHERITE.m_6649_(), 0.0, Float.MAX_VALUE)
      );
      builder.push("damage_absorption");
      this.mekaSuitFallDamageRatio = CachedFloatValue.wrap(
         this,
         builder.comment("Percent of damage taken from falling that can be absorbed by MekaSuit Boots when they have enough power.")
            .defineInRange("fallDamageReductionRatio", 1.0, 0.0, 1.0)
      );
      this.mekaSuitMagicDamageRatio = CachedFloatValue.wrap(
         this,
         builder.comment("Percent of damage taken from magic damage that can be absorbed by MekaSuit Helmet with Purification unit when it has enough power.")
            .defineInRange("magicDamageReductionRatio", 1.0, 0.0, 1.0)
      );
      this.mekaSuitUnspecifiedDamageRatio = CachedFloatValue.wrap(
         this,
         builder.comment(
               new String[]{
                  "Percent of damage taken from other non explicitly supported damage types that don't bypass armor when the MekaSuit has enough power and a full suit is equipped.",
                  "Note: Support for specific damage types can be added by adding an entry for the damage type in the damageReductionRatio config."
               }
            )
            .defineInRange("unspecifiedDamageReductionRatio", 1.0, 0.0, 1.0)
      );
      this.mekaSuitDamageRatios = CachedRL2FloatMapConfigValue.define(
         this,
         builder.comment(
            new String[]{
               "Map representing the percent of damage from different damage types that can be absorbed by the MekaSuit when there is enough power and a full suit is equipped.",
               "Values may be in the range [0.0, 1.0].",
               "See the #mekainsm:mekasuit_always_supported damage type tag for allowing damage that bypasses armor to be blocked."
            }
         ),
         "damageReductionRatio",
         () -> ItemMekaSuitArmor.BASE_ALWAYS_SUPPORTED.stream().collect(Collectors.toMap(ResourceKey::m_135782_, ItemMekaSuitArmor::getBaseDamageRatio)),
         f -> f >= 0.0F && f <= 1.0F
      );
      builder.pop(2);
      builder.pop();
      this.configSpec = builder.build();
   }

   @Override
   public String getFileName() {
      return "gear";
   }

   @Override
   public ForgeConfigSpec getConfigSpec() {
      return this.configSpec;
   }

   @Override
   public net.minecraftforge.fml.config.ModConfig.Type getConfigType() {
      return net.minecraftforge.fml.config.ModConfig.Type.SERVER;
   }

   @Override
   public boolean addToContainer() {
      return false;
   }
}
