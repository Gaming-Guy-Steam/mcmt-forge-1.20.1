package mekanism.api.recipes.ingredients.creator;

import java.util.Objects;
import mekanism.api.annotations.NothingNullByDefault;
import mekanism.api.chemical.Chemical;
import mekanism.api.chemical.ChemicalStack;
import mekanism.api.providers.IChemicalProvider;
import mekanism.api.recipes.ingredients.ChemicalStackIngredient;
import net.minecraft.tags.TagKey;

@NothingNullByDefault
public interface IChemicalStackIngredientCreator<CHEMICAL extends Chemical<CHEMICAL>, STACK extends ChemicalStack<CHEMICAL>, INGREDIENT extends ChemicalStackIngredient<CHEMICAL, STACK>>
   extends IIngredientCreator<CHEMICAL, STACK, INGREDIENT> {
   default INGREDIENT from(STACK instance) {
      Objects.requireNonNull(instance, "ChemicalStackIngredients cannot be created from a null ChemicalStack.");
      return this.from(instance.getType(), instance.getAmount());
   }

   default INGREDIENT from(CHEMICAL instance, int amount) {
      return this.from(instance, (long)amount);
   }

   INGREDIENT from(IChemicalProvider<CHEMICAL> var1, long var2);

   default INGREDIENT from(TagKey<CHEMICAL> tag, int amount) {
      return this.from(tag, (long)amount);
   }

   INGREDIENT from(TagKey<CHEMICAL> var1, long var2);
}
