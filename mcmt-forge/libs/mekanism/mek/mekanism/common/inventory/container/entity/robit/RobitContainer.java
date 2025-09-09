package mekanism.common.inventory.container.entity.robit;

import mekanism.api.inventory.IInventorySlot;
import mekanism.common.entity.EntityRobit;
import mekanism.common.inventory.container.entity.MekanismEntityContainer;
import mekanism.common.registration.impl.ContainerTypeRegistryObject;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.Slot;
import org.jetbrains.annotations.NotNull;

public class RobitContainer extends MekanismEntityContainer<EntityRobit> {
   public RobitContainer(ContainerTypeRegistryObject<?> type, int id, Inventory inv, EntityRobit robit) {
      super(type, id, inv, robit);
      robit.addContainerTrackers(this);
   }

   @Override
   protected void addSlots() {
      super.addSlots();
      if (this.entity.hasInventory()) {
         for (IInventorySlot inventorySlot : this.entity.getContainerInventorySlots(this.m_6772_())) {
            Slot containerSlot = inventorySlot.createContainerSlot();
            if (containerSlot != null) {
               this.m_38897_(containerSlot);
            }
         }
      }
   }

   @Override
   protected void openInventory(@NotNull Inventory inv) {
      super.openInventory(inv);
      this.entity.open(inv.f_35978_);
   }

   @Override
   protected void closeInventory(@NotNull Player player) {
      super.closeInventory(player);
      this.entity.close(player);
   }
}
