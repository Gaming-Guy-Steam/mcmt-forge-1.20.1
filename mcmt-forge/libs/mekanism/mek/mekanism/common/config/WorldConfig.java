package mekanism.common.config;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableList.Builder;
import java.util.EnumMap;
import java.util.List;
import java.util.Map;
import java.util.function.BooleanSupplier;
import java.util.function.IntSupplier;
import mekanism.api.functions.FloatSupplier;
import mekanism.common.config.value.CachedBooleanValue;
import mekanism.common.config.value.CachedFloatValue;
import mekanism.common.config.value.CachedIntValue;
import mekanism.common.resource.ore.BaseOreConfig;
import mekanism.common.resource.ore.OreType;
import mekanism.common.util.EnumUtils;
import mekanism.common.world.height.ConfigurableHeightRange;
import net.minecraft.world.level.dimension.DimensionType;
import net.minecraftforge.common.ForgeConfigSpec;
import net.minecraftforge.fml.config.ModConfig.Type;

public class WorldConfig extends BaseMekanismConfig {
   private final ForgeConfigSpec configSpec;
   public final CachedBooleanValue enableRegeneration;
   public final CachedIntValue userGenVersion;
   private final Map<OreType, WorldConfig.OreConfig> ores = new EnumMap<>(OreType.class);
   public final WorldConfig.SaltConfig salt;

   WorldConfig() {
      net.minecraftforge.common.ForgeConfigSpec.Builder builder = new net.minecraftforge.common.ForgeConfigSpec.Builder();
      builder.comment("World generation settings for Mekanism. This config is synced from server to client").push("world_generation");
      this.enableRegeneration = CachedBooleanValue.wrap(
         this, builder.comment("Allows chunks to retrogen Mekanism ore blocks.").define("enableRegeneration", false)
      );
      this.userGenVersion = CachedIntValue.wrap(
         this,
         builder.comment("Change this value to cause Mekanism to regen its ore in all loaded chunks.")
            .defineInRange("userWorldGenVersion", 0, 0, Integer.MAX_VALUE)
      );

      for (OreType ore : EnumUtils.ORE_TYPES) {
         this.ores.put(ore, new WorldConfig.OreConfig(this, builder, ore));
      }

      this.salt = new WorldConfig.SaltConfig(this, builder, 2, 2, 3, 1);
      builder.pop();
      this.configSpec = builder.build();
   }

   @Override
   public String getFileName() {
      return "world";
   }

   @Override
   public ForgeConfigSpec getConfigSpec() {
      return this.configSpec;
   }

   @Override
   public Type getConfigType() {
      return Type.SERVER;
   }

   @Override
   public boolean addToContainer() {
      return false;
   }

   public WorldConfig.OreVeinConfig getVeinConfig(OreType.OreVeinType oreVeinType) {
      return this.ores.get(oreVeinType.type()).veinConfigs.get(oreVeinType.index());
   }

   private static class OreConfig {
      private final CachedBooleanValue shouldGenerate;
      private final List<WorldConfig.OreVeinConfig> veinConfigs;

      private OreConfig(IMekanismConfig config, net.minecraftforge.common.ForgeConfigSpec.Builder builder, OreType oreType) {
         String ore = oreType.getResource().getRegistrySuffix();
         builder.comment("Generation Settings for " + ore + " ore.").push(ore);
         this.shouldGenerate = CachedBooleanValue.wrap(
            config, builder.comment("Determines if " + ore + " ore should be added to world generation.").define("shouldGenerate", true)
         );
         Builder<WorldConfig.OreVeinConfig> veinBuilder = ImmutableList.builder();

         for (BaseOreConfig baseConfig : oreType.getBaseConfigs()) {
            String veinType = baseConfig.name() + " " + ore + " vein";
            builder.comment(veinType + " Generation Settings.").push(baseConfig.name());
            CachedBooleanValue shouldVeinTypeGenerate = CachedBooleanValue.wrap(
               config,
               builder.comment("Determines if " + veinType + "s should be added to world generation. Note: Requires generating " + ore + " ore to be enabled.")
                  .define("shouldGenerate", true)
            );
            veinBuilder.add(
               new WorldConfig.OreVeinConfig(
                  () -> this.shouldGenerate.get() && shouldVeinTypeGenerate.get(),
                  CachedIntValue.wrap(
                     config, builder.comment("Chance that " + veinType + "s generates in a chunk.").defineInRange("perChunk", baseConfig.perChunk(), 1, 256)
                  ),
                  CachedIntValue.wrap(
                     config, builder.comment("Maximum number of blocks in a " + veinType + ".").defineInRange("maxVeinSize", baseConfig.maxVeinSize(), 1, 64)
                  ),
                  CachedFloatValue.wrap(
                     config,
                     builder.comment("Chance that blocks that are directly exposed to air in a " + veinType + " are not placed.")
                        .defineInRange("discardChanceOnAirExposure", baseConfig.discardChanceOnAirExposure(), 0.0, 1.0)
                  ),
                  ConfigurableHeightRange.create(config, builder, veinType, baseConfig)
               )
            );
            builder.pop();
         }

         this.veinConfigs = veinBuilder.build();
         builder.pop();
      }
   }

   public record OreVeinConfig(
      BooleanSupplier shouldGenerate, CachedIntValue perChunk, IntSupplier maxVeinSize, FloatSupplier discardChanceOnAirExposure, ConfigurableHeightRange range
   ) {
   }

   public static class SaltConfig {
      public final CachedBooleanValue shouldGenerate;
      public final CachedIntValue perChunk;
      public final CachedIntValue minRadius;
      public final CachedIntValue maxRadius;
      public final CachedIntValue halfHeight;

      private SaltConfig(IMekanismConfig config, net.minecraftforge.common.ForgeConfigSpec.Builder builder, int perChunk, int baseRadius, int spread, int ySize) {
         builder.comment("Generation Settings for salt.").push("salt");
         this.shouldGenerate = CachedBooleanValue.wrap(
            config, builder.comment("Determines if salt should be added to world generation.").define("shouldGenerate", true)
         );
         this.perChunk = CachedIntValue.wrap(config, builder.comment("Chance that salt generates in a chunk.").defineInRange("perChunk", perChunk, 1, 256));
         this.minRadius = CachedIntValue.wrap(config, builder.comment("Base radius of a vein of salt.").defineInRange("minRadius", baseRadius, 1, 4));
         this.maxRadius = CachedIntValue.wrap(
            config,
            builder.comment("Extended variability (spread) for the radius in a vein of salt.")
               .define("maxRadius", spread, o -> o instanceof Integer value && value >= 1 && value <= 4 ? value >= this.minRadius.get() : false)
         );
         this.halfHeight = CachedIntValue.wrap(
            config,
            builder.comment("Number of blocks to extend up and down when placing a vein of salt.")
               .defineInRange("halfHeight", ySize, 0, (DimensionType.f_156652_ - DimensionType.f_156653_ - 1) / 2)
         );
         builder.pop();
      }
   }
}
