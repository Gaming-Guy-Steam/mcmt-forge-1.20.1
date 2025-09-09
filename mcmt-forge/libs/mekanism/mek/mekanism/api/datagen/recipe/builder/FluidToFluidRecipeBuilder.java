package mekanism.api.datagen.recipe.builder;

import com.google.gson.JsonObject;
import mekanism.api.SerializerHelper;
import mekanism.api.annotations.NothingNullByDefault;
import mekanism.api.datagen.recipe.MekanismRecipeBuilder;
import mekanism.api.recipes.ingredients.FluidStackIngredient;
import net.minecraft.resources.ResourceLocation;
import net.minecraftforge.fluids.FluidStack;
import org.jetbrains.annotations.NotNull;

@NothingNullByDefault
public class FluidToFluidRecipeBuilder extends MekanismRecipeBuilder<FluidToFluidRecipeBuilder> {
   private final FluidStackIngredient input;
   private final FluidStack output;

   protected FluidToFluidRecipeBuilder(FluidStackIngredient input, FluidStack output) {
      super(mekSerializer("evaporating"));
      this.input = input;
      this.output = output;
   }

   public static FluidToFluidRecipeBuilder evaporating(FluidStackIngredient input, FluidStack output) {
      if (output.isEmpty()) {
         throw new IllegalArgumentException("This evaporating recipe requires a non empty fluid output.");
      } else {
         return new FluidToFluidRecipeBuilder(input, output);
      }
   }

   protected FluidToFluidRecipeBuilder.FluidToFluidRecipeResult getResult(ResourceLocation id) {
      return new FluidToFluidRecipeBuilder.FluidToFluidRecipeResult(id);
   }

   public class FluidToFluidRecipeResult extends MekanismRecipeBuilder<FluidToFluidRecipeBuilder>.RecipeResult {
      protected FluidToFluidRecipeResult(ResourceLocation id) {
         super(id);
      }

      public void m_7917_(@NotNull JsonObject json) {
         json.add("input", FluidToFluidRecipeBuilder.this.input.serialize());
         json.add("output", SerializerHelper.serializeFluidStack(FluidToFluidRecipeBuilder.this.output));
      }
   }
}
