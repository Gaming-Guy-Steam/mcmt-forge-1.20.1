package mekanism.common.recipe.ingredient.creator;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonSyntaxException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.function.Predicate;
import java.util.stream.Stream;
import mekanism.api.annotations.NothingNullByDefault;
import mekanism.api.recipes.ingredients.InputIngredient;
import mekanism.api.recipes.ingredients.ItemStackIngredient;
import mekanism.api.recipes.ingredients.creator.IItemStackIngredientCreator;
import mekanism.common.Mekanism;
import mekanism.common.network.BasePacketHandler;
import mekanism.common.recipe.ingredient.IMultiIngredient;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.chat.contents.LiteralContents;
import net.minecraft.util.GsonHelper;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.crafting.Ingredient;
import org.jetbrains.annotations.Nullable;

@NothingNullByDefault
public class ItemStackIngredientCreator implements IItemStackIngredientCreator {
   public static final ItemStackIngredientCreator INSTANCE = new ItemStackIngredientCreator();

   private ItemStackIngredientCreator() {
   }

   @Override
   public ItemStackIngredient from(Ingredient ingredient, int amount) {
      Objects.requireNonNull(ingredient, "ItemStackIngredients cannot be created from a null ingredient.");
      if (ingredient == Ingredient.f_43901_) {
         throw new IllegalArgumentException("ItemStackIngredients cannot be created using the empty ingredient.");
      } else if (amount <= 0) {
         throw new IllegalArgumentException("ItemStackIngredients must have an amount of at least one. Received size was: " + amount);
      } else {
         return new ItemStackIngredientCreator.SingleItemStackIngredient(ingredient, amount);
      }
   }

   public ItemStackIngredient read(FriendlyByteBuf buffer) {
      Objects.requireNonNull(buffer, "ItemStackIngredients cannot be read from a null packet buffer.");

      return switch ((ItemStackIngredientCreator.IngredientType)buffer.m_130066_(ItemStackIngredientCreator.IngredientType.class)) {
         case SINGLE -> this.from(Ingredient.m_43940_(buffer), buffer.m_130242_());
         case MULTI -> this.createMulti(BasePacketHandler.readArray(buffer, ItemStackIngredient[]::new, this::read));
      };
   }

   public ItemStackIngredient deserialize(@Nullable JsonElement json) {
      if (json != null && !json.isJsonNull()) {
         if (json.isJsonArray()) {
            JsonArray jsonArray = json.getAsJsonArray();
            int size = jsonArray.size();
            if (size == 0) {
               throw new JsonSyntaxException("Ingredient array cannot be empty, at least one ingredient must be defined.");
            }

            if (size > 1) {
               ItemStackIngredient[] ingredients = new ItemStackIngredient[size];

               for (int i = 0; i < size; i++) {
                  ingredients[i] = this.deserialize(jsonArray.get(i));
               }

               return this.createMulti(ingredients);
            }

            json = jsonArray.get(0);
         }

         if (!json.isJsonObject()) {
            throw new JsonSyntaxException("Expected item to be object or array of objects.");
         } else {
            JsonObject jsonObject = json.getAsJsonObject();
            int amount = 1;
            if (jsonObject.has("amount")) {
               JsonElement count = jsonObject.get("amount");
               if (!GsonHelper.m_13872_(count)) {
                  throw new JsonSyntaxException("Expected amount to be a number that is one or larger.");
               }

               amount = count.getAsJsonPrimitive().getAsInt();
               if (amount < 1) {
                  throw new JsonSyntaxException("Expected amount to larger than or equal to one.");
               }
            }

            JsonElement jsonelement = (JsonElement)(GsonHelper.m_13885_(jsonObject, "ingredient")
               ? GsonHelper.m_13933_(jsonObject, "ingredient")
               : GsonHelper.m_13930_(jsonObject, "ingredient"));
            Ingredient ingredient = Ingredient.m_43917_(jsonelement);
            return this.from(ingredient, amount);
         }
      } else {
         throw new JsonSyntaxException("Ingredient cannot be null.");
      }
   }

   public ItemStackIngredient createMulti(ItemStackIngredient... ingredients) {
      Objects.requireNonNull(ingredients, "Cannot create a multi ingredient out of a null array.");
      if (ingredients.length == 0) {
         throw new IllegalArgumentException("Cannot create a multi ingredient out of no ingredients.");
      } else if (ingredients.length == 1) {
         return ingredients[0];
      } else {
         List<ItemStackIngredient> cleanedIngredients = new ArrayList<>();

         for (ItemStackIngredient ingredient : ingredients) {
            if (ingredient instanceof ItemStackIngredientCreator.MultiItemStackIngredient multi) {
               Collections.addAll(cleanedIngredients, multi.ingredients);
            } else {
               cleanedIngredients.add(ingredient);
            }
         }

         return new ItemStackIngredientCreator.MultiItemStackIngredient(cleanedIngredients.toArray(new ItemStackIngredient[0]));
      }
   }

   public ItemStackIngredient from(Stream<ItemStackIngredient> ingredients) {
      return this.createMulti(ingredients.toArray(ItemStackIngredient[]::new));
   }

   private static enum IngredientType {
      SINGLE,
      MULTI;
   }

   @NothingNullByDefault
   public static class MultiItemStackIngredient extends ItemStackIngredient implements IMultiIngredient<ItemStack, ItemStackIngredient> {
      private final ItemStackIngredient[] ingredients;

