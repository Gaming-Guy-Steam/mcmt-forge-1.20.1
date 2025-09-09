package mekanism.common.world;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.function.ToIntFunction;
import mekanism.common.Mekanism;
import mekanism.common.resource.ore.OreType;
import mekanism.common.util.EnumUtils;
import net.minecraft.SharedConstants;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Holder;
import net.minecraft.core.Registry;
import net.minecraft.core.RegistryAccess;
import net.minecraft.core.SectionPos;
import net.minecraft.core.Holder.Reference;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.WorldGenLevel;
import net.minecraft.world.level.biome.FeatureSorter.StepFeatureData;
import net.minecraft.world.level.chunk.ChunkGenerator;
import net.minecraft.world.level.levelgen.RandomSupport;
import net.minecraft.world.level.levelgen.WorldgenRandom;
import net.minecraft.world.level.levelgen.XoroshiroRandomSource;
import net.minecraft.world.level.levelgen.GenerationStep.Decoration;
import net.minecraft.world.level.levelgen.placement.PlacedFeature;
import net.minecraft.world.level.levelgen.placement.PlacementContext;
import org.jetbrains.annotations.Nullable;

public class GenHandler {
   @Nullable
   private static List<GenHandler.MekFeature> cachedFeatures;

   private GenHandler() {
   }

   public static void reset() {
      cachedFeatures = null;
   }

   public static boolean generate(ServerLevel world, ChunkPos chunkPos) {
      boolean generated = false;
      if (!SharedConstants.m_183707_(chunkPos)) {
         SectionPos sectionPos = SectionPos.m_123196_(chunkPos, world.m_151560_());
         BlockPos blockPos = sectionPos.m_123249_();
         ChunkGenerator chunkGenerator = world.m_7726_().m_8481_();
         WorldgenRandom random = new WorldgenRandom(new XoroshiroRandomSource(RandomSupport.m_224599_()));
         long decorationSeed = random.m_64690_(world.m_7328_(), blockPos.m_123341_(), blockPos.m_123343_());
         int decorationStep = Decoration.UNDERGROUND_ORES.ordinal() - 1;
         List<StepFeatureData> list = (List<StepFeatureData>)chunkGenerator.f_223020_.get();
         ToIntFunction<PlacedFeature> featureIndex;
         if (decorationStep < list.size()) {
            featureIndex = list.get(decorationStep).f_220625_();
         } else {
            featureIndex = featurex -> -1;
         }

         for (GenHandler.MekFeature feature : getMekanismFeatures(world.m_9598_())) {
            generated |= place(world, chunkGenerator, blockPos, random, decorationSeed, decorationStep, featureIndex, feature);
         }

         world.m_143497_(null);
      }

      return generated;
   }

   private static boolean place(
      WorldGenLevel world,
      ChunkGenerator chunkGenerator,
      BlockPos blockPos,
      WorldgenRandom random,
      long decorationSeed,
      int decorationStep,
      ToIntFunction<PlacedFeature> featureIndex,
      GenHandler.MekFeature feature
   ) {
      PlacedFeature baseFeature = (PlacedFeature)feature.feature().get();
      random.m_190064_(decorationSeed, featureIndex.applyAsInt(baseFeature), decorationStep);
      world.m_143497_(feature::retrogenKey);
      return ((PlacedFeature)feature.retrogen().get()).m_226368_(new PlacementContext(world, chunkGenerator, Optional.of(baseFeature)), random, blockPos);
   }

   private static List<GenHandler.MekFeature> getMekanismFeatures(RegistryAccess registryAccess) {
      if (cachedFeatures != null) {
         return cachedFeatures;
      } else {
         cachedFeatures = new ArrayList<>();
         Registry<PlacedFeature> placedFeatures = registryAccess.m_175515_(Registries.f_256988_);

         for (OreType type : EnumUtils.ORE_TYPES) {
            int vein = 0;

            for (int features = type.getBaseConfigs().size(); vein < features; vein++) {
               OreType.OreVeinType oreVeinType = new OreType.OreVeinType(type, vein);
               GenHandler.MekFeature mekFeature = GenHandler.MekFeature.create(placedFeatures, Mekanism.rl(oreVeinType.name()));
               if (mekFeature != null) {
                  cachedFeatures.add(mekFeature);
               }
            }
         }

         GenHandler.MekFeature saltFeature = GenHandler.MekFeature.create(placedFeatures, Mekanism.rl("salt"));
         if (saltFeature != null) {
            cachedFeatures.add(saltFeature);
         }

         return cachedFeatures;
      }
   }

   private record MekFeature(Holder<PlacedFeature> feature, Holder<PlacedFeature> retrogen, String retrogenKey) {
      @Nullable
      public static GenHandler.MekFeature create(Registry<PlacedFeature> placedFeatures, ResourceLocation name) {
         Optional<Reference<PlacedFeature>> placedFeature = placedFeatures.m_203636_(ResourceKey.m_135785_(Registries.f_256988_, name));
         if (placedFeature.isEmpty()) {
            Mekanism.logger.error("Failed to retrieve placed feature ({}).", name);
            return null;
         } else {
            ResourceLocation retrogenName = name.m_266382_("_retrogen");
            ResourceKey<PlacedFeature> retrogenKey = ResourceKey.m_135785_(Registries.f_256988_, retrogenName);
            Optional<Reference<PlacedFeature>> retrogenFeature = placedFeatures.m_203636_(retrogenKey);
            if (retrogenFeature.isEmpty()) {
               Mekanism.logger.error("Failed to retrieve retrogen placed feature ({}).", retrogenName);
               return null;
            } else {
               return new GenHandler.MekFeature(
                  (Holder<PlacedFeature>)placedFeature.get(), (Holder<PlacedFeature>)retrogenFeature.get(), retrogenKey.toString()
               );
            }
         }
      }
   }
}
