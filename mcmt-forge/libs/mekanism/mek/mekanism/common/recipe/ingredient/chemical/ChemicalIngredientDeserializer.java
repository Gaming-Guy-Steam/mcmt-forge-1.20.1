package mekanism.common.recipe.ingredient.chemical;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParseException;
import com.google.gson.JsonSyntaxException;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.function.Function;
import java.util.function.IntFunction;
import mekanism.api.SerializerHelper;
import mekanism.api.annotations.NothingNullByDefault;
import mekanism.api.chemical.Chemical;
import mekanism.api.chemical.ChemicalStack;
import mekanism.api.chemical.ChemicalTags;
import mekanism.api.chemical.gas.Gas;
import mekanism.api.chemical.gas.GasStack;
import mekanism.api.chemical.infuse.InfuseType;
import mekanism.api.chemical.infuse.InfusionStack;
import mekanism.api.chemical.pigment.Pigment;
import mekanism.api.chemical.pigment.PigmentStack;
import mekanism.api.chemical.slurry.Slurry;
import mekanism.api.chemical.slurry.SlurryStack;
import mekanism.api.recipes.ingredients.ChemicalStackIngredient;
import mekanism.api.recipes.ingredients.creator.IChemicalStackIngredientCreator;
import mekanism.api.recipes.ingredients.creator.IngredientCreatorAccess;
import mekanism.common.network.BasePacketHandler;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.TagKey;
import net.minecraft.util.GsonHelper;
import net.minecraftforge.registries.tags.ITagManager;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

@NothingNullByDefault
public class ChemicalIngredientDeserializer<CHEMICAL extends Chemical<CHEMICAL>, STACK extends ChemicalStack<CHEMICAL>, INGREDIENT extends ChemicalStackIngredient<CHEMICAL, STACK>> {
   public static final ChemicalIngredientDeserializer<Gas, GasStack, ChemicalStackIngredient.GasStackIngredient> GAS = new ChemicalIngredientDeserializer<>(
      "gas",
      ChemicalIngredientInfo.GAS,
      ChemicalTags.GAS,
      GasStack::readFromPacket,
      SerializerHelper::deserializeGas,
      (IChemicalStackIngredientCreator<Gas, GasStack, INGREDIENT>)IngredientCreatorAccess.gas(),
      (Function<INGREDIENT[], INGREDIENT>)(MultiChemicalStackIngredient.MultiGasStackIngredient::new),
      (IntFunction<INGREDIENT[]>)(ChemicalStackIngredient.GasStackIngredient[]::new)
   );
   public static final ChemicalIngredientDeserializer<InfuseType, InfusionStack, ChemicalStackIngredient.InfusionStackIngredient> INFUSION = new ChemicalIngredientDeserializer<>(
      "infuse type",
      ChemicalIngredientInfo.INFUSION,
      ChemicalTags.INFUSE_TYPE,
      InfusionStack::readFromPacket,
      SerializerHelper::deserializeInfuseType,
      (IChemicalStackIngredientCreator<InfuseType, InfusionStack, INGREDIENT>)IngredientCreatorAccess.infusion(),
      (Function<INGREDIENT[], INGREDIENT>)(MultiChemicalStackIngredient.MultiInfusionStackIngredient::new),
      (IntFunction<INGREDIENT[]>)(ChemicalStackIngredient.InfusionStackIngredient[]::new)
   );
   public static final ChemicalIngredientDeserializer<Pigment, PigmentStack, ChemicalStackIngredient.PigmentStackIngredient> PIGMENT = new ChemicalIngredientDeserializer<>(
      "pigment",
      ChemicalIngredientInfo.PIGMENT,
      ChemicalTags.PIGMENT,
      PigmentStack::readFromPacket,
      SerializerHelper::deserializePigment,
      (IChemicalStackIngredientCreator<Pigment, PigmentStack, INGREDIENT>)IngredientCreatorAccess.pigment(),
      (Function<INGREDIENT[], INGREDIENT>)(MultiChemicalStackIngredient.MultiPigmentStackIngredient::new),
      (IntFunction<INGREDIENT[]>)(ChemicalStackIngredient.PigmentStackIngredient[]::new)
   );
   public static final ChemicalIngredientDeserializer<Slurry, SlurryStack, ChemicalStackIngredient.SlurryStackIngredient> SLURRY = new ChemicalIngredientDeserializer<>(
      "slurry",
      ChemicalIngredientInfo.SLURRY,
      ChemicalTags.SLURRY,
      SlurryStack::readFromPacket,
      SerializerHelper::deserializeSlurry,
      (IChemicalStackIngredientCreator<Slurry, SlurryStack, INGREDIENT>)IngredientCreatorAccess.slurry(),
      (Function<INGREDIENT[], INGREDIENT>)(MultiChemicalStackIngredient.MultiSlurryStackIngredient::new),
      (IntFunction<INGREDIENT[]>)(ChemicalStackIngredient.SlurryStackIngredient[]::new)
   );
   private final ChemicalTags<CHEMICAL> tags;
   private final Function<FriendlyByteBuf, STACK> fromPacket;
   private final Function<JsonObject, STACK> stackParser;
   private final ChemicalIngredientInfo<CHEMICAL, STACK> info;
   private final IChemicalStackIngredientCreator<CHEMICAL, STACK, INGREDIENT> ingredientCreator;
   private final IntFunction<INGREDIENT[]> arrayCreator;
   private final Function<INGREDIENT[], INGREDIENT> multiCreator;
   private final String name;

