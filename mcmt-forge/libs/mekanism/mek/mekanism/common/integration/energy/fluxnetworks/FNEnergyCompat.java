package mekanism.common.integration.energy.fluxnetworks;

import java.util.Collection;
import java.util.Set;
import mekanism.api.annotations.NothingNullByDefault;
import mekanism.api.energy.IStrictEnergyHandler;
import mekanism.common.Mekanism;
import mekanism.common.config.MekanismConfig;
import mekanism.common.config.value.CachedValue;
import mekanism.common.integration.energy.IEnergyCompat;
import mekanism.common.util.CapabilityUtils;
import mekanism.common.util.UnitDisplayUtils;
import net.minecraft.core.Direction;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.capabilities.CapabilityManager;
import net.minecraftforge.common.capabilities.CapabilityToken;
import net.minecraftforge.common.capabilities.ICapabilityProvider;
import net.minecraftforge.common.util.LazyOptional;
import org.jetbrains.annotations.Nullable;
import sonar.fluxnetworks.api.energy.IFNEnergyStorage;

@NothingNullByDefault
public class FNEnergyCompat implements IEnergyCompat {
   private static final Capability<IFNEnergyStorage> FN_ENERGY_CAPABILITY = CapabilityManager.get(new CapabilityToken<IFNEnergyStorage>() {});

   @Override
   public Capability<?> getCapability() {
      return FN_ENERGY_CAPABILITY;
   }

   @Override
   public boolean isMatchingCapability(Capability<?> capability) {
      return capability == FN_ENERGY_CAPABILITY;
   }

   @Override
   public boolean isUsable() {
      return Mekanism.hooks.FluxNetworksLoaded && UnitDisplayUtils.EnergyUnit.FORGE_ENERGY.isEnabled() && !MekanismConfig.general.blacklistFluxNetworks.get();
   }

   @Override
   public Collection<CachedValue<?>> getBackingConfigs() {
      return Mekanism.hooks.FluxNetworksLoaded ? Set.of(MekanismConfig.general.blacklistForge, MekanismConfig.general.blacklistFluxNetworks) : Set.of();
   }

   @Override
   public LazyOptional<?> getHandlerAs(IStrictEnergyHandler handler) {
      return LazyOptional.of(() -> new FNIntegration(handler));
   }

   @Override
   public LazyOptional<IStrictEnergyHandler> getLazyStrictEnergyHandler(ICapabilityProvider provider, @Nullable Direction side) {
      return CapabilityUtils.getCapability(provider, FN_ENERGY_CAPABILITY, side).lazyMap(FNStrictEnergyHandler::new);
   }
}
