package mekanism.api;

import java.util.UUID;
import mekanism.api.security.SecurityMode;
import org.jetbrains.annotations.Nullable;

public interface IFrequency {
   SecurityMode getSecurity();

   boolean isValid();

   String getName();

   @Nullable
   UUID getOwner();
}
