package mekanism.common.base;

import mekanism.common.lib.Version;

public interface IModModule {
   Version getVersion();

   String getName();

   void resetClient();

   default void launchClient() {
   }
}
