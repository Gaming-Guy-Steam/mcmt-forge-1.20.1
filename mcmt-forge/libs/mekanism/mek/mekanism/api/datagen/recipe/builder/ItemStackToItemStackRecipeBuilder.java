package mekanism.api.datagen.recipe.builder;

import com.google.gson.JsonObject;
import java.util.function.Consumer;
import mekanism.api.SerializerHelper;
import mekanism.api.annotations.NothingNullByDefault;
import mekanism.api.datagen.recipe.MekanismRecipeBuilder;
import mekanism.api.recipes.ingredients.ItemStackIngredient;
import net.minecraft.data.recipes.FinishedRecipe;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
import org.jetbrains.annotations.NotNull;

@NothingNullByDefault
public class ItemStackToItemStackRecipeBuilder extends MekanismRecipeBuilder<ItemStackToItemStackRecipeBuilder> {
   private final ItemStackIngredient input;
   private final ItemStack output;

   protected ItemStackToItemStackRecipeBuilder(ItemStackIngredient input, ItemStack output, ResourceLocation serializerName) {
      super(serializerName);
      this.input = input;
      this.output = output;
   }

   public static ItemStackToItemStackRecipeBuilder crushing(ItemStackIngredient input, ItemStack output) {
      if (output.m_41619_()) {
         throw new IllegalArgumentException("This crushing recipe requires a non empty item output.");
      } else {
         return new ItemStackToItemStackRecipeBuilder(input, output, mekSerializer("crushing"));
      }
   }

   public static ItemStackToItemStackRecipeBuilder enriching(ItemStackIngredient input, ItemStack output) {
      if (output.m_41619_()) {
         throw new IllegalArgumentException("This enriching recipe requires a non empty item output.");
      } else {
         return new ItemStackToItemStackRecipeBuilder(input, output, mekSerializer("enriching"));
      }
   }

   public static ItemStackToItemStackRecipeBuilder smelting(ItemStackIngredient input, ItemStack output) {
      if (output.m_41619_()) {
         throw new IllegalArgumentException("This smelting recipe requires a non empty item output.");
      } else {
         return new ItemStackToItemStackRecipeBuilder(input, output, mekSerializer("smelting"));
      }
   }

   protected ItemStackToItemStackRecipeBuilder.ItemStackToItemStackRecipeResult getResult(ResourceLocation id) {
      return new ItemStackToItemStackRecipeBuilder.ItemStackToItemStackRecipeResult(id);
   }

   public void build(Consumer<FinishedRecipe> consumer) {
      this.build(consumer, this.output.m_41720_());
   }

   public class ItemStackToItemStackRecipeResult extends MekanismRecipeBuilder<ItemStackToItemStackRecipeBuilder>.RecipeResult {
      protected ItemStackToItemStackRecipeResult(ResourceLocation id) {
         super(id);
      }

      public void m_7917_(@NotNull JsonObject json) {
         json.add("input", ItemStackToItemStackRecipeBuilder.this.input.serialize());
         json.add("output", SerializerHelper.serializeItemStack(ItemStackToItemStackRecipeBuilder.this.output));
      }
   }
}