      private MultiItemStackIngredient(ItemStackIngredient... ingredients) {
         this.ingredients = ingredients;
      }

      public boolean test(ItemStack stack) {
         return Arrays.stream(this.ingredients).anyMatch(ingredient -> ingredient.test(stack));
      }

      public boolean testType(ItemStack stack) {
         return Arrays.stream(this.ingredients).anyMatch(ingredient -> ingredient.testType(stack));
      }

      public ItemStack getMatchingInstance(ItemStack stack) {
         for (ItemStackIngredient ingredient : this.ingredients) {
            ItemStack matchingInstance = ingredient.getMatchingInstance(stack);
            if (!matchingInstance.m_41619_()) {
               return matchingInstance;
            }
         }

         return ItemStack.f_41583_;
      }

      public long getNeededAmount(ItemStack stack) {
         for (ItemStackIngredient ingredient : this.ingredients) {
            long amount = ingredient.getNeededAmount(stack);
            if (amount > 0L) {
               return amount;
            }
         }

         return 0L;
      }

      @Override
      public boolean hasNoMatchingInstances() {
         return Arrays.stream(this.ingredients).allMatch(InputIngredient::hasNoMatchingInstances);
      }

      @Override
      public void logMissingTags() {
         for (ItemStackIngredient ingredient : this.ingredients) {
            ingredient.logMissingTags();
         }
      }

      @Override
      public List<ItemStack> getRepresentations() {
         List<ItemStack> representations = new ArrayList<>();

         for (ItemStackIngredient ingredient : this.ingredients) {
            representations.addAll(ingredient.getRepresentations());
         }

         return representations;
      }

      @Override
      public boolean forEachIngredient(Predicate<ItemStackIngredient> checker) {
         boolean result = false;

         for (ItemStackIngredient ingredient : this.ingredients) {
            result |= checker.test(ingredient);
         }

         return result;
      }

      @Override
      public final List<ItemStackIngredient> getIngredients() {
         return List.of(this.ingredients);
      }

      @Override
      public void write(FriendlyByteBuf buffer) {
         buffer.m_130068_(ItemStackIngredientCreator.IngredientType.MULTI);
         BasePacketHandler.writeArray(buffer, this.ingredients, InputIngredient::write);
      }

      @Override
      public JsonElement serialize() {
         JsonArray json = new JsonArray();

         for (ItemStackIngredient ingredient : this.ingredients) {
            json.add(ingredient.serialize());
         }

         return json;
      }

      @Override
      public boolean equals(Object o) {
         if (this == o) {
            return true;
         } else {
            return o != null && this.getClass() == o.getClass()
               ? Arrays.equals((Object[])this.ingredients, (Object[])((ItemStackIngredientCreator.MultiItemStackIngredient)o).ingredients)
               : false;
         }
      }

      @Override
      public int hashCode() {
         return Arrays.hashCode((Object[])this.ingredients);
      }
   }

   @NothingNullByDefault
   public static class SingleItemStackIngredient extends ItemStackIngredient {
      private final Ingredient ingredient;
      private final int amount;

      private SingleItemStackIngredient(Ingredient ingredient, int amount) {
         this.ingredient = Objects.requireNonNull(ingredient);
         this.amount = amount;
      }

      public boolean test(ItemStack stack) {
         return this.testType(stack) && stack.m_41613_() >= this.amount;
      }

      public boolean testType(ItemStack stack) {
         return this.ingredient.test(stack);
      }

      public ItemStack getMatchingInstance(ItemStack stack) {
         return this.test(stack) ? stack.m_255036_(this.amount) : ItemStack.f_41583_;
      }

      public long getNeededAmount(ItemStack stack) {
         return this.testType(stack) ? this.amount : 0L;
      }

      @Override
      public boolean hasNoMatchingInstances() {
         ItemStack[] items = this.ingredient.m_43908_();
         if (items.length == 0) {
            return true;
         } else if (items.length != 1) {
            return false;
         } else {
            ItemStack item = items[0];
            return item.m_41720_() == Items.f_42127_
               && item.m_41786_().m_214077_() instanceof LiteralContents contents
               && contents.f_237368_().startsWith("Empty Tag: ");
         }
      }

      @Override
      public void logMissingTags() {
         if (this.hasNoMatchingInstances()) {
            Mekanism.logger.error("Empty item ingredient: {}", this.ingredient.m_43942_());
         }
      }

      @Override
      public List<ItemStack> getRepresentations() {
         List<ItemStack> representations = new ArrayList<>();

         for (ItemStack stack : this.ingredient.m_43908_()) {
            if (!stack.m_41619_()) {
               representations.add(stack.m_255036_(this.amount));
            }
         }

         return representations;
      }

      public Ingredient getInputRaw() {
         return this.ingredient;
      }

      public int getAmountRaw() {
         return this.amount;
      }

      @Override
      public void write(FriendlyByteBuf buffer) {
         buffer.m_130068_(ItemStackIngredientCreator.IngredientType.SINGLE);
         this.ingredient.m_43923_(buffer);
         buffer.m_130130_(this.amount);
      }

      @Override
      public JsonElement serialize() {
         JsonObject json = new JsonObject();
         if (this.amount > 1) {
            json.addProperty("amount", this.amount);
         }

         json.add("ingredient", this.ingredient.m_43942_());
         return json;
      }
   }
}
