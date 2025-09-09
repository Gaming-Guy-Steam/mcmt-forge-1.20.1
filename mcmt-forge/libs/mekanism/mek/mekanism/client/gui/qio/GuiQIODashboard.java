package mekanism.client.gui.qio;

import mekanism.client.gui.element.tab.GuiQIOFrequencyTab;
import mekanism.client.gui.element.tab.GuiSecurityTab;
import mekanism.common.content.qio.QIOFrequency;
import mekanism.common.inventory.container.tile.QIODashboardContainer;
import mekanism.common.lib.frequency.Frequency;
import mekanism.common.tile.qio.TileEntityQIODashboard;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Inventory;

public class GuiQIODashboard extends GuiQIOItemViewer<QIODashboardContainer> {
   private final TileEntityQIODashboard tile;

   public GuiQIODashboard(QIODashboardContainer container, Inventory inv, Component title) {
      super(container, inv, title);
      this.tile = container.getTileEntity();
   }

   @Override
   protected void addGuiElements() {
      super.addGuiElements();
      this.addRenderableWidget(new GuiQIOFrequencyTab(this, this.tile));
      this.addRenderableWidget(new GuiSecurityTab(this, this.tile));
   }

   public GuiQIOItemViewer<QIODashboardContainer> recreate(QIODashboardContainer container) {
      return new GuiQIODashboard(container, this.inv, this.f_96539_);
   }

   @Override
   public Frequency.FrequencyIdentity getFrequency() {
      QIOFrequency freq = this.tile.getQIOFrequency();
      return freq == null ? null : freq.getIdentity();
   }
}
