package mekanism.common.tile.machine;

import java.util.List;
import mekanism.api.IContentsListener;
import mekanism.api.chemical.ChemicalTankBuilder;
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
import mekanism.api.math.FloatingLong;
import mekanism.api.recipes.ChemicalCrystallizerRecipe;
import mekanism.api.recipes.cache.CachedRecipe;
import mekanism.api.recipes.cache.ChemicalCrystallizerCachedRecipe;
import mekanism.api.recipes.inputs.BoxedChemicalInputHandler;
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
import mekanism.common.inventory.container.slot.SlotOverlay;
import mekanism.common.inventory.slot.EnergyInventorySlot;
import mekanism.common.inventory.slot.OutputInventorySlot;
import mekanism.common.inventory.slot.chemical.MergedChemicalInventorySlot;
import mekanism.common.inventory.warning.WarningTracker;
import mekanism.common.lib.transmitter.TransmissionType;
import mekanism.common.recipe.IMekanismRecipeTypeProvider;
import mekanism.common.recipe.MekanismRecipeType;
import mekanism.common.recipe.lookup.cache.ChemicalCrystallizerInputRecipeCache;
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

public class TileEntityChemicalCrystallizer extends TileEntityProgressMachine<ChemicalCrystallizerRecipe> {
   private static final List<CachedRecipe.OperationTracker.RecipeError> TRACKED_ERROR_TYPES = List.of(
      CachedRecipe.OperationTracker.RecipeError.NOT_ENOUGH_ENERGY,
      CachedRecipe.OperationTracker.RecipeError.NOT_ENOUGH_INPUT,
      CachedRecipe.OperationTracker.RecipeError.NOT_ENOUGH_OUTPUT_SPACE,
      CachedRecipe.OperationTracker.RecipeError.INPUT_DOESNT_PRODUCE_OUTPUT
   );
   private static final long MAX_CHEMICAL = 10000L;
   public MergedChemicalTank inputTank;
   private final IOutputHandler<ItemStack> outputHandler;
   private final BoxedChemicalInputHandler inputHandler;
   private MachineEnergyContainer<TileEntityChemicalCrystallizer> energyContainer;
   @WrappingComputerMethod(
      wrapper = SpecialComputerMethodWrapper.ComputerIInventorySlotWrapper.class,
      methodNames = {"getInputItem"},
      docPlaceholder = "input item slot"
   )
   MergedChemicalInventorySlot<MergedChemicalTank> inputSlot;
   @WrappingComputerMethod(
      wrapper = SpecialComputerMethodWrapper.ComputerIInventorySlotWrapper.class,
      methodNames = {"getOutput"},
      docPlaceholder = "output slot"
   )
   OutputInventorySlot outputSlot;
   @WrappingComputerMethod(
      wrapper = SpecialComputerMethodWrapper.ComputerIInventorySlotWrapper.class,
      methodNames = {"getEnergyItem"},
      docPlaceholder = "energy slot"
   )
   EnergyInventorySlot energySlot;

   public TileEntityChemicalCrystallizer(BlockPos pos, BlockState state) {
      super(MekanismBlocks.CHEMICAL_CRYSTALLIZER, pos, state, TRACKED_ERROR_TYPES, 200);
      this.configComponent = new TileComponentConfig(
         this,
         TransmissionType.ITEM,
         TransmissionType.ENERGY,
         TransmissionType.GAS,
         TransmissionType.INFUSION,
         TransmissionType.PIGMENT,
         TransmissionType.SLURRY
      );
      this.configComponent.setupItemIOConfig(this.inputSlot, this.outputSlot, this.energySlot);
      this.configComponent.setupInputConfig(TransmissionType.ENERGY, this.energyContainer);
      this.configComponent.setupInputConfig(TransmissionType.GAS, this.inputTank.getGasTank());
      this.configComponent.setupInputConfig(TransmissionType.INFUSION, this.inputTank.getInfusionTank());
      this.configComponent.setupInputConfig(TransmissionType.PIGMENT, this.inputTank.getPigmentTank());
      this.configComponent.setupInputConfig(TransmissionType.SLURRY, this.inputTank.getSlurryTank());
      this.ejectorComponent = new TileComponentEjector(this);
      this.ejectorComponent.setOutputData(this.configComponent, TransmissionType.ITEM);
      this.inputHandler = new BoxedChemicalInputHandler(this.inputTank, CachedRecipe.OperationTracker.RecipeError.NOT_ENOUGH_INPUT);
      this.outputHandler = OutputHelper.getOutputHandler(this.outputSlot, CachedRecipe.OperationTracker.RecipeError.NOT_ENOUGH_OUTPUT_SPACE);
   }

   @Override
   protected void presetVariables() {
      super.presetVariables();
      this.inputTank = MergedChemicalTank.create(
         (IGasTank)ChemicalTankBuilder.GAS
            .input(10000L, gas -> this.getRecipeType().getInputCache().containsInput(this.f_58857_, gas), this.getRecipeCacheSaveOnlyListener()),
         (IInfusionTank)ChemicalTankBuilder.INFUSION
            .input(10000L, infuseType -> this.getRecipeType().getInputCache().containsInput(this.f_58857_, infuseType), this.getRecipeCacheSaveOnlyListener()),
         (IPigmentTank)ChemicalTankBuilder.PIGMENT
            .input(10000L, pigment -> this.getRecipeType().getInputCache().containsInput(this.f_58857_, pigment), this.getRecipeCacheSaveOnlyListener()),
         (ISlurryTank)ChemicalTankBuilder.SLURRY
            .input(10000L, slurry -> this.getRecipeType().getInputCache().containsInput(this.f_58857_, slurry), this.getRecipeCacheSaveOnlyListener())
      );
   }

