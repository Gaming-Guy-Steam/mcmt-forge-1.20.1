package mekanism.common.integration.crafttweaker.jeitweaker;

import com.blamejared.crafttweaker.api.annotation.ZenRegister;
import com.blamejared.crafttweaker_annotations.annotations.TypedExpansion;
import com.blamejared.jeitweaker.common.api.ingredient.JeiIngredient;
import com.blamejared.jeitweaker.common.api.ingredient.JeiIngredientType;
import com.blamejared.jeitweaker.common.api.ingredient.JeiIngredientTypes;
import com.blamejared.jeitweaker.common.api.ingredient.JeiIngredients;
import com.blamejared.jeitweaker.common.api.zen.ingredient.ZenJeiIngredient;
import com.google.common.base.Suppliers;
import java.util.function.Supplier;
import mekanism.api.chemical.Chemical;
import mekanism.api.chemical.ChemicalStack;
import mekanism.api.chemical.gas.GasStack;
import mekanism.api.chemical.infuse.InfusionStack;
import mekanism.api.chemical.pigment.PigmentStack;
import mekanism.api.chemical.slurry.SlurryStack;
import mekanism.common.Mekanism;
import mekanism.common.integration.crafttweaker.chemical.ICrTChemicalStack;
import org.openzen.zencode.java.ZenCodeType.Caster;

public class JEITweakerExpansions {
   private JEITweakerExpansions() {
   }

   private static <CHEMICAL extends Chemical<CHEMICAL>, STACK extends ChemicalStack<CHEMICAL>, CRT_STACK extends ICrTChemicalStack<CHEMICAL, STACK, CRT_STACK>> Supplier<JeiIngredientType<STACK, CRT_STACK>> find(
      String path
   ) {
      return Suppliers.memoize(() -> JeiIngredientTypes.findById(Mekanism.rl(path)));
   }

   @ZenRegister(
      modDeps = {"jeitweaker"}
   )
   @TypedExpansion(ICrTChemicalStack.ICrTGasStack.class)
   public static class ICrTGasStackExpansion {
      private static final Supplier<JeiIngredientType<GasStack, ICrTChemicalStack.ICrTGasStack>> TYPE = JEITweakerExpansions.find("gas");

      private ICrTGasStackExpansion() {
      }

      @Caster(
         implicit = true
      )
      public static ZenJeiIngredient asJeiIngredient(final ICrTChemicalStack.ICrTGasStack _this) {
         return JeiIngredients.toZenIngredient(JeiIngredient.ofZen(TYPE.get(), _this));
      }
   }

   @ZenRegister(
      modDeps = {"jeitweaker"}
   )
   @TypedExpansion(ICrTChemicalStack.ICrTInfusionStack.class)
   public static class ICrTInfusionStackExpansion {
      private static final Supplier<JeiIngredientType<InfusionStack, ICrTChemicalStack.ICrTInfusionStack>> TYPE = JEITweakerExpansions.find("infusion");

      private ICrTInfusionStackExpansion() {
      }

      @Caster(
         implicit = true
      )
      public static ZenJeiIngredient asJeiIngredient(final ICrTChemicalStack.ICrTInfusionStack _this) {
         return JeiIngredients.toZenIngredient(JeiIngredient.ofZen(TYPE.get(), _this));
      }
   }

   @ZenRegister(
      modDeps = {"jeitweaker"}
   )
   @TypedExpansion(ICrTChemicalStack.ICrTPigmentStack.class)
   public static class ICrTPigmentStackExpansion {
      private static final Supplier<JeiIngredientType<PigmentStack, ICrTChemicalStack.ICrTPigmentStack>> TYPE = JEITweakerExpansions.find("pigment");

      private ICrTPigmentStackExpansion() {
      }

      @Caster(
         implicit = true
      )
      public static ZenJeiIngredient asJeiIngredient(final ICrTChemicalStack.ICrTPigmentStack _this) {
         return JeiIngredients.toZenIngredient(JeiIngredient.ofZen(TYPE.get(), _this));
      }
   }

   @ZenRegister(
      modDeps = {"jeitweaker"}
   )
   @TypedExpansion(ICrTChemicalStack.ICrTSlurryStack.class)
   public static class ICrTSlurryStackExpansion {
      private static final Supplier<JeiIngredientType<SlurryStack, ICrTChemicalStack.ICrTSlurryStack>> TYPE = JEITweakerExpansions.find("slurry");

      private ICrTSlurryStackExpansion() {
      }

      @Caster(
         implicit = true
      )
      public static ZenJeiIngredient asJeiIngredient(final ICrTChemicalStack.ICrTSlurryStack _this) {
         return JeiIngredients.toZenIngredient(JeiIngredient.ofZen(TYPE.get(), _this));
      }
   }
}
