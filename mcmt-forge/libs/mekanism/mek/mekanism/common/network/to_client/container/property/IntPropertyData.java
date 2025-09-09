package mekanism.common.network.to_client.container.property;

import mekanism.common.inventory.container.MekanismContainer;
import net.minecraft.network.FriendlyByteBuf;

public class IntPropertyData extends PropertyData {
   private final int value;

   public IntPropertyData(short property, int value) {
      super(PropertyType.INT, property);
      this.value = value;
   }

   @Override
   public void handleWindowProperty(MekanismContainer container) {
      container.handleWindowProperty(this.getProperty(), this.value);
   }

   @Override
   public void writeToPacket(FriendlyByteBuf buffer) {
      super.writeToPacket(buffer);
      buffer.m_130130_(this.value);
   }
}
