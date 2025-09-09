package mekanism.common.capabilities.resolver;

import java.util.Collections;
import java.util.EnumMap;
import java.util.List;
import java.util.Map;
import mekanism.api.annotations.NothingNullByDefault;
import mekanism.common.capabilities.holder.IHolder;
import net.minecraft.core.Direction;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.util.LazyOptional;
import org.jetbrains.annotations.Nullable;

@NothingNullByDefault
public class BasicSidedCapabilityResolver<HANDLER, SIDED_HANDLER extends HANDLER> implements ICapabilityResolver {
   private final BasicSidedCapabilityResolver.ProxyCreator<HANDLER, SIDED_HANDLER> proxyCreator;
   private final Map<Direction, LazyOptional<HANDLER>> handlers;
   private final List<Capability<?>> supportedCapability;
   private final SIDED_HANDLER baseHandler;
   @Nullable
   private LazyOptional<HANDLER> readOnlyHandler;

   public BasicSidedCapabilityResolver(
      SIDED_HANDLER baseHandler, Capability<HANDLER> supportedCapability, BasicSidedCapabilityResolver.BasicProxyCreator<HANDLER, SIDED_HANDLER> proxyCreator
   ) {
      this(baseHandler, supportedCapability, proxyCreator, true);
   }

   protected BasicSidedCapabilityResolver(
      SIDED_HANDLER baseHandler,
      Capability<HANDLER> supportedCapability,
      BasicSidedCapabilityResolver.ProxyCreator<HANDLER, SIDED_HANDLER> proxyCreator,
      boolean canHandle
   ) {
      this.supportedCapability = Collections.singletonList(supportedCapability);
      this.baseHandler = baseHandler;
      this.proxyCreator = proxyCreator;
      if (canHandle) {
         this.handlers = new EnumMap<>(Direction.class);
      } else {
         this.handlers = Collections.emptyMap();
      }
   }

   public SIDED_HANDLER getInternal() {
      return this.baseHandler;
   }

   @Override
   public List<Capability<?>> getSupportedCapabilities() {
      return this.supportedCapability;
   }

   @Nullable
   protected IHolder getHolder() {
      return null;
   }

   @Override
   public <T> LazyOptional<T> resolve(Capability<T> capability, @Nullable Direction side) {
      if (side == null) {
         if (this.readOnlyHandler == null || !this.readOnlyHandler.isPresent()) {
            this.readOnlyHandler = LazyOptional.of(() -> this.proxyCreator.create(this.baseHandler, null, this.getHolder()));
         }

         return this.readOnlyHandler.cast();
      } else {
         LazyOptional<HANDLER> cachedCapability = this.handlers.get(side);
         if (cachedCapability == null || !cachedCapability.isPresent()) {
            this.handlers.put(side, cachedCapability = LazyOptional.of(() -> this.proxyCreator.create(this.baseHandler, side, this.getHolder())));
         }

         return cachedCapability.cast();
      }
   }

   @Override
   public void invalidate(Capability<?> capability, @Nullable Direction side) {
      if (side == null) {
         this.invalidateReadOnly();
      } else {
         this.invalidate(this.handlers.get(side));
      }
   }

   @Override
   public void invalidateAll() {
      this.invalidateReadOnly();
      this.handlers.values().forEach(this::invalidate);
   }

   private void invalidateReadOnly() {
      if (this.readOnlyHandler != null && this.readOnlyHandler.isPresent()) {
         this.readOnlyHandler.invalidate();
         this.readOnlyHandler = null;
      }
   }

   protected void invalidate(@Nullable LazyOptional<?> cachedCapability) {
      if (cachedCapability != null && cachedCapability.isPresent()) {
         cachedCapability.invalidate();
      }
   }

   @FunctionalInterface
   public interface BasicProxyCreator<HANDLER, SIDED_HANDLER extends HANDLER> extends BasicSidedCapabilityResolver.ProxyCreator<HANDLER, SIDED_HANDLER> {
      HANDLER create(SIDED_HANDLER handler, @Nullable Direction side);

      @Override
      default HANDLER create(SIDED_HANDLER handler, @Nullable Direction side, @Nullable IHolder holder) {
         return this.create(handler, side);
      }
   }

   @FunctionalInterface
   public interface ProxyCreator<HANDLER, SIDED_HANDLER extends HANDLER> {
      HANDLER create(SIDED_HANDLER handler, @Nullable Direction side, @Nullable IHolder holder);
   }
}
