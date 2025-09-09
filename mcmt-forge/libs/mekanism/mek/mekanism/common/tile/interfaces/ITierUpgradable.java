package mekanism.common.tile.interfaces;

import mekanism.common.upgrade.IUpgradeData;
import org.jetbrains.annotations.Nullable;

public interface ITierUpgradable {
   boolean canBeUpgraded();

   @Nullable
   default IUpgradeData getUpgradeData() {
      return null;
   }
}
