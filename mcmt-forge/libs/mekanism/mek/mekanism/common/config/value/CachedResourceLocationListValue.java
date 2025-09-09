package mekanism.common.config.value;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Locale;
import java.util.Objects;
import java.util.function.Predicate;
import java.util.function.Supplier;
import mekanism.common.config.IMekanismConfig;
import net.minecraft.resources.ResourceLocation;
import net.minecraftforge.common.ForgeConfigSpec.Builder;
import net.minecraftforge.common.ForgeConfigSpec.ConfigValue;

public class CachedResourceLocationListValue extends CachedResolvableConfigValue<List<ResourceLocation>, List<? extends String>> {
   private static final Supplier<List<? extends String>> EMPTY = ArrayList::new;

   private CachedResourceLocationListValue(IMekanismConfig config, ConfigValue<List<? extends String>> internal) {
      super(config, internal);
   }

   public static CachedResourceLocationListValue define(IMekanismConfig config, Builder builder, String path, Predicate<ResourceLocation> rlValidator) {
      return new CachedResourceLocationListValue(config, builder.defineListAllowEmpty(Collections.singletonList(path), EMPTY, o -> {
         if (o instanceof String string) {
            ResourceLocation rl = ResourceLocation.m_135820_(string.toLowerCase(Locale.ROOT));
            if (rl != null) {
               return rlValidator.test(rl);
            }
         }

         return false;
      }));
   }

   protected List<ResourceLocation> resolve(List<? extends String> encoded) {
      return encoded.stream().map(s -> ResourceLocation.m_135820_(s.toLowerCase(Locale.ROOT))).filter(Objects::nonNull).toList();
   }

   protected List<? extends String> encode(List<ResourceLocation> values) {
      return values.stream().map(ResourceLocation::toString).toList();
   }
}
