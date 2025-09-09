package mekanism.client.gui.element.progress;

import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.BufferBuilder;
import com.mojang.blaze3d.vertex.BufferUploader;
import com.mojang.blaze3d.vertex.DefaultVertexFormat;
import com.mojang.blaze3d.vertex.Tesselator;
import com.mojang.blaze3d.vertex.VertexFormat.Mode;
import java.util.function.BooleanSupplier;
import mekanism.client.gui.IGuiWrapper;
import mekanism.client.gui.element.GuiTexturedElement;
import mekanism.client.jei.MekanismJEIRecipeType;
import mekanism.client.jei.interfaces.IJEIRecipeArea;
import mekanism.client.render.MekanismRenderer;
import mekanism.common.inventory.warning.ISupportsWarning;
import mekanism.common.inventory.warning.WarningTracker;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.renderer.GameRenderer;
import net.minecraft.resources.ResourceLocation;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.joml.Matrix4f;

public class GuiProgress extends GuiTexturedElement implements IJEIRecipeArea<GuiProgress>, ISupportsWarning<GuiProgress> {
   protected final IProgressInfoHandler handler;
   protected final ProgressType type;
   private MekanismJEIRecipeType<?>[] recipeCategories;
   @Nullable
   private GuiProgress.ColorDetails colorDetails;
   @Nullable
   private BooleanSupplier warningSupplier;

   public GuiProgress(IProgressInfoHandler.IBooleanProgressInfoHandler handler, ProgressType type, IGuiWrapper gui, int x, int y) {
      this((IProgressInfoHandler)handler, type, gui, x, y);
   }

   public GuiProgress(IProgressInfoHandler handler, ProgressType type, IGuiWrapper gui, int x, int y) {
      super(type.getTexture(), gui, x, y, type.getWidth(), type.getHeight());
      this.type = type;
      this.handler = handler;
   }

   public GuiProgress colored(GuiProgress.ColorDetails colorDetails) {
      this.colorDetails = colorDetails;
      return this;
   }

   public GuiProgress warning(@NotNull WarningTracker.WarningType type, @NotNull BooleanSupplier warningSupplier) {
      this.warningSupplier = ISupportsWarning.compound(this.warningSupplier, this.gui().trackWarning(type, warningSupplier));
      return this;
   }

   @Override
   public void drawBackground(@NotNull GuiGraphics guiGraphics, int mouseX, int mouseY, float partialTicks) {
      super.drawBackground(guiGraphics, mouseX, mouseY, partialTicks);
      if (this.handler.isActive()) {
         ResourceLocation resource = this.getResource();
         guiGraphics.m_280163_(
            resource, this.relativeX, this.relativeY, 0.0F, 0.0F, this.f_93618_, this.f_93619_, this.type.getTextureWidth(), this.type.getTextureHeight()
         );
         boolean warning = this.warningSupplier != null && this.warningSupplier.getAsBoolean();
         double progress = warning ? 1.0 : this.getProgress();
         if (this.type.isVertical()) {
            int displayInt = (int)(progress * this.f_93619_);
            if (displayInt > 0) {
               int innerOffsetY = 0;
               if (this.type.isReverse()) {
                  innerOffsetY += this.type.getTextureHeight() - displayInt;
               }

               this.blit(
                  guiGraphics,
                  resource,
                  this.relativeX,
                  this.relativeY + innerOffsetY,
                  this.type.getOverlayX(warning),
                  this.type.getOverlayY(warning) + innerOffsetY,
                  this.f_93618_,
                  displayInt,
                  this.type.getTextureWidth(),
                  this.type.getTextureHeight(),
                  progress,
                  warning
               );
            }
         } else {
            int innerOffsetX = this.type == ProgressType.BAR ? 1 : 0;
            int displayInt = (int)(progress * (this.f_93618_ - 2 * innerOffsetX));
            if (displayInt > 0) {
               if (this.type.isReverse()) {
                  innerOffsetX += this.type.getTextureWidth() - displayInt;
               }

               this.blit(
                  guiGraphics,
                  resource,
                  this.relativeX + innerOffsetX,
                  this.relativeY,
                  this.type.getOverlayX(warning) + innerOffsetX,
                  this.type.getOverlayY(warning),
                  displayInt,
                  this.f_93619_,
                  this.type.getTextureWidth(),
                  this.type.getTextureHeight(),
                  progress,
                  warning
               );
            }
         }
      }
   }

   protected double getProgress() {
      return Math.min(this.handler.getProgress(), 1.0);
   }

   @Override
   public boolean isJEIAreaActive() {
      return this.handler.isActive();
   }

   @NotNull
   public GuiProgress jeiCategories(@NotNull MekanismJEIRecipeType<?>... recipeCategories) {
      this.recipeCategories = recipeCategories;
      return this;
   }

