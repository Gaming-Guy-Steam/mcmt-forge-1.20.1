package mekanism.common.tile.machine;

import mekanism.api.chemical.gas.Gas;
import mekanism.api.chemical.gas.GasStack;
import mekanism.api.recipes.ItemStackGasToItemStackRecipe;
import mekanism.common.recipe.IMekanismRecipeTypeProvider;
import mekanism.common.recipe.MekanismRecipeType;
import mekanism.common.recipe.lookup.cache.InputRecipeCache;
import mekanism.common.registries.MekanismBlocks;
import mekanism.common.tile.prefab.TileEntityAdvancedElectricMachine;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.block.state.BlockState;
import org.jetbrains.annotations.NotNull;

public class TileEntityPurificationChamber extends TileEntityAdvancedElectricMachine {
   public TileEntityPurificationChamber(BlockPos pos, BlockState state) {
      super(MekanismBlocks.PURIFICATION_CHAMBER, pos, state, 200);
   }

   @NotNull
   @Override
   public IMekanismRecipeTypeProvider<ItemStackGasToItemStackRecipe, InputRecipeCache.ItemChemical<Gas, GasStack, ItemStackGasToItemStackRecipe>> getRecipeType() {
      return MekanismRecipeType.PURIFYING;
   }

   @Override
   protected boolean useStatisticalMechanics() {
      return true;
   }
}
