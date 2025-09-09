package mekanism.common.integration.crafttweaker.chemical;

import com.blamejared.crafttweaker.api.annotation.ZenRegister;
import com.blamejared.crafttweaker.api.bracket.CommandStringDisplayable;
import mekanism.api.chemical.Chemical;
import mekanism.api.chemical.ChemicalStack;
import mekanism.api.chemical.gas.Gas;
import mekanism.api.chemical.gas.GasStack;
import mekanism.api.chemical.infuse.InfuseType;
import mekanism.api.chemical.infuse.InfusionStack;
import mekanism.api.chemical.pigment.Pigment;
import mekanism.api.chemical.pigment.PigmentStack;
import mekanism.api.chemical.slurry.Slurry;
import mekanism.api.chemical.slurry.SlurryStack;
import mekanism.api.recipes.ingredients.ChemicalStackIngredient;
import mekanism.common.integration.crafttweaker.bracket.IBracketSupport;
import mekanism.common.integration.crafttweaker.ingredient.CrTGasStackIngredient;
import mekanism.common.integration.crafttweaker.ingredient.CrTInfusionStackIngredient;
import mekanism.common.integration.crafttweaker.ingredient.CrTPigmentStackIngredient;
import mekanism.common.integration.crafttweaker.ingredient.CrTSlurryStackIngredient;
import net.minecraft.resources.ResourceLocation;
import org.openzen.zencode.java.ZenCodeType.Caster;
import org.openzen.zencode.java.ZenCodeType.Getter;
import org.openzen.zencode.java.ZenCodeType.Method;
import org.openzen.zencode.java.ZenCodeType.Name;
import org.openzen.zencode.java.ZenCodeType.Operator;
import org.openzen.zencode.java.ZenCodeType.OperatorType;

@ZenRegister
@Name("mods.mekanism.api.chemical.ChemicalStack")
public interface ICrTChemicalStack<CHEMICAL extends Chemical<CHEMICAL>, STACK extends ChemicalStack<CHEMICAL>, CRT_STACK extends ICrTChemicalStack<CHEMICAL, STACK, CRT_STACK>>
   extends CommandStringDisplayable,
   IBracketSupport {
   @Method
   @Getter("registryName")
   default ResourceLocation getRegistryName() {
      return this.getInternal().getTypeRegistryName();
   }

   @Method
   @Getter("empty")
   default boolean isEmpty() {
      return this.getInternal().isEmpty();
   }

   @Method
   @Getter("amount")
   default long getAmount() {
      return this.getInternal().getAmount();
   }

   @Method
   CRT_STACK setAmount(long amount);

   @Operator(OperatorType.MUL)
   default CRT_STACK multiply(long amount) {
      return this.setAmount(this.getAmount() * amount);
   }

   @Method
   default CRT_STACK grow(long amount) {
      return this.setAmount(this.getAmount() + amount);
   }

   @Method
   default CRT_STACK shrink(long amount) {
      return this.setAmount(this.getAmount() - amount);
   }

   @Method
   @Operator(OperatorType.CONTAINS)
   default boolean containsOther(CRT_STACK stack) {
      return this.getAmount() >= stack.getAmount() && this.isTypeEqual(stack);
   }

   @Method
   CRT_STACK asMutable();

   @Method
   CRT_STACK asImmutable();

   @Method
   CRT_STACK copy();

   @Method
   @Getter("type")
   @Caster(
      implicit = true
   )
   default CHEMICAL getType() {
      return this.getInternal().getType();
   }

   STACK getInternal();

   default STACK getImmutableInternal() {
      return this.copy().getInternal();
   }

   @Method
   @Operator(OperatorType.CONTAINS)
   default boolean isTypeEqual(CRT_STACK stack) {
      return this.getInternal().isTypeEqual(stack.getInternal());
   }

   @Method
   @Operator(OperatorType.EQUALS)
   default boolean isEqual(CRT_STACK other) {
      return this.equals(other);
   }

   @Caster(
      implicit = true
   )
   ChemicalStackIngredient<?, ?> asChemicalStackIngredient();

   @ZenRegister
   @Name("mods.mekanism.api.chemical.GasStack")
   public interface ICrTGasStack extends ICrTChemicalStack<Gas, GasStack, ICrTChemicalStack.ICrTGasStack>, IBracketSupport.IGasBracketSupport {
      @Override
      default ChemicalStackIngredient<Gas, GasStack> asChemicalStackIngredient() {
         return this.asGasStackIngredient();
      }

      @Caster(
         implicit = true
      )
      default ChemicalStackIngredient.GasStackIngredient asGasStackIngredient() {
         return CrTGasStackIngredient.from(this);
      }
   }

   @ZenRegister
   @Name("mods.mekanism.api.chemical.InfusionStack")
   public interface ICrTInfusionStack
      extends ICrTChemicalStack<InfuseType, InfusionStack, ICrTChemicalStack.ICrTInfusionStack>,
      IBracketSupport.IInfuseTypeBracketSupport {
      @Override
      default ChemicalStackIngredient<InfuseType, InfusionStack> asChemicalStackIngredient() {
         return this.asInfusionStackIngredient();
      }

      @Caster(
         implicit = true
      )
      default ChemicalStackIngredient.InfusionStackIngredient asInfusionStackIngredient() {
         return CrTInfusionStackIngredient.from(this);
      }
   }

   @ZenRegister
   @Name("mods.mekanism.api.chemical.PigmentStack")
   public interface ICrTPigmentStack
      extends ICrTChemicalStack<Pigment, PigmentStack, ICrTChemicalStack.ICrTPigmentStack>,
      IBracketSupport.IPigmentBracketSupport {
      @Override
      default ChemicalStackIngredient<Pigment, PigmentStack> asChemicalStackIngredient() {
         return this.asPigmentStackIngredient();
      }

      @Caster(
         implicit = true
      )
      default ChemicalStackIngredient.PigmentStackIngredient asPigmentStackIngredient() {
         return CrTPigmentStackIngredient.from(this);
      }
   }

   @ZenRegister
   @Name("mods.mekanism.api.chemical.SlurryStack")
   public interface ICrTSlurryStack extends ICrTChemicalStack<Slurry, SlurryStack, ICrTChemicalStack.ICrTSlurryStack>, IBracketSupport.ISlurryBracketSupport {
      @Override
      default ChemicalStackIngredient<Slurry, SlurryStack> asChemicalStackIngredient() {
         return this.asSlurryStackIngredient();
      }

      @Caster(
         implicit = true
      )
      default ChemicalStackIngredient.SlurryStackIngredient asSlurryStackIngredient() {
         return CrTSlurryStackIngredient.from(this);
      }
   }
}
