package mekanism.common.tile.factory;

import java.util.List;
import java.util.Set;
import mekanism.api.IContentsListener;
import mekanism.api.providers.IBlockProvider;
import mekanism.api.recipes.MekanismRecipe;
import mekanism.api.recipes.cache.CachedRecipe;
import mekanism.api.recipes.inputs.IInputHandler;
import mekanism.api.recipes.inputs.InputHelper;
import mekanism.api.recipes.outputs.IOutputHandler;
import mekanism.api.recipes.outputs.OutputHelper;
import mekanism.common.capabilities.holder.slot.InventorySlotHelper;
import mekanism.common.inventory.slot.FactoryInputInventorySlot;
import mekanism.common.inventory.slot.OutputInventorySlot;
import mekanism.common.inventory.warning.WarningTracker;
import mekanism.common.tier.FactoryTier;
import net.minecraft.core.BlockPos;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.state.BlockState;

public abstract class TileEntityItemToItemFactory<RECIPE extends MekanismRecipe> extends TileEntityFactory<RECIPE> {
   protected IInputHandler<ItemStack>[] inputHandlers;
   protected IOutputHandler<ItemStack>[] outputHandlers;

   protected TileEntityItemToItemFactory(
      IBlockProvider blockProvider,
      BlockPos pos,
      BlockState state,
      List<CachedRecipe.OperationTracker.RecipeError> errorTypes,
      Set<CachedRecipe.OperationTracker.RecipeError> globalErrorTypes
   ) {
      super(blockProvider, pos, state, errorTypes, globalErrorTypes);
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
         FactoryInputInventorySlot inputSlot = FactoryInputInventorySlot.create(this, i, outputSlot, this.recipeCacheLookupMonitors[i], xPos, 13);
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
         this.inputHandlers[i] = InputHelper.getInputHandler(inputSlot, CachedRecipe.OperationTracker.RecipeError.NOT_ENOUGH_INPUT);
         this.outputHandlers[i] = OutputHelper.getOutputHandler(outputSlot, CachedRecipe.OperationTracker.RecipeError.NOT_ENOUGH_OUTPUT_SPACE);
         this.processInfoSlots[i] = new TileEntityFactory.ProcessInfo(i, inputSlot, outputSlot, null);
      }
   }
}
