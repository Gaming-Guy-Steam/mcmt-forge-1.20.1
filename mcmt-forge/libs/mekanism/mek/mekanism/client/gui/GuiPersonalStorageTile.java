package mekanism.client.gui;

import mekanism.common.inventory.container.tile.MekanismTileContainer;
import mekanism.common.tile.TileEntityPersonalStorage;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Inventory;
import org.jetbrains.annotations.NotNull;

public class GuiPersonalStorageTile extends GuiMekanismTile<TileEntityPersonalStorage, MekanismTileContainer<TileEntityPersonalStorage>> {
   public GuiPersonalStorageTile(MekanismTileContainer<TileEntityPersonalStorage> container, Inventory inv, Component title) {
      super(container, inv, title);
      this.f_97727_ += 56;
      this.f_97731_ = this.f_97727_ - 94;
      this.dynamicSlots = true;
   }

   @Override
   protected void drawForegroundText(@NotNull GuiGraphics guiGraphics, int mouseX, int mouseY) {
      this.renderTitleText(guiGraphics);
      this.drawString(guiGraphics, this.f_169604_, this.f_97730_, this.f_97731_, this.titleTextColor());
      super.drawForegroundText(guiGraphics, mouseX, mouseY);
   }
}
