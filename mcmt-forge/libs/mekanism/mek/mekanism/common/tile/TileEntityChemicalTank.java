package mekanism.common.tile;

import it.unimi.dsi.fastutil.objects.Object2ObjectOpenHashMap;
import java.util.Map;
import mekanism.api.Action;
import mekanism.api.IContentsListener;
import mekanism.api.IIncrementalEnum;
import mekanism.api.RelativeSide;
import mekanism.api.annotations.NothingNullByDefault;
import mekanism.api.chemical.IChemicalTank;
import mekanism.api.chemical.gas.Gas;
import mekanism.api.chemical.gas.GasStack;
import mekanism.api.chemical.gas.IGasTank;
import mekanism.api.chemical.infuse.IInfusionTank;
import mekanism.api.chemical.infuse.InfuseType;
import mekanism.api.chemical.infuse.InfusionStack;
import mekanism.api.chemical.merged.MergedChemicalTank;
import mekanism.api.chemical.pigment.IPigmentTank;
import mekanism.api.chemical.pigment.Pigment;
import mekanism.api.chemical.pigment.PigmentStack;
import mekanism.api.chemical.slurry.ISlurryTank;
import mekanism.api.chemical.slurry.Slurry;
import mekanism.api.chemical.slurry.SlurryStack;
import mekanism.api.math.MathUtils;
import mekanism.api.providers.IBlockProvider;
import mekanism.api.text.IHasTextComponent;
import mekanism.api.text.ILangEntry;
import mekanism.common.MekanismLang;
import mekanism.common.block.attribute.Attribute;
import mekanism.common.capabilities.chemical.ChemicalTankChemicalTank;
import mekanism.common.capabilities.holder.chemical.ChemicalTankHelper;
import mekanism.common.capabilities.holder.chemical.IChemicalTankHolder;
import mekanism.common.capabilities.holder.slot.IInventorySlotHolder;
import mekanism.common.capabilities.holder.slot.InventorySlotHelper;
import mekanism.common.config.MekanismConfig;
import mekanism.common.integration.computer.ComputerException;
import mekanism.common.integration.computer.SpecialComputerMethodWrapper;
import mekanism.common.integration.computer.annotation.ComputerMethod;
import mekanism.common.integration.computer.annotation.SyntheticComputerMethod;
import mekanism.common.integration.computer.annotation.WrappingComputerMethod;
import mekanism.common.inventory.container.MekanismContainer;
import mekanism.common.inventory.container.slot.ContainerSlotType;
import mekanism.common.inventory.container.slot.SlotOverlay;
import mekanism.common.inventory.container.sync.SyncableEnum;
import mekanism.common.inventory.slot.chemical.MergedChemicalInventorySlot;
import mekanism.common.lib.transmitter.TransmissionType;
import mekanism.common.tier.ChemicalTankTier;
import mekanism.common.tile.base.SubstanceType;
import mekanism.common.tile.component.ITileComponent;
import mekanism.common.tile.component.TileComponentConfig;
import mekanism.common.tile.component.TileComponentEjector;
import mekanism.common.tile.interfaces.IHasGasMode;
import mekanism.common.tile.interfaces.ISustainedData;
import mekanism.common.tile.prefab.TileEntityConfigurableMachine;
import mekanism.common.upgrade.ChemicalTankUpgradeData;
import mekanism.common.upgrade.IUpgradeData;
import mekanism.common.util.MekanismUtils;
import mekanism.common.util.NBTUtils;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.world.level.block.state.BlockState;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class TileEntityChemicalTank extends TileEntityConfigurableMachine implements ISustainedData, IHasGasMode {
   @SyntheticComputerMethod(
      getter = "getDumpingMode",
      getterDescription = "Get the current Dumping configuration"
   )
   public TileEntityChemicalTank.GasMode dumping = TileEntityChemicalTank.GasMode.IDLE;
   private MergedChemicalTank chemicalTank;
   private ChemicalTankTier tier;
   @WrappingComputerMethod(
      wrapper = SpecialComputerMethodWrapper.ComputerIInventorySlotWrapper.class,
      methodNames = {"getDrainItem"},
      docPlaceholder = "drain slot"
   )
   MergedChemicalInventorySlot<MergedChemicalTank> drainSlot;
   @WrappingComputerMethod(
      wrapper = SpecialComputerMethodWrapper.ComputerIInventorySlotWrapper.class,
      methodNames = {"getFillItem"},
      docPlaceholder = "fill slot"
   )
   MergedChemicalInventorySlot<MergedChemicalTank> fillSlot;

   public TileEntityChemicalTank(IBlockProvider blockProvider, BlockPos pos, BlockState state) {
      super(blockProvider, pos, state);
      this.configComponent = new TileComponentConfig(
         this, TransmissionType.GAS, TransmissionType.INFUSION, TransmissionType.PIGMENT, TransmissionType.SLURRY, TransmissionType.ITEM
      );
      this.configComponent.setupIOConfig(TransmissionType.ITEM, this.drainSlot, this.fillSlot, RelativeSide.FRONT, true).setCanEject(false);
      this.configComponent.setupIOConfig(TransmissionType.GAS, this.getGasTank(), RelativeSide.FRONT).setEjecting(true);
      this.configComponent.setupIOConfig(TransmissionType.INFUSION, this.getInfusionTank(), RelativeSide.FRONT).setEjecting(true);
      this.configComponent.setupIOConfig(TransmissionType.PIGMENT, this.getPigmentTank(), RelativeSide.FRONT).setEjecting(true);
      this.configComponent.setupIOConfig(TransmissionType.SLURRY, this.getSlurryTank(), RelativeSide.FRONT).setEjecting(true);
      this.ejectorComponent = new TileComponentEjector(this, () -> this.tier.getOutput());
      this.ejectorComponent
         .setOutputData(this.configComponent, TransmissionType.GAS, TransmissionType.INFUSION, TransmissionType.PIGMENT, TransmissionType.SLURRY)
         .setCanEject(
            type -> MekanismUtils.canFunction(this) && (this.tier == ChemicalTankTier.CREATIVE || this.dumping != TileEntityChemicalTank.GasMode.DUMPING)
         );
   }

   @Override
   protected void presetVariables() {
      super.presetVariables();
      this.tier = Attribute.getTier(this.getBlockType(), ChemicalTankTier.class);
      this.chemicalTank = ChemicalTankChemicalTank.create(this.tier, this);
   }

   @NotNull
   @Override
   public IChemicalTankHolder<Gas, GasStack, IGasTank> getInitialGasTanks(IContentsListener listener) {
      ChemicalTankHelper<Gas, GasStack, IGasTank> builder = ChemicalTankHelper.forSideGasWithConfig(this::getDirection, this::getConfig);
      builder.addTank(this.getGasTank());
      return builder.build();
   }

   @NotNull
   @Override
   public IChemicalTankHolder<InfuseType, InfusionStack, IInfusionTank> getInitialInfusionTanks(IContentsListener listener) {
      ChemicalTankHelper<InfuseType, InfusionStack, IInfusionTank> builder = ChemicalTankHelper.forSideInfusionWithConfig(this::getDirection, this::getConfig);
      builder.addTank(this.getInfusionTank());
      return builder.build();
   }

   @NotNull
   @Override
   public IChemicalTankHolder<Pigment, PigmentStack, IPigmentTank> getInitialPigmentTanks(IContentsListener listener) {
      ChemicalTankHelper<Pigment, PigmentStack, IPigmentTank> builder = ChemicalTankHelper.forSidePigmentWithConfig(this::getDirection, this::getConfig);
      builder.addTank(this.getPigmentTank());
      return builder.build();
   }

   @NotNull
   @Override
   public IChemicalTankHolder<Slurry, SlurryStack, ISlurryTank> getInitialSlurryTanks(IContentsListener listener) {
      ChemicalTankHelper<Slurry, SlurryStack, ISlurryTank> builder = ChemicalTankHelper.forSideSlurryWithConfig(this::getDirection, this::getConfig);
      builder.addTank(this.getSlurryTank());
      return builder.build();
   }

   @NotNull
   @Override
   protected IInventorySlotHolder getInitialInventory(IContentsListener listener) {
      InventorySlotHelper builder = InventorySlotHelper.forSideWithConfig(this::getDirection, this::getConfig);
      builder.addSlot(this.drainSlot = MergedChemicalInventorySlot.drain(this.chemicalTank, listener, 16, 16));
      builder.addSlot(this.fillSlot = MergedChemicalInventorySlot.fill(this.chemicalTank, listener, 16, 48));
      this.drainSlot.setSlotType(ContainerSlotType.OUTPUT);
      this.drainSlot.setSlotOverlay(SlotOverlay.PLUS);
      this.fillSlot.setSlotType(ContainerSlotType.INPUT);
      this.fillSlot.setSlotOverlay(SlotOverlay.MINUS);
      return builder.build();
   }

   @Override
   protected void onUpdateServer() {
      super.onUpdateServer();
      this.drainSlot.drainChemicalTanks();
      this.fillSlot.fillChemicalTanks();
      if (this.dumping != TileEntityChemicalTank.GasMode.IDLE && this.tier != ChemicalTankTier.CREATIVE) {
         MergedChemicalTank.Current current = this.chemicalTank.getCurrent();
         if (current != MergedChemicalTank.Current.EMPTY) {
            IChemicalTank<?, ?> currentTank = this.chemicalTank.getTankFromCurrent(current);
            if (this.dumping == TileEntityChemicalTank.GasMode.DUMPING) {
               currentTank.shrinkStack(this.tier.getStorage() / 400L, Action.EXECUTE);
            } else {
               long target = MathUtils.clampToLong(currentTank.getCapacity() * MekanismConfig.general.dumpExcessKeepRatio.get());
               long stored = currentTank.getStored();
               if (target < stored) {
                  currentTank.shrinkStack(Math.min(stored - target, this.tier.getOutput()), Action.EXECUTE);
               }
            }
         }
      }
   }

   @Override
   public void nextMode(int tank) {
      if (tank == 0) {
         this.dumping = this.dumping.getNext();
         this.markForSave();
      }
   }

   @Override
   public boolean shouldDumpRadiation() {
      return this.tier != ChemicalTankTier.CREATIVE;
   }

   @Override
   public int getRedstoneLevel() {
      IChemicalTank<?, ?> currentTank = this.getCurrentTank();
      return MekanismUtils.redstoneLevelFromContents(currentTank.getStored(), currentTank.getCapacity());
   }

   @Override
   protected boolean makesComparatorDirty(@Nullable SubstanceType type) {
      return type == SubstanceType.GAS || type == SubstanceType.INFUSION || type == SubstanceType.PIGMENT || type == SubstanceType.SLURRY;
   }

   @WrappingComputerMethod(
      wrapper = SpecialComputerMethodWrapper.ComputerChemicalTankWrapper.class,
      methodNames = {"getStored", "getCapacity", "getNeeded", "getFilledPercentage"},
      docPlaceholder = "tank"
   )
   IChemicalTank<?, ?> getCurrentTank() {
      MergedChemicalTank.Current current = this.chemicalTank.getCurrent();
      return this.chemicalTank.getTankFromCurrent(current == MergedChemicalTank.Current.EMPTY ? MergedChemicalTank.Current.GAS : current);
   }

   public ChemicalTankTier getTier() {
      return this.tier;
   }

   public MergedChemicalTank getChemicalTank() {
      return this.chemicalTank;
   }

   public IGasTank getGasTank() {
      return this.chemicalTank.getGasTank();
   }

   public IInfusionTank getInfusionTank() {
      return this.chemicalTank.getInfusionTank();
   }

   public IPigmentTank getPigmentTank() {
      return this.chemicalTank.getPigmentTank();
   }

   public ISlurryTank getSlurryTank() {
      return this.chemicalTank.getSlurryTank();
   }

   @Override
   public void parseUpgradeData(@NotNull IUpgradeData upgradeData) {
      if (upgradeData instanceof ChemicalTankUpgradeData data) {
         this.redstone = data.redstone;
         this.setControlType(data.controlType);
         this.drainSlot.setStack(data.drainSlot.getStack());
         this.fillSlot.setStack(data.fillSlot.getStack());
         this.dumping = data.dumping;
         this.getGasTank().setStack(data.storedGas);
         this.getInfusionTank().setStack(data.storedInfusion);
         this.getPigmentTank().setStack(data.storedPigment);
         this.getSlurryTank().setStack(data.storedSlurry);

         for (ITileComponent component : this.getComponents()) {
            component.read(data.components);
         }
      } else {
         super.parseUpgradeData(upgradeData);
      }
   }

   @NotNull
   public ChemicalTankUpgradeData getUpgradeData() {
      return new ChemicalTankUpgradeData(
         this.redstone,
         this.getControlType(),
         this.drainSlot,
         this.fillSlot,
         this.dumping,
         this.getGasTank().getStack(),
         this.getInfusionTank().getStack(),
         this.getPigmentTank().getStack(),
         this.getSlurryTank().getStack(),
         this.getComponents()
      );
   }

   @Override
   public void writeSustainedData(CompoundTag dataMap) {
      NBTUtils.writeEnum(dataMap, "dumping", this.dumping);
   }

   @Override
   public void readSustainedData(CompoundTag dataMap) {
      NBTUtils.setEnumIfPresent(dataMap, "dumping", TileEntityChemicalTank.GasMode::byIndexStatic, mode -> this.dumping = mode);
   }

   @Override
   public Map<String, String> getTileDataRemap() {
      Map<String, String> remap = new Object2ObjectOpenHashMap();
      remap.put("dumping", "dumping");
      return remap;
   }

   @Override
   public void addContainerTrackers(MekanismContainer container) {
      super.addContainerTrackers(container);
      container.track(
         SyncableEnum.create(
            TileEntityChemicalTank.GasMode::byIndexStatic, TileEntityChemicalTank.GasMode.IDLE, () -> this.dumping, value -> this.dumping = value
         )
      );
   }

   @ComputerMethod(
      requiresPublicSecurity = true,
      methodDescription = "Set the Dumping mode of the tank"
   )
   void setDumpingMode(TileEntityChemicalTank.GasMode mode) throws ComputerException {
      this.validateSecurityIsPublic();
      if (this.dumping != mode) {
         this.dumping = mode;
         this.markForSave();
      }
   }

   @ComputerMethod(
      requiresPublicSecurity = true,
      methodDescription = "Advance the Dumping mode to the next configuration in the list"
   )
   void incrementDumpingMode() throws ComputerException {
      this.validateSecurityIsPublic();
      this.nextMode(0);
   }

   @ComputerMethod(
      requiresPublicSecurity = true,
      methodDescription = "Descend the Dumping mode to the previous configuration in the list"
   )
   void decrementDumpingMode() throws ComputerException {
      this.validateSecurityIsPublic();
      this.dumping = this.dumping.getPrevious();
      this.markForSave();
   }

   @NothingNullByDefault
   public static enum GasMode implements IIncrementalEnum<TileEntityChemicalTank.GasMode>, IHasTextComponent {
      IDLE(MekanismLang.IDLE),
      DUMPING_EXCESS(MekanismLang.DUMPING_EXCESS),
      DUMPING(MekanismLang.DUMPING);

      private static final TileEntityChemicalTank.GasMode[] MODES = values();
      private final ILangEntry langEntry;

      private GasMode(ILangEntry langEntry) {
         this.langEntry = langEntry;
      }

      @Override
      public Component getTextComponent() {
         return this.langEntry.translate();
      }

      public TileEntityChemicalTank.GasMode byIndex(int index) {
         return byIndexStatic(index);
      }

      public static TileEntityChemicalTank.GasMode byIndexStatic(int index) {
         return MathUtils.getByIndexMod(MODES, index);
      }
   }
}
