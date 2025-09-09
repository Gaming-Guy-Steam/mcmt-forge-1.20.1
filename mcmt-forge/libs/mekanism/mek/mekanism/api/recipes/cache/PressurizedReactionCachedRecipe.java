package mekanism.api.recipes.cache;

import java.util.Objects;
import java.util.function.BooleanSupplier;
import mekanism.api.annotations.NothingNullByDefault;
import mekanism.api.chemical.gas.GasStack;
import mekanism.api.recipes.PressurizedReactionRecipe;
import mekanism.api.recipes.inputs.IInputHandler;
import mekanism.api.recipes.outputs.IOutputHandler;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.fluids.FluidStack;
import org.jetbrains.annotations.Nullable;

@NothingNullByDefault
public class PressurizedReactionCachedRecipe extends CachedRecipe<PressurizedReactionRecipe> {
   private final IOutputHandler<PressurizedReactionRecipe.PressurizedReactionRecipeOutput> outputHandler;
   private final IInputHandler<ItemStack> itemInputHandler;
   private final IInputHandler<FluidStack> fluidInputHandler;
   private final IInputHandler<GasStack> gasInputHandler;
   private ItemStack recipeItem = ItemStack.f_41583_;
   private FluidStack recipeFluid = FluidStack.EMPTY;
   private GasStack recipeGas = GasStack.EMPTY;
   @Nullable
   private PressurizedReactionRecipe.PressurizedReactionRecipeOutput output;

   public PressurizedReactionCachedRecipe(
      PressurizedReactionRecipe recipe,
      BooleanSupplier recheckAllErrors,
      IInputHandler<ItemStack> itemInputHandler,
      IInputHandler<FluidStack> fluidInputHandler,
      IInputHandler<GasStack> gasInputHandler,
      IOutputHandler<PressurizedReactionRecipe.PressurizedReactionRecipeOutput> outputHandler
   ) {
      super(recipe, recheckAllErrors);
      this.itemInputHandler = Objects.requireNonNull(itemInputHandler, "Item input handler cannot be null.");
      this.fluidInputHandler = Objects.requireNonNull(fluidInputHandler, "Fluid input handler cannot be null.");
      this.gasInputHandler = Objects.requireNonNull(gasInputHandler, "Gas input handler cannot be null.");
      this.outputHandler = Objects.requireNonNull(outputHandler, "Output handler cannot be null.");
   }

   @Override
   protected void calculateOperationsThisTick(CachedRecipe.OperationTracker tracker) {
      super.calculateOperationsThisTick(tracker);
      if (tracker.shouldContinueChecking()) {
         this.recipeItem = this.itemInputHandler.getRecipeInput(this.recipe.getInputSolid());
         if (this.recipeItem.m_41619_()) {
            tracker.mismatchedRecipe();
         } else {
            this.recipeFluid = this.fluidInputHandler.getRecipeInput(this.recipe.getInputFluid());
            if (this.recipeFluid.isEmpty()) {
               tracker.mismatchedRecipe();
            } else {
               this.recipeGas = this.gasInputHandler.getRecipeInput(this.recipe.getInputGas());
               if (this.recipeGas.isEmpty()) {
                  tracker.mismatchedRecipe();
               } else {
                  this.itemInputHandler.calculateOperationsCanSupport(tracker, this.recipeItem);
                  if (tracker.shouldContinueChecking()) {
                     this.fluidInputHandler.calculateOperationsCanSupport(tracker, this.recipeFluid);
                     if (tracker.shouldContinueChecking()) {
                        this.gasInputHandler.calculateOperationsCanSupport(tracker, this.recipeGas);
                        if (tracker.shouldContinueChecking()) {
                           this.output = this.recipe.getOutput(this.recipeItem, this.recipeFluid, this.recipeGas);
                           this.outputHandler.calculateOperationsCanSupport(tracker, this.output);
                        }
                     }
                  }
               }
            }
         }
      }
   }

   @Override
   public boolean isInputValid() {
      ItemStack item = this.itemInputHandler.getInput();
      if (item.m_41619_()) {
         return false;
      } else {
         GasStack gas = this.gasInputHandler.getInput();
         if (gas.isEmpty()) {
            return false;
         } else {
            FluidStack fluid = this.fluidInputHandler.getInput();
            return !fluid.isEmpty() && this.recipe.test(item, fluid, gas);
         }
      }
   }

   @Override
   protected void finishProcessing(int operations) {
      if (this.output != null && !this.recipeItem.m_41619_() && !this.recipeFluid.isEmpty() && !this.recipeGas.isEmpty()) {
         this.itemInputHandler.use(this.recipeItem, operations);
         this.fluidInputHandler.use(this.recipeFluid, operations);
         this.gasInputHandler.use(this.recipeGas, operations);
         this.outputHandler.handleOutput(this.output, operations);
      }
   }
}
