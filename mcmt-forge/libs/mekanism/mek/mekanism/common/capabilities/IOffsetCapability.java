package mekanism.common.capabilities;

import net.minecraft.core.Direction;
import net.minecraft.core.Vec3i;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.util.LazyOptional;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public interface IOffsetCapability {
   @NotNull
   default <T> LazyOptional<T> getOffsetCapability(@NotNull Capability<T> capability, @Nullable Direction side, @NotNull Vec3i offset) {
      return this.isOffsetCapabilityDisabled(capability, side, offset) ? LazyOptional.empty() : this.getOffsetCapabilityIfEnabled(capability, side, offset);
   }

   default boolean isOffsetCapabilityDisabled(@NotNull Capability<?> capability, @Nullable Direction side, @NotNull Vec3i offset) {
      return false;
   }

   @NotNull
   <T> LazyOptional<T> getOffsetCapabilityIfEnabled(@NotNull Capability<T> capability, @Nullable Direction side, @NotNull Vec3i offset);
}
