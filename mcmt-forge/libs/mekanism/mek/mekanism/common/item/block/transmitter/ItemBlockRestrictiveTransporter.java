package mekanism.common.item.block.transmitter;

import java.util.List;
import mekanism.api.text.EnumColor;
import mekanism.client.key.MekKeyHandler;
import mekanism.client.key.MekanismKeyHandler;
import mekanism.common.MekanismLang;
import mekanism.common.block.transmitter.BlockRestrictiveTransporter;
import mekanism.common.item.block.ItemBlockMekanism;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.Item.Properties;
import net.minecraft.world.level.Level;
import org.jetbrains.annotations.NotNull;

public class ItemBlockRestrictiveTransporter extends ItemBlockMekanism<BlockRestrictiveTransporter> {
   public ItemBlockRestrictiveTransporter(BlockRestrictiveTransporter block) {
      super(block, new Properties());
   }

   public void m_7373_(@NotNull ItemStack stack, Level world, @NotNull List<Component> tooltip, @NotNull TooltipFlag flag) {
      if (MekKeyHandler.isKeyPressed(MekanismKeyHandler.detailsKey)) {
         tooltip.add(MekanismLang.CAPABLE_OF_TRANSFERRING.translateColored(EnumColor.DARK_GRAY, new Object[0]));
         tooltip.add(MekanismLang.ITEMS.translateColored(EnumColor.PURPLE, new Object[]{MekanismLang.UNIVERSAL}));
         tooltip.add(MekanismLang.BLOCKS.translateColored(EnumColor.PURPLE, new Object[]{MekanismLang.UNIVERSAL}));
         tooltip.add(MekanismLang.DESCRIPTION_RESTRICTIVE.translateColored(EnumColor.DARK_RED, new Object[0]));
      } else {
         tooltip.add(MekanismLang.HOLD_FOR_DETAILS.translateColored(EnumColor.GRAY, new Object[]{EnumColor.INDIGO, MekanismKeyHandler.detailsKey.m_90863_()}));
      }
   }
}
