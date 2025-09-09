package mekanism.common.content.sps;

import it.unimi.dsi.fastutil.objects.Object2ObjectOpenHashMap;
import java.util.Map;
import mekanism.api.Action;
import mekanism.api.AutomationType;
import mekanism.api.chemical.attribute.ChemicalAttributeValidator;
import mekanism.api.chemical.gas.GasStack;
import mekanism.api.chemical.gas.IGasTank;
import mekanism.api.math.FloatingLong;
import mekanism.api.math.MathUtils;
import mekanism.common.capabilities.chemical.multiblock.MultiblockChemicalTankBuilder;
import mekanism.common.config.MekanismConfig;
import mekanism.common.integration.computer.SpecialComputerMethodWrapper;
import mekanism.common.integration.computer.annotation.ComputerMethod;
import mekanism.common.integration.computer.annotation.WrappingComputerMethod;
import mekanism.common.inventory.container.sync.dynamic.ContainerSync;
import mekanism.common.lib.multiblock.IValveHandler;
import mekanism.common.lib.multiblock.MultiblockData;
import mekanism.common.registries.MekanismGases;
import mekanism.common.tile.multiblock.TileEntitySPSCasing;
import mekanism.common.tile.multiblock.TileEntitySPSPort;
import mekanism.common.util.MekanismUtils;
import mekanism.common.util.NBTUtils;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.NbtUtils;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.AABB;

public class SPSMultiblockData extends MultiblockData implements IValveHandler {
   @ContainerSync
   @WrappingComputerMethod(
      wrapper = SpecialComputerMethodWrapper.ComputerChemicalTankWrapper.class,
      methodNames = {"getInput", "getInputCapacity", "getInputNeeded", "getInputFilledPercentage"},
      docPlaceholder = "input tank"
   )
   public IGasTank inputTank;
   @ContainerSync
   @WrappingComputerMethod(
      wrapper = SpecialComputerMethodWrapper.ComputerChemicalTankWrapper.class,
      methodNames = {"getOutput", "getOutputCapacity", "getOutputNeeded", "getOutputFilledPercentage"},
      docPlaceholder = "output tank"
   )
   public IGasTank outputTank;
   public final SPSMultiblockData.SyncableCoilData coilData = new SPSMultiblockData.SyncableCoilData();
   @ContainerSync
   public double progress;
   @ContainerSync
   public int inputProcessed = 0;
   public FloatingLong receivedEnergy = FloatingLong.ZERO;
   @ContainerSync
   public FloatingLong lastReceivedEnergy = FloatingLong.ZERO;
   @ContainerSync
   public double lastProcessed;
   public boolean couldOperate;
   private AABB deathZone;

   public SPSMultiblockData(TileEntitySPSCasing tile) {
      super(tile);
      this.gasTanks
         .add(
            this.inputTank = (IGasTank)MultiblockChemicalTankBuilder.GAS
               .input(
                  this,
                  this::getMaxInputGas,
                  gas -> gas == MekanismGases.POLONIUM.get(),
                  ChemicalAttributeValidator.ALWAYS_ALLOW,
                  this.createSaveAndComparator()
               )
         );
      this.gasTanks
         .add(
            this.outputTank = (IGasTank)MultiblockChemicalTankBuilder.GAS
               .output(
                  this,
                  MekanismConfig.general.spsOutputTankCapacity,
                  gas -> gas == MekanismGases.ANTIMATTER.get(),
                  ChemicalAttributeValidator.ALWAYS_ALLOW,
                  this
               )
         );
   }

   @Override
   public void onCreated(Level world) {
      super.onCreated(world);
      this.deathZone = new AABB(this.getMinPos().m_7918_(1, 1, 1), this.getMaxPos());
   }

   private long getMaxInputGas() {
      return MekanismConfig.general.spsInputPerAntimatter.get() * 2L;
   }

