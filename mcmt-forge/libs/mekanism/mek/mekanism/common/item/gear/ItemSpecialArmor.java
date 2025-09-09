package mekanism.common.item.gear;

import java.util.ArrayList;
import java.util.List;
import mekanism.common.capabilities.ItemCapabilityWrapper;
import mekanism.common.integration.gender.GenderCapabilityHelper;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.item.ArmorItem;
import net.minecraft.world.item.ArmorMaterial;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.ArmorItem.Type;
import net.minecraft.world.item.Item.Properties;
import net.minecraft.world.item.enchantment.Enchantment;
import net.minecraftforge.common.capabilities.ICapabilityProvider;
import org.jetbrains.annotations.NotNull;

public abstract class ItemSpecialArmor extends ArmorItem {
   protected ItemSpecialArmor(ArmorMaterial material, Type armorType, Properties properties) {
      super(material, armorType, properties);
   }

   public String getArmorTexture(ItemStack stack, Entity entity, EquipmentSlot slot, String type) {
      return "mekanism:render/null_armor.png";
   }

   public boolean m_8120_(@NotNull ItemStack stack) {
      return this.f_40379_.m_6646_() > 0 && super.m_8120_(stack);
   }

   public boolean isBookEnchantable(ItemStack stack, ItemStack book) {
      return this.m_8120_(stack) && super.isBookEnchantable(stack, book);
   }

   public boolean canApplyAtEnchantingTable(ItemStack stack, Enchantment enchantment) {
      return this.m_8120_(stack) && super.canApplyAtEnchantingTable(stack, enchantment);
   }

   protected boolean areCapabilityConfigsLoaded() {
      return true;
   }

   protected void gatherCapabilities(List<ItemCapabilityWrapper.ItemCapability> capabilities, ItemStack stack, CompoundTag nbt) {
      GenderCapabilityHelper.addGenderCapability(this, capabilities::add);
   }

   public final ICapabilityProvider initCapabilities(ItemStack stack, CompoundTag nbt) {
      if (!this.areCapabilityConfigsLoaded()) {
         return super.initCapabilities(stack, nbt);
      } else {
         List<ItemCapabilityWrapper.ItemCapability> capabilities = new ArrayList<>();
         this.gatherCapabilities(capabilities, stack, nbt);
         return (ICapabilityProvider)(capabilities.isEmpty()
            ? super.initCapabilities(stack, nbt)
            : new ItemCapabilityWrapper(stack, capabilities.toArray(ItemCapabilityWrapper.ItemCapability[]::new)));
      }
   }
}
