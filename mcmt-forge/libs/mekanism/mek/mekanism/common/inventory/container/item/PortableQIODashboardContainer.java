package mekanism.common.inventory.container.item;

import mekanism.common.content.qio.IQIOCraftingWindowHolder;
import mekanism.common.inventory.PortableQIODashboardInventory;
import mekanism.common.inventory.container.QIOItemViewerContainer;
import mekanism.common.inventory.container.slot.HotBarSlot;
import mekanism.common.registries.MekanismContainerTypes;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.ClickType;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.common.capabilities.ICapabilityProvider;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class PortableQIODashboardContainer extends QIOItemViewerContainer {
   protected final InteractionHand hand;
   protected final ItemStack stack;

   private PortableQIODashboardContainer(
      int id, Inventory inv, InteractionHand hand, ItemStack stack, boolean remote, IQIOCraftingWindowHolder craftingWindowHolder
   ) {
      super(MekanismContainerTypes.PORTABLE_QIO_DASHBOARD, id, inv, remote, craftingWindowHolder);
      this.hand = hand;
      this.stack = stack;
      this.addSlotsAndOpen();
   }

   public PortableQIODashboardContainer(int id, Inventory inv, InteractionHand hand, ItemStack stack, boolean remote) {
      this(id, inv, hand, stack, remote, new PortableQIODashboardInventory(stack, inv));
   }

   public InteractionHand getHand() {
      return this.hand;
   }

   public ItemStack getStack() {
      return this.stack;
   }

   public PortableQIODashboardContainer recreate() {
      PortableQIODashboardContainer container = new PortableQIODashboardContainer(
         this.f_38840_, this.inv, this.hand, this.stack, true, this.craftingWindowHolder
      );
      this.sync(container);
      return container;
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

   @Nullable
   @Override
   public ICapabilityProvider getSecurityObject() {
      return this.stack;
   }

   public boolean m_6875_(@NotNull Player player) {
      return !this.stack.m_41619_() && player.m_21120_(this.hand).m_150930_(this.stack.m_41720_());
   }
}
