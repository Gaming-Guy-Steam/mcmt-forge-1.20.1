package mekanism.api.recipes;

import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.Random;
import java.util.function.Predicate;
import mekanism.api.annotations.NothingNullByDefault;
import mekanism.api.recipes.ingredients.ItemStackIngredient;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
import org.jetbrains.annotations.Contract;

@NothingNullByDefault
public abstract class SawmillRecipe extends MekanismRecipe implements Predicate<ItemStack> {
   protected static final Random RANDOM = new Random();
   private final ItemStackIngredient input;
   private final ItemStack mainOutput;
   private final ItemStack secondaryOutput;
   private final double secondaryChance;

   public SawmillRecipe(ResourceLocation id, ItemStackIngredient input, ItemStack mainOutput, ItemStack secondaryOutput, double secondaryChance) {
      super(id);
      this.input = Objects.requireNonNull(input, "Input cannot be null.");
      Objects.requireNonNull(mainOutput, "Main output cannot be null.");
      Objects.requireNonNull(secondaryOutput, "Secondary output cannot be null.");
      if (mainOutput.m_41619_() && secondaryOutput.m_41619_()) {
         throw new IllegalArgumentException("At least one output must not be empty.");
      } else if (!(secondaryChance < 0.0) && !(secondaryChance > 1.0)) {
         if (mainOutput.m_41619_()) {
            if (secondaryChance == 0.0 || secondaryChance == 1.0) {
               throw new IllegalArgumentException("Secondary output must have a chance greater than zero and less than one.");
            }
         } else if (secondaryOutput.m_41619_() && secondaryChance != 0.0) {
            throw new IllegalArgumentException("If there is no secondary output, the chance of getting the secondary output should be zero.");
         }

         this.mainOutput = mainOutput.m_41777_();
         this.secondaryOutput = secondaryOutput.m_41777_();
         this.secondaryChance = secondaryChance;
      } else {
         throw new IllegalArgumentException("Secondary output chance must be at least zero and at most one.");
      }
   }

   public boolean test(ItemStack stack) {
      return this.input.test(stack);
   }

   @Contract("_ -> new")
   public SawmillRecipe.ChanceOutput getOutput(ItemStack input) {
      return new SawmillRecipe.ChanceOutput(this.secondaryChance > 0.0 ? RANDOM.nextDouble() : 0.0);
   }

   public List<ItemStack> getMainOutputDefinition() {
      return this.mainOutput.m_41619_() ? Collections.emptyList() : Collections.singletonList(this.mainOutput);
   }

   public List<ItemStack> getSecondaryOutputDefinition() {
      return this.secondaryOutput.m_41619_() ? Collections.emptyList() : Collections.singletonList(this.secondaryOutput);
   }

   public double getSecondaryChance() {
      return this.secondaryChance;
   }

   public ItemStackIngredient getInput() {
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
      this.input.write(buffer);
      buffer.m_130055_(this.mainOutput);
      buffer.m_130055_(this.secondaryOutput);
      buffer.writeDouble(this.secondaryChance);
   }

   public class ChanceOutput {
      protected final double rand;

      protected ChanceOutput(double rand) {
         this.rand = rand;
      }

      public ItemStack getMainOutput() {
         return SawmillRecipe.this.mainOutput.m_41777_();
      }

      public ItemStack getMaxSecondaryOutput() {
         return SawmillRecipe.this.secondaryChance > 0.0 ? SawmillRecipe.this.secondaryOutput.m_41777_() : ItemStack.f_41583_;
      }

      public ItemStack getSecondaryOutput() {
         return this.rand <= SawmillRecipe.this.secondaryChance ? SawmillRecipe.this.secondaryOutput.m_41777_() : ItemStack.f_41583_;
      }

      public ItemStack nextSecondaryOutput() {
         if (SawmillRecipe.this.secondaryChance > 0.0) {
            double rand = SawmillRecipe.RANDOM.nextDouble();
            if (rand <= SawmillRecipe.this.secondaryChance) {
               return SawmillRecipe.this.secondaryOutput.m_41777_();
            }
         }

         return ItemStack.f_41583_;
      }
   }
}
