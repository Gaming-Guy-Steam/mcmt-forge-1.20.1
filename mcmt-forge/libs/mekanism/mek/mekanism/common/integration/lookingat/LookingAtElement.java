package mekanism.common.integration.lookingat;

import com.mojang.blaze3d.vertex.PoseStack;
import mekanism.client.gui.GuiUtils;
import mekanism.client.render.MekanismRenderer;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.network.chat.Component;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public abstract class LookingAtElement {
   private final int borderColor;
   private final int textColor;

   protected LookingAtElement(int borderColor, int textColor) {
      this.borderColor = borderColor;
      this.textColor = textColor;
   }

   public void render(@NotNull GuiGraphics guiGraphics, int x, int y) {
      int width = this.getWidth();
      int height = this.getHeight();
      guiGraphics.m_280509_(x, y, x + width - 1, y + 1, this.borderColor);
      guiGraphics.m_280509_(x, y, x + 1, y + height - 1, this.borderColor);
      guiGraphics.m_280509_(x + width - 1, y, x + width, y + height - 1, this.borderColor);
      guiGraphics.m_280509_(x, y + height - 1, x + width, y + height, this.borderColor);
      TextureAtlasSprite icon = this.getIcon();
      if (icon != null) {
         int scale = this.getScaledLevel(width - 2);
         if (scale > 0) {
            boolean colored = this.applyRenderColor(guiGraphics);
            GuiUtils.drawTiledSprite(guiGraphics, x + 1, y + 1, height - 2, scale, height - 2, icon, 16, 16, 0, GuiUtils.TilingDirection.DOWN_RIGHT);
            if (colored) {
               MekanismRenderer.resetColor(guiGraphics);
            }
         }
      }

      renderScaledText(Minecraft.m_91087_(), guiGraphics, x + 4, y + 3, this.textColor, width - 8, this.getText());
   }

   public int getWidth() {
      return 100;
   }

   public int getHeight() {
      return 13;
   }

   public abstract int getScaledLevel(int level);

   @Nullable
   public abstract TextureAtlasSprite getIcon();

   public abstract Component getText();

   protected boolean applyRenderColor(GuiGraphics guiGraphics) {
      return false;
   }

   public static void renderScaledText(Minecraft mc, @NotNull GuiGraphics guiGraphics, float x, float y, int color, float maxWidth, Component component) {
      int length = mc.f_91062_.m_92852_(component);
      if (length <= maxWidth) {
         GuiUtils.drawString(guiGraphics, mc.f_91062_, component, x, y, color, false);
      } else {
         float scale = maxWidth / length;
         float reverse = 1.0F / scale;
         float yAdd = 4.0F - scale * 8.0F / 2.0F;
         PoseStack pose = guiGraphics.m_280168_();
         pose.m_85836_();
         pose.m_85841_(scale, scale, scale);
         guiGraphics.m_280614_(mc.f_91062_, component, (int)(x * reverse), (int)(y * reverse + yAdd), color, false);
         pose.m_85849_();
      }
   }
}
