package mekanism.common.lib.security;

import java.util.List;
import java.util.UUID;
import mekanism.api.security.SecurityMode;
import mekanism.common.lib.collection.HashList;
import mekanism.common.lib.frequency.Frequency;
import mekanism.common.lib.frequency.FrequencyType;
import mekanism.common.network.BasePacketHandler;
import mekanism.common.util.MekanismUtils;
import mekanism.common.util.NBTUtils;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.NbtUtils;
import net.minecraft.nbt.Tag;
import net.minecraft.network.FriendlyByteBuf;
import org.jetbrains.annotations.Nullable;

public class SecurityFrequency extends Frequency {
   public static final String SECURITY = "Security";
   private boolean override = false;
   private final List<UUID> trusted = new HashList<>();
   private List<String> trustedCache = new HashList<>();
   private int trustedCacheHash;
   private SecurityMode securityMode = SecurityMode.PUBLIC;

   public SecurityFrequency(@Nullable UUID uuid) {
      super(FrequencyType.SECURITY, "Security", uuid);
   }

   public SecurityFrequency() {
      super(FrequencyType.SECURITY, "Security", null);
   }

   public UUID getKey() {
      return this.getOwner();
   }

   @Override
   public void write(CompoundTag nbtTags) {
      super.write(nbtTags);
      nbtTags.m_128379_("override", this.override);
      NBTUtils.writeEnum(nbtTags, "securityMode", this.securityMode);
      if (!this.trusted.isEmpty()) {
         ListTag trustedList = new ListTag();

         for (UUID uuid : this.trusted) {
            trustedList.add(NbtUtils.m_129226_(uuid));
         }

         nbtTags.m_128365_("trusted", trustedList);
      }
   }

   @Override
   protected void read(CompoundTag nbtTags) {
      super.read(nbtTags);
      NBTUtils.setBooleanIfPresent(nbtTags, "override", value -> this.override = value);
      NBTUtils.setEnumIfPresent(nbtTags, "securityMode", SecurityMode::byIndexStatic, mode -> this.securityMode = mode);
      if (nbtTags.m_128425_("trusted", 9)) {
         for (Tag trusted : nbtTags.m_128437_("trusted", 11)) {
            UUID uuid = NbtUtils.m_129233_(trusted);
            this.addTrustedRaw(uuid, MekanismUtils.getLastKnownUsername(uuid));
         }
      }
   }

   @Override
   public void write(FriendlyByteBuf buffer) {
      super.write(buffer);
      buffer.writeBoolean(this.override);
      buffer.m_130068_(this.securityMode);
      buffer.m_236828_(this.trustedCache, FriendlyByteBuf::m_130070_);
   }

   @Override
   protected void read(FriendlyByteBuf dataStream) {
      super.read(dataStream);
      this.override = dataStream.readBoolean();
      this.securityMode = (SecurityMode)dataStream.m_130066_(SecurityMode.class);
      this.trustedCache = dataStream.m_236845_(BasePacketHandler::readString);
   }

   @Override
   public int getSyncHash() {
      int code = super.getSyncHash();
      code = 31 * code + (this.override ? 1 : 0);
      code = 31 * code + (this.securityMode == null ? 0 : this.securityMode.ordinal());
      return 31 * code + this.trustedCacheHash;
   }

   public void setOverridden(boolean override) {
      if (this.override != override) {
         this.override = override;
         this.dirty = true;
      }
   }

   public boolean isOverridden() {
      return this.override;
   }

   public void setSecurityMode(SecurityMode securityMode) {
      if (this.securityMode != securityMode) {
         this.securityMode = securityMode;
         this.dirty = true;
      }
   }

   public SecurityMode getSecurityMode() {
      return this.securityMode;
   }

   public List<UUID> getTrustedUUIDs() {
      return this.trusted;
   }

   public List<String> getTrustedUsernameCache() {
      return this.trustedCache;
   }

   public void addTrusted(UUID uuid, String name) {
      if (!this.trusted.contains(uuid)) {
         this.addTrustedRaw(uuid, name);
         this.dirty = true;
      }
   }

   private void addTrustedRaw(UUID uuid, String name) {
      this.trusted.add(uuid);
      this.trustedCache.add(name);
      this.trustedCacheHash = this.trustedCache.hashCode();
   }

   @Nullable
   public UUID removeTrusted(int index) {
      UUID uuid = null;
      if (index >= 0 && index < this.trusted.size()) {
         uuid = this.trusted.remove(index);
         this.dirty = true;
      }

      if (index >= 0 && index < this.trustedCache.size()) {
         this.trustedCache.remove(index);
         this.trustedCacheHash = this.trustedCache.hashCode();
      }

      return uuid;
   }
}
