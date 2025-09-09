package mekanism.common.tile.machine;

import java.util.List;
import mekanism.api.IContentsListener;
import mekanism.api.RelativeSide;
import mekanism.api.Upgrade;
import mekanism.api.chemical.ChemicalTankBuilder;
import mekanism.api.chemical.attribute.ChemicalAttributeValidator;
import mekanism.api.chemical.gas.Gas;
import mekanism.api.chemical.gas.GasStack;
import mekanism.api.chemical.gas.IGasTank;
import mekanism.api.math.FloatingLong;
import mekanism.api.recipes.GasToGasRecipe;
import mekanism.api.recipes.cache.CachedRecipe;
import mekanism.api.recipes.cache.OneInputCachedRecipe;
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
import mekanism.common.recipe.lookup.ISingleRecipeLookupHandler;
import mekanism.common.recipe.lookup.cache.InputRecipeCache;
import mekanism.common.registries.MekanismBlocks;
import mekanism.common.tile.base.SubstanceType;
import mekanism.common.tile.component.TileComponentConfig;
import mekanism.common.tile.component.TileComponentEjector;
import mekanism.common.tile.interfaces.IBoundingBlock;
import mekanism.common.tile.prefab.TileEntityRecipeMachine;
import mekanism.common.util.MekanismUtils;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.block.state.BlockState;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class TileEntityIsotopicCentrifuge
   extends TileEntityRecipeMachine<GasToGasRecipe>
   implements IBoundingBlock,
   ISingleRecipeLookupHandler.ChemicalRecipeLookupHandler<Gas, GasStack, GasToGasRecipe> {
   private static final List<CachedRecipe.OperationTracker.RecipeError> TRACKED_ERROR_TYPES = List.of(
      CachedRecipe.OperationTracker.RecipeError.NOT_ENOUGH_ENERGY,
      CachedRecipe.OperationTracker.RecipeError.NOT_ENOUGH_ENERGY_REDUCED_RATE,
      CachedRecipe.OperationTracker.RecipeError.NOT_ENOUGH_INPUT,
      CachedRecipe.OperationTracker.RecipeError.NOT_ENOUGH_OUTPUT_SPACE,
      CachedRecipe.OperationTracker.RecipeError.INPUT_DOESNT_PRODUCE_OUTPUT
   );
   public static final int MAX_GAS = 10000;
   @WrappingComputerMethod(
      wrapper = SpecialComputerMethodWrapper.ComputerChemicalTankWrapper.class,
      methodNames = {"getInput", "getInputCapacity", "getInputNeeded", "getInputFilledPercentage"},
      docPlaceholder = "input tank"
   )
   public IGasTank inputTank;
   @WrappingComputerMethod(
      wrapper = SpecialComputerMethodWrapper.ComputerChemicalTankWrapper.class,
      methodNames = {"getOutput", "getOutputCapacity", "getOutputNeeded", "getOutputFilledPercentage"},
      docPlaceholder = "output tank"
   )
   public IGasTank outputTank;
   private FloatingLong clientEnergyUsed = FloatingLong.ZERO;
   private int baselineMaxOperations = 1;
   private final IOutputHandler<GasStack> outputHandler;
   private final IInputHandler<GasStack> inputHandler;
   private MachineEnergyContainer<TileEntityIsotopicCentrifuge> energyContainer;
   @WrappingComputerMethod(
      wrapper = SpecialComputerMethodWrapper.ComputerIInventorySlotWrapper.class,
      methodNames = {"getInputItem"},
      docPlaceholder = "input slot"
   )
   GasInventorySlot inputSlot;
   @WrappingComputerMethod(
      wrapper = SpecialComputerMethodWrapper.ComputerIInventorySlotWrapper.class,
      methodNames = {"getOutputItem"},
      docPlaceholder = "output slot"
   )
   GasInventorySlot outputSlot;
   @WrappingComputerMethod(
      wrapper = SpecialComputerMethodWrapper.ComputerIInventorySlotWrapper.class,
      methodNames = {"getEnergyItem"},
      docPlaceholder = "energy slot"
   )
   EnergyInventorySlot energySlot;

   public TileEntityIsotopicCentrifuge(BlockPos pos, BlockState state) {
      super(MekanismBlocks.ISOTOPIC_CENTRIFUGE, pos, state, TRACKED_ERROR_TYPES);
      this.configComponent = new TileComponentConfig(this, TransmissionType.ITEM, TransmissionType.GAS, TransmissionType.ENERGY);
      this.configComponent.setupItemIOConfig(this.inputSlot, this.outputSlot, this.energySlot);
      this.configComponent.setupIOConfig(TransmissionType.GAS, this.inputTank, this.outputTank, RelativeSide.FRONT, false, true).setEjecting(true);
      this.configComponent.setupInputConfig(TransmissionType.ENERGY, this.energyContainer);
      this.configComponent.addDisabledSides(RelativeSide.TOP);
      this.ejectorComponent = new TileComponentEjector(this);
      this.ejectorComponent.setOutputData(this.configComponent, TransmissionType.ITEM, TransmissionType.GAS).setCanTankEject(tank -> tank != this.inputTank);
      this.inputHandler = InputHelper.getInputHandler(this.inputTank, CachedRecipe.OperationTracker.RecipeError.NOT_ENOUGH_INPUT);
      this.outputHandler = OutputHelper.getOutputHandler(this.outputTank, CachedRecipe.OperationTracker.RecipeError.NOT_ENOUGH_OUTPUT_SPACE);
   }

   @NotNull
   @Override
   public IChemicalTankHolder<Gas, GasStack, IGasTank> getInitialGasTanks(IContentsListener listener, IContentsListener recipeCacheListener) {
      ChemicalTankHelper<Gas, GasStack, IGasTank> builder = ChemicalTankHelper.forSideGasWithConfig(this::getDirection, this::getConfig);
      builder.addTank(
         this.inputTank = (IGasTank)ChemicalTankBuilder.GAS
            .create(
               10000L,
               ChemicalTankHelper.radioactiveInputTankPredicate(() -> this.outputTank),
               ChemicalTankBuilder.GAS.alwaysTrueBi,
               this::containsRecipe,
               ChemicalAttributeValidator.ALWAYS_ALLOW,
               recipeCacheListener
            )
      );
      builder.addTank(this.outputTank = (IGasTank)ChemicalTankBuilder.GAS.output(10000L, listener));
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
      builder.addSlot(this.inputSlot = GasInventorySlot.fill(this.inputTank, listener, 5, 56));
      builder.addSlot(this.outputSlot = GasInventorySlot.drain(this.outputTank, listener, 155, 56));
      builder.addSlot(this.energySlot = EnergyInventorySlot.fillOrConvert(this.energyContainer, this::m_58904_, listener, 155, 14));
      this.inputSlot.setSlotType(ContainerSlotType.INPUT);
      this.inputSlot.setSlotOverlay(SlotOverlay.MINUS);
      this.outputSlot.setSlotType(ContainerSlotType.OUTPUT);
      this.outputSlot.setSlotOverlay(SlotOverlay.PLUS);
      return builder.build();
   }

   @Override
   protected void onUpdateServer() {
      super.onUpdateServer();
      this.energySlot.fillContainerOrConvert();
      this.inputSlot.fillTank();
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
   public IMekanismRecipeTypeProvider<GasToGasRecipe, InputRecipeCache.SingleChemical<Gas, GasStack, GasToGasRecipe>> getRecipeType() {
      return MekanismRecipeType.CENTRIFUGING;
   }

   @Nullable
   public GasToGasRecipe getRecipe(int cacheIndex) {
      return this.findFirstRecipe(this.inputHandler);
   }

   @NotNull
   public CachedRecipe<GasToGasRecipe> createNewCachedRecipe(@NotNull GasToGasRecipe recipe, int cacheIndex) {
      return OneInputCachedRecipe.chemicalToChemical(recipe, this.recheckAllRecipeErrors, this.inputHandler, this.outputHandler)
         .setErrorsChanged(x$0 -> this.onErrorsChanged(x$0))
         .setCanHolderFunction(() -> MekanismUtils.canFunction(this))
         .setActive(this::setActive)
         .setOnFinish(this::markForSave)
         .setEnergyRequirements(this.energyContainer::getEnergyPerTick, this.energyContainer)
         .setBaselineMaxOperations(() -> this.baselineMaxOperations);
   }

   @Override
   public void recalculateUpgrades(Upgrade upgrade) {
      super.recalculateUpgrades(upgrade);
      if (upgrade == Upgrade.SPEED) {
         this.baselineMaxOperations = (int)Math.pow(2.0, this.upgradeComponent.getUpgrades(Upgrade.SPEED));
      }
   }

   public MachineEnergyContainer<TileEntityIsotopicCentrifuge> getEnergyContainer() {
      return this.energyContainer;
   }

   @Override
   public int getRedstoneLevel() {
      return MekanismUtils.redstoneLevelFromContents(this.inputTank.getStored(), this.inputTank.getCapacity());
   }

   @Override
   protected boolean makesComparatorDirty(@Nullable SubstanceType type) {
      return type == SubstanceType.GAS;
   }

   @Override
   public void addContainerTrackers(MekanismContainer container) {
      super.addContainerTrackers(container);
      container.track(SyncableFloatingLong.create(this::getEnergyUsed, value -> this.clientEnergyUsed = value));
   }
}
