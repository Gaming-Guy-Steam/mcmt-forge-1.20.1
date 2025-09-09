package mekanism.common.integration.crafttweaker.bracket;

import com.blamejared.crafttweaker.api.CraftTweakerAPI;
import com.blamejared.crafttweaker.api.annotation.BracketResolver;
import com.blamejared.crafttweaker.api.annotation.ZenRegister;
import java.util.function.Function;
import java.util.function.Predicate;
import mekanism.api.MekanismAPI;
import mekanism.api.chemical.Chemical;
import mekanism.api.gear.ModuleData;
import mekanism.api.robit.RobitSkin;
import mekanism.common.integration.crafttweaker.CrTUtils;
import mekanism.common.integration.crafttweaker.chemical.ICrTChemicalStack;
import net.minecraft.core.Registry;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraftforge.registries.IForgeRegistry;
import org.openzen.zencode.java.ZenCodeType.Method;
import org.openzen.zencode.java.ZenCodeType.Name;

@ZenRegister
@Name("mods.mekanism.api.BracketHandlers")
public class CrTBracketHandlers {
   @Method
   @BracketResolver("gas")
   public static ICrTChemicalStack.ICrTGasStack getGasStack(String tokens) {
      return getChemicalStack("gas", tokens, MekanismAPI.gasRegistry(), CrTUtils::stackFromGas);
   }

   @Method
   @BracketResolver("infuse_type")
   public static ICrTChemicalStack.ICrTInfusionStack getInfusionStack(String tokens) {
      return getChemicalStack("infuse_type", tokens, MekanismAPI.infuseTypeRegistry(), CrTUtils::stackFromInfuseType);
   }

   @Method
   @BracketResolver("pigment")
   public static ICrTChemicalStack.ICrTPigmentStack getPigmentStack(String tokens) {
      return getChemicalStack("pigment", tokens, MekanismAPI.pigmentRegistry(), CrTUtils::stackFromPigment);
   }

   @Method
   @BracketResolver("slurry")
   public static ICrTChemicalStack.ICrTSlurryStack getSlurryStack(String tokens) {
      return getChemicalStack("slurry", tokens, MekanismAPI.slurryRegistry(), CrTUtils::stackFromSlurry);
   }

   @Method
   @BracketResolver("robit_skin")
   public static RobitSkin getRobitSkin(String tokens) {
      return getValue("robit_skin", tokens, MekanismAPI.ROBIT_SKIN_REGISTRY_NAME);
   }

   @Method
   @BracketResolver("module_data")
   public static ModuleData<?> getModuleData(String tokens) {
      return getValue("module_data", tokens, MekanismAPI.moduleRegistry());
   }

   private static <CHEMICAL extends Chemical<CHEMICAL>, CRT_STACK extends ICrTChemicalStack<CHEMICAL, ?, CRT_STACK>> CRT_STACK getChemicalStack(
      String bracket, String tokens, IForgeRegistry<CHEMICAL> registry, Function<CHEMICAL, CRT_STACK> getter
   ) {
      return getter.apply(getValue(bracket, tokens, registry));
   }

   private static <V> V getValue(String bracket, String tokens, IForgeRegistry<V> registry) {
      return getValue(bracket, tokens, registry::containsKey, registry::getValue);
   }

   private static <V> V getValue(String bracket, String tokens, ResourceKey<? extends Registry<? extends V>> registryKey) {
      Registry<V> registry = CraftTweakerAPI.getAccessibleElementsProvider().registryAccess().m_175515_(registryKey);
      return getValue(bracket, tokens, registry::m_7804_, registry::m_7745_);
   }

   private static <V> V getValue(String bracket, String tokens, Predicate<ResourceLocation> hasKey, Function<ResourceLocation, V> getter) {
      ResourceLocation registryName = ResourceLocation.m_135820_(tokens);
      if (registryName == null) {
         String typeName = bracket.replace("_", " ");
         throw new IllegalArgumentException(
            "Could not get " + typeName + " for <" + bracket + ":" + tokens + ">. Syntax is <" + bracket + ":modid:" + bracket + "_name>"
         );
      } else if (!hasKey.test(registryName)) {
         String typeName = bracket.replace("_", " ");
         throw new IllegalArgumentException("Could not get " + typeName + " for <" + bracket + ":" + tokens + ">, " + typeName + " does not appear to exist!");
      } else {
         return getter.apply(registryName);
      }
   }
}
