package mekanism.common.integration.crafttweaker.bracket;

import com.blamejared.crafttweaker.api.CraftTweakerAPI;
import com.blamejared.crafttweaker.api.annotation.BracketDumper;
import com.blamejared.crafttweaker.api.annotation.ZenRegister;
import java.util.Collection;
import java.util.function.Function;
import mekanism.api.MekanismAPI;
import mekanism.api.chemical.Chemical;
import mekanism.common.integration.crafttweaker.CrTUtils;
import mekanism.common.integration.crafttweaker.chemical.ICrTChemicalStack;
import net.minecraft.core.Registry;
import net.minecraft.resources.ResourceKey;
import net.minecraftforge.registries.IForgeRegistry;
import org.openzen.zencode.java.ZenCodeType.Name;

@ZenRegister
@Name("mods.mekanism.api.BracketDumpers")
public class CrTBracketDumpers {
   @BracketDumper(
      value = "gas",
      subCommandName = "gases"
   )
   public static Collection<String> getGasStackDump() {
      return getChemicalStackDump(MekanismAPI.gasRegistry(), CrTUtils::stackFromGas);
   }

   @BracketDumper(
      value = "infuse_type",
      subCommandName = "infuseTypes"
   )
   public static Collection<String> getInfusionStackDump() {
      return getChemicalStackDump(MekanismAPI.infuseTypeRegistry(), CrTUtils::stackFromInfuseType);
   }

   @BracketDumper(
      value = "pigment",
      subCommandName = "pigments"
   )
   public static Collection<String> getPigmentStackDump() {
      return getChemicalStackDump(MekanismAPI.pigmentRegistry(), CrTUtils::stackFromPigment);
   }

   @BracketDumper(
      value = "slurry",
      subCommandName = "slurries"
   )
   public static Collection<String> getSlurryStackDump() {
      return getChemicalStackDump(MekanismAPI.slurryRegistry(), CrTUtils::stackFromSlurry);
   }

   @BracketDumper(
      value = "robit_skin",
      subCommandName = "robitSkins"
   )
   public static Collection<String> getRobitSkinDump() {
      return getDump(MekanismAPI.ROBIT_SKIN_REGISTRY_NAME, "robit_skin");
   }

   @BracketDumper(
      value = "module_data",
      subCommandName = "moduleData"
   )
   public static Collection<String> getModuleDataDump() {
      return getDump(MekanismAPI.moduleRegistry(), "module_data");
   }

   private static <CHEMICAL extends Chemical<CHEMICAL>, CRT_STACK extends ICrTChemicalStack<CHEMICAL, ?, CRT_STACK>> Collection<String> getChemicalStackDump(
      IForgeRegistry<CHEMICAL> registry, Function<CHEMICAL, CRT_STACK> getter
   ) {
      return getDump(registry, chemical -> getter.apply((CHEMICAL)chemical).getCommandString());
   }

   private static <V> Collection<String> getDump(IForgeRegistry<V> registry, String bracket) {
      return getDump(registry, v -> "<" + bracket + ":" + registry.getKey(v) + ">");
   }

   private static <V> Collection<String> getDump(IForgeRegistry<V> registry, Function<V, String> getter) {
      return registry.getValues().stream().map(getter).toList();
   }

   private static Collection<String> getDump(ResourceKey<? extends Registry<?>> registryKey, String bracket) {
      return CraftTweakerAPI.getAccessibleElementsProvider()
         .registryAccess()
         .m_6632_(registryKey)
         .stream()
         .flatMap(registry -> registry.m_6566_().stream())
         .map(v -> "<" + bracket + ":" + v + ">")
         .toList();
   }
}
