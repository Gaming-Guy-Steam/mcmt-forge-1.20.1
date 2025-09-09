package mekanism.common.content.boiler;

import it.unimi.dsi.fastutil.objects.Object2BooleanMap;
import it.unimi.dsi.fastutil.objects.Object2BooleanOpenHashMap;
import java.util.Collections;
import java.util.UUID;
import mekanism.api.Action;
import mekanism.api.AutomationType;
import mekanism.api.chemical.gas.GasStack;
import mekanism.api.chemical.gas.IGasTank;
import mekanism.api.chemical.gas.attribute.GasAttributes;
import mekanism.api.heat.HeatAPI;
import mekanism.api.math.MathUtils;
import mekanism.common.capabilities.chemical.multiblock.MultiblockChemicalTankBuilder;
import mekanism.common.capabilities.fluid.VariableCapacityFluidTank;
import mekanism.common.capabilities.heat.VariableHeatCapacitor;
import mekanism.common.config.MekanismConfig;
import mekanism.common.integration.computer.SpecialComputerMethodWrapper;
import mekanism.common.integration.computer.annotation.ComputerMethod;
import mekanism.common.integration.computer.annotation.SyntheticComputerMethod;
import mekanism.common.integration.computer.annotation.WrappingComputerMethod;
import mekanism.common.inventory.container.sync.dynamic.ContainerSync;
import mekanism.common.lib.multiblock.IValveHandler;
import mekanism.common.lib.multiblock.MultiblockData;
import mekanism.common.registries.MekanismGases;
import mekanism.common.tags.MekanismTags;
import mekanism.common.tile.multiblock.TileEntityBoilerCasing;
import mekanism.common.util.HeatUtils;
import mekanism.common.util.MekanismUtils;
import mekanism.common.util.NBTUtils;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.NbtUtils;
import net.minecraft.world.level.Level;

public class BoilerMultiblockData extends MultiblockData implements IValveHandler {
   public static final Object2BooleanMap<UUID> hotMap = new Object2BooleanOpenHashMap();
   public static final double CASING_HEAT_CAPACITY = 50.0;
   private static final double CASING_INVERSE_INSULATION_COEFFICIENT = 100000.0;
   private static final double CASING_INVERSE_CONDUCTION_COEFFICIENT = 1.0;
   private static final double COOLANT_COOLING_EFFICIENCY = 0.4;
   @ContainerSync
   @WrappingComputerMethod(
      wrapper = SpecialComputerMethodWrapper.ComputerChemicalTankWrapper.class,
      methodNames = {"getHeatedCoolant", "getHeatedCoolantCapacity", "getHeatedCoolantNeeded", "getHeatedCoolantFilledPercentage"},
      docPlaceholder = "heated coolant tank"
   )
   public IGasTank superheatedCoolantTank;
   @ContainerSync
   @WrappingComputerMethod(
      wrapper = SpecialComputerMethodWrapper.ComputerChemicalTankWrapper.class,
      methodNames = {"getCooledCoolant", "getCooledCoolantCapacity", "getCooledCoolantNeeded", "getCooledCoolantFilledPercentage"},
      docPlaceholder = "cooled coolant tank"
   )
   public IGasTank cooledCoolantTank;
   @ContainerSync
   @WrappingComputerMethod(
      wrapper = SpecialComputerMethodWrapper.ComputerFluidTankWrapper.class,
      methodNames = {"getWater", "getWaterCapacity", "getWaterNeeded", "getWaterFilledPercentage"},
      docPlaceholder = "water tank"
   )
   public VariableCapacityFluidTank waterTank;
   @ContainerSync
   @WrappingComputerMethod(
      wrapper = SpecialComputerMethodWrapper.ComputerChemicalTankWrapper.class,
      methodNames = {"getSteam", "getSteamCapacity", "getSteamNeeded", "getSteamFilledPercentage"},
      docPlaceholder = "steam tank"
   )
   public IGasTank steamTank;
   @ContainerSync
   @WrappingComputerMethod(
      wrapper = SpecialComputerMethodWrapper.ComputerHeatCapacitorWrapper.class,
      methodNames = {"getTemperature"},
      docPlaceholder = "boiler"
   )
   public VariableHeatCapacitor heatCapacitor;
   private double biomeAmbientTemp;
   @ContainerSync
   @SyntheticComputerMethod(
      getter = "getEnvironmentalLoss",
      getterDescription = "Get the amount of heat lost to the environment in the last tick (Kelvin)"
   )
   public double lastEnvironmentLoss;
   @ContainerSync
   @SyntheticComputerMethod(
      getter = "getBoilRate",
      getterDescription = "Get the rate of boiling (mB/t)"
   )
   public int lastBoilRate;
   @ContainerSync
   @SyntheticComputerMethod(
      getter = "getMaxBoilRate",
      getterDescription = "Get the maximum rate of boiling seen (mB/t)"
   )
   public int lastMaxBoil;
   @ContainerSync
   @SyntheticComputerMethod(
      getter = "getSuperheaters",
      getterDescription = "How many superheaters this Boiler has"
   )
   public int superheatingElements;
   @ContainerSync(
      setter = "setWaterVolume"
   )
   private int waterVolume;
   @ContainerSync(
      setter = "setSteamVolume"
   )
   private int steamVolume;
   private int waterTankCapacity;
   private long superheatedCoolantCapacity;
   private long steamTankCapacity;
   private long cooledCoolantCapacity;
   public BlockPos upperRenderLocation;
   public float prevWaterScale;
   public float prevSteamScale;

