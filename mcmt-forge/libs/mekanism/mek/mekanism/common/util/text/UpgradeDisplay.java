package mekanism.common.util.text;

import mekanism.api.Upgrade;
import mekanism.api.annotations.NothingNullByDefault;
import mekanism.api.text.EnumColor;
import mekanism.api.text.IHasTextComponent;
import mekanism.common.MekanismLang;
import net.minecraft.network.chat.Component;

@NothingNullByDefault
public class UpgradeDisplay implements IHasTextComponent {
   private final Upgrade upgrade;
   private final int level;

   private UpgradeDisplay(Upgrade upgrade, int level) {
      this.upgrade = upgrade;
      this.level = level;
   }

   public static UpgradeDisplay of(Upgrade upgrade) {
      return of(upgrade, 0);
   }

   public static UpgradeDisplay of(Upgrade upgrade, int level) {
      return new UpgradeDisplay(upgrade, level);
   }

   @Override
   public Component getTextComponent() {
      return this.upgrade.getMax() > 1 && this.level > 0
         ? MekanismLang.UPGRADE_DISPLAY_LEVEL.translateColored(this.upgrade.getColor(), new Object[]{this.upgrade, EnumColor.GRAY, this.level})
         : MekanismLang.GENERIC_LIST.translateColored(this.upgrade.getColor(), new Object[]{this.upgrade});
   }
}
