package mekanism.client.gui.element.bar;

import java.util.function.BooleanSupplier;
import mekanism.client.gui.IGuiWrapper;
import mekanism.client.gui.element.GuiTexturedElement;
import mekanism.client.gui.element.slot.GuiSlot;
import mekanism.common.inventory.warning.ISupportsWarning;
import mekanism.common.inventory.warning.WarningTracker;
import mekanism.common.util.MekanismUtils;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public abstract class GuiBar<INFO extends GuiBar.IBarInfoHandler> extends GuiTexturedElement implements ISupportsWarning<GuiBar<INFO>> {
   public static final ResourceLocation BAR = MekanismUtils.getResource(MekanismUtils.ResourceType.GUI_BAR, "base.png");
   private final INFO handler;
   protected final boolean horizontal;
   @Nullable
   private BooleanSupplier warningSupplier;

   public GuiBar(ResourceLocation resource, IGuiWrapper gui, INFO handler, int x, int y, int width, int height, boolean horizontal) {
      super(resource, gui, x, y, width + 2, height + 2);
      this.handler = handler;
      this.horizontal = horizontal;
   }

   public GuiBar<INFO> warning(@NotNull WarningTracker.WarningType type, @NotNull BooleanSupplier warningSupplier) {
      this.warningSupplier = ISupportsWarning.compound(this.warningSupplier, this.gui().trackWarning(type, warningSupplier));
      return this;
   }

   public INFO getHandler() {
      return this.handler;
   }

   @Override
   public void drawBackground(@NotNull GuiGraphics guiGraphics, int mouseX, int mouseY, float partialTicks) {
      this.renderExtendedTexture(guiGraphics, BAR, 2, 2);
      boolean warning = this.warningSupplier != null && this.warningSupplier.getAsBoolean();
      if (warning) {
         guiGraphics.m_280163_(
            GuiSlot.WARNING_BACKGROUND_TEXTURE, this.relativeX + 1, this.relativeY + 1, 0.0F, 0.0F, this.f_93618_ - 2, this.f_93619_ - 2, 256, 256
         );
      }

      this.drawContentsChecked(guiGraphics, mouseX, mouseY, partialTicks, this.handler.getLevel(), warning);
   }

   void drawContentsChecked(@NotNull GuiGraphics guiGraphics, int mouseX, int mouseY, float partialTicks, double handlerLevel, boolean warning) {
      if (handlerLevel > 0.0) {
         this.renderBarOverlay(guiGraphics, mouseX, mouseY, partialTicks, handlerLevel);
         if (warning && handlerLevel >= 0.98) {
            if (this.horizontal) {
               int halfHeight = (this.f_93619_ - 2) / 2;
               guiGraphics.m_280163_(
                  WARNING_TEXTURE, this.relativeX + 1, this.relativeY + 1 + halfHeight, 0.0F, halfHeight, this.f_93618_ - 2, halfHeight, 256, 256
               );
            } else {
               int halfWidth = (this.f_93618_ - 2) / 2;
               guiGraphics.m_280163_(
                  WARNING_TEXTURE, this.relativeX + 1 + halfWidth, this.relativeY + 1, halfWidth, 0.0F, halfWidth, this.f_93619_ - 2, 256, 256
               );
            }
         }
      }
   }

   protected abstract void renderBarOverlay(GuiGraphics guiGraphics, int mouseX, int mouseY, float partialTicks, double handlerLevel);

   @Override
   public void renderToolTip(@NotNull GuiGraphics guiGraphics, int mouseX, int mouseY) {
      super.renderToolTip(guiGraphics, mouseX, mouseY);
      Component tooltip = this.handler.getTooltip();
      if (tooltip != null) {
         this.displayTooltips(guiGraphics, mouseX, mouseY, new Component[]{tooltip});
      }
   }

   protected static int calculateScaled(double scale, int value) {
      if (scale == 1.0) {
         return value;
      } else {
         return scale < 1.0 ? (int)(scale * value) : (int)Math.round(scale * value);
      }
   }

   public interface IBarInfoHandler {
      @Nullable
      default Component getTooltip() {
         return null;
      }

      double getLevel();
   }
}
