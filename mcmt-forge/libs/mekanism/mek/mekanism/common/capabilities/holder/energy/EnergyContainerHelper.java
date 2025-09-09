package mekanism.common.capabilities.holder.energy;

import java.util.function.Supplier;
import mekanism.api.RelativeSide;
import mekanism.api.energy.IEnergyContainer;
import mekanism.common.tile.component.TileComponentConfig;
import net.minecraft.core.Direction;
import org.jetbrains.annotations.NotNull;

public class EnergyContainerHelper {
   private final IEnergyContainerHolder slotHolder;
   private boolean built;

   private EnergyContainerHelper(IEnergyContainerHolder slotHolder) {
      this.slotHolder = slotHolder;
   }

   public static EnergyContainerHelper forSide(Supplier<Direction> facingSupplier) {
      return new EnergyContainerHelper(new EnergyContainerHolder(facingSupplier));
   }

   public static EnergyContainerHelper forSideWithConfig(Supplier<Direction> facingSupplier, Supplier<TileComponentConfig> configSupplier) {
      return new EnergyContainerHelper(new ConfigEnergyContainerHolder(facingSupplier, configSupplier));
   }

   public <CONTAINER extends IEnergyContainer> CONTAINER addContainer(@NotNull CONTAINER container) {
      if (this.built) {
         throw new IllegalStateException("Builder has already built.");
      } else {
         if (this.slotHolder instanceof EnergyContainerHolder slotHolder) {
            slotHolder.addContainer(container);
         } else {
            if (!(this.slotHolder instanceof ConfigEnergyContainerHolder slotHolder)) {
               throw new IllegalArgumentException("Holder does not know how to add containers");
            }

            slotHolder.addContainer(container);
         }

         return container;
      }
   }

   public <CONTAINER extends IEnergyContainer> CONTAINER addContainer(@NotNull CONTAINER container, RelativeSide... sides) {
      if (this.built) {
         throw new IllegalStateException("Builder has already built.");
      } else if (this.slotHolder instanceof EnergyContainerHolder slotHolder) {
         slotHolder.addContainer(container, sides);
         return container;
      } else {
         throw new IllegalArgumentException("Holder does not know how to add containers on specific sides");
      }
   }

   public IEnergyContainerHolder build() {
      this.built = true;
      return this.slotHolder;
   }
}
