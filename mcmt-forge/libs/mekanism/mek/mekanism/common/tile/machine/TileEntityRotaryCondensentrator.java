package mekanism.common.tile.machine;

import java.util.List;
import java.util.function.BooleanSupplier;
import mekanism.api.AutomationType;
import mekanism.api.IContentsListener;
import mekanism.api.RelativeSide;
import mekanism.api.Upgrade;
import mekanism.api.chemical.ChemicalTankBuilder;
import mekanism.api.chemical.attribute.ChemicalAttributeValidator;
import mekanism.api.chemical.gas.Gas;
import mekanism.api.chemical.gas.GasStack;
import mekanism.api.chemical.gas.IGasTank;
import mekanism.api.math.FloatingLong;
import mekanism.api.recipes.RotaryRecipe;
import mekanism.api.recipes.cache.CachedRecipe;
import mekanism.api.recipes.cache.RotaryCachedRecipe;
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
import mekanism.common.integration.computer.ComputerException;
import mekanism.common.integration.computer.SpecialComputerMethodWrapper;
import mekanism.common.integration.computer.annotation.ComputerMethod;
import mekanism.common.integration.computer.annotation.WrappingComputerMethod;
import mekanism.common.inventory.container.MekanismContainer;
import mekanism.common.inventory.container.slot.ContainerSlotType;
import mekanism.common.inventory.container.slot.SlotOverlay;
import mekanism.common.inventory.container.sync.SyncableBoolean;
import mekanism.common.inventory.container.sync.SyncableFloatingLong;
import mekanism.common.inventory.slot.EnergyInventorySlot;
import mekanism.common.inventory.slot.FluidInventorySlot;
import mekanism.common.inventory.slot.OutputInventorySlot;
import mekanism.common.inventory.slot.chemical.GasInventorySlot;
import mekanism.common.lib.transmitter.TransmissionType;
import mekanism.common.recipe.IMekanismRecipeTypeProvider;
import mekanism.common.recipe.MekanismRecipeType;
import mekanism.common.recipe.lookup.cache.RotaryInputRecipeCache;
import mekanism.common.registries.MekanismBlocks;
import mekanism.common.tile.base.SubstanceType;
import mekanism.common.tile.component.TileComponentConfig;
import mekanism.common.tile.component.TileComponentEjector;
import mekanism.common.tile.interfaces.IHasMode;
import mekanism.common.tile.prefab.TileEntityRecipeMachine;
import mekanism.common.util.MekanismUtils;
import mekanism.common.util.NBTUtils;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraftforge.fluids.FluidStack;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class TileEntityRotaryCondensentrator extends TileEntityRecipeMachine<RotaryRecipe> implements IHasMode {
   public static final CachedRecipe.OperationTracker.RecipeError NOT_ENOUGH_FLUID_INPUT_ERROR = CachedRecipe.OperationTracker.RecipeError.create();
   public static final CachedRecipe.OperationTracker.RecipeError NOT_ENOUGH_GAS_INPUT_ERROR = CachedRecipe.OperationTracker.RecipeError.create();
   public static final CachedRecipe.OperationTracker.RecipeError NOT_ENOUGH_SPACE_GAS_OUTPUT_ERROR = CachedRecipe.OperationTracker.RecipeError.create();
   public static final CachedRecipe.OperationTracker.RecipeError NOT_ENOUGH_SPACE_FLUID_OUTPUT_ERROR = CachedRecipe.OperationTracker.RecipeError.create();
   private static final List<CachedRecipe.OperationTracker.RecipeError> TRACKED_ERROR_TYPES = List.of(
      CachedRecipe.OperationTracker.RecipeError.NOT_ENOUGH_ENERGY,
      CachedRecipe.OperationTracker.RecipeError.NOT_ENOUGH_ENERGY_REDUCED_RATE,
      NOT_ENOUGH_FLUID_INPUT_ERROR,
      NOT_ENOUGH_GAS_INPUT_ERROR,
      NOT_ENOUGH_SPACE_GAS_OUTPUT_ERROR,
      NOT_ENOUGH_SPACE_FLUID_OUTPUT_ERROR,
      CachedRecipe.OperationTracker.RecipeError.INPUT_DOESNT_PRODUCE_OUTPUT
   );
   private static final int CAPACITY = 10000;
   @WrappingComputerMethod(
      wrapper = SpecialComputerMethodWrapper.ComputerChemicalTankWrapper.class,
      methodNames = {"getGas", "getGasCapacity", "getGasNeeded", "getGasFilledPercentage"},
      docPlaceholder = "gas tank"
   )
   public IGasTank gasTank;
   @WrappingComputerMethod(
      wrapper = SpecialComputerMethodWrapper.ComputerFluidTankWrapper.class,
      methodNames = {"getFluid", "getFluidCapacity", "getFluidNeeded", "getFluidFilledPercentage"},
      docPlaceholder = "fluid tank"
   )
   public BasicFluidTank fluidTank;
   public boolean mode;
   private final IOutputHandler<GasStack> gasOutputHandler;
   private final IOutputHandler<FluidStack> fluidOutputHandler;
   private final IInputHandler<FluidStack> fluidInputHandler;
   private final IInputHandler<GasStack> gasInputHandler;
   private FloatingLong clientEnergyUsed = FloatingLong.ZERO;
   private int baselineMaxOperations = 1;
   private MachineEnergyContainer<TileEntityRotaryCondensentrator> energyContainer;
   @WrappingComputerMethod(
      wrapper = SpecialComputerMethodWrapper.ComputerIInventorySlotWrapper.class,
      methodNames = {"getGasItemInput"},
      docPlaceholder = "gas item input slot"
   )
   GasInventorySlot gasInputSlot;
   @WrappingComputerMethod(
      wrapper = SpecialComputerMethodWrapper.ComputerIInventorySlotWrapper.class,
      methodNames = {"getGasItemOutput"},
      docPlaceholder = "gas item output slot"
   )
   GasInventorySlot gasOutputSlot;
   @WrappingComputerMethod(
      wrapper = SpecialComputerMethodWrapper.ComputerIInventorySlotWrapper.class,
      methodNames = {"getFluidItemInput"},
      docPlaceholder = "fluid item input slot"
   )
   FluidInventorySlot fluidInputSlot;
   @WrappingComputerMethod(
      wrapper = SpecialComputerMethodWrapper.ComputerIInventorySlotWrapper.class,
      methodNames = {"getFluidItemOutput"},
      docPlaceholder = "fluid item ouput slot"
   )
   OutputInventorySlot fluidOutputSlot;
   @WrappingComputerMethod(
      wrapper = SpecialComputerMethodWrapper.ComputerIInventorySlotWrapper.class,
      methodNames = {"getEnergyItem"},
      docPlaceholder = "energy slot"
   )
   EnergyInventorySlot energySlot;

   public TileEntityRotaryCondensentrator(BlockPos pos, BlockState state) {
      super(MekanismBlocks.ROTARY_CONDENSENTRATOR, pos, state, TRACKED_ERROR_TYPES);
      this.configComponent = new TileComponentConfig(this, TransmissionType.ITEM, TransmissionType.GAS, TransmissionType.FLUID, TransmissionType.ENERGY);
      this.configComponent
         .setupItemIOConfig(List.of(this.gasInputSlot, this.fluidInputSlot), List.of(this.gasOutputSlot, this.fluidOutputSlot), this.energySlot, true);
      this.configComponent.setupIOConfig(TransmissionType.GAS, this.gasTank, RelativeSide.LEFT, true).setEjecting(true);
      this.configComponent.setupIOConfig(TransmissionType.FLUID, this.fluidTank, RelativeSide.RIGHT, true).setEjecting(true);
      this.configComponent.setupInputConfig(TransmissionType.ENERGY, this.energyContainer);
      this.ejectorComponent = new TileComponentEjector(this);
      this.ejectorComponent
         .setOutputData(this.configComponent, TransmissionType.ITEM, TransmissionType.GAS, TransmissionType.FLUID)
         .setCanEject(transmissionType -> {
            if (transmissionType == TransmissionType.GAS) {
               return this.mode;
            } else {
               return transmissionType == TransmissionType.FLUID ? !this.mode : true;
            }
         });
      this.gasInputHandler = InputHelper.getInputHandler(this.gasTank, NOT_ENOUGH_GAS_INPUT_ERROR);
      this.fluidInputHandler = InputHelper.getInputHandler(this.fluidTank, NOT_ENOUGH_FLUID_INPUT_ERROR);
      this.gasOutputHandler = OutputHelper.getOutputHandler(this.gasTank, NOT_ENOUGH_SPACE_GAS_OUTPUT_ERROR);
      this.fluidOutputHandler = OutputHelper.getOutputHandler(this.fluidTank, NOT_ENOUGH_SPACE_FLUID_OUTPUT_ERROR);
   }

   @NotNull
   @Override
   public IChemicalTankHolder<Gas, GasStack, IGasTank> getInitialGasTanks(IContentsListener listener, IContentsListener recipeCacheListener) {
      ChemicalTankHelper<Gas, GasStack, IGasTank> builder = ChemicalTankHelper.forSideGasWithConfig(this::getDirection, this::getConfig);
      builder.addTank(
         this.gasTank = (IGasTank)ChemicalTankBuilder.GAS
            .create(
               10000L,
               (gas, automationType) -> automationType == AutomationType.MANUAL || this.mode,
               (gas, automationType) -> automationType == AutomationType.INTERNAL || !this.mode,
               this::isValidGas,
               ChemicalAttributeValidator.ALWAYS_ALLOW,
               recipeCacheListener
            )
      );
      return builder.build();
   }

   private boolean isValidGas(@NotNull Gas gas) {
      return this.getRecipeType().getInputCache().containsInput(this.f_58857_, gas.getStack(1L));
   }

   @NotNull
   @Override
   protected IFluidTankHolder getInitialFluidTanks(IContentsListener listener, IContentsListener recipeCacheListener) {
      FluidTankHelper builder = FluidTankHelper.forSideWithConfig(this::getDirection, this::getConfig);
      builder.addTank(
         this.fluidTank = BasicFluidTank.create(
            10000,
            (fluid, automationType) -> automationType == AutomationType.MANUAL || !this.mode,
            (fluid, automationType) -> automationType == AutomationType.INTERNAL || this.mode,
            this::isValidFluid,
            recipeCacheListener
         )
      );
      return builder.build();
   }

   private boolean isValidFluid(@NotNull FluidStack fluidStack) {
      return this.getRecipeType().getInputCache().containsInput(this.f_58857_, fluidStack);
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
      BooleanSupplier modeSupplier = () -> this.mode;
      builder.addSlot(this.gasInputSlot = GasInventorySlot.rotaryDrain(this.gasTank, modeSupplier, listener, 5, 25));
      builder.addSlot(this.gasOutputSlot = GasInventorySlot.rotaryFill(this.gasTank, modeSupplier, listener, 5, 56));
      builder.addSlot(this.fluidInputSlot = FluidInventorySlot.rotary(this.fluidTank, modeSupplier, listener, 155, 25));
      builder.addSlot(this.fluidOutputSlot = OutputInventorySlot.at(listener, 155, 56));
      builder.addSlot(this.energySlot = EnergyInventorySlot.fillOrConvert(this.energyContainer, this::m_58904_, listener, 155, 5));
      this.gasInputSlot.setSlotType(ContainerSlotType.INPUT);
      this.gasInputSlot.setSlotOverlay(SlotOverlay.PLUS);
      this.gasOutputSlot.setSlotType(ContainerSlotType.OUTPUT);
      this.gasOutputSlot.setSlotOverlay(SlotOverlay.MINUS);
      this.fluidInputSlot.setSlotType(ContainerSlotType.INPUT);
      return builder.build();
   }

   @Override
   protected void onUpdateServer() {
      super.onUpdateServer();
      this.energySlot.fillContainerOrConvert();
      if (this.mode) {
         this.fluidInputSlot.fillTank(this.fluidOutputSlot);
         this.gasInputSlot.drainTank();
      } else {
         this.gasOutputSlot.fillTank();
         this.fluidInputSlot.drainTank(this.fluidOutputSlot);
      }

      this.clientEnergyUsed = this.recipeCacheLookupMonitor.updateAndProcess(this.energyContainer);
   }

   @Override
   public void nextMode() {
      this.mode = !this.mode;
      this.m_6596_();
   }

   @Override
   public void previousMode() {
      this.nextMode();
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
   protected void loadGeneralPersistentData(CompoundTag data) {
      super.loadGeneralPersistentData(data);
      NBTUtils.setBooleanIfPresent(data, "mode", value -> this.mode = value);
   }

   @Override
   protected void addGeneralPersistentData(CompoundTag data) {
      super.addGeneralPersistentData(data);
      data.m_128379_("mode", this.mode);
   }

   @Override
   public int getRedstoneLevel() {
      return this.mode
         ? MekanismUtils.redstoneLevelFromContents(this.fluidTank.getFluidAmount(), this.fluidTank.getCapacity())
         : MekanismUtils.redstoneLevelFromContents(this.gasTank.getStored(), this.gasTank.getCapacity());
   }

   @Override
   protected boolean makesComparatorDirty(@Nullable SubstanceType type) {
      return type == SubstanceType.FLUID || type == SubstanceType.GAS;
   }

   @NotNull
   @Override
   public IMekanismRecipeTypeProvider<RotaryRecipe, RotaryInputRecipeCache> getRecipeType() {
      return MekanismRecipeType.ROTARY;
   }

   @Nullable
   public RotaryRecipe getRecipe(int cacheIndex) {
      RotaryInputRecipeCache inputCache = this.getRecipeType().getInputCache();
      return this.mode
         ? inputCache.findFirstRecipe(this.f_58857_, this.fluidInputHandler.getInput())
         : inputCache.findFirstRecipe(this.f_58857_, this.gasInputHandler.getInput());
   }

   public MachineEnergyContainer<TileEntityRotaryCondensentrator> getEnergyContainer() {
      return this.energyContainer;
   }

   @NotNull
   public CachedRecipe<RotaryRecipe> createNewCachedRecipe(@NotNull RotaryRecipe recipe, int cacheIndex) {
      return new RotaryCachedRecipe(
            recipe, this.recheckAllRecipeErrors, this.fluidInputHandler, this.gasInputHandler, this.gasOutputHandler, this.fluidOutputHandler, () -> this.mode
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
   public void addContainerTrackers(MekanismContainer container) {
      super.addContainerTrackers(container);
      container.track(SyncableBoolean.create(() -> this.mode, value -> this.mode = value));
      container.track(SyncableFloatingLong.create(this::getEnergyUsed, value -> this.clientEnergyUsed = value));
   }

   @ComputerMethod
   boolean isCondensentrating() {
      return !this.mode;
   }

   @ComputerMethod(
      requiresPublicSecurity = true
   )
   void setCondensentrating(boolean value) throws ComputerException {
      this.validateSecurityIsPublic();
      if (this.mode != value) {
         this.mode = value;
         this.m_6596_();
      }
   }
}
