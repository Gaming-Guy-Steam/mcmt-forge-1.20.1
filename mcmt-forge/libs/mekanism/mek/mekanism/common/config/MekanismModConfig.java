package mekanism.common.config;

import com.electronwill.nightconfig.core.file.CommentedFileConfig;
import java.nio.file.Path;
import java.util.function.Function;
import net.minecraftforge.fml.ModContainer;
import net.minecraftforge.fml.config.ConfigFileTypeHandler;
import net.minecraftforge.fml.config.ModConfig;
import net.minecraftforge.fml.event.config.ModConfigEvent;
import net.minecraftforge.fml.event.config.ModConfigEvent.Unloading;
import net.minecraftforge.fml.loading.FMLPaths;

public class MekanismModConfig extends ModConfig {
   private static final MekanismModConfig.MekanismConfigFileTypeHandler MEK_TOML = new MekanismModConfig.MekanismConfigFileTypeHandler();
   private final IMekanismConfig mekanismConfig;

   public MekanismModConfig(ModContainer container, IMekanismConfig config) {
      super(config.getConfigType(), config.getConfigSpec(), container, "Mekanism/" + config.getFileName() + ".toml");
      this.mekanismConfig = config;
   }

   public ConfigFileTypeHandler getHandler() {
      return MEK_TOML;
   }

   public void clearCache(ModConfigEvent event) {
      this.mekanismConfig.clearCache(event instanceof Unloading);
   }

   private static class MekanismConfigFileTypeHandler extends ConfigFileTypeHandler {
      private static Path getPath(Path configBasePath) {
         return configBasePath.endsWith("serverconfig") ? FMLPaths.CONFIGDIR.get() : configBasePath;
      }

      public Function<ModConfig, CommentedFileConfig> reader(Path configBasePath) {
         return super.reader(getPath(configBasePath));
      }

      public void unload(Path configBasePath, ModConfig config) {
         super.unload(getPath(configBasePath), config);
      }
   }
}
