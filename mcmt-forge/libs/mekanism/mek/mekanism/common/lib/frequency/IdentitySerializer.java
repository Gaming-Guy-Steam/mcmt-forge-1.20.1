package mekanism.common.lib.frequency;

import java.util.UUID;
import mekanism.common.network.BasePacketHandler;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.FriendlyByteBuf;

public interface IdentitySerializer {
   IdentitySerializer NAME = new IdentitySerializer() {
      @Override
      public Frequency.FrequencyIdentity read(FriendlyByteBuf buf) {
         return new Frequency.FrequencyIdentity(BasePacketHandler.readString(buf), buf.readBoolean());
      }

      @Override
      public Frequency.FrequencyIdentity load(CompoundTag data) {
         String name = data.m_128461_("name");
         return !name.isEmpty() ? new Frequency.FrequencyIdentity(name, data.m_128471_("publicFreq")) : null;
      }

      @Override
      public void write(FriendlyByteBuf buf, Frequency.FrequencyIdentity data) {
         buf.m_130070_(data.key().toString());
         buf.writeBoolean(data.isPublic());
      }

      @Override
      public CompoundTag serialize(Frequency.FrequencyIdentity data) {
         CompoundTag tag = new CompoundTag();
         tag.m_128359_("name", data.key().toString());
         tag.m_128379_("publicFreq", data.isPublic());
         return tag;
      }
   };
   IdentitySerializer UUID = new IdentitySerializer() {
      @Override
      public Frequency.FrequencyIdentity read(FriendlyByteBuf buf) {
         return new Frequency.FrequencyIdentity(buf.m_130259_(), buf.readBoolean());
      }

      @Override
      public Frequency.FrequencyIdentity load(CompoundTag data) {
         return data.m_128403_("owner") ? new Frequency.FrequencyIdentity(data.m_128342_("owner"), data.m_128471_("publicFreq")) : null;
      }

      @Override
      public void write(FriendlyByteBuf buf, Frequency.FrequencyIdentity data) {
         buf.m_130077_((UUID)data.key());
         buf.writeBoolean(data.isPublic());
      }

      @Override
      public CompoundTag serialize(Frequency.FrequencyIdentity data) {
         CompoundTag tag = new CompoundTag();
         tag.m_128362_("owner", (UUID)data.key());
         tag.m_128379_("publicFreq", data.isPublic());
         return tag;
      }
   };

   Frequency.FrequencyIdentity read(FriendlyByteBuf buf);

   Frequency.FrequencyIdentity load(CompoundTag data);

   void write(FriendlyByteBuf buf, Frequency.FrequencyIdentity data);

   CompoundTag serialize(Frequency.FrequencyIdentity data);
}
