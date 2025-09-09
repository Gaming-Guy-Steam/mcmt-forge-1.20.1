package mekanism.api.recipes;

import java.util.Collections;
import java.util.List;
import java.util.Objects;
import mekanism.api.annotations.NothingNullByDefault;
import mekanism.api.chemical.gas.GasStack;
import mekanism.api.math.FloatingLong;
import mekanism.api.recipes.ingredients.ChemicalStackIngredient;
import mekanism.api.recipes.ingredients.FluidStackIngredient;
import mekanism.api.recipes.ingredients.ItemStackIngredient;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.common.util.TriPredicate;
import net.minecraftforge.fluids.FluidStack;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;

@NothingNullByDefault
public abstract class PressurizedReactionRecipe extends MekanismRecipe implements TriPredicate<ItemStack, FluidStack, GasStack> {
   private final ItemStackIngredient inputSolid;
   private final FluidStackIngredient inputFluid;
   private final ChemicalStackIngredient.GasStackIngredient inputGas;
   private final FloatingLong energyRequired;
   private final int duration;
   private final ItemStack outputItem;
   private final GasStack outputGas;

   public PressurizedReactionRecipe(
      ResourceLocation id,
      ItemStackIngredient inputSolid,
      FluidStackIngredient inputFluid,
      ChemicalStackIngredient.GasStackIngredient inputGas,
      FloatingLong energyRequired,
      int duration,
      ItemStack outputItem,
      GasStack outputGas
   ) {
      super(id);
      this.inputSolid = Objects.requireNonNull(inputSolid, "Item input cannot be null.");
      this.inputFluid = Objects.requireNonNull(inputFluid, "Fluid input cannot be null.");
      this.inputGas = Objects.requireNonNull(inputGas, "Gas input cannot be null.");
      this.energyRequired = Objects.requireNonNull(energyRequired, "Required energy cannot be null.").copyAsConst();
      if (duration <= 0) {
         throw new IllegalArgumentException("Duration must be positive.");
      } else {
         this.duration = duration;
         Objects.requireNonNull(outputItem, "Item output cannot be null.");
         Objects.requireNonNull(outputGas, "Gas output cannot be null.");
         if (outputItem.m_41619_() && outputGas.isEmpty()) {
            throw new IllegalArgumentException("At least one output must not be empty.");
         } else {
            Objects.requireNonNull(outputItem, "Item output cannot be null.");
            Objects.requireNonNull(outputGas, "Gas output cannot be null.");
            if (outputItem.m_41619_() && outputGas.isEmpty()) {
               throw new IllegalArgumentException("At least one output must not be empty.");
            } else {
               this.outputItem = outputItem.m_41777_();
               this.outputGas = outputGas.copy();
            }
         }
      }
   }

   public ItemStackIngredient getInputSolid() {
      return this.inputSolid;
   }

   public FluidStackIngredient getInputFluid() {
      return this.inputFluid;
   }

   public ChemicalStackIngredient.GasStackIngredient getInputGas() {
      return this.inputGas;
   }

   public FloatingLong getEnergyRequired() {
      return this.energyRequired;
   }

   public int getDuration() {
      return this.duration;
   }

   public boolean test(ItemStack solid, FluidStack liquid, GasStack gas) {
      return this.inputSolid.test(solid) && this.inputFluid.test(liquid) && this.inputGas.test(gas);
   }

   public List<PressurizedReactionRecipe.PressurizedReactionRecipeOutput> getOutputDefinition() {
      return Collections.singletonList(new PressurizedReactionRecipe.PressurizedReactionRecipeOutput(this.outputItem, this.outputGas));
   }

   @Contract(
      value = "_, _, _ -> new",
      pure = true
   )
   public PressurizedReactionRecipe.PressurizedReactionRecipeOutput getOutput(ItemStack solid, FluidStack liquid, GasStack gas) {
      return new PressurizedReactionRecipe.PressurizedReactionRecipeOutput(this.outputItem.m_41777_(), this.outputGas.copy());
   }

   @Override
   public boolean m_142505_() {
      return this.inputSolid.hasNoMatchingInstances() || this.inputFluid.hasNoMatchingInstances() || this.inputGas.hasNoMatchingInstances();
   }

   @Override
   public void logMissingTags() {
      this.inputSolid.logMissingTags();
      this.inputFluid.logMissingTags();
      this.inputGas.logMissingTags();
   }

   @Override
   public void write(FriendlyByteBuf buffer) {
      this.inputSolid.write(buffer);
      this.inputFluid.write(buffer);
      this.inputGas.write(buffer);
      this.energyRequired.writeToBuffer(buffer);
      buffer.m_130130_(this.duration);
      buffer.m_130055_(this.outputItem);
      this.outputGas.writeToPacket(buffer);
   }

   public record PressurizedReactionRecipeOutput(@NotNull ItemStack item, @NotNull GasStack gas) {
      public PressurizedReactionRecipeOutput(@NotNull ItemStack item, @NotNull GasStack gas) {
         Objects.requireNonNull(item, "Item output cannot be null.");
         Objects.requireNonNull(gas, "Gas output cannot be null.");
         if (item.m_41619_() && gas.isEmpty()) {
            throw new IllegalArgumentException("At least one output must be present.");
         } else {
            this.item = item;
            this.gas = gas;
         }
      }
   }
}
