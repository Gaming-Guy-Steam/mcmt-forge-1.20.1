package mekanism.api.chemical.slurry;

import java.util.Objects;
import mekanism.api.annotations.NothingNullByDefault;
import mekanism.api.chemical.ChemicalBuilder;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.ItemTags;
import net.minecraft.tags.TagKey;
import net.minecraft.world.item.Item;
import org.jetbrains.annotations.Nullable;

@NothingNullByDefault
public class SlurryBuilder extends ChemicalBuilder<Slurry, SlurryBuilder> {
   @Nullable
   private TagKey<Item> oreTag;

   protected SlurryBuilder(ResourceLocation texture) {
      super(texture);
   }

   public static SlurryBuilder clean() {
      return builder(new ResourceLocation("mekanism", "slurry/clean"));
   }

   public static SlurryBuilder dirty() {
      return builder(new ResourceLocation("mekanism", "slurry/dirty"));
   }

   public static SlurryBuilder builder(ResourceLocation texture) {
      return new SlurryBuilder(Objects.requireNonNull(texture));
   }

   public SlurryBuilder ore(ResourceLocation oreTagLocation) {
      return this.ore(ItemTags.create(Objects.requireNonNull(oreTagLocation)));
   }

   public SlurryBuilder ore(TagKey<Item> oreTag) {
      this.oreTag = Objects.requireNonNull(oreTag);
      return this;
   }

   @Nullable
   public TagKey<Item> getOreTag() {
      return this.oreTag;
   }
}
