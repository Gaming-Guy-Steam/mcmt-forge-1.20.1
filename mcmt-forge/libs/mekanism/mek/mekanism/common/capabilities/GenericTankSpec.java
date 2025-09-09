package mekanism.common.capabilities;

import java.util.function.BiPredicate;
import java.util.function.Predicate;
import mekanism.api.AutomationType;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.common.util.TriPredicate;

public abstract class GenericTankSpec<TYPE> {
   public final Predicate<TYPE> isValid;
   public final BiPredicate<TYPE, AutomationType> canExtract;
   public final TriPredicate<TYPE, AutomationType, ItemStack> canInsert;
   private final Predicate<ItemStack> supportsStack;

   protected GenericTankSpec(
      BiPredicate<TYPE, AutomationType> canExtract,
      TriPredicate<TYPE, AutomationType, ItemStack> canInsert,
      Predicate<TYPE> isValid,
      Predicate<ItemStack> supportsStack
   ) {
      this.isValid = isValid;
      this.canExtract = canExtract;
      this.canInsert = canInsert;
      this.supportsStack = supportsStack;
   }

   public boolean supportsStack(ItemStack stack) {
      return this.supportsStack.test(stack);
   }
}
