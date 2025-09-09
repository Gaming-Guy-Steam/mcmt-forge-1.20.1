package mekanism.common.item.block.transmitter;

import java.util.List;
import mekanism.api.text.EnumColor;
import mekanism.client.key.MekKeyHandler;
import mekanism.client.key.MekanismKeyHandler;
import mekanism.common.MekanismLang;
import mekanism.common.block.attribute.Attribute;
import mekanism.common.block.transmitter.BlockUniversalCable;
import mekanism.common.item.block.ItemBlockMekanism;
import mekanism.common.tier.CableTier;
import mekanism.common.util.text.EnergyDisplay;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.Item.Properties;
import net.minecraft.world.level.Level;
import org.jetbrains.annotations.NotNull;

public class ItemBlockUniversalCable extends ItemBlockMekanism<BlockUniversalCable> {
   public ItemBlockUniversalCable(BlockUniversalCable block) {
      super(block, new Properties());
   }

   @NotNull
   public CableTier getTier() {
      return Attribute.getTier(this.m_40614_(), CableTier.class);
   }

   public void m_7373_(@NotNull ItemStack stack, Level world, @NotNull List<Component> tooltip, @NotNull TooltipFlag flag) {
      if (MekKeyHandler.isKeyPressed(MekanismKeyHandler.detailsKey)) {
         tooltip.add(MekanismLang.CAPABLE_OF_TRANSFERRING.translateColored(EnumColor.DARK_GRAY, new Object[0]));
         tooltip.add(MekanismLang.GENERIC_TRANSFER.translateColored(EnumColor.PURPLE, new Object[]{MekanismLang.ENERGY_FORGE_SHORT, MekanismLang.FORGE}));
         tooltip.add(MekanismLang.GENERIC_TRANSFER.translateColored(EnumColor.PURPLE, new Object[]{MekanismLang.ENERGY_EU_SHORT, MekanismLang.IC2}));
         tooltip.add(MekanismLang.GENERIC_TRANSFER.translateColored(EnumColor.PURPLE, new Object[]{MekanismLang.ENERGY_JOULES_PLURAL, MekanismLang.MEKANISM}));
      } else {
         CableTier tier = this.getTier();
         tooltip.add(MekanismLang.CAPACITY_PER_TICK.translateColored(EnumColor.INDIGO, new Object[]{EnumColor.GRAY, EnergyDisplay.of(tier.getCableCapacity())}));
         tooltip.add(MekanismLang.HOLD_FOR_DETAILS.translateColored(EnumColor.GRAY, new Object[]{EnumColor.INDIGO, MekanismKeyHandler.detailsKey.m_90863_()}));
      }
   }
}
