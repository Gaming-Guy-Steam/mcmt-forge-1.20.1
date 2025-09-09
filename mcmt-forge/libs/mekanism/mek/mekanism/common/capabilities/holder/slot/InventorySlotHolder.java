package mekanism.common.capabilities.holder.slot;

import java.util.List;
import java.util.function.Predicate;
import java.util.function.Supplier;
import mekanism.api.RelativeSide;
import mekanism.api.inventory.IInventorySlot;
import mekanism.common.capabilities.holder.BasicHolder;
import net.minecraft.core.Direction;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class InventorySlotHolder extends BasicHolder<IInventorySlot> implements IInventorySlotHolder {
   @Nullable
   private final Predicate<RelativeSide> insertPredicate;
   @Nullable
   private final Predicate<RelativeSide> extractPredicate;

   InventorySlotHolder(
      Supplier<Direction> facingSupplier, @Nullable Predicate<RelativeSide> insertPredicate, @Nullable Predicate<RelativeSide> extractPredicate
   ) {
      super(facingSupplier);
      this.insertPredicate = insertPredicate;
      this.extractPredicate = extractPredicate;
   }

   void addSlot(@NotNull IInventorySlot slot, RelativeSide... sides) {
      this.addSlotInternal(slot, sides);
   }

   @NotNull
   @Override
   public List<IInventorySlot> getInventorySlots(@Nullable Direction direction) {
      return this.getSlots(direction);
   }

   @Override
   public boolean canInsert(@Nullable Direction direction) {
      return direction != null
         && (this.insertPredicate == null || this.insertPredicate.test(RelativeSide.fromDirections(this.facingSupplier.get(), direction)));
   }

   @Override
   public boolean canExtract(@Nullable Direction direction) {
      return direction != null
         && (this.extractPredicate == null || this.extractPredicate.test(RelativeSide.fromDirections(this.facingSupplier.get(), direction)));
   }
}
