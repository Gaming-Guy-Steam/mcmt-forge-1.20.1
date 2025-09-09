package mekanism.common.world;

import com.mojang.serialization.Codec;
import net.minecraft.world.level.levelgen.feature.Feature;
import net.minecraft.world.level.levelgen.feature.FeaturePlaceContext;
import net.minecraft.world.level.levelgen.feature.configurations.DiskConfiguration;

public class ResizableDiskReplaceFeature extends Feature<ResizableDiskConfig> {
   public ResizableDiskReplaceFeature(Codec<ResizableDiskConfig> codec) {
      super(codec);
   }

   public boolean m_142674_(FeaturePlaceContext<ResizableDiskConfig> context) {
      FeaturePlaceContext<DiskConfiguration> vanillaContext = new FeaturePlaceContext(
         context.m_190935_(),
         context.m_159774_(),
         context.m_159775_(),
         context.m_225041_(),
         context.m_159777_(),
         ((ResizableDiskConfig)context.m_159778_()).asVanillaConfig()
      );
      return Feature.f_65781_.m_142674_(vanillaContext);
   }
}
