package mekanism.api.integration.jei;

import mekanism.api.chemical.gas.GasStack;
import mekanism.api.chemical.infuse.InfusionStack;
import mekanism.api.chemical.pigment.PigmentStack;
import mekanism.api.chemical.slurry.SlurryStack;
import mezz.jei.api.ingredients.IIngredientHelper;

public interface IMekanismJEIHelper {
   IIngredientHelper<GasStack> getGasStackHelper();

   IIngredientHelper<InfusionStack> getInfusionStackHelper();

   IIngredientHelper<PigmentStack> getPigmentStackHelper();

   IIngredientHelper<SlurryStack> getSlurryStackHelper();
}
