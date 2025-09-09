package mekanism.common.content.tank;

import com.mojang.datafixers.util.Either;
import java.util.ArrayList;
import java.util.List;
import mekanism.api.IContentsListener;
import mekanism.api.chemical.ChemicalStack;
import mekanism.api.chemical.ChemicalTankBuilder;
import mekanism.api.chemical.gas.IGasTank;
import mekanism.api.chemical.infuse.IInfusionTank;
import mekanism.api.chemical.pigment.IPigmentTank;
import mekanism.api.chemical.slurry.ISlurryTank;
import mekanism.api.fluid.IExtendedFluidTank;
import mekanism.api.inventory.IInventorySlot;
import mekanism.common.capabilities.chemical.multiblock.MultiblockChemicalTankBuilder;
import mekanism.common.capabilities.fluid.BasicFluidTank;
import mekanism.common.capabilities.fluid.VariableCapacityFluidTank;
import mekanism.common.capabilities.merged.MergedTank;
import mekanism.common.config.MekanismConfig;
import mekanism.common.integration.computer.SpecialComputerMethodWrapper;
import mekanism.common.integration.computer.annotation.ComputerMethod;
import mekanism.common.integration.computer.annotation.SyntheticComputerMethod;
import mekanism.common.integration.computer.annotation.WrappingComputerMethod;
import mekanism.common.inventory.container.slot.ContainerSlotType;
import mekanism.common.inventory.container.sync.dynamic.ContainerSync;
import mekanism.common.inventory.slot.HybridInventorySlot;
import mekanism.common.lib.multiblock.IValveHandler;
import mekanism.common.lib.multiblock.MultiblockData;
import mekanism.common.tile.interfaces.IFluidContainerManager;
import mekanism.common.tile.multiblock.TileEntityDynamicTank;
import mekanism.common.util.MekanismUtils;
import mekanism.common.util.NBTUtils;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.level.Level;
import net.minecraftforge.fluids.FluidStack;

public class TankMultiblockData extends MultiblockData implements IValveHandler {
   @ContainerSync
   public final MergedTank mergedTank;
   @ContainerSync
   @SyntheticComputerMethod(
      getter = "getContainerEditMode"
   )
   public IFluidContainerManager.ContainerEditMode editMode = IFluidContainerManager.ContainerEditMode.BOTH;
   @WrappingComputerMethod(
      wrapper = SpecialComputerMethodWrapper.ComputerIInventorySlotWrapper.class,
      methodNames = {"getInputItem"},
      docPlaceholder = "input slot"
   )
   HybridInventorySlot inputSlot;
   @WrappingComputerMethod(
      wrapper = SpecialComputerMethodWrapper.ComputerIInventorySlotWrapper.class,
      methodNames = {"getOutputItem"},
      docPlaceholder = "output slot"
   )
   HybridInventorySlot outputSlot;
   private int tankCapacity;
   private long chemicalTankCapacity;
   public float prevScale;

   public TankMultiblockData(TileEntityDynamicTank tile) {
      super(tile);
      IContentsListener saveAndComparator = this.createSaveAndComparator();
      this.mergedTank = MergedTank.create(
         VariableCapacityFluidTank.create(this, this::getTankCapacity, BasicFluidTank.alwaysTrue, saveAndComparator),
         (IGasTank)MultiblockChemicalTankBuilder.GAS.create(this, this::getChemicalTankCapacity, ChemicalTankBuilder.GAS.alwaysTrue, saveAndComparator),
         (IInfusionTank)MultiblockChemicalTankBuilder.INFUSION
            .create(this, this::getChemicalTankCapacity, ChemicalTankBuilder.INFUSION.alwaysTrue, saveAndComparator),
         (IPigmentTank)MultiblockChemicalTankBuilder.PIGMENT
            .create(this, this::getChemicalTankCapacity, ChemicalTankBuilder.PIGMENT.alwaysTrue, saveAndComparator),
         (ISlurryTank)MultiblockChemicalTankBuilder.SLURRY
            .create(this, this::getChemicalTankCapacity, ChemicalTankBuilder.SLURRY.alwaysTrue, saveAndComparator)
      );
      this.fluidTanks.add(this.mergedTank.getFluidTank());
      this.gasTanks.add(this.mergedTank.getGasTank());
      this.infusionTanks.add(this.mergedTank.getInfusionTank());
      this.pigmentTanks.add(this.mergedTank.getPigmentTank());
      this.slurryTanks.add(this.mergedTank.getSlurryTank());
      this.inventorySlots.addAll(this.createBaseInventorySlots());
   }

   private List<IInventorySlot> createBaseInventorySlots() {
      List<IInventorySlot> inventorySlots = new ArrayList<>();
      inventorySlots.add(this.inputSlot = HybridInventorySlot.inputOrDrain(this.mergedTank, this, 146, 21));
      inventorySlots.add(this.outputSlot = HybridInventorySlot.outputOrFill(this.mergedTank, this, 146, 51));
      this.inputSlot.setSlotType(ContainerSlotType.INPUT);
      this.outputSlot.setSlotType(ContainerSlotType.OUTPUT);
      return inventorySlots;
   }

