package mekanism.api;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonSyntaxException;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import java.util.function.Function;
import mekanism.api.annotations.NothingNullByDefault;
import mekanism.api.chemical.Chemical;
import mekanism.api.chemical.ChemicalStack;
import mekanism.api.chemical.ChemicalType;
import mekanism.api.chemical.gas.Gas;
import mekanism.api.chemical.gas.GasStack;
import mekanism.api.chemical.infuse.InfuseType;
import mekanism.api.chemical.infuse.InfusionStack;
import mekanism.api.chemical.merged.BoxedChemicalStack;
import mekanism.api.chemical.pigment.Pigment;
import mekanism.api.chemical.pigment.PigmentStack;
import mekanism.api.chemical.slurry.Slurry;
import mekanism.api.chemical.slurry.SlurryStack;
import mekanism.api.math.FloatingLong;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.TagParser;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.GsonHelper;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.ShapedRecipe;
import net.minecraft.world.level.material.Fluid;
import net.minecraft.world.level.material.Fluids;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.registries.ForgeRegistries;
import org.jetbrains.annotations.NotNull;

@NothingNullByDefault
public class SerializerHelper {
   private static final Gson GSON = new GsonBuilder().setPrettyPrinting().disableHtmlEscaping().create();

   private SerializerHelper() {
   }

   public static FloatingLong getFloatingLong(@NotNull JsonObject json, @NotNull String key) {
      if (!json.has(key)) {
         throw new JsonSyntaxException("Missing '" + key + "', expected to find an object");
      } else {
         JsonElement jsonElement = json.get(key);
         if (!jsonElement.isJsonPrimitive()) {
            throw new JsonSyntaxException("Expected '" + key + "' to be a json primitive representing a FloatingLong");
         } else {
            try {
               return FloatingLong.parseFloatingLong(jsonElement.getAsNumber().toString(), true);
            } catch (NumberFormatException var4) {
               throw new JsonSyntaxException("Expected '" + key + "' to be a valid FloatingLong (positive decimal number)");
            }
         }
      }
   }

   private static void validateKey(@NotNull JsonObject json, @NotNull String key) {
      if (!json.has(key)) {
         throw new JsonSyntaxException("Missing '" + key + "', expected to find an object");
      } else if (!json.get(key).isJsonObject()) {
         throw new JsonSyntaxException("Expected '" + key + "' to be an object");
      }
   }

   public static ChemicalType getChemicalType(@NotNull JsonObject json) {
      if (!json.has("chemicalType")) {
         throw new JsonSyntaxException("Missing 'chemicalType', expected to find a string");
      } else {
         JsonElement element = json.get("chemicalType");
         if (!element.isJsonPrimitive()) {
            throw new JsonSyntaxException("Expected 'chemicalType' to be a json primitive representing a string");
         } else {
            String name = element.getAsString();
            ChemicalType chemicalType = ChemicalType.fromString(name);
            if (chemicalType == null) {
               throw new JsonSyntaxException("Invalid chemical type '" + name + "'.");
            } else {
               return chemicalType;
            }
         }
      }
   }

   public static ItemStack getItemStack(@NotNull JsonObject json, @NotNull String key) {
      validateKey(json, key);
      return ShapedRecipe.m_151274_(GsonHelper.m_13930_(json, key));
   }

   public static FluidStack getFluidStack(@NotNull JsonObject json, @NotNull String key) {
      validateKey(json, key);
      return deserializeFluid(GsonHelper.m_13930_(json, key));
   }

   public static ChemicalStack<?> getBoxedChemicalStack(@NotNull JsonObject json, @NotNull String key) {
      validateKey(json, key);
      JsonObject jsonObject = GsonHelper.m_13930_(json, key);
      ChemicalType chemicalType = getChemicalType(jsonObject);

      return (ChemicalStack<?>)(switch (chemicalType) {
         case GAS -> deserializeGas(jsonObject);
         case INFUSION -> deserializeInfuseType(jsonObject);
         case PIGMENT -> deserializePigment(jsonObject);
         case SLURRY -> deserializeSlurry(jsonObject);
      });
   }

   public static GasStack getGasStack(@NotNull JsonObject json, @NotNull String key) {
      validateKey(json, key);
      return deserializeGas(GsonHelper.m_13930_(json, key));
   }

   public static InfusionStack getInfusionStack(@NotNull JsonObject json, @NotNull String key) {
      validateKey(json, key);
      return deserializeInfuseType(GsonHelper.m_13930_(json, key));
   }

   public static PigmentStack getPigmentStack(@NotNull JsonObject json, @NotNull String key) {
      validateKey(json, key);
      return deserializePigment(GsonHelper.m_13930_(json, key));
   }

   public static SlurryStack getSlurryStack(@NotNull JsonObject json, @NotNull String key) {
      validateKey(json, key);
      return deserializeSlurry(GsonHelper.m_13930_(json, key));
   }

