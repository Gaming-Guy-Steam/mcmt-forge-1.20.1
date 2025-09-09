package mekanism.client.gui.element.gauge;

import java.util.ArrayList;
import java.util.List;
import java.util.function.BooleanSupplier;
import mekanism.api.text.EnumColor;
import mekanism.client.gui.GuiMekanismTile;
import mekanism.client.gui.GuiUtils;
import mekanism.client.gui.IGuiWrapper;
import mekanism.client.gui.element.GuiTexturedElement;
import mekanism.client.gui.element.slot.GuiSlot;
import mekanism.client.render.MekanismRenderer;
import mekanism.common.MekanismLang;
import mekanism.common.inventory.warning.ISupportsWarning;
import mekanism.common.inventory.warning.WarningTracker;
import mekanism.common.item.ItemConfigurator;
import mekanism.common.lib.transmitter.TransmissionType;
import mekanism.common.tile.component.config.ConfigInfo;
import mekanism.common.tile.component.config.DataType;
import mekanism.common.tile.interfaces.ISideConfiguration;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.ItemStack;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public abstract class GuiGauge<T> extends GuiTexturedElement implements ISupportsWarning<GuiGauge<T>> {
   private final GaugeType gaugeType;
   protected boolean dummy;
   protected T dummyType;
   @Nullable
   private BooleanSupplier warningSupplier;

   public GuiGauge(GaugeType gaugeType, IGuiWrapper gui, int x, int y) {
      this(gaugeType, gui, x, y, gaugeType.getGaugeOverlay().getWidth() + 2, gaugeType.getGaugeOverlay().getHeight() + 2);
   }

   public GuiGauge(GaugeType gaugeType, IGuiWrapper gui, int x, int y, int sizeX, int sizeY) {
      super(gaugeType.getGaugeOverlay().getBarOverlay(), gui, x, y, sizeX, sizeY);
      this.gaugeType = gaugeType;
   }

   public GuiGauge<T> warning(@NotNull WarningTracker.WarningType type, @NotNull BooleanSupplier warningSupplier) {
      this.warningSupplier = ISupportsWarning.compound(this.warningSupplier, this.gui().trackWarning(type, warningSupplier));
      return this;
   }

   public abstract int getScaledLevel();

   @Nullable
   public abstract TextureAtlasSprite getIcon();

   public abstract Component getLabel();

   public abstract List<Component> getTooltipText();

   public GaugeOverlay getGaugeOverlay() {
      return this.gaugeType.getGaugeOverlay();
   }

   protected GaugeInfo getGaugeColor() {
      return this.gaugeType.getGaugeInfo();
   }

   protected void applyRenderColor(GuiGraphics guiGraphics) {
   }

   @Override
   public void drawBackground(@NotNull GuiGraphics guiGraphics, int mouseX, int mouseY, float partialTicks) {
      super.drawBackground(guiGraphics, mouseX, mouseY, partialTicks);
      GaugeInfo color = this.getGaugeColor();
      this.renderExtendedTexture(guiGraphics, color.getResourceLocation(), color.getSideWidth(), color.getSideHeight());
      if (!this.dummy) {
         this.renderContents(guiGraphics);
      }
   }

   public void renderContents(GuiGraphics guiGraphics) {
      boolean warning = this.warningSupplier != null && this.warningSupplier.getAsBoolean();
      if (warning) {
         guiGraphics.m_280163_(
            GuiSlot.WARNING_BACKGROUND_TEXTURE, this.relativeX + 1, this.relativeY + 1, 0.0F, 0.0F, this.f_93618_ - 2, this.f_93619_ - 2, 256, 256
         );
      }

      int scale = this.getScaledLevel();
      TextureAtlasSprite icon = this.getIcon();
      if (scale > 0 && icon != null) {
         this.applyRenderColor(guiGraphics);
         this.drawTiledSprite(
            guiGraphics, this.relativeX + 1, this.relativeY + 1, this.f_93619_ - 2, this.f_93618_ - 2, scale, icon, GuiUtils.TilingDirection.UP_RIGHT
         );
         MekanismRenderer.resetColor(guiGraphics);
         if (warning && (double)scale / (this.f_93619_ - 2) > 0.98) {
            int halfWidth = (this.f_93618_ - 2) / 2;
            guiGraphics.m_280163_(WARNING_TEXTURE, this.relativeX + 1 + halfWidth, this.relativeY + 1, halfWidth, 0.0F, halfWidth, this.f_93619_ - 2, 256, 256);
         }
      }

      this.drawBarOverlay(guiGraphics);
   }

   public void drawBarOverlay(GuiGraphics guiGraphics) {
      GaugeOverlay gaugeOverlay = this.getGaugeOverlay();
      guiGraphics.m_280411_(
         this.getResource(),
         this.relativeX + 1,
         this.relativeY + 1,
         this.m_5711_() - 2,
         this.m_93694_() - 2,
         0.0F,
         0.0F,
         gaugeOverlay.getWidth(),
         gaugeOverlay.getHeight(),
         gaugeOverlay.getWidth(),
         gaugeOverlay.getHeight()
      );
   }

   @Override
   public void renderToolTip(@NotNull GuiGraphics guiGraphics, int mouseX, int mouseY) {
      super.renderToolTip(guiGraphics, mouseX, mouseY);
      ItemStack stack = this.gui().getCarriedItem();
      EnumColor color = this.getGaugeColor().getColor();
      if (!stack.m_41619_() && stack.m_41720_() instanceof ItemConfigurator && color != null) {
         if (this.gui() instanceof GuiMekanismTile<?, ?> gui && gui.getTileEntity() instanceof ISideConfiguration sideConfig && this.getTransmission() != null) {
            DataType dataType = null;
            ConfigInfo config = sideConfig.getConfig().getConfig(this.getTransmission());
            if (config != null) {
               for (DataType type : config.getSupportedDataTypes()) {
                  if (type.getColor() == color) {
                     dataType = type;
                     break;
                  }
               }
            }

            if (dataType == null) {
               this.displayTooltips(
                  guiGraphics, mouseX, mouseY, new Component[]{MekanismLang.GENERIC_PARENTHESIS.translateColored(color, new Object[]{color.getName()})}
               );
            } else {
               this.displayTooltips(
                  guiGraphics,
                  mouseX,
                  mouseY,
                  new Component[]{MekanismLang.GENERIC_WITH_PARENTHESIS.translateColored(color, new Object[]{dataType, color.getName()})}
               );
            }
         }
      } else {
         List<Component> list = new ArrayList<>();
         if (this.getLabel() != null) {
            list.add(this.getLabel());
         }

         list.addAll(this.getTooltipText());
         this.displayTooltips(guiGraphics, mouseX, mouseY, list);
      }
   }

   @Nullable
   public abstract TransmissionType getTransmission();

   public void setDummyType(T type) {
      this.dummyType = type;
   }
}
