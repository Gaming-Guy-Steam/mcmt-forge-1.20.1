package mekanism.client.gui;

import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.BufferBuilder;
import com.mojang.blaze3d.vertex.BufferUploader;
import com.mojang.blaze3d.vertex.DefaultVertexFormat;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.Tesselator;
import com.mojang.blaze3d.vertex.VertexFormat.Mode;
import com.mojang.math.Divisor;
import it.unimi.dsi.fastutil.ints.IntIterator;
import java.util.List;
import java.util.function.Predicate;
import mekanism.client.gui.element.GuiElement;
import mekanism.common.Mekanism;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.events.GuiEventListener;
import net.minecraft.client.renderer.GameRenderer;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;
import net.minecraft.util.FastColor.ARGB32;
import net.minecraft.util.profiling.ProfilerFiller;
import net.minecraft.world.item.ItemStack;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.joml.Matrix4f;

public class GuiUtils {
   private GuiUtils() {
   }

   public static void renderExtendedTexture(
      GuiGraphics guiGraphics, ResourceLocation resource, int sideWidth, int sideHeight, int left, int top, int width, int height
   ) {
      int textureWidth = 2 * sideWidth + 1;
      int textureHeight = 2 * sideHeight + 1;
      blitNineSlicedSized(
         guiGraphics, resource, left, top, width, height, sideWidth, sideHeight, textureWidth, textureHeight, 0, 0, textureWidth, textureHeight
      );
   }

   public static void renderBackgroundTexture(
      GuiGraphics guiGraphics,
      ResourceLocation resource,
      int texSideWidth,
      int texSideHeight,
      int left,
      int top,
      int width,
      int height,
      int textureWidth,
      int textureHeight
   ) {
      blitNineSlicedSized(
         guiGraphics, resource, left, top, width, height, texSideWidth, texSideHeight, textureWidth, textureHeight, 0, 0, textureWidth, textureHeight
      );
   }

   public static void drawOutline(GuiGraphics guiGraphics, int x, int y, int width, int height, int color) {
      fill(guiGraphics, x, y, width, 1, color);
      fill(guiGraphics, x, y + height - 1, width, 1, color);
      if (height > 2) {
         fill(guiGraphics, x, y + 1, 1, height - 2, color);
         fill(guiGraphics, x + width - 1, y + 1, 1, height - 2, color);
      }
   }

   public static void fill(GuiGraphics guiGraphics, RenderType renderType, int x, int y, int width, int height, int color) {
      if (width != 0 && height != 0) {
         guiGraphics.m_285944_(renderType, x, y, x + width, y + height, color);
      }
   }

   public static void fill(GuiGraphics guiGraphics, int x, int y, int width, int height, int color) {
      if (width != 0 && height != 0) {
         guiGraphics.m_280509_(x, y, x + width, y + height, color);
      }
   }

   public static void drawBackdrop(GuiGraphics guiGraphics, Minecraft minecraft, int x, int y, int width, int alpha) {
      drawBackdrop(guiGraphics, minecraft, x, y, width, 9, alpha);
   }

   public static void drawBackdrop(GuiGraphics guiGraphics, Minecraft minecraft, int x, int y, int width, int height, int alpha) {
      int backgroundColor = minecraft.f_91066_.m_92170_(0.0F);
      if (backgroundColor != 0) {
         int argb = 16777215 | alpha << 24;
         guiGraphics.m_280509_(x - 2, y - 2, x + width + 2, y + height + 2, ARGB32.m_13657_(backgroundColor, argb));
      }
   }

   public static void drawTiledSprite(
      GuiGraphics guiGraphics,
      int xPosition,
      int yPosition,
      int yOffset,
      int desiredWidth,
      int desiredHeight,
      TextureAtlasSprite sprite,
      int textureWidth,
      int textureHeight,
      int zLevel,
      GuiUtils.TilingDirection tilingDirection
   ) {
      drawTiledSprite(
         guiGraphics, xPosition, yPosition, yOffset, desiredWidth, desiredHeight, sprite, textureWidth, textureHeight, zLevel, tilingDirection, true
      );
   }

