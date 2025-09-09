package mekanism.common.tile.factory;

import java.util.List;
import java.util.Set;
import mekanism.api.IContentsListener;
import mekanism.api.inventory.IInventorySlot;
import mekanism.api.math.MathUtils;
import mekanism.api.providers.IBlockProvider;
import mekanism.api.recipes.SawmillRecipe;
import mekanism.api.recipes.cache.CachedRecipe;
import mekanism.api.recipes.cache.OneInputCachedRecipe;
import mekanism.api.recipes.inputs.IInputHandler;
import mekanism.api.recipes.inputs.InputHelper;
import mekanism.api.recipes.outputs.IOutputHandler;
import mekanism.api.recipes.outputs.OutputHelper;
import mekanism.common.Mekanism;
import mekanism.common.capabilities.holder.slot.InventorySlotHelper;
import mekanism.common.integration.computer.ComputerException;
import mekanism.common.integration.computer.annotation.ComputerMethod;
import mekanism.common.inventory.slot.FactoryInputInventorySlot;
import mekanism.common.inventory.slot.OutputInventorySlot;
import mekanism.common.inventory.warning.WarningTracker;
import mekanism.common.recipe.IMekanismRecipeTypeProvider;
import mekanism.common.recipe.MekanismRecipeType;
import mekanism.common.recipe.lookup.ISingleRecipeLookupHandler;
import mekanism.common.recipe.lookup.cache.InputRecipeCache;
import mekanism.common.tier.FactoryTier;
import mekanism.common.tile.machine.TileEntityPrecisionSawmill;
import mekanism.common.upgrade.IUpgradeData;
import mekanism.common.upgrade.SawmillUpgradeData;
import mekanism.common.util.InventoryUtils;
import mekanism.common.util.MekanismUtils;
import net.minecraft.core.BlockPos;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraftforge.items.ItemHandlerHelper;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class TileEntitySawingFactory extends TileEntityFactory<SawmillRecipe> implements ISingleRecipeLookupHandler.ItemRecipeLookupHandler<SawmillRecipe> {
   private static final List<CachedRecipe.OperationTracker.RecipeError> TRACKED_ERROR_TYPES = List.of(
      CachedRecipe.OperationTracker.RecipeError.NOT_ENOUGH_ENERGY,
      CachedRecipe.OperationTracker.RecipeError.NOT_ENOUGH_INPUT,
      CachedRecipe.OperationTracker.RecipeError.NOT_ENOUGH_OUTPUT_SPACE,
      TileEntityPrecisionSawmill.NOT_ENOUGH_SPACE_SECONDARY_OUTPUT_ERROR,
      CachedRecipe.OperationTracker.RecipeError.INPUT_DOESNT_PRODUCE_OUTPUT
   );
   private static final Set<CachedRecipe.OperationTracker.RecipeError> GLOBAL_ERROR_TYPES = Set.of(CachedRecipe.OperationTracker.RecipeError.NOT_ENOUGH_ENERGY);
   protected IInputHandler<ItemStack>[] inputHandlers;
   protected IOutputHandler<SawmillRecipe.ChanceOutput>[] outputHandlers;

   public TileEntitySawingFactory(IBlockProvider blockProvider, BlockPos pos, BlockState state) {
      super(blockProvider, pos, state, TRACKED_ERROR_TYPES, GLOBAL_ERROR_TYPES);
   }

   @Override
   protected void addSlots(InventorySlotHelper builder, IContentsListener listener, IContentsListener updateSortingListener) {
      this.inputHandlers = new IInputHandler[this.tier.processes];
      this.outputHandlers = new IOutputHandler[this.tier.processes];
      this.processInfoSlots = new TileEntityFactory.ProcessInfo[this.tier.processes];
      int baseX = this.tier == FactoryTier.BASIC ? 55 : (this.tier == FactoryTier.ADVANCED ? 35 : (this.tier == FactoryTier.ELITE ? 29 : 27));
      int baseXMult = this.tier == FactoryTier.BASIC ? 38 : (this.tier == FactoryTier.ADVANCED ? 26 : 19);

      for (int i = 0; i < this.tier.processes; i++) {
         int xPos = baseX + i * baseXMult;
         OutputInventorySlot outputSlot = OutputInventorySlot.at(updateSortingListener, xPos, 57);
         OutputInventorySlot secondaryOutputSlot = OutputInventorySlot.at(updateSortingListener, xPos, 77);
         FactoryInputInventorySlot inputSlot = FactoryInputInventorySlot.create(
            this, i, outputSlot, secondaryOutputSlot, this.recipeCacheLookupMonitors[i], xPos, 13
         );
         int index = i;
         builder.addSlot(inputSlot)
            .tracksWarnings(
               slot -> slot.warning(
                  WarningTracker.WarningType.NO_MATCHING_RECIPE, this.getWarningCheck(CachedRecipe.OperationTracker.RecipeError.NOT_ENOUGH_INPUT, index)
               )
            );
         builder.addSlot(outputSlot)
            .tracksWarnings(
               slot -> slot.warning(
                  WarningTracker.WarningType.NO_SPACE_IN_OUTPUT, this.getWarningCheck(CachedRecipe.OperationTracker.RecipeError.NOT_ENOUGH_OUTPUT_SPACE, index)
               )
            );
         builder.addSlot(secondaryOutputSlot)
            .tracksWarnings(
               slot -> slot.warning(
                  WarningTracker.WarningType.NO_SPACE_IN_OUTPUT,
                  this.getWarningCheck(TileEntityPrecisionSawmill.NOT_ENOUGH_SPACE_SECONDARY_OUTPUT_ERROR, index)
               )
            );
         this.inputHandlers[i] = InputHelper.getInputHandler(inputSlot, CachedRecipe.OperationTracker.RecipeError.NOT_ENOUGH_INPUT);
         this.outputHandlers[i] = OutputHelper.getOutputHandler(
            outputSlot,
            CachedRecipe.OperationTracker.RecipeError.NOT_ENOUGH_OUTPUT_SPACE,
            secondaryOutputSlot,
            TileEntityPrecisionSawmill.NOT_ENOUGH_SPACE_SECONDARY_OUTPUT_ERROR
         );
         this.processInfoSlots[i] = new TileEntityFactory.ProcessInfo(i, inputSlot, outputSlot, secondaryOutputSlot);
      }
   }

   @Override
   public boolean isValidInputItem(@NotNull ItemStack stack) {
      return this.containsRecipe(stack);
   }

   protected int getNeededInput(SawmillRecipe recipe, ItemStack inputStack) {
      return MathUtils.clampToInt(recipe.getInput().getNeededAmount(inputStack));
   }

   @Override
   protected boolean isCachedRecipeValid(@Nullable CachedRecipe<SawmillRecipe> cached, @NotNull ItemStack stack) {
      return cached != null && cached.getRecipe().getInput().testType(stack);
   }

   protected SawmillRecipe findRecipe(
      int process, @NotNull ItemStack fallbackInput, @NotNull IInventorySlot outputSlot, @Nullable IInventorySlot secondaryOutputSlot
   ) {
      ItemStack output = outputSlot.getStack();
      ItemStack extra = secondaryOutputSlot == null ? ItemStack.f_41583_ : secondaryOutputSlot.getStack();
      return this.getRecipeType().getInputCache().findTypeBasedRecipe(this.f_58857_, fallbackInput, recipe -> {
         SawmillRecipe.ChanceOutput chanceOutput = recipe.getOutput(fallbackInput);
         if (InventoryUtils.areItemsStackable(chanceOutput.getMainOutput(), output)) {
            if (extra.m_41619_()) {
               return true;
            } else {
               ItemStack secondaryOutput = chanceOutput.getMaxSecondaryOutput();
               return secondaryOutput.m_41619_() || ItemHandlerHelper.canItemStacksStack(secondaryOutput, extra);
            }
         } else {
            return false;
         }
      });
   }

   @NotNull
   @Override
   public IMekanismRecipeTypeProvider<SawmillRecipe, InputRecipeCache.SingleItem<SawmillRecipe>> getRecipeType() {
      return MekanismRecipeType.SAWING;
   }

   @Nullable
   public SawmillRecipe getRecipe(int cacheIndex) {
      return this.findFirstRecipe(this.inputHandlers[cacheIndex]);
   }

   @NotNull
   public CachedRecipe<SawmillRecipe> createNewCachedRecipe(@NotNull SawmillRecipe recipe, int cacheIndex) {
      return OneInputCachedRecipe.sawing(recipe, this.recheckAllRecipeErrors[cacheIndex], this.inputHandlers[cacheIndex], this.outputHandlers[cacheIndex])
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
      if (upgradeData instanceof SawmillUpgradeData) {
         super.parseUpgradeData(upgradeData);
      } else {
         Mekanism.logger.warn("Unhandled upgrade data.", new Throwable());
      }
   }

   @NotNull
   public SawmillUpgradeData getUpgradeData() {
      return new SawmillUpgradeData(
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

   @ComputerMethod
   ItemStack getSecondaryOutput(int process) throws ComputerException {
      this.validateValidProcess(process);
      IInventorySlot secondaryOutputSlot = this.processInfoSlots[process].secondaryOutputSlot();
      return secondaryOutputSlot == null ? ItemStack.f_41583_ : secondaryOutputSlot.getStack();
   }
}
