package mekanism.client.gui.qio;

import mekanism.client.gui.element.GuiScreenSwitch;
import mekanism.common.Mekanism;
import mekanism.common.MekanismLang;
import mekanism.common.inventory.container.tile.MekanismTileContainer;
import mekanism.common.network.to_server.PacketGuiInteract;
import mekanism.common.tile.qio.TileEntityQIOExporter;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Inventory;

public class GuiQIOExporter extends GuiQIOFilterHandler<TileEntityQIOExporter> {
   public GuiQIOExporter(MekanismTileContainer<TileEntityQIOExporter> container, Inventory inv, Component title) {
      super(container, inv, title);
   }

   @Override
   protected void addGuiElements() {
      super.addGuiElements();
      this.addRenderableWidget(
         new GuiScreenSwitch(
            this,
            9,
            122,
            this.f_97726_ - 18,
            MekanismLang.QIO_EXPORT_WITHOUT_FILTER.translate(new Object[0]),
            this.tile::getExportWithoutFilter,
            () -> Mekanism.packetHandler().sendToServer(new PacketGuiInteract(PacketGuiInteract.GuiInteraction.QIO_TOGGLE_EXPORT_WITHOUT_FILTER, this.tile))
         )
      );
   }
}
