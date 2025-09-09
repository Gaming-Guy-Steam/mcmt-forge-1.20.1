package mekanism.api.recipes.chemical;

import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.function.BiPredicate;
import mekanism.api.annotations.NothingNullByDefault;
import mekanism.api.chemical.Chemical;
import mekanism.api.chemical.ChemicalStack;
import mekanism.api.recipes.MekanismRecipe;
import mekanism.api.recipes.ingredients.ChemicalStackIngredient;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;
import org.jetbrains.annotations.Contract;

@NothingNullByDefault
public abstract class ChemicalChemicalToChemicalRecipe<CHEMICAL extends Chemical<CHEMICAL>, STACK extends ChemicalStack<CHEMICAL>, INGREDIENT extends ChemicalStackIngredient<CHEMICAL, STACK>>
   extends MekanismRecipe
   implements BiPredicate<STACK, STACK> {
   private final INGREDIENT leftInput;
   private final INGREDIENT rightInput;
   protected final STACK output;

   public ChemicalChemicalToChemicalRecipe(ResourceLocation id, INGREDIENT leftInput, INGREDIENT rightInput, STACK output) {
      super(id);
      this.leftInput = Objects.requireNonNull(leftInput, "Left input cannot be null.");
      this.rightInput = Objects.requireNonNull(rightInput, "Right input cannot be null.");
      Objects.requireNonNull(output, "Output cannot be null.");
      if (output.isEmpty()) {
         throw new IllegalArgumentException("Output cannot be empty.");
      } else {
         this.output = (STACK)output.copy();
      }
   }

   public boolean test(STACK input1, STACK input2) {
      return this.leftInput.test(input1) && this.rightInput.test(input2) || this.rightInput.test(input1) && this.leftInput.test(input2);
   }

   @Contract(
      value = "_, _ -> new",
      pure = true
   )
   public STACK getOutput(STACK input1, STACK input2) {
      return (STACK)this.output.copy();
   }

   public INGREDIENT getLeftInput() {
      return this.leftInput;
   }

   public INGREDIENT getRightInput() {
      return this.rightInput;
   }

   public List<STACK> getOutputDefinition() {
      return Collections.singletonList(this.output);
   }

   @Override
   public boolean m_142505_() {
      return this.leftInput.hasNoMatchingInstances() || this.rightInput.hasNoMatchingInstances();
   }

   @Override
   public void logMissingTags() {
      this.leftInput.logMissingTags();
      this.rightInput.logMissingTags();
   }

   @Override
   public void write(FriendlyByteBuf buffer) {
      this.leftInput.write(buffer);
      this.rightInput.write(buffer);
      this.output.writeToPacket(buffer);
   }
}
