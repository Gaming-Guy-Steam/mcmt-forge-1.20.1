package mekanism.common.inventory.container;

import net.minecraftforge.common.capabilities.ICapabilityProvider;
import org.jetbrains.annotations.Nullable;

public interface ISecurityContainer {
   @Nullable
   ICapabilityProvider getSecurityObject();
}
