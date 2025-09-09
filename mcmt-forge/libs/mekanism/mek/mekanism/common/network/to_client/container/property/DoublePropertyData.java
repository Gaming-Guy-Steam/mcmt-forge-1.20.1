package mekanism.common.network.to_client.container.property;

import mekanism.common.inventory.container.MekanismContainer;
import net.minecraft.network.FriendlyByteBuf;

public class DoublePropertyData extends PropertyData {
   private final double value;

   public DoublePropertyData(short property, double value) {
      super(PropertyType.DOUBLE, property);
      this.value = value;
   }

   @Override
   public void handleWindowProperty(MekanismContainer container) {
      container.handleWindowProperty(this.getProperty(), this.value);
   }

   @Override
   public void writeToPacket(FriendlyByteBuf buffer) {
      super.writeToPacket(buffer);
      buffer.writeDouble(this.value);
   }
}
