package mekanism.common.tile.machine;

import it.unimi.dsi.fastutil.objects.Object2ObjectOpenHashMap;
import java.util.List;
import java.util.Map;
import java.util.function.BiFunction;
import mekanism.api.Action;
import mekanism.api.IContentsListener;
import mekanism.api.RelativeSide;
import mekanism.api.Upgrade;
import mekanism.api.chemical.ChemicalTankBuilder;
import mekanism.api.chemical.gas.Gas;
import mekanism.api.chemical.gas.GasStack;
import mekanism.api.chemical.gas.IGasTank;
import mekanism.api.math.FloatingLong;
import mekanism.api.math.MathUtils;
import mekanism.api.recipes.ElectrolysisRecipe;
import mekanism.api.recipes.cache.CachedRecipe;
import mekanism.api.recipes.cache.OneInputCachedRecipe;
import mekanism.api.recipes.inputs.IInputHandler;
import mekanism.api.recipes.inputs.InputHelper;
import mekanism.api.recipes.outputs.IOutputHandler;
import mekanism.api.recipes.outputs.OutputHelper;
import mekanism.common.capabilities.energy.FixedUsageEnergyContainer;
import mekanism.common.capabilities.fluid.BasicFluidTank;
import mekanism.common.capabilities.holder.chemical.ChemicalTankHelper;
import mekanism.common.capabilities.holder.chemical.IChemicalTankHolder;
import mekanism.common.capabilities.holder.energy.EnergyContainerHelper;
import mekanism.common.capabilities.holder.energy.IEnergyContainerHolder;
import mekanism.common.capabilities.holder.fluid.FluidTankHelper;
import mekanism.common.capabilities.holder.fluid.IFluidTankHolder;
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
import mekanism.common.inventory.container.sync.SyncableEnum;
import mekanism.common.inventory.container.sync.SyncableFloatingLong;
import mekanism.common.inventory.slot.EnergyInventorySlot;
import mekanism.common.inventory.slot.FluidInventorySlot;
import mekanism.common.inventory.slot.chemical.GasInventorySlot;
import mekanism.common.lib.transmitter.TransmissionType;
import mekanism.common.recipe.IMekanismRecipeTypeProvider;
import mekanism.common.recipe.MekanismRecipeType;
import mekanism.common.recipe.lookup.ISingleRecipeLookupHandler;
import mekanism.common.recipe.lookup.cache.InputRecipeCache;
import mekanism.common.registries.MekanismBlocks;
import mekanism.common.tile.TileEntityChemicalTank;
import mekanism.common.tile.base.SubstanceType;
import mekanism.common.tile.component.TileComponentConfig;
import mekanism.common.tile.component.TileComponentEjector;
import mekanism.common.tile.component.config.ConfigInfo;
import mekanism.common.tile.component.config.DataType;
import mekanism.common.tile.component.config.slot.ChemicalSlotInfo;
import mekanism.common.tile.component.config.slot.InventorySlotInfo;
import mekanism.common.tile.interfaces.IHasGasMode;
import mekanism.common.tile.interfaces.ISustainedData;
import mekanism.common.tile.prefab.TileEntityRecipeMachine;
import mekanism.common.util.MekanismUtils;
import mekanism.common.util.NBTUtils;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraftforge.fluids.FluidStack;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class TileEntityElectrolyticSeparator
   extends TileEntityRecipeMachine<ElectrolysisRecipe>
   implements IHasGasMode,
   ISingleRecipeLookupHandler.FluidRecipeLookupHandler<ElectrolysisRecipe>,
   ISustainedData {
   public static final CachedRecipe.OperationTracker.RecipeError NOT_ENOUGH_SPACE_LEFT_OUTPUT_ERROR = CachedRecipe.OperationTracker.RecipeError.create();
   public static final CachedRecipe.OperationTracker.RecipeError NOT_ENOUGH_SPACE_RIGHT_OUTPUT_ERROR = CachedRecipe.OperationTracker.RecipeError.create();
   private static final List<CachedRecipe.OperationTracker.RecipeError> TRACKED_ERROR_TYPES = List.of(
      CachedRecipe.OperationTracker.RecipeError.NOT_ENOUGH_ENERGY,
      CachedRecipe.OperationTracker.RecipeError.NOT_ENOUGH_ENERGY_REDUCED_RATE,
      CachedRecipe.OperationTracker.RecipeError.NOT_ENOUGH_INPUT,
      NOT_ENOUGH_SPACE_LEFT_OUTPUT_ERROR,
      NOT_ENOUGH_SPACE_RIGHT_OUTPUT_ERROR,
      CachedRecipe.OperationTracker.RecipeError.INPUT_DOESNT_PRODUCE_OUTPUT
   );
   private static final long MAX_GAS = 2400L;
   private static final BiFunction<FloatingLong, TileEntityElectrolyticSeparator, FloatingLong> BASE_ENERGY_CALCULATOR = (base, tile) -> base.multiply(
      tile.getRecipeEnergyMultiplier()
   );
   @WrappingComputerMethod(
      wrapper = SpecialComputerMethodWrapper.ComputerFluidTankWrapper.class,
      methodNames = {"getInput", "getInputCapacity", "getInputNeeded", "getInputFilledPercentage"},
      docPlaceholder = "input tank"
   )
   public BasicFluidTank fluidTank;
   @WrappingComputerMethod(
      wrapper = SpecialComputerMethodWrapper.ComputerChemicalTankWrapper.class,
      methodNames = {"getLeftOutput", "getLeftOutputCapacity", "getLeftOutputNeeded", "getLeftOutputFilledPercentage"},
      docPlaceholder = "left output tank"
   )
   public IGasTank leftTank;
   @WrappingComputerMethod(
      wrapper = SpecialComputerMethodWrapper.ComputerChemicalTankWrapper.class,
      methodNames = {"getRightOutput", "getRightOutputCapacity", "getRightOutputNeeded", "getRightOutputFilledPercentage"},
      docPlaceholder = "right output tank"
   )
   public IGasTank rightTank;
   @SyntheticComputerMethod(
      getter = "getLeftOutputDumpingMode"
   )
   public TileEntityChemicalTank.GasMode dumpLeft = TileEntityChemicalTank.GasMode.IDLE;
   @SyntheticComputerMethod(
      getter = "getRightOutputDumpingMode"
   )
   public TileEntityChemicalTank.GasMode dumpRight = TileEntityChemicalTank.GasMode.IDLE;
   private FloatingLong clientEnergyUsed = FloatingLong.ZERO;
   private FloatingLong recipeEnergyMultiplier = FloatingLong.ONE;
   private int baselineMaxOperations = 1;
   private final IOutputHandler<ElectrolysisRecipe.ElectrolysisRecipeOutput> outputHandler;
   private final IInputHandler<FluidStack> inputHandler;
   private FixedUsageEnergyContainer<TileEntityElectrolyticSeparator> energyContainer;
   @WrappingComputerMethod(
      wrapper = SpecialComputerMethodWrapper.ComputerIInventorySlotWrapper.class,
      methodNames = {"getInputItem"},
      docPlaceholder = "input item slot"
   )
   FluidInventorySlot fluidSlot;
   @WrappingComputerMethod(
      wrapper = SpecialComputerMethodWrapper.ComputerIInventorySlotWrapper.class,
      methodNames = {"getLeftOutputItem"},
      docPlaceholder = "left output item slot"
   )
   GasInventorySlot leftOutputSlot;
   @WrappingComputerMethod(
      wrapper = SpecialComputerMethodWrapper.ComputerIInventorySlotWrapper.class,
      methodNames = {"getRightOutputItem"},
      docPlaceholder = "right output item slot"
   )
   GasInventorySlot rightOutputSlot;
   @WrappingComputerMethod(
      wrapper = SpecialComputerMethodWrapper.ComputerIInventorySlotWrapper.class,
      methodNames = {"getEnergyItem"},
      docPlaceholder = "energy slot"
   )
   EnergyInventorySlot energySlot;

   public TileEntityElectrolyticSeparator(BlockPos pos, BlockState state) {
      super(MekanismBlocks.ELECTROLYTIC_SEPARATOR, pos, state, TRACKED_ERROR_TYPES);
      this.configComponent = new TileComponentConfig(this, TransmissionType.ITEM, TransmissionType.GAS, TransmissionType.FLUID, TransmissionType.ENERGY);
      ConfigInfo itemConfig = this.configComponent.getConfig(TransmissionType.ITEM);
      if (itemConfig != null) {
         itemConfig.addSlotInfo(DataType.INPUT, new InventorySlotInfo(true, true, this.fluidSlot));
         itemConfig.addSlotInfo(DataType.OUTPUT_1, new InventorySlotInfo(true, true, this.leftOutputSlot));
         itemConfig.addSlotInfo(DataType.OUTPUT_2, new InventorySlotInfo(true, true, this.rightOutputSlot));
         itemConfig.addSlotInfo(DataType.INPUT_OUTPUT, new InventorySlotInfo(true, true, this.fluidSlot, this.leftOutputSlot, this.rightOutputSlot));
         itemConfig.addSlotInfo(DataType.ENERGY, new InventorySlotInfo(true, true, this.energySlot));
         itemConfig.setDataType(DataType.INPUT, RelativeSide.FRONT);
         itemConfig.setDataType(DataType.OUTPUT_1, RelativeSide.LEFT);
         itemConfig.setDataType(DataType.OUTPUT_2, RelativeSide.RIGHT);
         itemConfig.setDataType(DataType.ENERGY, RelativeSide.BACK);
      }

      ConfigInfo gasConfig = this.configComponent.getConfig(TransmissionType.GAS);
      if (gasConfig != null) {
         gasConfig.addSlotInfo(DataType.OUTPUT_1, new ChemicalSlotInfo.GasSlotInfo(false, true, this.leftTank));
         gasConfig.addSlotInfo(DataType.OUTPUT_2, new ChemicalSlotInfo.GasSlotInfo(false, true, this.rightTank));
         gasConfig.setDataType(DataType.OUTPUT_1, RelativeSide.LEFT);
         gasConfig.setDataType(DataType.OUTPUT_2, RelativeSide.RIGHT);
         gasConfig.setEjecting(true);
      }

      this.configComponent.setupInputConfig(TransmissionType.FLUID, this.fluidTank);
      this.configComponent.setupInputConfig(TransmissionType.ENERGY, this.energyContainer);
      this.ejectorComponent = new TileComponentEjector(this);
      this.ejectorComponent.setOutputData(this.configComponent, TransmissionType.ITEM, TransmissionType.GAS).setCanTankEject(tank -> {
         if (tank == this.leftTank) {
            return this.dumpLeft != TileEntityChemicalTank.GasMode.DUMPING;
         } else {
            return tank == this.rightTank ? this.dumpRight != TileEntityChemicalTank.GasMode.DUMPING : true;
         }
      });
      this.inputHandler = InputHelper.getInputHandler(this.fluidTank, CachedRecipe.OperationTracker.RecipeError.NOT_ENOUGH_INPUT);
      this.outputHandler = OutputHelper.getOutputHandler(this.leftTank, NOT_ENOUGH_SPACE_LEFT_OUTPUT_ERROR, this.rightTank, NOT_ENOUGH_SPACE_RIGHT_OUTPUT_ERROR);
   }

   @NotNull
   @Override
   protected IFluidTankHolder getInitialFluidTanks(IContentsListener listener, IContentsListener recipeCacheListener) {
      FluidTankHelper builder = FluidTankHelper.forSideWithConfig(this::getDirection, this::getConfig);
      builder.addTank(this.fluidTank = BasicFluidTank.input(24000, this::containsRecipe, recipeCacheListener));
      return builder.build();
   }

   @NotNull
   @Override
   public IChemicalTankHolder<Gas, GasStack, IGasTank> getInitialGasTanks(IContentsListener listener, IContentsListener recipeCacheListener) {
      ChemicalTankHelper<Gas, GasStack, IGasTank> builder = ChemicalTankHelper.forSideGasWithConfig(this::getDirection, this::getConfig);
      builder.addTank(this.leftTank = (IGasTank)ChemicalTankBuilder.GAS.output(2400L, listener));
      builder.addTank(this.rightTank = (IGasTank)ChemicalTankBuilder.GAS.output(2400L, listener));
      return builder.build();
   }

   @NotNull
   @Override
   protected IEnergyContainerHolder getInitialEnergyContainers(IContentsListener listener, IContentsListener recipeCacheListener) {
      EnergyContainerHelper builder = EnergyContainerHelper.forSideWithConfig(this::getDirection, this::getConfig);
      builder.addContainer(this.energyContainer = FixedUsageEnergyContainer.input(this, BASE_ENERGY_CALCULATOR, listener));
      return builder.build();
   }

   @NotNull
   @Override
   protected IInventorySlotHolder getInitialInventory(IContentsListener listener, IContentsListener recipeCacheListener) {
      InventorySlotHelper builder = InventorySlotHelper.forSideWithConfig(this::getDirection, this::getConfig);
      builder.addSlot(this.fluidSlot = FluidInventorySlot.fill(this.fluidTank, listener, 26, 35));
      builder.addSlot(this.leftOutputSlot = GasInventorySlot.drain(this.leftTank, listener, 59, 52));
      builder.addSlot(this.rightOutputSlot = GasInventorySlot.drain(this.rightTank, listener, 101, 52));
      builder.addSlot(this.energySlot = EnergyInventorySlot.fillOrConvert(this.energyContainer, this::m_58904_, listener, 143, 35));
      this.fluidSlot.setSlotType(ContainerSlotType.INPUT);
      this.leftOutputSlot.setSlotType(ContainerSlotType.OUTPUT);
      this.rightOutputSlot.setSlotType(ContainerSlotType.OUTPUT);
      return builder.build();
   }

   @Override
   public void onCachedRecipeChanged(@Nullable CachedRecipe<ElectrolysisRecipe> cachedRecipe, int cacheIndex) {
      super.onCachedRecipeChanged(cachedRecipe, cacheIndex);
      this.recipeEnergyMultiplier = cachedRecipe == null ? FloatingLong.ONE : cachedRecipe.getRecipe().getEnergyMultiplier();
      this.energyContainer.updateEnergyPerTick();
   }

   @Override
   protected void onUpdateServer() {
      super.onUpdateServer();
      this.energySlot.fillContainerOrConvert();
      this.fluidSlot.fillTank();
      this.leftOutputSlot.drainTank();
      this.rightOutputSlot.drainTank();
      this.clientEnergyUsed = this.recipeCacheLookupMonitor.updateAndProcess(this.energyContainer);
      this.handleTank(this.leftTank, this.dumpLeft);
      this.handleTank(this.rightTank, this.dumpRight);
   }

   private void handleTank(IGasTank tank, TileEntityChemicalTank.GasMode mode) {
      if (!tank.isEmpty()) {
         if (mode == TileEntityChemicalTank.GasMode.DUMPING) {
            tank.shrinkStack(8L * (long)Math.pow(2.0, this.upgradeComponent.getUpgrades(Upgrade.SPEED)), Action.EXECUTE);
         } else if (mode == TileEntityChemicalTank.GasMode.DUMPING_EXCESS) {
            long target = this.getDumpingExcessTarget(tank);
            long stored = tank.getStored();
            if (target < stored) {
               tank.shrinkStack(Math.min(stored - target, MekanismConfig.general.chemicalAutoEjectRate.get()), Action.EXECUTE);
            }
         }
      }
   }

   private long getDumpingExcessTarget(IGasTank tank) {
      return MathUtils.clampToLong(tank.getCapacity() * MekanismConfig.general.dumpExcessKeepRatio.get());
   }

   private boolean atDumpingExcessTarget(IGasTank tank) {
      return tank.getStored() >= this.getDumpingExcessTarget(tank);
   }

   private boolean canFunction() {
      return MekanismUtils.canFunction(this)
         && (
            this.dumpLeft != TileEntityChemicalTank.GasMode.DUMPING_EXCESS
               || this.dumpRight != TileEntityChemicalTank.GasMode.DUMPING_EXCESS
               || !this.atDumpingExcessTarget(this.leftTank)
               || !this.atDumpingExcessTarget(this.rightTank)
         );
   }

   public FloatingLong getRecipeEnergyMultiplier() {
      return this.recipeEnergyMultiplier;
   }

   @NotNull
   @ComputerMethod(
      nameOverride = "getEnergyUsage",
      methodDescription = "Get the energy used in the last tick by the machine"
   )
   public FloatingLong getEnergyUsed() {
      return this.clientEnergyUsed;
   }

   @NotNull
   @Override
   public IMekanismRecipeTypeProvider<ElectrolysisRecipe, InputRecipeCache.SingleFluid<ElectrolysisRecipe>> getRecipeType() {
      return MekanismRecipeType.SEPARATING;
   }

   @Nullable
   public ElectrolysisRecipe getRecipe(int cacheIndex) {
      return this.findFirstRecipe(this.inputHandler);
   }

   @NotNull
   public CachedRecipe<ElectrolysisRecipe> createNewCachedRecipe(@NotNull ElectrolysisRecipe recipe, int cacheIndex) {
      return OneInputCachedRecipe.separating(recipe, this.recheckAllRecipeErrors, this.inputHandler, this.outputHandler)
         .setErrorsChanged(x$0 -> this.onErrorsChanged(x$0))
         .setCanHolderFunction(this::canFunction)
         .setActive(this::setActive)
         .setEnergyRequirements(this.energyContainer::getEnergyPerTick, this.energyContainer)
         .setBaselineMaxOperations(() -> this.baselineMaxOperations)
         .setOnFinish(this::markForSave);
   }

   @Override
   public void recalculateUpgrades(Upgrade upgrade) {
      super.recalculateUpgrades(upgrade);
      if (upgrade == Upgrade.SPEED) {
         this.baselineMaxOperations = (int)Math.pow(2.0, this.upgradeComponent.getUpgrades(Upgrade.SPEED));
      }
   }

   public FixedUsageEnergyContainer<TileEntityElectrolyticSeparator> getEnergyContainer() {
      return this.energyContainer;
   }

   @Override
   public void nextMode(int tank) {
      if (tank == 0) {
         this.dumpLeft = this.dumpLeft.getNext();
         this.markForSave();
      } else if (tank == 1) {
         this.dumpRight = this.dumpRight.getNext();
         this.markForSave();
      }
   }

   @Override
   public void writeSustainedData(CompoundTag dataMap) {
      NBTUtils.writeEnum(dataMap, "dumpLeft", this.dumpLeft);
      NBTUtils.writeEnum(dataMap, "dumpRight", this.dumpRight);
   }

   @Override
   public void readSustainedData(CompoundTag dataMap) {
      NBTUtils.setEnumIfPresent(dataMap, "dumpLeft", TileEntityChemicalTank.GasMode::byIndexStatic, mode -> this.dumpLeft = mode);
      NBTUtils.setEnumIfPresent(dataMap, "dumpRight", TileEntityChemicalTank.GasMode::byIndexStatic, mode -> this.dumpRight = mode);
   }

   @Override
   public Map<String, String> getTileDataRemap() {
      Map<String, String> remap = new Object2ObjectOpenHashMap();
      remap.put("dumpLeft", "dumpLeft");
      remap.put("dumpRight", "dumpRight");
      return remap;
   }

   @Override
   public int getRedstoneLevel() {
      return MekanismUtils.redstoneLevelFromContents(this.fluidTank.getFluidAmount(), this.fluidTank.getCapacity());
   }

   @Override
   protected boolean makesComparatorDirty(@Nullable SubstanceType type) {
      return type == SubstanceType.FLUID;
   }

   @Override
   public void addContainerTrackers(MekanismContainer container) {
      super.addContainerTrackers(container);
      container.track(
         SyncableEnum.create(
            TileEntityChemicalTank.GasMode::byIndexStatic, TileEntityChemicalTank.GasMode.IDLE, () -> this.dumpLeft, value -> this.dumpLeft = value
         )
      );
      container.track(
         SyncableEnum.create(
            TileEntityChemicalTank.GasMode::byIndexStatic, TileEntityChemicalTank.GasMode.IDLE, () -> this.dumpRight, value -> this.dumpRight = value
         )
      );
      container.track(SyncableFloatingLong.create(this::getEnergyUsed, value -> this.clientEnergyUsed = value));
   }

   @ComputerMethod(
      requiresPublicSecurity = true
   )
   void setLeftOutputDumpingMode(TileEntityChemicalTank.GasMode mode) throws ComputerException {
      this.validateSecurityIsPublic();
      if (this.dumpLeft != mode) {
         this.dumpLeft = mode;
         this.markForSave();
      }
   }

   @ComputerMethod(
      requiresPublicSecurity = true
   )
   void incrementLeftOutputDumpingMode() throws ComputerException {
      this.validateSecurityIsPublic();
      this.nextMode(0);
   }

   @ComputerMethod(
      requiresPublicSecurity = true
   )
   void decrementLeftOutputDumpingMode() throws ComputerException {
      this.validateSecurityIsPublic();
      this.dumpLeft = this.dumpLeft.getPrevious();
      this.markForSave();
   }

   @ComputerMethod(
      requiresPublicSecurity = true
   )
   void setRightOutputDumpingMode(TileEntityChemicalTank.GasMode mode) throws ComputerException {
      this.validateSecurityIsPublic();
      if (this.dumpRight != mode) {
         this.dumpRight = mode;
         this.markForSave();
      }
   }

   @ComputerMethod(
      requiresPublicSecurity = true
   )
   void incrementRightOutputDumpingMode() throws ComputerException {
      this.validateSecurityIsPublic();
      this.nextMode(1);
   }

   @ComputerMethod(
      requiresPublicSecurity = true
   )
   void decrementRightOutputDumpingMode() throws ComputerException {
      this.validateSecurityIsPublic();
      this.dumpRight = this.dumpRight.getPrevious();
      this.markForSave();
   }
}
