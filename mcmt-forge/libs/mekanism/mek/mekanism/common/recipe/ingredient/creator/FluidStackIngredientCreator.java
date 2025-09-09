package mekanism.common.recipe.ingredient.creator;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParseException;
import com.google.gson.JsonSyntaxException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.function.Predicate;
import java.util.stream.Stream;
import mekanism.api.SerializerHelper;
import mekanism.api.annotations.NothingNullByDefault;
import mekanism.api.recipes.ingredients.FluidStackIngredient;
import mekanism.api.recipes.ingredients.InputIngredient;
import mekanism.api.recipes.ingredients.creator.IFluidStackIngredientCreator;
import mekanism.common.Mekanism;
import mekanism.common.network.BasePacketHandler;
import mekanism.common.recipe.ingredient.IMultiIngredient;
import mekanism.common.tags.TagUtils;
import mekanism.common.util.RegistryUtils;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.FluidTags;
import net.minecraft.tags.TagKey;
import net.minecraft.util.GsonHelper;
import net.minecraft.world.level.material.Fluid;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.tags.ITag;
import net.minecraftforge.registries.tags.ITagManager;
import org.jetbrains.annotations.Nullable;

@NothingNullByDefault
public class FluidStackIngredientCreator implements IFluidStackIngredientCreator {
   public static final FluidStackIngredientCreator INSTANCE = new FluidStackIngredientCreator();

   private FluidStackIngredientCreator() {
   }

   public FluidStackIngredient from(FluidStack instance) {
      Objects.requireNonNull(instance, "FluidStackIngredients cannot be created from a null FluidStack.");
      if (instance.isEmpty()) {
         throw new IllegalArgumentException("FluidStackIngredients cannot be created using the empty stack.");
      } else {
         return new FluidStackIngredientCreator.SingleFluidStackIngredient(instance.copy());
      }
   }

   public FluidStackIngredient from(TagKey<Fluid> tag, int amount) {
      Objects.requireNonNull(tag, "FluidStackIngredients cannot be created from a null tag.");
      if (amount <= 0) {
         throw new IllegalArgumentException("FluidStackIngredients must have an amount of at least one. Received size was: " + amount);
      } else {
         return new FluidStackIngredientCreator.TaggedFluidStackIngredient(tag, amount);
      }
   }

   public FluidStackIngredient read(FriendlyByteBuf buffer) {
      Objects.requireNonNull(buffer, "FluidStackIngredients cannot be read from a null packet buffer.");

      return switch ((FluidStackIngredientCreator.IngredientType)buffer.m_130066_(FluidStackIngredientCreator.IngredientType.class)) {
         case SINGLE -> this.from(FluidStack.readFromPacket(buffer));
         case TAGGED -> this.from(FluidTags.create(buffer.m_130281_()), buffer.m_130242_());
         case MULTI -> this.createMulti(BasePacketHandler.readArray(buffer, FluidStackIngredient[]::new, this::read));
      };
   }

   public FluidStackIngredient deserialize(@Nullable JsonElement json) {
      if (json != null && !json.isJsonNull()) {
         if (json.isJsonArray()) {
            JsonArray jsonArray = json.getAsJsonArray();
            int size = jsonArray.size();
            if (size == 0) {
               throw new JsonSyntaxException("Ingredient array cannot be empty, at least one ingredient must be defined.");
            }

            if (size > 1) {
               FluidStackIngredient[] ingredients = new FluidStackIngredient[size];

               for (int i = 0; i < size; i++) {
                  ingredients[i] = this.deserialize(jsonArray.get(i));
               }

               return this.createMulti(ingredients);
            }

            json = jsonArray.get(0);
         }

         if (!json.isJsonObject()) {
            throw new JsonSyntaxException("Expected fluid to be object or array of objects.");
         } else {
            JsonObject jsonObject = json.getAsJsonObject();
            if (jsonObject.has("fluid") && jsonObject.has("tag")) {
               throw new JsonParseException("An ingredient entry is either a tag or an fluid, not both.");
            } else if (jsonObject.has("fluid")) {
               FluidStack stack = SerializerHelper.deserializeFluid(jsonObject);
               if (stack.isEmpty()) {
                  throw new JsonSyntaxException("Unable to create an ingredient from an empty stack.");
               } else {
                  return this.from(stack);
               }
            } else if (!jsonObject.has("tag")) {
               throw new JsonSyntaxException("Expected to receive a resource location representing either a tag or a fluid.");
            } else if (!jsonObject.has("amount")) {
               throw new JsonSyntaxException("Expected to receive a amount that is greater than zero.");
            } else {
               JsonElement count = jsonObject.get("amount");
               if (!GsonHelper.m_13872_(count)) {
                  throw new JsonSyntaxException("Expected amount to be a number greater than zero.");
               } else {
                  int amount = count.getAsJsonPrimitive().getAsInt();
                  if (amount < 1) {
                     throw new JsonSyntaxException("Expected amount to be greater than zero.");
                  } else {
                     ResourceLocation resourceLocation = new ResourceLocation(GsonHelper.m_13906_(jsonObject, "tag"));
                     ITagManager<Fluid> tagManager = TagUtils.manager(ForgeRegistries.FLUIDS);
                     TagKey<Fluid> key = tagManager.createTagKey(resourceLocation);
                     return this.from(key, amount);
                  }
               }
            }
         }
      } else {
         throw new JsonSyntaxException("Ingredient cannot be null.");
      }
   }

