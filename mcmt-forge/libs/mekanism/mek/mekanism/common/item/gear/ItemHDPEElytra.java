package mekanism.common.item.gear;

import mekanism.common.registries.MekanismItems;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.item.ElytraItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Item.Properties;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class ItemHDPEElytra extends ElytraItem {
   public ItemHDPEElytra(Properties properties) {
      super(properties);
   }

   @Nullable
   public EquipmentSlot getEquipmentSlot(ItemStack stack) {
      return EquipmentSlot.CHEST;
   }

   public boolean m_6832_(@NotNull ItemStack toRepair, ItemStack repair) {
      return repair.m_41720_() == MekanismItems.HDPE_SHEET.m_5456_();
   }
}
