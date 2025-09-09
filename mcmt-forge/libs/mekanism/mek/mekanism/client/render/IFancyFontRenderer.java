package mekanism.client.render;

import com.mojang.blaze3d.vertex.PoseStack;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;
import mekanism.api.text.TextComponentUtil;
import mekanism.client.SpecialColors;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.network.chat.Component;
import org.jetbrains.annotations.Nullable;

public interface IFancyFontRenderer {
   int getXSize();

   Font getFont();

   default int titleTextColor() {
      return SpecialColors.TEXT_TITLE.argb();
   }

   default int headingTextColor() {
      return SpecialColors.TEXT_HEADING.argb();
   }

   default int subheadingTextColor() {
      return SpecialColors.TEXT_SUBHEADING.argb();
   }

   default int screenTextColor() {
      return SpecialColors.TEXT_SCREEN.argb();
   }

   default int activeButtonTextColor() {
      return SpecialColors.TEXT_ACTIVE_BUTTON.argb();
   }

   default int inactiveButtonTextColor() {
      return SpecialColors.TEXT_INACTIVE_BUTTON.argb();
   }

   default int drawString(GuiGraphics guiGraphics, Component component, int x, int y, int color) {
      return guiGraphics.m_280614_(this.getFont(), component, x, y, color, false);
   }

   default int getStringWidth(Component component) {
      return this.getFont().m_92852_(component);
   }

   default void drawCenteredText(GuiGraphics guiGraphics, Component component, float x, float y, int color) {
      this.drawCenteredText(guiGraphics, component, x, 0.0F, y, color);
   }

   default void drawCenteredText(GuiGraphics guiGraphics, Component component, float xStart, float areaWidth, float y, int color) {
      int textWidth = this.getStringWidth(component);
      float centerX = xStart + areaWidth / 2.0F - textWidth / 2.0F;
      this.drawTextExact(guiGraphics, component, centerX, y, color);
   }

   default void drawTitleText(GuiGraphics guiGraphics, Component text, float y) {
      this.drawCenteredTextScaledBound(guiGraphics, text, this.getXSize() - 8, y, this.titleTextColor());
   }

   default void drawScaledCenteredTextScaledBound(GuiGraphics guiGraphics, Component text, float left, float y, int color, float maxX, float textScale) {
      float width = this.getStringWidth(text) * textScale;
      float scale = Math.min(1.0F, maxX / width) * textScale;
      this.drawScaledCenteredText(guiGraphics, text, left, y, color, scale);
   }

   default void drawScaledCenteredText(GuiGraphics guiGraphics, Component text, float left, float y, int color, float scale) {
      int textWidth = this.getStringWidth(text);
      float centerX = left - textWidth / 2.0F * scale;
      this.drawTextWithScale(guiGraphics, text, centerX, y, color, scale);
   }

   default void drawCenteredTextScaledBound(GuiGraphics guiGraphics, Component text, float maxLength, float y, int color) {
      this.drawCenteredTextScaledBound(guiGraphics, text, maxLength, 0.0F, y, color);
   }

   default void drawCenteredTextScaledBound(GuiGraphics guiGraphics, Component text, float maxLength, float x, float y, int color) {
      float scale = Math.min(1.0F, maxLength / this.getStringWidth(text));
      this.drawScaledCenteredText(guiGraphics, text, x + this.getXSize() / 2.0F, y, color, scale);
   }

   default void drawTextExact(GuiGraphics guiGraphics, Component text, float x, float y, int color) {
      PoseStack pose = guiGraphics.m_280168_();
      pose.m_85836_();
      pose.m_252880_(x, y, 0.0F);
      this.drawString(guiGraphics, text, 0, 0, color);
      pose.m_85849_();
   }

   default float getNeededScale(Component text, float maxLength) {
      int length = this.getStringWidth(text);
      return length <= maxLength ? 1.0F : maxLength / length;
   }

   default void drawTextScaledBound(GuiGraphics guiGraphics, String text, float x, float y, int color, float maxLength) {
      this.drawTextScaledBound(guiGraphics, TextComponentUtil.getString(text), x, y, color, maxLength);
   }

   default void drawTextScaledBound(GuiGraphics guiGraphics, Component component, float x, float y, int color, float maxLength) {
      int length = this.getStringWidth(component);
      if (length <= maxLength) {
         this.drawTextExact(guiGraphics, component, x, y, color);
      } else {
         this.drawTextWithScale(guiGraphics, component, x, y, color, maxLength / length);
      }
   }

   default void drawScaledTextScaledBound(GuiGraphics guiGraphics, Component text, float x, float y, int color, float maxX, float textScale) {
      float width = this.getStringWidth(text) * textScale;
      float scale = Math.min(1.0F, maxX / width) * textScale;
      this.drawTextWithScale(guiGraphics, text, x, y, color, scale);
   }

   default void drawTextWithScale(GuiGraphics guiGraphics, Component text, float x, float y, int color, float scale) {
      this.prepTextScale(guiGraphics, g -> this.drawString(g, text, 0, 0, color), x, y, scale);
   }