   public FluidStackIngredient createMulti(FluidStackIngredient... ingredients) {
      Objects.requireNonNull(ingredients, "Cannot create a multi ingredient out of a null array.");
      if (ingredients.length == 0) {
         throw new IllegalArgumentException("Cannot create a multi ingredient out of no ingredients.");
      } else if (ingredients.length == 1) {
         return ingredients[0];
      } else {
         List<FluidStackIngredient> cleanedIngredients = new ArrayList<>();

         for (FluidStackIngredient ingredient : ingredients) {
            if (ingredient instanceof FluidStackIngredientCreator.MultiFluidStackIngredient multi) {
               Collections.addAll(cleanedIngredients, multi.ingredients);
            } else {
               cleanedIngredients.add(ingredient);
            }
         }

         return new FluidStackIngredientCreator.MultiFluidStackIngredient(cleanedIngredients.toArray(new FluidStackIngredient[0]));
      }
   }

   public FluidStackIngredient from(Stream<FluidStackIngredient> ingredients) {
      return this.createMulti(ingredients.toArray(FluidStackIngredient[]::new));
   }

   private static enum IngredientType {
      SINGLE,
      TAGGED,
      MULTI;
   }

   @NothingNullByDefault
   public static class MultiFluidStackIngredient extends FluidStackIngredient implements IMultiIngredient<FluidStack, FluidStackIngredient> {
      private final FluidStackIngredient[] ingredients;

      private MultiFluidStackIngredient(FluidStackIngredient... ingredients) {
         this.ingredients = ingredients;
      }

      public boolean test(FluidStack stack) {
         return Arrays.stream(this.ingredients).anyMatch(ingredient -> ingredient.test(stack));
      }

      public boolean testType(FluidStack stack) {
         return Arrays.stream(this.ingredients).anyMatch(ingredient -> ingredient.testType(stack));
      }

      public FluidStack getMatchingInstance(FluidStack stack) {
         for (FluidStackIngredient ingredient : this.ingredients) {
            FluidStack matchingInstance = ingredient.getMatchingInstance(stack);
            if (!matchingInstance.isEmpty()) {
               return matchingInstance;
            }
         }

         return FluidStack.EMPTY;
      }

      public long getNeededAmount(FluidStack stack) {
         for (FluidStackIngredient ingredient : this.ingredients) {
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
         for (FluidStackIngredient ingredient : this.ingredients) {
            ingredient.logMissingTags();
         }
      }

      @Override
      public List<FluidStack> getRepresentations() {
         List<FluidStack> representations = new ArrayList<>();

         for (FluidStackIngredient ingredient : this.ingredients) {
            representations.addAll(ingredient.getRepresentations());
         }

         return representations;
      }

      @Override
      public boolean forEachIngredient(Predicate<FluidStackIngredient> checker) {
         boolean result = false;

         for (FluidStackIngredient ingredient : this.ingredients) {
            result |= checker.test(ingredient);
         }

         return result;
      }

      @Override
      public final List<FluidStackIngredient> getIngredients() {
         return List.of(this.ingredients);
      }

      @Override
      public void write(FriendlyByteBuf buffer) {
         buffer.m_130068_(FluidStackIngredientCreator.IngredientType.MULTI);
         BasePacketHandler.writeArray(buffer, this.ingredients, InputIngredient::write);
      }

      @Override
      public JsonElement serialize() {
         JsonArray json = new JsonArray();

         for (FluidStackIngredient ingredient : this.ingredients) {
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
               ? Arrays.equals((Object[])this.ingredients, (Object[])((FluidStackIngredientCreator.MultiFluidStackIngredient)o).ingredients)
               : false;
         }
      }

      @Override
      public int hashCode() {
         return Arrays.hashCode((Object[])this.ingredients);
      }
   }

   @NothingNullByDefault
   public static class SingleFluidStackIngredient extends FluidStackIngredient {
      private final List<FluidStack> representations;
      private final FluidStack fluidInstance;

      private SingleFluidStackIngredient(FluidStack fluidInstance) {
         this.fluidInstance = Objects.requireNonNull(fluidInstance);
         this.representations = Collections.singletonList(this.fluidInstance.copy());
      }

