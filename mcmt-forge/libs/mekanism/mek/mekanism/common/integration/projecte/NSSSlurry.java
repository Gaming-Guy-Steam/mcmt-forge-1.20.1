package mekanism.common.integration.projecte;

import com.mojang.datafixers.util.Either;
import java.util.Optional;
import java.util.function.Function;
import mekanism.api.MekanismAPI;
import mekanism.api.chemical.slurry.Slurry;
import mekanism.api.chemical.slurry.SlurryStack;
import mekanism.api.providers.ISlurryProvider;
import moze_intel.projecte.api.nss.AbstractNSSTag;
import moze_intel.projecte.api.nss.NormalizedSimpleStack;
import net.minecraft.core.HolderSet.Named;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.TagKey;
import net.minecraftforge.registries.tags.ITag;
import org.jetbrains.annotations.NotNull;

public final class NSSSlurry extends AbstractNSSTag<Slurry> {
   private NSSSlurry(@NotNull ResourceLocation resourceLocation, boolean isTag) {
      super(resourceLocation, isTag);
   }

   @NotNull
   public static NSSSlurry createSlurry(@NotNull SlurryStack stack) {
      return createSlurry(stack.getType());
   }

   @NotNull
   public static NSSSlurry createSlurry(@NotNull ISlurryProvider slurryProvider) {
      return createSlurry(slurryProvider.getChemical());
   }

   @NotNull
   public static NSSSlurry createSlurry(@NotNull Slurry slurry) {
      if (slurry.isEmptyType()) {
         throw new IllegalArgumentException("Can't make NSSSlurry with an empty slurry");
      } else {
         return createSlurry(slurry.getRegistryName());
      }
   }

   @NotNull
   public static NSSSlurry createSlurry(@NotNull ResourceLocation slurryID) {
      return new NSSSlurry(slurryID, false);
   }

   @NotNull
   public static NSSSlurry createTag(@NotNull ResourceLocation tagId) {
      return new NSSSlurry(tagId, true);
   }

   @NotNull
   public static NSSSlurry createTag(@NotNull TagKey<Slurry> tag) {
      return createTag(tag.f_203868_());
   }

   protected boolean isInstance(AbstractNSSTag o) {
      return o instanceof NSSSlurry;
   }

   @NotNull
   public String getJsonPrefix() {
      return "SLURRY|";
   }

   @NotNull
   public String getType() {
      return "Slurry";
   }

   @NotNull
   protected Optional<Either<Named<Slurry>, ITag<Slurry>>> getTag() {
      return this.getTag(MekanismAPI.slurryRegistry());
   }

   protected Function<Slurry, NormalizedSimpleStack> createNew() {
      return NSSSlurry::createSlurry;
   }
}
