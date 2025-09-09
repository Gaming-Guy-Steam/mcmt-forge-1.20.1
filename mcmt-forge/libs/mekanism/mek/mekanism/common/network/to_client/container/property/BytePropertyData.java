package mekanism.common.network.to_client.container.property;

import mekanism.common.inventory.container.MekanismContainer;
import net.minecraft.network.FriendlyByteBuf;

public class BytePropertyData extends PropertyData {
   private final byte value;

   public BytePropertyData(short property, byte value) {
      super(PropertyType.BYTE, property);
      this.value = value;
   }

   @Override
   public void handleWindowProperty(MekanismContainer container) {
      container.handleWindowProperty(this.getProperty(), this.value);
   }

   @Override
   public void writeToPacket(FriendlyByteBuf buffer) {
      super.writeToPacket(buffer);
      buffer.writeByte(this.value);
   }
}
