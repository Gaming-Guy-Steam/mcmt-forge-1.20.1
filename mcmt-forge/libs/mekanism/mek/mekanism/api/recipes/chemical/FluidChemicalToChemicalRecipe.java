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
import mekanism.api.recipes.ingredients.FluidStackIngredient;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;
import net.minecraftforge.fluids.FluidStack;
import org.jetbrains.annotations.Contract;

@NothingNullByDefault
public abstract class FluidChemicalToChemicalRecipe<CHEMICAL extends Chemical<CHEMICAL>, STACK extends ChemicalStack<CHEMICAL>, INGREDIENT extends ChemicalStackIngredient<CHEMICAL, STACK>>
   extends MekanismRecipe
   implements BiPredicate<FluidStack, STACK> {
   private final FluidStackIngredient fluidInput;
   private final INGREDIENT chemicalInput;
   protected final STACK output;

   public FluidChemicalToChemicalRecipe(ResourceLocation id, FluidStackIngredient fluidInput, INGREDIENT chemicalInput, STACK output) {
      super(id);
      this.fluidInput = Objects.requireNonNull(fluidInput, "Fluid input cannot be null.");
      this.chemicalInput = Objects.requireNonNull(chemicalInput, "Chemical input cannot be null.");
      Objects.requireNonNull(output, "Output cannot be null.");
      if (output.isEmpty()) {
         throw new IllegalArgumentException("Output cannot be empty.");
      } else {
         this.output = (STACK)output.copy();
      }
   }

   public boolean test(FluidStack fluidStack, STACK chemicalStack) {
      return this.fluidInput.test(fluidStack) && this.chemicalInput.test(chemicalStack);
   }

   public FluidStackIngredient getFluidInput() {
      return this.fluidInput;
   }

   public INGREDIENT getChemicalInput() {
      return this.chemicalInput;
   }

   public List<STACK> getOutputDefinition() {
      return Collections.singletonList(this.output);
   }

   @Contract(
      value = "_, _ -> new",
      pure = true
   )
   public STACK getOutput(FluidStack fluidStack, STACK chemicalStack) {
      return (STACK)this.output.copy();
   }

   @Override
   public boolean m_142505_() {
      return this.fluidInput.hasNoMatchingInstances() || this.chemicalInput.hasNoMatchingInstances();
   }

   @Override
   public void logMissingTags() {
      this.fluidInput.logMissingTags();
      this.chemicalInput.logMissingTags();
   }

   @Override
   public void write(FriendlyByteBuf buffer) {
      this.fluidInput.write(buffer);
      this.chemicalInput.write(buffer);
      this.output.writeToPacket(buffer);
   }
}
