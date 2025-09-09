package mekanism.common.world;

import java.util.BitSet;
import java.util.function.Function;
import net.minecraft.core.BlockPos;
import net.minecraft.core.SectionPos;
import net.minecraft.core.BlockPos.MutableBlockPos;
import net.minecraft.util.Mth;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.WorldGenLevel;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.chunk.BulkSectionAccess;
import net.minecraft.world.level.chunk.LevelChunkSection;
import net.minecraft.world.level.levelgen.Heightmap.Types;
import net.minecraft.world.level.levelgen.feature.Feature;
import net.minecraft.world.level.levelgen.feature.FeaturePlaceContext;
import net.minecraft.world.level.levelgen.feature.configurations.OreConfiguration.TargetBlockState;
import org.jetbrains.annotations.NotNull;

public class ResizableOreFeature extends Feature<ResizableOreFeatureConfig> {
   public ResizableOreFeature() {
      super(ResizableOreFeatureConfig.CODEC);
   }

   protected Types getHeightmapType() {
      return Types.OCEAN_FLOOR_WG;
   }

   public boolean m_142674_(@NotNull FeaturePlaceContext<ResizableOreFeatureConfig> context) {
      RandomSource random = context.m_225041_();
      BlockPos pos = context.m_159777_();
      WorldGenLevel world = context.m_159774_();
      ResizableOreFeatureConfig config = (ResizableOreFeatureConfig)context.m_159778_();
      float angle = random.m_188501_() * (float) Math.PI;
      float adjustedSize = config.size().getAsInt() / 8.0F;
      int i = Mth.m_14167_((adjustedSize + 1.0F) / 2.0F);
      double sin = Math.sin(angle) * adjustedSize;
      double cos = Math.cos(angle) * adjustedSize;
      double xMin = pos.m_123341_() + sin;
      double xMax = pos.m_123341_() - sin;
      double zMin = pos.m_123343_() + cos;
      double zMax = pos.m_123343_() - cos;
      double yMin = pos.m_123342_() + random.m_188503_(3) - 2;
      double yMax = pos.m_123342_() + random.m_188503_(3) - 2;
      int minXStart = pos.m_123341_() - Mth.m_14167_(adjustedSize) - i;
      int minYStart = pos.m_123342_() - 2 - i;
      int minZStart = pos.m_123343_() - Mth.m_14167_(adjustedSize) - i;
      int width = 2 * (Mth.m_14167_(adjustedSize) + i);
      int height = 2 * (2 + i);

      for (int x = minXStart; x <= minXStart + width; x++) {
         for (int z = minZStart; z <= minZStart + width; z++) {
            if (minYStart <= world.m_6924_(this.getHeightmapType(), x, z)) {
               return this.doPlace(world, random, config, xMin, xMax, zMin, zMax, yMin, yMax, minXStart, minYStart, minZStart, width, height);
            }
         }
      }

      return false;
   }