   public static void drawTiledSprite(
      GuiGraphics guiGraphics,
      int xPosition,
      int yPosition,
      int yOffset,
      int desiredWidth,
      int desiredHeight,
      TextureAtlasSprite sprite,
      int textureWidth,
      int textureHeight,
      int zLevel,
      GuiUtils.TilingDirection tilingDirection,
      boolean blend
   ) {
      if (desiredWidth != 0 && desiredHeight != 0 && textureWidth != 0 && textureHeight != 0) {
         RenderSystem.setShader(GameRenderer::m_172817_);
         RenderSystem.setShaderTexture(0, sprite.m_247685_());
         int xTileCount = desiredWidth / textureWidth;
         int xRemainder = desiredWidth - xTileCount * textureWidth;
         int yTileCount = desiredHeight / textureHeight;
         int yRemainder = desiredHeight - yTileCount * textureHeight;
         int yStart = yPosition + yOffset;
         float uMin = sprite.m_118409_();
         float uMax = sprite.m_118410_();
         float vMin = sprite.m_118411_();
         float vMax = sprite.m_118412_();
         float uDif = uMax - uMin;
         float vDif = vMax - vMin;
         if (blend) {
            RenderSystem.enableBlend();
         }

         BufferBuilder vertexBuffer = Tesselator.m_85913_().m_85915_();
         vertexBuffer.m_166779_(Mode.QUADS, DefaultVertexFormat.f_85817_);
         Matrix4f matrix4f = guiGraphics.m_280168_().m_85850_().m_252922_();

         for (int xTile = 0; xTile <= xTileCount; xTile++) {
            int width = xTile == xTileCount ? xRemainder : textureWidth;
            if (width == 0) {
               break;
            }

            int x = xPosition + xTile * textureWidth;
            int maskRight = textureWidth - width;
            int shiftedX = x + textureWidth - maskRight;
            float uLocalDif = uDif * maskRight / textureWidth;
            float uLocalMin;
            float uLocalMax;
            if (tilingDirection.right) {
               uLocalMin = uMin;
               uLocalMax = uMax - uLocalDif;
            } else {
               uLocalMin = uMin + uLocalDif;
               uLocalMax = uMax;
            }

            for (int yTile = 0; yTile <= yTileCount; yTile++) {
               int height = yTile == yTileCount ? yRemainder : textureHeight;
               if (height != 0) {
                  int y = yStart - (yTile + 1) * textureHeight;
                  int maskTop = textureHeight - height;
                  float vLocalDif = vDif * maskTop / textureHeight;
                  float vLocalMin;
                  float vLocalMax;
                  if (tilingDirection.down) {
                     vLocalMin = vMin;
                     vLocalMax = vMax - vLocalDif;
                  } else {
                     vLocalMin = vMin + vLocalDif;
                     vLocalMax = vMax;
                  }

                  vertexBuffer.m_252986_(matrix4f, x, y + textureHeight, zLevel).m_7421_(uLocalMin, vLocalMax).m_5752_();
                  vertexBuffer.m_252986_(matrix4f, shiftedX, y + textureHeight, zLevel).m_7421_(uLocalMax, vLocalMax).m_5752_();
                  vertexBuffer.m_252986_(matrix4f, shiftedX, y + maskTop, zLevel).m_7421_(uLocalMax, vLocalMin).m_5752_();
                  vertexBuffer.m_252986_(matrix4f, x, y + maskTop, zLevel).m_7421_(uLocalMin, vLocalMin).m_5752_();
                  continue;
               }
            }
         }

         BufferUploader.m_231202_(vertexBuffer.m_231175_());
         if (blend) {
            RenderSystem.disableBlend();
         }
      }
   }

   public static boolean checkChildren(List<? extends GuiEventListener> children, Predicate<GuiElement> checker) {
      for (int i = children.size() - 1; i >= 0; i--) {
         if (children.get(i) instanceof GuiElement element && checker.test(element)) {
            return true;
         }
      }

      return false;
   }

   public static int drawString(GuiGraphics guiGraphics, Font font, Component component, float x, float y, int color, boolean drawShadow) {
      return guiGraphics.drawString(font, component.m_7532_(), x, y, color, drawShadow);
   }

   public static void renderItem(
      GuiGraphics guiGraphics, @NotNull ItemStack stack, int xAxis, int yAxis, float scale, Font font, @Nullable String text, boolean overlay
   ) {
      if (!stack.m_41619_()) {
         try {
            PoseStack pose = guiGraphics.m_280168_();
            pose.m_85836_();
            if (scale != 1.0F) {
               pose.m_252880_(xAxis, yAxis, 0.0F);
               pose.m_85841_(scale, scale, scale);
               xAxis = 0;
               yAxis = 0;
            }

            guiGraphics.m_280480_(stack, xAxis, yAxis);
            if (overlay) {
               pose.m_252880_(0.0F, 0.0F, -25.0F);
               guiGraphics.m_280302_(font, stack, xAxis, yAxis, text);
            }

            pose.m_85849_();
         } catch (Exception var9) {
            Mekanism.logger.error("Failed to render stack into gui: {}", stack, var9);
         }
      }
   }