   @Override
   public boolean tick(Level world) {
      boolean needsPacket = super.tick(world);
      double processed = 0.0;
      this.couldOperate = this.canOperate();
      if (this.couldOperate && !this.receivedEnergy.isZero()) {
         double lastProgress = this.progress;
         int inputPerAntimatter = MekanismConfig.general.spsInputPerAntimatter.get();
         long inputNeeded = inputPerAntimatter - this.inputProcessed + inputPerAntimatter * (this.outputTank.getNeeded() - 1L);
         double processable = this.receivedEnergy.doubleValue() / MekanismConfig.general.spsEnergyPerInput.get().doubleValue();
         if (processable + this.progress >= inputNeeded) {
            processed = this.process(inputNeeded);
            this.progress = 0.0;
         } else {
            processed = processable;
            this.progress += processable;
            long toProcess = MathUtils.clampToLong(this.progress);
            long actualProcessed = this.process(toProcess);
            if (actualProcessed < toProcess) {
               long processedDif = toProcess - actualProcessed;
               this.progress -= processedDif;
               processed = processable - processedDif;
            }

            this.progress %= 1.0;
         }

         if (lastProgress != this.progress) {
            this.markDirty();
         }
      }

      if (!this.receivedEnergy.equals(this.lastReceivedEnergy) || processed != this.lastProcessed) {
         needsPacket = true;
      }

      this.lastReceivedEnergy = this.receivedEnergy;
      this.receivedEnergy = FloatingLong.ZERO;
      this.lastProcessed = processed;
      this.kill(world);
      return needsPacket | this.coilData.tick();
   }

   @Override
   public void readUpdateTag(CompoundTag tag) {
      super.readUpdateTag(tag);
      this.coilData.read(tag);
      this.lastReceivedEnergy = FloatingLong.parseFloatingLong(tag.m_128461_("energyUsage"));
      this.lastProcessed = tag.m_128459_("lastProcessed");
   }

   @Override
   public void writeUpdateTag(CompoundTag tag) {
      super.writeUpdateTag(tag);
      this.coilData.write(tag);
      tag.m_128359_("energyUsage", this.lastReceivedEnergy.toString());
      tag.m_128347_("lastProcessed", this.lastProcessed);
   }

   @Override
   protected int getMultiblockRedstoneLevel() {
      return MekanismUtils.redstoneLevelFromContents(this.inputTank.getStored(), this.inputTank.getCapacity());
   }

   private long process(long operations) {
      if (operations == 0L) {
         return 0L;
      } else {
         long processed = this.inputTank.shrinkStack(operations, Action.EXECUTE);
         int lastInputProcessed = this.inputProcessed;
         this.inputProcessed = this.inputProcessed + MathUtils.clampToInt(processed);
         int inputPerAntimatter = MekanismConfig.general.spsInputPerAntimatter.get();
         if (this.inputProcessed >= inputPerAntimatter) {
            GasStack toAdd = MekanismGases.ANTIMATTER.getStack(this.inputProcessed / inputPerAntimatter);
            this.outputTank.insert(toAdd, Action.EXECUTE, AutomationType.INTERNAL);
            this.inputProcessed %= inputPerAntimatter;
         }

         if (lastInputProcessed != this.inputProcessed) {
            this.markDirty();
         }

         return processed;
      }
   }

   private void kill(Level world) {
      if (!this.lastReceivedEnergy.isZero() && this.couldOperate && world.m_213780_().m_188502_() % 20 == 0) {
         for (Entity entity : this.getWorld().m_45976_(Entity.class, this.deathZone)) {
            entity.m_6469_(entity.m_269291_().m_269425_(), this.lastReceivedEnergy.floatValue() / 1000.0F);
         }
      }
   }

   public boolean canSupplyCoilEnergy(TileEntitySPSPort tile) {
      return (this.couldOperate || this.canOperate()) && this.coilData.coilMap.containsKey(tile.m_58899_());
   }

