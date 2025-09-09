package mekanism.common.item.block;

import java.util.List;
import mekanism.api.text.EnumColor;
import mekanism.common.MekanismLang;
import mekanism.common.block.BlockRadioactiveWasteBarrel;
import mekanism.common.config.MekanismConfig;
import mekanism.common.util.UnitDisplayUtils;
import mekanism.common.util.text.TextUtils;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.Item.Properties;
import net.minecraft.world.level.Level;
import org.jetbrains.annotations.NotNull;

public class ItemBlockRadioactiveWasteBarrel extends ItemBlockTooltip<BlockRadioactiveWasteBarrel> {
   public ItemBlockRadioactiveWasteBarrel(BlockRadioactiveWasteBarrel block, Properties properties) {
      super(block, properties);
   }

   @Override
   protected void addStats(@NotNull ItemStack stack, Level world, @NotNull List<Component> tooltip, @NotNull TooltipFlag flag) {
      tooltip.add(
         MekanismLang.CAPACITY_MB
            .translateColored(EnumColor.INDIGO, new Object[]{EnumColor.GRAY, TextUtils.format(MekanismConfig.general.radioactiveWasteBarrelMaxGas.get())})
      );
      int ticks = MekanismConfig.general.radioactiveWasteBarrelProcessTicks.get();
      long decayAmount = MekanismConfig.general.radioactiveWasteBarrelDecayAmount.get();
      if (decayAmount != 0L && ticks != 1) {
         tooltip.add(
            MekanismLang.WASTE_BARREL_DECAY_RATE
               .translateColored(
                  EnumColor.INDIGO, new Object[]{EnumColor.GRAY, TextUtils.format(UnitDisplayUtils.roundDecimals((double)decayAmount / ticks, 4))}
               )
         );
         tooltip.add(
            MekanismLang.WASTE_BARREL_DECAY_RATE_ACTUAL
               .translateColored(EnumColor.INDIGO, new Object[]{EnumColor.GRAY, TextUtils.format(decayAmount), EnumColor.GRAY, TextUtils.format((long)ticks)})
         );
      } else {
         tooltip.add(MekanismLang.WASTE_BARREL_DECAY_RATE.translateColored(EnumColor.INDIGO, new Object[]{EnumColor.GRAY, TextUtils.format(decayAmount)}));
      }
   }
}
