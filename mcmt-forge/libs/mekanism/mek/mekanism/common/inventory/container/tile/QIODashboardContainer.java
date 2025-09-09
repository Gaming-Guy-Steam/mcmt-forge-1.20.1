package mekanism.common.inventory.container.tile;

import mekanism.common.inventory.container.QIOItemViewerContainer;
import mekanism.common.registries.MekanismContainerTypes;
import mekanism.common.tile.qio.TileEntityQIODashboard;
import mekanism.common.util.WorldUtils;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraftforge.common.capabilities.ICapabilityProvider;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class QIODashboardContainer extends QIOItemViewerContainer {
   private final TileEntityQIODashboard tile;

   public QIODashboardContainer(int id, Inventory inv, TileEntityQIODashboard tile, boolean remote) {
      super(MekanismContainerTypes.QIO_DASHBOARD, id, inv, remote, tile);
      this.tile = tile;
      tile.addContainerTrackers(this);
      this.addSlotsAndOpen();
   }

   public QIODashboardContainer recreate() {
      QIODashboardContainer container = new QIODashboardContainer(this.f_38840_, this.inv, this.tile, true);
      this.sync(container);
      return container;
   }

   @Override
   protected void openInventory(@NotNull Inventory inv) {
      super.openInventory(inv);
      this.tile.open(inv.f_35978_);
   }

   @Override
   protected void closeInventory(@NotNull Player player) {
      super.closeInventory(player);
      this.tile.close(player);
   }

   public boolean m_6875_(@NotNull Player player) {
      return this.tile.hasGui() && !this.tile.m_58901_() && WorldUtils.isBlockLoaded(this.tile.m_58904_(), this.tile.m_58899_());
   }

   public TileEntityQIODashboard getTileEntity() {
      return this.tile;
   }

   @Nullable
   @Override
   public ICapabilityProvider getSecurityObject() {
      return this.tile;
   }
}
