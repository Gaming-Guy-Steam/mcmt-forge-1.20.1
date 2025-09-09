package mekanism.client.gui;

import mekanism.client.gui.element.bar.GuiVerticalPowerBar;
import mekanism.client.gui.element.custom.GuiFrequencySelector;
import mekanism.client.gui.element.custom.GuiTeleporterStatus;
import mekanism.common.content.teleporter.TeleporterFrequency;
import mekanism.common.inventory.container.tile.MekanismTileContainer;
import mekanism.common.lib.frequency.FrequencyType;
import mekanism.common.tile.TileEntityTeleporter;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Inventory;
import org.jetbrains.annotations.NotNull;

public class GuiTeleporter
   extends GuiMekanismTile<TileEntityTeleporter, MekanismTileContainer<TileEntityTeleporter>>
   implements GuiFrequencySelector.ITileGuiFrequencySelector<TeleporterFrequency, TileEntityTeleporter>,
   GuiFrequencySelector.IGuiColorFrequencySelector<TeleporterFrequency> {
   public GuiTeleporter(MekanismTileContainer<TileEntityTeleporter> container, Inventory inv, Component title) {
      super(container, inv, title);
      this.f_97727_ += 74;
      this.f_97729_ = 4;
      this.f_97731_ = this.f_97727_ - 93;
      this.dynamicSlots = true;
   }

   @Override
   protected void addGuiElements() {
      super.addGuiElements();
      this.addRenderableWidget(new GuiTeleporterStatus(this, () -> this.getFrequency() != null, () -> this.tile.status));
      this.addRenderableWidget(new GuiVerticalPowerBar(this, this.tile.getEnergyContainer(), 158, 26));
      this.addRenderableWidget(new GuiFrequencySelector<>(this, 14));
   }

   @Override
   protected void drawForegroundText(@NotNull GuiGraphics guiGraphics, int mouseX, int mouseY) {
      this.renderTitleText(guiGraphics);
      this.drawString(guiGraphics, this.f_169604_, this.f_97730_, this.f_97731_, this.titleTextColor());
      super.drawForegroundText(guiGraphics, mouseX, mouseY);
   }

   @Override
   public FrequencyType<TeleporterFrequency> getFrequencyType() {
      return FrequencyType.TELEPORTER;
   }
}