   public static void blitNineSlicedSized(
      GuiGraphics guiGraphics,
      ResourceLocation texture,
      int x,
      int y,
      int width,
      int height,
      int sliceWidth,
      int sliceHeight,
      int uWidth,
      int vHeight,
      int uOffset,
      int vOffset,
      int textureWidth,
      int textureHeight
   ) {
      ProfilerFiller profiler = Minecraft.m_91087_().m_91307_();
      profiler.m_6180_("blit setup");
      RenderSystem.setShaderTexture(0, texture);
      RenderSystem.setShader(GameRenderer::m_172817_);
      Matrix4f matrix4f = guiGraphics.m_280168_().m_85850_().m_252922_();
      profiler.m_7238_();
      BufferBuilder bufferbuilder = Tesselator.m_85913_().m_85915_();
      bufferbuilder.m_166779_(Mode.QUADS, DefaultVertexFormat.f_85817_);
      profiler.m_6180_("blitting");
      int cornerWidth = Math.min(sliceWidth, width / 2);
      int edgeWidth = Math.min(sliceWidth, width / 2);
      int cornerHeight = Math.min(sliceHeight, height / 2);
      int edgeHeight = Math.min(sliceHeight, height / 2);
      if (width == uWidth && height == vHeight) {
         blit(bufferbuilder, matrix4f, x, y, uOffset, vOffset, width, height, textureWidth, textureHeight);
      } else if (height == vHeight) {
         blit(bufferbuilder, matrix4f, x, y, uOffset, vOffset, cornerWidth, height, textureWidth, textureHeight);
         blitRepeating(
            bufferbuilder,
            matrix4f,
            x + cornerWidth,
            y,
            width - edgeWidth - cornerWidth,
            height,
            uOffset + cornerWidth,
            vOffset,
            uWidth - edgeWidth - cornerWidth,
            vHeight,
            textureWidth,
            textureHeight
         );
         blit(bufferbuilder, matrix4f, x + width - edgeWidth, y, uOffset + uWidth - edgeWidth, vOffset, edgeWidth, height, textureWidth, textureHeight);
      } else if (width == uWidth) {
         blit(bufferbuilder, matrix4f, x, y, uOffset, vOffset, width, cornerHeight, textureWidth, textureHeight);
         blitRepeating(
            bufferbuilder,
            matrix4f,
            x,
            y + cornerHeight,
            width,
            height - edgeHeight - cornerHeight,
            uOffset,
            vOffset + cornerHeight,
            uWidth,
            vHeight - edgeHeight - cornerHeight,
            textureWidth,
            textureHeight
         );
         blit(bufferbuilder, matrix4f, x, y + height - edgeHeight, uOffset, vOffset + vHeight - edgeHeight, width, edgeHeight, textureWidth, textureHeight);
      } else {
         blit(bufferbuilder, matrix4f, x, y, uOffset, vOffset, cornerWidth, cornerHeight, textureWidth, textureHeight);
         blitRepeating(
            bufferbuilder,
            matrix4f,
            x + cornerWidth,
            y,
            width - edgeWidth - cornerWidth,
            cornerHeight,
            uOffset + cornerWidth,
            vOffset,
            uWidth - edgeWidth - cornerWidth,
            cornerHeight,
            textureWidth,
            textureHeight
         );
         blit(bufferbuilder, matrix4f, x + width - edgeWidth, y, uOffset + uWidth - edgeWidth, vOffset, edgeWidth, cornerHeight, textureWidth, textureHeight);
         blit(
            bufferbuilder, matrix4f, x, y + height - edgeHeight, uOffset, vOffset + vHeight - edgeHeight, cornerWidth, edgeHeight, textureWidth, textureHeight
         );
         blitRepeating(
            bufferbuilder,
            matrix4f,
            x + cornerWidth,
            y + height - edgeHeight,
            width - edgeWidth - cornerWidth,
            edgeHeight,
            uOffset + cornerWidth,
            vOffset + vHeight - edgeHeight,
            uWidth - edgeWidth - cornerWidth,
            edgeHeight,
            textureWidth,
            textureHeight
         );
         blit(
            bufferbuilder,
            matrix4f,
            x + width - edgeWidth,
            y + height - edgeHeight,
            uOffset + uWidth - edgeWidth,
            vOffset + vHeight - edgeHeight,
            edgeWidth,
            edgeHeight,
            textureWidth,
            textureHeight
         );
         blitRepeating(
            bufferbuilder,
            matrix4f,
            x,
            y + cornerHeight,
            cornerWidth,
            height - edgeHeight - cornerHeight,
            uOffset,
            vOffset + cornerHeight,
            cornerWidth,
            vHeight - edgeHeight - cornerHeight,
            textureWidth,
            textureHeight
         );
         blitRepeating(
            bufferbuilder,
            matrix4f,
            x + cornerWidth,
            y + cornerHeight,
            width - edgeWidth - cornerWidth,
            height - edgeHeight - cornerHeight,
            uOffset + cornerWidth,
            vOffset + cornerHeight,
            uWidth - edgeWidth - cornerWidth,
            vHeight - edgeHeight - cornerHeight,
            textureWidth,
            textureHeight
         );
         blitRepeating(
            bufferbuilder,
            matrix4f,
            x + width - edgeWidth,
            y + cornerHeight,
            cornerWidth,
            height - edgeHeight - cornerHeight,
            uOffset + uWidth - edgeWidth,
            vOffset + cornerHeight,
            edgeWidth,
            vHeight - edgeHeight - cornerHeight,
            textureWidth,
            textureHeight
         );
      }

      profiler.m_7238_();
      profiler.m_6180_("drawing");
      BufferUploader.m_231202_(bufferbuilder.m_231175_());
      profiler.m_7238_();
   }

