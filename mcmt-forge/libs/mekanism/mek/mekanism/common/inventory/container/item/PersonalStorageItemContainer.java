package mekanism.common.inventory.container.item;

import mekanism.api.inventory.IInventorySlot;
import mekanism.common.inventory.container.slot.HotBarSlot;
import mekanism.common.lib.inventory.personalstorage.AbstractPersonalStorageItemInventory;
import mekanism.common.lib.inventory.personalstorage.ClientSidePersonalStorageInventory;
import mekanism.common.lib.inventory.personalstorage.PersonalStorageManager;
import mekanism.common.registries.MekanismContainerTypes;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.ClickType;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;
import org.jetbrains.annotations.NotNull;

public class PersonalStorageItemContainer extends MekanismItemContainer {
   private final AbstractPersonalStorageItemInventory itemInventory;

   public PersonalStorageItemContainer(int id, Inventory inv, InteractionHand hand, ItemStack stack, boolean isRemote) {
      super(MekanismContainerTypes.PERSONAL_STORAGE_ITEM, id, inv, hand, stack);
      this.itemInventory = (AbstractPersonalStorageItemInventory)(!isRemote
         ? PersonalStorageManager.getInventoryFor(stack).orElseThrow(() -> new IllegalStateException("Inventory not available"))
         : new ClientSidePersonalStorageInventory());
      super.addSlotsAndOpen();
   }

   @Override
   protected void addSlotsAndOpen() {
   }

   @Override
   protected void addSlots() {
      super.addSlots();

      for (IInventorySlot inventorySlot : this.itemInventory.getInventorySlots(null)) {
         Slot containerSlot = inventorySlot.createContainerSlot();
         if (containerSlot != null) {
            this.m_38897_(containerSlot);
         }
      }
   }

   public InteractionHand getHand() {
      return this.hand;
   }

   @Override
   protected int getInventoryYOffset() {
      return 140;
   }

   @Override
   protected HotBarSlot createHotBarSlot(@NotNull Inventory inv, int index, int x, int y) {
      return index == inv.f_35977_ && this.hand == InteractionHand.MAIN_HAND ? new HotBarSlot(inv, index, x, y) {
         public boolean m_8010_(@NotNull Player player) {
            return false;
         }
      } : super.createHotBarSlot(inv, index, x, y);
   }

   public void m_150399_(int slotId, int dragType, @NotNull ClickType clickType, @NotNull Player player) {
      if (clickType == ClickType.SWAP) {
         if (this.hand == InteractionHand.OFF_HAND && dragType == 40) {
            return;
         }

         if (this.hand == InteractionHand.MAIN_HAND && dragType >= 0 && dragType < Inventory.m_36059_() && !this.hotBarSlots.get(dragType).m_8010_(player)) {
            return;
         }
      }

      super.m_150399_(slotId, dragType, clickType, player);
   }
}
