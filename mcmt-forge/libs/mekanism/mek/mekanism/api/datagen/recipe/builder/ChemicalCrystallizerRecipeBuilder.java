package mekanism.api.datagen.recipe.builder;

import com.google.gson.JsonObject;
import java.util.function.Consumer;
import mekanism.api.SerializerHelper;
import mekanism.api.annotations.NothingNullByDefault;
import mekanism.api.chemical.ChemicalType;
import mekanism.api.datagen.recipe.MekanismRecipeBuilder;
import mekanism.api.recipes.ingredients.ChemicalStackIngredient;
import net.minecraft.data.recipes.FinishedRecipe;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
import org.jetbrains.annotations.NotNull;

@NothingNullByDefault
public class ChemicalCrystallizerRecipeBuilder extends MekanismRecipeBuilder<ChemicalCrystallizerRecipeBuilder> {
   private final ChemicalType chemicalType;
   private final ChemicalStackIngredient<?, ?> input;
   private final ItemStack output;

   protected ChemicalCrystallizerRecipeBuilder(ResourceLocation serializerName, ChemicalStackIngredient<?, ?> input, ItemStack output) {
      super(serializerName);
      this.input = input;
      this.chemicalType = ChemicalType.getTypeFor(input);
      this.output = output;
   }

   public static ChemicalCrystallizerRecipeBuilder crystallizing(ChemicalStackIngredient<?, ?> input, ItemStack output) {
      if (output.m_41619_()) {
         throw new IllegalArgumentException("This crystallizing recipe requires a non empty item output.");
      } else {
         return new ChemicalCrystallizerRecipeBuilder(mekSerializer("crystallizing"), input, output);
      }
   }

   protected ChemicalCrystallizerRecipeBuilder.ChemicalCrystallizerRecipeResult getResult(ResourceLocation id) {
      return new ChemicalCrystallizerRecipeBuilder.ChemicalCrystallizerRecipeResult(id);
   }

   public void build(Consumer<FinishedRecipe> consumer) {
      this.build(consumer, this.output.m_41720_());
   }

   public class ChemicalCrystallizerRecipeResult extends MekanismRecipeBuilder<ChemicalCrystallizerRecipeBuilder>.RecipeResult {
      protected ChemicalCrystallizerRecipeResult(ResourceLocation id) {
         super(id);
      }

      public void m_7917_(@NotNull JsonObject json) {
         json.addProperty("chemicalType", ChemicalCrystallizerRecipeBuilder.this.chemicalType.m_7912_());
         json.add("input", ChemicalCrystallizerRecipeBuilder.this.input.serialize());
         json.add("output", SerializerHelper.serializeItemStack(ChemicalCrystallizerRecipeBuilder.this.output));
      }
   }
}
