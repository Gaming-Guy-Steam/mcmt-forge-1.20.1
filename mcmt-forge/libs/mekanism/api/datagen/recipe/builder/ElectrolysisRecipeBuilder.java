package mekanism.api.datagen.recipe.builder;

import com.google.gson.JsonObject;
import mekanism.api.SerializerHelper;
import mekanism.api.annotations.NothingNullByDefault;
import mekanism.api.chemical.gas.GasStack;
import mekanism.api.datagen.recipe.MekanismRecipeBuilder;
import mekanism.api.math.FloatingLong;
import mekanism.api.recipes.ingredients.FluidStackIngredient;
import net.minecraft.resources.ResourceLocation;
import org.jetbrains.annotations.NotNull;

@NothingNullByDefault
public class ElectrolysisRecipeBuilder extends MekanismRecipeBuilder<ElectrolysisRecipeBuilder> {
   private final FluidStackIngredient input;
   private final GasStack leftGasOutput;
   private final GasStack rightGasOutput;
   private FloatingLong energyMultiplier = FloatingLong.ONE;

   protected ElectrolysisRecipeBuilder(FluidStackIngredient input, GasStack leftGasOutput, GasStack rightGasOutput) {
      super(mekSerializer("separating"));
      this.input = input;
      this.leftGasOutput = leftGasOutput;
      this.rightGasOutput = rightGasOutput;
   }

   public static ElectrolysisRecipeBuilder separating(FluidStackIngredient input, GasStack leftGasOutput, GasStack rightGasOutput) {
      if (!leftGasOutput.isEmpty() && !rightGasOutput.isEmpty()) {
         return new ElectrolysisRecipeBuilder(input, leftGasOutput, rightGasOutput);
      } else {
         throw new IllegalArgumentException("This separating recipe requires non empty gas outputs.");
      }
   }

   public ElectrolysisRecipeBuilder energyMultiplier(FloatingLong multiplier) {
      if (multiplier.smallerThan(FloatingLong.ONE)) {
         throw new IllegalArgumentException("Energy multiplier must be greater than or equal to one");
      } else {
         this.energyMultiplier = multiplier;
         return this;
      }
   }

   protected ElectrolysisRecipeBuilder.ElectrolysisRecipeResult getResult(ResourceLocation id) {
      return new ElectrolysisRecipeBuilder.ElectrolysisRecipeResult(id);
   }

   public class ElectrolysisRecipeResult extends MekanismRecipeBuilder<ElectrolysisRecipeBuilder>.RecipeResult {
      protected ElectrolysisRecipeResult(ResourceLocation id) {
         super(id);
      }

      public void m_7917_(@NotNull JsonObject json) {
         json.add("input", ElectrolysisRecipeBuilder.this.input.serialize());
         if (ElectrolysisRecipeBuilder.this.energyMultiplier.greaterThan(FloatingLong.ONE)) {
            json.addProperty("energyMultiplier", ElectrolysisRecipeBuilder.this.energyMultiplier);
         }

         json.add("leftGasOutput", SerializerHelper.serializeGasStack(ElectrolysisRecipeBuilder.this.leftGasOutput));
         json.add("rightGasOutput", SerializerHelper.serializeGasStack(ElectrolysisRecipeBuilder.this.rightGasOutput));
      }
   }
}
