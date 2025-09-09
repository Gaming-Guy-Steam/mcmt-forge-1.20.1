package mekanism.common.tile.machine;

import java.util.List;
import mekanism.api.IContentsListener;
import mekanism.api.RelativeSide;
import mekanism.api.Upgrade;
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
import mekanism.api.recipes.ChemicalDissolutionRecipe;
import mekanism.api.recipes.cache.CachedRecipe;
import mekanism.api.recipes.cache.ChemicalDissolutionCachedRecipe;
import mekanism.api.recipes.inputs.IInputHandler;
import mekanism.api.recipes.inputs.ILongInputHandler;
import mekanism.api.recipes.inputs.InputHelper;
import mekanism.api.recipes.outputs.BoxedChemicalOutputHandler;
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
import mekanism.common.inventory.slot.InputInventorySlot;
import mekanism.common.inventory.slot.chemical.GasInventorySlot;
import mekanism.common.inventory.slot.chemical.MergedChemicalInventorySlot;
import mekanism.common.inventory.warning.WarningTracker;
import mekanism.common.lib.transmitter.TransmissionType;
import mekanism.common.recipe.IMekanismRecipeTypeProvider;
import mekanism.common.recipe.MekanismRecipeType;
import mekanism.common.recipe.lookup.IDoubleRecipeLookupHandler;
import mekanism.common.recipe.lookup.cache.InputRecipeCache;
import mekanism.common.registries.MekanismBlocks;
import mekanism.common.tile.base.SubstanceType;
import mekanism.common.tile.component.TileComponentConfig;
import mekanism.common.tile.component.TileComponentEjector;
import mekanism.common.tile.prefab.TileEntityProgressMachine;
import mekanism.common.util.MekanismUtils;
import mekanism.common.util.StatUtils;
import net.minecraft.core.BlockPos;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.state.BlockState;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class TileEntityChemicalDissolutionChamber
   extends TileEntityProgressMachine<ChemicalDissolutionRecipe>
   implements IDoubleRecipeLookupHandler.ItemChemicalRecipeLookupHandler<Gas, GasStack, ChemicalDissolutionRecipe> {
   private static final List<CachedRecipe.OperationTracker.RecipeError> TRACKED_ERROR_TYPES = List.of(
      CachedRecipe.OperationTracker.RecipeError.NOT_ENOUGH_ENERGY,
      CachedRecipe.OperationTracker.RecipeError.NOT_ENOUGH_ENERGY_REDUCED_RATE,
      CachedRecipe.OperationTracker.RecipeError.NOT_ENOUGH_INPUT,
      CachedRecipe.OperationTracker.RecipeError.NOT_ENOUGH_SECONDARY_INPUT,
      CachedRecipe.OperationTracker.RecipeError.NOT_ENOUGH_OUTPUT_SPACE,
      CachedRecipe.OperationTracker.RecipeError.INPUT_DOESNT_PRODUCE_OUTPUT
   );
   private static final long MAX_CHEMICAL = 10000L;
   public static final int BASE_TICKS_REQUIRED = 100;
   @WrappingComputerMethod(
      wrapper = SpecialComputerMethodWrapper.ComputerChemicalTankWrapper.class,
      methodNames = {"getGasInput", "getGasInputCapacity", "getGasInputNeeded", "getGasInputFilledPercentage"},
      docPlaceholder = "gas input tank"
   )
   public IGasTank injectTank;
   public MergedChemicalTank outputTank;
   public double injectUsage = 1.0;
   private final BoxedChemicalOutputHandler outputHandler;
   private final IInputHandler<ItemStack> itemInputHandler;
   private final ILongInputHandler<GasStack> gasInputHandler;
   private MachineEnergyContainer<TileEntityChemicalDissolutionChamber> energyContainer;
   @WrappingComputerMethod(
      wrapper = SpecialComputerMethodWrapper.ComputerIInventorySlotWrapper.class,
      methodNames = {"getInputGasItem"},
      docPlaceholder = "gas input item slot"
   )
   GasInventorySlot gasInputSlot;
   @WrappingComputerMethod(
      wrapper = SpecialComputerMethodWrapper.ComputerIInventorySlotWrapper.class,
      methodNames = {"getInputItem"},
      docPlaceholder = "input slot"
   )
   InputInventorySlot inputSlot;
   @WrappingComputerMethod(
      wrapper = SpecialComputerMethodWrapper.ComputerIInventorySlotWrapper.class,
      methodNames = {"getOutputItem"},
      docPlaceholder = "output slot"
   )
   MergedChemicalInventorySlot<MergedChemicalTank> outputSlot;
   @WrappingComputerMethod(
      wrapper = SpecialComputerMethodWrapper.ComputerIInventorySlotWrapper.class,
      methodNames = {"getEnergyItem"},
      docPlaceholder = "energy slot"
   )
   EnergyInventorySlot energySlot;

   public TileEntityChemicalDissolutionChamber(BlockPos pos, BlockState state) {
      super(MekanismBlocks.CHEMICAL_DISSOLUTION_CHAMBER, pos, state, TRACKED_ERROR_TYPES, 100);
      this.configComponent = new TileComponentConfig(
         this,
         TransmissionType.ITEM,
         TransmissionType.GAS,
         TransmissionType.INFUSION,
         TransmissionType.PIGMENT,
         TransmissionType.SLURRY,
         TransmissionType.ENERGY
      );
      this.configComponent.setupItemIOExtraConfig(this.inputSlot, this.outputSlot, this.gasInputSlot, this.energySlot);
      this.configComponent.setupIOConfig(TransmissionType.GAS, this.injectTank, this.outputTank.getGasTank(), RelativeSide.RIGHT).setEjecting(true);
      this.configComponent.setupOutputConfig(TransmissionType.INFUSION, this.outputTank.getInfusionTank(), RelativeSide.RIGHT);
      this.configComponent.setupOutputConfig(TransmissionType.PIGMENT, this.outputTank.getPigmentTank(), RelativeSide.RIGHT);
      this.configComponent.setupOutputConfig(TransmissionType.SLURRY, this.outputTank.getSlurryTank(), RelativeSide.RIGHT);
      this.configComponent.setupInputConfig(TransmissionType.ENERGY, this.energyContainer);
      this.ejectorComponent = new TileComponentEjector(this);
      this.ejectorComponent
         .setOutputData(
            this.configComponent, TransmissionType.ITEM, TransmissionType.GAS, TransmissionType.INFUSION, TransmissionType.PIGMENT, TransmissionType.SLURRY
         )
         .setCanTankEject(tank -> tank != this.injectTank);
      this.itemInputHandler = InputHelper.getInputHandler(this.inputSlot, CachedRecipe.OperationTracker.RecipeError.NOT_ENOUGH_INPUT);
      this.gasInputHandler = InputHelper.getConstantInputHandler(this.injectTank);
      this.outputHandler = new BoxedChemicalOutputHandler(this.outputTank, CachedRecipe.OperationTracker.RecipeError.NOT_ENOUGH_OUTPUT_SPACE);
   }

   @Override
   protected void presetVariables() {
      super.presetVariables();
      IContentsListener saveOnlyListener = this::markForSave;
      this.outputTank = MergedChemicalTank.create(
         (IGasTank)ChemicalTankBuilder.GAS.output(10000L, this.getListener(SubstanceType.GAS, saveOnlyListener)),
         (IInfusionTank)ChemicalTankBuilder.INFUSION.output(10000L, this.getListener(SubstanceType.INFUSION, saveOnlyListener)),
         (IPigmentTank)ChemicalTankBuilder.PIGMENT.output(10000L, this.getListener(SubstanceType.PIGMENT, saveOnlyListener)),
         (ISlurryTank)ChemicalTankBuilder.SLURRY.output(10000L, this.getListener(SubstanceType.SLURRY, saveOnlyListener))
      );
   }

   @NotNull
   @Override
   public IChemicalTankHolder<Gas, GasStack, IGasTank> getInitialGasTanks(IContentsListener listener, IContentsListener recipeCacheListener) {
      ChemicalTankHelper<Gas, GasStack, IGasTank> builder = ChemicalTankHelper.forSideGasWithConfig(this::getDirection, this::getConfig);
      builder.addTank(
         this.injectTank = (IGasTank)ChemicalTankBuilder.GAS
            .input(10000L, gas -> this.containsRecipeBA(this.inputSlot.getStack(), gas), this::containsRecipeB, recipeCacheListener)
      );
      builder.addTank(this.outputTank.getGasTank());
      return builder.build();
   }

   @NotNull
   @Override
   public IChemicalTankHolder<InfuseType, InfusionStack, IInfusionTank> getInitialInfusionTanks(
      IContentsListener listener, IContentsListener recipeCacheListener
   ) {
      ChemicalTankHelper<InfuseType, InfusionStack, IInfusionTank> builder = ChemicalTankHelper.forSideInfusionWithConfig(this::getDirection, this::getConfig);
      builder.addTank(this.outputTank.getInfusionTank());
      return builder.build();
   }

   @NotNull
   @Override
   public IChemicalTankHolder<Pigment, PigmentStack, IPigmentTank> getInitialPigmentTanks(IContentsListener listener, IContentsListener recipeCacheListener) {
      ChemicalTankHelper<Pigment, PigmentStack, IPigmentTank> builder = ChemicalTankHelper.forSidePigmentWithConfig(this::getDirection, this::getConfig);
      builder.addTank(this.outputTank.getPigmentTank());
      return builder.build();
   }

   @NotNull
   @Override
   public IChemicalTankHolder<Slurry, SlurryStack, ISlurryTank> getInitialSlurryTanks(IContentsListener listener, IContentsListener recipeCacheListener) {
      ChemicalTankHelper<Slurry, SlurryStack, ISlurryTank> builder = ChemicalTankHelper.forSideSlurryWithConfig(this::getDirection, this::getConfig);
      builder.addTank(this.outputTank.getSlurryTank());
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
      builder.addSlot(this.gasInputSlot = GasInventorySlot.fillOrConvert(this.injectTank, this::m_58904_, listener, 8, 65));
      builder.addSlot(
            this.inputSlot = InputInventorySlot.at(
               item -> this.containsRecipeAB(item, this.injectTank.getStack()), this::containsRecipeA, recipeCacheListener, 28, 36
            )
         )
         .tracksWarnings(
            slot -> slot.warning(
               WarningTracker.WarningType.NO_MATCHING_RECIPE, this.getWarningCheck(CachedRecipe.OperationTracker.RecipeError.NOT_ENOUGH_INPUT)
            )
         );
      builder.addSlot(this.outputSlot = MergedChemicalInventorySlot.drain(this.outputTank, listener, 152, 55));
      builder.addSlot(this.energySlot = EnergyInventorySlot.fillOrConvert(this.energyContainer, this::m_58904_, listener, 152, 14));
      this.gasInputSlot.setSlotOverlay(SlotOverlay.MINUS);
      this.outputSlot.setSlotOverlay(SlotOverlay.PLUS);
      return builder.build();
   }

   @Override
   protected void onUpdateServer() {
      super.onUpdateServer();
      this.energySlot.fillContainerOrConvert();
      this.gasInputSlot.fillTankOrConvert();
      this.outputSlot.drainChemicalTanks();
      this.recipeCacheLookupMonitor.updateAndProcess();
   }

   @NotNull
   @Override
   public IMekanismRecipeTypeProvider<ChemicalDissolutionRecipe, InputRecipeCache.ItemChemical<Gas, GasStack, ChemicalDissolutionRecipe>> getRecipeType() {
      return MekanismRecipeType.DISSOLUTION;
   }

   @Nullable
   public ChemicalDissolutionRecipe getRecipe(int cacheIndex) {
      return this.findFirstRecipe(this.itemInputHandler, this.gasInputHandler);
   }

   @NotNull
   public CachedRecipe<ChemicalDissolutionRecipe> createNewCachedRecipe(@NotNull ChemicalDissolutionRecipe recipe, int cacheIndex) {
      return new ChemicalDissolutionCachedRecipe(
            recipe,
            this.recheckAllRecipeErrors,
            this.itemInputHandler,
            this.gasInputHandler,
            () -> StatUtils.inversePoisson(this.injectUsage),
            this.outputHandler
         )
         .setErrorsChanged(x$0 -> this.onErrorsChanged(x$0))
         .setCanHolderFunction(() -> MekanismUtils.canFunction(this))
         .setActive(this::setActive)
         .setEnergyRequirements(this.energyContainer::getEnergyPerTick, this.energyContainer)
         .setRequiredTicks(this::getTicksRequired)
         .setOnFinish(this::markForSave)
         .setOperatingTicksChanged(x$0 -> this.setOperatingTicks(x$0));
   }

   @Override
   public void recalculateUpgrades(Upgrade upgrade) {
      super.recalculateUpgrades(upgrade);
      if (upgrade == Upgrade.GAS || upgrade == Upgrade.SPEED) {
         this.injectUsage = MekanismUtils.getGasPerTickMeanMultiplier(this);
      }
   }

   public MachineEnergyContainer<TileEntityChemicalDissolutionChamber> getEnergyContainer() {
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
      methodNames = {"getOutput", "getOutputCapacity", "getOutputNeeded", "getOutputFilledPercentage"},
      docPlaceholder = "output tank"
   )
   IChemicalTank<?, ?> getOutputTank() {
      MergedChemicalTank.Current current = this.outputTank.getCurrent();
      return this.outputTank.getTankFromCurrent(current == MergedChemicalTank.Current.EMPTY ? MergedChemicalTank.Current.GAS : current);
   }
}
