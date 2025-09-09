package mekanism.api.recipes;

import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.function.Predicate;
import mekanism.api.annotations.NothingNullByDefault;
import mekanism.api.chemical.gas.GasStack;
import mekanism.api.math.FloatingLong;
import mekanism.api.recipes.ingredients.FluidStackIngredient;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;
import net.minecraftforge.fluids.FluidStack;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;

@NothingNullByDefault
public abstract class ElectrolysisRecipe extends MekanismRecipe implements Predicate<FluidStack> {
   private final FluidStackIngredient input;
   private final GasStack leftGasOutput;
   private final GasStack rightGasOutput;
   private final FloatingLong energyMultiplier;

   public ElectrolysisRecipe(ResourceLocation id, FluidStackIngredient input, FloatingLong energyMultiplier, GasStack leftGasOutput, GasStack rightGasOutput) {
      super(id);
      this.input = Objects.requireNonNull(input, "Input cannot be null.");
      this.energyMultiplier = Objects.requireNonNull(energyMultiplier, "Energy multiplier cannot be null.").copyAsConst();
      if (energyMultiplier.smallerThan(FloatingLong.ONE)) {
         throw new IllegalArgumentException("Energy multiplier must be at least one.");
      } else {
         Objects.requireNonNull(leftGasOutput, "Left output cannot be null");
         Objects.requireNonNull(rightGasOutput, "Right output cannot be null");
         if (leftGasOutput.isEmpty()) {
            throw new IllegalArgumentException("Left output cannot be empty.");
         } else if (rightGasOutput.isEmpty()) {
            throw new IllegalArgumentException("Right output cannot be empty.");
         } else {
            this.leftGasOutput = leftGasOutput.copy();
            this.rightGasOutput = rightGasOutput.copy();
         }
      }
   }

   public FluidStackIngredient getInput() {
      return this.input;
   }

   public List<ElectrolysisRecipe.ElectrolysisRecipeOutput> getOutputDefinition() {
      return Collections.singletonList(new ElectrolysisRecipe.ElectrolysisRecipeOutput(this.leftGasOutput, this.rightGasOutput));
   }

   public boolean test(FluidStack fluidStack) {
      return this.input.test(fluidStack);
   }

   @Contract(
      value = "_ -> new",
      pure = true
   )
   public ElectrolysisRecipe.ElectrolysisRecipeOutput getOutput(FluidStack input) {
      return new ElectrolysisRecipe.ElectrolysisRecipeOutput(this.leftGasOutput.copy(), this.rightGasOutput.copy());
   }

   public FloatingLong getEnergyMultiplier() {
      return this.energyMultiplier;
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
      this.energyMultiplier.writeToBuffer(buffer);
      this.leftGasOutput.writeToPacket(buffer);
      this.rightGasOutput.writeToPacket(buffer);
   }

   public record ElectrolysisRecipeOutput(@NotNull GasStack left, @NotNull GasStack right) {
      public ElectrolysisRecipeOutput(@NotNull GasStack left, @NotNull GasStack right) {
         Objects.requireNonNull(left, "Left output cannot be null.");
         Objects.requireNonNull(right, "Right output cannot be null.");
         if (left.isEmpty()) {
            throw new IllegalArgumentException("Left output cannot be empty.");
         } else if (right.isEmpty()) {
            throw new IllegalArgumentException("Right output cannot be empty.");
         } else {
            this.left = left;
            this.right = right;
         }
      }
   }
}
