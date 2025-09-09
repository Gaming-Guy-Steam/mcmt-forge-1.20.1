package mekanism.client.gui.element.tab.window;

import java.util.function.Supplier;
import mekanism.client.SpecialColors;
import mekanism.client.gui.IGuiWrapper;
import mekanism.client.gui.element.window.GuiUpgradeWindow;
import mekanism.client.gui.element.window.GuiWindow;
import mekanism.client.render.MekanismRenderer;
import mekanism.common.MekanismLang;
import mekanism.common.tile.base.TileEntityMekanism;
import mekanism.common.util.MekanismUtils;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.network.chat.Component;
import org.jetbrains.annotations.NotNull;

public class GuiUpgradeWindowTab extends GuiWindowCreatorTab<TileEntityMekanism, GuiUpgradeWindowTab> {
   public GuiUpgradeWindowTab(IGuiWrapper gui, TileEntityMekanism tile, Supplier<GuiUpgradeWindowTab> elementSupplier) {
      super(MekanismUtils.getResource(MekanismUtils.ResourceType.GUI, "upgrade.png"), gui, tile, gui.getWidth(), 6, 26, 18, false, elementSupplier);
   }

   @Override
   public void renderToolTip(@NotNull GuiGraphics guiGraphics, int mouseX, int mouseY) {
      super.renderToolTip(guiGraphics, mouseX, mouseY);
      this.displayTooltips(guiGraphics, mouseX, mouseY, new Component[]{MekanismLang.UPGRADES.translate(new Object[0])});
   }

   @Override
   protected void colorTab(GuiGraphics guiGraphics) {
      MekanismRenderer.color(guiGraphics, SpecialColors.TAB_UPGRADE);
   }

   @Override
   protected GuiWindow createWindow() {
      return new GuiUpgradeWindow(this.gui(), this.getGuiWidth() / 2 - 78, 15, this.dataSource);
   }
}
