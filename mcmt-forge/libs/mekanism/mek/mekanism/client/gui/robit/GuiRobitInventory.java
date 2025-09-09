package mekanism.client.gui.robit;

import mekanism.common.inventory.container.entity.robit.RobitContainer;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Inventory;
import org.jetbrains.annotations.NotNull;

public class GuiRobitInventory extends GuiRobit<RobitContainer> {
   public GuiRobitInventory(RobitContainer container, Inventory inv, Component title) {
      super(container, inv, title);
      this.f_97731_ = this.f_97727_ - 93;
      this.dynamicSlots = true;
   }

   @Override
   protected void drawForegroundText(@NotNull GuiGraphics guiGraphics, int mouseX, int mouseY) {
      this.drawString(guiGraphics, this.f_96539_, this.f_97728_, this.f_97729_, this.titleTextColor());
      this.drawString(guiGraphics, this.f_169604_, this.f_97730_, this.f_97731_, this.titleTextColor());
      super.drawForegroundText(guiGraphics, mouseX, mouseY);
   }

   @Override
   protected boolean shouldOpenGui(GuiRobit.RobitGuiType guiType) {
      return guiType != GuiRobit.RobitGuiType.INVENTORY;
   }
}