   @NotNull
   @Override
   public IChemicalTankHolder<Gas, GasStack, IGasTank> getInitialGasTanks(IContentsListener listener, IContentsListener recipeCacheListener) {
      ChemicalTankHelper<Gas, GasStack, IGasTank> builder = ChemicalTankHelper.forSideGasWithConfig(this::getDirection, this::getConfig);
      builder.addTank(this.inputTank.getGasTank());
      return builder.build();
   }

   @NotNull
   @Override
   public IChemicalTankHolder<InfuseType, InfusionStack, IInfusionTank> getInitialInfusionTanks(
      IContentsListener listener, IContentsListener recipeCacheListener
   ) {
      ChemicalTankHelper<InfuseType, InfusionStack, IInfusionTank> builder = ChemicalTankHelper.forSideInfusionWithConfig(this::getDirection, this::getConfig);
      builder.addTank(this.inputTank.getInfusionTank());
      return builder.build();
   }

   @NotNull
   @Override
   public IChemicalTankHolder<Pigment, PigmentStack, IPigmentTank> getInitialPigmentTanks(IContentsListener listener, IContentsListener recipeCacheListener) {
      ChemicalTankHelper<Pigment, PigmentStack, IPigmentTank> builder = ChemicalTankHelper.forSidePigmentWithConfig(this::getDirection, this::getConfig);
      builder.addTank(this.inputTank.getPigmentTank());
      return builder.build();
   }

   @NotNull
   @Override
   public IChemicalTankHolder<Slurry, SlurryStack, ISlurryTank> getInitialSlurryTanks(IContentsListener listener, IContentsListener recipeCacheListener) {
      ChemicalTankHelper<Slurry, SlurryStack, ISlurryTank> builder = ChemicalTankHelper.forSideSlurryWithConfig(this::getDirection, this::getConfig);
      builder.addTank(this.inputTank.getSlurryTank());
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
      builder.addSlot(this.inputSlot = MergedChemicalInventorySlot.fill(this.inputTank, listener, 8, 65));
      builder.addSlot(this.outputSlot = OutputInventorySlot.at(listener, 129, 57))
         .tracksWarnings(
            slot -> slot.warning(
               WarningTracker.WarningType.NO_SPACE_IN_OUTPUT, this.getWarningCheck(CachedRecipe.OperationTracker.RecipeError.NOT_ENOUGH_OUTPUT_SPACE)
            )
         );
      builder.addSlot(this.energySlot = EnergyInventorySlot.fillOrConvert(this.energyContainer, this::m_58904_, listener, 152, 5));
      this.inputSlot.setSlotOverlay(SlotOverlay.PLUS);
      return builder.build();
   }

   @Override
   protected void onUpdateServer() {
      super.onUpdateServer();
      this.energySlot.fillContainerOrConvert();
      this.inputSlot.fillChemicalTanks();
      this.recipeCacheLookupMonitor.updateAndProcess();
   }

   @NotNull
   @Override
   public IMekanismRecipeTypeProvider<ChemicalCrystallizerRecipe, ChemicalCrystallizerInputRecipeCache> getRecipeType() {
      return MekanismRecipeType.CRYSTALLIZING;
   }

   @Nullable
   public ChemicalCrystallizerRecipe getRecipe(int cacheIndex) {
      return this.getRecipeType().getInputCache().findFirstRecipe(this.f_58857_, this.inputHandler.getInput());
   }

   @NotNull
   public CachedRecipe<ChemicalCrystallizerRecipe> createNewCachedRecipe(@NotNull ChemicalCrystallizerRecipe recipe, int cacheIndex) {
      return new ChemicalCrystallizerCachedRecipe(recipe, this.recheckAllRecipeErrors, this.inputHandler, this.outputHandler)
         .setErrorsChanged(x$0 -> this.onErrorsChanged(x$0))
         .setCanHolderFunction(() -> MekanismUtils.canFunction(this))
         .setActive(this::setActive)
         .setEnergyRequirements(this.energyContainer::getEnergyPerTick, this.energyContainer)
         .setRequiredTicks(this::getTicksRequired)
         .setOnFinish(this::markForSave)
         .setOperatingTicksChanged(x$0 -> this.setOperatingTicks(x$0));
   }

   public MachineEnergyContainer<TileEntityChemicalCrystallizer> getEnergyContainer() {
      return this.energyContainer;
   }

   @ComputerMethod(
      methodDescription = "Get the energy used in the last tick by the machine"
   )
   FloatingLong getEnergyUsage() {
      return this.getActive() ? this.energyContainer.getEnergyPerTick() : FloatingLong.ZERO;
   }

   @WrappingComputerMethod(
      wrapper = SpecialComputerMethodWrapper.ComputerChemicalTankWrapper.class,
      methodNames = {"getInput", "getInputCapacity", "getInputNeeded", "getInputFilledPercentage"},
      docPlaceholder = "input tank"
   )
   IChemicalTank<?, ?> getInputTank() {
      MergedChemicalTank.Current current = this.inputTank.getCurrent();
      return this.inputTank.getTankFromCurrent(current == MergedChemicalTank.Current.EMPTY ? MergedChemicalTank.Current.GAS : current);
   }
}
