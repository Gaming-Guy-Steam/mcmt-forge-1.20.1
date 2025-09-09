package mekanism.common.integration.energy;

import java.util.Collection;
import java.util.Collections;
import mekanism.api.annotations.NothingNullByDefault;
import mekanism.api.energy.IStrictEnergyHandler;
import mekanism.common.config.value.CachedValue;
import mekanism.common.util.CapabilityUtils;
import net.minecraft.core.Direction;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.capabilities.ICapabilityProvider;
import net.minecraftforge.common.util.LazyOptional;
import org.jetbrains.annotations.Nullable;

@NothingNullByDefault
public interface IEnergyCompat {
   boolean isUsable();

   default Collection<CachedValue<?>> getBackingConfigs() {
      return Collections.emptySet();
   }

   Capability<?> getCapability();

   default boolean isMatchingCapability(Capability<?> capability) {
      return capability == this.getCapability();
   }

   default boolean isCapabilityPresent(ICapabilityProvider provider, @Nullable Direction side) {
      return CapabilityUtils.getCapability(provider, this.getCapability(), side).isPresent();
   }

   LazyOptional<?> getHandlerAs(IStrictEnergyHandler handler);

   LazyOptional<IStrictEnergyHandler> getLazyStrictEnergyHandler(ICapabilityProvider provider, @Nullable Direction side);
}
