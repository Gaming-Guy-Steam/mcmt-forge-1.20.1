package mekanism.common.item.block;

import java.util.List;
import mekanism.api.text.EnumColor;
import mekanism.common.MekanismLang;
import mekanism.common.block.prefab.BlockTile;
import mekanism.common.content.blocktype.BlockTypeTile;
import mekanism.common.item.interfaces.IItemSustainedInventory;
import mekanism.common.tile.TileEntitySecurityDesk;
import mekanism.common.util.SecurityUtils;
import mekanism.common.util.text.BooleanStateDisplay;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.level.Level;
import org.jetbrains.annotations.NotNull;

public class ItemBlockSecurityDesk
   extends ItemBlockTooltip<BlockTile.BlockTileModel<TileEntitySecurityDesk, BlockTypeTile<TileEntitySecurityDesk>>>
   implements IItemSustainedInventory {
   public ItemBlockSecurityDesk(BlockTile.BlockTileModel<TileEntitySecurityDesk, BlockTypeTile<TileEntitySecurityDesk>> block) {
      super(block);
   }

   @Override
   protected void addDetails(@NotNull ItemStack stack, Level world, @NotNull List<Component> tooltip, @NotNull TooltipFlag flag) {
      SecurityUtils.get().addOwnerTooltip(stack, tooltip);
      tooltip.add(
         MekanismLang.HAS_INVENTORY
            .translateColored(EnumColor.AQUA, new Object[]{EnumColor.GRAY, BooleanStateDisplay.YesNo.of(this.hasSustainedInventory(stack))})
      );
   }
}