      public boolean test(FluidStack fluidStack) {
         return this.testType(fluidStack) && fluidStack.getAmount() >= this.fluidInstance.getAmount();
      }

      public boolean testType(FluidStack fluidStack) {
         return Objects.requireNonNull(fluidStack).isFluidEqual(this.fluidInstance);
      }

      public FluidStack getMatchingInstance(FluidStack fluidStack) {
         return this.test(fluidStack) ? this.fluidInstance.copy() : FluidStack.EMPTY;
      }

      public long getNeededAmount(FluidStack stack) {
         return this.testType(stack) ? this.fluidInstance.getAmount() : 0L;
      }

      @Override
      public boolean hasNoMatchingInstances() {
         return false;
      }

      @Override
      public void logMissingTags() {
      }

      @Override
      public List<FluidStack> getRepresentations() {
         return this.representations;
      }

      public FluidStack getInputRaw() {
         return this.fluidInstance;
      }

      @Override
      public void write(FriendlyByteBuf buffer) {
         buffer.m_130068_(FluidStackIngredientCreator.IngredientType.SINGLE);
         this.fluidInstance.writeToPacket(buffer);
      }

      @Override
      public JsonElement serialize() {
         JsonObject json = new JsonObject();
         json.addProperty("amount", this.fluidInstance.getAmount());
         json.addProperty("fluid", RegistryUtils.getName(this.fluidInstance.getFluid()).toString());
         if (this.fluidInstance.hasTag()) {
            json.addProperty("nbt", this.fluidInstance.getTag().toString());
         }

         return json;
      }

      @Override
      public boolean equals(Object o) {
         if (this == o) {
            return true;
         } else if (o != null && this.getClass() == o.getClass()) {
            FluidStackIngredientCreator.SingleFluidStackIngredient other = (FluidStackIngredientCreator.SingleFluidStackIngredient)o;
            return this.fluidInstance.isFluidStackIdentical(other.fluidInstance);
         } else {
            return false;
         }
      }

      @Override
      public int hashCode() {
         return this.fluidInstance.hashCode();
      }
   }

   @NothingNullByDefault
   public static class TaggedFluidStackIngredient extends FluidStackIngredient {
      private final ITag<Fluid> tag;
      private final int amount;

      private TaggedFluidStackIngredient(TagKey<Fluid> tag, int amount) {
         this(TagUtils.tag(ForgeRegistries.FLUIDS, tag), amount);
      }

      private TaggedFluidStackIngredient(ITag<Fluid> tag, int amount) {
         this.tag = tag;
         this.amount = amount;
      }

      public boolean test(FluidStack fluidStack) {
         return this.testType(fluidStack) && fluidStack.getAmount() >= this.amount;
      }

      public boolean testType(FluidStack fluidStack) {
         return this.tag.contains(Objects.requireNonNull(fluidStack).getFluid());
      }

      public FluidStack getMatchingInstance(FluidStack fluidStack) {
         return this.test(fluidStack) ? new FluidStack(fluidStack, this.amount) : FluidStack.EMPTY;
      }

      public long getNeededAmount(FluidStack stack) {
         return this.testType(stack) ? this.amount : 0L;
      }

      @Override
      public boolean hasNoMatchingInstances() {
         return this.tag.isEmpty();
      }

      @Override
      public void logMissingTags() {
         if (this.tag.isEmpty()) {
            Mekanism.logger.error("Empty tag: {}", this.tag.getKey());
         }
      }

      @Override
      public List<FluidStack> getRepresentations() {
         List<FluidStack> representations = new ArrayList<>();

         for (Fluid fluid : this.tag) {
            representations.add(new FluidStack(fluid, this.amount));
         }

         return representations;
      }

      public Iterable<Fluid> getRawInput() {
         return this.tag;
      }

      public int getRawAmount() {
         return this.amount;
      }

      public TagKey<Fluid> getTag() {
         return this.tag.getKey();
      }

      @Override
      public void write(FriendlyByteBuf buffer) {
         buffer.m_130068_(FluidStackIngredientCreator.IngredientType.TAGGED);
         buffer.m_130085_(this.tag.getKey().f_203868_());
         buffer.m_130130_(this.amount);
      }

      @Override
      public JsonElement serialize() {
         JsonObject json = new JsonObject();
         json.addProperty("amount", this.amount);
         json.addProperty("tag", this.tag.getKey().f_203868_().toString());
         return json;
      }

      @Override
      public boolean equals(Object o) {
         if (this == o) {
            return true;
         } else if (o != null && this.getClass() == o.getClass()) {
            FluidStackIngredientCreator.TaggedFluidStackIngredient other = (FluidStackIngredientCreator.TaggedFluidStackIngredient)o;
            return this.amount == other.amount && this.tag.equals(other.tag);
         } else {
            return false;
         }
      }

      @Override
      public int hashCode() {
         return Objects.hash(this.tag, this.amount);
      }
   }
}
