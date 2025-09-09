package mekanism.common.integration.crafttweaker;

import com.blamejared.crafttweaker.api.plugin.CraftTweakerPlugin;
import com.blamejared.crafttweaker.api.plugin.ICraftTweakerPlugin;
import com.blamejared.crafttweaker.api.plugin.ILoaderRegistrationHandler;
import com.blamejared.crafttweaker.api.plugin.IRecipeComponentRegistrationHandler;
import com.blamejared.crafttweaker.api.plugin.IScriptLoadSourceRegistrationHandler;
import com.blamejared.crafttweaker.api.plugin.IScriptRunModuleConfiguratorRegistrationHandler;
import com.blamejared.crafttweaker.api.zencode.scriptrun.IScriptRunModuleConfigurator;

@CraftTweakerPlugin("mekanism:crt_plugin")
public class MekCraftTweakerPlugin implements ICraftTweakerPlugin {
   public void registerLoadSource(IScriptLoadSourceRegistrationHandler handler) {
      handler.registerLoadSource(CrTConstants.CONTENT_LOADER_SOURCE_ID);
   }

   public void registerLoaders(ILoaderRegistrationHandler handler) {
      handler.registerLoader("mekanismcontent", new String[]{"crafttweaker"});
   }

   public void registerModuleConfigurators(IScriptRunModuleConfiguratorRegistrationHandler handler) {
      IScriptRunModuleConfigurator defaultConfig = IScriptRunModuleConfigurator.createDefault("crafttweaker");
      handler.registerConfigurator("mekanismcontent", defaultConfig);
   }

   public void registerRecipeComponents(IRecipeComponentRegistrationHandler handler) {
      handler.registerRecipeComponent(CrTRecipeComponents.ITEM.input());
      handler.registerRecipeComponent(CrTRecipeComponents.FLUID.input());
      handler.registerRecipeComponent(CrTRecipeComponents.FLUID.output());

      for (CrTRecipeComponents.ChemicalRecipeComponent<?, ?, ?, ?> chemicalComponent : CrTRecipeComponents.CHEMICAL_COMPONENTS) {
         handler.registerRecipeComponent(chemicalComponent.input());
         handler.registerRecipeComponent(chemicalComponent.output());
      }

      handler.registerRecipeComponent(CrTRecipeComponents.CHANCE);
      handler.registerRecipeComponent(CrTRecipeComponents.ENERGY);
   }
}
