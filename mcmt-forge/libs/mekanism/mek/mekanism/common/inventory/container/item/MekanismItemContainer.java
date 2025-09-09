package mekanism.common.inventory.container.item;

import mekanism.common.inventory.container.MekanismContainer;
import mekanism.common.registration.impl.ContainerTypeRegistryObject;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.common.capabilities.ICapabilityProvider;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public abstract class MekanismItemContainer extends MekanismContainer {
   protected final InteractionHand hand;
   protected final ItemStack stack;

   protected MekanismItemContainer(ContainerTypeRegistryObject<?> type, int id, Inventory inv, InteractionHand hand, ItemStack stack) {
      super(type, id, inv);
      this.hand = hand;
      this.stack = stack;
      if (!stack.m_41619_()) {
         this.addContainerTrackers();
      }

      this.addSlotsAndOpen();
   }

   protected void addContainerTrackers() {
      if (this.stack.m_41720_() instanceof MekanismItemContainer.IItemContainerTracker containerTracker) {
         containerTracker.addContainerTrackers(this, this.stack);
      }
   }

   @Nullable
   @Override
   public ICapabilityProvider getSecurityObject() {
      return this.stack;
   }

   public boolean m_6875_(@NotNull Player player) {
      return !this.stack.m_41619_() && player.m_21120_(this.hand).m_150930_(this.stack.m_41720_());
   }

   public interface IItemContainerTracker {
      void addContainerTrackers(MekanismContainer container, ItemStack stack);
   }
}
