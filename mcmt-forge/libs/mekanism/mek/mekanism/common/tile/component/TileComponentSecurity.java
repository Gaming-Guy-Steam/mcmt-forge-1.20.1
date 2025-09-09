package mekanism.common.tile.component;

import java.util.UUID;
import mekanism.api.security.ISecurityUtils;
import mekanism.api.security.SecurityMode;
import mekanism.common.integration.computer.annotation.ComputerMethod;
import mekanism.common.inventory.container.MekanismContainer;
import mekanism.common.inventory.container.sync.SyncableEnum;
import mekanism.common.lib.frequency.Frequency;
import mekanism.common.lib.frequency.FrequencyType;
import mekanism.common.lib.security.SecurityFrequency;
import mekanism.common.tile.base.TileEntityMekanism;
import mekanism.common.util.MekanismUtils;
import mekanism.common.util.NBTUtils;
import net.minecraft.nbt.CompoundTag;

public class TileComponentSecurity implements ITileComponent {
   public final TileEntityMekanism tile;
   private UUID ownerUUID;
   private String ownerName;
   private SecurityMode securityMode = SecurityMode.PUBLIC;

   public TileComponentSecurity(TileEntityMekanism tile) {
      this.tile = tile;
      tile.addComponent(this);
      tile.getFrequencyComponent().track(FrequencyType.SECURITY, true, false, true);
   }

   public SecurityFrequency getFrequency() {
      return this.tile.getFrequency(FrequencyType.SECURITY);
   }

   @ComputerMethod
   public UUID getOwnerUUID() {
      return this.ownerUUID;
   }

   public void setOwnerUUID(UUID uuid) {
      this.ownerUUID = uuid;
      if (this.ownerUUID == null) {
         this.tile.getFrequencyComponent().unsetFrequency(FrequencyType.SECURITY);
      } else {
         this.tile.setFrequency(FrequencyType.SECURITY, new Frequency.FrequencyIdentity(this.ownerUUID, true), this.ownerUUID);
      }
   }

   @ComputerMethod
   public String getOwnerName() {
      return this.ownerName;
   }

   public SecurityMode getMode() {
      return this.securityMode;
   }

   public void setMode(SecurityMode mode) {
      if (this.securityMode != mode) {
         SecurityMode old = this.securityMode;
         this.securityMode = mode;
         this.tile.onSecurityChanged(old, this.securityMode);
         if (!this.tile.isRemote()) {
            this.tile.markForSave();
         }
      }
   }

   @Override
   public void read(CompoundTag nbtTags) {
      if (nbtTags.m_128425_("componentSecurity", 10)) {
         CompoundTag securityNBT = nbtTags.m_128469_("componentSecurity");
         NBTUtils.setEnumIfPresent(securityNBT, "securityMode", SecurityMode::byIndexStatic, mode -> this.securityMode = mode);
         NBTUtils.setUUIDIfPresent(securityNBT, "owner", uuid -> this.ownerUUID = uuid);
      }
   }

   @Override
   public void write(CompoundTag nbtTags) {
      CompoundTag securityNBT = new CompoundTag();
      NBTUtils.writeEnum(securityNBT, "securityMode", this.securityMode);
      if (this.ownerUUID != null) {
         securityNBT.m_128362_("owner", this.ownerUUID);
      }

      nbtTags.m_128365_("componentSecurity", securityNBT);
   }

   @Override
   public void trackForMainContainer(MekanismContainer container) {
      container.track(SyncableEnum.create(SecurityMode::byIndexStatic, SecurityMode.PUBLIC, this::getMode, this::setMode));
   }

   @Override
   public void addToUpdateTag(CompoundTag updateTag) {
      if (this.ownerUUID != null) {
         updateTag.m_128362_("owner", this.ownerUUID);
         updateTag.m_128359_("ownerName", MekanismUtils.getLastKnownUsername(this.ownerUUID));
      }
   }

   @Override
   public void readFromUpdateTag(CompoundTag updateTag) {
      NBTUtils.setUUIDIfPresent(updateTag, "owner", uuid -> this.ownerUUID = uuid);
      NBTUtils.setStringIfPresent(updateTag, "ownerName", name -> this.ownerName = name);
   }

   @ComputerMethod(
      nameOverride = "getSecurityMode"
   )
   SecurityMode getComputerSecurityMode() {
      return ISecurityUtils.INSTANCE.getSecurityMode(this.tile, this.tile.isRemote());
   }
}
