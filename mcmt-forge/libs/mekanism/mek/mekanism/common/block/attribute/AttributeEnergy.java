package mekanism.common.block.attribute;

import mekanism.api.math.FloatingLong;
import mekanism.api.math.FloatingLongSupplier;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class AttributeEnergy implements Attribute {
   private FloatingLongSupplier energyUsage = () -> FloatingLong.ZERO;
   private FloatingLongSupplier energyStorage = () -> this.energyUsage.get().multiply(400L);

   public AttributeEnergy(@Nullable FloatingLongSupplier energyUsage, @Nullable FloatingLongSupplier energyStorage) {
      if (energyUsage != null) {
         this.energyUsage = energyUsage;
      }

      if (energyStorage != null) {
         this.energyStorage = energyStorage;
      }
   }

   @NotNull
   public FloatingLong getUsage() {
      return this.energyUsage.get();
   }

   @NotNull
   public FloatingLong getConfigStorage() {
      return this.energyStorage.get();
   }

   @NotNull
   public FloatingLong getStorage() {
      return this.getConfigStorage().max(this.getUsage());
   }
}
