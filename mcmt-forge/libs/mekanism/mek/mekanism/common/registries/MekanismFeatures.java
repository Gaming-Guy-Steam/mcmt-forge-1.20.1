package mekanism.common.registries;

import mekanism.common.registration.impl.FeatureDeferredRegister;
import mekanism.common.registration.impl.FeatureRegistryObject;
import mekanism.common.world.OreRetrogenFeature;
import mekanism.common.world.ResizableDiskConfig;
import mekanism.common.world.ResizableDiskReplaceFeature;
import mekanism.common.world.ResizableOreFeature;
import mekanism.common.world.ResizableOreFeatureConfig;

public class MekanismFeatures {
   public static final FeatureDeferredRegister FEATURES = new FeatureDeferredRegister("mekanism");
   public static final FeatureRegistryObject<ResizableDiskConfig, ResizableDiskReplaceFeature> DISK = FEATURES.register(
      "disk", () -> new ResizableDiskReplaceFeature(ResizableDiskConfig.CODEC)
   );
   public static final FeatureRegistryObject<ResizableOreFeatureConfig, ResizableOreFeature> ORE = FEATURES.register("ore", ResizableOreFeature::new);
   public static final FeatureRegistryObject<ResizableOreFeatureConfig, OreRetrogenFeature> ORE_RETROGEN = FEATURES.register(
      "ore_retrogen", OreRetrogenFeature::new
   );

   private MekanismFeatures() {
   }
}
