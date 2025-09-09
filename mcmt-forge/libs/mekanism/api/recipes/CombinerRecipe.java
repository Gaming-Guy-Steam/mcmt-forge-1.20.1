package mekanism.api.recipes;

import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.function.BiPredicate;
import mekanism.api.annotations.NothingNullByDefault;
import mekanism.api.recipes.ingredients.ItemStackIngredient;
import net.minecraft.core.RegistryAccess;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;

@NothingNullByDefault
public abstract class CombinerRecipe extends MekanismRecipe implements BiPredicate<ItemStack, ItemStack> {
   private final ItemStackIngredient mainInput;
   private final ItemStackIngredient extraInput;
   private final ItemStack output;

   public CombinerRecipe(ResourceLocation id, ItemStackIngredient mainInput, ItemStackIngredient extraInput, ItemStack output) {
      super(id);
      this.mainInput = Objects.requireNonNull(mainInput, "Main input cannot be null.");
      this.extraInput = Objects.requireNonNull(extraInput, "Secondary/Extra input cannot be null.");
      Objects.requireNonNull(output, "Output cannot be null.");
      if (output.m_41619_()) {
         throw new IllegalArgumentException("Output cannot be empty.");
      } else {
         this.output = output.m_41777_();
      }
   }

   public boolean test(ItemStack input, ItemStack extra) {
      return this.mainInput.test(input) && this.extraInput.test(extra);
   }

   public ItemStackIngredient getMainInput() {
      return this.mainInput;
   }

   public ItemStackIngredient getExtraInput() {
      return this.extraInput;
   }

   @Contract(
      value = "_, _ -> new",
      pure = true
   )
   public ItemStack getOutput(@NotNull ItemStack input, @NotNull ItemStack extra) {
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
      return this.mainInput.hasNoMatchingInstances() || this.extraInput.hasNoMatchingInstances();
   }

   @Override
   public void logMissingTags() {
      this.mainInput.logMissingTags();
      this.extraInput.logMissingTags();
   }

   @Override
   public void write(FriendlyByteBuf buffer) {
      this.mainInput.write(buffer);
      this.extraInput.write(buffer);
      buffer.m_130055_(this.output);
   }
}
