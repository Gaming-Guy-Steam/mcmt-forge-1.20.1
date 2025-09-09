package mekanism.common.world;

import net.minecraft.world.level.levelgen.Heightmap.Types;

public class OreRetrogenFeature extends ResizableOreFeature {
   @Override
   protected Types getHeightmapType() {
      return Types.OCEAN_FLOOR;
   }
}
