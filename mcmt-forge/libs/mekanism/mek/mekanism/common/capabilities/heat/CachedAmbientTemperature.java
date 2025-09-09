package mekanism.common.capabilities.heat;

import java.util.Arrays;
import java.util.function.DoubleSupplier;
import java.util.function.Supplier;
import mekanism.api.heat.HeatAPI;
import mekanism.common.util.EnumUtils;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.level.Level;
import org.jetbrains.annotations.Nullable;

public class CachedAmbientTemperature implements DoubleSupplier {
   private final double[] ambientTemperature = new double[EnumUtils.DIRECTIONS.length + 1];
   private final Supplier<Level> worldSupplier;
   private final Supplier<BlockPos> positionSupplier;

   public CachedAmbientTemperature(Supplier<Level> worldSupplier, Supplier<BlockPos> positionSupplier) {
      this.worldSupplier = worldSupplier;
      this.positionSupplier = positionSupplier;
      Arrays.fill(this.ambientTemperature, -1.0);
   }

   @Override
   public double getAsDouble() {
      return this.getTemperature(null);
   }

   public double getTemperature(@Nullable Direction side) {
      int index = side == null ? EnumUtils.DIRECTIONS.length : side.ordinal();
      double biomeAmbientTemp = this.ambientTemperature[index];
      if (biomeAmbientTemp == -1.0) {
         Level world = this.worldSupplier.get();
         if (world == null) {
            return 300.0;
         } else {
            BlockPos pos = this.positionSupplier.get();
            if (side != null) {
               pos = pos.m_121945_(side);
            }

            return this.ambientTemperature[index] = HeatAPI.getAmbientTemp(world, pos);
         }
      } else {
         return biomeAmbientTemp;
      }
   }
}