   default void prepTextScale(GuiGraphics guiGraphics, Consumer<GuiGraphics> runnable, float x, float y, float scale) {
      float yAdd = 4.0F - scale * 8.0F / 2.0F;
      PoseStack pose = guiGraphics.m_280168_();
      pose.m_85836_();
      pose.m_252880_(x, y + yAdd, 0.0F);
      pose.m_85841_(scale, scale, scale);
      runnable.accept(guiGraphics);
      pose.m_85849_();
   }

   default int drawWrappedTextWithScale(GuiGraphics guiGraphics, Component text, float x, float y, int color, float maxLength, float scale) {
      return new IFancyFontRenderer.WrappedTextRenderer(this, text).renderWithScale(guiGraphics, x, y, color, maxLength, scale);
   }

   default void drawWrappedCenteredText(GuiGraphics guiGraphics, Component text, float x, float y, int color, float maxLength) {
      new IFancyFontRenderer.WrappedTextRenderer(this, text).renderCentered(guiGraphics, x, y, color, maxLength);
   }

   public static class WrappedTextRenderer {
      private final List<IFancyFontRenderer.WrappedTextRenderer.LineData> linesToDraw = new ArrayList<>();
      private final IFancyFontRenderer font;
      private final String text;
      @Nullable
      private Font lastFont;
      private float lastMaxLength = -1.0F;
      private float lineLength = 0.0F;

      public WrappedTextRenderer(IFancyFontRenderer font, Component text) {
         this(font, text.getString());
      }

      public WrappedTextRenderer(IFancyFontRenderer font, String text) {
         this.font = font;
         this.text = text;
      }

      public void renderCentered(GuiGraphics guiGraphics, float x, float y, int color, float maxLength) {
         this.calculateLines(maxLength);
         float startY = y;

         for (IFancyFontRenderer.WrappedTextRenderer.LineData line : this.linesToDraw) {
            this.font.drawTextExact(guiGraphics, line.component(), x - line.length() / 2.0F, startY, color);
            startY += 9.0F;
         }
      }

      public int renderWithScale(GuiGraphics guiGraphics, float x, float y, int color, float maxLength, float scale) {
         this.calculateLines(maxLength / scale);
         this.font.prepTextScale(guiGraphics, g -> {
            int startY = 0;

            for (IFancyFontRenderer.WrappedTextRenderer.LineData line : this.linesToDraw) {
               this.font.drawString(g, line.component(), 0, startY, color);
               startY += 9;
            }
         }, x, y, scale);
         return this.linesToDraw.size();
      }

      void calculateLines(float maxLength) {
         Font font = this.font.getFont();
         if (font != null && (this.lastFont != font || this.lastMaxLength != maxLength)) {
            this.lastFont = font;
            this.lastMaxLength = maxLength;
            this.linesToDraw.clear();
            StringBuilder lineBuilder = new StringBuilder();
            StringBuilder wordBuilder = new StringBuilder();
            int spaceLength = this.lastFont.m_92895_(" ");
            int wordLength = 0;

            for (char c : this.text.toCharArray()) {
               if (c == ' ') {
                  lineBuilder = this.addWord(lineBuilder, wordBuilder, maxLength, spaceLength, wordLength);
                  wordBuilder = new StringBuilder();
                  wordLength = 0;
               } else {
                  wordBuilder.append(c);
                  wordLength += this.lastFont.m_92895_(Character.toString(c));
               }
            }

            if (!wordBuilder.isEmpty()) {
               lineBuilder = this.addWord(lineBuilder, wordBuilder, maxLength, spaceLength, wordLength);
            }

            if (!lineBuilder.isEmpty()) {
               this.linesToDraw.add(new IFancyFontRenderer.WrappedTextRenderer.LineData(TextComponentUtil.getString(lineBuilder.toString()), this.lineLength));
            }
         }
      }

      StringBuilder addWord(StringBuilder lineBuilder, StringBuilder wordBuilder, float maxLength, int spaceLength, int wordLength) {
         float spacingLength = lineBuilder.isEmpty() ? 0.0F : spaceLength;
         if (this.lineLength + spacingLength + wordLength > maxLength) {
            this.linesToDraw.add(new IFancyFontRenderer.WrappedTextRenderer.LineData(TextComponentUtil.getString(lineBuilder.toString()), this.lineLength));
            lineBuilder = new StringBuilder(wordBuilder);
            this.lineLength = wordLength;
         } else {
            if (spacingLength > 0.0F) {
               lineBuilder.append(" ");
            }

            lineBuilder.append((CharSequence)wordBuilder);
            this.lineLength += spacingLength + wordLength;
         }

         return lineBuilder;
      }

      public static int calculateHeightRequired(Font font, Component text, int width, float maxLength) {
         return calculateHeightRequired(font, text.getString(), width, maxLength);
      }

      public static int calculateHeightRequired(Font font, String text, int width, float maxLength) {
         IFancyFontRenderer.WrappedTextRenderer wrappedTextRenderer = new IFancyFontRenderer.WrappedTextRenderer(new IFancyFontRenderer() {
            @Override
            public int getXSize() {
               return width;
            }

            @Override
            public Font getFont() {
               return font;
            }
         }, text);
         wrappedTextRenderer.calculateLines(maxLength);
         return 9 * wrappedTextRenderer.linesToDraw.size();
      }

      private record LineData(Component component, float length) {
      }
   }
}
