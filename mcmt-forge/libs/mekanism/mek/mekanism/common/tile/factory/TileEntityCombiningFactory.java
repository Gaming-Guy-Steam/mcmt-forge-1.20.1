package mekanism.common.tile.factory;

import java.util.List;
import java.util.Set;
import mekanism.api.IContentsListener;
import mekanism.api.inventory.IInventorySlot;
import mekanism.api.math.MathUtils;
import mekanism.api.providers.IBlockProvider;
import mekanism.api.recipes.CombinerRecipe;
import mekanism.api.recipes.cache.CachedRecipe;
import mekanism.api.recipes.cache.TwoInputCachedRecipe;
import mekanism.api.recipes.inputs.IInputHandler;
import mekanism.api.recipes.inputs.InputHelper;
import mekanism.common.Mekanism;
import mekanism.common.capabilities.holder.slot.InventorySlotHelper;
import mekanism.common.integration.computer.SpecialComputerMethodWrapper;
import mekanism.common.integration.computer.annotation.WrappingComputerMethod;
import mekanism.common.inventory.container.slot.ContainerSlotType;
import mekanism.common.inventory.slot.InputInventorySlot;
import mekanism.common.recipe.IMekanismRecipeTypeProvider;
import mekanism.common.recipe.MekanismRecipeType;
import mekanism.common.recipe.lookup.IDoubleRecipeLookupHandler;
import mekanism.common.recipe.lookup.cache.InputRecipeCache;
import mekanism.common.upgrade.CombinerUpgradeData;
import mekanism.common.upgrade.IUpgradeData;
import mekanism.common.util.InventoryUtils;
import mekanism.common.util.MekanismUtils;
import net.minecraft.core.BlockPos;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.state.BlockState;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class TileEntityCombiningFactory
   extends TileEntityItemToItemFactory<CombinerRecipe>
   implements IDoubleRecipeLookupHandler.DoubleItemRecipeLookupHandler<CombinerRecipe> {
   private static final List<CachedRecipe.OperationTracker.RecipeError> TRACKED_ERROR_TYPES = List.of(
      CachedRecipe.OperationTracker.RecipeError.NOT_ENOUGH_ENERGY,
      CachedRecipe.OperationTracker.RecipeError.NOT_ENOUGH_INPUT,
      CachedRecipe.OperationTracker.RecipeError.NOT_ENOUGH_SECONDARY_INPUT,
      CachedRecipe.OperationTracker.RecipeError.NOT_ENOUGH_OUTPUT_SPACE,
      CachedRecipe.OperationTracker.RecipeError.INPUT_DOESNT_PRODUCE_OUTPUT
   );
   private static final Set<CachedRecipe.OperationTracker.RecipeError> GLOBAL_ERROR_TYPES = Set.of(
      CachedRecipe.OperationTracker.RecipeError.NOT_ENOUGH_ENERGY, CachedRecipe.OperationTracker.RecipeError.NOT_ENOUGH_SECONDARY_INPUT
   );
   private final IInputHandler<ItemStack> extraInputHandler;
   @WrappingComputerMethod(
      wrapper = SpecialComputerMethodWrapper.ComputerIInventorySlotWrapper.class,
      methodNames = {"getSecondaryInput"},
      docPlaceholder = "secondary input slot"
   )
   InputInventorySlot extraSlot;

   public TileEntityCombiningFactory(IBlockProvider blockProvider, BlockPos pos, BlockState state) {
      super(blockProvider, pos, state, TRACKED_ERROR_TYPES, GLOBAL_ERROR_TYPES);
      this.extraInputHandler = InputHelper.getInputHandler(this.extraSlot, CachedRecipe.OperationTracker.RecipeError.NOT_ENOUGH_SECONDARY_INPUT);
   }

   @Override
   protected void addSlots(InventorySlotHelper builder, IContentsListener listener, IContentsListener updateSortingListener) {
      super.addSlots(builder, listener, updateSortingListener);
      builder.addSlot(this.extraSlot = InputInventorySlot.at(this::containsRecipeB, this.markAllMonitorsChanged(listener), 7, 57));
      this.extraSlot.setSlotType(ContainerSlotType.EXTRA);
   }

   @Nullable
   protected InputInventorySlot getExtraSlot() {
      return this.extraSlot;
   }

   @Override
   public boolean isValidInputItem(@NotNull ItemStack stack) {
      return this.containsRecipeA(stack);
   }

   protected int getNeededInput(CombinerRecipe recipe, ItemStack inputStack) {
      return MathUtils.clampToInt(recipe.getMainInput().getNeededAmount(inputStack));
   }

   @Override
   protected boolean isCachedRecipeValid(@Nullable CachedRecipe<CombinerRecipe> cached, @NotNull ItemStack stack) {
      if (cached == null) {
         return false;
      } else {
         CombinerRecipe cachedRecipe = cached.getRecipe();
         return cachedRecipe.getMainInput().testType(stack) && (this.extraSlot.isEmpty() || cachedRecipe.getExtraInput().testType(this.extraSlot.getStack()));
      }
   }

   protected CombinerRecipe findRecipe(
      int process, @NotNull ItemStack fallbackInput, @NotNull IInventorySlot outputSlot, @Nullable IInventorySlot secondaryOutputSlot
   ) {
      ItemStack extra = this.extraSlot.getStack();
      ItemStack output = outputSlot.getStack();
      return this.getRecipeType()
         .getInputCache()
         .findTypeBasedRecipe(this.f_58857_, fallbackInput, extra, recipe -> InventoryUtils.areItemsStackable(recipe.getOutput(fallbackInput, extra), output));
   }

   @NotNull
   @Override
   public IMekanismRecipeTypeProvider<CombinerRecipe, InputRecipeCache.DoubleItem<CombinerRecipe>> getRecipeType() {
      return MekanismRecipeType.COMBINING;
   }

   @Nullable
   public CombinerRecipe getRecipe(int cacheIndex) {
      return this.findFirstRecipe(this.inputHandlers[cacheIndex], this.extraInputHandler);
   }

   @NotNull
   public CachedRecipe<CombinerRecipe> createNewCachedRecipe(@NotNull CombinerRecipe recipe, int cacheIndex) {
      return TwoInputCachedRecipe.combiner(
            recipe, this.recheckAllRecipeErrors[cacheIndex], this.inputHandlers[cacheIndex], this.extraInputHandler, this.outputHandlers[cacheIndex]
         )
         .setErrorsChanged(errors -> this.errorTracker.onErrorsChanged(errors, cacheIndex))
         .setCanHolderFunction(() -> MekanismUtils.canFunction(this))
         .setActive(active -> this.setActiveState(active, cacheIndex))
         .setEnergyRequirements(this.energyContainer::getEnergyPerTick, this.energyContainer)
         .setRequiredTicks(this::getTicksRequired)
         .setOnFinish(this::markForSave)
         .setOperatingTicksChanged(operatingTicks -> this.progress[cacheIndex] = operatingTicks);
   }

   @Override
   public void parseUpgradeData(@NotNull IUpgradeData upgradeData) {
      if (upgradeData instanceof CombinerUpgradeData data) {
         super.parseUpgradeData(upgradeData);
         this.extraSlot.deserializeNBT(data.extraSlot.serializeNBT());
      } else {
         Mekanism.logger.warn("Unhandled upgrade data.", new Throwable());
      }
   }

   @NotNull
   public CombinerUpgradeData getUpgradeData() {
      return new CombinerUpgradeData(
         this.redstone,
         this.getControlType(),
         this.getEnergyContainer(),
         this.progress,
         this.energySlot,
         this.extraSlot,
         this.inputSlots,
         this.outputSlots,
         this.isSorting(),
         this.getComponents()
      );
   }
}
