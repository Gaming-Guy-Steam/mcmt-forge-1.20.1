package mekanism.api.datagen.recipe.builder;

import com.google.gson.JsonObject;
import java.util.function.Consumer;
import mekanism.api.SerializerHelper;
import mekanism.api.annotations.NothingNullByDefault;
import mekanism.api.chemical.Chemical;
import mekanism.api.chemical.ChemicalStack;
import mekanism.api.chemical.gas.Gas;
import mekanism.api.chemical.gas.GasStack;
import mekanism.api.chemical.infuse.InfuseType;
import mekanism.api.chemical.infuse.InfusionStack;
import mekanism.api.chemical.pigment.Pigment;
import mekanism.api.chemical.pigment.PigmentStack;
import mekanism.api.datagen.recipe.MekanismRecipeBuilder;
import mekanism.api.recipes.ingredients.ChemicalStackIngredient;
import mekanism.api.recipes.ingredients.ItemStackIngredient;
import net.minecraft.data.recipes.FinishedRecipe;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
import org.jetbrains.annotations.NotNull;

@NothingNullByDefault
public class ItemStackChemicalToItemStackRecipeBuilder<CHEMICAL extends Chemical<CHEMICAL>, STACK extends ChemicalStack<CHEMICAL>, INGREDIENT extends ChemicalStackIngredient<CHEMICAL, STACK>>
   extends MekanismRecipeBuilder<ItemStackChemicalToItemStackRecipeBuilder<CHEMICAL, STACK, INGREDIENT>> {
   private final ItemStackIngredient itemInput;
   private final INGREDIENT chemicalInput;
   private final ItemStack output;

   protected ItemStackChemicalToItemStackRecipeBuilder(
      ResourceLocation serializerName, ItemStackIngredient itemInput, INGREDIENT chemicalInput, ItemStack output
   ) {
      super(serializerName);
      this.itemInput = itemInput;
      this.chemicalInput = chemicalInput;
      this.output = output;
   }

   public static ItemStackChemicalToItemStackRecipeBuilder<Gas, GasStack, ChemicalStackIngredient.GasStackIngredient> compressing(
      ItemStackIngredient itemInput, ChemicalStackIngredient.GasStackIngredient gasInput, ItemStack output
   ) {
      if (output.m_41619_()) {
         throw new IllegalArgumentException("This compressing recipe requires a non empty item output.");
      } else {
         return new ItemStackChemicalToItemStackRecipeBuilder<>(mekSerializer("compressing"), itemInput, (INGREDIENT)gasInput, output);
      }
   }

   public static ItemStackChemicalToItemStackRecipeBuilder<Gas, GasStack, ChemicalStackIngredient.GasStackIngredient> purifying(
      ItemStackIngredient itemInput, ChemicalStackIngredient.GasStackIngredient gasInput, ItemStack output
   ) {
      if (output.m_41619_()) {
         throw new IllegalArgumentException("This purifying recipe requires a non empty item output.");
      } else {
         return new ItemStackChemicalToItemStackRecipeBuilder<>(mekSerializer("purifying"), itemInput, (INGREDIENT)gasInput, output);
      }
   }

   public static ItemStackChemicalToItemStackRecipeBuilder<Gas, GasStack, ChemicalStackIngredient.GasStackIngredient> injecting(
      ItemStackIngredient itemInput, ChemicalStackIngredient.GasStackIngredient gasInput, ItemStack output
   ) {
      if (output.m_41619_()) {
         throw new IllegalArgumentException("This injecting recipe requires a non empty item output.");
      } else {
         return new ItemStackChemicalToItemStackRecipeBuilder<>(mekSerializer("injecting"), itemInput, (INGREDIENT)gasInput, output);
      }
   }

   public static ItemStackChemicalToItemStackRecipeBuilder<InfuseType, InfusionStack, ChemicalStackIngredient.InfusionStackIngredient> metallurgicInfusing(
      ItemStackIngredient itemInput, ChemicalStackIngredient.InfusionStackIngredient infusionInput, ItemStack output
   ) {
      if (output.m_41619_()) {
         throw new IllegalArgumentException("This metallurgic infusing recipe requires a non empty output.");
      } else {
         return new ItemStackChemicalToItemStackRecipeBuilder<>(mekSerializer("metallurgic_infusing"), itemInput, (INGREDIENT)infusionInput, output);
      }
   }

   public static ItemStackChemicalToItemStackRecipeBuilder<Pigment, PigmentStack, ChemicalStackIngredient.PigmentStackIngredient> painting(
      ItemStackIngredient itemInput, ChemicalStackIngredient.PigmentStackIngredient pigmentInput, ItemStack output
   ) {
      if (output.m_41619_()) {
         throw new IllegalArgumentException("This painting recipe requires a non empty item output.");
      } else {
         return new ItemStackChemicalToItemStackRecipeBuilder<>(mekSerializer("painting"), itemInput, (INGREDIENT)pigmentInput, output);
      }
   }

   protected ItemStackChemicalToItemStackRecipeBuilder<CHEMICAL, STACK, INGREDIENT>.ItemStackChemicalToItemStackRecipeResult getResult(ResourceLocation id) {
      return new ItemStackChemicalToItemStackRecipeBuilder.ItemStackChemicalToItemStackRecipeResult(id);
   }

   public void build(Consumer<FinishedRecipe> consumer) {
      this.build(consumer, this.output.m_41720_());
   }

   public class ItemStackChemicalToItemStackRecipeResult
      extends MekanismRecipeBuilder<ItemStackChemicalToItemStackRecipeBuilder<CHEMICAL, STACK, INGREDIENT>>.RecipeResult {
      protected ItemStackChemicalToItemStackRecipeResult(ResourceLocation id) {
         super(id);
      }

      public void m_7917_(@NotNull JsonObject json) {
         json.add("itemInput", ItemStackChemicalToItemStackRecipeBuilder.this.itemInput.serialize());
         json.add("chemicalInput", ItemStackChemicalToItemStackRecipeBuilder.this.chemicalInput.serialize());
         json.add("output", SerializerHelper.serializeItemStack(ItemStackChemicalToItemStackRecipeBuilder.this.output));
      }
   }
}
