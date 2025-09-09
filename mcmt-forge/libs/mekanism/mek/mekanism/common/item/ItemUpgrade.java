package mekanism.common.item;

import java.util.List;
import mekanism.api.Upgrade;
import mekanism.api.text.APILang;
import mekanism.api.text.EnumColor;
import mekanism.client.key.MekKeyHandler;
import mekanism.client.key.MekanismKeyHandler;
import mekanism.common.MekanismLang;
import mekanism.common.item.interfaces.IUpgradeItem;
import mekanism.common.tile.component.TileComponentUpgrade;
import mekanism.common.tile.interfaces.IUpgradeTile;
import mekanism.common.util.WorldUtils;
import net.minecraft.network.chat.Component;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Rarity;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.Item.Properties;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.Level;
import org.jetbrains.annotations.NotNull;

public class ItemUpgrade extends Item implements IUpgradeItem {
   private final Upgrade upgrade;

   public ItemUpgrade(Upgrade type, Properties properties) {
      super(properties.m_41497_(Rarity.UNCOMMON));
      this.upgrade = type;
   }

   public void m_7373_(@NotNull ItemStack stack, Level world, @NotNull List<Component> tooltip, @NotNull TooltipFlag flag) {
      if (MekKeyHandler.isKeyPressed(MekanismKeyHandler.detailsKey)) {
         Upgrade upgradeType = this.getUpgradeType(stack);
         tooltip.add(upgradeType.getDescription());
         tooltip.add(APILang.UPGRADE_MAX_INSTALLED.translate(new Object[]{upgradeType.getMax()}));
      } else {
         tooltip.add(MekanismLang.HOLD_FOR_DETAILS.translateColored(EnumColor.GRAY, new Object[]{EnumColor.INDIGO, MekanismKeyHandler.detailsKey.m_90863_()}));
      }
   }

   @Override
   public Upgrade getUpgradeType(ItemStack stack) {
      return this.upgrade;
   }

   @NotNull
   public InteractionResult m_6225_(UseOnContext context) {
      Player player = context.m_43723_();
      if (player != null && player.m_6144_()) {
         Level world = context.m_43725_();
         if (WorldUtils.getTileEntity(world, context.m_8083_()) instanceof IUpgradeTile upgradeTile && upgradeTile.supportsUpgrades()) {
            TileComponentUpgrade component = upgradeTile.getComponent();
            ItemStack stack = context.m_43722_();
            Upgrade type = this.getUpgradeType(stack);
            if (component.supports(type)) {
               if (!world.f_46443_) {
                  int added = component.addUpgrades(type, stack.m_41613_());
                  if (added > 0) {
                     stack.m_41774_(added);
                  }
               }

               return InteractionResult.m_19078_(world.f_46443_);
            }
         }
      }

      return InteractionResult.PASS;
   }
}