   public BoilerMultiblockData(TileEntityBoilerCasing tile) {
      super(tile);
      this.biomeAmbientTemp = HeatAPI.getAmbientTemp(tile.m_58904_(), tile.getTilePos());
      this.superheatedCoolantTank = (IGasTank)MultiblockChemicalTankBuilder.GAS
         .input(this, () -> this.superheatedCoolantCapacity, gas -> gas.has(GasAttributes.HeatedCoolant.class), this);
      this.waterTank = VariableCapacityFluidTank.input(
         this, () -> this.waterTankCapacity, fluid -> MekanismTags.Fluids.WATER_LOOKUP.contains(fluid.getFluid()), this.createSaveAndComparator()
      );
      this.fluidTanks.add(this.waterTank);
      this.steamTank = (IGasTank)MultiblockChemicalTankBuilder.GAS
         .output(this, () -> this.steamTankCapacity, gas -> gas == MekanismGases.STEAM.getChemical(), this);
      this.cooledCoolantTank = (IGasTank)MultiblockChemicalTankBuilder.GAS
         .output(this, () -> this.cooledCoolantCapacity, gas -> gas.has(GasAttributes.CooledCoolant.class), this);
      Collections.addAll(this.gasTanks, this.steamTank, this.superheatedCoolantTank, this.cooledCoolantTank);
      this.heatCapacitor = VariableHeatCapacitor.create(50.0, () -> 1.0, () -> 100000.0, () -> this.biomeAmbientTemp, this);
      this.heatCapacitors.add(this.heatCapacitor);
   }

   @Override
   public void onCreated(Level world) {
      super.onCreated(world);
      this.biomeAmbientTemp = this.calculateAverageAmbientTemperature(world);
      this.heatCapacitor.setHeatCapacity(50.0 * this.locations.size(), true);
   }

   @Override
   public void remove(Level world) {
      hotMap.removeBoolean(this.inventoryID);
      super.remove(world);
   }

   @Override
   public boolean tick(Level world) {
      boolean needsPacket = super.tick(world);
      hotMap.put(this.inventoryID, this.getTotalTemperature() >= HeatUtils.BASE_BOIL_TEMP - 0.01);
      this.lastEnvironmentLoss = this.simulateEnvironment();
      this.updateHeatCapacitors(null);
      if (!this.superheatedCoolantTank.isEmpty()) {
         this.superheatedCoolantTank.getStack().ifAttributePresent(GasAttributes.HeatedCoolant.class, coolantType -> {
            long toCool = Math.round(0.4 * this.superheatedCoolantTank.getStored());
            toCool = MathUtils.clampToLong(toCool * (1.0 - this.heatCapacitor.getTemperature() / 100000.0));
            GasStack cooledCoolant = coolantType.getCooledGas().getStack(toCool);
            toCool = Math.min(toCool, toCool - this.cooledCoolantTank.insert(cooledCoolant, Action.EXECUTE, AutomationType.INTERNAL).getAmount());
            if (toCool > 0L) {
               double heatEnergy = toCool * coolantType.getThermalEnthalpy();
               this.heatCapacitor.handleHeat(heatEnergy);
               this.superheatedCoolantTank.shrinkStack(toCool, Action.EXECUTE);
            }
         });
      }

      if (this.getTotalTemperature() >= HeatUtils.BASE_BOIL_TEMP && !this.waterTank.isEmpty()) {
         double heatAvailable = this.getHeatAvailable();
         this.lastMaxBoil = (int)Math.floor(HeatUtils.getSteamEnergyEfficiency() * heatAvailable / HeatUtils.getWaterThermalEnthalpy());
         int amountToBoil = Math.min(this.lastMaxBoil, this.waterTank.getFluidAmount());
         amountToBoil = Math.min(amountToBoil, MathUtils.clampToInt(this.steamTank.getNeeded()));
         if (!this.waterTank.isEmpty()) {
            this.waterTank.shrinkStack(amountToBoil, Action.EXECUTE);
         }

         if (this.steamTank.isEmpty()) {
            this.steamTank.setStack(MekanismGases.STEAM.getStack(amountToBoil));
         } else {
            this.steamTank.growStack(amountToBoil, Action.EXECUTE);
         }

         this.heatCapacitor.handleHeat(-amountToBoil * HeatUtils.getWaterThermalEnthalpy() / HeatUtils.getSteamEnergyEfficiency());
         this.lastBoilRate = amountToBoil;
      } else {
         this.lastBoilRate = 0;
         this.lastMaxBoil = 0;
      }

      float waterScale = MekanismUtils.getScale(this.prevWaterScale, this.waterTank);
      if (waterScale != this.prevWaterScale) {
         needsPacket = true;
         this.prevWaterScale = waterScale;
      }

      float steamScale = MekanismUtils.getScale(this.prevSteamScale, this.steamTank);
      if (steamScale != this.prevSteamScale) {
         needsPacket = true;
         this.prevSteamScale = steamScale;
      }

      return needsPacket;
   }

