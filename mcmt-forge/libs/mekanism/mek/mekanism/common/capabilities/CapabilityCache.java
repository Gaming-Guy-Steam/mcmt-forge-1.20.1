package mekanism.common.capabilities;

import it.unimi.dsi.fastutil.objects.ReferenceOpenHashSet;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.IdentityHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.BooleanSupplier;
import mekanism.api.annotations.NothingNullByDefault;
import mekanism.common.Mekanism;
import mekanism.common.capabilities.resolver.ICapabilityResolver;
import mekanism.common.tile.component.TileComponentConfig;
import net.minecraft.core.Direction;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.util.LazyOptional;
import org.jetbrains.annotations.Nullable;

@NothingNullByDefault
public class CapabilityCache {
   private final Map<Capability<?>, ICapabilityResolver> capabilityResolvers = new IdentityHashMap<>();
   private final List<ICapabilityResolver> uniqueResolvers = new ArrayList<>();
   private final Set<Capability<?>> alwaysDisabled = new ReferenceOpenHashSet();
   private final Map<Capability<?>, List<BooleanSupplier>> semiDisabled = new IdentityHashMap<>();
   @Nullable
   private TileComponentConfig config;

   public void addCapabilityResolver(ICapabilityResolver resolver) {
      this.uniqueResolvers.add(resolver);

      for (Capability<?> supportedCapability : resolver.getSupportedCapabilities()) {
         if (this.capabilityResolvers.put(supportedCapability, resolver) != null) {
            Mekanism.logger.warn("Multiple capability resolvers registered for {}. Overriding", supportedCapability.getName(), new Exception());
         }
      }
   }

   public void addDisabledCapabilities(Capability<?>... capabilities) {
      Collections.addAll(this.alwaysDisabled, capabilities);
   }

   public void addDisabledCapabilities(Collection<Capability<?>> capabilities) {
      this.alwaysDisabled.addAll(capabilities);
   }

   public void addSemiDisabledCapability(Capability<?> capability, BooleanSupplier checker) {
      this.semiDisabled.computeIfAbsent(capability, cap -> new ArrayList<>()).add(checker);
   }

   public void addConfigComponent(TileComponentConfig config) {
      if (this.config != null) {
         Mekanism.logger.warn("Config component already registered. Overriding", new Exception());
      }

      this.config = config;
   }

   public boolean isCapabilityDisabled(Capability<?> capability, @Nullable Direction side) {
      if (capability.isRegistered() && !this.alwaysDisabled.contains(capability)) {
         if (this.semiDisabled.containsKey(capability)) {
            for (BooleanSupplier predicate : this.semiDisabled.get(capability)) {
               if (predicate.getAsBoolean()) {
                  return true;
               }
            }
         }

         return this.config == null ? false : this.config.isCapabilityDisabled(capability, side);
      } else {
         return true;
      }
   }

   public boolean canResolve(Capability<?> capability) {
      return this.capabilityResolvers.containsKey(capability);
   }

   public <T> LazyOptional<T> getCapability(Capability<T> capability, @Nullable Direction side) {
      return !this.isCapabilityDisabled(capability, side) && this.canResolve(capability) ? this.getCapabilityUnchecked(capability, side) : LazyOptional.empty();
   }

   public <T> LazyOptional<T> getCapabilityUnchecked(Capability<T> capability, @Nullable Direction side) {
      ICapabilityResolver capabilityResolver = this.capabilityResolvers.get(capability);
      return capabilityResolver == null ? LazyOptional.empty() : capabilityResolver.resolve(capability, side);
   }

   public void invalidate(Capability<?> capability, @Nullable Direction side) {
      ICapabilityResolver capabilityResolver = this.capabilityResolvers.get(capability);
      if (capabilityResolver != null) {
         capabilityResolver.invalidate(capability, side);
      }
   }

   public void invalidateSides(Capability<?> capability, Direction... sides) {
      ICapabilityResolver capabilityResolver = this.capabilityResolvers.get(capability);
      if (capabilityResolver != null) {
         for (Direction side : sides) {
            capabilityResolver.invalidate(capability, side);
         }
      }
   }

   public void invalidateAll() {
      this.uniqueResolvers.forEach(ICapabilityResolver::invalidateAll);
   }
}
