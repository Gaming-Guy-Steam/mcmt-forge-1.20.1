package mekanism.common.item.block;

import java.util.List;
import mekanism.api.text.EnumColor;
import mekanism.common.MekanismLang;
import mekanism.common.block.attribute.Attribute;
import mekanism.common.block.basic.BlockBin;
import mekanism.common.inventory.BinMekanismInventory;
import mekanism.common.inventory.slot.BinInventorySlot;
import mekanism.common.item.interfaces.IItemSustainedInventory;
import mekanism.common.tier.BinTier;
import mekanism.common.util.text.TextUtils;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.Item.Properties;
import net.minecraft.world.level.Level;
import org.jetbrains.annotations.NotNull;

public class ItemBlockBin extends ItemBlockTooltip<BlockBin> implements IItemSustainedInventory {
   public ItemBlockBin(BlockBin block) {
      super(block, new Properties().m_41487_(1));
   }

   public BinTier getTier() {
      return Attribute.getTier(this.m_40614_(), BinTier.class);
   }

   @Override
   protected void addStats(@NotNull ItemStack stack, Level world, @NotNull List<Component> tooltip, @NotNull TooltipFlag flag) {
      BinMekanismInventory inventory = BinMekanismInventory.create(stack);
      BinTier tier = this.getTier();
      if (inventory != null && tier != null) {
         BinInventorySlot slot = inventory.getBinSlot();
         if (slot.isEmpty()) {
            tooltip.add(MekanismLang.EMPTY.translateColored(EnumColor.DARK_RED, new Object[0]));
         } else {
            tooltip.add(MekanismLang.STORING.translateColored(EnumColor.BRIGHT_GREEN, new Object[]{EnumColor.GRAY, slot.getStack()}));
            if (tier == BinTier.CREATIVE) {
               tooltip.add(MekanismLang.ITEM_AMOUNT.translateColored(EnumColor.PURPLE, new Object[]{EnumColor.GRAY, MekanismLang.INFINITE}));
            } else {
               tooltip.add(MekanismLang.ITEM_AMOUNT.translateColored(EnumColor.PURPLE, new Object[]{EnumColor.GRAY, TextUtils.format((long)slot.getCount())}));
            }
         }

         if (slot.isLocked()) {
            tooltip.add(MekanismLang.LOCKED.translateColored(EnumColor.AQUA, new Object[]{EnumColor.GRAY, slot.getLockStack()}));
         }

         if (tier == BinTier.CREATIVE) {
            tooltip.add(MekanismLang.CAPACITY.translateColored(EnumColor.INDIGO, new Object[]{EnumColor.GRAY, MekanismLang.INFINITE}));
         } else {
            tooltip.add(MekanismLang.CAPACITY_ITEMS.translateColored(EnumColor.INDIGO, new Object[]{EnumColor.GRAY, TextUtils.format((long)tier.getStorage())}));
         }
      }
   }

   @Override
   public boolean canContentsDrop(ItemStack stack) {
      return this.getTier() != BinTier.CREATIVE;
   }
}
