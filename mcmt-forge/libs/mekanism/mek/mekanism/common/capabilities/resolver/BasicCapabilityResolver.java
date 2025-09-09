package mekanism.common.capabilities.resolver;

import java.util.List;
import mekanism.api.annotations.NothingNullByDefault;
import mekanism.api.security.ISecurityObject;
import mekanism.common.capabilities.Capabilities;
import net.minecraft.core.Direction;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.util.LazyOptional;
import net.minecraftforge.common.util.NonNullLazy;
import net.minecraftforge.common.util.NonNullSupplier;
import org.jetbrains.annotations.Nullable;

@NothingNullByDefault
public class BasicCapabilityResolver implements ICapabilityResolver {
   private final List<Capability<?>> supportedCapability;
   private final NonNullSupplier<?> supplier;
   @Nullable
   private LazyOptional<?> cachedCapability;

   public static <T> BasicCapabilityResolver create(Capability<T> supportedCapability, NonNullSupplier<T> supplier) {
      return new BasicCapabilityResolver(supplier, supportedCapability);
   }

   public static <T> BasicCapabilityResolver persistent(Capability<T> supportedCapability, NonNullSupplier<T> supplier) {
      return create(supportedCapability, (NonNullSupplier<T>)(supplier instanceof NonNullLazy ? supplier : NonNullLazy.of(supplier)));
   }

   public static <T> BasicCapabilityResolver constant(Capability<T> supportedCapability, T value) {
      return create(supportedCapability, () -> value);
   }

   public static BasicCapabilityResolver security(ISecurityObject value) {
      return new BasicCapabilityResolver(() -> value, Capabilities.OWNER_OBJECT, Capabilities.SECURITY_OBJECT);
   }

   @SafeVarargs
   protected <T> BasicCapabilityResolver(NonNullSupplier<T> supplier, Capability<? super T>... supportedCapabilities) {
      this.supportedCapability = List.of(supportedCapabilities);
      this.supplier = supplier;
   }

   @Override
   public List<Capability<?>> getSupportedCapabilities() {
      return this.supportedCapability;
   }

   @Override
   public <T> LazyOptional<T> resolve(Capability<T> capability, @Nullable Direction side) {
      if (this.cachedCapability == null || !this.cachedCapability.isPresent()) {
         this.cachedCapability = LazyOptional.of(this.supplier);
      }

      return this.cachedCapability.cast();
   }

   @Override
   public void invalidate(Capability<?> capability, @Nullable Direction side) {
      this.invalidateAll();
   }

   @Override
   public void invalidateAll() {
      if (this.cachedCapability != null && this.cachedCapability.isPresent()) {
         this.cachedCapability.invalidate();
         this.cachedCapability = null;
      }
   }
}
