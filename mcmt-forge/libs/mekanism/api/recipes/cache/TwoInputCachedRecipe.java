package mekanism.api.recipes.cache;

import java.util.Objects;
import java.util.function.BiFunction;
import java.util.function.BiPredicate;
import java.util.function.BooleanSupplier;
import java.util.function.Predicate;
import java.util.function.Supplier;
import mekanism.api.annotations.NothingNullByDefault;
import mekanism.api.chemical.Chemical;
import mekanism.api.chemical.ChemicalStack;
import mekanism.api.recipes.CombinerRecipe;
import mekanism.api.recipes.MekanismRecipe;
import mekanism.api.recipes.chemical.FluidChemicalToChemicalRecipe;
import mekanism.api.recipes.chemical.ItemStackChemicalToItemStackRecipe;
import mekanism.api.recipes.ingredients.ChemicalStackIngredient;
import mekanism.api.recipes.ingredients.InputIngredient;
import mekanism.api.recipes.inputs.IInputHandler;
import mekanism.api.recipes.outputs.IOutputHandler;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.fluids.FluidStack;
import org.jetbrains.annotations.Nullable;

@NothingNullByDefault
public class TwoInputCachedRecipe<INPUT_A, INPUT_B, OUTPUT, RECIPE extends MekanismRecipe & BiPredicate<INPUT_A, INPUT_B>> extends CachedRecipe<RECIPE> {
   private final IInputHandler<INPUT_A> inputHandler;
   private final IInputHandler<INPUT_B> secondaryInputHandler;
   private final IOutputHandler<OUTPUT> outputHandler;
   private final Predicate<INPUT_A> inputEmptyCheck;
   private final Predicate<INPUT_B> secondaryInputEmptyCheck;
   private final Supplier<? extends InputIngredient<INPUT_A>> inputSupplier;
   private final Supplier<? extends InputIngredient<INPUT_B>> secondaryInputSupplier;
   private final BiFunction<INPUT_A, INPUT_B, OUTPUT> outputGetter;
   private final Predicate<OUTPUT> outputEmptyCheck;
   @Nullable
   private INPUT_A input;
   @Nullable
   private INPUT_B secondaryInput;
   @Nullable
   private OUTPUT output;

   protected TwoInputCachedRecipe(
      RECIPE recipe,
      BooleanSupplier recheckAllErrors,
      IInputHandler<INPUT_A> inputHandler,
      IInputHandler<INPUT_B> secondaryInputHandler,
      IOutputHandler<OUTPUT> outputHandler,
      Supplier<InputIngredient<INPUT_A>> inputSupplier,
      Supplier<InputIngredient<INPUT_B>> secondaryInputSupplier,
      BiFunction<INPUT_A, INPUT_B, OUTPUT> outputGetter,
      Predicate<INPUT_A> inputEmptyCheck,
      Predicate<INPUT_B> secondaryInputEmptyCheck,
      Predicate<OUTPUT> outputEmptyCheck
   ) {
      super(recipe, recheckAllErrors);
      this.inputHandler = Objects.requireNonNull(inputHandler, "Input handler cannot be null.");
      this.secondaryInputHandler = Objects.requireNonNull(secondaryInputHandler, "Secondary input handler cannot be null.");
      this.outputHandler = Objects.requireNonNull(outputHandler, "Output handler cannot be null.");
      this.inputSupplier = Objects.requireNonNull(inputSupplier, "Input ingredient supplier cannot be null.");
      this.secondaryInputSupplier = Objects.requireNonNull(secondaryInputSupplier, "Secondary input ingredient supplier cannot be null.");
      this.outputGetter = Objects.requireNonNull(outputGetter, "Output getter cannot be null.");
      this.inputEmptyCheck = Objects.requireNonNull(inputEmptyCheck, "Input empty check cannot be null.");
      this.secondaryInputEmptyCheck = Objects.requireNonNull(secondaryInputEmptyCheck, "Secondary input empty check cannot be null.");
      this.outputEmptyCheck = Objects.requireNonNull(outputEmptyCheck, "Output empty check cannot be null.");
   }

