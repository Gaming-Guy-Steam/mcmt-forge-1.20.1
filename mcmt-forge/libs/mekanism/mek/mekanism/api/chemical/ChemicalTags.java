package mekanism.api.chemical;

import java.util.Optional;
import java.util.function.Supplier;
import mekanism.api.MekanismAPI;
import mekanism.api.chemical.gas.Gas;
import mekanism.api.chemical.infuse.InfuseType;
import mekanism.api.chemical.pigment.Pigment;
import mekanism.api.chemical.slurry.Slurry;
import net.minecraft.core.Registry;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.TagKey;
import net.minecraftforge.registries.IForgeRegistry;
import net.minecraftforge.registries.tags.ITagManager;

public class ChemicalTags<CHEMICAL extends Chemical<CHEMICAL>> {
   public static final ChemicalTags<Gas> GAS = new ChemicalTags<>(MekanismAPI.GAS_REGISTRY_NAME, MekanismAPI::gasRegistry);
   public static final ChemicalTags<InfuseType> INFUSE_TYPE = new ChemicalTags<>(MekanismAPI.INFUSE_TYPE_REGISTRY_NAME, MekanismAPI::infuseTypeRegistry);
   public static final ChemicalTags<Pigment> PIGMENT = new ChemicalTags<>(MekanismAPI.PIGMENT_REGISTRY_NAME, MekanismAPI::pigmentRegistry);
   public static final ChemicalTags<Slurry> SLURRY = new ChemicalTags<>(MekanismAPI.SLURRY_REGISTRY_NAME, MekanismAPI::slurryRegistry);
   private final Supplier<IForgeRegistry<CHEMICAL>> registrySupplier;
   private final ResourceKey<? extends Registry<CHEMICAL>> registryKeySupplier;

   private ChemicalTags(ResourceKey<? extends Registry<CHEMICAL>> registryKeySupplier, Supplier<IForgeRegistry<CHEMICAL>> registrySupplier) {
      this.registrySupplier = registrySupplier;
      this.registryKeySupplier = registryKeySupplier;
   }

   public TagKey<CHEMICAL> tag(ResourceLocation name) {
      return this.getManager().map(manager -> manager.createTagKey(name)).orElseGet(() -> TagKey.m_203882_(this.registryKeySupplier, name));
   }

   public Optional<ITagManager<CHEMICAL>> getManager() {
      IForgeRegistry<CHEMICAL> registry = this.registrySupplier.get();
      return registry == null ? Optional.empty() : Optional.ofNullable(registry.tags());
   }
}