   @Override
   public void readUpdateTag(CompoundTag tag) {
      super.readUpdateTag(tag);
      NBTUtils.setFloatIfPresent(tag, "scale", scale -> this.prevWaterScale = scale);
      NBTUtils.setFloatIfPresent(tag, "scaleAlt", scale -> this.prevSteamScale = scale);
      NBTUtils.setIntIfPresent(tag, "volume", this::setWaterVolume);
      NBTUtils.setIntIfPresent(tag, "lowerVolume", this::setSteamVolume);
      NBTUtils.setFluidStackIfPresent(tag, "fluid", value -> this.waterTank.setStack(value));
      NBTUtils.setGasStackIfPresent(tag, "gas", value -> this.steamTank.setStack(value));
      NBTUtils.setBlockPosIfPresent(tag, "renderY", value -> this.upperRenderLocation = value);
      this.readValves(tag);
   }

   @Override
   public void writeUpdateTag(CompoundTag tag) {
      super.writeUpdateTag(tag);
      tag.m_128350_("scale", this.prevWaterScale);
      tag.m_128350_("scaleAlt", this.prevSteamScale);
      tag.m_128405_("volume", this.getWaterVolume());
      tag.m_128405_("lowerVolume", this.getSteamVolume());
      tag.m_128365_("fluid", this.waterTank.getFluid().writeToNBT(new CompoundTag()));
      tag.m_128365_("gas", this.steamTank.getStack().write(new CompoundTag()));
      tag.m_128365_("renderY", NbtUtils.m_129224_(this.upperRenderLocation));
      this.writeValves(tag);
   }

   @Override
   protected int getMultiblockRedstoneLevel() {
      return MekanismUtils.redstoneLevelFromContents(this.waterTank.getFluidAmount(), this.waterTank.getCapacity());
   }

   private double getHeatAvailable() {
      double heatAvailable = (this.heatCapacitor.getTemperature() - HeatUtils.BASE_BOIL_TEMP)
         * (this.heatCapacitor.getHeatCapacity() * MekanismConfig.general.boilerWaterConductivity.get());
      return Math.min(heatAvailable, MekanismConfig.general.superheatingHeatTransfer.get() * this.superheatingElements);
   }

   @Override
   public double simulateEnvironment() {
      double invConduction = 110001.0;
      double tempToTransfer = (this.heatCapacitor.getTemperature() - this.biomeAmbientTemp) / invConduction;
      this.heatCapacitor.handleHeat(-tempToTransfer * this.heatCapacitor.getHeatCapacity());
      return Math.max(tempToTransfer, 0.0);
   }

   public int getWaterVolume() {
      return this.waterVolume;
   }

   public void setWaterVolume(int volume) {
      if (this.waterVolume != volume) {
         this.waterVolume = volume;
         this.waterTankCapacity = volume * MekanismConfig.general.boilerWaterPerTank.get();
         this.superheatedCoolantCapacity = volume * MekanismConfig.general.boilerHeatedCoolantPerTank.get();
      }
   }

   public int getSteamVolume() {
      return this.steamVolume;
   }

   public void setSteamVolume(int volume) {
      if (this.steamVolume != volume) {
         this.steamVolume = volume;
         this.steamTankCapacity = volume * MekanismConfig.general.boilerSteamPerTank.get();
         this.cooledCoolantCapacity = volume * MekanismConfig.general.boilerCooledCoolantPerTank.get();
      }
   }

   @ComputerMethod(
      methodDescription = "Get the maximum possible boil rate for this Boiler, based on the number of Superheating Elements"
   )
   public long getBoilCapacity() {
      double boilCapacity = MekanismConfig.general.superheatingHeatTransfer.get() * this.superheatingElements / HeatUtils.getWaterThermalEnthalpy();
      return MathUtils.clampToLong(boilCapacity * HeatUtils.getSteamEnergyEfficiency());
   }
}
