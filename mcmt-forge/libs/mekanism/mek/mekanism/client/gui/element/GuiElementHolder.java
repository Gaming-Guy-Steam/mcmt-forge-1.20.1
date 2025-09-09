package mekanism.client.gui.element;

import com.mojang.blaze3d.platform.NativeImage;
import java.io.InputStream;
import mekanism.client.gui.IGuiWrapper;
import mekanism.common.Mekanism;
import mekanism.common.lib.Color;
import mekanism.common.util.MekanismUtils;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.FastColor.ARGB32;
import org.jetbrains.annotations.NotNull;

public class GuiElementHolder extends GuiScalableElement {
   public static final ResourceLocation HOLDER = MekanismUtils.getResource(MekanismUtils.ResourceType.GUI, "element_holder.png");
   public static final int HOLDER_SIZE = 32;
   private static int BACKGROUND_COLOR = -8882056;

   public GuiElementHolder(IGuiWrapper gui, int x, int y, int width, int height) {
      super(HOLDER, gui, x, y, width, height, 32, 32);
   }

   @Override
   public void m_87963_(@NotNull GuiGraphics guiGraphics, int mouseX, int mouseY, float partialTicks) {
      this.renderBackgroundTexture(guiGraphics, this.getResource(), this.sideWidth, this.sideHeight);
   }

   @Override
   public void drawBackground(@NotNull GuiGraphics guiGraphics, int mouseX, int mouseY, float partialTicks) {
   }

   public static int getBackgroundColor() {
      return BACKGROUND_COLOR;
   }

   public static void updateBackgroundColor() {
      try (InputStream stream = Minecraft.m_91087_().m_91098_().m_215595_(HOLDER)) {
         NativeImage image = NativeImage.m_85058_(stream);

         try {
            int argb = Color.argbToFromABGR(image.m_84985_(33, 33));
            if (ARGB32.m_13655_(argb) == 0) {
               argb = -8882056;
               Mekanism.logger.warn("Unable to retrieve background color for element holder.");
            }

            BACKGROUND_COLOR = argb;
         } catch (Throwable var6) {
            if (image != null) {
               try {
                  image.close();
               } catch (Throwable var5) {
                  var6.addSuppressed(var5);
               }
            }

            throw var6;
         }

         if (image != null) {
            image.close();
         }
      } catch (Exception var8) {
         Mekanism.logger.error("Failed to retrieve background color for element holder", var8);
      }
   }
}
