package mekanism.common.integration.crafttweaker.projecte;

import com.blamejared.crafttweaker.api.annotation.ZenRegister;
import com.blamejared.crafttweaker.api.tag.type.KnownTag;
import mekanism.api.chemical.Chemical;
import mekanism.api.chemical.ChemicalStack;
import mekanism.api.chemical.gas.Gas;
import mekanism.api.chemical.infuse.InfuseType;
import mekanism.api.chemical.pigment.Pigment;
import mekanism.api.chemical.slurry.Slurry;
import mekanism.common.integration.crafttweaker.CrTUtils;
import mekanism.common.integration.crafttweaker.chemical.ICrTChemicalStack;
import mekanism.common.integration.projecte.NSSGas;
import mekanism.common.integration.projecte.NSSInfuseType;
import mekanism.common.integration.projecte.NSSPigment;
import mekanism.common.integration.projecte.NSSSlurry;
import moze_intel.projecte.api.nss.NormalizedSimpleStack;
import org.openzen.zencode.java.ZenCodeType.Expansion;
import org.openzen.zencode.java.ZenCodeType.StaticExpansionMethod;

@ZenRegister(
   modDeps = {"projecte"}
)
@Expansion("mods.projecte.NSSResolver")
public class CrTNSSResolverExpansion {
   private CrTNSSResolverExpansion() {
   }

   @StaticExpansionMethod
   public static NormalizedSimpleStack fromGas(Gas gas) {
      return NSSGas.createGas(validateNotEmptyAndGet(gas, "gas"));
   }

   @StaticExpansionMethod
   public static NormalizedSimpleStack fromGas(ICrTChemicalStack.ICrTGasStack stack) {
      return NSSGas.createGas(validateNotEmptyAndGet(stack, "gas"));
   }

   @StaticExpansionMethod
   public static NormalizedSimpleStack fromGasTag(KnownTag<Gas> tag) {
      return NSSGas.createTag(CrTUtils.validateTagAndGet(tag));
   }

   @StaticExpansionMethod
   public static NormalizedSimpleStack fromInfuseType(InfuseType infuseType) {
      return NSSInfuseType.createInfuseType(validateNotEmptyAndGet(infuseType, "infuse type"));
   }

   @StaticExpansionMethod
   public static NormalizedSimpleStack fromInfuseType(ICrTChemicalStack.ICrTInfusionStack stack) {
      return NSSInfuseType.createInfuseType(validateNotEmptyAndGet(stack, "infusion"));
   }

   @StaticExpansionMethod
   public static NormalizedSimpleStack fromInfuseTypeTag(KnownTag<InfuseType> tag) {
      return NSSInfuseType.createTag(CrTUtils.validateTagAndGet(tag));
   }

   @StaticExpansionMethod
   public static NormalizedSimpleStack fromPigment(Pigment pigment) {
      return NSSPigment.createPigment(validateNotEmptyAndGet(pigment, "pigment"));
   }

   @StaticExpansionMethod
   public static NormalizedSimpleStack fromPigment(ICrTChemicalStack.ICrTPigmentStack stack) {
      return NSSPigment.createPigment(validateNotEmptyAndGet(stack, "pigment"));
   }

   @StaticExpansionMethod
   public static NormalizedSimpleStack fromPigmentTag(KnownTag<Pigment> tag) {
      return NSSPigment.createTag(CrTUtils.validateTagAndGet(tag));
   }

   @StaticExpansionMethod
   public static NormalizedSimpleStack fromSlurry(Slurry slurry) {
      return NSSSlurry.createSlurry(validateNotEmptyAndGet(slurry, "slurry"));
   }

   @StaticExpansionMethod
   public static NormalizedSimpleStack fromSlurry(ICrTChemicalStack.ICrTSlurryStack stack) {
      return NSSSlurry.createSlurry(validateNotEmptyAndGet(stack, "slurry"));
   }

   @StaticExpansionMethod
   public static NormalizedSimpleStack fromSlurryTag(KnownTag<Slurry> tag) {
      return NSSSlurry.createTag(CrTUtils.validateTagAndGet(tag));
   }

   private static <CHEMICAL extends Chemical<CHEMICAL>> CHEMICAL validateNotEmptyAndGet(CHEMICAL chemical, String type) {
      if (chemical.isEmptyType()) {
         throw new IllegalArgumentException("Cannot make an NSS Representation using an empty " + type + ".");
      } else {
         return chemical.getChemical();
      }
   }

   private static <STACK extends ChemicalStack<?>, CRT_STACK extends ICrTChemicalStack<?, STACK, ?>> STACK validateNotEmptyAndGet(CRT_STACK stack, String type) {
      if (stack.isEmpty()) {
         throw new IllegalArgumentException("Cannot make an NSS Representation using an empty " + type + " stack.");
      } else {
         return stack.getInternal();
      }
   }
}
