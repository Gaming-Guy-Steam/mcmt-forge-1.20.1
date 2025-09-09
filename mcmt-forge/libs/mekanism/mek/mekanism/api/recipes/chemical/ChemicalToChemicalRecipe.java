package mekanism.api.recipes.chemical;

import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.function.Predicate;
import mekanism.api.annotations.NothingNullByDefault;
import mekanism.api.chemical.Chemical;
import mekanism.api.chemical.ChemicalStack;
import mekanism.api.recipes.MekanismRecipe;
import mekanism.api.recipes.ingredients.ChemicalStackIngredient;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;
import org.jetbrains.annotations.Contract;

@NothingNullByDefault
public abstract class ChemicalToChemicalRecipe<CHEMICAL extends Chemical<CHEMICAL>, STACK extends ChemicalStack<CHEMICAL>, INGREDIENT extends ChemicalStackIngredient<CHEMICAL, STACK>>
   extends MekanismRecipe
   implements Predicate<STACK> {
   private final INGREDIENT input;
   protected final STACK output;

   public ChemicalToChemicalRecipe(ResourceLocation id, INGREDIENT input, STACK output) {
      super(id);
      this.input = Objects.requireNonNull(input, "Input cannot be null.");
      Objects.requireNonNull(output, "Output cannot be null.");
      if (output.isEmpty()) {
         throw new IllegalArgumentException("Output cannot be empty.");
      } else {
         this.output = (STACK)output.copy();
      }
   }

   public boolean test(STACK chemicalStack) {
      return this.input.test(chemicalStack);
   }

   public INGREDIENT getInput() {
      return this.input;
   }

   public List<STACK> getOutputDefinition() {
      return Collections.singletonList(this.output);
   }

   @Contract(
      value = "_ -> new",
      pure = true
   )
   public STACK getOutput(STACK input) {
      return (STACK)this.output.copy();
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
      this.input.write(buffer);
      this.output.writeToPacket(buffer);
   }
}
