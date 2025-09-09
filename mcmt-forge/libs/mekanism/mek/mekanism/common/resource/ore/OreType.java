package mekanism.common.resource.ore;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import java.util.List;
import mekanism.common.resource.IResource;
import mekanism.common.resource.MiscResource;
import mekanism.common.resource.PrimaryResource;
import mekanism.common.world.height.HeightShape;
import net.minecraft.util.StringRepresentable;
import org.jetbrains.annotations.NotNull;

public enum OreType implements StringRepresentable {
   TIN(
      PrimaryResource.TIN,
      new BaseOreConfig("small", 14, 0.0F, 4, HeightShape.TRAPEZOID, OreAnchor.absolute(-20), OreAnchor.absolute(94)),
      new BaseOreConfig("large", 12, 0.0F, 9, HeightShape.TRAPEZOID, OreAnchor.absolute(-32), OreAnchor.absolute(72))
   ),
   OSMIUM(
      PrimaryResource.OSMIUM,
      new BaseOreConfig("upper", 65, 0.0F, 7, HeightShape.TRAPEZOID, OreAnchor.absolute(72), OreAnchor.belowTop(-24), 8),
      new BaseOreConfig("middle", 6, 0.0F, 9, HeightShape.TRAPEZOID, OreAnchor.absolute(-32), OreAnchor.absolute(56)),
      new BaseOreConfig("small", 8, 0.0F, 4, HeightShape.UNIFORM, OreAnchor.aboveBottom(0), OreAnchor.absolute(64))
   ),
   URANIUM(
      PrimaryResource.URANIUM,
      new BaseOreConfig("small", 4, 0.0F, 4, HeightShape.TRAPEZOID, OreAnchor.aboveBottom(0), OreAnchor.absolute(8)),
      new BaseOreConfig("buried", 7, 0.75F, 9, HeightShape.TRAPEZOID, OreAnchor.aboveBottom(-24), OreAnchor.aboveBottom(56), 16)
   ),
   FLUORITE(
      MiscResource.FLUORITE,
      1,
      4,
      new BaseOreConfig("normal", 5, 0.0F, 5, HeightShape.UNIFORM, OreAnchor.aboveBottom(0), OreAnchor.absolute(23)),
      new BaseOreConfig("buried", 3, 1.0F, 13, HeightShape.TRAPEZOID, OreAnchor.aboveBottom(0), OreAnchor.absolute(4))
   ),
   LEAD(PrimaryResource.LEAD, new BaseOreConfig("normal", 8, 0.25F, 9, HeightShape.TRAPEZOID, OreAnchor.aboveBottom(-24), OreAnchor.absolute(64)));

   public static Codec<OreType> CODEC = StringRepresentable.m_216439_(OreType::values);
   private final List<BaseOreConfig> baseConfigs;
   private final IResource resource;
   private final int minExp;
   private final int maxExp;

   private OreType(IResource resource, BaseOreConfig... configs) {
      this(resource, 0, configs);
   }

   private OreType(IResource resource, int exp, BaseOreConfig... configs) {
      this(resource, exp, exp, configs);
   }

   private OreType(IResource resource, int minExp, int maxExp, BaseOreConfig... configs) {
      this.resource = resource;
      this.minExp = minExp;
      this.maxExp = maxExp;
      this.baseConfigs = List.of(configs);
   }

   public IResource getResource() {
      return this.resource;
   }

   public List<BaseOreConfig> getBaseConfigs() {
      return this.baseConfigs;
   }

   public int getMinExp() {
      return this.minExp;
   }

   public int getMaxExp() {
      return this.maxExp;
   }

   public static OreType get(IResource resource) {
      for (OreType ore : values()) {
         if (resource == ore.resource) {
            return ore;
         }
      }

      return null;
   }

   @NotNull
   public String m_7912_() {
      return this.resource.getRegistrySuffix();
   }

   public record OreVeinType(OreType type, int index) {
      public static final Codec<OreType.OreVeinType> CODEC = RecordCodecBuilder.create(
         builder -> builder.group(OreType.CODEC.fieldOf("type").forGetter(config -> config.type), Codec.INT.fieldOf("index").forGetter(config -> config.index))
            .apply(builder, OreType.OreVeinType::new)
      );

      public OreVeinType(OreType type, int index) {
         if (index >= 0 && index < type.getBaseConfigs().size()) {
            this.type = type;
            this.index = index;
         } else {
            throw new IndexOutOfBoundsException("Vein Type index out of range: " + index);
         }
      }

      public String name() {
         return "ore_" + this.type.getResource().getRegistrySuffix() + "_" + this.type.getBaseConfigs().get(this.index).name();
      }
   }
}
