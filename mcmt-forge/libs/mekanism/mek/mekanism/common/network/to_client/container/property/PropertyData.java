package mekanism.common.network.to_client.container.property;

import mekanism.common.inventory.container.MekanismContainer;
import net.minecraft.network.FriendlyByteBuf;

public abstract class PropertyData {
   private final PropertyType type;
   private final short property;

   protected PropertyData(PropertyType type, short property) {
      this.type = type;
      this.property = property;
   }

   public PropertyType getType() {
      return this.type;
   }

   public short getProperty() {
      return this.property;
   }

   public abstract void handleWindowProperty(MekanismContainer container);

   public void writeToPacket(FriendlyByteBuf buffer) {
      buffer.m_130068_(this.type);
      buffer.writeShort(this.property);
   }

   public static PropertyData fromBuffer(FriendlyByteBuf buffer) {
      PropertyType type = (PropertyType)buffer.m_130066_(PropertyType.class);
      short property = buffer.readShort();
      return type.createData(property, buffer);
   }
}