   public static void blitNineSlicedSized(
      GuiGraphics guiGraphics,
      ResourceLocation texture,
      int x,
      int y,
      int width,
      int height,
      int sliceSize,
      int uWidth,
      int vHeight,
      int uOffset,
      int vOffset,
      int textureWidth,
      int textureHeight
   ) {
      blitNineSlicedSized(guiGraphics, texture, x, y, width, height, sliceSize, sliceSize, uWidth, vHeight, uOffset, vOffset, textureWidth, textureHeight);
   }

   private static void blit(
      BufferBuilder bufferbuilder,
      Matrix4f matrix4f,
      int pX,
      int pY,
      float pUOffset,
      float pVOffset,
      int pWidth,
      int pHeight,
      int pTextureWidth,
      int pTextureHeight
   ) {
      bufferbuilder.m_252986_(matrix4f, pX, pY, 0.0F).m_7421_((pUOffset + 0.0F) / pTextureWidth, (pVOffset + 0.0F) / pTextureHeight).m_5752_();
      bufferbuilder.m_252986_(matrix4f, pX, pY + pHeight, 0.0F).m_7421_((pUOffset + 0.0F) / pTextureWidth, (pVOffset + pHeight) / pTextureHeight).m_5752_();
      bufferbuilder.m_252986_(matrix4f, pX + pWidth, pY + pHeight, 0.0F)
         .m_7421_((pUOffset + pWidth) / pTextureWidth, (pVOffset + pHeight) / pTextureHeight)
         .m_5752_();
      bufferbuilder.m_252986_(matrix4f, pX + pWidth, pY, 0.0F).m_7421_((pUOffset + pWidth) / pTextureWidth, (pVOffset + 0.0F) / pTextureHeight).m_5752_();
   }

   private static void blitRepeating(
      BufferBuilder bufferbuilder,
      Matrix4f matrix4f,
      int pX,
      int pY,
      int pWidth,
      int pHeight,
      int pUOffset,
      int pVOffset,
      int pSourceWidth,
      int pSourceHeight,
      int textureWidth,
      int textureHeight
   ) {
      int i = pX;
      IntIterator intiterator = slices(pWidth, pSourceWidth);

      while (intiterator.hasNext()) {
         int j = intiterator.nextInt();
         int k = (pSourceWidth - j) / 2;
         int l = pY;
         IntIterator intiterator1 = slices(pHeight, pSourceHeight);

         while (intiterator1.hasNext()) {
            int i1 = intiterator1.nextInt();
            int j1 = (pSourceHeight - i1) / 2;
            blit(bufferbuilder, matrix4f, i, l, pUOffset + k, pVOffset + j1, j, i1, textureWidth, textureHeight);
            l += i1;
         }

         i += j;
      }
   }

   private static IntIterator slices(int pTarget, int pTotal) {
      int i = Mth.m_184652_(pTarget, pTotal);
      return new Divisor(pTarget, i);
   }

   public static enum TilingDirection {
      DOWN_RIGHT(true, true),
      DOWN_LEFT(true, false),
      UP_RIGHT(false, true),
      UP_LEFT(false, false);

      private final boolean down;
      private final boolean right;

      private TilingDirection(boolean down, boolean right) {
         this.down = down;
         this.right = right;
      }
   }
}
