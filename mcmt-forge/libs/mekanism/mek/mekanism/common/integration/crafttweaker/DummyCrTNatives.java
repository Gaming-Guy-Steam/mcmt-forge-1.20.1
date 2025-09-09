package mekanism.common.integration.crafttweaker;

import com.blamejared.crafttweaker.api.annotation.ZenRegister;
import com.blamejared.crafttweaker_annotations.annotations.NativeTypeRegistration;
import mekanism.api.chemical.ChemicalBuilder;
import mekanism.api.chemical.ChemicalStack;
import mekanism.api.gear.ICustomModule;
import mekanism.api.providers.IChemicalProvider;
import mekanism.api.providers.IModuleDataProvider;

public class DummyCrTNatives {
   private static final String DUMMY = "NativeDummy";

   private DummyCrTNatives() {
   }

   @ZenRegister(
      loaders = {"mekanismcontent"}
   )
   @NativeTypeRegistration(
      value = ChemicalBuilder.class,
      zenCodeName = "mods.mekanism.content.builder.ChemicalBuilderNativeDummy"
   )
   public static class CrTNativeChemicalBuilder {
      private CrTNativeChemicalBuilder() {
      }
   }

   @ZenRegister
   @NativeTypeRegistration(
      value = IChemicalProvider.class,
      zenCodeName = "mods.mekanism.api.provider.ChemicalProviderNativeDummy"
   )
   public static class CrTNativeChemicalProvider {
      private CrTNativeChemicalProvider() {
      }
   }

   @ZenRegister
   @NativeTypeRegistration(
      value = ChemicalStack.class,
      zenCodeName = "mods.mekanism.api.chemical.ChemicalStackNativeDummy"
   )
   public static class CrTNativeChemicalStack {
      private CrTNativeChemicalStack() {
      }
   }

   @ZenRegister
   @NativeTypeRegistration(
      value = ICustomModule.class,
      zenCodeName = "mods.mekanism.api.gear.CustomModuleNativeDummy"
   )
   public static class CrTNativeCustomModule {
      private CrTNativeCustomModule() {
      }
   }

   @ZenRegister
   @NativeTypeRegistration(
      value = IModuleDataProvider.class,
      zenCodeName = "mods.mekanism.api.gear.ModuleDataProviderNativeDummy"
   )
   public static class CrTNativeModuleDataProvider {
      private CrTNativeModuleDataProvider() {
      }
   }
}
