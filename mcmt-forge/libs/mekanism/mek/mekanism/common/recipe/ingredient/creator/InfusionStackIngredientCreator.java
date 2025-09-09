package mekanism.common.recipe.ingredient.creator;

import java.util.Objects;
import java.util.stream.Stream;
import mekanism.api.annotations.NothingNullByDefault;
import mekanism.api.chemical.ChemicalTags;
import mekanism.api.chemical.infuse.InfuseType;
import mekanism.api.chemical.infuse.InfusionStack;
import mekanism.api.providers.IChemicalProvider;
import mekanism.api.recipes.ingredients.ChemicalStackIngredient;
import mekanism.common.recipe.ingredient.chemical.ChemicalIngredientDeserializer;
import mekanism.common.recipe.ingredient.chemical.ChemicalIngredientInfo;
import mekanism.common.recipe.ingredient.chemical.SingleChemicalStackIngredient;
import mekanism.common.recipe.ingredient.chemical.TaggedChemicalStackIngredient;
import net.minecraft.tags.TagKey;

@NothingNullByDefault
public class InfusionStackIngredientCreator extends ChemicalStackIngredientCreator<InfuseType, InfusionStack, ChemicalStackIngredient.InfusionStackIngredient> {
   public static final InfusionStackIngredientCreator INSTANCE = new InfusionStackIngredientCreator();

   private InfusionStackIngredientCreator() {
   }

   @Override
   protected ChemicalIngredientDeserializer<InfuseType, InfusionStack, ChemicalStackIngredient.InfusionStackIngredient> getDeserializer() {
      return ChemicalIngredientDeserializer.INFUSION;
   }

   public ChemicalStackIngredient.InfusionStackIngredient from(IChemicalProvider<InfuseType> provider, long amount) {
      Objects.requireNonNull(provider, "InfusionStackIngredients cannot be created from a null chemical provider.");
      InfuseType infuseType = provider.getChemical();
      this.assertNonEmpty(infuseType);
      this.assertPositiveAmount(amount);
      return new InfusionStackIngredientCreator.SingleInfusionStackIngredient(infuseType.getStack(amount));
   }

   public ChemicalStackIngredient.InfusionStackIngredient from(TagKey<InfuseType> tag, long amount) {
      Objects.requireNonNull(tag, "InfusionStackIngredients cannot be created from a null tag.");
      this.assertPositiveAmount(amount);
      return new InfusionStackIngredientCreator.TaggedInfusionStackIngredient(tag, amount);
   }

   public ChemicalStackIngredient.InfusionStackIngredient from(Stream<ChemicalStackIngredient.InfusionStackIngredient> ingredients) {
      return this.createMulti(ingredients.toArray(ChemicalStackIngredient.InfusionStackIngredient[]::new));
   }

   public static class SingleInfusionStackIngredient
      extends SingleChemicalStackIngredient<InfuseType, InfusionStack>
      implements ChemicalStackIngredient.InfusionStackIngredient {
      private SingleInfusionStackIngredient(InfusionStack stack) {
         super(stack);
      }

      @Override
      protected ChemicalIngredientInfo<InfuseType, InfusionStack> getIngredientInfo() {
         return ChemicalIngredientInfo.INFUSION;
      }
   }

   public static class TaggedInfusionStackIngredient
      extends TaggedChemicalStackIngredient<InfuseType, InfusionStack>
      implements ChemicalStackIngredient.InfusionStackIngredient {
      private TaggedInfusionStackIngredient(TagKey<InfuseType> tag, long amount) {
         super(ChemicalTags.INFUSE_TYPE, tag, amount);
      }

      @Override
      protected ChemicalIngredientInfo<InfuseType, InfusionStack> getIngredientInfo() {
         return ChemicalIngredientInfo.INFUSION;
      }
   }
}
