package mekanism.common.recipe.ingredient.chemical;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import mekanism.api.chemical.Chemical;
import mekanism.api.chemical.ChemicalStack;
import mekanism.api.recipes.ingredients.ChemicalStackIngredient;
import mekanism.common.util.ChemicalUtil;
import net.minecraft.network.FriendlyByteBuf;
import org.jetbrains.annotations.NotNull;

public abstract class SingleChemicalStackIngredient<CHEMICAL extends Chemical<CHEMICAL>, STACK extends ChemicalStack<CHEMICAL>>
   implements ChemicalStackIngredient<CHEMICAL, STACK> {
   @NotNull
   private final List<STACK> representations;
   @NotNull
   private final STACK chemicalInstance;

   public SingleChemicalStackIngredient(@NotNull STACK chemicalInstance) {
      this.chemicalInstance = chemicalInstance;
      this.representations = Collections.singletonList(ChemicalUtil.copy(this.chemicalInstance));
   }

   protected abstract ChemicalIngredientInfo<CHEMICAL, STACK> getIngredientInfo();

   public boolean test(@NotNull STACK chemicalStack) {
      return this.testType(chemicalStack) && chemicalStack.getAmount() >= this.chemicalInstance.getAmount();
   }

   public boolean testType(@NotNull STACK chemicalStack) {
      return this.chemicalInstance.isTypeEqual(Objects.requireNonNull(chemicalStack));
   }

   @Override
   public boolean testType(@NotNull CHEMICAL chemical) {
      return this.chemicalInstance.isTypeEqual(Objects.requireNonNull(chemical));
   }

   @NotNull
   public STACK getMatchingInstance(@NotNull STACK chemicalStack) {
      return this.test(chemicalStack)
         ? this.getIngredientInfo().createStack(this.chemicalInstance, this.chemicalInstance.getAmount())
         : this.getIngredientInfo().getEmptyStack();
   }

   public long getNeededAmount(@NotNull STACK stack) {
      return this.testType(stack) ? this.chemicalInstance.getAmount() : 0L;
   }

   @Override
   public boolean hasNoMatchingInstances() {
      return false;
   }

   @Override
   public void logMissingTags() {
   }

   @NotNull
   @Override
   public List<STACK> getRepresentations() {
      return this.representations;
   }

   public CHEMICAL getInputRaw() {
      return this.chemicalInstance.getType();
   }

   @Override
   public void write(FriendlyByteBuf buffer) {
      buffer.m_130068_(ChemicalIngredientDeserializer.IngredientType.SINGLE);
      this.chemicalInstance.writeToPacket(buffer);
   }

   @NotNull
   @Override
   public JsonElement serialize() {
      JsonObject json = new JsonObject();
      json.addProperty("amount", this.chemicalInstance.getAmount());
      json.addProperty(this.getIngredientInfo().getSerializationKey(), this.chemicalInstance.getTypeRegistryName().toString());
      return json;
   }

   @Override
   public boolean equals(Object o) {
      if (this == o) {
         return true;
      } else {
         return o != null && this.getClass() == o.getClass() ? this.chemicalInstance.equals(((SingleChemicalStackIngredient)o).chemicalInstance) : false;
      }
   }

   @Override
   public int hashCode() {
      return this.chemicalInstance.hashCode();
   }
}
