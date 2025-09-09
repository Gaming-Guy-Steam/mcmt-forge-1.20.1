package mekanism.client.gui.qio;

import mekanism.client.gui.element.GuiScreenSwitch;
import mekanism.common.Mekanism;
import mekanism.common.MekanismLang;
import mekanism.common.inventory.container.tile.MekanismTileContainer;
import mekanism.common.network.to_server.PacketGuiInteract;
import mekanism.common.tile.qio.TileEntityQIOImporter;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Inventory;

public class GuiQIOImporter extends GuiQIOFilterHandler<TileEntityQIOImporter> {
   public GuiQIOImporter(MekanismTileContainer<TileEntityQIOImporter> container, Inventory inv, Component title) {
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
            MekanismLang.QIO_IMPORT_WITHOUT_FILTER.translate(new Object[0]),
            this.tile::getImportWithoutFilter,
            () -> Mekanism.packetHandler().sendToServer(new PacketGuiInteract(PacketGuiInteract.GuiInteraction.QIO_TOGGLE_IMPORT_WITHOUT_FILTER, this.tile))
         )
      );
   }
}
