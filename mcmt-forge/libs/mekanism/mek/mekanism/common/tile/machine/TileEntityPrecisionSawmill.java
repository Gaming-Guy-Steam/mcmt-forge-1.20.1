package mekanism.common.tile.machine;

import java.util.Collections;
import java.util.List;
import mekanism.api.IContentsListener;
import mekanism.api.math.FloatingLong;
import mekanism.api.recipes.SawmillRecipe;
import mekanism.api.recipes.cache.CachedRecipe;
import mekanism.api.recipes.cache.OneInputCachedRecipe;
import mekanism.api.recipes.inputs.IInputHandler;
import mekanism.api.recipes.inputs.InputHelper;
import mekanism.api.recipes.outputs.IOutputHandler;
import mekanism.api.recipes.outputs.OutputHelper;
import mekanism.common.capabilities.energy.MachineEnergyContainer;
import mekanism.common.capabilities.holder.energy.EnergyContainerHelper;
import mekanism.common.capabilities.holder.energy.IEnergyContainerHolder;
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
import mekanism.common.recipe.lookup.ISingleRecipeLookupHandler;
import mekanism.common.recipe.lookup.cache.InputRecipeCache;
import mekanism.common.registries.MekanismBlocks;
import mekanism.common.tile.component.TileComponentConfig;
import mekanism.common.tile.component.TileComponentEjector;
import mekanism.common.tile.prefab.TileEntityProgressMachine;
import mekanism.common.upgrade.SawmillUpgradeData;
import mekanism.common.util.MekanismUtils;
import net.minecraft.core.BlockPos;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class TileEntityPrecisionSawmill
   extends TileEntityProgressMachine<SawmillRecipe>
   implements ISingleRecipeLookupHandler.ItemRecipeLookupHandler<SawmillRecipe> {
   public static final CachedRecipe.OperationTracker.RecipeError NOT_ENOUGH_SPACE_SECONDARY_OUTPUT_ERROR = CachedRecipe.OperationTracker.RecipeError.create();
   private static final List<CachedRecipe.OperationTracker.RecipeError> TRACKED_ERROR_TYPES = List.of(
      CachedRecipe.OperationTracker.RecipeError.NOT_ENOUGH_ENERGY,
      CachedRecipe.OperationTracker.RecipeError.NOT_ENOUGH_INPUT,
      CachedRecipe.OperationTracker.RecipeError.NOT_ENOUGH_OUTPUT_SPACE,
      NOT_ENOUGH_SPACE_SECONDARY_OUTPUT_ERROR,
      CachedRecipe.OperationTracker.RecipeError.INPUT_DOESNT_PRODUCE_OUTPUT
   );
   private final IOutputHandler<SawmillRecipe.ChanceOutput> outputHandler;
   private final IInputHandler<ItemStack> inputHandler;
   private MachineEnergyContainer<TileEntityPrecisionSawmill> energyContainer;
   @WrappingComputerMethod(
      wrapper = SpecialComputerMethodWrapper.ComputerIInventorySlotWrapper.class,
      methodNames = {"getInput"},
      docPlaceholder = "input slot"
   )
   InputInventorySlot inputSlot;
   @WrappingComputerMethod(
      wrapper = SpecialComputerMethodWrapper.ComputerIInventorySlotWrapper.class,
      methodNames = {"getOutput"},
      docPlaceholder = "output slot"
   )
   OutputInventorySlot outputSlot;
   @WrappingComputerMethod(
      wrapper = SpecialComputerMethodWrapper.ComputerIInventorySlotWrapper.class,
      methodNames = {"getSecondaryOutput"},
      docPlaceholder = "secondary output slot"
   )
   OutputInventorySlot secondaryOutputSlot;
   @WrappingComputerMethod(
      wrapper = SpecialComputerMethodWrapper.ComputerIInventorySlotWrapper.class,
      methodNames = {"getEnergyItem"},
      docPlaceholder = "energy slot"
   )
   EnergyInventorySlot energySlot;

   public TileEntityPrecisionSawmill(BlockPos pos, BlockState state) {
      super(MekanismBlocks.PRECISION_SAWMILL, pos, state, TRACKED_ERROR_TYPES, 200);
      this.configComponent = new TileComponentConfig(this, TransmissionType.ITEM, TransmissionType.ENERGY);
      this.configComponent
         .setupItemIOConfig(Collections.singletonList(this.inputSlot), List.of(this.outputSlot, this.secondaryOutputSlot), this.energySlot, false);
      this.configComponent.setupInputConfig(TransmissionType.ENERGY, this.energyContainer);
      this.ejectorComponent = new TileComponentEjector(this);
      this.ejectorComponent.setOutputData(this.configComponent, TransmissionType.ITEM);
      this.inputHandler = InputHelper.getInputHandler(this.inputSlot, CachedRecipe.OperationTracker.RecipeError.NOT_ENOUGH_INPUT);
      this.outputHandler = OutputHelper.getOutputHandler(
         this.outputSlot, CachedRecipe.OperationTracker.RecipeError.NOT_ENOUGH_OUTPUT_SPACE, this.secondaryOutputSlot, NOT_ENOUGH_SPACE_SECONDARY_OUTPUT_ERROR
      );
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
      builder.addSlot(this.inputSlot = InputInventorySlot.at(this::containsRecipe, recipeCacheListener, 56, 17))
         .tracksWarnings(
            slot -> slot.warning(
               WarningTracker.WarningType.NO_MATCHING_RECIPE, this.getWarningCheck(CachedRecipe.OperationTracker.RecipeError.NOT_ENOUGH_INPUT)
            )
         );
      builder.addSlot(this.outputSlot = OutputInventorySlot.at(listener, 116, 35));
      builder.addSlot(this.secondaryOutputSlot = OutputInventorySlot.at(listener, 132, 35));
      builder.addSlot(this.energySlot = EnergyInventorySlot.fillOrConvert(this.energyContainer, this::m_58904_, listener, 56, 53));
      return builder.build();
   }

   @Override
   protected void onUpdateServer() {
      super.onUpdateServer();
      this.energySlot.fillContainerOrConvert();
      this.recipeCacheLookupMonitor.updateAndProcess();
   }

   @NotNull
   @Override
   public IMekanismRecipeTypeProvider<SawmillRecipe, InputRecipeCache.SingleItem<SawmillRecipe>> getRecipeType() {
      return MekanismRecipeType.SAWING;
   }

   @Nullable
   public SawmillRecipe getRecipe(int cacheIndex) {
      return this.findFirstRecipe(this.inputHandler);
   }

   @NotNull
   public CachedRecipe<SawmillRecipe> createNewCachedRecipe(@NotNull SawmillRecipe recipe, int cacheIndex) {
      return OneInputCachedRecipe.sawing(recipe, this.recheckAllRecipeErrors, this.inputHandler, this.outputHandler)
         .setErrorsChanged(x$0 -> this.onErrorsChanged(x$0))
         .setCanHolderFunction(() -> MekanismUtils.canFunction(this))
         .setActive(this::setActive)
         .setEnergyRequirements(this.energyContainer::getEnergyPerTick, this.energyContainer)
         .setRequiredTicks(this::getTicksRequired)
         .setOnFinish(this::markForSave)
         .setOperatingTicksChanged(x$0 -> this.setOperatingTicks(x$0));
   }

   @NotNull
   public SawmillUpgradeData getUpgradeData() {
      return new SawmillUpgradeData(
         this.redstone,
         this.getControlType(),
         this.getEnergyContainer(),
         this.getOperatingTicks(),
         this.energySlot,
         this.inputSlot,
         this.outputSlot,
         this.secondaryOutputSlot,
         this.getComponents()
      );
   }

   public MachineEnergyContainer<TileEntityPrecisionSawmill> getEnergyContainer() {
      return this.energyContainer;
   }

   @Override
   public boolean isConfigurationDataCompatible(BlockEntityType<?> tileType) {
      return super.isConfigurationDataCompatible(tileType) || MekanismUtils.isSameTypeFactory(this.getBlockType(), tileType);
   }

   @ComputerMethod(
      methodDescription = "Get the energy used in the last tick by the machine"
   )
   FloatingLong getEnergyUsage() {
      return this.getActive() ? this.energyContainer.getEnergyPerTick() : FloatingLong.ZERO;
   }
}
