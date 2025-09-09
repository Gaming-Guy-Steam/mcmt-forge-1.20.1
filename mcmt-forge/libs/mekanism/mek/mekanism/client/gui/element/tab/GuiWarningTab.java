package mekanism.client.gui.element.tab;

import java.util.ArrayList;
import java.util.List;
import mekanism.api.text.EnumColor;
import mekanism.client.gui.IGuiWrapper;
import mekanism.client.gui.element.GuiTexturedElement;
import mekanism.common.MekanismLang;
import mekanism.common.inventory.warning.IWarningTracker;
import mekanism.common.util.MekanismUtils;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.network.chat.Component;
import org.jetbrains.annotations.NotNull;

public class GuiWarningTab extends GuiTexturedElement {
   private final IWarningTracker warningTracker;

   public GuiWarningTab(IGuiWrapper gui, IWarningTracker warningTracker, int y) {
      super(MekanismUtils.getResource(MekanismUtils.ResourceType.GUI_TAB, "warning_info.png"), gui, -26, y, 26, 26);
      this.warningTracker = warningTracker;
      this.updateVisibility();
   }

   private void updateVisibility() {
      this.f_93624_ = this.warningTracker.hasWarning();
   }

   @Override
   public void tick() {
      super.tick();
      this.updateVisibility();
   }

   @Override
   public void drawBackground(@NotNull GuiGraphics guiGraphics, int mouseX, int mouseY, float partialTicks) {
      super.drawBackground(guiGraphics, mouseX, mouseY, partialTicks);
      guiGraphics.m_280163_(this.getResource(), this.relativeX, this.relativeY, 0.0F, 0.0F, this.f_93618_, this.f_93619_, this.f_93618_, this.f_93619_);
   }

   @Override
   public void renderToolTip(@NotNull GuiGraphics guiGraphics, int mouseX, int mouseY) {
      super.renderToolTip(guiGraphics, mouseX, mouseY);
      List<Component> info = new ArrayList<>();
      info.add(MekanismLang.ISSUES.translateColored(EnumColor.YELLOW, new Object[0]));
      info.addAll(this.warningTracker.getWarnings());
      this.displayTooltips(guiGraphics, mouseX, mouseY, info);
   }
}
