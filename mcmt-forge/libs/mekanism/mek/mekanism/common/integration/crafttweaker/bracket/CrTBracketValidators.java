package mekanism.common.integration.crafttweaker.bracket;

import com.blamejared.crafttweaker.api.CraftTweakerAPI;
import com.blamejared.crafttweaker.api.annotation.BracketValidator;
import com.blamejared.crafttweaker.api.annotation.ZenRegister;
import java.util.Optional;
import java.util.function.Predicate;
import mekanism.api.MekanismAPI;
import mekanism.common.integration.crafttweaker.CrTConstants;
import net.minecraft.core.Registry;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraftforge.registries.ForgeRegistry;
import net.minecraftforge.registries.IForgeRegistry;
import org.openzen.zencode.java.ZenCodeType.Method;
import org.openzen.zencode.java.ZenCodeType.Name;

@ZenRegister
@Name("mods.mekanism.api.BracketValidators")
public class CrTBracketValidators {
   @Method
   @BracketValidator("gas")
   public static boolean validateGasStack(String tokens) {
      return validate("gas", tokens, MekanismAPI.gasRegistry());
   }

   @Method
   @BracketValidator("infuse_type")
   public static boolean validateInfusionStack(String tokens) {
      return validate("infuse_type", tokens, MekanismAPI.infuseTypeRegistry());
   }

   @Method
   @BracketValidator("pigment")
   public static boolean validatePigmentStack(String tokens) {
      return validate("pigment", tokens, MekanismAPI.pigmentRegistry());
   }

   @Method
   @BracketValidator("slurry")
   public static boolean validateSlurryStack(String tokens) {
      return validate("slurry", tokens, MekanismAPI.slurryRegistry());
   }

   @Method
   @BracketValidator("robit_skin")
   public static boolean validateRobitSkin(String tokens) {
      return validate("robit_skin", tokens, MekanismAPI.ROBIT_SKIN_REGISTRY_NAME);
   }

   @Method
   @BracketValidator("module_data")
   public static boolean validateModuleData(String tokens) {
      return validate("module_data", tokens, MekanismAPI.moduleRegistry());
   }

   private static boolean validate(String bracket, String tokens, IForgeRegistry<?> registry) {
      return validate(bracket, tokens, (Predicate<ResourceLocation>)(registryName -> isRegistryUnlocked(registry) || registry.containsKey(registryName)));
   }

   private static boolean isRegistryUnlocked(IForgeRegistry<?> registry) {
      return registry instanceof ForgeRegistry<?> forgeRegistry && !forgeRegistry.isLocked();
   }

   private static boolean validate(String bracket, String tokens, ResourceKey<? extends Registry<?>> registryKey) {
      return validate(bracket, tokens, (Predicate<ResourceLocation>)(registryName -> {
         Optional<Registry<Object>> registry = CraftTweakerAPI.getAccessibleElementsProvider().registryAccess().m_6632_(registryKey);
         return registry.isEmpty() || registry.get().m_7804_(registryName);
      }));
   }

   private static boolean validate(String bracket, String tokens, Predicate<ResourceLocation> unlockedOrHas) {
      ResourceLocation registryName = ResourceLocation.m_135820_(tokens);
      if (registryName == null) {
         CrTConstants.CRT_LOGGER.error("Could not get BEP <{}:{}>. Syntax is <{}:modid:{}_name>", bracket, tokens, bracket, bracket);
         return false;
      } else if (unlockedOrHas.test(registryName)) {
         return true;
      } else {
         String typeName = bracket.replace("_", " ");
         CrTConstants.CRT_LOGGER.error("Could not get {} for <{}:{}>, {} does not appear to exist!", typeName, bracket, tokens, typeName);
         return false;
      }
   }
}
