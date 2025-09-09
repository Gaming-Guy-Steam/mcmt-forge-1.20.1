package mekanism.api.recipes;

import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.function.Predicate;
import mekanism.api.annotations.NothingNullByDefault;
import mekanism.api.chemical.Chemical;
import mekanism.api.chemical.ChemicalStack;
import mekanism.api.chemical.ChemicalType;
import mekanism.api.chemical.merged.BoxedChemicalStack;
import mekanism.api.recipes.ingredients.ChemicalStackIngredient;
import net.minecraft.core.RegistryAccess;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;

@NothingNullByDefault
public abstract class ChemicalCrystallizerRecipe extends MekanismRecipe implements Predicate<BoxedChemicalStack> {
   private final ChemicalType chemicalType;
   private final ChemicalStackIngredient<?, ?> input;
   private final ItemStack output;

   public ChemicalCrystallizerRecipe(ResourceLocation id, ChemicalStackIngredient<?, ?> input, ItemStack output) {
      super(id);
      this.input = Objects.requireNonNull(input, "Input cannot be null.");
      this.chemicalType = ChemicalType.getTypeFor(input);
      Objects.requireNonNull(output, "Output cannot be null.");
      if (output.m_41619_()) {
         throw new IllegalArgumentException("Output cannot be empty.");
      } else {
         this.output = output.m_41777_();
      }
   }

   @Contract(
      value = "_ -> new",
      pure = true
   )
   public ItemStack getOutput(BoxedChemicalStack input) {
      return this.output.m_41777_();
   }

   @NotNull
   @Override
   public ItemStack m_8043_(@NotNull RegistryAccess registryAccess) {
      return this.output.m_41777_();
   }

   public List<ItemStack> getOutputDefinition() {
      return Collections.singletonList(this.output);
   }

   public boolean test(BoxedChemicalStack chemicalStack) {
      return this.chemicalType == chemicalStack.getChemicalType() && this.testInternal(chemicalStack.getChemicalStack());
   }

   public boolean test(ChemicalStack<?> stack) {
      return this.chemicalType == ChemicalType.getTypeFor(stack) && this.testInternal(stack);
   }

   public boolean testType(ChemicalStack<?> stack) {
      return this.chemicalType == ChemicalType.getTypeFor(stack) && this.testTypeInternal(stack);
   }

   public boolean testType(BoxedChemicalStack stack) {
      return this.chemicalType == stack.getChemicalType() && this.testTypeInternal(stack.getChemicalStack());
   }

   private <CHEMICAL extends Chemical<CHEMICAL>, STACK extends ChemicalStack<CHEMICAL>> boolean testInternal(STACK stack) {
      return this.input.test(stack);
   }

   private <CHEMICAL extends Chemical<CHEMICAL>, STACK extends ChemicalStack<CHEMICAL>> boolean testTypeInternal(STACK stack) {
      return this.input.testType(stack);
   }

   public ChemicalStackIngredient<?, ?> getInput() {
      return this.input;
   }

   @Override
   public boolean m_142505_() {
      return this.input.hasNoMatchingInstances();
   }

   @Override
   public void logMissingTags() {
      this.input.logMissingTags();
   }

   @Override
   public void write(FriendlyByteBuf buffer) {
      buffer.m_130068_(this.chemicalType);
      this.input.write(buffer);
      buffer.m_130055_(this.output);
   }
}
