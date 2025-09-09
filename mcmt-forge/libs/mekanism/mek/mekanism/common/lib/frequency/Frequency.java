package mekanism.common.lib.frequency;

import java.util.Objects;
import java.util.UUID;
import mekanism.api.IFrequency;
import mekanism.api.security.SecurityMode;
import mekanism.common.network.BasePacketHandler;
import mekanism.common.util.MekanismUtils;
import mekanism.common.util.NBTUtils;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.world.level.block.entity.BlockEntity;
import org.jetbrains.annotations.Nullable;

public abstract class Frequency implements IFrequency {
   protected boolean dirty;
   private boolean removed;
   private String name;
   @Nullable
   private UUID ownerUUID;
   private String clientOwner;
   private boolean valid = true;
   private boolean publicFreq;
   private final FrequencyType<?> frequencyType;

   public Frequency(FrequencyType<?> frequencyType, String name, @Nullable UUID uuid) {
      this(frequencyType);
      this.name = name;
      this.ownerUUID = uuid;
   }

   public Frequency(FrequencyType<?> frequencyType) {
      this.frequencyType = frequencyType;
   }

   public boolean tick() {
      return this.dirty;
   }

   public void onRemove() {
      this.removed = true;
   }

   public boolean isRemoved() {
      return this.removed;
   }

   public boolean onDeactivate(BlockEntity tile) {
      return false;
   }

   public boolean update(BlockEntity tile) {
      return false;
   }

   public FrequencyType<?> getType() {
      return this.frequencyType;
   }

   public Object getKey() {
      return this.name;
   }

   @Override
   public final SecurityMode getSecurity() {
      return this.isPublic() ? SecurityMode.PUBLIC : SecurityMode.PRIVATE;
   }

   public boolean isPublic() {
      return this.publicFreq;
   }

   public Frequency setPublic(boolean isPublic) {
      if (this.publicFreq != isPublic) {
         this.publicFreq = isPublic;
         this.dirty = true;
      }

      return this;
   }

   @Override
   public boolean isValid() {
      return this.valid;
   }

   public void setValid(boolean valid) {
      this.valid = valid;
   }

   @Override
   public String getName() {
      return this.name;
   }

   @Nullable
   @Override
   public UUID getOwner() {
      return this.ownerUUID;
   }

   public boolean ownerMatches(UUID toCheck) {
      return Objects.equals(this.ownerUUID, toCheck);
   }

   public String getClientOwner() {
      return this.clientOwner;
   }

   public void writeComponentData(CompoundTag nbtTags) {
      nbtTags.m_128359_("name", this.name);
      if (this.ownerUUID != null) {
         nbtTags.m_128362_("owner", this.ownerUUID);
      }

      nbtTags.m_128379_("publicFreq", this.publicFreq);
   }

   public void write(CompoundTag nbtTags) {
      this.writeComponentData(nbtTags);
   }

   protected void read(CompoundTag nbtTags) {
      this.name = nbtTags.m_128461_("name");
      NBTUtils.setUUIDIfPresent(nbtTags, "owner", uuid -> this.ownerUUID = uuid);
      this.publicFreq = nbtTags.m_128471_("publicFreq");
   }

   public void write(FriendlyByteBuf buffer) {
      this.getType().write(buffer);
      buffer.m_130070_(this.name);
      BasePacketHandler.writeOptional(buffer, this.ownerUUID, FriendlyByteBuf::m_130077_);
      buffer.m_130070_(MekanismUtils.getLastKnownUsername(this.ownerUUID));
      buffer.writeBoolean(this.publicFreq);
   }

   protected void read(FriendlyByteBuf dataStream) {
      this.name = BasePacketHandler.readString(dataStream);
      this.ownerUUID = BasePacketHandler.readOptional(dataStream, FriendlyByteBuf::m_130259_);
      this.clientOwner = BasePacketHandler.readString(dataStream);
      this.publicFreq = dataStream.readBoolean();
   }

   public int getSyncHash() {
      return this.hashCode();
   }

   @Override
   public int hashCode() {
      int code = 1;
      code = 31 * code + this.name.hashCode();
      if (this.ownerUUID != null) {
         code = 31 * code + this.ownerUUID.hashCode();
      }

      return 31 * code + (this.publicFreq ? 1 : 0);
   }

   @Override
   public boolean equals(Object obj) {
      return obj instanceof Frequency other
         && this.publicFreq == other.publicFreq
         && this.ownerUUID != null
         && this.name.equals(other.name)
         && this.ownerUUID.equals(other.ownerUUID);
   }

   public Frequency.FrequencyIdentity getIdentity() {
      return new Frequency.FrequencyIdentity(this.getKey(), this.publicFreq);
   }

   public boolean areIdentitiesEqual(Frequency other) {
      return this.getIdentity().equals(other.getIdentity());
   }

   public CompoundTag serializeIdentity() {
      return this.frequencyType.getIdentitySerializer().serialize(this.getIdentity());
   }

   public CompoundTag serializeIdentityWithOwner() {
      CompoundTag serializedIdentity = this.serializeIdentity();
      if (!serializedIdentity.m_128403_("owner") && this.ownerUUID != null) {
         serializedIdentity.m_128362_("owner", this.ownerUUID);
      }

      return serializedIdentity;
   }

   public static <FREQ extends Frequency> FREQ readFromPacket(FriendlyByteBuf dataStream) {
      return FrequencyType.<FREQ>load(dataStream).create(dataStream);
   }

   public record FrequencyIdentity(Object key, boolean isPublic) {
      public static Frequency.FrequencyIdentity load(FrequencyType<?> type, CompoundTag tag) {
         return type.getIdentitySerializer().load(tag);
      }
   }
}
