package mekanism.common.inventory.slot.chemical;

import java.util.Objects;
import java.util.function.Predicate;
import java.util.function.Supplier;
import mekanism.api.IContentsListener;
import mekanism.api.annotations.NothingNullByDefault;
import mekanism.api.chemical.IChemicalHandler;
import mekanism.api.chemical.pigment.IPigmentHandler;
import mekanism.api.chemical.pigment.IPigmentTank;
import mekanism.api.chemical.pigment.Pigment;
import mekanism.api.chemical.pigment.PigmentStack;
import mekanism.common.capabilities.Capabilities;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import org.jetbrains.annotations.Nullable;

@NothingNullByDefault
public class PigmentInventorySlot extends ChemicalInventorySlot<Pigment, PigmentStack> {
   @Nullable
   public static IPigmentHandler getCapability(ItemStack stack) {
      return getCapability(stack, Capabilities.PIGMENT_HANDLER);
   }

   public static PigmentInventorySlot fill(IPigmentTank pigmentTank, @Nullable IContentsListener listener, int x, int y) {
      Objects.requireNonNull(pigmentTank, "Pigment tank cannot be null");
      return new PigmentInventorySlot(
         pigmentTank,
         getFillExtractPredicate(pigmentTank, PigmentInventorySlot::getCapability),
         stack -> fillInsertCheck(pigmentTank, getCapability(stack)),
         stack -> stack.getCapability(Capabilities.PIGMENT_HANDLER).isPresent(),
         listener,
         x,
         y
      );
   }

   public static PigmentInventorySlot drain(IPigmentTank pigmentTank, @Nullable IContentsListener listener, int x, int y) {
      Objects.requireNonNull(pigmentTank, "Pigment tank cannot be null");
      Predicate<ItemStack> insertPredicate = getDrainInsertPredicate(pigmentTank, PigmentInventorySlot::getCapability);
      return new PigmentInventorySlot(
         pigmentTank, insertPredicate.negate(), insertPredicate, stack -> stack.getCapability(Capabilities.PIGMENT_HANDLER).isPresent(), listener, x, y
      );
   }

   private PigmentInventorySlot(
      IPigmentTank pigmentTank,
      Predicate<ItemStack> canExtract,
      Predicate<ItemStack> canInsert,
      Predicate<ItemStack> validator,
      @Nullable IContentsListener listener,
      int x,
      int y
   ) {
      this(pigmentTank, () -> null, canExtract, canInsert, validator, listener, x, y);
   }

   private PigmentInventorySlot(
      IPigmentTank pigmentTank,
      Supplier<Level> worldSupplier,
      Predicate<ItemStack> canExtract,
      Predicate<ItemStack> canInsert,
      Predicate<ItemStack> validator,
      @Nullable IContentsListener listener,
      int x,
      int y
   ) {
      super(pigmentTank, worldSupplier, canExtract, canInsert, validator, listener, x, y);
   }

   @Nullable
   @Override
   protected IChemicalHandler<Pigment, PigmentStack> getCapability() {
      return getCapability(this.current);
   }
}
