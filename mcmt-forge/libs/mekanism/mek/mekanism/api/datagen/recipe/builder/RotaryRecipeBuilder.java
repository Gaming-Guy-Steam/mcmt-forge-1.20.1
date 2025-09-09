package mekanism.api.datagen.recipe.builder;

import com.google.gson.JsonObject;
import mekanism.api.SerializerHelper;
import mekanism.api.annotations.NothingNullByDefault;
import mekanism.api.chemical.gas.GasStack;
import mekanism.api.datagen.recipe.MekanismRecipeBuilder;
import mekanism.api.recipes.ingredients.ChemicalStackIngredient;
import mekanism.api.recipes.ingredients.FluidStackIngredient;
import net.minecraft.resources.ResourceLocation;
import net.minecraftforge.fluids.FluidStack;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

@NothingNullByDefault
public class RotaryRecipeBuilder extends MekanismRecipeBuilder<RotaryRecipeBuilder> {
   private final RotaryRecipeBuilder.RecipeDirection direction;
   @Nullable
   private final ChemicalStackIngredient.GasStackIngredient gasInput;
   @Nullable
   private final FluidStackIngredient fluidInput;
   private final FluidStack fluidOutput;
   private final GasStack gasOutput;

   protected RotaryRecipeBuilder(
      @Nullable FluidStackIngredient fluidInput,
      @Nullable ChemicalStackIngredient.GasStackIngredient gasInput,
      GasStack gasOutput,
      FluidStack fluidOutput,
      RotaryRecipeBuilder.RecipeDirection direction
   ) {
      super(mekSerializer("rotary"));
      this.direction = direction;
      this.gasInput = gasInput;
      this.fluidInput = fluidInput;
      this.gasOutput = gasOutput;
      this.fluidOutput = fluidOutput;
   }

   public static RotaryRecipeBuilder rotary(FluidStackIngredient fluidInput, GasStack gasOutput) {
      if (gasOutput.isEmpty()) {
         throw new IllegalArgumentException("This rotary condensentrator recipe requires a non empty gas output.");
      } else {
         return new RotaryRecipeBuilder(fluidInput, null, gasOutput, FluidStack.EMPTY, RotaryRecipeBuilder.RecipeDirection.FLUID_TO_GAS);
      }
   }

   public static RotaryRecipeBuilder rotary(ChemicalStackIngredient.GasStackIngredient gasInput, FluidStack fluidOutput) {
      if (fluidOutput.isEmpty()) {
         throw new IllegalArgumentException("This rotary condensentrator recipe requires a non empty fluid output.");
      } else {
         return new RotaryRecipeBuilder(null, gasInput, GasStack.EMPTY, fluidOutput, RotaryRecipeBuilder.RecipeDirection.GAS_TO_FLUID);
      }
   }

   public static RotaryRecipeBuilder rotary(
      FluidStackIngredient fluidInput, ChemicalStackIngredient.GasStackIngredient gasInput, GasStack gasOutput, FluidStack fluidOutput
   ) {
      if (!gasOutput.isEmpty() && !fluidOutput.isEmpty()) {
         return new RotaryRecipeBuilder(fluidInput, gasInput, gasOutput, fluidOutput, RotaryRecipeBuilder.RecipeDirection.BOTH);
      } else {
         throw new IllegalArgumentException("This rotary condensentrator recipe requires non empty gas and fluid outputs.");
      }
   }

   protected RotaryRecipeBuilder.RotaryRecipeResult getResult(ResourceLocation id) {
      return new RotaryRecipeBuilder.RotaryRecipeResult(id);
   }

   private static enum RecipeDirection {
      FLUID_TO_GAS(true, false),
      GAS_TO_FLUID(false, true),
      BOTH(true, true);

      private final boolean hasFluidToGas;
      private final boolean hasGasToFluid;

      private RecipeDirection(boolean hasFluidToGas, boolean hasGasToFluid) {
         this.hasFluidToGas = hasFluidToGas;
         this.hasGasToFluid = hasGasToFluid;
      }
   }

   public class RotaryRecipeResult extends MekanismRecipeBuilder<RotaryRecipeBuilder>.RecipeResult {
      protected RotaryRecipeResult(ResourceLocation id) {
         super(id);
      }

      public void m_7917_(@NotNull JsonObject json) {
         if (RotaryRecipeBuilder.this.direction.hasFluidToGas && RotaryRecipeBuilder.this.fluidInput != null) {
            json.add("fluidInput", RotaryRecipeBuilder.this.fluidInput.serialize());
            json.add("gasOutput", SerializerHelper.serializeGasStack(RotaryRecipeBuilder.this.gasOutput));
         }

         if (RotaryRecipeBuilder.this.direction.hasGasToFluid && RotaryRecipeBuilder.this.gasInput != null) {
            json.add("gasInput", RotaryRecipeBuilder.this.gasInput.serialize());
            json.add("fluidOutput", SerializerHelper.serializeFluidStack(RotaryRecipeBuilder.this.fluidOutput));
         }
      }
   }
}
