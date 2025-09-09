package mekanism.common.tile.factory;

import java.util.List;
import java.util.Set;
import mekanism.api.inventory.IInventorySlot;
import mekanism.api.math.MathUtils;
import mekanism.api.providers.IBlockProvider;
import mekanism.api.recipes.ItemStackToItemStackRecipe;
import mekanism.api.recipes.cache.CachedRecipe;
import mekanism.api.recipes.cache.OneInputCachedRecipe;
import mekanism.common.recipe.IMekanismRecipeTypeProvider;
import mekanism.common.recipe.MekanismRecipeType;
import mekanism.common.recipe.lookup.ISingleRecipeLookupHandler;
import mekanism.common.recipe.lookup.cache.InputRecipeCache;
import mekanism.common.upgrade.MachineUpgradeData;
import mekanism.common.util.InventoryUtils;
import mekanism.common.util.MekanismUtils;
import net.minecraft.core.BlockPos;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.state.BlockState;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class TileEntityItemStackToItemStackFactory
   extends TileEntityItemToItemFactory<ItemStackToItemStackRecipe>
   implements ISingleRecipeLookupHandler.ItemRecipeLookupHandler<ItemStackToItemStackRecipe> {
   private static final List<CachedRecipe.OperationTracker.RecipeError> TRACKED_ERROR_TYPES = List.of(
      CachedRecipe.OperationTracker.RecipeError.NOT_ENOUGH_ENERGY,
      CachedRecipe.OperationTracker.RecipeError.NOT_ENOUGH_INPUT,
      CachedRecipe.OperationTracker.RecipeError.NOT_ENOUGH_OUTPUT_SPACE,
      CachedRecipe.OperationTracker.RecipeError.INPUT_DOESNT_PRODUCE_OUTPUT
   );
   private static final Set<CachedRecipe.OperationTracker.RecipeError> GLOBAL_ERROR_TYPES = Set.of(CachedRecipe.OperationTracker.RecipeError.NOT_ENOUGH_ENERGY);

   public TileEntityItemStackToItemStackFactory(IBlockProvider blockProvider, BlockPos pos, BlockState state) {
      super(blockProvider, pos, state, TRACKED_ERROR_TYPES, GLOBAL_ERROR_TYPES);
   }

   @Override
   public boolean isValidInputItem(@NotNull ItemStack stack) {
      return this.containsRecipe(stack);
   }

   protected int getNeededInput(ItemStackToItemStackRecipe recipe, ItemStack inputStack) {
      return MathUtils.clampToInt(recipe.getInput().getNeededAmount(inputStack));
   }

   @Override
   protected boolean isCachedRecipeValid(@Nullable CachedRecipe<ItemStackToItemStackRecipe> cached, @NotNull ItemStack stack) {
      return cached != null && cached.getRecipe().getInput().testType(stack);
   }

   protected ItemStackToItemStackRecipe findRecipe(
      int process, @NotNull ItemStack fallbackInput, @NotNull IInventorySlot outputSlot, @Nullable IInventorySlot secondaryOutputSlot
   ) {
      ItemStack output = outputSlot.getStack();
      return this.getRecipeType()
         .getInputCache()
         .findTypeBasedRecipe(this.f_58857_, fallbackInput, recipe -> InventoryUtils.areItemsStackable(recipe.getOutput(fallbackInput), output));
   }

   @NotNull
   @Override
   public IMekanismRecipeTypeProvider<ItemStackToItemStackRecipe, InputRecipeCache.SingleItem<ItemStackToItemStackRecipe>> getRecipeType() {
      return switch (this.type) {
         case ENRICHING -> MekanismRecipeType.ENRICHING;
         case CRUSHING -> MekanismRecipeType.CRUSHING;
         default -> MekanismRecipeType.SMELTING;
      };
   }

   @Nullable
   public ItemStackToItemStackRecipe getRecipe(int cacheIndex) {
      return this.findFirstRecipe(this.inputHandlers[cacheIndex]);
   }

   @NotNull
   public CachedRecipe<ItemStackToItemStackRecipe> createNewCachedRecipe(@NotNull ItemStackToItemStackRecipe recipe, int cacheIndex) {
      return OneInputCachedRecipe.itemToItem(recipe, this.recheckAllRecipeErrors[cacheIndex], this.inputHandlers[cacheIndex], this.outputHandlers[cacheIndex])
         .setErrorsChanged(errors -> this.errorTracker.onErrorsChanged(errors, cacheIndex))
         .setCanHolderFunction(() -> MekanismUtils.canFunction(this))
         .setActive(active -> this.setActiveState(active, cacheIndex))
         .setEnergyRequirements(this.energyContainer::getEnergyPerTick, this.energyContainer)
         .setRequiredTicks(this::getTicksRequired)
         .setOnFinish(this::markForSave)
         .setOperatingTicksChanged(operatingTicks -> this.progress[cacheIndex] = operatingTicks);
   }

   @NotNull
   public MachineUpgradeData getUpgradeData() {
      return new MachineUpgradeData(
         this.redstone,
         this.getControlType(),
         this.getEnergyContainer(),
         this.progress,
         this.energySlot,
         this.inputSlots,
         this.outputSlots,
         this.isSorting(),
         this.getComponents()
      );
   }
}
