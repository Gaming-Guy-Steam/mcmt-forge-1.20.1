package mekanism.common.capabilities.resolver;

import java.util.ArrayList;
import java.util.IdentityHashMap;
import java.util.List;
import java.util.Map;
import mekanism.api.annotations.NothingNullByDefault;
import mekanism.api.energy.ISidedStrictEnergyHandler;
import mekanism.api.energy.IStrictEnergyHandler;
import mekanism.common.integration.energy.EnergyCompatUtils;
import net.minecraft.core.Direction;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.util.LazyOptional;
import org.jetbrains.annotations.Nullable;

@NothingNullByDefault
public class EnergyCapabilityResolver implements ICapabilityResolver {
   private final Map<Capability<?>, LazyOptional<?>> cachedCapabilities = new IdentityHashMap<>();
   private final IStrictEnergyHandler handler;

   public EnergyCapabilityResolver(ISidedStrictEnergyHandler handler) {
      this.handler = handler;
   }

   @Override
   public List<Capability<?>> getSupportedCapabilities() {
      return EnergyCompatUtils.getEnabledEnergyCapabilities();
   }

   @Override
   public <T> LazyOptional<T> resolve(Capability<T> capability, @Nullable Direction side) {
      return getCachedOrResolve(capability, this.cachedCapabilities, this.handler);
   }

   @Override
   public void invalidate(Capability<?> capability, @Nullable Direction side) {
      this.invalidate(this.cachedCapabilities.get(capability));
   }

   @Override
   public void invalidateAll() {
      for (LazyOptional<?> lazyOptional : new ArrayList<>(this.cachedCapabilities.values())) {
         this.invalidate(lazyOptional);
      }
   }

   protected void invalidate(@Nullable LazyOptional<?> cachedCapability) {
      if (cachedCapability != null && cachedCapability.isPresent()) {
         cachedCapability.invalidate();
      }
   }

   public static <T> LazyOptional<T> getCachedOrResolve(
      Capability<T> capability, Map<Capability<?>, LazyOptional<?>> cachedCapabilities, IStrictEnergyHandler handler
   ) {
      if (cachedCapabilities.containsKey(capability)) {
         LazyOptional<?> cachedCapability = cachedCapabilities.get(capability);
         if (cachedCapability.isPresent()) {
            return cachedCapability.cast();
         }
      }

      LazyOptional<T> uncachedCapability = EnergyCompatUtils.getEnergyCapability(capability, handler);
      cachedCapabilities.put(capability, uncachedCapability);
      return uncachedCapability;
   }
}
