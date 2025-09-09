package mekanism.common.capabilities;

import java.util.List;
import java.util.function.Function;
import mekanism.api.IContentsListener;
import mekanism.api.annotations.NothingNullByDefault;
import net.minecraft.core.Direction;
import org.jetbrains.annotations.Nullable;

@NothingNullByDefault
public abstract class DynamicHandler<TANK> implements IContentsListener {
   protected final Function<Direction, List<TANK>> containerSupplier;
   protected final DynamicHandler.InteractPredicate canExtract;
   protected final DynamicHandler.InteractPredicate canInsert;
   @Nullable
   private final IContentsListener listener;

   protected DynamicHandler(
      Function<Direction, List<TANK>> containerSupplier,
      DynamicHandler.InteractPredicate canExtract,
      DynamicHandler.InteractPredicate canInsert,
      @Nullable IContentsListener listener
   ) {
      this.containerSupplier = containerSupplier;
      this.canExtract = canExtract;
      this.canInsert = canInsert;
      this.listener = listener;
   }

   @Override
   public void onContentsChanged() {
      if (this.listener != null) {
         this.listener.onContentsChanged();
      }
   }

   @FunctionalInterface
   public interface InteractPredicate {
      DynamicHandler.InteractPredicate ALWAYS_TRUE = (tank, side) -> true;

      boolean test(int tank, @Nullable Direction side);
   }
}
