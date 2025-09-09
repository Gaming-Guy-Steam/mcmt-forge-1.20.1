package mekanism.common.inventory.slot;

import java.util.Objects;
import java.util.function.Predicate;
import java.util.function.ToIntFunction;
import mekanism.api.Action;
import mekanism.api.AutomationType;
import mekanism.api.IContentsListener;
import mekanism.api.annotations.NothingNullByDefault;
import mekanism.common.util.MekanismUtils;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.common.ForgeHooks;
import org.jetbrains.annotations.Nullable;

@NothingNullByDefault
public class FuelInventorySlot extends BasicInventorySlot {
   public static FuelInventorySlot forFuel(ToIntFunction<ItemStack> fuelValue, @Nullable IContentsListener listener, int x, int y) {
      Objects.requireNonNull(fuelValue, "Fuel value calculator cannot be null");
      return new FuelInventorySlot(stack -> fuelValue.applyAsInt(stack) == 0, stack -> fuelValue.applyAsInt(stack) > 0, alwaysTrue, listener, x, y);
   }

   private FuelInventorySlot(
      Predicate<ItemStack> canExtract, Predicate<ItemStack> canInsert, Predicate<ItemStack> validator, @Nullable IContentsListener listener, int x, int y
   ) {
      super(
         (stack, automationType) -> automationType == AutomationType.MANUAL || canExtract.test(stack),
         (stack, automationType) -> canInsert.test(stack),
         validator,
         listener,
         x,
         y
      );
   }

   public int burn() {
      if (this.isEmpty()) {
         return 0;
      } else {
         int burnTime = ForgeHooks.getBurnTime(this.current, null) / 2;
         if (burnTime > 0) {
            if (this.current.hasCraftingRemainingItem()) {
               if (this.current.m_41613_() > 1) {
                  return 0;
               }

               this.setStack(this.current.getCraftingRemainingItem());
            } else {
               MekanismUtils.logMismatchedStackSize(this.shrinkStack(1, Action.EXECUTE), 1L);
            }
         }

         return burnTime;
      }
   }
}