   @Override
   protected void calculateOperationsThisTick(CachedRecipe.OperationTracker tracker) {
      super.calculateOperationsThisTick(tracker);
      CachedRecipeHelper.twoInputCalculateOperationsThisTick(
         tracker, this.inputHandler, this.inputSupplier, this.secondaryInputHandler, this.secondaryInputSupplier, (input, secondary) -> {
            this.input = input;
            this.secondaryInput = secondary;
         }, this.outputHandler, this.outputGetter, output -> this.output = output, this.inputEmptyCheck, this.secondaryInputEmptyCheck
      );
   }

   @Override
   public boolean isInputValid() {
      INPUT_A input = this.inputHandler.getInput();
      if (this.inputEmptyCheck.test(input)) {
         return false;
      } else {
         INPUT_B secondaryInput = this.secondaryInputHandler.getInput();
         return !this.secondaryInputEmptyCheck.test(secondaryInput) && this.recipe.test(input, secondaryInput);
      }
   }

   @Override
   protected void finishProcessing(int operations) {
      if (this.input != null
         && this.secondaryInput != null
         && this.output != null
         && !this.inputEmptyCheck.test(this.input)
         && !this.secondaryInputEmptyCheck.test(this.secondaryInput)
         && !this.outputEmptyCheck.test(this.output)) {
         this.inputHandler.use(this.input, operations);
         this.secondaryInputHandler.use(this.secondaryInput, operations);
         this.outputHandler.handleOutput(this.output, operations);
      }
   }

   public static <CHEMICAL extends Chemical<CHEMICAL>, STACK extends ChemicalStack<CHEMICAL>, INGREDIENT extends ChemicalStackIngredient<CHEMICAL, STACK>, RECIPE extends FluidChemicalToChemicalRecipe<CHEMICAL, STACK, INGREDIENT>> TwoInputCachedRecipe<FluidStack, STACK, STACK, RECIPE> fluidChemicalToChemical(
      RECIPE recipe,
      BooleanSupplier recheckAllErrors,
      IInputHandler<FluidStack> fluidInputHandler,
      IInputHandler<STACK> chemicalInputHandler,
      IOutputHandler<STACK> outputHandler
   ) {
      return new TwoInputCachedRecipe<>(
         recipe,
         recheckAllErrors,
         fluidInputHandler,
         chemicalInputHandler,
         outputHandler,
         recipe::getFluidInput,
         recipe::getChemicalInput,
         recipe::getOutput,
         FluidStack::isEmpty,
         ChemicalStack::isEmpty,
         ChemicalStack::isEmpty
      );
   }

   public static <CHEMICAL extends Chemical<CHEMICAL>, STACK extends ChemicalStack<CHEMICAL>, INGREDIENT extends ChemicalStackIngredient<CHEMICAL, STACK>, RECIPE extends ItemStackChemicalToItemStackRecipe<CHEMICAL, STACK, INGREDIENT>> TwoInputCachedRecipe<ItemStack, STACK, ItemStack, RECIPE> itemChemicalToItem(
      RECIPE recipe,
      BooleanSupplier recheckAllErrors,
      IInputHandler<ItemStack> itemInputHandler,
      IInputHandler<STACK> chemicalInputHandler,
      IOutputHandler<ItemStack> outputHandler
   ) {
      return new TwoInputCachedRecipe<>(
         recipe,
         recheckAllErrors,
         itemInputHandler,
         chemicalInputHandler,
         outputHandler,
         recipe::getItemInput,
         recipe::getChemicalInput,
         recipe::getOutput,
         ItemStack::m_41619_,
         ChemicalStack::isEmpty,
         ItemStack::m_41619_
      );
   }

   public static TwoInputCachedRecipe<ItemStack, ItemStack, ItemStack, CombinerRecipe> combiner(
      CombinerRecipe recipe,
      BooleanSupplier recheckAllErrors,
      IInputHandler<ItemStack> inputHandler,
      IInputHandler<ItemStack> extraInputHandler,
      IOutputHandler<ItemStack> outputHandler
   ) {
      return new TwoInputCachedRecipe<>(
         (RECIPE)recipe,
         recheckAllErrors,
         inputHandler,
         extraInputHandler,
         outputHandler,
         recipe::getMainInput,
         recipe::getExtraInput,
         recipe::getOutput,
         ItemStack::m_41619_,
         ItemStack::m_41619_,
         ItemStack::m_41619_
      );
   }
}
