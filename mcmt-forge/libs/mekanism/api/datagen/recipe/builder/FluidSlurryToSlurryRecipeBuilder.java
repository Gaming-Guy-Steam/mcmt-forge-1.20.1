package mekanism.api.datagen.recipe.builder;

import com.google.gson.JsonObject;
import mekanism.api.SerializerHelper;
import mekanism.api.annotations.NothingNullByDefault;
import mekanism.api.chemical.slurry.SlurryStack;
import mekanism.api.datagen.recipe.MekanismRecipeBuilder;
import mekanism.api.recipes.ingredients.ChemicalStackIngredient;
import mekanism.api.recipes.ingredients.FluidStackIngredient;
import net.minecraft.resources.ResourceLocation;
import org.jetbrains.annotations.NotNull;

@NothingNullByDefault
public class FluidSlurryToSlurryRecipeBuilder extends MekanismRecipeBuilder<FluidSlurryToSlurryRecipeBuilder> {
   private final ChemicalStackIngredient.SlurryStackIngredient slurryInput;
   private final FluidStackIngredient fluidInput;
   private final SlurryStack output;

   protected FluidSlurryToSlurryRecipeBuilder(FluidStackIngredient fluidInput, ChemicalStackIngredient.SlurryStackIngredient slurryInput, SlurryStack output) {
      super(mekSerializer("washing"));
      this.fluidInput = fluidInput;
      this.slurryInput = slurryInput;
      this.output = output;
   }

   public static FluidSlurryToSlurryRecipeBuilder washing(
      FluidStackIngredient fluidInput, ChemicalStackIngredient.SlurryStackIngredient slurryInput, SlurryStack output
   ) {
      if (output.isEmpty()) {
         throw new IllegalArgumentException("This washing recipe requires a non empty slurry output.");
      } else {
         return new FluidSlurryToSlurryRecipeBuilder(fluidInput, slurryInput, output);
      }
   }

   protected FluidSlurryToSlurryRecipeBuilder.FluidSlurryToSlurryRecipeResult getResult(ResourceLocation id) {
      return new FluidSlurryToSlurryRecipeBuilder.FluidSlurryToSlurryRecipeResult(id);
   }

   public class FluidSlurryToSlurryRecipeResult extends MekanismRecipeBuilder<FluidSlurryToSlurryRecipeBuilder>.RecipeResult {
      protected FluidSlurryToSlurryRecipeResult(ResourceLocation id) {
         super(id);
      }

      public void m_7917_(@NotNull JsonObject json) {
         json.add("fluidInput", FluidSlurryToSlurryRecipeBuilder.this.fluidInput.serialize());
         json.add("slurryInput", FluidSlurryToSlurryRecipeBuilder.this.slurryInput.serialize());
         json.add("output", SerializerHelper.serializeSlurryStack(FluidSlurryToSlurryRecipeBuilder.this.output));
      }
   }
}
