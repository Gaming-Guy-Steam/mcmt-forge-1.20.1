package mekanism.common.tile.machine;

import java.util.List;
import mekanism.api.IContentsListener;
import mekanism.api.RelativeSide;
import mekanism.api.Upgrade;
import mekanism.api.chemical.ChemicalTankBuilder;
import mekanism.api.chemical.gas.Gas;
import mekanism.api.chemical.gas.GasStack;
import mekanism.api.chemical.gas.IGasTank;
import mekanism.api.math.FloatingLong;
import mekanism.api.recipes.ChemicalInfuserRecipe;
import mekanism.api.recipes.cache.CachedRecipe;
import mekanism.api.recipes.cache.ChemicalChemicalToChemicalCachedRecipe;
import mekanism.api.recipes.inputs.IInputHandler;
import mekanism.api.recipes.inputs.InputHelper;
import mekanism.api.recipes.outputs.IOutputHandler;
import mekanism.api.recipes.outputs.OutputHelper;
import mekanism.common.capabilities.energy.MachineEnergyContainer;
import mekanism.common.capabilities.holder.chemical.ChemicalTankHelper;
import mekanism.common.capabilities.holder.chemical.IChemicalTankHolder;
import mekanism.common.capabilities.holder.energy.EnergyContainerHelper;
import mekanism.common.capabilities.holder.energy.IEnergyContainerHolder;
import mekanism.common.capabilities.holder.slot.IInventorySlotHolder;
import mekanism.common.capabilities.holder.slot.InventorySlotHelper;
import mekanism.common.integration.computer.SpecialComputerMethodWrapper;
import mekanism.common.integration.computer.annotation.ComputerMethod;
import mekanism.common.integration.computer.annotation.WrappingComputerMethod;
import mekanism.common.inventory.container.MekanismContainer;
import mekanism.common.inventory.container.slot.ContainerSlotType;
import mekanism.common.inventory.container.slot.SlotOverlay;
import mekanism.common.inventory.container.sync.SyncableFloatingLong;
import mekanism.common.inventory.slot.EnergyInventorySlot;
import mekanism.common.inventory.slot.chemical.GasInventorySlot;
import mekanism.common.lib.transmitter.TransmissionType;
import mekanism.common.recipe.IMekanismRecipeTypeProvider;
import mekanism.common.recipe.MekanismRecipeType;
import mekanism.common.recipe.lookup.IEitherSideRecipeLookupHandler;
import mekanism.common.recipe.lookup.cache.InputRecipeCache;
import mekanism.common.registries.MekanismBlocks;
import mekanism.common.tile.component.TileComponentConfig;
import mekanism.common.tile.component.TileComponentEjector;
import mekanism.common.tile.component.config.ConfigInfo;
import mekanism.common.tile.component.config.DataType;
import mekanism.common.tile.component.config.slot.ChemicalSlotInfo;
import mekanism.common.tile.component.config.slot.InventorySlotInfo;
import mekanism.common.tile.prefab.TileEntityRecipeMachine;
import mekanism.common.util.MekanismUtils;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.block.state.BlockState;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class TileEntityChemicalInfuser
   extends TileEntityRecipeMachine<ChemicalInfuserRecipe>
   implements IEitherSideRecipeLookupHandler.EitherSideChemicalRecipeLookupHandler<Gas, GasStack, ChemicalInfuserRecipe> {
   private static final List<CachedRecipe.OperationTracker.RecipeError> TRACKED_ERROR_TYPES = List.of(
      CachedRecipe.OperationTracker.RecipeError.NOT_ENOUGH_ENERGY,
      CachedRecipe.OperationTracker.RecipeError.NOT_ENOUGH_ENERGY_REDUCED_RATE,
      CachedRecipe.OperationTracker.RecipeError.NOT_ENOUGH_LEFT_INPUT,
      CachedRecipe.OperationTracker.RecipeError.NOT_ENOUGH_RIGHT_INPUT,
      CachedRecipe.OperationTracker.RecipeError.NOT_ENOUGH_OUTPUT_SPACE,
      CachedRecipe.OperationTracker.RecipeError.INPUT_DOESNT_PRODUCE_OUTPUT
   );
   public static final long MAX_GAS = 10000L;
   @WrappingComputerMethod(
      wrapper = SpecialComputerMethodWrapper.ComputerChemicalTankWrapper.class,
      methodNames = {"getLeftInput", "getLeftInputCapacity", "getLeftInputNeeded", "getLeftInputFilledPercentage"},
      docPlaceholder = "left input tank"
   )
   public IGasTank leftTank;
   @WrappingComputerMethod(
      wrapper = SpecialComputerMethodWrapper.ComputerChemicalTankWrapper.class,
      methodNames = {"getRightInput", "getRightInputCapacity", "getRightInputNeeded", "getRightInputFilledPercentage"},
      docPlaceholder = "right input tank"
   )
   public IGasTank rightTank;
   @WrappingComputerMethod(
      wrapper = SpecialComputerMethodWrapper.ComputerChemicalTankWrapper.class,
      methodNames = {"getOutput", "getOutputCapacity", "getOutputNeeded", "getOutputFilledPercentage"},
      docPlaceholder = "output (center) tank"
   )
   public IGasTank centerTank;
   private FloatingLong clientEnergyUsed = FloatingLong.ZERO;
   private int baselineMaxOperations = 1;
   private final IOutputHandler<GasStack> outputHandler;
   private final IInputHandler<GasStack> leftInputHandler;
   private final IInputHandler<GasStack> rightInputHandler;
   private MachineEnergyContainer<TileEntityChemicalInfuser> energyContainer;
   @WrappingComputerMethod(
      wrapper = SpecialComputerMethodWrapper.ComputerIInventorySlotWrapper.class,
      methodNames = {"getLeftInputItem"},
      docPlaceholder = "left input item slot"
   )
   GasInventorySlot leftInputSlot;
   @WrappingComputerMethod(
      wrapper = SpecialComputerMethodWrapper.ComputerIInventorySlotWrapper.class,
      methodNames = {"getOutputItem"},
      docPlaceholder = "output item slot"
   )
   GasInventorySlot outputSlot;
   @WrappingComputerMethod(
      wrapper = SpecialComputerMethodWrapper.ComputerIInventorySlotWrapper.class,
      methodNames = {"getRightInputItem"},
      docPlaceholder = "right input item slot"
   )
   GasInventorySlot rightInputSlot;
   @WrappingComputerMethod(
      wrapper = SpecialComputerMethodWrapper.ComputerIInventorySlotWrapper.class,
      methodNames = {"getEnergyItem"},
      docPlaceholder = "energy slot"
   )
   EnergyInventorySlot energySlot;

   public TileEntityChemicalInfuser(BlockPos pos, BlockState state) {
      super(MekanismBlocks.CHEMICAL_INFUSER, pos, state, TRACKED_ERROR_TYPES);
      this.configComponent = new TileComponentConfig(this, TransmissionType.ITEM, TransmissionType.GAS, TransmissionType.ENERGY);
      ConfigInfo itemConfig = this.configComponent.getConfig(TransmissionType.ITEM);
      if (itemConfig != null) {
         itemConfig.addSlotInfo(DataType.INPUT_1, new InventorySlotInfo(true, true, this.leftInputSlot));
         itemConfig.addSlotInfo(DataType.INPUT_2, new InventorySlotInfo(true, true, this.rightInputSlot));
         itemConfig.addSlotInfo(DataType.OUTPUT, new InventorySlotInfo(true, true, this.outputSlot));
         itemConfig.addSlotInfo(DataType.INPUT_OUTPUT, new InventorySlotInfo(true, true, this.leftInputSlot, this.rightInputSlot, this.outputSlot));
         itemConfig.addSlotInfo(DataType.ENERGY, new InventorySlotInfo(true, true, this.energySlot));
         itemConfig.setDataType(DataType.INPUT_1, RelativeSide.LEFT);
         itemConfig.setDataType(DataType.INPUT_2, RelativeSide.RIGHT);
         itemConfig.setDataType(DataType.OUTPUT, RelativeSide.FRONT);
         itemConfig.setDataType(DataType.ENERGY, RelativeSide.BACK);
      }

      ConfigInfo gasConfig = this.configComponent.getConfig(TransmissionType.GAS);
      if (gasConfig != null) {
         gasConfig.addSlotInfo(DataType.INPUT_1, new ChemicalSlotInfo.GasSlotInfo(true, false, this.leftTank));
         gasConfig.addSlotInfo(DataType.INPUT_2, new ChemicalSlotInfo.GasSlotInfo(true, false, this.rightTank));
         gasConfig.addSlotInfo(DataType.OUTPUT, new ChemicalSlotInfo.GasSlotInfo(false, true, this.centerTank));
         gasConfig.addSlotInfo(DataType.INPUT_OUTPUT, new ChemicalSlotInfo.GasSlotInfo(true, true, this.leftTank, this.rightTank, this.centerTank));
         gasConfig.setDataType(DataType.INPUT_1, RelativeSide.LEFT);
         gasConfig.setDataType(DataType.INPUT_2, RelativeSide.RIGHT);
         gasConfig.setDataType(DataType.OUTPUT, RelativeSide.FRONT);
         gasConfig.setEjecting(true);
      }

      this.configComponent.setupInputConfig(TransmissionType.ENERGY, this.energyContainer);
      this.ejectorComponent = new TileComponentEjector(this);
      this.ejectorComponent.setOutputData(this.configComponent, TransmissionType.ITEM, TransmissionType.GAS).setCanTankEject(tank -> tank == this.centerTank);
      this.leftInputHandler = InputHelper.getInputHandler(this.leftTank, CachedRecipe.OperationTracker.RecipeError.NOT_ENOUGH_LEFT_INPUT);
      this.rightInputHandler = InputHelper.getInputHandler(this.rightTank, CachedRecipe.OperationTracker.RecipeError.NOT_ENOUGH_RIGHT_INPUT);
      this.outputHandler = OutputHelper.getOutputHandler(this.centerTank, CachedRecipe.OperationTracker.RecipeError.NOT_ENOUGH_OUTPUT_SPACE);
   }

   @NotNull
   @Override
   public IChemicalTankHolder<Gas, GasStack, IGasTank> getInitialGasTanks(IContentsListener listener, IContentsListener recipeCacheListener) {
      ChemicalTankHelper<Gas, GasStack, IGasTank> builder = ChemicalTankHelper.forSideGasWithConfig(this::getDirection, this::getConfig);
      builder.addTank(
         this.leftTank = (IGasTank)ChemicalTankBuilder.GAS
            .input(10000L, gas -> this.containsRecipe(gas, this.rightTank.getStack()), this::containsRecipe, recipeCacheListener)
      );
      builder.addTank(
         this.rightTank = (IGasTank)ChemicalTankBuilder.GAS
            .input(10000L, gas -> this.containsRecipe(gas, this.leftTank.getStack()), this::containsRecipe, recipeCacheListener)
      );
      builder.addTank(this.centerTank = (IGasTank)ChemicalTankBuilder.GAS.output(10000L, listener));
      return builder.build();
   }

   @NotNull
   @Override
   protected IEnergyContainerHolder getInitialEnergyContainers(IContentsListener listener, IContentsListener recipeCacheListener) {
      EnergyContainerHelper builder = EnergyContainerHelper.forSideWithConfig(this::getDirection, this::getConfig);
      builder.addContainer(this.energyContainer = MachineEnergyContainer.input(this, listener));
      return builder.build();
   }

   @NotNull
   @Override
   protected IInventorySlotHolder getInitialInventory(IContentsListener listener, IContentsListener recipeCacheListener) {
      InventorySlotHelper builder = InventorySlotHelper.forSideWithConfig(this::getDirection, this::getConfig);
      builder.addSlot(this.leftInputSlot = GasInventorySlot.fill(this.leftTank, listener, 6, 56));
      builder.addSlot(this.rightInputSlot = GasInventorySlot.fill(this.rightTank, listener, 154, 56));
      builder.addSlot(this.outputSlot = GasInventorySlot.drain(this.centerTank, listener, 80, 65));
      builder.addSlot(this.energySlot = EnergyInventorySlot.fillOrConvert(this.energyContainer, this::m_58904_, listener, 154, 14));
      this.leftInputSlot.setSlotType(ContainerSlotType.INPUT);
      this.leftInputSlot.setSlotOverlay(SlotOverlay.MINUS);
      this.rightInputSlot.setSlotType(ContainerSlotType.INPUT);
      this.rightInputSlot.setSlotOverlay(SlotOverlay.MINUS);
      this.outputSlot.setSlotType(ContainerSlotType.OUTPUT);
      this.outputSlot.setSlotOverlay(SlotOverlay.PLUS);
      return builder.build();
   }

   @Override
   protected void onUpdateServer() {
      super.onUpdateServer();
      this.energySlot.fillContainerOrConvert();
      this.leftInputSlot.fillTank();
      this.rightInputSlot.fillTank();
      this.outputSlot.drainTank();
      this.clientEnergyUsed = this.recipeCacheLookupMonitor.updateAndProcess(this.energyContainer);
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
   public IMekanismRecipeTypeProvider<ChemicalInfuserRecipe, InputRecipeCache.EitherSideChemical<Gas, GasStack, ChemicalInfuserRecipe>> getRecipeType() {
      return MekanismRecipeType.CHEMICAL_INFUSING;
   }

   @Nullable
   public ChemicalInfuserRecipe getRecipe(int cacheIndex) {
      return this.findFirstRecipe(this.leftInputHandler, this.rightInputHandler);
   }

   @NotNull
   public CachedRecipe<ChemicalInfuserRecipe> createNewCachedRecipe(@NotNull ChemicalInfuserRecipe recipe, int cacheIndex) {
      return new ChemicalChemicalToChemicalCachedRecipe(recipe, this.recheckAllRecipeErrors, this.leftInputHandler, this.rightInputHandler, this.outputHandler)
         .setErrorsChanged(x$0 -> this.onErrorsChanged(x$0))
         .setCanHolderFunction(() -> MekanismUtils.canFunction(this))
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

   public MachineEnergyContainer<TileEntityChemicalInfuser> getEnergyContainer() {
      return this.energyContainer;
   }

   @Override
   public void addContainerTrackers(MekanismContainer container) {
      super.addContainerTrackers(container);
      container.track(SyncableFloatingLong.create(this::getEnergyUsed, value -> this.clientEnergyUsed = value));
   }
}
