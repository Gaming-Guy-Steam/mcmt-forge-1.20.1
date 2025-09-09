package mekanism.common.item.block.transmitter;

import java.util.List;
import mekanism.api.text.EnumColor;
import mekanism.client.key.MekKeyHandler;
import mekanism.client.key.MekanismKeyHandler;
import mekanism.common.MekanismLang;
import mekanism.common.block.attribute.Attribute;
import mekanism.common.block.transmitter.BlockPressurizedTube;
import mekanism.common.item.block.ItemBlockMekanism;
import mekanism.common.tier.TubeTier;
import mekanism.common.util.text.TextUtils;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.Item.Properties;
import net.minecraft.world.level.Level;
import org.jetbrains.annotations.NotNull;

public class ItemBlockPressurizedTube extends ItemBlockMekanism<BlockPressurizedTube> {
   public ItemBlockPressurizedTube(BlockPressurizedTube block) {
      super(block, new Properties());
   }

   @NotNull
   public TubeTier getTier() {
      return Attribute.getTier(this.m_40614_(), TubeTier.class);
   }

   public void m_7373_(@NotNull ItemStack stack, Level world, @NotNull List<Component> tooltip, @NotNull TooltipFlag flag) {
      if (MekKeyHandler.isKeyPressed(MekanismKeyHandler.detailsKey)) {
         tooltip.add(MekanismLang.CAPABLE_OF_TRANSFERRING.translateColored(EnumColor.DARK_GRAY, new Object[0]));
         tooltip.add(MekanismLang.GASES.translateColored(EnumColor.PURPLE, new Object[]{MekanismLang.MEKANISM}));
         tooltip.add(MekanismLang.INFUSE_TYPES.translateColored(EnumColor.PURPLE, new Object[]{MekanismLang.MEKANISM}));
         tooltip.add(MekanismLang.PIGMENTS.translateColored(EnumColor.PURPLE, new Object[]{MekanismLang.MEKANISM}));
         tooltip.add(MekanismLang.SLURRIES.translateColored(EnumColor.PURPLE, new Object[]{MekanismLang.MEKANISM}));
      } else {
         TubeTier tier = this.getTier();
         tooltip.add(
            MekanismLang.CAPACITY_MB_PER_TICK.translateColored(EnumColor.INDIGO, new Object[]{EnumColor.GRAY, TextUtils.format(tier.getTubeCapacity())})
         );
         tooltip.add(MekanismLang.PUMP_RATE_MB.translateColored(EnumColor.INDIGO, new Object[]{EnumColor.GRAY, TextUtils.format(tier.getTubePullAmount())}));
         tooltip.add(MekanismLang.HOLD_FOR_DETAILS.translateColored(EnumColor.GRAY, new Object[]{EnumColor.INDIGO, MekanismKeyHandler.detailsKey.m_90863_()}));
      }
   }
}
