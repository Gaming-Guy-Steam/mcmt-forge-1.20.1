package mekanism.api.inventory;

import mekanism.api.annotations.NothingNullByDefault;
import net.minecraft.world.Container;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;

@NothingNullByDefault
public final class IgnoredIInventory implements Container {
   public static final IgnoredIInventory INSTANCE = new IgnoredIInventory();

   private IgnoredIInventory() {
   }

   public int m_6643_() {
      return 0;
   }

   public boolean m_7983_() {
      return true;
   }

   public ItemStack m_8020_(int index) {
      return ItemStack.f_41583_;
   }

   public ItemStack m_7407_(int index, int count) {
      return ItemStack.f_41583_;
   }

   public ItemStack m_8016_(int index) {
      return ItemStack.f_41583_;
   }

   public void m_6836_(int index, ItemStack stack) {
   }

   public void m_6596_() {
   }

   public boolean m_6542_(Player player) {
      return false;
   }

   public void m_6211_() {
   }
}
