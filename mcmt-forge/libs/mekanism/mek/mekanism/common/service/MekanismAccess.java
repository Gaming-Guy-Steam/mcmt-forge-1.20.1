package mekanism.common.service;

import com.mojang.serialization.Codec;
import mekanism.api.IMekanismAccess;
import mekanism.api.chemical.gas.Gas;
import mekanism.api.chemical.gas.GasStack;
import mekanism.api.chemical.infuse.InfuseType;
import mekanism.api.chemical.infuse.InfusionStack;
import mekanism.api.chemical.pigment.Pigment;
import mekanism.api.chemical.pigment.PigmentStack;
import mekanism.api.chemical.slurry.Slurry;
import mekanism.api.chemical.slurry.SlurryStack;
import mekanism.api.integration.jei.IMekanismJEIHelper;
import mekanism.api.recipes.ingredients.ChemicalStackIngredient;
import mekanism.api.recipes.ingredients.creator.IChemicalStackIngredientCreator;
import mekanism.api.recipes.ingredients.creator.IFluidStackIngredientCreator;
import mekanism.api.recipes.ingredients.creator.IItemStackIngredientCreator;
import mekanism.api.robit.RobitSkin;
import mekanism.client.jei.MekanismJEIHelper;
import mekanism.common.Mekanism;
import mekanism.common.recipe.ingredient.creator.FluidStackIngredientCreator;
import mekanism.common.recipe.ingredient.creator.GasStackIngredientCreator;
import mekanism.common.recipe.ingredient.creator.InfusionStackIngredientCreator;
import mekanism.common.recipe.ingredient.creator.ItemStackIngredientCreator;
import mekanism.common.recipe.ingredient.creator.PigmentStackIngredientCreator;
import mekanism.common.recipe.ingredient.creator.SlurryStackIngredientCreator;
import mekanism.common.registries.MekanismRobitSkins;

public class MekanismAccess implements IMekanismAccess {
   @Override
   public IMekanismJEIHelper jeiHelper() {
      if (Mekanism.hooks.JEILoaded) {
         return MekanismJEIHelper.INSTANCE;
      } else {
         throw new IllegalStateException("JEI is not loaded.");
      }
   }

   @Override
   public Codec<RobitSkin> robitSkinCodec() {
      return MekanismRobitSkins.getDirectCodec();
   }

   @Override
   public IItemStackIngredientCreator itemStackIngredientCreator() {
      return ItemStackIngredientCreator.INSTANCE;
   }

   @Override
   public IFluidStackIngredientCreator fluidStackIngredientCreator() {
      return FluidStackIngredientCreator.INSTANCE;
   }

   @Override
   public IChemicalStackIngredientCreator<Gas, GasStack, ChemicalStackIngredient.GasStackIngredient> gasStackIngredientCreator() {
      return GasStackIngredientCreator.INSTANCE;
   }

   @Override
   public IChemicalStackIngredientCreator<InfuseType, InfusionStack, ChemicalStackIngredient.InfusionStackIngredient> infusionStackIngredientCreator() {
      return InfusionStackIngredientCreator.INSTANCE;
   }

   @Override
   public IChemicalStackIngredientCreator<Pigment, PigmentStack, ChemicalStackIngredient.PigmentStackIngredient> pigmentStackIngredientCreator() {
      return PigmentStackIngredientCreator.INSTANCE;
   }

   @Override
   public IChemicalStackIngredientCreator<Slurry, SlurryStack, ChemicalStackIngredient.SlurryStackIngredient> slurryStackIngredientCreator() {
      return SlurryStackIngredientCreator.INSTANCE;
   }
}
