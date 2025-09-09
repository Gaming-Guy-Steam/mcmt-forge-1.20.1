package mekanism.api.security;

import mekanism.api.annotations.NothingNullByDefault;
import net.minecraftforge.common.capabilities.AutoRegisterCapability;

@AutoRegisterCapability
@NothingNullByDefault
public interface ISecurityObject extends IOwnerObject {
   SecurityMode getSecurityMode();

   void setSecurityMode(SecurityMode var1);

   default void onSecurityChanged(SecurityMode old, SecurityMode mode) {
   }
}
