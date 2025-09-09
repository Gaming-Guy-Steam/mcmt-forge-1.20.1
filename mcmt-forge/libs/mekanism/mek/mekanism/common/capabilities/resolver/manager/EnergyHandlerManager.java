package mekanism.common.capabilities.resolver.manager;

import java.util.ArrayList;
import java.util.Collections;
import java.util.EnumMap;
import java.util.IdentityHashMap;
import java.util.List;
import java.util.Map;
import mekanism.api.annotations.NothingNullByDefault;
import mekanism.api.energy.IEnergyContainer;
import mekanism.api.energy.ISidedStrictEnergyHandler;
import mekanism.api.energy.IStrictEnergyHandler;
import mekanism.common.capabilities.holder.energy.IEnergyContainerHolder;
import mekanism.common.capabilities.proxy.ProxyStrictEnergyHandler;
import mekanism.common.capabilities.resolver.EnergyCapabilityResolver;
import mekanism.common.integration.energy.EnergyCompatUtils;
import net.minecraft.core.Direction;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.util.LazyOptional;
import org.jetbrains.annotations.Nullable;

@NothingNullByDefault
public class EnergyHandlerManager implements ICapabilityHandlerManager<IEnergyContainer> {
   private final Map<Direction, Map<Capability<?>, LazyOptional<?>>> cachedCapabilities;
   private final Map<Capability<?>, LazyOptional<?>> cachedReadOnlyCapabilities;
   private final Map<Direction, IStrictEnergyHandler> handlers;
   private final ISidedStrictEnergyHandler baseHandler;
   private final boolean canHandle;
   @Nullable
   private IStrictEnergyHandler readOnlyHandler;
   @Nullable
   private final IEnergyContainerHolder holder;

   public EnergyHandlerManager(@Nullable IEnergyContainerHolder holder, ISidedStrictEnergyHandler baseHandler) {
      this.holder = holder;
      this.canHandle = this.holder != null;
      this.baseHandler = baseHandler;
      if (this.canHandle) {
         this.handlers = new EnumMap<>(Direction.class);
         this.cachedCapabilities = new EnumMap<>(Direction.class);
         this.cachedReadOnlyCapabilities = new IdentityHashMap<>();
      } else {
         this.handlers = Collections.emptyMap();
         this.cachedCapabilities = Collections.emptyMap();
         this.cachedReadOnlyCapabilities = Collections.emptyMap();
      }
   }

   @Override
   public boolean canHandle() {
      return this.canHandle;
   }

   @Override
   public List<IEnergyContainer> getContainers(@Nullable Direction side) {
      return this.canHandle() ? this.holder.getEnergyContainers(side) : Collections.emptyList();
   }

   @Override
   public List<Capability<?>> getSupportedCapabilities() {
      return EnergyCompatUtils.getEnabledEnergyCapabilities();
   }

   @Override
   public <T> LazyOptional<T> resolve(Capability<T> capability, @Nullable Direction side) {
      if (this.getContainers(side).isEmpty()) {
         return LazyOptional.empty();
      } else if (side == null) {
         if (this.readOnlyHandler == null) {
            this.readOnlyHandler = new ProxyStrictEnergyHandler(this.baseHandler, null, this.holder);
         }

         return EnergyCapabilityResolver.getCachedOrResolve(capability, this.cachedReadOnlyCapabilities, this.readOnlyHandler);
      } else {
         IStrictEnergyHandler handler = this.handlers.computeIfAbsent(side, s -> new ProxyStrictEnergyHandler(this.baseHandler, s, this.holder));
         return EnergyCapabilityResolver.getCachedOrResolve(capability, this.cachedCapabilities.computeIfAbsent(side, key -> new IdentityHashMap<>()), handler);
      }
   }

   @Override
   public void invalidate(Capability<?> capability, @Nullable Direction side) {
      if (side == null) {
         this.invalidate(this.cachedReadOnlyCapabilities.get(capability));
      } else {
         Map<Capability<?>, LazyOptional<?>> cachedSide = this.cachedCapabilities.get(side);
         if (cachedSide != null) {
            this.invalidate(cachedSide.get(capability));
         }
      }
   }

   @Override
   public void invalidateAll() {
      for (Map<Capability<?>, LazyOptional<?>> cachedSide : this.cachedCapabilities.values()) {
         for (LazyOptional<?> lazyOptional : new ArrayList<>(cachedSide.values())) {
            this.invalidate(lazyOptional);
         }
      }

      for (LazyOptional<?> lazyOptional : new ArrayList<>(this.cachedReadOnlyCapabilities.values())) {
         this.invalidate(lazyOptional);
      }
   }

   protected void invalidate(@Nullable LazyOptional<?> cachedCapability) {
      if (cachedCapability != null && cachedCapability.isPresent()) {
         cachedCapability.invalidate();
      }
   }
}
