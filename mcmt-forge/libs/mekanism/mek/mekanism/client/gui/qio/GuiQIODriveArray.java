package mekanism.client.gui.qio;

import mekanism.client.gui.GuiMekanismTile;
import mekanism.client.gui.element.custom.GuiQIOFrequencyDataScreen;
import mekanism.client.gui.element.tab.GuiQIOFrequencyTab;
import mekanism.common.inventory.container.tile.MekanismTileContainer;
import mekanism.common.tile.qio.TileEntityQIODriveArray;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Inventory;
import org.jetbrains.annotations.NotNull;

public class GuiQIODriveArray extends GuiMekanismTile<TileEntityQIODriveArray, MekanismTileContainer<TileEntityQIODriveArray>> {
   public GuiQIODriveArray(MekanismTileContainer<TileEntityQIODriveArray> container, Inventory inv, Component title) {
      super(container, inv, title);
      this.dynamicSlots = true;
      this.f_97727_ += 40;
      this.f_97731_ = this.f_97727_ - 94;
   }

   @Override
   protected void addGuiElements() {
      super.addGuiElements();
      this.addRenderableWidget(new GuiQIOFrequencyTab(this, this.tile));
      this.addRenderableWidget(new GuiQIOFrequencyDataScreen(this, 15, 19, this.f_97726_ - 32, 46, this.tile::getQIOFrequency));
   }

   @Override
   protected void drawForegroundText(@NotNull GuiGraphics guiGraphics, int mouseX, int mouseY) {
      this.renderTitleText(guiGraphics);
      this.drawString(guiGraphics, this.f_169604_, this.f_97730_, this.f_97731_, this.titleTextColor());
      super.drawForegroundText(guiGraphics, mouseX, mouseY);
   }
}
