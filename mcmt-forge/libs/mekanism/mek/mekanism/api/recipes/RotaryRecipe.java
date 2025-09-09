package mekanism.api.recipes;

import java.util.Collections;
import java.util.List;
import java.util.Objects;
import mekanism.api.annotations.NothingNullByDefault;
import mekanism.api.chemical.gas.GasStack;
import mekanism.api.recipes.ingredients.ChemicalStackIngredient;
import mekanism.api.recipes.ingredients.FluidStackIngredient;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;
import net.minecraftforge.fluids.FluidStack;
import org.jetbrains.annotations.Contract;

@NothingNullByDefault
public abstract class RotaryRecipe extends MekanismRecipe {
   private final ChemicalStackIngredient.GasStackIngredient gasInput;
   private final FluidStackIngredient fluidInput;
   private final FluidStack fluidOutput;
   private final GasStack gasOutput;
   private final boolean hasGasToFluid;
   private final boolean hasFluidToGas;

   public RotaryRecipe(ResourceLocation id, FluidStackIngredient fluidInput, GasStack gasOutput) {
      super(id);
      this.fluidInput = Objects.requireNonNull(fluidInput, "Fluid input cannot be null.");
      Objects.requireNonNull(gasOutput, "Gas output cannot be null.");
      if (gasOutput.isEmpty()) {
         throw new IllegalArgumentException("Gas output cannot be empty.");
      } else {
         this.gasOutput = gasOutput.copy();
         this.gasInput = null;
         this.fluidOutput = FluidStack.EMPTY;
         this.hasGasToFluid = false;
         this.hasFluidToGas = true;
      }
   }

   public RotaryRecipe(ResourceLocation id, ChemicalStackIngredient.GasStackIngredient gasInput, FluidStack fluidOutput) {
      super(id);
      this.gasInput = Objects.requireNonNull(gasInput, "Gas input cannot be null.");
      Objects.requireNonNull(fluidOutput, "Fluid output cannot be null.");
      if (fluidOutput.isEmpty()) {
         throw new IllegalArgumentException("Fluid output cannot be empty.");
      } else {
         this.fluidOutput = fluidOutput.copy();
         this.fluidInput = null;
         this.gasOutput = GasStack.EMPTY;
         this.hasGasToFluid = true;
         this.hasFluidToGas = false;
      }
   }

   public RotaryRecipe(
      ResourceLocation id, FluidStackIngredient fluidInput, ChemicalStackIngredient.GasStackIngredient gasInput, GasStack gasOutput, FluidStack fluidOutput
   ) {
      super(id);
      this.gasInput = Objects.requireNonNull(gasInput, "Gas input cannot be null.");
      this.fluidInput = Objects.requireNonNull(fluidInput, "Fluid input cannot be null.");
      Objects.requireNonNull(gasOutput, "Gas output cannot be null.");
      Objects.requireNonNull(fluidOutput, "Fluid output cannot be null.");
      if (gasOutput.isEmpty()) {
         throw new IllegalArgumentException("Gas output cannot be empty.");
      } else if (fluidOutput.isEmpty()) {
         throw new IllegalArgumentException("Fluid output cannot be empty.");
      } else {
         this.gasOutput = gasOutput.copy();
         this.fluidOutput = fluidOutput.copy();
         this.hasGasToFluid = true;
         this.hasFluidToGas = true;
      }
   }

   public boolean hasGasToFluid() {
      return this.hasGasToFluid;
   }

   public boolean hasFluidToGas() {
      return this.hasFluidToGas;
   }

   protected void assertHasGasToFluid() {
      if (!this.hasGasToFluid()) {
         throw new IllegalStateException("This recipe has no gas to fluid conversion.");
      }
   }

   protected void assertHasFluidToGas() {
      if (!this.hasFluidToGas()) {
         throw new IllegalStateException("This recipe has no fluid to gas conversion.");
      }
   }

   public boolean test(FluidStack fluidStack) {
      return this.hasFluidToGas() && this.fluidInput.test(fluidStack);
   }

   public boolean test(GasStack gasStack) {
      return this.hasGasToFluid() && this.gasInput.test(gasStack);
   }

   public FluidStackIngredient getFluidInput() {
      this.assertHasFluidToGas();
      return this.fluidInput;
   }

   public ChemicalStackIngredient.GasStackIngredient getGasInput() {
      this.assertHasGasToFluid();
      return this.gasInput;
   }

   public List<GasStack> getGasOutputDefinition() {
      this.assertHasFluidToGas();
      return Collections.singletonList(this.gasOutput);
   }

   public List<FluidStack> getFluidOutputDefinition() {
      this.assertHasGasToFluid();
      return Collections.singletonList(this.fluidOutput);
   }

   @Contract(
      value = "_ -> new",
      pure = true
   )
   public GasStack getGasOutput(FluidStack input) {
      this.assertHasFluidToGas();
      return this.gasOutput.copy();
   }

   @Contract(
      value = "_ -> new",
      pure = true
   )
   public FluidStack getFluidOutput(GasStack input) {
      this.assertHasGasToFluid();
      return this.fluidOutput.copy();
   }

   @Override
   public boolean m_142505_() {
      return this.hasFluidToGas && this.fluidInput.hasNoMatchingInstances() || this.hasGasToFluid && this.gasInput.hasNoMatchingInstances();
   }

   @Override
   public void logMissingTags() {
      if (this.hasFluidToGas) {
         this.fluidInput.logMissingTags();
      }

      if (this.hasGasToFluid) {
         this.gasInput.logMissingTags();
      }
   }

   @Override
   public void write(FriendlyByteBuf buffer) {
      buffer.writeBoolean(this.hasFluidToGas);
      if (this.hasFluidToGas) {
         this.fluidInput.write(buffer);
         this.gasOutput.writeToPacket(buffer);
      }

      buffer.writeBoolean(this.hasGasToFluid);
      if (this.hasGasToFluid) {
         this.gasInput.write(buffer);
         this.fluidOutput.writeToPacket(buffer);
      }
   }
}
