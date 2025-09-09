package mekanism.api.recipes.ingredients.creator;

import mekanism.api.IMekanismAccess;
import mekanism.api.chemical.ChemicalType;
import mekanism.api.chemical.gas.Gas;
import mekanism.api.chemical.gas.GasStack;
import mekanism.api.chemical.infuse.InfuseType;
import mekanism.api.chemical.infuse.InfusionStack;
import mekanism.api.chemical.pigment.Pigment;
import mekanism.api.chemical.pigment.PigmentStack;
import mekanism.api.chemical.slurry.Slurry;
import mekanism.api.chemical.slurry.SlurryStack;
import mekanism.api.recipes.ingredients.ChemicalStackIngredient;

public class IngredientCreatorAccess {
   private IngredientCreatorAccess() {
   }

   public static IChemicalStackIngredientCreator<?, ?, ?> getCreatorForType(ChemicalType chemicalType) {
      return switch (chemicalType) {
         case GAS -> gas();
         case INFUSION -> infusion();
         case PIGMENT -> pigment();
         case SLURRY -> slurry();
      };
   }

   public static IItemStackIngredientCreator item() {
      return IMekanismAccess.INSTANCE.itemStackIngredientCreator();
   }

   public static IFluidStackIngredientCreator fluid() {
      return IMekanismAccess.INSTANCE.fluidStackIngredientCreator();
   }

   public static IChemicalStackIngredientCreator<Gas, GasStack, ChemicalStackIngredient.GasStackIngredient> gas() {
      return IMekanismAccess.INSTANCE.gasStackIngredientCreator();
   }

   public static IChemicalStackIngredientCreator<InfuseType, InfusionStack, ChemicalStackIngredient.InfusionStackIngredient> infusion() {
      return IMekanismAccess.INSTANCE.infusionStackIngredientCreator();
   }

   public static IChemicalStackIngredientCreator<Pigment, PigmentStack, ChemicalStackIngredient.PigmentStackIngredient> pigment() {
      return IMekanismAccess.INSTANCE.pigmentStackIngredientCreator();
   }

   public static IChemicalStackIngredientCreator<Slurry, SlurryStack, ChemicalStackIngredient.SlurryStackIngredient> slurry() {
      return IMekanismAccess.INSTANCE.slurryStackIngredientCreator();
   }
}
