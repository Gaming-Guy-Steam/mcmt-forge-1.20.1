package mekanism.client.gui.item;

import mekanism.client.gui.GuiMekanism;
import mekanism.client.gui.element.tab.GuiSecurityTab;
import mekanism.common.inventory.container.item.PersonalStorageItemContainer;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Inventory;
import org.jetbrains.annotations.NotNull;

public class GuiPersonalStorageItem extends GuiMekanism<PersonalStorageItemContainer> {
   public GuiPersonalStorageItem(PersonalStorageItemContainer container, Inventory inv, Component title) {
      super(container, inv, title);
      this.f_97727_ += 56;
      this.f_97731_ = this.f_97727_ - 94;
      this.dynamicSlots = true;
   }

   @Override
   protected void addGuiElements() {
      super.addGuiElements();
      this.addRenderableWidget(new GuiSecurityTab(this, ((PersonalStorageItemContainer)this.f_97732_).getHand()));
   }

   @Override
   protected void drawForegroundText(@NotNull GuiGraphics guiGraphics, int mouseX, int mouseY) {
      this.renderTitleText(guiGraphics);
      this.drawString(guiGraphics, this.f_169604_, this.f_97730_, this.f_97731_, this.titleTextColor());
      super.drawForegroundText(guiGraphics, mouseX, mouseY);
   }
}
