package mekanism.common.item;

import java.util.List;
import mekanism.api.text.EnumColor;
import mekanism.api.text.TextComponentUtil;
import mekanism.common.MekanismLang;
import mekanism.common.content.qio.IQIODriveItem;
import mekanism.common.tier.QIODriveTier;
import mekanism.common.util.text.TextUtils;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.Item.Properties;
import net.minecraft.world.level.Level;
import org.jetbrains.annotations.NotNull;

public class ItemQIODrive extends Item implements IQIODriveItem {
   private final QIODriveTier tier;

   public ItemQIODrive(QIODriveTier tier, Properties properties) {
      super(properties.m_41487_(1));
      this.tier = tier;
   }

   public void m_7373_(@NotNull ItemStack stack, Level world, @NotNull List<Component> tooltip, @NotNull TooltipFlag flag) {
      IQIODriveItem.DriveMetadata meta = IQIODriveItem.DriveMetadata.load(stack);
      tooltip.add(
         MekanismLang.QIO_ITEMS_DETAIL
            .translateColored(EnumColor.GRAY, new Object[]{EnumColor.INDIGO, TextUtils.format(meta.count()), TextUtils.format(this.getCountCapacity(stack))})
      );
      tooltip.add(
         MekanismLang.QIO_TYPES_DETAIL
            .translateColored(
               EnumColor.GRAY, new Object[]{EnumColor.INDIGO, TextUtils.format((long)meta.types()), TextUtils.format((long)this.getTypeCapacity(stack))}
            )
      );
   }

   @NotNull
   public Component m_7626_(@NotNull ItemStack stack) {
      return TextComponentUtil.build(this.tier.getBaseTier().getColor(), super.m_7626_(stack));
   }

   @Override
   public long getCountCapacity(ItemStack stack) {
      return this.tier.getMaxCount();
   }

   @Override
   public int getTypeCapacity(ItemStack stack) {
      return this.tier.getMaxTypes();
   }
}