   protected boolean doPlace(
      WorldGenLevel world,
      RandomSource random,
      ResizableOreFeatureConfig config,
      double xMin,
      double xMax,
      double zMin,
      double zMax,
      double yMin,
      double yMax,
      int minXStart,
      int minYStart,
      int minZStart,
      int width,
      int height
   ) {
      BitSet bitset = new BitSet(width * height * width);
      MutableBlockPos mutablePos = new MutableBlockPos();
      int size = config.size().getAsInt();
      double[] adouble = new double[size * 4];

      for (int k = 0; k < size; k++) {
         float f = (float)k / size;
         int k4 = k * 4;
         adouble[k4] = Mth.m_14139_(f, xMin, xMax);
         adouble[k4 + 1] = Mth.m_14139_(f, yMin, yMax);
         adouble[k4 + 2] = Mth.m_14139_(f, zMin, zMax);
         double d3 = random.m_188500_() * size / 16.0;
         adouble[k4 + 3] = ((Mth.m_14031_((float) Math.PI * f) + 1.0F) * d3 + 1.0) / 2.0;
      }

      for (int i = 0; i < size - 1; i++) {
         int i4 = i * 4;
         if (adouble[i4 + 3] > 0.0) {
            for (int j = i + 1; j < size; j++) {
               int j4 = j * 4;
               if (adouble[j4 + 3] > 0.0) {
                  double d1 = adouble[i4] - adouble[j4];
                  double d2 = adouble[i4 + 1] - adouble[j4 + 1];
                  double d3 = adouble[i4 + 2] - adouble[j4 + 2];
                  double d4 = adouble[i4 + 3] - adouble[j4 + 3];
                  if (d4 * d4 > d1 * d1 + d2 * d2 + d3 * d3) {
                     if (d4 > 0.0) {
                        adouble[j4 + 3] = -1.0;
                     } else {
                        adouble[i4 + 3] = -1.0;
                     }
                  }
               }
            }
         }
      }

      int ix = 0;
      BulkSectionAccess bulkSectionAccess = new BulkSectionAccess(world);

      try {
         float discardChanceOnAirExposure = config.discardChanceOnAirExposure().getAsFloat();

         for (int jx = 0; jx < size; jx++) {
            int j4 = jx * 4;
            double d1 = adouble[j4 + 3];
            if (d1 >= 0.0) {
               double d2 = adouble[j4];
               double d3 = adouble[j4 + 1];
               double d4 = adouble[j4 + 2];
               int xStart = Math.max(Mth.m_14107_(d2 - d1), minXStart);
               int yStart = Math.max(Mth.m_14107_(d3 - d1), minYStart);
               int zStart = Math.max(Mth.m_14107_(d4 - d1), minZStart);
               int xEnd = Math.max(Mth.m_14107_(d2 + d1), xStart);
               int yEnd = Math.max(Mth.m_14107_(d3 + d1), yStart);
               int zEnd = Math.max(Mth.m_14107_(d4 + d1), zStart);

               for (int x = xStart; x <= xEnd; x++) {
                  double d5 = (x + 0.5 - d2) / d1;
                  double d5_squared = d5 * d5;
                  if (d5_squared < 1.0) {
                     for (int y = yStart; y <= yEnd; y++) {
                        double d6 = (y + 0.5 - d3) / d1;
                        double d6_squared = d6 * d6;
                        if (d5_squared + d6_squared < 1.0) {
                           for (int z = zStart; z <= zEnd; z++) {
                              double d7 = (z + 0.5 - d4) / d1;
                              if (d5_squared + d6_squared + d7 * d7 < 1.0 && !world.m_151562_(y)) {
                                 int l2 = x - minXStart + (y - minYStart) * width + (z - minZStart) * width * height;
                                 if (!bitset.get(l2)) {
                                    bitset.set(l2);
                                    mutablePos.m_122178_(x, y, z);
                                    if (world.m_180807_(mutablePos)) {
                                       LevelChunkSection section = bulkSectionAccess.m_156104_(mutablePos);
                                       if (section != null) {
                                          int sectionX = SectionPos.m_123207_(x);
                                          int sectionY = SectionPos.m_123207_(y);
                                          int sectionZ = SectionPos.m_123207_(z);
                                          BlockState state = section.m_62982_(sectionX, sectionY, sectionZ);

                                          for (TargetBlockState targetState : config.targetStates()) {
                                             if (canPlaceOre(state, bulkSectionAccess::m_156110_, random, discardChanceOnAirExposure, targetState, mutablePos)) {
                                                section.m_62991_(sectionX, sectionY, sectionZ, targetState.f_161033_, false);
                                                ix++;
                                                break;
                                             }
                                          }
                                       }
                                    }
                                 }
                              }
                           }
                        }
                     }
                  }
               }
            }
         }
      } catch (Throwable var66) {
         try {
            bulkSectionAccess.close();
         } catch (Throwable var65) {
            var66.addSuppressed(var65);
         }

         throw var66;
      }

      bulkSectionAccess.close();
      return ix > 0;
   }

   private static boolean canPlaceOre(
      BlockState state,
      Function<BlockPos, BlockState> adjacentStateAccessor,
      RandomSource random,
      float discardChanceOnAirExposure,
      TargetBlockState targetState,
      MutableBlockPos mutablePos
   ) {
      if (!targetState.f_161032_.m_213865_(state, random)) {
         return false;
      } else {
         return shouldSkipAirCheck(random, discardChanceOnAirExposure) ? true : !m_159750_(adjacentStateAccessor, mutablePos);
      }
   }

   private static boolean shouldSkipAirCheck(RandomSource random, float discardChanceOnAirExposure) {
      if (discardChanceOnAirExposure <= 0.0F) {
         return true;
      } else {
         return discardChanceOnAirExposure >= 1.0F ? false : random.m_188501_() >= discardChanceOnAirExposure;
      }
   }
}
