package mekanism.common.tile.machine;

import java.util.List;
import mekanism.api.IContentsListener;
import mekanism.api.math.FloatingLong;
import mekanism.api.recipes.CombinerRecipe;
import mekanism.api.recipes.cache.CachedRecipe;
import mekanism.api.recipes.cache.TwoInputCachedRecipe;
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
import mekanism.common.inventory.container.slot.ContainerSlotType;
import mekanism.common.inventory.slot.EnergyInventorySlot;
import mekanism.common.inventory.slot.InputInventorySlot;
import mekanism.common.inventory.slot.OutputInventorySlot;
import mekanism.common.inventory.warning.WarningTracker;
import mekanism.common.lib.transmitter.TransmissionType;
import mekanism.common.recipe.IMekanismRecipeTypeProvider;
import mekanism.common.recipe.MekanismRecipeType;
import mekanism.common.recipe.lookup.IDoubleRecipeLookupHandler;
import mekanism.common.recipe.lookup.cache.InputRecipeCache;
import mekanism.common.registries.MekanismBlocks;
import mekanism.common.tile.component.TileComponentConfig;
import mekanism.common.tile.component.TileComponentEjector;
import mekanism.common.tile.prefab.TileEntityProgressMachine;
import mekanism.common.upgrade.CombinerUpgradeData;
import mekanism.common.util.MekanismUtils;
import net.minecraft.core.BlockPos;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class TileEntityCombiner
   extends TileEntityProgressMachine<CombinerRecipe>
   implements IDoubleRecipeLookupHandler.DoubleItemRecipeLookupHandler<CombinerRecipe> {
   private static final List<CachedRecipe.OperationTracker.RecipeError> TRACKED_ERROR_TYPES = List.of(
      CachedRecipe.OperationTracker.RecipeError.NOT_ENOUGH_ENERGY,
      CachedRecipe.OperationTracker.RecipeError.NOT_ENOUGH_INPUT,
      CachedRecipe.OperationTracker.RecipeError.NOT_ENOUGH_SECONDARY_INPUT,
      CachedRecipe.OperationTracker.RecipeError.NOT_ENOUGH_OUTPUT_SPACE,
      CachedRecipe.OperationTracker.RecipeError.INPUT_DOESNT_PRODUCE_OUTPUT
   );
   private final IOutputHandler<ItemStack> outputHandler;
   private final IInputHandler<ItemStack> inputHandler;
   private final IInputHandler<ItemStack> extraInputHandler;
   private MachineEnergyContainer<TileEntityCombiner> energyContainer;
   @WrappingComputerMethod(
      wrapper = SpecialComputerMethodWrapper.ComputerIInventorySlotWrapper.class,
      methodNames = {"getMainInput"},
      docPlaceholder = "main input slot"
   )
   InputInventorySlot mainInputSlot;
   @WrappingComputerMethod(
      wrapper = SpecialComputerMethodWrapper.ComputerIInventorySlotWrapper.class,
      methodNames = {"getSecondaryInput"},
      docPlaceholder = "secondary input slot"
   )
   InputInventorySlot extraInputSlot;
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

   public TileEntityCombiner(BlockPos pos, BlockState state) {
      super(MekanismBlocks.COMBINER, pos, state, TRACKED_ERROR_TYPES, 200);
      this.configComponent = new TileComponentConfig(this, TransmissionType.ITEM, TransmissionType.ENERGY);
      this.configComponent.setupItemIOExtraConfig(this.mainInputSlot, this.outputSlot, this.extraInputSlot, this.energySlot);
      this.configComponent.setupInputConfig(TransmissionType.ENERGY, this.energyContainer);
      this.ejectorComponent = new TileComponentEjector(this);
      this.ejectorComponent.setOutputData(this.configComponent, TransmissionType.ITEM);
      this.inputHandler = InputHelper.getInputHandler(this.mainInputSlot, CachedRecipe.OperationTracker.RecipeError.NOT_ENOUGH_INPUT);
      this.extraInputHandler = InputHelper.getInputHandler(this.extraInputSlot, CachedRecipe.OperationTracker.RecipeError.NOT_ENOUGH_SECONDARY_INPUT);
      this.outputHandler = OutputHelper.getOutputHandler(this.outputSlot, CachedRecipe.OperationTracker.RecipeError.NOT_ENOUGH_OUTPUT_SPACE);
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
      builder.addSlot(
            this.mainInputSlot = InputInventorySlot.at(
               item -> this.containsRecipeAB(item, this.extraInputSlot.getStack()), this::containsRecipeA, recipeCacheListener, 64, 17
            )
         )
         .tracksWarnings(
            slot -> slot.warning(
               WarningTracker.WarningType.NO_MATCHING_RECIPE, this.getWarningCheck(CachedRecipe.OperationTracker.RecipeError.NOT_ENOUGH_INPUT)
            )
         );
      builder.addSlot(
            this.extraInputSlot = InputInventorySlot.at(
               item -> this.containsRecipeBA(this.mainInputSlot.getStack(), item), this::containsRecipeB, recipeCacheListener, 64, 53
            )
         )
         .tracksWarnings(
            slot -> slot.warning(
               WarningTracker.WarningType.NO_MATCHING_RECIPE, this.getWarningCheck(CachedRecipe.OperationTracker.RecipeError.NOT_ENOUGH_SECONDARY_INPUT)
            )
         );
      builder.addSlot(this.outputSlot = OutputInventorySlot.at(listener, 116, 35))
         .tracksWarnings(
            slot -> slot.warning(
               WarningTracker.WarningType.NO_SPACE_IN_OUTPUT, this.getWarningCheck(CachedRecipe.OperationTracker.RecipeError.NOT_ENOUGH_OUTPUT_SPACE)
            )
         );
      builder.addSlot(this.energySlot = EnergyInventorySlot.fillOrConvert(this.energyContainer, this::m_58904_, listener, 39, 35));
      this.extraInputSlot.setSlotType(ContainerSlotType.EXTRA);
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
   public IMekanismRecipeTypeProvider<CombinerRecipe, InputRecipeCache.DoubleItem<CombinerRecipe>> getRecipeType() {
      return MekanismRecipeType.COMBINING;
   }

   @Nullable
   public CombinerRecipe getRecipe(int cacheIndex) {
      return this.findFirstRecipe(this.inputHandler, this.extraInputHandler);
   }

   @NotNull
   public CachedRecipe<CombinerRecipe> createNewCachedRecipe(@NotNull CombinerRecipe recipe, int cacheIndex) {
      return TwoInputCachedRecipe.combiner(recipe, this.recheckAllRecipeErrors, this.inputHandler, this.extraInputHandler, this.outputHandler)
         .setErrorsChanged(x$0 -> this.onErrorsChanged(x$0))
         .setCanHolderFunction(() -> MekanismUtils.canFunction(this))
         .setActive(this::setActive)
         .setEnergyRequirements(this.energyContainer::getEnergyPerTick, this.energyContainer)
         .setRequiredTicks(this::getTicksRequired)
         .setOnFinish(this::markForSave)
         .setOperatingTicksChanged(x$0 -> this.setOperatingTicks(x$0));
   }

   @NotNull
   public CombinerUpgradeData getUpgradeData() {
      return new CombinerUpgradeData(
         this.redstone,
         this.getControlType(),
         this.getEnergyContainer(),
         this.getOperatingTicks(),
         this.energySlot,
         this.extraInputSlot,
         this.mainInputSlot,
         this.outputSlot,
         this.getComponents()
      );
   }

   public MachineEnergyContainer<TileEntityCombiner> getEnergyContainer() {
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
