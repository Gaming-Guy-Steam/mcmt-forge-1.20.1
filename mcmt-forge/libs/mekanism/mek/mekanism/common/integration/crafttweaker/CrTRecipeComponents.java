package mekanism.common.integration.crafttweaker;

import com.blamejared.crafttweaker.api.fluid.IFluidStack;
import com.blamejared.crafttweaker.api.item.IItemStack;
import com.blamejared.crafttweaker.api.recipe.component.DecomposedRecipeBuilder;
import com.blamejared.crafttweaker.api.recipe.component.IRecipeComponent;
import com.blamejared.crafttweaker.api.recipe.component.RecipeComponentEqualityCheckers;
import com.blamejared.crafttweaker.api.recipe.component.BuiltinRecipeComponents.Output;
import com.google.gson.reflect.TypeToken;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import mekanism.api.chemical.Chemical;
import mekanism.api.chemical.ChemicalStack;
import mekanism.api.chemical.ChemicalType;
import mekanism.api.chemical.gas.Gas;
import mekanism.api.chemical.gas.GasStack;
import mekanism.api.chemical.infuse.InfuseType;
import mekanism.api.chemical.infuse.InfusionStack;
import mekanism.api.chemical.pigment.Pigment;
import mekanism.api.chemical.pigment.PigmentStack;
import mekanism.api.chemical.slurry.Slurry;
import mekanism.api.chemical.slurry.SlurryStack;
import mekanism.api.math.FloatingLong;
import mekanism.api.recipes.ingredients.ChemicalStackIngredient;
import mekanism.api.recipes.ingredients.FluidStackIngredient;
import mekanism.api.recipes.ingredients.InputIngredient;
import mekanism.api.recipes.ingredients.ItemStackIngredient;
import mekanism.api.recipes.ingredients.creator.IChemicalStackIngredientCreator;
import mekanism.api.recipes.ingredients.creator.IngredientCreatorAccess;
import mekanism.common.Mekanism;
import mekanism.common.integration.crafttweaker.chemical.ICrTChemicalStack;
import mekanism.common.recipe.ingredient.IMultiIngredient;

public class CrTRecipeComponents {
   public static final IRecipeComponent<Double> CHANCE = IRecipeComponent.simple(
      Mekanism.rl("chance"), new TypeToken<Double>() {}, RecipeComponentEqualityCheckers::areNumbersEqual
   );
   public static final IRecipeComponent<FloatingLong> ENERGY = IRecipeComponent.simple(
      Mekanism.rl("energy"), new TypeToken<FloatingLong>() {}, FloatingLong::equals
   );
   public static final CrTRecipeComponents.PairedRecipeComponent<ItemStackIngredient, IItemStack> ITEM = new CrTRecipeComponents.PairedRecipeComponent<>(
      IRecipeComponent.composite(
         Mekanism.rl("input/item"),
         new TypeToken<ItemStackIngredient>() {},
         CrTRecipeComponents::ingredientsMatch,
         CrTRecipeComponents::unwrapIngredient,
         ingredients -> IngredientCreatorAccess.item().from(ingredients.stream())
      ),
      Output.ITEMS
   );
   public static final CrTRecipeComponents.PairedRecipeComponent<FluidStackIngredient, IFluidStack> FLUID = new CrTRecipeComponents.PairedRecipeComponent<>(
      IRecipeComponent.composite(
         Mekanism.rl("input/fluid"),
         new TypeToken<FluidStackIngredient>() {},
         CrTRecipeComponents::ingredientsMatch,
         CrTRecipeComponents::unwrapIngredient,
         ingredients -> IngredientCreatorAccess.fluid().from(ingredients.stream())
      ),
      IRecipeComponent.simple(Mekanism.rl("output/fluid"), new TypeToken<IFluidStack>() {}, RecipeComponentEqualityCheckers::areFluidStacksEqual)
   );
   public static final CrTRecipeComponents.ChemicalRecipeComponent<Gas, GasStack, ChemicalStackIngredient.GasStackIngredient, ICrTChemicalStack.ICrTGasStack> GAS = new CrTRecipeComponents.ChemicalRecipeComponent<>(
      ChemicalType.GAS, new TypeToken<ChemicalStackIngredient.GasStackIngredient>() {}, new TypeToken<ICrTChemicalStack.ICrTGasStack>() {}
   );
   public static final CrTRecipeComponents.ChemicalRecipeComponent<InfuseType, InfusionStack, ChemicalStackIngredient.InfusionStackIngredient, ICrTChemicalStack.ICrTInfusionStack> INFUSION = new CrTRecipeComponents.ChemicalRecipeComponent<>(
      ChemicalType.INFUSION, new TypeToken<ChemicalStackIngredient.InfusionStackIngredient>() {}, new TypeToken<ICrTChemicalStack.ICrTInfusionStack>() {}
   );
   public static final CrTRecipeComponents.ChemicalRecipeComponent<Pigment, PigmentStack, ChemicalStackIngredient.PigmentStackIngredient, ICrTChemicalStack.ICrTPigmentStack> PIGMENT = new CrTRecipeComponents.ChemicalRecipeComponent<>(
      ChemicalType.PIGMENT, new TypeToken<ChemicalStackIngredient.PigmentStackIngredient>() {}, new TypeToken<ICrTChemicalStack.ICrTPigmentStack>() {}
   );
   public static final CrTRecipeComponents.ChemicalRecipeComponent<Slurry, SlurryStack, ChemicalStackIngredient.SlurryStackIngredient, ICrTChemicalStack.ICrTSlurryStack> SLURRY = new CrTRecipeComponents.ChemicalRecipeComponent<>(
      ChemicalType.SLURRY, new TypeToken<ChemicalStackIngredient.SlurryStackIngredient>() {}, new TypeToken<ICrTChemicalStack.ICrTSlurryStack>() {}
   );
   public static final List<CrTRecipeComponents.ChemicalRecipeComponent<?, ?, ?, ?>> CHEMICAL_COMPONENTS = List.of(GAS, INFUSION, PIGMENT, SLURRY);

