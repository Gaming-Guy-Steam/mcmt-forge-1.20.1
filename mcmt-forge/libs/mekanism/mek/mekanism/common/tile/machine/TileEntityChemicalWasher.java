package mekanism.common.tile.machine;

import java.util.Collections;
import java.util.List;
import mekanism.api.IContentsListener;
import mekanism.api.RelativeSide;
import mekanism.api.Upgrade;
import mekanism.api.chemical.ChemicalTankBuilder;
import mekanism.api.chemical.slurry.ISlurryTank;
import mekanism.api.chemical.slurry.Slurry;
import mekanism.api.chemical.slurry.SlurryStack;
import mekanism.api.math.FloatingLong;
import mekanism.api.recipes.FluidSlurryToSlurryRecipe;
import mekanism.api.recipes.cache.CachedRecipe;
import mekanism.api.recipes.cache.TwoInputCachedRecipe;
import mekanism.api.recipes.inputs.IInputHandler;
import mekanism.api.recipes.inputs.InputHelper;
import mekanism.api.recipes.outputs.IOutputHandler;
import mekanism.api.recipes.outputs.OutputHelper;
import mekanism.common.capabilities.energy.MachineEnergyContainer;
import mekanism.common.capabilities.fluid.BasicFluidTank;
import mekanism.common.capabilities.holder.chemical.ChemicalTankHelper;
import mekanism.common.capabilities.holder.chemical.IChemicalTankHolder;
import mekanism.common.capabilities.holder.energy.EnergyContainerHelper;
import mekanism.common.capabilities.holder.energy.IEnergyContainerHolder;
import mekanism.common.capabilities.holder.fluid.FluidTankHelper;
import mekanism.common.capabilities.holder.fluid.IFluidTankHolder;
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
import mekanism.common.inventory.slot.FluidInventorySlot;
import mekanism.common.inventory.slot.OutputInventorySlot;
import mekanism.common.inventory.slot.chemical.SlurryInventorySlot;
import mekanism.common.lib.transmitter.TransmissionType;
import mekanism.common.recipe.IMekanismRecipeTypeProvider;
import mekanism.common.recipe.MekanismRecipeType;
import mekanism.common.recipe.lookup.IDoubleRecipeLookupHandler;
import mekanism.common.recipe.lookup.cache.InputRecipeCache;
import mekanism.common.registries.MekanismBlocks;
import mekanism.common.tile.base.SubstanceType;
import mekanism.common.tile.component.TileComponentConfig;
import mekanism.common.tile.component.TileComponentEjector;
import mekanism.common.tile.prefab.TileEntityRecipeMachine;
import mekanism.common.util.MekanismUtils;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraftforge.fluids.FluidStack;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class TileEntityChemicalWasher
   extends TileEntityRecipeMachine<FluidSlurryToSlurryRecipe>
   implements IDoubleRecipeLookupHandler.FluidChemicalRecipeLookupHandler<Slurry, SlurryStack, FluidSlurryToSlurryRecipe> {
   private static final List<CachedRecipe.OperationTracker.RecipeError> TRACKED_ERROR_TYPES = List.of(
      CachedRecipe.OperationTracker.RecipeError.NOT_ENOUGH_ENERGY,
      CachedRecipe.OperationTracker.RecipeError.NOT_ENOUGH_ENERGY_REDUCED_RATE,
      CachedRecipe.OperationTracker.RecipeError.NOT_ENOUGH_INPUT,
      CachedRecipe.OperationTracker.RecipeError.NOT_ENOUGH_SECONDARY_INPUT,
      CachedRecipe.OperationTracker.RecipeError.NOT_ENOUGH_OUTPUT_SPACE,
      CachedRecipe.OperationTracker.RecipeError.INPUT_DOESNT_PRODUCE_OUTPUT
   );
   private static final long MAX_SLURRY = 10000L;
   private static final int MAX_FLUID = 10000;
   @WrappingComputerMethod(
      wrapper = SpecialComputerMethodWrapper.ComputerFluidTankWrapper.class,
      methodNames = {"getFluid", "getFluidCapacity", "getFluidNeeded", "getFluidFilledPercentage"},
      docPlaceholder = "fluid tank"
   )
   public BasicFluidTank fluidTank;
   @WrappingComputerMethod(
      wrapper = SpecialComputerMethodWrapper.ComputerChemicalTankWrapper.class,
      methodNames = {"getSlurryInput", "getSlurryInputCapacity", "getSlurryInputNeeded", "getSlurryInputFilledPercentage"},
      docPlaceholder = "input slurry tank"
   )
   public ISlurryTank inputTank;
   @WrappingComputerMethod(
      wrapper = SpecialComputerMethodWrapper.ComputerChemicalTankWrapper.class,
      methodNames = {"getSlurryOutput", "getSlurryOutputCapacity", "getSlurryOutputNeeded", "getSlurryOutputFilledPercentage"},
      docPlaceholder = "output slurry tank"
   )
   public ISlurryTank outputTank;
   private FloatingLong clientEnergyUsed = FloatingLong.ZERO;
   private int baselineMaxOperations = 1;
   private final IOutputHandler<SlurryStack> outputHandler;
   private final IInputHandler<FluidStack> fluidInputHandler;
   private final IInputHandler<SlurryStack> slurryInputHandler;
   private MachineEnergyContainer<TileEntityChemicalWasher> energyContainer;
   @WrappingComputerMethod(
      wrapper = SpecialComputerMethodWrapper.ComputerIInventorySlotWrapper.class,
      methodNames = {"getFluidItemInput"},
      docPlaceholder = "fluid item input slot"
   )
   FluidInventorySlot fluidSlot;
   @WrappingComputerMethod(
      wrapper = SpecialComputerMethodWrapper.ComputerIInventorySlotWrapper.class,
      methodNames = {"getFluidItemOutput"},
      docPlaceholder = "fluid item output slot"
   )
   OutputInventorySlot fluidOutputSlot;
   @WrappingComputerMethod(
      wrapper = SpecialComputerMethodWrapper.ComputerIInventorySlotWrapper.class,
      methodNames = {"getOutputItem"},
      docPlaceholder = "slurry item output slot"
   )
   SlurryInventorySlot slurryOutputSlot;
   @WrappingComputerMethod(
      wrapper = SpecialComputerMethodWrapper.ComputerIInventorySlotWrapper.class,
      methodNames = {"getEnergyItem"},
      docPlaceholder = "energy slot"
   )
   EnergyInventorySlot energySlot;

   public TileEntityChemicalWasher(BlockPos pos, BlockState state) {
      super(MekanismBlocks.CHEMICAL_WASHER, pos, state, TRACKED_ERROR_TYPES);
      this.configComponent = new TileComponentConfig(this, TransmissionType.ITEM, TransmissionType.SLURRY, TransmissionType.FLUID, TransmissionType.ENERGY);
      this.configComponent
         .setupItemIOConfig(Collections.singletonList(this.fluidSlot), List.of(this.slurryOutputSlot, this.fluidOutputSlot), this.energySlot, true);
      this.configComponent.setupIOConfig(TransmissionType.SLURRY, this.inputTank, this.outputTank, RelativeSide.RIGHT).setEjecting(true);
      this.configComponent.setupInputConfig(TransmissionType.FLUID, this.fluidTank);
      this.configComponent.setupInputConfig(TransmissionType.ENERGY, this.energyContainer);
      this.ejectorComponent = new TileComponentEjector(this);
      this.ejectorComponent.setOutputData(this.configComponent, TransmissionType.ITEM, TransmissionType.SLURRY).setCanTankEject(tank -> tank != this.inputTank);
      this.slurryInputHandler = InputHelper.getInputHandler(this.inputTank, CachedRecipe.OperationTracker.RecipeError.NOT_ENOUGH_INPUT);
      this.fluidInputHandler = InputHelper.getInputHandler(this.fluidTank, CachedRecipe.OperationTracker.RecipeError.NOT_ENOUGH_SECONDARY_INPUT);
      this.outputHandler = OutputHelper.getOutputHandler(this.outputTank, CachedRecipe.OperationTracker.RecipeError.NOT_ENOUGH_OUTPUT_SPACE);
   }

   @NotNull
   @Override
   public IChemicalTankHolder<Slurry, SlurryStack, ISlurryTank> getInitialSlurryTanks(IContentsListener listener, IContentsListener recipeCacheListener) {
      ChemicalTankHelper<Slurry, SlurryStack, ISlurryTank> builder = ChemicalTankHelper.forSideSlurryWithConfig(this::getDirection, this::getConfig);
      builder.addTank(
         this.inputTank = (ISlurryTank)ChemicalTankBuilder.SLURRY
            .input(10000L, slurry -> this.containsRecipeBA(this.fluidTank.getFluid(), slurry), this::containsRecipeB, recipeCacheListener)
      );
      builder.addTank(this.outputTank = (ISlurryTank)ChemicalTankBuilder.SLURRY.output(10000L, listener));
      return builder.build();
   }

   @NotNull
   @Override
   protected IFluidTankHolder getInitialFluidTanks(IContentsListener listener, IContentsListener recipeCacheListener) {
      FluidTankHelper builder = FluidTankHelper.forSideWithConfig(this::getDirection, this::getConfig);
      builder.addTank(
         this.fluidTank = BasicFluidTank.input(
            10000, fluid -> this.containsRecipeAB(fluid, this.inputTank.getStack()), this::containsRecipeA, recipeCacheListener
         )
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
      builder.addSlot(this.fluidSlot = FluidInventorySlot.fill(this.fluidTank, listener, 180, 71));
      builder.addSlot(this.fluidOutputSlot = OutputInventorySlot.at(listener, 180, 102));
      builder.addSlot(this.slurryOutputSlot = SlurryInventorySlot.drain(this.outputTank, listener, 152, 56));
      builder.addSlot(this.energySlot = EnergyInventorySlot.fillOrConvert(this.energyContainer, this::m_58904_, listener, 152, 14));
      this.slurryOutputSlot.setSlotOverlay(SlotOverlay.MINUS);
      this.fluidSlot.setSlotType(ContainerSlotType.INPUT);
      return builder.build();
   }

   @Override
   protected void onUpdateServer() {
      super.onUpdateServer();
      this.energySlot.fillContainerOrConvert();
      this.fluidSlot.fillTank(this.fluidOutputSlot);
      this.slurryOutputSlot.drainTank();
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
   public IMekanismRecipeTypeProvider<FluidSlurryToSlurryRecipe, InputRecipeCache.FluidChemical<Slurry, SlurryStack, FluidSlurryToSlurryRecipe>> getRecipeType() {
      return MekanismRecipeType.WASHING;
   }

   @Nullable
   public FluidSlurryToSlurryRecipe getRecipe(int cacheIndex) {
      return this.getRecipeType().getInputCache().findFirstRecipe(this.f_58857_, this.fluidInputHandler.getInput(), this.slurryInputHandler.getInput(), false);
   }

   @NotNull
   public CachedRecipe<FluidSlurryToSlurryRecipe> createNewCachedRecipe(@NotNull FluidSlurryToSlurryRecipe recipe, int cacheIndex) {
      return TwoInputCachedRecipe.fluidChemicalToChemical(
            recipe, this.recheckAllRecipeErrors, this.fluidInputHandler, this.slurryInputHandler, this.outputHandler
         )
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

   @Override
   public int getRedstoneLevel() {
      return MekanismUtils.redstoneLevelFromContents(this.inputTank.getStored(), this.inputTank.getCapacity());
   }

   @Override
   protected boolean makesComparatorDirty(@Nullable SubstanceType type) {
      return type == SubstanceType.SLURRY;
   }

   public MachineEnergyContainer<TileEntityChemicalWasher> getEnergyContainer() {
      return this.energyContainer;
   }

   @Override
   public void addContainerTrackers(MekanismContainer container) {
      super.addContainerTrackers(container);
      container.track(SyncableFloatingLong.create(this::getEnergyUsed, value -> this.clientEnergyUsed = value));
   }
}
