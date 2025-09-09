package mekanism.api.recipes.chemical;

import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.function.Predicate;
import mekanism.api.annotations.NothingNullByDefault;
import mekanism.api.chemical.Chemical;
import mekanism.api.chemical.ChemicalStack;
import mekanism.api.recipes.MekanismRecipe;
import mekanism.api.recipes.ingredients.ItemStackIngredient;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
import org.jetbrains.annotations.Contract;

@NothingNullByDefault
public abstract class ItemStackToChemicalRecipe<CHEMICAL extends Chemical<CHEMICAL>, STACK extends ChemicalStack<CHEMICAL>>
   extends MekanismRecipe
   implements Predicate<ItemStack> {
   protected final ItemStackIngredient input;
   protected final STACK output;

   public ItemStackToChemicalRecipe(ResourceLocation id, ItemStackIngredient input, STACK output) {
      super(id);
      this.input = Objects.requireNonNull(input, "Input cannot be null.");
      Objects.requireNonNull(output, "Output cannot be null.");
      if (output.isEmpty()) {
         throw new IllegalArgumentException("Output cannot be empty.");
      } else {
         this.output = (STACK)output.copy();
      }
   }

   public boolean test(ItemStack itemStack) {
      return this.input.test(itemStack);
   }

   public ItemStackIngredient getInput() {
      return this.input;
   }

   @Contract(
      value = "_ -> new",
      pure = true
   )
   public STACK getOutput(ItemStack input) {
      return (STACK)this.output.copy();
   }

   public List<STACK> getOutputDefinition() {
      return Collections.singletonList(this.output);
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
