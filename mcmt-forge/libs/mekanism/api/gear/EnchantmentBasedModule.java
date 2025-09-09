package mekanism.api.gear;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Map.Entry;
import mekanism.api.annotations.NothingNullByDefault;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.world.item.enchantment.Enchantment;
import net.minecraft.world.item.enchantment.EnchantmentHelper;

@NothingNullByDefault
public abstract class EnchantmentBasedModule<MODULE extends EnchantmentBasedModule<MODULE>> implements ICustomModule<MODULE> {
   public abstract Enchantment getEnchantment();

   @Override
   public void onAdded(IModule<MODULE> module, boolean first) {
      if (module.isEnabled()) {
         if (first) {
            this.enchant(module, this.getEnchantment());
         } else {
            Map<Enchantment, Integer> enchantments = this.getEnchantments(module);
            enchantments.put(this.getEnchantment(), module.getInstalledCount());
            this.setEnchantments(module, enchantments);
         }
      }
   }

   @Override
   public void onRemoved(IModule<MODULE> module, boolean last) {
      if (module.isEnabled()) {
         Map<Enchantment, Integer> enchantments = this.getEnchantments(module);
         if (last) {
            enchantments.remove(this.getEnchantment());
         } else {
            enchantments.put(this.getEnchantment(), module.getInstalledCount());
         }

         this.setEnchantments(module, enchantments);
      }
   }

   @Override
   public void onEnabledStateChange(IModule<MODULE> module) {
      if (module.isEnabled()) {
         this.enchant(module, this.getEnchantment());
      } else {
         Map<Enchantment, Integer> enchantments = this.getEnchantments(module);
         enchantments.remove(this.getEnchantment());
         this.setEnchantments(module, enchantments);
      }
   }

   private void enchant(IModule<MODULE> module, Enchantment enchantment) {
      CompoundTag dataMap = this.getOrCreateDataTag(module);
      ListTag enchantments;
      if (dataMap.m_128425_("Enchantments", 9)) {
         enchantments = dataMap.m_128437_("Enchantments", 10);
      } else {
         dataMap.m_128365_("Enchantments", enchantments = new ListTag());
      }

      enchantments.add(EnchantmentHelper.m_182443_(EnchantmentHelper.m_182432_(enchantment), (byte)module.getInstalledCount()));
   }

   private Map<Enchantment, Integer> getEnchantments(IModule<MODULE> module) {
      CompoundTag tag = module.getContainer().m_41783_();
      if (tag != null && tag.m_128425_("mekData", 10)) {
         CompoundTag mekData = tag.m_128469_("mekData");
         ListTag enchantmentTag = mekData.m_128437_("Enchantments", 10);
         return EnchantmentHelper.m_44882_(enchantmentTag);
      } else {
         return new LinkedHashMap<>();
      }
   }

   private CompoundTag getOrCreateDataTag(IModule<MODULE> module) {
      CompoundTag tag = module.getContainer().m_41784_();
      if (tag.m_128425_("mekData", 10)) {
         return tag.m_128469_("mekData");
      } else {
         CompoundTag dataMap = new CompoundTag();
         tag.m_128365_("mekData", dataMap);
         return dataMap;
      }
   }

   private void setEnchantments(IModule<MODULE> module, Map<Enchantment, Integer> enchantments) {
      ListTag enchantmentTag = new ListTag();

      for (Entry<Enchantment, Integer> entry : enchantments.entrySet()) {
         Enchantment enchantment = entry.getKey();
         if (enchantment != null) {
            enchantmentTag.add(EnchantmentHelper.m_182443_(EnchantmentHelper.m_182432_(enchantment), entry.getValue()));
         }
      }

      CompoundTag dataMap = this.getOrCreateDataTag(module);
      if (enchantments.isEmpty()) {
         dataMap.m_128473_("Enchantments");
         if (dataMap.m_128456_()) {
            module.getContainer().m_41749_("mekData");
         }
      } else {
         dataMap.m_128365_("Enchantments", enchantmentTag);
      }
   }
}
