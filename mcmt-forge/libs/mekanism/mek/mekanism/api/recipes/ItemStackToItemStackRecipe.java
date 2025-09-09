package mekanism.api.recipes;

import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.function.Predicate;
import mekanism.api.annotations.NothingNullByDefault;
import mekanism.api.recipes.ingredients.ItemStackIngredient;
import net.minecraft.core.RegistryAccess;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;

@NothingNullByDefault
public abstract class ItemStackToItemStackRecipe extends MekanismRecipe implements Predicate<ItemStack> {
   private final ItemStackIngredient input;
   private final ItemStack output;

   public ItemStackToItemStackRecipe(ResourceLocation id, ItemStackIngredient input, ItemStack output) {
      super(id);
      this.input = Objects.requireNonNull(input, "Input cannot be null.");
      Objects.requireNonNull(output, "Output cannot be null.");
      if (output.m_41619_()) {
         throw new IllegalArgumentException("Output cannot be empty.");
      } else {
         this.output = output.m_41777_();
      }
   }

   public boolean test(ItemStack input) {
      return this.input.test(input);
   }

   public ItemStackIngredient getInput() {
      return this.input;
   }

   @Contract(
      value = "_ -> new",
      pure = true
   )
   public ItemStack getOutput(ItemStack input) {
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
      buffer.m_130055_(this.output);
   }
}
