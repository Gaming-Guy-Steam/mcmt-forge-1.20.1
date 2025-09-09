package mekanism.common.lib.radial;

import mekanism.api.IIncrementalEnum;
import mekanism.api.radial.mode.IRadialMode;
import mekanism.common.util.ItemDataUtils;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;

public interface IRadialEnumModeItem<MODE extends Enum<MODE> & IIncrementalEnum<MODE> & IRadialMode> extends IRadialModeItem<MODE> {
   String getModeSaveKey();

   MODE getModeByIndex(int ordinal);

   default MODE getMode(ItemStack stack) {
      return this.getModeByIndex(ItemDataUtils.getInt(stack, this.getModeSaveKey()));
   }

   default void setMode(ItemStack stack, Player player, MODE mode) {
      ItemDataUtils.setInt(stack, this.getModeSaveKey(), mode.ordinal());
   }
}
