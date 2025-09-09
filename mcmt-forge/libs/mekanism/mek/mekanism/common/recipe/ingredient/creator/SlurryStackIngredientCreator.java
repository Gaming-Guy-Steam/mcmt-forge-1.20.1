package mekanism.common.recipe.ingredient.creator;

import java.util.Objects;
import java.util.stream.Stream;
import mekanism.api.annotations.NothingNullByDefault;
import mekanism.api.chemical.ChemicalTags;
import mekanism.api.chemical.slurry.Slurry;
import mekanism.api.chemical.slurry.SlurryStack;
import mekanism.api.providers.IChemicalProvider;
import mekanism.api.recipes.ingredients.ChemicalStackIngredient;
import mekanism.common.recipe.ingredient.chemical.ChemicalIngredientDeserializer;
import mekanism.common.recipe.ingredient.chemical.ChemicalIngredientInfo;
import mekanism.common.recipe.ingredient.chemical.SingleChemicalStackIngredient;
import mekanism.common.recipe.ingredient.chemical.TaggedChemicalStackIngredient;
import net.minecraft.tags.TagKey;

@NothingNullByDefault
public class SlurryStackIngredientCreator extends ChemicalStackIngredientCreator<Slurry, SlurryStack, ChemicalStackIngredient.SlurryStackIngredient> {
   public static final SlurryStackIngredientCreator INSTANCE = new SlurryStackIngredientCreator();

   private SlurryStackIngredientCreator() {
   }

   @Override
   protected ChemicalIngredientDeserializer<Slurry, SlurryStack, ChemicalStackIngredient.SlurryStackIngredient> getDeserializer() {
      return ChemicalIngredientDeserializer.SLURRY;
   }

   public ChemicalStackIngredient.SlurryStackIngredient from(IChemicalProvider<Slurry> provider, long amount) {
      Objects.requireNonNull(provider, "SlurryStackIngredients cannot be created from a null chemical provider.");
      Slurry slurry = provider.getChemical();
      this.assertNonEmpty(slurry);
      this.assertPositiveAmount(amount);
      return new SlurryStackIngredientCreator.SingleSlurryStackIngredient(slurry.getStack(amount));
   }

   public ChemicalStackIngredient.SlurryStackIngredient from(TagKey<Slurry> tag, long amount) {
      Objects.requireNonNull(tag, "SlurryStackIngredients cannot be created from a null tag.");
      this.assertPositiveAmount(amount);
      return new SlurryStackIngredientCreator.TaggedSlurryStackIngredient(tag, amount);
   }

   public ChemicalStackIngredient.SlurryStackIngredient from(Stream<ChemicalStackIngredient.SlurryStackIngredient> ingredients) {
      return this.createMulti(ingredients.toArray(ChemicalStackIngredient.SlurryStackIngredient[]::new));
   }

   public static class SingleSlurryStackIngredient
      extends SingleChemicalStackIngredient<Slurry, SlurryStack>
      implements ChemicalStackIngredient.SlurryStackIngredient {
      private SingleSlurryStackIngredient(SlurryStack stack) {
         super(stack);
      }

      @Override
      protected ChemicalIngredientInfo<Slurry, SlurryStack> getIngredientInfo() {
         return ChemicalIngredientInfo.SLURRY;
      }
   }

   public static class TaggedSlurryStackIngredient
      extends TaggedChemicalStackIngredient<Slurry, SlurryStack>
      implements ChemicalStackIngredient.SlurryStackIngredient {
      private TaggedSlurryStackIngredient(TagKey<Slurry> tag, long amount) {
         super(ChemicalTags.SLURRY, tag, amount);
      }

      @Override
      protected ChemicalIngredientInfo<Slurry, SlurryStack> getIngredientInfo() {
         return ChemicalIngredientInfo.SLURRY;
      }
   }
}
