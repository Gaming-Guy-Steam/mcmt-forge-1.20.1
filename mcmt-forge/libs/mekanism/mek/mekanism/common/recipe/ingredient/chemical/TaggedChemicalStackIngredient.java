package mekanism.common.recipe.ingredient.chemical;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import mekanism.api.chemical.Chemical;
import mekanism.api.chemical.ChemicalStack;
import mekanism.api.chemical.ChemicalTags;
import mekanism.api.recipes.ingredients.ChemicalStackIngredient;
import mekanism.common.Mekanism;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.tags.TagKey;
import net.minecraftforge.registries.tags.ITag;
import org.jetbrains.annotations.NotNull;

public abstract class TaggedChemicalStackIngredient<CHEMICAL extends Chemical<CHEMICAL>, STACK extends ChemicalStack<CHEMICAL>>
   implements ChemicalStackIngredient<CHEMICAL, STACK> {
   @NotNull
   private final ITag<CHEMICAL> tag;
   private final long amount;

   protected TaggedChemicalStackIngredient(@NotNull ChemicalTags<CHEMICAL> tags, @NotNull TagKey<CHEMICAL> tag, long amount) {
      this(tags.getManager().map(manager -> manager.getTag(tag)).orElseThrow(), amount);
   }

   protected TaggedChemicalStackIngredient(@NotNull ITag<CHEMICAL> tag, long amount) {
      this.tag = tag;
      this.amount = amount;
   }

   protected abstract ChemicalIngredientInfo<CHEMICAL, STACK> getIngredientInfo();

   public boolean test(@NotNull STACK chemicalStack) {
      return this.testType(chemicalStack) && chemicalStack.getAmount() >= this.amount;
   }

   public boolean testType(@NotNull STACK chemicalStack) {
      return this.testType(Objects.<STACK>requireNonNull(chemicalStack).getType());
   }

   @Override
   public boolean testType(@NotNull CHEMICAL chemical) {
      return this.tag.contains(Objects.requireNonNull(chemical));
   }

   @NotNull
   public STACK getMatchingInstance(@NotNull STACK chemicalStack) {
      return this.test(chemicalStack) ? this.getIngredientInfo().createStack(chemicalStack, this.amount) : this.getIngredientInfo().getEmptyStack();
   }

   public long getNeededAmount(@NotNull STACK stack) {
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

   @NotNull
   @Override
   public List<STACK> getRepresentations() {
      ChemicalIngredientInfo<CHEMICAL, STACK> ingredientInfo = this.getIngredientInfo();
      List<STACK> representations = new ArrayList<>();

      for (CHEMICAL chemical : this.tag) {
         representations.add(ingredientInfo.createStack(chemical, this.amount));
      }

      return representations;
   }

   public Iterable<CHEMICAL> getRawInput() {
      return this.tag;
   }

   @Override
   public void write(FriendlyByteBuf buffer) {
      buffer.m_130068_(ChemicalIngredientDeserializer.IngredientType.TAGGED);
      buffer.m_130085_(this.tag.getKey().f_203868_());
      buffer.m_130103_(this.amount);
   }

   @NotNull
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
         TaggedChemicalStackIngredient<CHEMICAL, STACK> other = (TaggedChemicalStackIngredient<CHEMICAL, STACK>)o;
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
