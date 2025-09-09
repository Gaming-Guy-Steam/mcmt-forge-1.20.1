package mekanism.api.chemical.slurry;

import mekanism.api.MekanismAPI;
import mekanism.api.annotations.NothingNullByDefault;
import mekanism.api.chemical.Chemical;
import mekanism.api.chemical.ChemicalTags;
import mekanism.api.chemical.ChemicalUtils;
import mekanism.api.providers.ISlurryProvider;
import net.minecraft.Util;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.TagKey;
import net.minecraft.world.item.Item;
import net.minecraftforge.registries.IForgeRegistry;
import org.jetbrains.annotations.Nullable;

@NothingNullByDefault
public class Slurry extends Chemical<Slurry> implements ISlurryProvider {
   @Nullable
   private final TagKey<Item> oreTag;

   public Slurry(SlurryBuilder builder) {
      super(builder, ChemicalTags.SLURRY);
      this.oreTag = builder.getOreTag();
   }

   public static Slurry readFromNBT(@Nullable CompoundTag nbtTags) {
      return ChemicalUtils.readChemicalFromNBT(nbtTags, MekanismAPI.EMPTY_SLURRY, "slurryName", Slurry::getFromRegistry);
   }

   public static Slurry getFromRegistry(@Nullable ResourceLocation name) {
      return ChemicalUtils.readChemicalFromRegistry(name, MekanismAPI.EMPTY_SLURRY, MekanismAPI.slurryRegistry());
   }

   @Override
   public String toString() {
      return "[Slurry: " + this.getRegistryName() + "]";
   }

   @Override
   public CompoundTag write(CompoundTag nbtTags) {
      nbtTags.m_128359_("slurryName", this.getRegistryName().toString());
      return nbtTags;
   }

   @Override
   public final boolean isEmptyType() {
      return this == MekanismAPI.EMPTY_SLURRY;
   }

   @Override
   public final ResourceLocation getRegistryName() {
      IForgeRegistry<Slurry> registry = MekanismAPI.slurryRegistry();
      return registry == null ? null : registry.getKey(this);
   }

   @Override
   protected String getDefaultTranslationKey() {
      return Util.m_137492_("slurry", this.getRegistryName());
   }

   @Nullable
   public TagKey<Item> getOreTag() {
      return this.oreTag;
   }
}
