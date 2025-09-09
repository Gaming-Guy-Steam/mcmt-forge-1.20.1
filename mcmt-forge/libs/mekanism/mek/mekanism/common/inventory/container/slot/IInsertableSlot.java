package mekanism.common.inventory.container.slot;

import mekanism.api.Action;
import mekanism.common.inventory.container.SelectedWindowData;
import net.minecraft.world.item.ItemStack;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public interface IInsertableSlot {
   @NotNull
   ItemStack insertItem(@NotNull ItemStack stack, Action action);

   default boolean canMergeWith(@NotNull ItemStack stack) {
      return true;
   }

   default boolean exists(@Nullable SelectedWindowData windowData) {
      return true;
   }
}
