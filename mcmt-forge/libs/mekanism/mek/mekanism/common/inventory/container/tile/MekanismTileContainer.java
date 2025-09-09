package mekanism.common.inventory.container.tile;

import mekanism.api.inventory.IInventorySlot;
import mekanism.common.inventory.container.IEmptyContainer;
import mekanism.common.inventory.container.MekanismContainer;
import mekanism.common.inventory.container.slot.VirtualInventoryContainerSlot;
import mekanism.common.registration.impl.ContainerTypeRegistryObject;
import mekanism.common.tile.base.TileEntityMekanism;
import mekanism.common.util.WorldUtils;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.Slot;
import net.minecraftforge.common.capabilities.ICapabilityProvider;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class MekanismTileContainer<TILE extends TileEntityMekanism> extends MekanismContainer {
   private VirtualInventoryContainerSlot upgradeSlot;
   private VirtualInventoryContainerSlot upgradeOutputSlot;
   @NotNull
   protected final TILE tile;

   public MekanismTileContainer(ContainerTypeRegistryObject<?> type, int id, Inventory inv, @NotNull TILE tile) {
      super(type, id, inv);
      this.tile = tile;
      this.addContainerTrackers();
      this.addSlotsAndOpen();
   }

   protected void addContainerTrackers() {
      this.tile.addContainerTrackers(this);
   }

   public TILE getTileEntity() {
      return this.tile;
   }

   @Nullable
   @Override
   public ICapabilityProvider getSecurityObject() {
      return this.tile;
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

   @Override
   protected void addSlots() {
      super.addSlots();
      if (!(this instanceof IEmptyContainer)) {
         if (this.tile.supportsUpgrades()) {
            this.m_38897_(this.upgradeSlot = this.tile.getComponent().getUpgradeSlot().createContainerSlot());
            this.m_38897_(this.upgradeOutputSlot = this.tile.getComponent().getUpgradeOutputSlot().createContainerSlot());
         }

         if (this.tile.hasInventory()) {
            for (IInventorySlot inventorySlot : this.tile.getInventorySlots(null)) {
               Slot containerSlot = inventorySlot.createContainerSlot();
               if (containerSlot != null) {
                  this.m_38897_(containerSlot);
               }
            }
         }
      }
   }

   @Nullable
   public VirtualInventoryContainerSlot getUpgradeSlot() {
      return this.upgradeSlot;
   }

   @Nullable
   public VirtualInventoryContainerSlot getUpgradeOutputSlot() {
      return this.upgradeOutputSlot;
   }
}
