package mekanism.common.integration;

import java.util.List;
import java.util.Optional;
import mekanism.common.integration.computer.FactoryRegistry;
import mekanism.common.integration.computer.computercraft.CCCapabilityHelper;
import mekanism.common.integration.crafttweaker.content.CrTContentUtils;
import mekanism.common.integration.curios.CuriosIntegration;
import mekanism.common.integration.energy.EnergyCompatUtils;
import mekanism.common.integration.jsonthings.JsonThingsIntegration;
import mekanism.common.integration.lookingat.theoneprobe.TOPProvider;
import mekanism.common.integration.projecte.NSSHelper;
import mekanism.common.recipe.bin.BinInsertRecipe;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.data.loading.DatagenModLoader;
import net.minecraftforge.eventbus.api.EventPriority;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.fml.InterModComms;
import net.minecraftforge.fml.ModContainer;
import net.minecraftforge.fml.ModList;
import net.minecraftforge.fml.event.lifecycle.InterModEnqueueEvent;
import net.minecraftforge.fml.javafmlmod.FMLModContainer;

public final class MekanismHooks {
   public static final String CC_MOD_ID = "computercraft";
   public static final String CRAFTTWEAKER_MOD_ID = "crafttweaker";
   public static final String CURIOS_MODID = "curios";
   public static final String DARK_MODE_EVERYWHERE_MODID = "darkmodeeverywhere";
   public static final String FLUX_NETWORKS_MOD_ID = "fluxnetworks";
   public static final String IC2_MOD_ID = "ic2";
   public static final String JEI_MOD_ID = "jei";
   public static final String JEITWEAKER_MOD_ID = "jeitweaker";
   public static final String JSON_THINGS_MOD_ID = "jsonthings";
   public static final String OC2_MOD_ID = "oc2";
   public static final String PROJECTE_MOD_ID = "projecte";
   public static final String RECIPE_STAGES_MOD_ID = "recipestages";
   public static final String TOP_MOD_ID = "theoneprobe";
   public static final String WILDFIRE_GENDER_MOD_ID = "wildfire_gender";
   public boolean CCLoaded;
   public boolean CraftTweakerLoaded;
   public boolean CuriosLoaded;
   public boolean DMELoaded;
   public boolean FluxNetworksLoaded;
   public boolean IC2Loaded;
   public boolean JEILoaded;
   public boolean JsonThingsLoaded;
   public boolean OC2Loaded;
   public boolean ProjectELoaded;
   public boolean RecipeStagesLoaded;
   public boolean TOPLoaded;
   public boolean WildfireGenderModLoaded;

   public void hookConstructor(final IEventBus bus) {
      ModList modList = ModList.get();
      this.CraftTweakerLoaded = modList.isLoaded("crafttweaker");
      this.CuriosLoaded = modList.isLoaded("curios");
      this.JsonThingsLoaded = modList.isLoaded("jsonthings");
      if (this.CuriosLoaded) {
         CuriosIntegration.addListeners(bus);
      }

      if (this.CraftTweakerLoaded && !DatagenModLoader.isRunningDataGen()) {
         IEventBus crtModEventBus = bus;
         Optional<? extends ModContainer> crtModContainer = ModList.get().getModContainerById("crafttweaker");
         if (crtModContainer.isPresent()) {
            ModContainer container = crtModContainer.get();
            if (container instanceof FMLModContainer modContainer) {
               crtModEventBus = modContainer.getEventBus();
            }
         }

         crtModEventBus.addListener(EventPriority.LOWEST, CrTContentUtils::registerCrTContent);
      }

      if (this.JsonThingsLoaded) {
         JsonThingsIntegration.hook(bus);
      }
   }

   public void hookCommonSetup() {
      ModList modList = ModList.get();
      this.CCLoaded = modList.isLoaded("computercraft");
      this.DMELoaded = modList.isLoaded("darkmodeeverywhere");
      this.IC2Loaded = modList.isLoaded("ic2");
      this.JEILoaded = modList.isLoaded("jei");
      this.OC2Loaded = modList.isLoaded("oc2");
      this.ProjectELoaded = modList.isLoaded("projecte");
      this.RecipeStagesLoaded = modList.isLoaded("recipestages");
      this.TOPLoaded = modList.isLoaded("theoneprobe");
      this.FluxNetworksLoaded = modList.isLoaded("fluxnetworks");
      this.WildfireGenderModLoaded = modList.isLoaded("wildfire_gender");
      if (this.computerCompatEnabled()) {
         FactoryRegistry.load();
         if (this.CCLoaded) {
            CCCapabilityHelper.registerApis();
         }
      }

      EnergyCompatUtils.initLoadedCache();
      if (modList.isLoaded("fastbench")) {
         MinecraftForge.EVENT_BUS.addListener(BinInsertRecipe::onCrafting);
      }
   }

   public void sendIMCMessages(InterModEnqueueEvent event) {
      if (this.DMELoaded) {
         this.sendDarkModeEverywhereIMC();
      }

      if (this.ProjectELoaded) {
         NSSHelper.init();
      }

      if (this.TOPLoaded) {
         InterModComms.sendTo("theoneprobe", "getTheOneProbe", TOPProvider::new);
      }
   }

   public boolean computerCompatEnabled() {
      return this.CCLoaded || this.OC2Loaded;
   }

   private void sendDarkModeEverywhereIMC() {
      for (String method : List.of(
         "mekanism.client.gui.GuiUtils:drawTiledSprite",
         "mekanism.client.render.HUDRenderer:renderCompass",
         "mekanism.client.render.HUDRenderer:renderHUDElement"
      )) {
         InterModComms.sendTo("darkmodeeverywhere", "dme-shaderblacklist", () -> method);
      }
   }
}
