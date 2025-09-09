package mekanism.common.capabilities.holder;

import java.util.ArrayList;
import java.util.Collections;
import java.util.EnumMap;
import java.util.List;
import java.util.Map;
import java.util.function.Supplier;
import mekanism.api.RelativeSide;
import net.minecraft.core.Direction;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class BasicHolder<TYPE> implements IHolder {
   private final Map<RelativeSide, List<TYPE>> directionalSlots = new EnumMap<>(RelativeSide.class);
   private final List<TYPE> inventorySlots = new ArrayList<>();
   protected final Supplier<Direction> facingSupplier;

   protected BasicHolder(Supplier<Direction> facingSupplier) {
      this.facingSupplier = facingSupplier;
   }

   protected void addSlotInternal(@NotNull TYPE slot, RelativeSide... sides) {
      this.inventorySlots.add(slot);

      for (RelativeSide side : sides) {
         this.directionalSlots.computeIfAbsent(side, k -> new ArrayList<>()).add(slot);
      }
   }

   @NotNull
   public List<TYPE> getSlots(@Nullable Direction side) {
      if (side != null && !this.directionalSlots.isEmpty()) {
         List<TYPE> slots = this.directionalSlots.get(RelativeSide.fromDirections(this.facingSupplier.get(), side));
         return slots == null ? Collections.emptyList() : slots;
      } else {
         return this.inventorySlots;
      }
   }
}
