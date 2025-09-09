package mekanism.common.item.block;

import java.util.List;
import mekanism.api.text.EnumColor;
import mekanism.common.MekanismLang;
import mekanism.common.block.attribute.Attribute;
import mekanism.common.block.prefab.BlockTile;
import mekanism.common.content.blocktype.BlockTypeTile;
import mekanism.common.tier.InductionProviderTier;
import mekanism.common.tile.multiblock.TileEntityInductionProvider;
import mekanism.common.util.text.EnergyDisplay;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.Item.Properties;
import net.minecraft.world.level.Level;
import org.jetbrains.annotations.NotNull;

public class ItemBlockInductionProvider extends ItemBlockTooltip<BlockTile<TileEntityInductionProvider, BlockTypeTile<TileEntityInductionProvider>>> {
   public ItemBlockInductionProvider(BlockTile<TileEntityInductionProvider, BlockTypeTile<TileEntityInductionProvider>> block) {
      super(block, new Properties());
   }

   @NotNull
   public InductionProviderTier getTier() {
      return Attribute.getTier(this.m_40614_(), InductionProviderTier.class);
   }

   @Override
   protected void addStats(@NotNull ItemStack stack, Level world, @NotNull List<Component> tooltip, @NotNull TooltipFlag flag) {
      InductionProviderTier tier = this.getTier();
      tooltip.add(
         MekanismLang.INDUCTION_PORT_OUTPUT_RATE
            .translateColored(tier.getBaseTier().getColor(), new Object[]{EnumColor.GRAY, EnergyDisplay.of(tier.getOutput())})
      );
   }
}