   private ChemicalIngredientDeserializer(
      String name,
      ChemicalIngredientInfo<CHEMICAL, STACK> info,
      ChemicalTags<CHEMICAL> tags,
      Function<FriendlyByteBuf, STACK> fromPacket,
      Function<JsonObject, STACK> stackParser,
      IChemicalStackIngredientCreator<CHEMICAL, STACK, INGREDIENT> ingredientCreator,
      Function<INGREDIENT[], INGREDIENT> multiCreator,
      IntFunction<INGREDIENT[]> arrayCreator
   ) {
      this.fromPacket = fromPacket;
      this.stackParser = stackParser;
      this.tags = tags;
      this.info = info;
      this.ingredientCreator = ingredientCreator;
      this.arrayCreator = arrayCreator;
      this.multiCreator = multiCreator;
      this.name = name;
   }

   private String getNameWithPrefix() {
      return "aeiou".indexOf(Character.toLowerCase(this.name.charAt(0))) == -1 ? "a " + this.name : "an " + this.name;
   }

   public final INGREDIENT read(FriendlyByteBuf buffer) {
      Objects.requireNonNull(buffer, "ChemicalStackIngredients cannot be read from a null packet buffer.");

      return (INGREDIENT)(switch ((ChemicalIngredientDeserializer.IngredientType)buffer.m_130066_(ChemicalIngredientDeserializer.IngredientType.class)) {
         case SINGLE -> this.ingredientCreator.from(this.fromPacket.apply(buffer));
         case TAGGED -> this.ingredientCreator.from(this.tags.tag(buffer.m_130281_()), buffer.m_130258_());
         case MULTI -> this.createMulti(BasePacketHandler.readArray(buffer, this.arrayCreator, this::read));
      });
   }

   public final INGREDIENT deserialize(@Nullable JsonElement json) {
      if (json != null && !json.isJsonNull()) {
         if (json.isJsonArray()) {
            JsonArray jsonArray = json.getAsJsonArray();
            int size = jsonArray.size();
            if (size == 0) {
               throw new JsonSyntaxException("Ingredient array cannot be empty, at least one ingredient must be defined.");
            }

            if (size > 1) {
               INGREDIENT[] ingredients = (INGREDIENT[])((ChemicalStackIngredient[])this.arrayCreator.apply(size));

               for (int i = 0; i < size; i++) {
                  ingredients[i] = this.deserialize(jsonArray.get(i));
               }

               return this.createMulti(ingredients);
            }

            json = jsonArray.get(0);
         }

         if (!json.isJsonObject()) {
            throw new JsonSyntaxException("Expected " + this.name + " to be object or array of objects.");
         } else {
            JsonObject jsonObject = json.getAsJsonObject();
            String serializationKey = this.info.getSerializationKey();
            if (jsonObject.has(serializationKey) && jsonObject.has("tag")) {
               throw new JsonParseException("An ingredient entry is either a tag or " + this.getNameWithPrefix() + ", not both.");
            } else if (jsonObject.has(serializationKey)) {
               STACK stack = this.deserializeStack(jsonObject);
               if (stack.isEmpty()) {
                  throw new JsonSyntaxException("Unable to create an ingredient from an empty stack.");
               } else {
                  return this.ingredientCreator.from(stack);
               }
            } else if (!jsonObject.has("tag")) {
               throw new JsonSyntaxException("Expected to receive a resource location representing either a tag or " + this.getNameWithPrefix() + ".");
            } else if (!jsonObject.has("amount")) {
               throw new JsonSyntaxException("Expected to receive a amount that is greater than zero.");
            } else {
               JsonElement count = jsonObject.get("amount");
               if (!GsonHelper.m_13872_(count)) {
                  throw new JsonSyntaxException("Expected amount to be a number greater than zero.");
               } else {
                  long amount = count.getAsJsonPrimitive().getAsLong();
                  if (amount < 1L) {
                     throw new JsonSyntaxException("Expected amount to be greater than zero.");
                  } else {
                     ResourceLocation resourceLocation = new ResourceLocation(GsonHelper.m_13906_(jsonObject, "tag"));
                     Optional<ITagManager<CHEMICAL>> manager = this.tags.getManager();
                     if (manager.isEmpty()) {
                        throw new JsonSyntaxException("Unexpected error trying to retrieve the chemical tag manager.");
                     } else {
                        ITagManager<CHEMICAL> tagManager = manager.get();
                        TagKey<CHEMICAL> key = tagManager.createTagKey(resourceLocation);
                        return this.ingredientCreator.from(key, amount);
                     }
                  }
               }
            }
         }
      } else {
         throw new JsonSyntaxException("Ingredient cannot be null.");
      }
   }

   @SafeVarargs
   public final INGREDIENT createMulti(INGREDIENT... ingredients) {
      Objects.requireNonNull(ingredients, "Cannot create a multi ingredient out of a null array.");
      if (ingredients.length == 0) {
         throw new IllegalArgumentException("Cannot create a multi ingredient out of no ingredients.");
      } else if (ingredients.length == 1) {
         return ingredients[0];
      } else {
         List<INGREDIENT> cleanedIngredients = new ArrayList<>();

         for (INGREDIENT ingredient : ingredients) {
            if (ingredient instanceof MultiChemicalStackIngredient) {
               cleanedIngredients.addAll(((MultiChemicalStackIngredient)ingredient).getIngredients());
            } else {
               cleanedIngredients.add(ingredient);
            }
         }

         return this.multiCreator.apply((ChemicalStackIngredient[])cleanedIngredients.toArray((ChemicalStackIngredient[])this.arrayCreator.apply(0)));
      }
   }

   public final STACK deserializeStack(@NotNull JsonObject json) {
      return this.stackParser.apply(json);
   }

   static enum IngredientType {
      SINGLE,
      TAGGED,
      MULTI;
   }
}
