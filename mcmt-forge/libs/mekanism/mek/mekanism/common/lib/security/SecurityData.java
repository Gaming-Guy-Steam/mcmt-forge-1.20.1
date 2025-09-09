package mekanism.common.lib.security;

import mekanism.api.security.SecurityMode;
import net.minecraft.network.FriendlyByteBuf;

public record SecurityData(SecurityMode mode, boolean override) {
   public static final SecurityData DUMMY = new SecurityData(SecurityMode.PUBLIC, false);

   public SecurityData(SecurityFrequency frequency) {
      this(frequency.getSecurityMode(), frequency.isOverridden());
   }

   public static SecurityData read(FriendlyByteBuf dataStream) {
      return new SecurityData((SecurityMode)dataStream.m_130066_(SecurityMode.class), dataStream.readBoolean());
   }

   public void write(FriendlyByteBuf dataStream) {
      dataStream.m_130068_(this.mode);
      dataStream.writeBoolean(this.override);
   }
}
