package mekanism.common.item.block.transmitter;

import java.util.List;
import mekanism.api.text.EnumColor;
import mekanism.client.key.MekKeyHandler;
import mekanism.client.key.MekanismKeyHandler;
import mekanism.common.MekanismLang;
import mekanism.common.block.attribute.Attribute;
import mekanism.common.block.transmitter.BlockThermodynamicConductor;
import mekanism.common.item.block.ItemBlockMekanism;
import mekanism.common.tier.ConductorTier;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.Item.Properties;
import net.minecraft.world.level.Level;
import org.jetbrains.annotations.NotNull;

public class ItemBlockThermodynamicConductor extends ItemBlockMekanism<BlockThermodynamicConductor> {
   public ItemBlockThermodynamicConductor(BlockThermodynamicConductor block) {
      super(block, new Properties());
   }

   @NotNull
   public ConductorTier getTier() {
      return Attribute.getTier(this.m_40614_(), ConductorTier.class);
   }

   public void m_7373_(@NotNull ItemStack stack, Level world, @NotNull List<Component> tooltip, @NotNull TooltipFlag flag) {
      if (MekKeyHandler.isKeyPressed(MekanismKeyHandler.detailsKey)) {
         tooltip.add(MekanismLang.CAPABLE_OF_TRANSFERRING.translateColored(EnumColor.DARK_GRAY, new Object[0]));
         tooltip.add(MekanismLang.HEAT.translateColored(EnumColor.PURPLE, new Object[]{MekanismLang.MEKANISM}));
      } else {
         ConductorTier tier = this.getTier();
         tooltip.add(MekanismLang.CONDUCTION.translateColored(EnumColor.INDIGO, new Object[]{EnumColor.GRAY, tier.getInverseConduction()}));
         tooltip.add(MekanismLang.INSULATION.translateColored(EnumColor.INDIGO, new Object[]{EnumColor.GRAY, tier.getBaseConductionInsulation()}));
         tooltip.add(MekanismLang.HEAT_CAPACITY.translateColored(EnumColor.INDIGO, new Object[]{EnumColor.GRAY, tier.getHeatCapacity()}));
         tooltip.add(MekanismLang.HOLD_FOR_DETAILS.translateColored(EnumColor.GRAY, new Object[]{EnumColor.INDIGO, MekanismKeyHandler.detailsKey.m_90863_()}));
      }
   }
}
