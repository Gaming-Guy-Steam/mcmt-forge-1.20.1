package mekanism.common.util;

import net.minecraft.core.Direction;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.capabilities.ICapabilityProvider;
import net.minecraftforge.common.util.LazyOptional;
import net.minecraftforge.common.util.NonNullConsumer;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public final class CapabilityUtils {
   private CapabilityUtils() {
   }

   @NotNull
   public static <T> LazyOptional<T> getCapability(@Nullable ICapabilityProvider provider, @Nullable Capability<T> cap, @Nullable Direction side) {
      return provider != null && cap != null && cap.isRegistered() ? provider.getCapability(cap, side) : LazyOptional.empty();
   }

   public static void addListener(@NotNull LazyOptional<?> lazyOptional, @NotNull NonNullConsumer listener) {
      lazyOptional.addListener(listener);
   }
}
