package mekanism.common.integration.projecte;

import com.mojang.datafixers.util.Either;
import java.util.Optional;
import java.util.function.Function;
import mekanism.api.MekanismAPI;
import mekanism.api.chemical.infuse.InfuseType;
import mekanism.api.chemical.infuse.InfusionStack;
import mekanism.api.providers.IInfuseTypeProvider;
import moze_intel.projecte.api.nss.AbstractNSSTag;
import moze_intel.projecte.api.nss.NormalizedSimpleStack;
import net.minecraft.core.HolderSet.Named;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.TagKey;
import net.minecraftforge.registries.tags.ITag;
import org.jetbrains.annotations.NotNull;

public final class NSSInfuseType extends AbstractNSSTag<InfuseType> {
   private NSSInfuseType(@NotNull ResourceLocation resourceLocation, boolean isTag) {
      super(resourceLocation, isTag);
   }

   @NotNull
   public static NSSInfuseType createInfuseType(@NotNull InfusionStack stack) {
      return createInfuseType(stack.getType());
   }

   @NotNull
   public static NSSInfuseType createInfuseType(@NotNull IInfuseTypeProvider infuseTypeProvider) {
      return createInfuseType(infuseTypeProvider.getChemical());
   }

   @NotNull
   public static NSSInfuseType createInfuseType(@NotNull InfuseType infuseType) {
      if (infuseType.isEmptyType()) {
         throw new IllegalArgumentException("Can't make NSSInfuseType with an empty infuse type");
      } else {
         return createInfuseType(infuseType.getRegistryName());
      }
   }

   @NotNull
   public static NSSInfuseType createInfuseType(@NotNull ResourceLocation infuseTypeID) {
      return new NSSInfuseType(infuseTypeID, false);
   }

   @NotNull
   public static NSSInfuseType createTag(@NotNull ResourceLocation tagId) {
      return new NSSInfuseType(tagId, true);
   }

   @NotNull
   public static NSSInfuseType createTag(@NotNull TagKey<InfuseType> tag) {
      return createTag(tag.f_203868_());
   }

   protected boolean isInstance(AbstractNSSTag o) {
      return o instanceof NSSInfuseType;
   }

   @NotNull
   public String getJsonPrefix() {
      return "INFUSE_TYPE|";
   }

   @NotNull
   public String getType() {
      return "Infuse Type";
   }

   @NotNull
   protected Optional<Either<Named<InfuseType>, ITag<InfuseType>>> getTag() {
      return this.getTag(MekanismAPI.infuseTypeRegistry());
   }

   protected Function<InfuseType, NormalizedSimpleStack> createNew() {
      return NSSInfuseType::createInfuseType;
   }
}
