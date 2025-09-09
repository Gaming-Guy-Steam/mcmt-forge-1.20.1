package mekanism.common.resource;

import java.util.function.Supplier;
import mekanism.common.resource.ore.OreType;
import mekanism.common.tags.MekanismTags;
import net.minecraft.tags.TagKey;
import net.minecraft.world.item.Item;
import net.minecraftforge.common.Tags.Items;
import org.jetbrains.annotations.Nullable;

public enum PrimaryResource implements IResource {
   IRON("iron", -5271945, Items.ORES_IRON),
   GOLD("gold", -864921, Items.ORES_GOLD),
   OSMIUM("osmium", -14779965, () -> MekanismTags.Items.ORES.get(OreType.OSMIUM), BlockResourceInfo.OSMIUM, BlockResourceInfo.RAW_OSMIUM),
   COPPER("copper", -5616871, Items.ORES_COPPER),
   TIN("tin", -3355431, () -> MekanismTags.Items.ORES.get(OreType.TIN), BlockResourceInfo.TIN, BlockResourceInfo.RAW_TIN),
   LEAD("lead", -12959670, () -> MekanismTags.Items.ORES.get(OreType.LEAD), BlockResourceInfo.LEAD, BlockResourceInfo.RAW_LEAD),
   URANIUM("uranium", -12163505, () -> MekanismTags.Items.ORES.get(OreType.URANIUM), BlockResourceInfo.URANIUM, BlockResourceInfo.RAW_URANIUM);

   private final String name;
   private final int tint;
   private final Supplier<TagKey<Item>> oreTag;
   private final boolean isVanilla;
   private final BlockResourceInfo resourceBlockInfo;
   private final BlockResourceInfo rawResourceBlockInfo;

   private PrimaryResource(String name, int tint, TagKey<Item> oreTag) {
      this(name, tint, () -> oreTag, true, null, null);
   }

   private PrimaryResource(String name, int tint, Supplier<TagKey<Item>> oreTag, BlockResourceInfo resourceBlockInfo, BlockResourceInfo rawResourceBlockInfo) {
      this(name, tint, oreTag, false, resourceBlockInfo, rawResourceBlockInfo);
   }

   private PrimaryResource(
      String name, int tint, Supplier<TagKey<Item>> oreTag, boolean isVanilla, BlockResourceInfo resourceBlockInfo, BlockResourceInfo rawResourceBlockInfo
   ) {
      this.name = name;
      this.tint = tint;
      this.oreTag = oreTag;
      this.isVanilla = isVanilla;
      this.resourceBlockInfo = resourceBlockInfo;
      this.rawResourceBlockInfo = rawResourceBlockInfo;
   }

   @Override
   public String getRegistrySuffix() {
      return this.name;
   }

   public int getTint() {
      return this.tint;
   }

   public TagKey<Item> getOreTag() {
      return this.oreTag.get();
   }

   public boolean has(ResourceType type) {
      return type != ResourceType.ENRICHED && (!this.isVanilla || !type.isVanilla());
   }

   public boolean isVanilla() {
      return this.isVanilla;
   }

   @Nullable
   public BlockResourceInfo getResourceBlockInfo() {
      return this.resourceBlockInfo;
   }

   @Nullable
   public BlockResourceInfo getRawResourceBlockInfo() {
      return this.rawResourceBlockInfo;
   }
}
