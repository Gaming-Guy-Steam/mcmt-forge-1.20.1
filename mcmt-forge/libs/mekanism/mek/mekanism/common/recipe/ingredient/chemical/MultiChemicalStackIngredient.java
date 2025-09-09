package mekanism.common.recipe.ingredient.chemical;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.function.Predicate;
import mekanism.api.chemical.Chemical;
import mekanism.api.chemical.ChemicalStack;
import mekanism.api.chemical.gas.Gas;
import mekanism.api.chemical.gas.GasStack;
import mekanism.api.chemical.infuse.InfuseType;
import mekanism.api.chemical.infuse.InfusionStack;
import mekanism.api.chemical.pigment.Pigment;
import mekanism.api.chemical.pigment.PigmentStack;
import mekanism.api.chemical.slurry.Slurry;
import mekanism.api.chemical.slurry.SlurryStack;
import mekanism.api.recipes.ingredients.ChemicalStackIngredient;
import mekanism.api.recipes.ingredients.InputIngredient;
import mekanism.common.network.BasePacketHandler;
import mekanism.common.recipe.ingredient.IMultiIngredient;
import net.minecraft.network.FriendlyByteBuf;
import org.jetbrains.annotations.NotNull;

public abstract class MultiChemicalStackIngredient<CHEMICAL extends Chemical<CHEMICAL>, STACK extends ChemicalStack<CHEMICAL>, INGREDIENT extends ChemicalStackIngredient<CHEMICAL, STACK>>
   implements ChemicalStackIngredient<CHEMICAL, STACK>,
   IMultiIngredient<STACK, INGREDIENT> {
   private final INGREDIENT[] ingredients;

   @SafeVarargs
   protected MultiChemicalStackIngredient(@NotNull INGREDIENT... ingredients) {
      this.ingredients = ingredients;
   }

   protected abstract ChemicalIngredientInfo<CHEMICAL, STACK> getIngredientInfo();

   @Override
   public final List<INGREDIENT> getIngredients() {
      return List.of(this.ingredients);
   }

   public boolean test(@NotNull STACK stack) {
      return Arrays.stream(this.ingredients).anyMatch(ingredient -> ingredient.test(stack));
   }

   public boolean testType(@NotNull STACK stack) {
      return Arrays.stream(this.ingredients).anyMatch(ingredient -> ingredient.testType(stack));
   }

   @Override
   public boolean testType(@NotNull CHEMICAL chemical) {
      return Arrays.stream(this.ingredients).anyMatch(ingredient -> ingredient.testType(chemical));
   }

   @NotNull
   public STACK getMatchingInstance(@NotNull STACK stack) {
      for (INGREDIENT ingredient : this.ingredients) {
         STACK matchingInstance = ingredient.getMatchingInstance(stack);
         if (!matchingInstance.isEmpty()) {
            return matchingInstance;
         }
      }

      return this.getIngredientInfo().getEmptyStack();
   }

   public long getNeededAmount(@NotNull STACK stack) {
      for (INGREDIENT ingredient : this.ingredients) {
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
      for (INGREDIENT ingredient : this.ingredients) {
         ingredient.logMissingTags();
      }
   }

   @NotNull
   @Override
   public List<STACK> getRepresentations() {
      List<STACK> representations = new ArrayList<>();

      for (INGREDIENT ingredient : this.ingredients) {
         representations.addAll(ingredient.getRepresentations());
      }

      return representations;
   }

   @Override
   public boolean forEachIngredient(Predicate<INGREDIENT> checker) {
      boolean result = false;

      for (INGREDIENT ingredient : this.ingredients) {
         result |= checker.test(ingredient);
      }

      return result;
   }

   @Override
   public void write(FriendlyByteBuf buffer) {
      buffer.m_130068_(ChemicalIngredientDeserializer.IngredientType.MULTI);
      BasePacketHandler.writeArray(buffer, this.ingredients, InputIngredient::write);
   }

   @NotNull
   @Override
   public JsonElement serialize() {
      JsonArray json = new JsonArray();

      for (INGREDIENT ingredient : this.ingredients) {
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
            ? Arrays.equals((Object[])this.ingredients, (Object[])((MultiChemicalStackIngredient)o).ingredients)
            : false;
      }
   }

   @Override
   public int hashCode() {
      return Arrays.hashCode((Object[])this.ingredients);
   }

   public static class MultiGasStackIngredient
      extends MultiChemicalStackIngredient<Gas, GasStack, ChemicalStackIngredient.GasStackIngredient>
      implements ChemicalStackIngredient.GasStackIngredient {
      MultiGasStackIngredient(ChemicalStackIngredient.GasStackIngredient... ingredients) {
         super(ingredients);
      }

      @Override
      protected ChemicalIngredientInfo<Gas, GasStack> getIngredientInfo() {
         return ChemicalIngredientInfo.GAS;
      }
   }

   public static class MultiInfusionStackIngredient
      extends MultiChemicalStackIngredient<InfuseType, InfusionStack, ChemicalStackIngredient.InfusionStackIngredient>
      implements ChemicalStackIngredient.InfusionStackIngredient {
      MultiInfusionStackIngredient(ChemicalStackIngredient.InfusionStackIngredient... ingredients) {
         super(ingredients);
      }

      @Override
      protected ChemicalIngredientInfo<InfuseType, InfusionStack> getIngredientInfo() {
         return ChemicalIngredientInfo.INFUSION;
      }
   }

   public static class MultiPigmentStackIngredient
      extends MultiChemicalStackIngredient<Pigment, PigmentStack, ChemicalStackIngredient.PigmentStackIngredient>
      implements ChemicalStackIngredient.PigmentStackIngredient {
      MultiPigmentStackIngredient(ChemicalStackIngredient.PigmentStackIngredient... ingredients) {
         super(ingredients);
      }

      @Override
      protected ChemicalIngredientInfo<Pigment, PigmentStack> getIngredientInfo() {
         return ChemicalIngredientInfo.PIGMENT;
      }
   }

   public static class MultiSlurryStackIngredient
      extends MultiChemicalStackIngredient<Slurry, SlurryStack, ChemicalStackIngredient.SlurryStackIngredient>
      implements ChemicalStackIngredient.SlurryStackIngredient {
      MultiSlurryStackIngredient(ChemicalStackIngredient.SlurryStackIngredient... ingredients) {
         super(ingredients);
      }

      @Override
      protected ChemicalIngredientInfo<Slurry, SlurryStack> getIngredientInfo() {
         return ChemicalIngredientInfo.SLURRY;
      }
   }
}
