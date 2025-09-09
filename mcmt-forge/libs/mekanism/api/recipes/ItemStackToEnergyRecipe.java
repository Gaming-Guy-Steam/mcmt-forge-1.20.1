package mekanism.api.recipes;

import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.function.Predicate;
import mekanism.api.annotations.NothingNullByDefault;
import mekanism.api.math.FloatingLong;
import mekanism.api.recipes.ingredients.ItemStackIngredient;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;

@NothingNullByDefault
public abstract class ItemStackToEnergyRecipe extends MekanismRecipe implements Predicate<ItemStack> {
   protected final ItemStackIngredient input;
   protected final FloatingLong output;

   public ItemStackToEnergyRecipe(ResourceLocation id, ItemStackIngredient input, FloatingLong output) {
      super(id);
      this.input = Objects.requireNonNull(input, "Input cannot be null.");
      Objects.requireNonNull(output, "Output cannot be null.");
      if (output.isZero()) {
         throw new IllegalArgumentException("Output must be greater than zero.");
      } else {
         this.output = output.copyAsConst();
      }
   }

   public boolean test(ItemStack itemStack) {
      return this.input.test(itemStack);
   }

   public ItemStackIngredient getInput() {
      return this.input;
   }

   public FloatingLong getOutput(ItemStack input) {
      return this.output;
   }

   public List<FloatingLong> getOutputDefinition() {
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
      this.output.writeToBuffer(buffer);
   }
}
