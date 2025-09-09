package mekanism.common.tile;

import java.util.UUID;
import mekanism.api.IContentsListener;
import mekanism.api.security.ISecurityUtils;
import mekanism.api.security.SecurityMode;
import mekanism.common.Mekanism;
import mekanism.common.capabilities.Capabilities;
import mekanism.common.capabilities.holder.slot.IInventorySlotHolder;
import mekanism.common.capabilities.holder.slot.InventorySlotHelper;
import mekanism.common.inventory.container.ISecurityContainer;
import mekanism.common.inventory.slot.SecurityInventorySlot;
import mekanism.common.lib.frequency.FrequencyType;
import mekanism.common.lib.security.SecurityFrequency;
import mekanism.common.network.to_client.PacketSecurityUpdate;
import mekanism.common.registries.MekanismBlocks;
import mekanism.common.tile.base.TileEntityMekanism;
import mekanism.common.tile.interfaces.IBoundingBlock;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.Vec3i;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.capabilities.ForgeCapabilities;
import net.minecraftforge.server.ServerLifecycleHooks;
import org.jetbrains.annotations.NotNull;

public class TileEntitySecurityDesk extends TileEntityMekanism implements IBoundingBlock {
   private SecurityInventorySlot unlockSlot;
   private SecurityInventorySlot lockSlot;

   public TileEntitySecurityDesk(BlockPos pos, BlockState state) {
      super(MekanismBlocks.SECURITY_DESK, pos, state);
      this.addDisabledCapabilities(new Capability[]{ForgeCapabilities.ITEM_HANDLER, Capabilities.SECURITY_OBJECT});
   }

   @NotNull
   @Override
   protected IInventorySlotHolder getInitialInventory(IContentsListener listener) {
      InventorySlotHelper builder = InventorySlotHelper.forSide(this::getDirection);
      builder.addSlot(this.unlockSlot = SecurityInventorySlot.unlock(this::getOwnerUUID, listener, 146, 18));
      builder.addSlot(this.lockSlot = SecurityInventorySlot.lock(listener, 146, 97));
      return builder.build();
   }

   @Override
   protected void onUpdateServer() {
      super.onUpdateServer();
      SecurityFrequency frequency = this.getFreq();
      UUID ownerUUID = this.getOwnerUUID();
      if (ownerUUID != null && frequency != null) {
         this.unlockSlot.unlock(ownerUUID);
         this.lockSlot.lock(ownerUUID, frequency);
      }
   }

   public void toggleOverride() {
      SecurityFrequency frequency = this.getFreq();
      if (frequency != null) {
         frequency.setOverridden(!frequency.isOverridden());
         this.markForSave();
         Mekanism.packetHandler().sendToAll(new PacketSecurityUpdate(frequency));
         this.validateAccess();
      }
   }

   private void validateAccess() {
      if (this.m_58898_()) {
         MinecraftServer server = this.getWorldNN().m_7654_();
         if (server != null) {
            for (ServerPlayer player : server.m_6846_().m_11314_()) {
               if (player.f_36096_ instanceof ISecurityContainer container && !ISecurityUtils.INSTANCE.canAccess(player, container.getSecurityObject())) {
                  player.m_6915_();
               }
            }
         }
      }
   }

   public void removeTrusted(int index) {
      SecurityFrequency frequency = this.getFreq();
      if (frequency != null) {
         UUID removed = frequency.removeTrusted(index);
         this.markForSave();
         if (removed != null && this.m_58898_()) {
            MinecraftServer server = this.getWorldNN().m_7654_();
            if (server != null) {
               Player player = server.m_6846_().m_11259_(removed);
               if (player != null
                  && player.f_36096_ instanceof ISecurityContainer container
                  && ISecurityUtils.INSTANCE.canAccess(player, container.getSecurityObject())) {
                  player.m_6915_();
               }
            }
         }
      }
   }

   public void setSecurityDeskMode(SecurityMode mode) {
      SecurityFrequency frequency = this.getFreq();
      if (frequency != null) {
         SecurityMode old = frequency.getSecurityMode();
         if (old != mode) {
            frequency.setSecurityMode(mode);
            this.markForSave();
            Mekanism.packetHandler().sendToAll(new PacketSecurityUpdate(frequency));
            if (ISecurityUtils.INSTANCE.moreRestrictive(old, mode)) {
               this.validateAccess();
            }
         }
      }
   }

   public void addTrusted(String name) {
      SecurityFrequency frequency = this.getFreq();
      if (frequency != null) {
         ServerLifecycleHooks.getCurrentServer().m_129927_().m_10996_(name).ifPresent(profile -> frequency.addTrusted(profile.getId(), profile.getName()));
      }
   }

   public SecurityFrequency getFreq() {
      return this.getFrequency(FrequencyType.SECURITY);
   }

   @Override
   public boolean isOffsetCapabilityDisabled(@NotNull Capability<?> capability, Direction side, @NotNull Vec3i offset) {
      return true;
   }
}
