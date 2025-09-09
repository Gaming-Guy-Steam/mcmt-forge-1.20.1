package mekanism.api.recipes;

import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.function.BiPredicate;
import mekanism.api.annotations.NothingNullByDefault;
import mekanism.api.chemical.ChemicalStack;
import mekanism.api.chemical.gas.GasStack;
import mekanism.api.chemical.merged.BoxedChemicalStack;
import mekanism.api.recipes.ingredients.ChemicalStackIngredient;
import mekanism.api.recipes.ingredients.ItemStackIngredient;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
import org.jetbrains.annotations.Contract;

@NothingNullByDefault
public abstract class ChemicalDissolutionRecipe extends MekanismRecipe implements BiPredicate<ItemStack, GasStack> {
   private final ItemStackIngredient itemInput;
   private final ChemicalStackIngredient.GasStackIngredient gasInput;
   private final BoxedChemicalStack output;

   public ChemicalDissolutionRecipe(
      ResourceLocation id, ItemStackIngredient itemInput, ChemicalStackIngredient.GasStackIngredient gasInput, ChemicalStack<?> output
   ) {
      super(id);
      this.itemInput = Objects.requireNonNull(itemInput, "Item input cannot be null.");
      this.gasInput = Objects.requireNonNull(gasInput, "Gas input cannot be null.");
      Objects.requireNonNull(output, "Output cannot be null.");
      if (output.isEmpty()) {
         throw new IllegalArgumentException("Output cannot be empty.");
      } else {
         this.output = BoxedChemicalStack.box(output.copy());
      }
   }

   public ItemStackIngredient getItemInput() {
      return this.itemInput;
   }

   public ChemicalStackIngredient.GasStackIngredient getGasInput() {
      return this.gasInput;
   }

   @Contract(
      value = "_, _ -> new",
      pure = true
   )
   public BoxedChemicalStack getOutput(ItemStack inputItem, GasStack inputGas) {
      return this.output.copy();
   }

   public boolean test(ItemStack itemStack, GasStack gasStack) {
      return this.itemInput.test(itemStack) && this.gasInput.test(gasStack);
   }

   public List<BoxedChemicalStack> getOutputDefinition() {
      return Collections.singletonList(this.output);
   }

   @Override
   public boolean m_142505_() {
      return this.itemInput.hasNoMatchingInstances() || this.gasInput.hasNoMatchingInstances();
   }

   @Override
   public void logMissingTags() {
      this.itemInput.logMissingTags();
      this.gasInput.logMissingTags();
   }

   @Override
   public void write(FriendlyByteBuf buffer) {
      this.itemInput.write(buffer);
      this.gasInput.write(buffer);
      buffer.m_130068_(this.output.getChemicalType());
      this.output.getChemicalStack().writeToPacket(buffer);
   }
}
