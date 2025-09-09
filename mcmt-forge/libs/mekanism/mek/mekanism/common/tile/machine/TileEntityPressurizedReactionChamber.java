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
import mekanism.api.recipes.PressurizedReactionRecipe;
import mekanism.api.recipes.cache.CachedRecipe;
import mekanism.api.recipes.cache.PressurizedReactionCachedRecipe;
import mekanism.api.recipes.inputs.IInputHandler;
import mekanism.api.recipes.inputs.InputHelper;
import mekanism.api.recipes.outputs.IOutputHandler;
import mekanism.api.recipes.outputs.OutputHelper;
import mekanism.common.capabilities.energy.PRCEnergyContainer;
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
import mekanism.common.inventory.slot.EnergyInventorySlot;
import mekanism.common.inventory.slot.InputInventorySlot;
import mekanism.common.inventory.slot.OutputInventorySlot;
import mekanism.common.inventory.warning.WarningTracker;
import mekanism.common.lib.transmitter.TransmissionType;
import mekanism.common.recipe.IMekanismRecipeTypeProvider;
import mekanism.common.recipe.MekanismRecipeType;
import mekanism.common.recipe.lookup.ITripleRecipeLookupHandler;
import mekanism.common.recipe.lookup.cache.InputRecipeCache;
import mekanism.common.registries.MekanismBlocks;
import mekanism.common.tile.component.TileComponentConfig;
import mekanism.common.tile.component.TileComponentEjector;
import mekanism.common.tile.prefab.TileEntityProgressMachine;
import mekanism.common.util.MekanismUtils;
import net.minecraft.core.BlockPos;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraftforge.fluids.FluidStack;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class TileEntityPressurizedReactionChamber
   extends TileEntityProgressMachine<PressurizedReactionRecipe>
   implements ITripleRecipeLookupHandler.ItemFluidChemicalRecipeLookupHandler<Gas, GasStack, PressurizedReactionRecipe> {
   public static final CachedRecipe.OperationTracker.RecipeError NOT_ENOUGH_ITEM_INPUT_ERROR = CachedRecipe.OperationTracker.RecipeError.create();
   public static final CachedRecipe.OperationTracker.RecipeError NOT_ENOUGH_FLUID_INPUT_ERROR = CachedRecipe.OperationTracker.RecipeError.create();
   public static final CachedRecipe.OperationTracker.RecipeError NOT_ENOUGH_GAS_INPUT_ERROR = CachedRecipe.OperationTracker.RecipeError.create();
   public static final CachedRecipe.OperationTracker.RecipeError NOT_ENOUGH_SPACE_ITEM_OUTPUT_ERROR = CachedRecipe.OperationTracker.RecipeError.create();
   public static final CachedRecipe.OperationTracker.RecipeError NOT_ENOUGH_SPACE_GAS_OUTPUT_ERROR = CachedRecipe.OperationTracker.RecipeError.create();
   private static final List<CachedRecipe.OperationTracker.RecipeError> TRACKED_ERROR_TYPES = List.of(
      CachedRecipe.OperationTracker.RecipeError.NOT_ENOUGH_ENERGY,
      NOT_ENOUGH_ITEM_INPUT_ERROR,
      NOT_ENOUGH_FLUID_INPUT_ERROR,
      NOT_ENOUGH_GAS_INPUT_ERROR,
      NOT_ENOUGH_SPACE_ITEM_OUTPUT_ERROR,
      NOT_ENOUGH_SPACE_GAS_OUTPUT_ERROR,
      CachedRecipe.OperationTracker.RecipeError.INPUT_DOESNT_PRODUCE_OUTPUT
   );
   private static final int BASE_DURATION = 100;
   private static final long MAX_GAS = 10000L;
   @WrappingComputerMethod(
      wrapper = SpecialComputerMethodWrapper.ComputerFluidTankWrapper.class,
      methodNames = {"getInputFluid", "getInputFluidCapacity", "getInputFluidNeeded", "getInputFluidFilledPercentage"},
      docPlaceholder = "fluid input"
   )
   public BasicFluidTank inputFluidTank;
   @WrappingComputerMethod(
      wrapper = SpecialComputerMethodWrapper.ComputerChemicalTankWrapper.class,
      methodNames = {"getInputGas", "getInputGasCapacity", "getInputGasNeeded", "getInputGasFilledPercentage"},
      docPlaceholder = "gas input"
   )
   public IGasTank inputGasTank;
   @WrappingComputerMethod(
      wrapper = SpecialComputerMethodWrapper.ComputerChemicalTankWrapper.class,
      methodNames = {"getOutputGas", "getOutputGasCapacity", "getOutputGasNeeded", "getOutputGasFilledPercentage"},
      docPlaceholder = "gas output"
   )
   public IGasTank outputGasTank;
   private FloatingLong recipeEnergyRequired = FloatingLong.ZERO;
   private final IOutputHandler<PressurizedReactionRecipe.PressurizedReactionRecipeOutput> outputHandler;
   private final IInputHandler<ItemStack> itemInputHandler;
   private final IInputHandler<FluidStack> fluidInputHandler;
   private final IInputHandler<GasStack> gasInputHandler;
   private PRCEnergyContainer energyContainer;
   @WrappingComputerMethod(
      wrapper = SpecialComputerMethodWrapper.ComputerIInventorySlotWrapper.class,
      methodNames = {"getInputItem"},
      docPlaceholder = "item input slot"
   )
   InputInventorySlot inputSlot;
   @WrappingComputerMethod(
      wrapper = SpecialComputerMethodWrapper.ComputerIInventorySlotWrapper.class,
      methodNames = {"getOutputItem"},
      docPlaceholder = "item output slot"
   )
   OutputInventorySlot outputSlot;
   @WrappingComputerMethod(
      wrapper = SpecialComputerMethodWrapper.ComputerIInventorySlotWrapper.class,
      methodNames = {"getEnergyItem"},
      docPlaceholder = "energy slot"
   )
   EnergyInventorySlot energySlot;

   public TileEntityPressurizedReactionChamber(BlockPos pos, BlockState state) {
      super(MekanismBlocks.PRESSURIZED_REACTION_CHAMBER, pos, state, TRACKED_ERROR_TYPES, 100);
      this.configComponent = new TileComponentConfig(this, TransmissionType.ITEM, TransmissionType.ENERGY, TransmissionType.FLUID, TransmissionType.GAS);
      this.configComponent.setupItemIOConfig(this.inputSlot, this.outputSlot, this.energySlot);
      this.configComponent.setupInputConfig(TransmissionType.FLUID, this.inputFluidTank);
      this.configComponent.setupIOConfig(TransmissionType.GAS, this.inputGasTank, this.outputGasTank, RelativeSide.RIGHT, false, true).setEjecting(true);
      this.configComponent.setupInputConfig(TransmissionType.ENERGY, this.energyContainer);
      this.ejectorComponent = new TileComponentEjector(this);
      this.ejectorComponent.setOutputData(this.configComponent, TransmissionType.ITEM, TransmissionType.GAS).setCanTankEject(tank -> tank != this.inputGasTank);
      this.itemInputHandler = InputHelper.getInputHandler(this.inputSlot, NOT_ENOUGH_ITEM_INPUT_ERROR);
      this.fluidInputHandler = InputHelper.getInputHandler(this.inputFluidTank, NOT_ENOUGH_FLUID_INPUT_ERROR);
      this.gasInputHandler = InputHelper.getInputHandler(this.inputGasTank, NOT_ENOUGH_GAS_INPUT_ERROR);
      this.outputHandler = OutputHelper.getOutputHandler(
         this.outputSlot, NOT_ENOUGH_SPACE_ITEM_OUTPUT_ERROR, this.outputGasTank, NOT_ENOUGH_SPACE_GAS_OUTPUT_ERROR
      );
   }

   @NotNull
   @Override
   public IChemicalTankHolder<Gas, GasStack, IGasTank> getInitialGasTanks(IContentsListener listener, IContentsListener recipeCacheListener) {
      ChemicalTankHelper<Gas, GasStack, IGasTank> builder = ChemicalTankHelper.forSideGasWithConfig(this::getDirection, this::getConfig);
      builder.addTank(
         this.inputGasTank = (IGasTank)ChemicalTankBuilder.GAS
            .create(
               10000L,
               ChemicalTankHelper.radioactiveInputTankPredicate(() -> this.outputGasTank),
               (gas, automationType) -> this.containsRecipeCAB(this.inputSlot.getStack(), this.inputFluidTank.getFluid(), gas),
               this::containsRecipeC,
               ChemicalAttributeValidator.ALWAYS_ALLOW,
               recipeCacheListener
            )
      );
      builder.addTank(this.outputGasTank = (IGasTank)ChemicalTankBuilder.GAS.output(10000L, listener));
      return builder.build();
   }

   @NotNull
   @Override
   protected IFluidTankHolder getInitialFluidTanks(IContentsListener listener, IContentsListener recipeCacheListener) {
      FluidTankHelper builder = FluidTankHelper.forSideWithConfig(this::getDirection, this::getConfig);
      builder.addTank(
         this.inputFluidTank = BasicFluidTank.input(
            10000, fluid -> this.containsRecipeBAC(this.inputSlot.getStack(), fluid, this.inputGasTank.getStack()), this::containsRecipeB, recipeCacheListener
         )
      );
      return builder.build();
   }

   @NotNull
   @Override
   protected IEnergyContainerHolder getInitialEnergyContainers(IContentsListener listener, IContentsListener recipeCacheListener) {
      EnergyContainerHelper builder = EnergyContainerHelper.forSideWithConfig(this::getDirection, this::getConfig);
      builder.addContainer(this.energyContainer = PRCEnergyContainer.input(this, listener));
      return builder.build();
   }

   @NotNull
   @Override
   protected IInventorySlotHolder getInitialInventory(IContentsListener listener, IContentsListener recipeCacheListener) {
      InventorySlotHelper builder = InventorySlotHelper.forSideWithConfig(this::getDirection, this::getConfig);
      builder.addSlot(
            this.inputSlot = InputInventorySlot.at(
               item -> this.containsRecipeABC(item, this.inputFluidTank.getFluid(), this.inputGasTank.getStack()),
               this::containsRecipeA,
               recipeCacheListener,
               54,
               35
            )
         )
         .tracksWarnings(slot -> slot.warning(WarningTracker.WarningType.NO_MATCHING_RECIPE, this.getWarningCheck(NOT_ENOUGH_ITEM_INPUT_ERROR)));
      builder.addSlot(this.outputSlot = OutputInventorySlot.at(listener, 116, 35))
         .tracksWarnings(slot -> slot.warning(WarningTracker.WarningType.NO_SPACE_IN_OUTPUT, this.getWarningCheck(NOT_ENOUGH_SPACE_ITEM_OUTPUT_ERROR)));
      builder.addSlot(this.energySlot = EnergyInventorySlot.fillOrConvert(this.energyContainer, this::m_58904_, listener, 141, 17));
      return builder.build();
   }

   @Override
   public void onCachedRecipeChanged(@Nullable CachedRecipe<PressurizedReactionRecipe> cachedRecipe, int cacheIndex) {
      super.onCachedRecipeChanged(cachedRecipe, cacheIndex);
      int recipeDuration;
      if (cachedRecipe == null) {
         recipeDuration = 100;
         this.recipeEnergyRequired = FloatingLong.ZERO;
      } else {
         PressurizedReactionRecipe recipe = cachedRecipe.getRecipe();
         recipeDuration = recipe.getDuration();
         this.recipeEnergyRequired = recipe.getEnergyRequired();
      }

      boolean update = this.baseTicksRequired != recipeDuration;
      this.baseTicksRequired = recipeDuration;
      if (update) {
         this.recalculateUpgrades(Upgrade.SPEED);
      }

      this.energyContainer.updateEnergyPerTick();
   }

   @Override
   protected void onUpdateServer() {
      super.onUpdateServer();
      this.energySlot.fillContainerOrConvert();
      this.recipeCacheLookupMonitor.updateAndProcess();
   }

   public FloatingLong getRecipeEnergyRequired() {
      return this.recipeEnergyRequired;
   }

   @NotNull
   @Override
   public IMekanismRecipeTypeProvider<PressurizedReactionRecipe, InputRecipeCache.ItemFluidChemical<Gas, GasStack, PressurizedReactionRecipe>> getRecipeType() {
      return MekanismRecipeType.REACTION;
   }

   @Nullable
   public PressurizedReactionRecipe getRecipe(int cacheIndex) {
      return this.findFirstRecipe(this.itemInputHandler, this.fluidInputHandler, this.gasInputHandler);
   }

   @NotNull
   public CachedRecipe<PressurizedReactionRecipe> createNewCachedRecipe(@NotNull PressurizedReactionRecipe recipe, int cacheIndex) {
      return new PressurizedReactionCachedRecipe(
            recipe, this.recheckAllRecipeErrors, this.itemInputHandler, this.fluidInputHandler, this.gasInputHandler, this.outputHandler
         )
         .setErrorsChanged(x$0 -> this.onErrorsChanged(x$0))
         .setCanHolderFunction(() -> MekanismUtils.canFunction(this))
         .setActive(this::setActive)
         .setEnergyRequirements(this.energyContainer::getEnergyPerTick, this.energyContainer)
         .setRequiredTicks(this::getTicksRequired)
         .setOnFinish(this::markForSave)
         .setOperatingTicksChanged(x$0 -> this.setOperatingTicks(x$0));
   }

   public PRCEnergyContainer getEnergyContainer() {
      return this.energyContainer;
   }

   @ComputerMethod(
      methodDescription = "Get the energy used in the last tick by the machine"
   )
   FloatingLong getEnergyUsage() {
      return this.getActive() ? this.energyContainer.getEnergyPerTick() : FloatingLong.ZERO;
   }
}
