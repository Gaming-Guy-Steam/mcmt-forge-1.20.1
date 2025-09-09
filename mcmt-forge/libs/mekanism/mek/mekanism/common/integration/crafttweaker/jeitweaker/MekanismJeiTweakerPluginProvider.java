package mekanism.common.integration.crafttweaker.jeitweaker;

import com.blamejared.jeitweaker.common.api.ingredient.JeiIngredientType;
import com.blamejared.jeitweaker.common.api.plugin.JeiIngredientTypeRegistration;
import com.blamejared.jeitweaker.common.api.plugin.JeiTweakerPlugin;
import com.blamejared.jeitweaker.common.api.plugin.JeiTweakerPluginProvider;
import java.util.function.Function;
import mekanism.api.chemical.Chemical;
import mekanism.api.chemical.ChemicalStack;
import mekanism.api.chemical.gas.GasStack;
import mekanism.api.chemical.infuse.InfusionStack;
import mekanism.api.chemical.pigment.PigmentStack;
import mekanism.api.chemical.slurry.SlurryStack;
import mekanism.client.jei.MekanismJEI;
import mekanism.common.Mekanism;
import mekanism.common.integration.crafttweaker.CrTUtils;
import mekanism.common.integration.crafttweaker.chemical.ICrTChemicalStack;
import mezz.jei.api.ingredients.IIngredientType;

@JeiTweakerPlugin("mekanism:crt_jei")
public class MekanismJeiTweakerPluginProvider implements JeiTweakerPluginProvider {
   public void registerIngredientTypes(JeiIngredientTypeRegistration registration) {
      this.registerType(registration, "gas", GasStack.class, ICrTChemicalStack.ICrTGasStack.class, MekanismJEI.TYPE_GAS, CrTUtils.GAS_CONVERTER);
      this.registerType(
         registration, "infusion", InfusionStack.class, ICrTChemicalStack.ICrTInfusionStack.class, MekanismJEI.TYPE_INFUSION, CrTUtils.INFUSION_CONVERTER
      );
      this.registerType(
         registration, "pigment", PigmentStack.class, ICrTChemicalStack.ICrTPigmentStack.class, MekanismJEI.TYPE_PIGMENT, CrTUtils.PIGMENT_CONVERTER
      );
      this.registerType(registration, "slurry", SlurryStack.class, ICrTChemicalStack.ICrTSlurryStack.class, MekanismJEI.TYPE_SLURRY, CrTUtils.SLURRY_CONVERTER);
   }

   private <CHEMICAL extends Chemical<CHEMICAL>, STACK extends ChemicalStack<CHEMICAL>, CRT_STACK extends ICrTChemicalStack<CHEMICAL, STACK, CRT_STACK>> void registerType(
      JeiIngredientTypeRegistration registration,
      String type,
      Class<STACK> clazz,
      Class<CRT_STACK> crtClass,
      IIngredientType<STACK> ingredientType,
      Function<STACK, CRT_STACK> converter
   ) {
      registration.registerIngredientType(
         JeiIngredientType.of(Mekanism.rl(type), clazz, crtClass), new JeiChemicalIngredientConverter<>(converter), ingredientType
      );
   }
}