   public void addCoil(BlockPos portPos, Direction side) {
      this.coilData.coilMap.put(portPos, new SPSMultiblockData.CoilData(portPos, side));
   }

   public void supplyCoilEnergy(TileEntitySPSPort tile, FloatingLong energy) {
      this.receivedEnergy = this.receivedEnergy.plusEqual(energy);
      this.coilData.coilMap.get(tile.m_58899_()).receiveEnergy(energy);
   }

   private boolean canOperate() {
      return !this.inputTank.isEmpty() && this.outputTank.getNeeded() > 0L;
   }

   private static int getCoilLevel(FloatingLong energy) {
      return energy.isZero() ? 0 : 1 + Math.max(0, (int)((Math.log10(energy.doubleValue()) - 3.0) * 1.8));
   }

   @ComputerMethod
   public double getProcessRate() {
      return Math.round(this.lastProcessed / MekanismConfig.general.spsInputPerAntimatter.get() * 1000.0) / 1000.0;
   }

   public double getScaledProgress() {
      return (this.inputProcessed + this.progress) / MekanismConfig.general.spsInputPerAntimatter.get();
   }

   public boolean handlesSound(TileEntitySPSCasing tile) {
      return tile.m_58899_().equals(this.getMinPos().m_7918_(3, 0, 0)) || tile.m_58899_().equals(this.getMaxPos().m_7918_(-3, 0, 0));
   }

   @ComputerMethod
   int getCoils() {
      return this.coilData.coilMap.size();
   }

   public static class CoilData {
      public final BlockPos coilPos;
      public final Direction side;
      public int prevLevel;
      private int laserLevel;

      private CoilData(BlockPos pos, Direction side) {
         this.coilPos = pos;
         this.side = side;
      }

      private void receiveEnergy(FloatingLong energy) {
         this.laserLevel = this.laserLevel + SPSMultiblockData.getCoilLevel(energy);
      }

      @Override
      public int hashCode() {
         int result = 1;
         result = 31 * result + this.coilPos.hashCode();
         return 31 * result + this.prevLevel;
      }

      @Override
      public boolean equals(Object o) {
         return o == this ? true : o instanceof SPSMultiblockData.CoilData other && this.coilPos.equals(other.coilPos) && this.prevLevel == other.prevLevel;
      }
   }

   public static class SyncableCoilData {
      public final Map<BlockPos, SPSMultiblockData.CoilData> coilMap = new Object2ObjectOpenHashMap();
      public int prevHash;

      private boolean tick() {
         this.coilMap.values().forEach(data -> {
            data.prevLevel = data.laserLevel;
            data.laserLevel = 0;
         });
         int newHash = this.coilMap.hashCode();
         boolean ret = newHash != this.prevHash;
         this.prevHash = newHash;
         return ret;
      }

      public void write(CompoundTag tags) {
         ListTag list = new ListTag();

         for (SPSMultiblockData.CoilData data : this.coilMap.values()) {
            CompoundTag tag = new CompoundTag();
            tag.m_128365_("position", NbtUtils.m_129224_(data.coilPos));
            NBTUtils.writeEnum(tag, "side", data.side);
            tag.m_128405_("level", data.prevLevel);
            list.add(tag);
         }

         tags.m_128365_("coils", list);
      }

      public void read(CompoundTag tags) {
         this.coilMap.clear();
         ListTag list = tags.m_128437_("coils", 10);

         for (int i = 0; i < list.size(); i++) {
            CompoundTag tag = list.m_128728_(i);
            BlockPos pos = NbtUtils.m_129239_(tag.m_128469_("position"));
            Direction side = Direction.m_122376_(tag.m_128451_("side"));
            SPSMultiblockData.CoilData data = new SPSMultiblockData.CoilData(pos, side);
            data.prevLevel = tag.m_128451_("level");
            this.coilMap.put(data.coilPos, data);
         }
      }
   }
}
