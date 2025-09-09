package mekanism.common.item.block.machine;

import java.util.List;
import mekanism.api.text.EnumColor;
import mekanism.common.MekanismLang;
import mekanism.common.block.attribute.Attribute;
import mekanism.common.block.attribute.AttributeFactoryType;
import mekanism.common.block.prefab.BlockFactoryMachine;
import mekanism.common.tier.FactoryTier;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.level.Level;
import org.jetbrains.annotations.NotNull;

public class ItemBlockFactory extends ItemBlockMachine {
   public ItemBlockFactory(BlockFactoryMachine.BlockFactory<?> block) {
      super(block);
   }

   public FactoryTier getTier() {
      return Attribute.getTier(this.m_40614_(), FactoryTier.class);
   }

   @Override
   protected void addTypeDetails(@NotNull ItemStack stack, Level world, @NotNull List<Component> tooltip, @NotNull TooltipFlag flag) {
      Attribute.ifPresent(
         this.m_40614_(),
         AttributeFactoryType.class,
         attribute -> tooltip.add(MekanismLang.FACTORY_TYPE.translateColored(EnumColor.INDIGO, new Object[]{EnumColor.GRAY, attribute.getFactoryType()}))
      );
      super.addTypeDetails(stack, world, tooltip, flag);
   }
}