   private CrTRecipeComponents() {
   }

   private static <TYPE, INGREDIENT extends InputIngredient<TYPE>> boolean ingredientsMatch(INGREDIENT a, INGREDIENT b) {
      return Objects.equals(a, b) || a.getRepresentations().stream().allMatch(b) && b.getRepresentations().stream().allMatch(a);
   }

   private static <TYPE, INGREDIENT extends InputIngredient<TYPE>> Collection<INGREDIENT> unwrapIngredient(INGREDIENT ingredient) {
      return ingredient instanceof IMultiIngredient ? ((IMultiIngredient)ingredient).getIngredients() : Collections.singletonList(ingredient);
   }

   public record ChemicalRecipeComponent<CHEMICAL extends Chemical<CHEMICAL>, STACK extends ChemicalStack<CHEMICAL>, INGREDIENT extends ChemicalStackIngredient<CHEMICAL, STACK>, CRT_STACK extends ICrTChemicalStack<CHEMICAL, STACK, CRT_STACK>>(
      ChemicalType chemicalType, IRecipeComponent<INGREDIENT> input, IRecipeComponent<CRT_STACK> output
   ) {
      ChemicalRecipeComponent(ChemicalType chemicalType, TypeToken<INGREDIENT> inputType, TypeToken<CRT_STACK> outputType) {
         this(
            chemicalType,
            inputType,
            outputType,
            (IChemicalStackIngredientCreator<CHEMICAL, STACK, INGREDIENT>)IngredientCreatorAccess.getCreatorForType(chemicalType)
         );
      }

      private ChemicalRecipeComponent(
         ChemicalType chemicalType,
         TypeToken<INGREDIENT> inputType,
         TypeToken<CRT_STACK> outputType,
         IChemicalStackIngredientCreator<CHEMICAL, STACK, INGREDIENT> ingredientCreator
      ) {
         this(
            chemicalType,
            IRecipeComponent.composite(
               Mekanism.rl("input/" + chemicalType.m_7912_()),
               inputType,
               CrTRecipeComponents::ingredientsMatch,
               CrTRecipeComponents::unwrapIngredient,
               ingredients -> ingredientCreator.from(ingredients.stream())
            ),
            IRecipeComponent.simple(Mekanism.rl("output/" + chemicalType.m_7912_()), outputType, ICrTChemicalStack::containsOther)
         );
      }

      public DecomposedRecipeBuilder withOutput(DecomposedRecipeBuilder builder, List<STACK> output) {
         return builder.with(this.output(), CrTUtils.convertChemical(output));
      }
   }

   public record PairedRecipeComponent<INPUT, OUTPUT>(IRecipeComponent<INPUT> input, IRecipeComponent<OUTPUT> output) {
   }
}
