package mekanism.common.integration.projecte;

import com.mojang.datafixers.util.Either;
import java.util.Optional;
import java.util.function.Function;
import mekanism.api.MekanismAPI;
import mekanism.api.chemical.pigment.Pigment;
import mekanism.api.chemical.pigment.PigmentStack;
import mekanism.api.providers.IPigmentProvider;
import moze_intel.projecte.api.nss.AbstractNSSTag;
import moze_intel.projecte.api.nss.NormalizedSimpleStack;
import net.minecraft.core.HolderSet.Named;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.TagKey;
import net.minecraftforge.registries.tags.ITag;
import org.jetbrains.annotations.NotNull;

public final class NSSPigment extends AbstractNSSTag<Pigment> {
   private NSSPigment(@NotNull ResourceLocation resourceLocation, boolean isTag) {
      super(resourceLocation, isTag);
   }

   @NotNull
   public static NSSPigment createPigment(@NotNull PigmentStack stack) {
      return createPigment(stack.getType());
   }

   @NotNull
   public static NSSPigment createPigment(@NotNull IPigmentProvider pigmentProvider) {
      return createPigment(pigmentProvider.getChemical());
   }

   @NotNull
   public static NSSPigment createPigment(@NotNull Pigment pigment) {
      if (pigment.isEmptyType()) {
         throw new IllegalArgumentException("Can't make NSSPigment with an empty pigment");
      } else {
         return createPigment(pigment.getRegistryName());
      }
   }

   @NotNull
   public static NSSPigment createPigment(@NotNull ResourceLocation pigmentID) {
      return new NSSPigment(pigmentID, false);
   }

   @NotNull
   public static NSSPigment createTag(@NotNull ResourceLocation tagId) {
      return new NSSPigment(tagId, true);
   }

   @NotNull
   public static NSSPigment createTag(@NotNull TagKey<Pigment> tag) {
      return createTag(tag.f_203868_());
   }

   protected boolean isInstance(AbstractNSSTag o) {
      return o instanceof NSSPigment;
   }

   @NotNull
   public String getJsonPrefix() {
      return "PIGMENT|";
   }

   @NotNull
   public String getType() {
      return "Pigment";
   }

   @NotNull
   protected Optional<Either<Named<Pigment>, ITag<Pigment>>> getTag() {
      return this.getTag(MekanismAPI.pigmentRegistry());
   }

   protected Function<Pigment, NormalizedSimpleStack> createNew() {
      return NSSPigment::createPigment;
   }
}