   public static FluidStack deserializeFluid(@NotNull JsonObject json) {
      if (!json.has("amount")) {
         throw new JsonSyntaxException("Expected to receive a amount that is greater than zero");
      } else {
         JsonElement count = json.get("amount");
         if (!GsonHelper.m_13872_(count)) {
            throw new JsonSyntaxException("Expected amount to be a number greater than zero.");
         } else {
            int amount = count.getAsJsonPrimitive().getAsInt();
            if (amount < 1) {
               throw new JsonSyntaxException("Expected amount to be greater than zero.");
            } else {
               ResourceLocation resourceLocation = new ResourceLocation(GsonHelper.m_13906_(json, "fluid"));
               Fluid fluid = (Fluid)ForgeRegistries.FLUIDS.getValue(resourceLocation);
               if (fluid != null && fluid != Fluids.f_76191_) {
                  CompoundTag nbt = null;
                  if (json.has("nbt")) {
                     JsonElement jsonNBT = json.get("nbt");

                     try {
                        if (jsonNBT.isJsonObject()) {
                           nbt = TagParser.m_129359_(GSON.toJson(jsonNBT));
                        } else {
                           nbt = TagParser.m_129359_(GsonHelper.m_13805_(jsonNBT, "nbt"));
                        }
                     } catch (CommandSyntaxException var8) {
                        throw new JsonSyntaxException("Invalid NBT entry for fluid '" + resourceLocation + "'");
                     }
                  }

                  return new FluidStack(fluid, amount, nbt);
               } else {
                  throw new JsonSyntaxException("Invalid fluid type '" + resourceLocation + "'");
               }
            }
         }
      }
   }

   public static GasStack deserializeGas(@NotNull JsonObject json) {
      return deserializeChemicalStack(json, "gas", Gas::getFromRegistry);
   }

   public static InfusionStack deserializeInfuseType(@NotNull JsonObject json) {
      return deserializeChemicalStack(json, "infuse_type", InfuseType::getFromRegistry);
   }

   public static PigmentStack deserializePigment(@NotNull JsonObject json) {
      return deserializeChemicalStack(json, "pigment", Pigment::getFromRegistry);
   }

   public static SlurryStack deserializeSlurry(@NotNull JsonObject json) {
      return deserializeChemicalStack(json, "slurry", Slurry::getFromRegistry);
   }

   private static <CHEMICAL extends Chemical<CHEMICAL>, STACK extends ChemicalStack<CHEMICAL>> STACK deserializeChemicalStack(
      @NotNull JsonObject json, @NotNull String serializationKey, @NotNull Function<ResourceLocation, CHEMICAL> fromRegistry
   ) {
      if (!json.has("amount")) {
         throw new JsonSyntaxException("Expected to receive a amount that is greater than zero");
      } else {
         JsonElement count = json.get("amount");
         if (!GsonHelper.m_13872_(count)) {
            throw new JsonSyntaxException("Expected amount to be a number greater than zero.");
         } else {
            long amount = count.getAsJsonPrimitive().getAsLong();
            if (amount < 1L) {
               throw new JsonSyntaxException("Expected amount to be greater than zero.");
            } else {
               ResourceLocation resourceLocation = new ResourceLocation(GsonHelper.m_13906_(json, serializationKey));
               CHEMICAL chemical = (CHEMICAL)fromRegistry.apply(resourceLocation);
               if (chemical.isEmptyType()) {
                  throw new JsonSyntaxException("Invalid " + serializationKey + " type '" + resourceLocation + "'");
               } else {
                  return (STACK)chemical.getStack(amount);
               }
            }
         }
      }
   }

   public static JsonElement serializeItemStack(@NotNull ItemStack stack) {
      JsonObject json = new JsonObject();
      json.addProperty("item", ForgeRegistries.ITEMS.getKey(stack.m_41720_()).toString());
      if (stack.m_41613_() > 1) {
         json.addProperty("count", stack.m_41613_());
      }

      if (stack.m_41782_()) {
         json.addProperty("nbt", stack.m_41783_().toString());
      }

      return json;
   }

   public static JsonElement serializeFluidStack(@NotNull FluidStack stack) {
      JsonObject json = new JsonObject();
      json.addProperty("fluid", ForgeRegistries.FLUIDS.getKey(stack.getFluid()).toString());
      json.addProperty("amount", stack.getAmount());
      if (stack.hasTag()) {
         json.addProperty("nbt", stack.getTag().toString());
      }

      return json;
   }

   public static JsonElement serializeBoxedChemicalStack(@NotNull BoxedChemicalStack stack) {
      ChemicalType chemicalType = stack.getChemicalType();

      JsonObject json = switch (chemicalType) {
         case GAS -> serializeGasStack((GasStack)stack.getChemicalStack());
         case INFUSION -> serializeInfusionStack((InfusionStack)stack.getChemicalStack());
         case PIGMENT -> serializePigmentStack((PigmentStack)stack.getChemicalStack());
         case SLURRY -> serializeSlurryStack((SlurryStack)stack.getChemicalStack());
      };
      json.addProperty("chemicalType", chemicalType.m_7912_());
      return json;
   }

   public static JsonObject serializeGasStack(@NotNull GasStack stack) {
      return serializeChemicalStack("gas", stack);
   }

   public static JsonObject serializeInfusionStack(@NotNull InfusionStack stack) {
      return serializeChemicalStack("infuse_type", stack);
   }

   public static JsonObject serializePigmentStack(@NotNull PigmentStack stack) {
      return serializeChemicalStack("pigment", stack);
   }

   public static JsonObject serializeSlurryStack(@NotNull SlurryStack stack) {
      return serializeChemicalStack("slurry", stack);
   }

   private static JsonObject serializeChemicalStack(@NotNull String serializationKey, @NotNull ChemicalStack<?> stack) {
      JsonObject json = new JsonObject();
      json.addProperty(serializationKey, stack.getTypeRegistryName().toString());
      json.addProperty("amount", stack.getAmount());
      return json;
   }
}
