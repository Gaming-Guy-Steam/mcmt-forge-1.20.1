package mekanism.client.render.lib;

import com.mojang.blaze3d.platform.NativeImage;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Supplier;
import mekanism.common.Mekanism;
import mekanism.common.lib.Color;
import net.minecraft.client.Minecraft;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.FastColor.ARGB32;
import org.jetbrains.annotations.Nullable;

public class ColorAtlas {
   private static final int ATLAS_SIZE = 16;
   private final String name;
   private final List<ColorAtlas.ColorRegistryObject> colors = new ArrayList<>();

   public ColorAtlas(String name) {
      this.name = name;
   }

   public ColorAtlas.ColorRegistryObject register() {
      return this.register(-1);
   }

   public ColorAtlas.ColorRegistryObject register(int defaultARGB) {
      ColorAtlas.ColorRegistryObject obj = new ColorAtlas.ColorRegistryObject(defaultARGB);
      this.colors.add(obj);
      return obj;
   }

   public void parse(ResourceLocation rl) {
      List<Color> parsed = load(rl, this.colors.size());
      if (parsed.size() < this.colors.size()) {
         Mekanism.logger.error("Failed to parse '{}' color atlas.", this.name);
      } else {
         for (int i = 0; i < parsed.size(); i++) {
            this.colors.get(i).setColor(parsed.get(i));
         }
      }
   }

   public static List<Color> load(ResourceLocation rl, int count) {
      List<Color> ret = new ArrayList<>();

      try {
         loadColorAtlas(rl, count, ret);
      } catch (Exception var4) {
         Mekanism.logger.error("Failed to load color atlas: {}", rl, var4);
      }

      return ret;
   }

   private static void loadColorAtlas(ResourceLocation rl, int count, List<Color> ret) throws IOException {
      try (InputStream input = Minecraft.m_91087_().m_91098_().m_215595_(rl)) {
         NativeImage image = NativeImage.m_85058_(input);

         try {
            for (int i = 0; i < count; i++) {
               int argb = Color.argbToFromABGR(image.m_84985_(i % 16, i / 16));
               if (ARGB32.m_13655_(argb) == 0) {
                  ret.add(null);
                  Mekanism.logger.warn("Unable to retrieve color marker: '{}' for atlas: '{}'. This is likely due to an out of date resource pack.", count, rl);
               } else {
                  ret.add(Color.argb(argb));
               }
            }
         } catch (Throwable var9) {
            if (image != null) {
               try {
                  image.close();
               } catch (Throwable var8) {
                  var9.addSuppressed(var8);
               }
            }

            throw var9;
         }

         if (image != null) {
            image.close();
         }
      }
   }

   public static class ColorRegistryObject implements Supplier<Color> {
      private final int defaultARGB;
      private Color color;
      private int argb;

      private ColorRegistryObject(int defaultARGB) {
         this.defaultARGB = defaultARGB;
         this.setColor(null);
      }

      private void setColor(@Nullable Color color) {
         if (color == null) {
            color = Color.argb(this.defaultARGB);
         }

         this.color = color;
         this.argb = color.argb();
      }

      public Color get() {
         return this.color;
      }

      public int argb() {
         return this.argb;
      }
   }
}
