package mekanism.common.capabilities.holder.heat;

import java.util.function.Supplier;
import mekanism.api.RelativeSide;
import mekanism.api.heat.IHeatCapacitor;
import mekanism.common.tile.component.TileComponentConfig;
import net.minecraft.core.Direction;
import org.jetbrains.annotations.NotNull;

public class HeatCapacitorHelper {
   private final IHeatCapacitorHolder slotHolder;
   private boolean built;

   private HeatCapacitorHelper(IHeatCapacitorHolder slotHolder) {
      this.slotHolder = slotHolder;
   }

   public static HeatCapacitorHelper forSide(Supplier<Direction> facingSupplier) {
      return new HeatCapacitorHelper(new HeatCapacitorHolder(facingSupplier));
   }

   public static HeatCapacitorHelper forSideWithConfig(Supplier<Direction> facingSupplier, Supplier<TileComponentConfig> configSupplier) {
      return new HeatCapacitorHelper(new ConfigHeatCapacitorHolder(facingSupplier, configSupplier));
   }

   public <CAPACITOR extends IHeatCapacitor> CAPACITOR addCapacitor(@NotNull CAPACITOR capacitor) {
      if (this.built) {
         throw new IllegalStateException("Builder has already built.");
      } else {
         if (this.slotHolder instanceof HeatCapacitorHolder slotHolder) {
            slotHolder.addCapacitor(capacitor);
         } else {
            if (!(this.slotHolder instanceof ConfigHeatCapacitorHolder slotHolder)) {
               throw new IllegalArgumentException("Holder does not know how to add capacitors");
            }

            slotHolder.addCapacitor(capacitor);
         }

         return capacitor;
      }
   }

   public <CAPACITOR extends IHeatCapacitor> CAPACITOR addCapacitor(@NotNull CAPACITOR capacitor, RelativeSide... sides) {
      if (this.built) {
         throw new IllegalStateException("Builder has already built.");
      } else if (this.slotHolder instanceof HeatCapacitorHolder slotHolder) {
         slotHolder.addCapacitor(capacitor, sides);
         return capacitor;
      } else {
         throw new IllegalArgumentException("Holder does not know how to add capacitors on specific sides");
      }
   }

   public IHeatCapacitorHolder build() {
      this.built = true;
      return this.slotHolder;
   }
}
