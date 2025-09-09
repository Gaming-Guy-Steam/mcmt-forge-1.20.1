package mekanism.common.item.gear;

import mekanism.api.annotations.NothingNullByDefault;
import mekanism.common.capabilities.ItemCapabilityWrapper;
import mekanism.common.capabilities.radiation.item.RadiationShieldingHandler;
import mekanism.common.integration.gender.GenderCapabilityHelper;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.item.ArmorItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Rarity;
import net.minecraft.world.item.ArmorItem.Type;
import net.minecraft.world.item.Item.Properties;
import net.minecraft.world.item.ItemStack.TooltipPart;
import net.minecraft.world.item.enchantment.Enchantment;
import net.minecraftforge.common.capabilities.ICapabilityProvider;
import org.jetbrains.annotations.NotNull;

public class ItemHazmatSuitArmor extends ArmorItem {
   private static final ItemHazmatSuitArmor.HazmatMaterial HAZMAT_MATERIAL = new ItemHazmatSuitArmor.HazmatMaterial();

   public ItemHazmatSuitArmor(Type armorType, Properties properties) {
      super(HAZMAT_MATERIAL, armorType, properties.m_41497_(Rarity.UNCOMMON));
   }

   public static double getShieldingByArmor(Type type) {
      return switch (type) {
         case HELMET -> 0.25;
         case CHESTPLATE -> 0.4;
         case LEGGINGS -> 0.2;
         case BOOTS -> 0.15;
         default -> throw new IncompatibleClassChangeError();
      };
   }

   public int getDefaultTooltipHideFlags(@NotNull ItemStack stack) {
      return super.getDefaultTooltipHideFlags(stack) | TooltipPart.MODIFIERS.m_41809_();
   }

   public ICapabilityProvider initCapabilities(ItemStack stack, CompoundTag nbt) {
      ItemCapabilityWrapper wrapper = new ItemCapabilityWrapper(stack, RadiationShieldingHandler.create(item -> getShieldingByArmor(this.m_266204_())));
      GenderCapabilityHelper.addGenderCapability(this, xva$0 -> wrapper.add(xva$0));
      return wrapper;
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

   @NothingNullByDefault
   protected static class HazmatMaterial extends BaseSpecialArmorMaterial {
      public String m_6082_() {
         return "mekanism:hazmat";
      }
   }
}
