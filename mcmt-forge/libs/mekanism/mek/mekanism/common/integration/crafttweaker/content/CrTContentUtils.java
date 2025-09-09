package mekanism.common.integration.crafttweaker.content;

import com.blamejared.crafttweaker.api.CraftTweakerAPI;
import com.blamejared.crafttweaker.api.zencode.scriptrun.ScriptRunConfiguration;
import com.blamejared.crafttweaker.api.zencode.scriptrun.ScriptRunConfiguration.RunKind;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;
import java.util.Map.Entry;
import mekanism.api.MekanismAPI;
import mekanism.api.chemical.gas.Gas;
import mekanism.api.chemical.infuse.InfuseType;
import mekanism.api.chemical.pigment.Pigment;
import mekanism.api.chemical.slurry.Slurry;
import mekanism.common.integration.crafttweaker.CrTConstants;
import net.minecraft.resources.ResourceLocation;
import net.minecraftforge.registries.RegisterEvent;
import net.minecraftforge.registries.RegisterEvent.RegisterHelper;
import org.jetbrains.annotations.Nullable;

public class CrTContentUtils {
   private static Map<ResourceLocation, Gas> queuedGases = new HashMap<>();
   private static Map<ResourceLocation, InfuseType> queuedInfuseTypes = new HashMap<>();
   private static Map<ResourceLocation, Pigment> queuedPigments = new HashMap<>();
   private static Map<ResourceLocation, Slurry> queuedSlurries = new HashMap<>();

   public static void queueGasForRegistration(ResourceLocation registryName, Gas gas) {
      queueForRegistration("Gas", queuedGases, registryName, gas);
   }

   public static void queueInfuseTypeForRegistration(ResourceLocation registryName, InfuseType infuseType) {
      queueForRegistration("Infuse Type", queuedInfuseTypes, registryName, infuseType);
   }

   public static void queuePigmentForRegistration(ResourceLocation registryName, Pigment pigment) {
      queueForRegistration("Pigment", queuedPigments, registryName, pigment);
   }

   public static void queueSlurryForRegistration(ResourceLocation registryName, Slurry slurry) {
      queueForRegistration("Slurry", queuedSlurries, registryName, slurry);
   }

   private static <V> void queueForRegistration(String type, @Nullable Map<ResourceLocation, V> queued, ResourceLocation registryName, V element) {
      if (queued != null) {
         if (queued.put(registryName, element) == null) {
            CrTConstants.CRT_LOGGER.info("Queueing {} '{}' for registration.", type, registryName);
         } else {
            CrTConstants.CRT_LOGGER.warn("Registration for {} '{}' is already queued, skipping duplicate.", type, registryName);
         }
      }
   }

   public static void registerCrTContent(RegisterEvent event) {
      event.register(
         MekanismAPI.GAS_REGISTRY_NAME,
         helper -> {
            try {
               CraftTweakerAPI.getScriptRunManager()
                  .createScriptRun(new ScriptRunConfiguration("mekanismcontent", CrTConstants.CONTENT_LOADER_SOURCE_ID, RunKind.EXECUTE))
                  .execute();
            } catch (Throwable var2) {
               CrTConstants.CRT_LOGGER.error("Unable to register chemicals due to an error.", var2);
            }

            registerQueued(helper, queuedGases, () -> queuedGases = null, "Gas", "gases");
         }
      );
      event.register(
         MekanismAPI.INFUSE_TYPE_REGISTRY_NAME,
         helper -> registerQueued(helper, queuedInfuseTypes, () -> queuedInfuseTypes = null, "Infuse Type", "infuse types")
      );
      event.register(MekanismAPI.PIGMENT_REGISTRY_NAME, helper -> registerQueued(helper, queuedPigments, () -> queuedPigments = null, "Pigment", "pigments"));
      event.register(MekanismAPI.SLURRY_REGISTRY_NAME, helper -> registerQueued(helper, queuedSlurries, () -> queuedSlurries = null, "Slurry", "slurries"));
   }

   private static <V> void registerQueued(RegisterHelper<V> helper, Map<ResourceLocation, V> queued, Runnable setNull, String type, String plural) {
      if (queued != null) {
         setNull.run();
         int count = queued.size();
         CrTConstants.CRT_LOGGER.info("Registering {} custom {}.", count, count == 1 ? type.toLowerCase(Locale.ROOT) : plural);

         for (Entry<ResourceLocation, V> entry : queued.entrySet()) {
            ResourceLocation registryName = entry.getKey();
            helper.register(registryName, entry.getValue());
            CrTConstants.CRT_LOGGER.info("Registered {}: '{}'.", type, registryName);
         }
      }
   }
}
