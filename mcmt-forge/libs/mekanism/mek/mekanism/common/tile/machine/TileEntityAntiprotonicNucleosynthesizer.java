package mekanism.common.tile.machine;

import java.util.List;
import mekanism.api.IContentsListener;
import mekanism.api.chemical.ChemicalTankBuilder;
import mekanism.api.chemical.gas.Gas;
import mekanism.api.chemical.gas.GasStack;
import mekanism.api.chemical.gas.IGasTank;
import mekanism.api.math.FloatingLong;
import mekanism.api.recipes.NucleosynthesizingRecipe;
import mekanism.api.recipes.cache.CachedRecipe;
import mekanism.api.recipes.cache.TwoInputCachedRecipe;
import mekanism.api.recipes.inputs.IInputHandler;
import mekanism.api.recipes.inputs.ILongInputHandler;
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
import mekanism.common.inventory.container.slot.SlotOverlay;
import mekanism.common.inventory.container.sync.SyncableFloatingLong;
import mekanism.common.inventory.slot.EnergyInventorySlot;
import mekanism.common.inventory.slot.InputInventorySlot;
import mekanism.common.inventory.slot.OutputInventorySlot;
import mekanism.common.inventory.slot.chemical.GasInventorySlot;
import mekanism.common.inventory.warning.WarningTracker;
import mekanism.common.lib.transmitter.TransmissionType;
import mekanism.common.recipe.IMekanismRecipeTypeProvider;
import mekanism.common.recipe.MekanismRecipeType;
import mekanism.common.recipe.lookup.IDoubleRecipeLookupHandler;
import mekanism.common.recipe.lookup.cache.InputRecipeCache;
import mekanism.common.recipe.lookup.monitor.NucleosynthesizerRecipeCacheLookupMonitor;
import mekanism.common.recipe.lookup.monitor.RecipeCacheLookupMonitor;
import mekanism.common.registries.MekanismBlocks;
import mekanism.common.tile.component.TileComponentConfig;
import mekanism.common.tile.component.TileComponentEjector;
import mekanism.common.tile.prefab.TileEntityProgressMachine;
import mekanism.common.util.MekanismUtils;
import net.minecraft.core.BlockPos;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.state.BlockState;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class TileEntityAntiprotonicNucleosynthesizer
   extends TileEntityProgressMachine<NucleosynthesizingRecipe>
   implements IDoubleRecipeLookupHandler.ItemChemicalRecipeLookupHandler<Gas, GasStack, NucleosynthesizingRecipe> {
   private static final List<CachedRecipe.OperationTracker.RecipeError> TRACKED_ERROR_TYPES = List.of(
      CachedRecipe.OperationTracker.RecipeError.NOT_ENOUGH_ENERGY,
      CachedRecipe.OperationTracker.RecipeError.NOT_ENOUGH_INPUT,
      CachedRecipe.OperationTracker.RecipeError.NOT_ENOUGH_SECONDARY_INPUT,
      CachedRecipe.OperationTracker.RecipeError.NOT_ENOUGH_OUTPUT_SPACE,
      CachedRecipe.OperationTracker.RecipeError.INPUT_DOESNT_PRODUCE_OUTPUT
   );
   private static final int BASE_DURATION = 400;
   private static final long MAX_GAS = 10000L;
   @WrappingComputerMethod(
      wrapper = SpecialComputerMethodWrapper.ComputerChemicalTankWrapper.class,
      methodNames = {"getInputChemical", "getInputChemicalCapacity", "getInputChemicalNeeded", "getInputChemicalFilledPercentage"},
      docPlaceholder = "input gas tank"
   )
   public IGasTank gasTank;
   protected final IOutputHandler<ItemStack> outputHandler;
   protected final IInputHandler<ItemStack> itemInputHandler;
   protected final ILongInputHandler<GasStack> gasInputHandler;
   private MachineEnergyContainer<TileEntityAntiprotonicNucleosynthesizer> energyContainer;
   @WrappingComputerMethod(
      wrapper = SpecialComputerMethodWrapper.ComputerIInventorySlotWrapper.class,
      methodNames = {"getInputChemicalItem"},
      docPlaceholder = "input gas item slot"
   )
   GasInventorySlot gasInputSlot;
   @WrappingComputerMethod(
      wrapper = SpecialComputerMethodWrapper.ComputerIInventorySlotWrapper.class,
      methodNames = {"getInputItem"},
      docPlaceholder = "input item slot"
   )
   InputInventorySlot inputSlot;
   @WrappingComputerMethod(
      wrapper = SpecialComputerMethodWrapper.ComputerIInventorySlotWrapper.class,
      methodNames = {"getOutputItem"},
      docPlaceholder = "output slot"
   )
   OutputInventorySlot outputSlot;
   @WrappingComputerMethod(
      wrapper = SpecialComputerMethodWrapper.ComputerIInventorySlotWrapper.class,
      methodNames = {"getEnergyItem"},
      docPlaceholder = "energy slot"
   )
   EnergyInventorySlot energySlot;
   private FloatingLong clientEnergyUsed = FloatingLong.ZERO;

   public TileEntityAntiprotonicNucleosynthesizer(BlockPos pos, BlockState state) {
      super(MekanismBlocks.ANTIPROTONIC_NUCLEOSYNTHESIZER, pos, state, TRACKED_ERROR_TYPES, 400);
      this.configComponent = new TileComponentConfig(this, TransmissionType.ITEM, TransmissionType.GAS, TransmissionType.ENERGY);
      this.configComponent.setupItemIOExtraConfig(this.inputSlot, this.outputSlot, this.gasInputSlot, this.energySlot);
      this.configComponent.setupInputConfig(TransmissionType.GAS, this.gasTank);
      this.configComponent.setupInputConfig(TransmissionType.ENERGY, this.energyContainer);
      this.ejectorComponent = new TileComponentEjector(this);
      this.ejectorComponent.setOutputData(this.configComponent, TransmissionType.ITEM);
      this.itemInputHandler = InputHelper.getInputHandler(this.inputSlot, CachedRecipe.OperationTracker.RecipeError.NOT_ENOUGH_INPUT);
      this.gasInputHandler = InputHelper.getInputHandler(this.gasTank, CachedRecipe.OperationTracker.RecipeError.NOT_ENOUGH_SECONDARY_INPUT);
      this.outputHandler = OutputHelper.getOutputHandler(this.outputSlot, CachedRecipe.OperationTracker.RecipeError.NOT_ENOUGH_OUTPUT_SPACE);
   }

   @Override
   protected RecipeCacheLookupMonitor<NucleosynthesizingRecipe> createNewCacheMonitor() {
      return new NucleosynthesizerRecipeCacheLookupMonitor(this);
   }

   @NotNull
   @Override
   public IChemicalTankHolder<Gas, GasStack, IGasTank> getInitialGasTanks(IContentsListener listener, IContentsListener recipeCacheListener) {
      ChemicalTankHelper<Gas, GasStack, IGasTank> builder = ChemicalTankHelper.forSideGasWithConfig(this::getDirection, this::getConfig);
      builder.addTank(
         this.gasTank = (IGasTank)ChemicalTankBuilder.GAS
            .input(10000L, gas -> this.containsRecipeBA(this.inputSlot.getStack(), gas), this::containsRecipeB, recipeCacheListener)
      );
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
      builder.addSlot(this.gasInputSlot = GasInventorySlot.fillOrConvert(this.gasTank, this::m_58904_, listener, 6, 69));
      builder.addSlot(
            this.inputSlot = InputInventorySlot.at(
               item -> this.containsRecipeAB(item, this.gasTank.getStack()), this::containsRecipeA, recipeCacheListener, 26, 40
            )
         )
         .tracksWarnings(
            slot -> slot.warning(
               WarningTracker.WarningType.NO_MATCHING_RECIPE, this.getWarningCheck(CachedRecipe.OperationTracker.RecipeError.NOT_ENOUGH_INPUT)
            )
         );
      builder.addSlot(this.outputSlot = OutputInventorySlot.at(listener, 152, 40))
         .tracksWarnings(
            slot -> slot.warning(
               WarningTracker.WarningType.NO_SPACE_IN_OUTPUT, this.getWarningCheck(CachedRecipe.OperationTracker.RecipeError.NOT_ENOUGH_OUTPUT_SPACE)
            )
         );
      builder.addSlot(this.energySlot = EnergyInventorySlot.fillOrConvert(this.energyContainer, this::m_58904_, listener, 173, 69));
      this.gasInputSlot.setSlotOverlay(SlotOverlay.MINUS);
      return builder.build();
   }

   public double getProcessRate() {
      return this.clientEnergyUsed.divide(this.energyContainer.getEnergyPerTick()).doubleValue();
   }

   @NotNull
   @ComputerMethod(
      nameOverride = "getEnergyUsage",
      methodDescription = "Get the energy used in the last tick by the machine"
   )
   public FloatingLong getEnergyUsed() {
      return this.clientEnergyUsed;
   }

   @Override
   public void onCachedRecipeChanged(@Nullable CachedRecipe<NucleosynthesizingRecipe> cachedRecipe, int cacheIndex) {
      super.onCachedRecipeChanged(cachedRecipe, cacheIndex);
      this.ticksRequired = cachedRecipe == null ? 400 : cachedRecipe.getRecipe().getDuration();
   }

   @Override
   protected void onUpdateServer() {
      super.onUpdateServer();
      this.energySlot.fillContainerOrConvert();
      this.gasInputSlot.fillTankOrConvert();
      this.clientEnergyUsed = this.recipeCacheLookupMonitor.updateAndProcess(this.energyContainer);
   }

   @Nullable
   public NucleosynthesizingRecipe getRecipe(int cacheIndex) {
      return this.findFirstRecipe(this.itemInputHandler, this.gasInputHandler);
   }

   @NotNull
   public CachedRecipe<NucleosynthesizingRecipe> createNewCachedRecipe(@NotNull NucleosynthesizingRecipe recipe, int cacheIndex) {
      return TwoInputCachedRecipe.itemChemicalToItem(recipe, this.recheckAllRecipeErrors, this.itemInputHandler, this.gasInputHandler, this.outputHandler)
         .setErrorsChanged(x$0 -> this.onErrorsChanged(x$0))
         .setCanHolderFunction(() -> MekanismUtils.canFunction(this))
         .setActive(this::setActive)
         .setEnergyRequirements(this.energyContainer::getEnergyPerTick, this.energyContainer)
         .setRequiredTicks(this::getTicksRequired)
         .setOnFinish(this::markForSave)
         .setOperatingTicksChanged(x$0 -> this.setOperatingTicks(x$0));
   }

   public MachineEnergyContainer<TileEntityAntiprotonicNucleosynthesizer> getEnergyContainer() {
      return this.energyContainer;
   }

   @NotNull
   @Override
   public IMekanismRecipeTypeProvider<NucleosynthesizingRecipe, InputRecipeCache.ItemChemical<Gas, GasStack, NucleosynthesizingRecipe>> getRecipeType() {
      return MekanismRecipeType.NUCLEOSYNTHESIZING;
   }

   @Override
   public void addContainerTrackers(MekanismContainer container) {
      super.addContainerTrackers(container);
      container.track(SyncableFloatingLong.create(this::getEnergyUsed, value -> this.clientEnergyUsed = value));
   }
}
