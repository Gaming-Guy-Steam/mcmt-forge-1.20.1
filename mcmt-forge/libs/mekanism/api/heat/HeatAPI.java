package mekanism.api.heat;

import net.minecraft.core.BlockPos;
import net.minecraft.util.Mth;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.biome.Biome;
import org.jetbrains.annotations.Nullable;

public class HeatAPI {
   public static final double AMBIENT_TEMP = 300.0;
   public static final double AIR_INVERSE_COEFFICIENT = 10000.0;
   public static final double DEFAULT_HEAT_CAPACITY = 1.0;
   public static final double DEFAULT_INVERSE_CONDUCTION = 1.0;
   public static final double DEFAULT_INVERSE_INSULATION = 0.0;
   public static final double EPSILON = 1.0E-6F;

   private HeatAPI() {
   }

   public static double getAmbientTemp(@Nullable LevelReader world, BlockPos pos) {
      return world == null ? 300.0 : getAmbientTemp(((Biome)world.m_204166_(pos).m_203334_()).m_47505_(pos));
   }

   public static double getAmbientTemp(double biomeTemp) {
      biomeTemp = Mth.m_14008_(biomeTemp, -5.0, 5.0);
      return 300.0 + 25.0 * (biomeTemp - 0.8);
   }

   public record HeatTransfer(double adjacentTransfer, double environmentTransfer) {
   }
}
