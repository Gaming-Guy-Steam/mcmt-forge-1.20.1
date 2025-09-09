package mekanism.api.datagen.tag;

import java.util.concurrent.CompletableFuture;
import mekanism.api.MekanismAPI;
import mekanism.api.chemical.Chemical;
import mekanism.api.chemical.gas.Gas;
import mekanism.api.chemical.infuse.InfuseType;
import mekanism.api.chemical.pigment.Pigment;
import mekanism.api.chemical.slurry.Slurry;
import net.minecraft.core.Registry;
import net.minecraft.core.HolderLookup.Provider;
import net.minecraft.data.PackOutput;
import net.minecraft.data.tags.IntrinsicHolderTagsProvider;
import net.minecraft.data.tags.TagsProvider.TagLookup;
import net.minecraft.resources.ResourceKey;
import net.minecraftforge.common.data.ExistingFileHelper;
import org.jetbrains.annotations.Nullable;

public abstract class ChemicalTagsProvider<CHEMICAL extends Chemical<CHEMICAL>> extends IntrinsicHolderTagsProvider<CHEMICAL> {
   protected ChemicalTagsProvider(
      PackOutput packOutput,
      ResourceKey<? extends Registry<CHEMICAL>> registryKey,
      CompletableFuture<Provider> lookupProvider,
      String modid,
      @Nullable ExistingFileHelper existingFileHelper
   ) {
      super(
         packOutput,
         registryKey,
         lookupProvider,
         CompletableFuture.completedFuture(TagLookup.m_274566_()),
         chemical -> ResourceKey.m_135785_(registryKey, chemical.getRegistryName()),
         modid,
         existingFileHelper
      );
   }

   public abstract static class GasTagsProvider extends ChemicalTagsProvider<Gas> {
      protected GasTagsProvider(
         PackOutput packOutput, CompletableFuture<Provider> lookupProvider, String modid, @Nullable ExistingFileHelper existingFileHelper
      ) {
         super(packOutput, MekanismAPI.GAS_REGISTRY_NAME, lookupProvider, modid, existingFileHelper);
      }
   }

   public abstract static class InfuseTypeTagsProvider extends ChemicalTagsProvider<InfuseType> {
      protected InfuseTypeTagsProvider(
         PackOutput packOutput, CompletableFuture<Provider> lookupProvider, String modid, @Nullable ExistingFileHelper existingFileHelper
      ) {
         super(packOutput, MekanismAPI.INFUSE_TYPE_REGISTRY_NAME, lookupProvider, modid, existingFileHelper);
      }
   }

   public abstract static class PigmentTagsProvider extends ChemicalTagsProvider<Pigment> {
      protected PigmentTagsProvider(
         PackOutput packOutput, CompletableFuture<Provider> lookupProvider, String modid, @Nullable ExistingFileHelper existingFileHelper
      ) {
         super(packOutput, MekanismAPI.PIGMENT_REGISTRY_NAME, lookupProvider, modid, existingFileHelper);
      }
   }

   public abstract static class SlurryTagsProvider extends ChemicalTagsProvider<Slurry> {
      protected SlurryTagsProvider(
         PackOutput packOutput, CompletableFuture<Provider> lookupProvider, String modid, @Nullable ExistingFileHelper existingFileHelper
      ) {
         super(packOutput, MekanismAPI.SLURRY_REGISTRY_NAME, lookupProvider, modid, existingFileHelper);
      }
   }
}
