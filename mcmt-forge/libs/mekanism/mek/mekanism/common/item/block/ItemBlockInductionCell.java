package mekanism.common.item.block;

import java.util.List;
import mekanism.api.text.EnumColor;
import mekanism.common.MekanismLang;
import mekanism.common.block.attribute.Attribute;
import mekanism.common.block.prefab.BlockTile;
import mekanism.common.content.blocktype.BlockTypeTile;
import mekanism.common.tier.InductionCellTier;
import mekanism.common.tile.multiblock.TileEntityInductionCell;
import mekanism.common.util.StorageUtils;
import mekanism.common.util.text.EnergyDisplay;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.Item.Properties;
import net.minecraft.world.level.Level;
import org.jetbrains.annotations.NotNull;

public class ItemBlockInductionCell extends ItemBlockTooltip<BlockTile<TileEntityInductionCell, BlockTypeTile<TileEntityInductionCell>>> {
   public ItemBlockInductionCell(BlockTile<TileEntityInductionCell, BlockTypeTile<TileEntityInductionCell>> block) {
      super(block, new Properties());
   }

   @NotNull
   public InductionCellTier getTier() {
      return Attribute.getTier(this.m_40614_(), InductionCellTier.class);
   }

   @Override
   protected void addStats(@NotNull ItemStack stack, Level world, @NotNull List<Component> tooltip, @NotNull TooltipFlag flag) {
      InductionCellTier tier = this.getTier();
      tooltip.add(MekanismLang.CAPACITY.translateColored(tier.getBaseTier().getColor(), new Object[]{EnumColor.GRAY, EnergyDisplay.of(tier.getMaxEnergy())}));
      tooltip.add(
         MekanismLang.STORED_ENERGY
            .translateColored(
               EnumColor.BRIGHT_GREEN, new Object[]{EnumColor.GRAY, EnergyDisplay.of(StorageUtils.getStoredEnergyFromNBT(stack), tier.getMaxEnergy())}
            )
      );
   }
}
