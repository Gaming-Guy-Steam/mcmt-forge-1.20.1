package mekanism.common.item.interfaces;

import mekanism.api.Upgrade;
import net.minecraft.world.item.ItemStack;

public interface IUpgradeItem {
   Upgrade getUpgradeType(ItemStack stack);
}
