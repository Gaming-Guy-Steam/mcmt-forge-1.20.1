package mekanism.common.tile.machine;

import java.util.List;
import mekanism.api.IContentsListener;
import mekanism.api.RelativeSide;
import mekanism.api.chemical.ChemicalTankBuilder;
import mekanism.api.chemical.pigment.IPigmentTank;
import mekanism.api.chemical.pigment.Pigment;
import mekanism.api.chemical.pigment.PigmentStack;
import mekanism.api.inventory.IInventorySlot;
import mekanism.api.math.FloatingLong;
import mekanism.api.recipes.ItemStackToPigmentRecipe;
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
import mekanism.common.inventory.container.slot.SlotOverlay;
import mekanism.common.inventory.slot.EnergyInventorySlot;
import mekanism.common.inventory.slot.InputInventorySlot;
import mekanism.common.inventory.slot.chemical.PigmentInventorySlot;
import mekanism.common.inventory.warning.WarningTracker;
import mekanism.common.lib.transmitter.TransmissionType;
import mekanism.common.recipe.IMekanismRecipeTypeProvider;
import mekanism.common.recipe.MekanismRecipeType;
import mekanism.common.recipe.lookup.ISingleRecipeLookupHandler;
import mekanism.common.recipe.lookup.cache.InputRecipeCache;
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

