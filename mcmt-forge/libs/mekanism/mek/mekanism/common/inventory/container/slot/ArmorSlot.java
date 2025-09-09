package mekanism.common.inventory.container.slot;

import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.InventoryMenu;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.enchantment.EnchantmentHelper;
import org.jetbrains.annotations.NotNull;

public class ArmorSlot extends InsertableSlot {
   protected static final ResourceLocation[] ARMOR_SLOT_TEXTURES = new ResourceLocation[]{
      InventoryMenu.f_39696_, InventoryMenu.f_39695_, InventoryMenu.f_39694_, InventoryMenu.f_39693_
   };
   private final EquipmentSlot slotType;

   public ArmorSlot(Inventory inventory, int index, int x, int y, EquipmentSlot slotType) {
      super(inventory, index, x, y);
      this.slotType = slotType;
      this.setBackground(InventoryMenu.f_39692_, ARMOR_SLOT_TEXTURES[this.slotType.m_20749_()]);
   }

   public int m_6641_() {
      return 1;
   }

   public boolean m_5857_(ItemStack stack) {
      return stack.canEquip(this.slotType, ((Inventory)this.f_40218_).f_35978_);
   }

   public boolean m_8010_(@NotNull Player player) {
      ItemStack stack = this.m_7993_();
      return (stack.m_41619_() || player.m_7500_() || !EnchantmentHelper.m_44920_(stack)) && super.m_8010_(player);
   }
}
