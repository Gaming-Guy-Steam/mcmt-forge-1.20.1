package mekanism.common.recipe.ingredient.chemical;

import mekanism.api.chemical.Chemical;
import mekanism.api.chemical.ChemicalStack;
import mekanism.api.chemical.ChemicalTags;
import mekanism.api.chemical.ChemicalUtils;
import mekanism.api.chemical.IEmptyStackProvider;
import mekanism.api.chemical.gas.Gas;
import mekanism.api.chemical.gas.GasStack;
import mekanism.api.chemical.infuse.InfuseType;
import mekanism.api.chemical.infuse.InfusionStack;
import mekanism.api.chemical.pigment.Pigment;
import mekanism.api.chemical.pigment.PigmentStack;
import mekanism.api.chemical.slurry.Slurry;
import mekanism.api.chemical.slurry.SlurryStack;
import org.jetbrains.annotations.NotNull;

public class ChemicalIngredientInfo<CHEMICAL extends Chemical<CHEMICAL>, STACK extends ChemicalStack<CHEMICAL>> implements IEmptyStackProvider<CHEMICAL, STACK> {
   public static final ChemicalIngredientInfo<Gas, GasStack> GAS = new ChemicalIngredientInfo<>(
      GasStack.EMPTY, GasStack::new, GasStack::new, "gas", ChemicalTags.GAS
   );
   public static final ChemicalIngredientInfo<InfuseType, InfusionStack> INFUSION = new ChemicalIngredientInfo<>(
      InfusionStack.EMPTY, InfusionStack::new, InfusionStack::new, "infuse_type", ChemicalTags.INFUSE_TYPE
   );
   public static final ChemicalIngredientInfo<Pigment, PigmentStack> PIGMENT = new ChemicalIngredientInfo<>(
      PigmentStack.EMPTY, PigmentStack::new, PigmentStack::new, "pigment", ChemicalTags.PIGMENT
   );
   public static final ChemicalIngredientInfo<Slurry, SlurryStack> SLURRY = new ChemicalIngredientInfo<>(
      SlurryStack.EMPTY, SlurryStack::new, SlurryStack::new, "slurry", ChemicalTags.SLURRY
   );
   private final ChemicalUtils.ChemicalToStackCreator<CHEMICAL, STACK> chemicalToStackCreator;
   private final ChemicalUtils.StackToStackCreator<STACK> stackToStackCreator;
   private final ChemicalTags<CHEMICAL> tags;
   private final String serializationKey;
   private final STACK emptyStack;

   private ChemicalIngredientInfo(
      STACK emptyStack,
      ChemicalUtils.ChemicalToStackCreator<CHEMICAL, STACK> chemicalToStackCreator,
      ChemicalUtils.StackToStackCreator<STACK> stackToStackCreator,
      String serializationKey,
      ChemicalTags<CHEMICAL> tags
   ) {
      this.chemicalToStackCreator = chemicalToStackCreator;
      this.stackToStackCreator = stackToStackCreator;
      this.serializationKey = serializationKey;
      this.emptyStack = emptyStack;
      this.tags = tags;
   }

   public String getSerializationKey() {
      return this.serializationKey;
   }

   @NotNull
   @Override
   public STACK getEmptyStack() {
      return this.emptyStack;
   }

   public STACK createStack(CHEMICAL chemical, long amount) {
      return this.chemicalToStackCreator.createStack(chemical, amount);
   }

   public STACK createStack(STACK stack, long amount) {
      return this.stackToStackCreator.createStack(stack, amount);
   }
}
