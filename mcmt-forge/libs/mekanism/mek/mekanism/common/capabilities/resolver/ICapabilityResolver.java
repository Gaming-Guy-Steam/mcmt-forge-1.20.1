package mekanism.common.capabilities.resolver;

import java.util.List;
import mekanism.api.annotations.NothingNullByDefault;
import net.minecraft.core.Direction;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.util.LazyOptional;
import org.jetbrains.annotations.Nullable;

@NothingNullByDefault
public interface ICapabilityResolver {
   List<Capability<?>> getSupportedCapabilities();

   <T> LazyOptional<T> resolve(Capability<T> capability, @Nullable Direction side);

   void invalidate(Capability<?> capability, @Nullable Direction side);

   void invalidateAll();
}
