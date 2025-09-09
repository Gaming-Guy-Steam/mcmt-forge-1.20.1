package mekanism.api.datagen.recipe.builder;

import com.google.gson.JsonObject;
import mekanism.api.SerializerHelper;
import mekanism.api.annotations.NothingNullByDefault;
import mekanism.api.datagen.recipe.MekanismRecipeBuilder;
import mekanism.api.recipes.ingredients.ItemStackIngredient;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
import org.jetbrains.annotations.NotNull;

@NothingNullByDefault
public class SawmillRecipeBuilder extends MekanismRecipeBuilder<SawmillRecipeBuilder> {
   private final SawmillRecipeBuilder.OutputType outputType;
   private final ItemStackIngredient input;
   private final ItemStack mainOutput;
   private final ItemStack secondaryOutput;
   private final double secondaryChance;

   protected SawmillRecipeBuilder(
      ItemStackIngredient input, ItemStack mainOutput, ItemStack secondaryOutput, double secondaryChance, SawmillRecipeBuilder.OutputType outputType
   ) {
      super(mekSerializer("sawing"));
      this.outputType = outputType;
      this.input = input;
      this.mainOutput = mainOutput;
      this.secondaryOutput = secondaryOutput;
      this.secondaryChance = secondaryChance;
   }

   public static SawmillRecipeBuilder sawing(ItemStackIngredient input, ItemStack mainOutput) {
      if (mainOutput.m_41619_()) {
         throw new IllegalArgumentException("This sawing recipe requires a non empty output.");
      } else {
         return new SawmillRecipeBuilder(input, mainOutput, ItemStack.f_41583_, 0.0, SawmillRecipeBuilder.OutputType.PRIMARY);
      }
   }

   public static SawmillRecipeBuilder sawing(ItemStackIngredient input, ItemStack secondaryOutput, double secondaryChance) {
      if (secondaryOutput.m_41619_()) {
         throw new IllegalArgumentException("This sawing recipe requires a non empty secondary output.");
      } else if (secondaryChance <= 0.0 || secondaryChance > 1.0) {
         throw new IllegalArgumentException("This sawing recipe requires a secondary output chance greater than zero and at most one.");
      } else if (secondaryChance == 1.0) {
         throw new IllegalArgumentException("Sawing recipes with a single 100% change output should specify their output as the main output.");
      } else {
         return new SawmillRecipeBuilder(input, ItemStack.f_41583_, secondaryOutput, secondaryChance, SawmillRecipeBuilder.OutputType.SECONDARY);
      }
   }

   public static SawmillRecipeBuilder sawing(ItemStackIngredient input, ItemStack mainOutput, ItemStack secondaryOutput, double secondaryChance) {
      if (mainOutput.m_41619_() || secondaryOutput.m_41619_()) {
         throw new IllegalArgumentException("This sawing recipe requires a non empty primary, and secondary output.");
      } else if (!(secondaryChance <= 0.0) && !(secondaryChance > 1.0)) {
         return new SawmillRecipeBuilder(input, mainOutput, secondaryOutput, secondaryChance, SawmillRecipeBuilder.OutputType.BOTH);
      } else {
         throw new IllegalArgumentException("This sawing recipe requires a secondary output chance greater than zero and at most one.");
      }
   }

   protected SawmillRecipeBuilder.SawmillRecipeResult getResult(ResourceLocation id) {
      return new SawmillRecipeBuilder.SawmillRecipeResult(id);
   }

   private static enum OutputType {
      PRIMARY(true, false),
      SECONDARY(false, true),
      BOTH(true, true);

      private final boolean hasPrimary;
      private final boolean hasSecondary;

      private OutputType(boolean hasPrimary, boolean hasSecondary) {
         this.hasPrimary = hasPrimary;
         this.hasSecondary = hasSecondary;
      }
   }

   public class SawmillRecipeResult extends MekanismRecipeBuilder<SawmillRecipeBuilder>.RecipeResult {
      protected SawmillRecipeResult(ResourceLocation id) {
         super(id);
      }

      public void m_7917_(@NotNull JsonObject json) {
         json.add("input", SawmillRecipeBuilder.this.input.serialize());
         if (SawmillRecipeBuilder.this.outputType.hasPrimary) {
            json.add("mainOutput", SerializerHelper.serializeItemStack(SawmillRecipeBuilder.this.mainOutput));
         }

         if (SawmillRecipeBuilder.this.outputType.hasSecondary) {
            json.add("secondaryOutput", SerializerHelper.serializeItemStack(SawmillRecipeBuilder.this.secondaryOutput));
            json.addProperty("secondaryChance", SawmillRecipeBuilder.this.secondaryChance);
         }
      }
   }
}
