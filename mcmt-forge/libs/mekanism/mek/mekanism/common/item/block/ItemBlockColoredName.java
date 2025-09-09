package mekanism.common.item.block;

import mekanism.api.text.EnumColor;
import mekanism.api.text.TextComponentUtil;
import mekanism.common.block.interfaces.IColoredBlock;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Item.Properties;
import net.minecraft.world.level.block.Block;
import org.jetbrains.annotations.NotNull;

public class ItemBlockColoredName extends BlockItem {
   public <BLOCK extends Block & IColoredBlock> ItemBlockColoredName(BLOCK block) {
      this(block, new Properties());
   }

   public <BLOCK extends Block & IColoredBlock> ItemBlockColoredName(BLOCK block, Properties properties) {
      super(block, properties);
   }

   @NotNull
   public Component m_7626_(@NotNull ItemStack stack) {
      EnumColor color = this.getColor(stack);
      if (color == EnumColor.BLACK) {
         color = EnumColor.DARK_GRAY;
      }

      return TextComponentUtil.build(color, super.m_7626_(stack));
   }

   private EnumColor getColor(ItemStack stack) {
      return stack.m_41720_() instanceof ItemBlockColoredName itemBlock ? ((IColoredBlock)itemBlock.m_40614_()).getColor() : EnumColor.BLACK;
   }
}
