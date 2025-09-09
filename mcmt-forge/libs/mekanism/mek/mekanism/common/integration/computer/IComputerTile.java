package mekanism.common.integration.computer;

import mekanism.common.Mekanism;

public interface IComputerTile {
   default boolean hasComputerSupport() {
      return Mekanism.hooks.computerCompatEnabled();
   }

   default boolean isComputerCapabilityPersistent() {
      return this.hasComputerSupport();
   }

   default void getComputerMethods(BoundMethodHolder holder) {
      FactoryRegistry.bindTo(holder, this);
   }

   String getComputerName();
}
