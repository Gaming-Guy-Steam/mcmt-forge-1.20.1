package mekanism.common.integration.crafttweaker.ingredient;

import com.blamejared.crafttweaker.api.annotation.ZenRegister;
import com.blamejared.crafttweaker_annotations.annotations.NativeMethod;
import com.blamejared.crafttweaker_annotations.annotations.NativeTypeRegistration;
import mekanism.api.chemical.Chemical;
import mekanism.api.recipes.ingredients.ChemicalStackIngredient;

@ZenRegister
@NativeMethod(
   name = "testType",
   parameters = {Chemical.class}
)
@NativeTypeRegistration(
   value = ChemicalStackIngredient.class,
   zenCodeName = "mods.mekanism.api.ingredient.ChemicalStackIngredient"
)
public class CrTChemicalStackIngredient {
   private CrTChemicalStackIngredient() {
   }
}