   @Override
   public boolean tick(Level world) {
      boolean needsPacket = super.tick(world);
      MergedTank.CurrentType type = this.mergedTank.getCurrentType();
      if (type == MergedTank.CurrentType.EMPTY) {
         this.inputSlot.handleTank(this.outputSlot, this.editMode);
         this.inputSlot.drainChemicalTanks();
         this.outputSlot.fillChemicalTanks();
      } else if (type == MergedTank.CurrentType.FLUID) {
         this.inputSlot.handleTank(this.outputSlot, this.editMode);
      } else {
         this.inputSlot.drainChemicalTank(type);
         this.outputSlot.fillChemicalTank(type);
      }

      float scale = this.getScale();
      if (scale != this.prevScale) {
         this.prevScale = scale;
         needsPacket = true;
      }

      return needsPacket;
   }

   @Override
   public void readUpdateTag(CompoundTag tag) {
      super.readUpdateTag(tag);
      NBTUtils.setFloatIfPresent(tag, "scale", scale -> this.prevScale = scale);
      this.mergedTank.readFromUpdateTag(tag);
      this.readValves(tag);
   }

   @Override
   public void writeUpdateTag(CompoundTag tag) {
      super.writeUpdateTag(tag);
      tag.m_128350_("scale", this.prevScale);
      this.mergedTank.addToUpdateTag(tag);
      this.writeValves(tag);
   }

   private float getScale() {
      return switch (this.mergedTank.getCurrentType()) {
         case FLUID -> MekanismUtils.getScale(this.prevScale, this.getFluidTank());
         case GAS -> MekanismUtils.getScale(this.prevScale, this.getGasTank());
         case INFUSION -> MekanismUtils.getScale(this.prevScale, this.getInfusionTank());
         case PIGMENT -> MekanismUtils.getScale(this.prevScale, this.getPigmentTank());
         case SLURRY -> MekanismUtils.getScale(this.prevScale, this.getSlurryTank());
         default -> MekanismUtils.getScale(this.prevScale, 0L, this.getChemicalTankCapacity(), true);
      };
   }

   @ComputerMethod
   public int getTankCapacity() {
      return this.tankCapacity;
   }

   @ComputerMethod
   public long getChemicalTankCapacity() {
      return this.chemicalTankCapacity;
   }

   @Override
   public void setVolume(int volume) {
      if (this.getVolume() != volume) {
         super.setVolume(volume);
         this.tankCapacity = volume * MekanismConfig.general.dynamicTankFluidPerTank.get();
         this.chemicalTankCapacity = volume * MekanismConfig.general.dynamicTankChemicalPerTank.get();
      }
   }

   @Override
   protected int getMultiblockRedstoneLevel() {
      long capacity = this.mergedTank.getCurrentType() == MergedTank.CurrentType.FLUID ? this.getTankCapacity() : this.getChemicalTankCapacity();
      return MekanismUtils.redstoneLevelFromContents(this.getStoredAmount(), capacity);
   }

   private long getStoredAmount() {
      return switch (this.mergedTank.getCurrentType()) {
         case FLUID -> this.getFluidTank().getFluidAmount();
         case GAS -> this.getGasTank().getStored();
         case INFUSION -> this.getInfusionTank().getStored();
         case PIGMENT -> this.getPigmentTank().getStored();
         case SLURRY -> this.getSlurryTank().getStored();
         default -> 0L;
      };
   }

   public IExtendedFluidTank getFluidTank() {
      return this.mergedTank.getFluidTank();
   }

   public IGasTank getGasTank() {
      return this.mergedTank.getGasTank();
   }

   public IInfusionTank getInfusionTank() {
      return this.mergedTank.getInfusionTank();
   }

   public IPigmentTank getPigmentTank() {
      return this.mergedTank.getPigmentTank();
   }

   public ISlurryTank getSlurryTank() {
      return this.mergedTank.getSlurryTank();
   }

   public boolean isEmpty() {
      return this.mergedTank.getCurrentType() == MergedTank.CurrentType.EMPTY;
   }

   @ComputerMethod
   public void setContainerEditMode(IFluidContainerManager.ContainerEditMode mode) {
      if (this.editMode != mode) {
         this.editMode = mode;
         this.markDirty();
      }
   }

   @ComputerMethod
   void incrementContainerEditMode() {
      this.setContainerEditMode(this.editMode.getNext());
   }

   @ComputerMethod
   void decrementContainerEditMode() {
      this.setContainerEditMode(this.editMode.getPrevious());
   }

   @ComputerMethod
   Either<ChemicalStack<?>, FluidStack> getStored() {
      return switch (this.mergedTank.getCurrentType()) {
         case FLUID -> Either.right(this.getFluidTank().getFluid());
         case GAS -> Either.left(this.getGasTank().getStack());
         case INFUSION -> Either.left(this.getInfusionTank().getStack());
         case PIGMENT -> Either.left(this.getPigmentTank().getStack());
         case SLURRY -> Either.left(this.getSlurryTank().getStack());
         default -> Either.right(FluidStack.EMPTY);
      };
   }

   @ComputerMethod
   double getFilledPercentage() {
      long capacity = this.mergedTank.getCurrentType() == MergedTank.CurrentType.FLUID ? this.getTankCapacity() : this.getChemicalTankCapacity();
      return (double)this.getStoredAmount() / capacity;
   }
}
