package mekanism.common.config;

import java.nio.file.Path;
import net.minecraftforge.fml.ModContainer;
import net.minecraftforge.fml.loading.FMLPaths;

public class MekanismConfigHelper {
   public static final Path CONFIG_DIR = FMLPaths.getOrCreateGameRelativePath(FMLPaths.CONFIGDIR.get().resolve("Mekanism"));

   private MekanismConfigHelper() {
   }

   public static void registerConfig(ModContainer modContainer, IMekanismConfig config) {
      MekanismModConfig modConfig = new MekanismModConfig(modContainer, config);
      if (config.addToContainer()) {
         modContainer.addConfig(modConfig);
      }
   }
}
