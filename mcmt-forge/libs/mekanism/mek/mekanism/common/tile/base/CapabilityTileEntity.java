package mekanism.common.tile.base;

import java.util.Collection;
import java.util.List;
import java.util.function.BooleanSupplier;
import mekanism.common.capabilities.CapabilityCache;
import mekanism.common.capabilities.resolver.ICapabilityResolver;
import mekanism.common.capabilities.resolver.manager.ICapabilityHandlerManager;
import mekanism.common.registration.impl.TileEntityTypeRegistryObject;
import mekanism.common.tile.component.TileComponentConfig;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.util.LazyOptional;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public abstract class CapabilityTileEntity extends TileEntityUpdateable {
   private final CapabilityCache capabilityCache = new CapabilityCache();

   public CapabilityTileEntity(TileEntityTypeRegistryObject<?> type, BlockPos pos, BlockState state) {
      super(type, pos, state);
   }

   protected final void addCapabilityResolvers(List<ICapabilityHandlerManager<?>> capabilityHandlerManagers) {
      for (ICapabilityHandlerManager<?> capabilityHandlerManager : capabilityHandlerManagers) {
         if (capabilityHandlerManager.canHandle()) {
            this.addCapabilityResolver(capabilityHandlerManager);
         }
      }
   }

   protected final void addCapabilityResolver(ICapabilityResolver resolver) {
      this.capabilityCache.addCapabilityResolver(resolver);
   }

   protected final void addDisabledCapabilities(Capability<?>... capabilities) {
      this.capabilityCache.addDisabledCapabilities(capabilities);
   }

   protected final void addDisabledCapabilities(Collection<Capability<?>> capabilities) {
      this.capabilityCache.addDisabledCapabilities(capabilities);
   }

   protected final void addSemiDisabledCapability(Capability<?> capability, BooleanSupplier checker) {
      this.capabilityCache.addSemiDisabledCapability(capability, checker);
   }

   protected final void addConfigComponent(TileComponentConfig config) {
      this.capabilityCache.addConfigComponent(config);
   }

   protected <T> boolean canEverResolve(Capability<T> capability) {
      return this.capabilityCache.canResolve(capability);
   }

   @NotNull
   public <T> LazyOptional<T> getCapability(@NotNull Capability<T> capability, @Nullable Direction side) {
      if (this.capabilityCache.isCapabilityDisabled(capability, side)) {
         return LazyOptional.empty();
      } else {
         return this.capabilityCache.canResolve(capability)
            ? this.capabilityCache.getCapabilityUnchecked(capability, side)
            : super.getCapability(capability, side);
      }
   }

   public void invalidateCaps() {
      super.invalidateCaps();
      this.invalidateCachedCapabilities();
   }

   public void invalidateCachedCapabilities() {
      this.capabilityCache.invalidateAll();
   }

   public void invalidateCapability(@NotNull Capability<?> capability, @Nullable Direction side) {
      this.capabilityCache.invalidate(capability, side);
   }

   public void invalidateCapability(@NotNull Capability<?> capability, Direction... sides) {
      this.capabilityCache.invalidateSides(capability, sides);
   }

   public void invalidateCapabilities(@NotNull Collection<Capability<?>> capabilities, @Nullable Direction side) {
      for (Capability<?> capability : capabilities) {
         this.invalidateCapability(capability, side);
      }
   }

   public void invalidateCapabilities(@NotNull Collection<Capability<?>> capabilities, Direction... sides) {
      for (Capability<?> capability : capabilities) {
         this.invalidateCapability(capability, sides);
      }
   }
}