public class TileEntityPigmentExtractor
   extends TileEntityProgressMachine<ItemStackToPigmentRecipe>
   implements ISingleRecipeLookupHandler.ItemRecipeLookupHandler<ItemStackToPigmentRecipe> {
   private static final List<CachedRecipe.OperationTracker.RecipeError> TRACKED_ERROR_TYPES = List.of(
      CachedRecipe.OperationTracker.RecipeError.NOT_ENOUGH_ENERGY,
      CachedRecipe.OperationTracker.RecipeError.NOT_ENOUGH_INPUT,
      CachedRecipe.OperationTracker.RecipeError.NOT_ENOUGH_OUTPUT_SPACE,
      CachedRecipe.OperationTracker.RecipeError.INPUT_DOESNT_PRODUCE_OUTPUT
   );
   @WrappingComputerMethod(
      wrapper = SpecialComputerMethodWrapper.ComputerChemicalTankWrapper.class,
      methodNames = {"getOutput", "getOutputCapacity", "getOutputNeeded", "getOutputFilledPercentage"},
      docPlaceholder = "pigment tank"
   )
   public IPigmentTank pigmentTank;
   private final IOutputHandler<PigmentStack> outputHandler;
   private final IInputHandler<ItemStack> inputHandler;
   private MachineEnergyContainer<TileEntityPigmentExtractor> energyContainer;
   @WrappingComputerMethod(
      wrapper = SpecialComputerMethodWrapper.ComputerIInventorySlotWrapper.class,
      methodNames = {"getInput"},
      docPlaceholder = "input slot"
   )
   InputInventorySlot inputSlot;
   @WrappingComputerMethod(
      wrapper = SpecialComputerMethodWrapper.ComputerIInventorySlotWrapper.class,
      methodNames = {"getOutputItem"},
      docPlaceholder = "output slot"
   )
   PigmentInventorySlot outputSlot;
   @WrappingComputerMethod(
      wrapper = SpecialComputerMethodWrapper.ComputerIInventorySlotWrapper.class,
      methodNames = {"getEnergyItem"},
      docPlaceholder = "energy slot"
   )
   EnergyInventorySlot energySlot;

   public TileEntityPigmentExtractor(BlockPos pos, BlockState state) {
      super(MekanismBlocks.PIGMENT_EXTRACTOR, pos, state, TRACKED_ERROR_TYPES, 100);
      this.configComponent = new TileComponentConfig(this, TransmissionType.ITEM, TransmissionType.PIGMENT, TransmissionType.ENERGY);
      this.configComponent.setupItemIOConfig(this.inputSlot, this.outputSlot, this.energySlot);
      this.configComponent.setupOutputConfig(TransmissionType.PIGMENT, this.pigmentTank, RelativeSide.RIGHT);
      this.configComponent.setupInputConfig(TransmissionType.ENERGY, this.energyContainer);
      this.ejectorComponent = new TileComponentEjector(this);
      this.ejectorComponent.setOutputData(this.configComponent, TransmissionType.PIGMENT);
      this.inputHandler = InputHelper.getInputHandler(this.inputSlot, CachedRecipe.OperationTracker.RecipeError.NOT_ENOUGH_INPUT);
      this.outputHandler = OutputHelper.getOutputHandler(this.pigmentTank, CachedRecipe.OperationTracker.RecipeError.NOT_ENOUGH_OUTPUT_SPACE);
   }

   @NotNull
   @Override
   public IChemicalTankHolder<Pigment, PigmentStack, IPigmentTank> getInitialPigmentTanks(IContentsListener listener, IContentsListener recipeCacheListener) {
      ChemicalTankHelper<Pigment, PigmentStack, IPigmentTank> builder = ChemicalTankHelper.forSidePigmentWithConfig(this::getDirection, this::getConfig);
      builder.addTank(this.pigmentTank = (IPigmentTank)ChemicalTankBuilder.PIGMENT.output(20000L, listener));
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
      builder.addSlot(this.inputSlot = InputInventorySlot.at(this::containsRecipe, recipeCacheListener, 26, 36))
         .tracksWarnings(
            slot -> slot.warning(
               WarningTracker.WarningType.NO_MATCHING_RECIPE, this.getWarningCheck(CachedRecipe.OperationTracker.RecipeError.NOT_ENOUGH_INPUT)
            )
         );
      builder.addSlot(this.outputSlot = PigmentInventorySlot.drain(this.pigmentTank, listener, 152, 55));
      builder.addSlot(this.energySlot = EnergyInventorySlot.fillOrConvert(this.energyContainer, this::m_58904_, listener, 152, 14));
      this.outputSlot.setSlotOverlay(SlotOverlay.PLUS);
      return builder.build();
   }

   @Override
   protected void onUpdateServer() {
      super.onUpdateServer();
      this.energySlot.fillContainerOrConvert();
      this.outputSlot.drainTank();
      this.recipeCacheLookupMonitor.updateAndProcess();
   }

   public IInventorySlot getInputSlot() {
      return this.inputSlot;
   }

   @NotNull
   @Override
   public IMekanismRecipeTypeProvider<ItemStackToPigmentRecipe, InputRecipeCache.SingleItem<ItemStackToPigmentRecipe>> getRecipeType() {
      return MekanismRecipeType.PIGMENT_EXTRACTING;
   }

   @Nullable
   public ItemStackToPigmentRecipe getRecipe(int cacheIndex) {
      return this.findFirstRecipe(this.inputHandler);
   }

   @NotNull
   public CachedRecipe<ItemStackToPigmentRecipe> createNewCachedRecipe(@NotNull ItemStackToPigmentRecipe recipe, int cacheIndex) {
      return OneInputCachedRecipe.itemToChemical(recipe, this.recheckAllRecipeErrors, this.inputHandler, this.outputHandler)
         .setErrorsChanged(x$0 -> this.onErrorsChanged(x$0))
         .setCanHolderFunction(() -> MekanismUtils.canFunction(this))
         .setActive(this::setActive)
         .setEnergyRequirements(this.energyContainer::getEnergyPerTick, this.energyContainer)
         .setRequiredTicks(this::getTicksRequired)
         .setOnFinish(this::markForSave)
         .setOperatingTicksChanged(x$0 -> this.setOperatingTicks(x$0));
   }

   public MachineEnergyContainer<TileEntityPigmentExtractor> getEnergyContainer() {
      return this.energyContainer;
   }

   @ComputerMethod(
      methodDescription = "Get the energy used in the last tick by the machine"
   )
   FloatingLong getEnergyUsage() {
      return this.getActive() ? this.energyContainer.getEnergyPerTick() : FloatingLong.ZERO;
   }
}
