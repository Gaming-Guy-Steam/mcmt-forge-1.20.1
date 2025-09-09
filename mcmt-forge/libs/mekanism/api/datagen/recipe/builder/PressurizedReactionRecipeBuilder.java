package mekanism.api.datagen.recipe.builder;

import com.google.gson.JsonObject;
import mekanism.api.SerializerHelper;
import mekanism.api.annotations.NothingNullByDefault;
import mekanism.api.chemical.gas.GasStack;
import mekanism.api.datagen.recipe.MekanismRecipeBuilder;
import mekanism.api.math.FloatingLong;
import mekanism.api.recipes.ingredients.ChemicalStackIngredient;
import mekanism.api.recipes.ingredients.FluidStackIngredient;
import mekanism.api.recipes.ingredients.ItemStackIngredient;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
import org.jetbrains.annotations.NotNull;

@NothingNullByDefault
public class PressurizedReactionRecipeBuilder extends MekanismRecipeBuilder<PressurizedReactionRecipeBuilder> {
   private final ItemStackIngredient inputSolid;
   private final FluidStackIngredient inputFluid;
   private final ChemicalStackIngredient.GasStackIngredient inputGas;
   private FloatingLong energyRequired = FloatingLong.ZERO;
   private final int duration;
   private final ItemStack outputItem;
   private final GasStack outputGas;

   protected PressurizedReactionRecipeBuilder(
      ItemStackIngredient inputSolid,
      FluidStackIngredient inputFluid,
      ChemicalStackIngredient.GasStackIngredient inputGas,
      int duration,
      ItemStack outputItem,
      GasStack outputGas
   ) {
      super(mekSerializer("reaction"));
      this.inputSolid = inputSolid;
      this.inputFluid = inputFluid;
      this.inputGas = inputGas;
      this.duration = duration;
      this.outputItem = outputItem;
      this.outputGas = outputGas;
   }

   public static PressurizedReactionRecipeBuilder reaction(
      ItemStackIngredient inputSolid, FluidStackIngredient inputFluid, ChemicalStackIngredient.GasStackIngredient inputGas, int duration, ItemStack outputItem
   ) {
      if (outputItem.m_41619_()) {
         throw new IllegalArgumentException("This reaction recipe requires a non empty output item.");
      } else {
         validateDuration(duration);
         return new PressurizedReactionRecipeBuilder(inputSolid, inputFluid, inputGas, duration, outputItem, GasStack.EMPTY);
      }
   }

   public static PressurizedReactionRecipeBuilder reaction(
      ItemStackIngredient inputSolid, FluidStackIngredient inputFluid, ChemicalStackIngredient.GasStackIngredient inputGas, int duration, GasStack outputGas
   ) {
      if (outputGas.isEmpty()) {
         throw new IllegalArgumentException("This reaction recipe requires a non empty output gas.");
      } else {
         validateDuration(duration);
         return new PressurizedReactionRecipeBuilder(inputSolid, inputFluid, inputGas, duration, ItemStack.f_41583_, outputGas);
      }
   }

   public static PressurizedReactionRecipeBuilder reaction(
      ItemStackIngredient inputSolid,
      FluidStackIngredient inputFluid,
      ChemicalStackIngredient.GasStackIngredient inputGas,
      int duration,
      ItemStack outputItem,
      GasStack outputGas
   ) {
      if (!outputItem.m_41619_() && !outputGas.isEmpty()) {
         validateDuration(duration);
         return new PressurizedReactionRecipeBuilder(inputSolid, inputFluid, inputGas, duration, outputItem, outputGas);
      } else {
         throw new IllegalArgumentException("This reaction recipe requires non empty item and gas outputs.");
      }
   }

   private static void validateDuration(int duration) {
      if (duration <= 0) {
         throw new IllegalArgumentException("This reaction recipe must have a positive duration.");
      }
   }

   public PressurizedReactionRecipeBuilder energyRequired(FloatingLong energyRequired) {
      this.energyRequired = energyRequired;
      return this;
   }

   protected PressurizedReactionRecipeBuilder.PressurizedReactionRecipeResult getResult(ResourceLocation id) {
      return new PressurizedReactionRecipeBuilder.PressurizedReactionRecipeResult(id);
   }

   public class PressurizedReactionRecipeResult extends MekanismRecipeBuilder<PressurizedReactionRecipeBuilder>.RecipeResult {
      protected PressurizedReactionRecipeResult(ResourceLocation id) {
         super(id);
      }

      public void m_7917_(@NotNull JsonObject json) {
         json.add("itemInput", PressurizedReactionRecipeBuilder.this.inputSolid.serialize());
         json.add("fluidInput", PressurizedReactionRecipeBuilder.this.inputFluid.serialize());
         json.add("gasInput", PressurizedReactionRecipeBuilder.this.inputGas.serialize());
         if (!PressurizedReactionRecipeBuilder.this.energyRequired.isZero()) {
            json.addProperty("energyRequired", PressurizedReactionRecipeBuilder.this.energyRequired);
         }

         json.addProperty("duration", PressurizedReactionRecipeBuilder.this.duration);
         if (!PressurizedReactionRecipeBuilder.this.outputItem.m_41619_()) {
            json.add("itemOutput", SerializerHelper.serializeItemStack(PressurizedReactionRecipeBuilder.this.outputItem));
         }

         if (!PressurizedReactionRecipeBuilder.this.outputGas.isEmpty()) {
            json.add("gasOutput", SerializerHelper.serializeGasStack(PressurizedReactionRecipeBuilder.this.outputGas));
         }
      }
   }
}
