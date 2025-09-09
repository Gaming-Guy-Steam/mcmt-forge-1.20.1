package mekanism.api.chemical.infuse;

import mekanism.api.MekanismAPI;
import mekanism.api.annotations.NothingNullByDefault;
import mekanism.api.chemical.Chemical;
import mekanism.api.chemical.ChemicalTags;
import mekanism.api.chemical.ChemicalUtils;
import mekanism.api.providers.IInfuseTypeProvider;
import net.minecraft.Util;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.ResourceLocation;
import net.minecraftforge.registries.IForgeRegistry;
import org.jetbrains.annotations.Nullable;

@NothingNullByDefault
public class InfuseType extends Chemical<InfuseType> implements IInfuseTypeProvider {
   public InfuseType(InfuseTypeBuilder builder) {
      super(builder, ChemicalTags.INFUSE_TYPE);
   }

   public static InfuseType readFromNBT(@Nullable CompoundTag nbtTags) {
      return ChemicalUtils.readChemicalFromNBT(nbtTags, MekanismAPI.EMPTY_INFUSE_TYPE, "infuseTypeName", InfuseType::getFromRegistry);
   }

   public static InfuseType getFromRegistry(@Nullable ResourceLocation name) {
      return ChemicalUtils.readChemicalFromRegistry(name, MekanismAPI.EMPTY_INFUSE_TYPE, MekanismAPI.infuseTypeRegistry());
   }

   @Override
   public String toString() {
      return "[InfuseType: " + this.getRegistryName() + "]";
   }

   @Override
   public CompoundTag write(CompoundTag nbtTags) {
      nbtTags.m_128359_("infuseTypeName", this.getRegistryName().toString());
      return nbtTags;
   }

   @Override
   public final boolean isEmptyType() {
      return this == MekanismAPI.EMPTY_INFUSE_TYPE;
   }

   @Override
   public final ResourceLocation getRegistryName() {
      IForgeRegistry<InfuseType> registry = MekanismAPI.infuseTypeRegistry();
      return registry == null ? null : registry.getKey(this);
   }

   @Override
   protected String getDefaultTranslationKey() {
      return Util.m_137492_("infuse_type", this.getRegistryName());
   }
}
