package mekanism.client.gui.qio;

import mekanism.client.gui.element.tab.GuiQIOFrequencyTab;
import mekanism.common.inventory.container.item.PortableQIODashboardContainer;
import mekanism.common.lib.frequency.Frequency;
import mekanism.common.lib.frequency.IFrequencyItem;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Inventory;

public class GuiPortableQIODashboard extends GuiQIOItemViewer<PortableQIODashboardContainer> {
   public GuiPortableQIODashboard(PortableQIODashboardContainer container, Inventory inv, Component title) {
      super(container, inv, title);
   }

   @Override
   protected void addGuiElements() {
      super.addGuiElements();
      this.addRenderableWidget(new GuiQIOFrequencyTab(this, ((PortableQIODashboardContainer)this.f_97732_).getHand()));
   }

   public GuiQIOItemViewer<PortableQIODashboardContainer> recreate(PortableQIODashboardContainer container) {
      return new GuiPortableQIODashboard(container, this.inv, this.f_96539_);
   }

   @Override
   public Frequency.FrequencyIdentity getFrequency() {
      return ((IFrequencyItem)((PortableQIODashboardContainer)this.f_97732_).getStack().m_41720_())
         .getFrequencyIdentity(((PortableQIODashboardContainer)this.f_97732_).getStack());
   }
}
