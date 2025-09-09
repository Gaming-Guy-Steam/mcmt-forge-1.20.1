package mekanism.client.gui;

import mekanism.client.gui.element.tab.window.GuiSideConfigurationTab;
import mekanism.client.gui.element.tab.window.GuiTransporterConfigTab;
import mekanism.common.inventory.container.tile.MekanismTileContainer;
import mekanism.common.tile.base.TileEntityMekanism;
import mekanism.common.tile.interfaces.ISideConfiguration;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Inventory;

public abstract class GuiConfigurableTile<TILE extends TileEntityMekanism & ISideConfiguration, CONTAINER extends MekanismTileContainer<TILE>>
   extends GuiMekanismTile<TILE, CONTAINER> {
   private GuiSideConfigurationTab<TILE> sideConfigTab;
   private GuiTransporterConfigTab<TILE> transporterConfigTab;

   protected GuiConfigurableTile(CONTAINER container, Inventory inv, Component title) {
      super(container, inv, title);
   }

   @Override
   protected void addGuiElements() {
      super.addGuiElements();
      this.sideConfigTab = this.addRenderableWidget(new GuiSideConfigurationTab<>(this, this.tile, () -> this.sideConfigTab));
      this.transporterConfigTab = this.addRenderableWidget(new GuiTransporterConfigTab<>(this, this.tile, () -> this.transporterConfigTab));
   }
}
