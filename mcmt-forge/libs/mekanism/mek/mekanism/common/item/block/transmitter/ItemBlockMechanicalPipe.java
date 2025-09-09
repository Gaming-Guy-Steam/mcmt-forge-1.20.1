package mekanism.common.item.block.transmitter;

import java.util.List;
import mekanism.api.text.EnumColor;
import mekanism.client.key.MekKeyHandler;
import mekanism.client.key.MekanismKeyHandler;
import mekanism.common.MekanismLang;
import mekanism.common.block.attribute.Attribute;
import mekanism.common.block.transmitter.BlockMechanicalPipe;
import mekanism.common.item.block.ItemBlockMekanism;
import mekanism.common.tier.PipeTier;
import mekanism.common.util.text.TextUtils;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.Item.Properties;
import net.minecraft.world.level.Level;
import org.jetbrains.annotations.NotNull;

public class ItemBlockMechanicalPipe extends ItemBlockMekanism<BlockMechanicalPipe> {
   public ItemBlockMechanicalPipe(BlockMechanicalPipe block) {
      super(block, new Properties());
   }

   @NotNull
   public PipeTier getTier() {
      return Attribute.getTier(this.m_40614_(), PipeTier.class);
   }

   public void m_7373_(@NotNull ItemStack stack, Level world, @NotNull List<Component> tooltip, @NotNull TooltipFlag flag) {
      if (MekKeyHandler.isKeyPressed(MekanismKeyHandler.detailsKey)) {
         tooltip.add(MekanismLang.CAPABLE_OF_TRANSFERRING.translateColored(EnumColor.DARK_GRAY, new Object[0]));
         tooltip.add(MekanismLang.FLUIDS.translateColored(EnumColor.PURPLE, new Object[]{EnumColor.GRAY, MekanismLang.FORGE}));
      } else {
         PipeTier tier = this.getTier();
         tooltip.add(
            MekanismLang.CAPACITY_MB_PER_TICK.translateColored(EnumColor.INDIGO, new Object[]{EnumColor.GRAY, TextUtils.format((long)tier.getPipeCapacity())})
         );
         tooltip.add(
            MekanismLang.PUMP_RATE_MB.translateColored(EnumColor.INDIGO, new Object[]{EnumColor.GRAY, TextUtils.format((long)tier.getPipePullAmount())})
         );
         tooltip.add(MekanismLang.HOLD_FOR_DETAILS.translateColored(EnumColor.GRAY, new Object[]{EnumColor.INDIGO, MekanismKeyHandler.detailsKey.m_90863_()}));
      }
   }
}