   @Nullable
   @Override
   public MekanismJEIRecipeType<?>[] getRecipeCategories() {
      return this.recipeCategories;
   }

   private void blit(
      GuiGraphics guiGraphics,
      ResourceLocation resource,
      int x,
      int y,
      float uOffset,
      float vOffset,
      int width,
      int height,
      int textureWidth,
      int textureHeight,
      double progress,
      boolean warning
   ) {
      if (!warning && this.colorDetails != null) {
         int colorFrom = this.colorDetails.getColorFrom();
         int colorTo = this.colorDetails.getColorTo();
         if (colorFrom == -1 && colorTo == -1) {
            guiGraphics.m_280163_(resource, x, y, uOffset, vOffset, width, height, textureWidth, textureHeight);
         } else {
            int x2 = x + width;
            int y2 = y + height;
            Matrix4f matrix = guiGraphics.m_280168_().m_85850_().m_252922_();
            float minU = uOffset / textureWidth;
            float maxU = (uOffset + width) / textureWidth;
            float minV = vOffset / textureHeight;
            float maxV = (vOffset + height) / textureHeight;
            float alphaFrom = MekanismRenderer.getAlpha(colorFrom);
            float redFrom = MekanismRenderer.getRed(colorFrom);
            float greenFrom = MekanismRenderer.getGreen(colorFrom);
            float blueFrom = MekanismRenderer.getBlue(colorFrom);
            float alphaTo = MekanismRenderer.getAlpha(colorTo);
            float redTo = MekanismRenderer.getRed(colorTo);
            float greenTo = MekanismRenderer.getGreen(colorTo);
            float blueTo = MekanismRenderer.getBlue(colorTo);
            float percent = (float)progress;
            alphaTo = alphaFrom + percent * (alphaTo - alphaFrom);
            redTo = redFrom + percent * (redTo - redFrom);
            greenTo = greenFrom + percent * (greenTo - greenFrom);
            blueTo = blueFrom + percent * (blueTo - blueFrom);
            if (this.type.isReverse()) {
               float alphaTemp = alphaTo;
               float redTemp = redTo;
               float greenTemp = greenTo;
               float blueTemp = blueTo;
               alphaTo = alphaFrom;
               redTo = redFrom;
               greenTo = greenFrom;
               blueTo = blueFrom;
               alphaFrom = alphaTemp;
               redFrom = redTemp;
               greenFrom = greenTemp;
               blueFrom = blueTemp;
            }

            RenderSystem.enableBlend();
            RenderSystem.defaultBlendFunc();
            RenderSystem.setShaderTexture(0, resource);
            RenderSystem.setShader(GameRenderer::m_172814_);
            BufferBuilder builder = Tesselator.m_85913_().m_85915_();
            builder.m_166779_(Mode.QUADS, DefaultVertexFormat.f_85818_);
            if (this.type.isVertical()) {
               builder.m_252986_(matrix, x, y2, 0.0F).m_85950_(redTo, greenTo, blueTo, alphaTo).m_7421_(minU, maxV).m_5752_();
               builder.m_252986_(matrix, x2, y2, 0.0F).m_85950_(redTo, greenTo, blueTo, alphaTo).m_7421_(maxU, maxV).m_5752_();
               builder.m_252986_(matrix, x2, y, 0.0F).m_85950_(redFrom, greenFrom, blueFrom, alphaFrom).m_7421_(maxU, minV).m_5752_();
               builder.m_252986_(matrix, x, y, 0.0F).m_85950_(redFrom, greenFrom, blueFrom, alphaFrom).m_7421_(minU, minV).m_5752_();
            } else {
               builder.m_252986_(matrix, x, y2, 0.0F).m_85950_(redFrom, greenFrom, blueFrom, alphaFrom).m_7421_(minU, maxV).m_5752_();
               builder.m_252986_(matrix, x2, y2, 0.0F).m_85950_(redTo, greenTo, blueTo, alphaTo).m_7421_(maxU, maxV).m_5752_();
               builder.m_252986_(matrix, x2, y, 0.0F).m_85950_(redTo, greenTo, blueTo, alphaTo).m_7421_(maxU, minV).m_5752_();
               builder.m_252986_(matrix, x, y, 0.0F).m_85950_(redFrom, greenFrom, blueFrom, alphaFrom).m_7421_(minU, minV).m_5752_();
            }

            BufferUploader.m_231202_(builder.m_231175_());
            RenderSystem.disableBlend();
         }
      } else {
         guiGraphics.m_280163_(resource, x, y, uOffset, vOffset, width, height, textureWidth, textureHeight);
      }
   }

   public interface ColorDetails {
      int getColorFrom();

      int getColorTo();
   }
}
