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
import mekanism.api.recipes.ingredients.ItemStackIngredient;
import net.minecraft.core.RegistryAccess;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;

@NothingNullByDefault
public abstract class ItemStackChemicalToItemStackRecipe<CHEMICAL extends Chemical<CHEMICAL>, STACK extends ChemicalStack<CHEMICAL>, INGREDIENT extends ChemicalStackIngredient<CHEMICAL, STACK>>
   extends MekanismRecipe
   implements BiPredicate<ItemStack, STACK> {
   private final ItemStackIngredient itemInput;
   private final INGREDIENT chemicalInput;
   private final ItemStack output;

   public ItemStackChemicalToItemStackRecipe(ResourceLocation id, ItemStackIngredient itemInput, INGREDIENT chemicalInput, ItemStack output) {
      super(id);
      this.itemInput = Objects.requireNonNull(itemInput, "Item input cannot be null.");
      this.chemicalInput = Objects.requireNonNull(chemicalInput, "Chemical input cannot be null.");
      Objects.requireNonNull(output, "Output cannot be null.");
      if (output.m_41619_()) {
         throw new IllegalArgumentException("Output cannot be empty.");
      } else {
         this.output = output.m_41777_();
      }
   }

   public ItemStackIngredient getItemInput() {
      return this.itemInput;
   }

   public INGREDIENT getChemicalInput() {
      return this.chemicalInput;
   }

   @Contract(
      value = "_, _ -> new",
      pure = true
   )
   public ItemStack getOutput(ItemStack inputItem, STACK inputChemical) {
      return this.output.m_41777_();
   }

   @NotNull
   @Override
   public ItemStack m_8043_(@NotNull RegistryAccess registryAccess) {
      return this.output.m_41777_();
   }

   public boolean test(ItemStack itemStack, STACK gasStack) {
      return this.itemInput.test(itemStack) && this.chemicalInput.test(gasStack);
   }

   public List<ItemStack> getOutputDefinition() {
      return Collections.singletonList(this.output);
   }

   @Override
   public boolean m_142505_() {
      return this.itemInput.hasNoMatchingInstances() || this.chemicalInput.hasNoMatchingInstances();
   }

   @Override
   public void logMissingTags() {
      this.itemInput.logMissingTags();
      this.chemicalInput.logMissingTags();
   }

   @Override
   public void write(FriendlyByteBuf buffer) {
      this.itemInput.write(buffer);
      this.chemicalInput.write(buffer);
      buffer.m_130055_(this.output);
   }
}
