package mekanism.api;

import com.mojang.serialization.Codec;
import java.util.ServiceLoader;
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

public interface IMekanismAccess {
   IMekanismAccess INSTANCE = ServiceLoader.load(IMekanismAccess.class)
      .findFirst()
      .orElseThrow(() -> new IllegalStateException("No valid ServiceImpl for IMekanismAccess found"));

   IMekanismJEIHelper jeiHelper();

   Codec<RobitSkin> robitSkinCodec();

   IItemStackIngredientCreator itemStackIngredientCreator();

   IFluidStackIngredientCreator fluidStackIngredientCreator();

   IChemicalStackIngredientCreator<Gas, GasStack, ChemicalStackIngredient.GasStackIngredient> gasStackIngredientCreator();

   IChemicalStackIngredientCreator<InfuseType, InfusionStack, ChemicalStackIngredient.InfusionStackIngredient> infusionStackIngredientCreator();

   IChemicalStackIngredientCreator<Pigment, PigmentStack, ChemicalStackIngredient.PigmentStackIngredient> pigmentStackIngredientCreator();

   IChemicalStackIngredientCreator<Slurry, SlurryStack, ChemicalStackIngredient.SlurryStackIngredient> slurryStackIngredientCreator();
}
