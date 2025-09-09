package mekanism.api.chemical.pigment;

import mekanism.api.MekanismAPI;
import mekanism.api.annotations.NothingNullByDefault;
import mekanism.api.chemical.Chemical;
import mekanism.api.chemical.ChemicalTags;
import mekanism.api.chemical.ChemicalUtils;
import mekanism.api.providers.IPigmentProvider;
import net.minecraft.Util;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.ResourceLocation;
import net.minecraftforge.registries.IForgeRegistry;
import org.jetbrains.annotations.Nullable;

@NothingNullByDefault
public class Pigment extends Chemical<Pigment> implements IPigmentProvider {
   public Pigment(PigmentBuilder builder) {
      super(builder, ChemicalTags.PIGMENT);
   }

   public static Pigment readFromNBT(@Nullable CompoundTag nbtTags) {
      return ChemicalUtils.readChemicalFromNBT(nbtTags, MekanismAPI.EMPTY_PIGMENT, "pigmentName", Pigment::getFromRegistry);
   }

   public static Pigment getFromRegistry(@Nullable ResourceLocation name) {
      return ChemicalUtils.readChemicalFromRegistry(name, MekanismAPI.EMPTY_PIGMENT, MekanismAPI.pigmentRegistry());
   }

   @Override
   public String toString() {
      return "[Pigment: " + this.getRegistryName() + "]";
   }

   @Override
   public CompoundTag write(CompoundTag nbtTags) {
      nbtTags.m_128359_("pigmentName", this.getRegistryName().toString());
      return nbtTags;
   }

   @Override
   public final boolean isEmptyType() {
      return this == MekanismAPI.EMPTY_PIGMENT;
   }

   @Override
   public final ResourceLocation getRegistryName() {
      IForgeRegistry<Pigment> registry = MekanismAPI.pigmentRegistry();
      return registry == null ? null : registry.getKey(this);
   }

   @Override
   protected String getDefaultTranslationKey() {
      return Util.m_137492_("pigment", this.getRegistryName());
   }
}
