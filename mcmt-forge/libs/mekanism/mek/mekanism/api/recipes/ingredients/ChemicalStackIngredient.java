package mekanism.api.recipes.ingredients;

import mekanism.api.chemical.Chemical;
import mekanism.api.chemical.ChemicalStack;
import mekanism.api.chemical.gas.Gas;
import mekanism.api.chemical.gas.GasStack;
import mekanism.api.chemical.infuse.InfuseType;
import mekanism.api.chemical.infuse.InfusionStack;
import mekanism.api.chemical.pigment.Pigment;
import mekanism.api.chemical.pigment.PigmentStack;
import mekanism.api.chemical.slurry.Slurry;
import mekanism.api.chemical.slurry.SlurryStack;
import org.jetbrains.annotations.NotNull;

public interface ChemicalStackIngredient<CHEMICAL extends Chemical<CHEMICAL>, STACK extends ChemicalStack<CHEMICAL>> extends InputIngredient<STACK> {
   boolean testType(@NotNull CHEMICAL var1);

   public interface GasStackIngredient extends ChemicalStackIngredient<Gas, GasStack> {
   }

   public interface InfusionStackIngredient extends ChemicalStackIngredient<InfuseType, InfusionStack> {
   }

   public interface PigmentStackIngredient extends ChemicalStackIngredient<Pigment, PigmentStack> {
   }

   public interface SlurryStackIngredient extends ChemicalStackIngredient<Slurry, SlurryStack> {
   }
}
