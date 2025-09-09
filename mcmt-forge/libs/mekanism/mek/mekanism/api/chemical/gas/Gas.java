package mekanism.api.chemical.gas;

import mekanism.api.MekanismAPI;
import mekanism.api.annotations.NothingNullByDefault;
import mekanism.api.chemical.Chemical;
import mekanism.api.chemical.ChemicalTags;
import mekanism.api.chemical.ChemicalUtils;
import mekanism.api.providers.IGasProvider;
import net.minecraft.Util;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.ResourceLocation;
import net.minecraftforge.registries.IForgeRegistry;
import org.jetbrains.annotations.Nullable;

@NothingNullByDefault
public class Gas extends Chemical<Gas> implements IGasProvider {
   public Gas(GasBuilder builder) {
      super(builder, ChemicalTags.GAS);
   }

   public static Gas readFromNBT(@Nullable CompoundTag nbtTags) {
      return ChemicalUtils.readChemicalFromNBT(nbtTags, MekanismAPI.EMPTY_GAS, "gasName", Gas::getFromRegistry);
   }

   public static Gas getFromRegistry(@Nullable ResourceLocation name) {
      return ChemicalUtils.readChemicalFromRegistry(name, MekanismAPI.EMPTY_GAS, MekanismAPI.gasRegistry());
   }

   @Override
   public CompoundTag write(CompoundTag nbtTags) {
      nbtTags.m_128359_("gasName", this.getRegistryName().toString());
      return nbtTags;
   }

   @Override
   public String toString() {
      return "[Gas: " + this.getRegistryName() + "]";
   }

   @Override
   public final boolean isEmptyType() {
      return this == MekanismAPI.EMPTY_GAS;
   }

   @Override
   public final ResourceLocation getRegistryName() {
      IForgeRegistry<Gas> registry = MekanismAPI.gasRegistry();
      return registry == null ? null : registry.getKey(this);
   }

   @Override
   protected String getDefaultTranslationKey() {
      return Util.m_137492_("gas", this.getRegistryName());
   }
}
