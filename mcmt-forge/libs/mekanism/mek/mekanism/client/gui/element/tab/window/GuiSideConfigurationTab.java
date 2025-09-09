package mekanism.client.gui.element.tab.window;

import java.util.function.Supplier;
import mekanism.client.SpecialColors;
import mekanism.client.gui.IGuiWrapper;
import mekanism.client.gui.element.window.GuiSideConfiguration;
import mekanism.client.gui.element.window.GuiWindow;
import mekanism.client.render.MekanismRenderer;
import mekanism.common.MekanismLang;
import mekanism.common.tile.base.TileEntityMekanism;
import mekanism.common.tile.interfaces.ISideConfiguration;
import mekanism.common.util.MekanismUtils;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.network.chat.Component;
import org.jetbrains.annotations.NotNull;

public class GuiSideConfigurationTab<TILE extends TileEntityMekanism & ISideConfiguration> extends GuiWindowCreatorTab<TILE, GuiSideConfigurationTab<TILE>> {
   public GuiSideConfigurationTab(IGuiWrapper gui, TILE tile, Supplier<GuiSideConfigurationTab<TILE>> elementSupplier) {
      super(MekanismUtils.getResource(MekanismUtils.ResourceType.GUI, "configuration.png"), gui, tile, -26, 6, 26, 18, true, elementSupplier);
   }

   @Override
   public void renderToolTip(@NotNull GuiGraphics guiGraphics, int mouseX, int mouseY) {
      super.renderToolTip(guiGraphics, mouseX, mouseY);
      this.displayTooltips(guiGraphics, mouseX, mouseY, new Component[]{MekanismLang.SIDE_CONFIG.translate(new Object[0])});
   }

   @Override
   protected void colorTab(GuiGraphics guiGraphics) {
      MekanismRenderer.color(guiGraphics, SpecialColors.TAB_CONFIGURATION);
   }

   @Override
   protected GuiWindow createWindow() {
      return new GuiSideConfiguration(this.gui(), this.getGuiWidth() / 2 - 78, 15, this.dataSource);
   }
}
