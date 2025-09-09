package mekanism.common.integration.projecte;

import com.mojang.datafixers.util.Either;
import java.util.Optional;
import java.util.function.Function;
import mekanism.api.MekanismAPI;
import mekanism.api.chemical.gas.Gas;
import mekanism.api.chemical.gas.GasStack;
import mekanism.api.providers.IGasProvider;
import moze_intel.projecte.api.nss.AbstractNSSTag;
import moze_intel.projecte.api.nss.NormalizedSimpleStack;
import net.minecraft.core.HolderSet.Named;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.TagKey;
import net.minecraftforge.registries.tags.ITag;
import org.jetbrains.annotations.NotNull;

public final class NSSGas extends AbstractNSSTag<Gas> {
   private NSSGas(@NotNull ResourceLocation resourceLocation, boolean isTag) {
      super(resourceLocation, isTag);
   }

   @NotNull
   public static NSSGas createGas(@NotNull GasStack stack) {
      return createGas(stack.getType());
   }

   @NotNull
   public static NSSGas createGas(@NotNull IGasProvider gasProvider) {
      return createGas(gasProvider.getChemical());
   }

   @NotNull
   public static NSSGas createGas(@NotNull Gas gas) {
      if (gas.isEmptyType()) {
         throw new IllegalArgumentException("Can't make NSSGas with an empty gas");
      } else {
         return createGas(gas.getRegistryName());
      }
   }

   @NotNull
   public static NSSGas createGas(@NotNull ResourceLocation gasID) {
      return new NSSGas(gasID, false);
   }

   @NotNull
   public static NSSGas createTag(@NotNull ResourceLocation tagId) {
      return new NSSGas(tagId, true);
   }

   @NotNull
   public static NSSGas createTag(@NotNull TagKey<Gas> tag) {
      return createTag(tag.f_203868_());
   }

   protected boolean isInstance(AbstractNSSTag o) {
      return o instanceof NSSGas;
   }

   @NotNull
   public String getJsonPrefix() {
      return "GAS|";
   }

   @NotNull
   public String getType() {
      return "Gas";
   }

   @NotNull
   protected Optional<Either<Named<Gas>, ITag<Gas>>> getTag() {
      return this.getTag(MekanismAPI.gasRegistry());
   }

   protected Function<Gas, NormalizedSimpleStack> createNew() {
      return NSSGas::createGas;
   }
}
