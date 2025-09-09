package mekanism.api;

import mekanism.api.providers.IModuleDataProvider;
import net.minecraftforge.fml.InterModComms;

public class MekanismIMC {
   public static final String ADD_MEKA_TOOL_MODULES = "add_meka_tool_modules";
   public static final String ADD_MEKA_SUIT_HELMET_MODULES = "add_meka_suit_helmet_modules";
   public static final String ADD_MEKA_SUIT_BODYARMOR_MODULES = "add_meka_suit_bodyarmor_modules";
   public static final String ADD_MEKA_SUIT_PANTS_MODULES = "add_meka_suit_pants_modules";
   public static final String ADD_MEKA_SUIT_BOOTS_MODULES = "add_meka_suit_boots_modules";

   private MekanismIMC() {
   }

   public static void addModulesToAll(IModuleDataProvider<?>... moduleDataProviders) {
      addMekaToolModules(moduleDataProviders);
      addMekaSuitModules(moduleDataProviders);
   }

   public static void addMekaSuitModules(IModuleDataProvider<?>... moduleDataProviders) {
      addMekaSuitHelmetModules(moduleDataProviders);
      addMekaSuitBodyarmorModules(moduleDataProviders);
      addMekaSuitPantsModules(moduleDataProviders);
      addMekaSuitBootsModules(moduleDataProviders);
   }

   public static void addMekaToolModules(IModuleDataProvider<?>... moduleDataProviders) {
      sendModuleIMC("add_meka_tool_modules", moduleDataProviders);
   }

   public static void addMekaSuitHelmetModules(IModuleDataProvider<?>... moduleDataProviders) {
      sendModuleIMC("add_meka_suit_helmet_modules", moduleDataProviders);
   }

   public static void addMekaSuitBodyarmorModules(IModuleDataProvider<?>... moduleDataProviders) {
      sendModuleIMC("add_meka_suit_bodyarmor_modules", moduleDataProviders);
   }

   public static void addMekaSuitPantsModules(IModuleDataProvider<?>... moduleDataProviders) {
      sendModuleIMC("add_meka_suit_pants_modules", moduleDataProviders);
   }

   public static void addMekaSuitBootsModules(IModuleDataProvider<?>... moduleDataProviders) {
      sendModuleIMC("add_meka_suit_boots_modules", moduleDataProviders);
   }

   private static void sendModuleIMC(String method, IModuleDataProvider<?>... moduleDataProviders) {
      if (moduleDataProviders != null && moduleDataProviders.length != 0) {
         InterModComms.sendTo("mekanism", method, () -> moduleDataProviders);
      } else {
         throw new IllegalArgumentException("No module data providers given.");
      }
   }
}
