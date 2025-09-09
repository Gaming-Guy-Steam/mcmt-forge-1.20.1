package mekanism.api.recipes;

import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.function.Predicate;
import mekanism.api.annotations.NothingNullByDefault;
import mekanism.api.recipes.ingredients.FluidStackIngredient;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;
import net.minecraftforge.fluids.FluidStack;
import org.jetbrains.annotations.Contract;

@NothingNullByDefault
public abstract class FluidToFluidRecipe extends MekanismRecipe implements Predicate<FluidStack> {
   private final FluidStackIngredient input;
   private final FluidStack output;

   public FluidToFluidRecipe(ResourceLocation id, FluidStackIngredient input, FluidStack output) {
      super(id);
      this.input = Objects.requireNonNull(input, "Input cannot be null.");
      Objects.requireNonNull(output, "Output cannot be null.");
      if (output.isEmpty()) {
         throw new IllegalArgumentException("Output cannot be empty.");
      } else {
         this.output = output.copy();
      }
   }

   public boolean test(FluidStack fluidStack) {
      return this.input.test(fluidStack);
   }

   public FluidStackIngredient getInput() {
      return this.input;
   }

   public List<FluidStack> getOutputDefinition() {
      return Collections.singletonList(this.output);
   }

   @Contract(
      value = "_ ->new",
      pure = true
   )
   public FluidStack getOutput(FluidStack input) {
      return this.output.copy();
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
