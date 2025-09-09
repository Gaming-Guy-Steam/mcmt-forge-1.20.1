package mekanism.client.model.data;

import java.util.EnumMap;
import java.util.Map;
import java.util.Objects;
import mekanism.common.lib.transmitter.ConnectionType;
import mekanism.common.util.EnumUtils;
import net.minecraft.core.Direction;

public sealed class TransmitterModelData permits TransmitterModelData.Diversion {
   private final Map<Direction, ConnectionType> connections = new EnumMap<>(Direction.class);
   private boolean hasColor;

   public void setConnectionData(Direction direction, ConnectionType connectionType) {
      this.connections.put(direction, connectionType);
   }

   public Map<Direction, ConnectionType> getConnectionsMap() {
      return this.connections;
   }

   public ConnectionType getConnectionType(Direction side) {
      return this.connections.get(side);
   }

   public void setHasColor(boolean hasColor) {
      this.hasColor = hasColor;
   }

   public boolean getHasColor() {
      return this.hasColor;
   }

   public boolean check(ConnectionType... types) {
      if (types.length != EnumUtils.DIRECTIONS.length) {
         return false;
      } else {
         for (int i = 0; i < types.length; i++) {
            if (this.connections.get(EnumUtils.DIRECTIONS[i]) != types[i]) {
               return false;
            }
         }

         return true;
      }
   }

   @Override
   public boolean equals(Object o) {
      return o == this ? true : o instanceof TransmitterModelData other && this.hasColor == other.hasColor && this.connections.equals(other.connections);
   }

   @Override
   public int hashCode() {
      return Objects.hash(this.connections, this.hasColor);
   }

   public static final class Diversion extends TransmitterModelData {
      @Override
      public void setHasColor(boolean hasColor) {
      }
   }
}
