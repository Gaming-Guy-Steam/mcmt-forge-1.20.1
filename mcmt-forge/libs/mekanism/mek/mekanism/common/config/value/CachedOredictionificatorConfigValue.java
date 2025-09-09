package mekanism.common.config.value;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.function.Consumer;
import java.util.function.Supplier;
import mekanism.common.config.IMekanismConfig;
import net.minecraft.ResourceLocationException;
import net.minecraft.resources.ResourceLocation;
import net.minecraftforge.common.ForgeConfigSpec.Builder;
import net.minecraftforge.common.ForgeConfigSpec.ConfigValue;

public class CachedOredictionificatorConfigValue extends CachedMapConfigValue<String, List<String>> {
   private CachedOredictionificatorConfigValue(IMekanismConfig config, ConfigValue<List<? extends String>> internal) {
      super(config, internal);
   }

   public static CachedOredictionificatorConfigValue define(IMekanismConfig config, Builder builder, String path, Supplier<Map<String, List<String>>> defaults) {
      return new CachedOredictionificatorConfigValue(
         config,
         builder.defineListAllowEmpty(
            path,
            () -> encodeStatic(defaults.get(), CachedOredictionificatorConfigValue::encodeStatic),
            o -> o instanceof String string && ResourceLocation.m_135830_(string.toLowerCase(Locale.ROOT))
         )
      );
   }

   @Override
   protected void resolve(String encoded, Map<String, List<String>> resolved) {
      ResourceLocation rl = ResourceLocation.m_135820_(encoded.toLowerCase(Locale.ROOT));
      if (rl != null) {
         resolved.computeIfAbsent(rl.m_135827_(), r -> new ArrayList<>()).add(rl.m_135815_());
      }
   }

   protected void encode(String key, List<String> values, Consumer<String> adder) {
      encodeStatic(key, values, adder);
   }

   private static void encodeStatic(String key, List<String> values, Consumer<String> adder) {
      String namespace = key.toLowerCase(Locale.ROOT);

      for (String path : values) {
         try {
            ResourceLocation rl = new ResourceLocation(namespace, path.toLowerCase(Locale.ROOT));
            adder.accept(rl.toString());
         } catch (ResourceLocationException var7) {
         }
      }
   }
}
