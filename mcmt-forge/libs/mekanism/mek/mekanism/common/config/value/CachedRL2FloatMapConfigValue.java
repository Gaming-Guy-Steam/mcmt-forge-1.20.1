package mekanism.common.config.value;

import it.unimi.dsi.fastutil.floats.FloatPredicate;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.function.Consumer;
import java.util.function.Supplier;
import mekanism.common.config.IMekanismConfig;
import net.minecraft.resources.ResourceLocation;
import net.minecraftforge.common.ForgeConfigSpec.Builder;
import net.minecraftforge.common.ForgeConfigSpec.ConfigValue;

public class CachedRL2FloatMapConfigValue extends CachedMapConfigValue<ResourceLocation, Float> {
   private CachedRL2FloatMapConfigValue(IMekanismConfig config, ConfigValue<List<? extends String>> internal) {
      super(config, internal);
   }

   public static CachedRL2FloatMapConfigValue define(IMekanismConfig config, Builder builder, String path, Supplier<Map<ResourceLocation, Float>> defaults) {
      return define(config, builder, path, defaults, f -> true);
   }

   public static CachedRL2FloatMapConfigValue define(
      IMekanismConfig config, Builder builder, String path, Supplier<Map<ResourceLocation, Float>> defaults, FloatPredicate range
   ) {
      return new CachedRL2FloatMapConfigValue(
         config, builder.defineListAllowEmpty(path, () -> encodeStatic(defaults.get(), CachedRL2FloatMapConfigValue::encodeStatic), o -> {
            if (o instanceof String string) {
               String[] parts = string.split(",", 2);
               if (parts.length == 2 && ResourceLocation.m_135830_(parts[0].toLowerCase(Locale.ROOT))) {
                  try {
                     float f = Float.parseFloat(parts[1]);
                     return range.test(f);
                  } catch (NumberFormatException var5) {
                  }
               }
            }

            return false;
         })
      );
   }

   @Override
   protected void resolve(String encoded, Map<ResourceLocation, Float> resolved) {
      String[] parts = encoded.split(",", 2);
      if (parts.length == 2) {
         ResourceLocation rl = ResourceLocation.m_135820_(parts[0].toLowerCase(Locale.ROOT));
         if (rl != null) {
            try {
               float value = Float.parseFloat(parts[1]);
               resolved.putIfAbsent(rl, value);
            } catch (NumberFormatException var6) {
            }
         }
      }
   }

   protected void encode(ResourceLocation key, Float value, Consumer<String> adder) {
      encodeStatic(key, value, adder);
   }

   private static void encodeStatic(ResourceLocation key, Float value, Consumer<String> adder) {
      if (value != null) {
         adder.accept(key + "," + value);
      }
   }
}
