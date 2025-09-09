package mekanism.common.capabilities.resolver.manager;

import java.util.List;
import mekanism.common.capabilities.resolver.ICapabilityResolver;
import net.minecraft.MethodsReturnNonnullByDefault;
import net.minecraft.core.Direction;
import org.jetbrains.annotations.Nullable;

@MethodsReturnNonnullByDefault
public interface ICapabilityHandlerManager<CONTAINER> extends ICapabilityResolver {
   boolean canHandle();

   List<CONTAINER> getContainers(@Nullable Direction side);
}
