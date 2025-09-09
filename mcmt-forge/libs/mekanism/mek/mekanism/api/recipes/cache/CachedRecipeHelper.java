package mekanism.api.recipes.cache;

import java.util.function.BiConsumer;
import java.util.function.BiFunction;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.function.Supplier;
import mekanism.api.recipes.ingredients.InputIngredient;
import mekanism.api.recipes.inputs.IInputHandler;
import mekanism.api.recipes.outputs.IOutputHandler;

public class CachedRecipeHelper {
   private CachedRecipeHelper() {
   }

   public static <INPUT, OUTPUT> void oneInputCalculateOperationsThisTick(
      CachedRecipe.OperationTracker tracker,
      IInputHandler<INPUT> inputHandler,
      Supplier<? extends InputIngredient<INPUT>> inputIngredient,
      Consumer<INPUT> inputSetter,
      IOutputHandler<OUTPUT> outputHandler,
      Function<INPUT, OUTPUT> outputGetter,
      Consumer<OUTPUT> outputSetter,
      Predicate<INPUT> emptyCheck
   ) {
      if (tracker.shouldContinueChecking()) {
         INPUT input = inputHandler.getRecipeInput((InputIngredient<INPUT>)inputIngredient.get());
         inputSetter.accept(input);
         if (emptyCheck.test(input)) {
            tracker.mismatchedRecipe();
         } else {
            inputHandler.calculateOperationsCanSupport(tracker, input);
            if (tracker.shouldContinueChecking()) {
               OUTPUT output = outputGetter.apply(input);
               outputSetter.accept(output);
               outputHandler.calculateOperationsCanSupport(tracker, output);
            }
         }
      }
   }

   public static <INPUT_A, INPUT_B, OUTPUT> void twoInputCalculateOperationsThisTick(
      CachedRecipe.OperationTracker tracker,
      IInputHandler<INPUT_A> inputAHandler,
      Supplier<? extends InputIngredient<INPUT_A>> inputAIngredient,
      IInputHandler<INPUT_B> inputBHandler,
      Supplier<? extends InputIngredient<INPUT_B>> inputBIngredient,
      BiConsumer<INPUT_A, INPUT_B> inputsSetter,
      IOutputHandler<OUTPUT> outputHandler,
      BiFunction<INPUT_A, INPUT_B, OUTPUT> outputGetter,
      Consumer<OUTPUT> outputSetter,
      Predicate<INPUT_A> emptyCheckA,
      Predicate<INPUT_B> emptyCheckB
   ) {
      if (tracker.shouldContinueChecking()) {
         INPUT_A inputA = inputAHandler.getRecipeInput((InputIngredient<INPUT_A>)inputAIngredient.get());
         if (emptyCheckA.test(inputA)) {
            tracker.mismatchedRecipe();
         } else {
            INPUT_B inputB = inputBHandler.getRecipeInput((InputIngredient<INPUT_B>)inputBIngredient.get());
            if (emptyCheckB.test(inputB)) {
               tracker.mismatchedRecipe();
            } else {
               inputsSetter.accept(inputA, inputB);
               inputAHandler.calculateOperationsCanSupport(tracker, inputA);
               if (tracker.shouldContinueChecking()) {
                  inputBHandler.calculateOperationsCanSupport(tracker, inputB);
                  if (tracker.shouldContinueChecking()) {
                     OUTPUT output = outputGetter.apply(inputA, inputB);
                     outputSetter.accept(output);
                     outputHandler.calculateOperationsCanSupport(tracker, output);
                  }
               }
            }
         }
      }
   }
}
