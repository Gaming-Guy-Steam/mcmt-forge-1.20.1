package mekanism.common.network.to_server;

import mekanism.api.Action;
import mekanism.api.AutomationType;
import mekanism.api.Coord4D;
import mekanism.api.energy.IEnergyContainer;
import mekanism.api.math.FloatingLong;
import mekanism.common.Mekanism;
import mekanism.common.content.teleporter.TeleporterFrequency;
import mekanism.common.item.ItemPortableTeleporter;
import mekanism.common.lib.frequency.Frequency;
import mekanism.common.lib.frequency.FrequencyType;
import mekanism.common.network.IMekanismPacket;
import mekanism.common.network.to_client.PacketPortalFX;
import mekanism.common.tile.TileEntityTeleporter;
import mekanism.common.util.StorageUtils;
import mekanism.common.util.WorldUtils;
import net.minecraft.core.BlockPos;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraftforge.network.NetworkEvent.Context;
import net.minecraftforge.server.ServerLifecycleHooks;

public class PacketPortableTeleporterTeleport implements IMekanismPacket {
   private final Frequency.FrequencyIdentity identity;
   private final InteractionHand currentHand;

   public PacketPortableTeleporterTeleport(InteractionHand hand, Frequency.FrequencyIdentity identity) {
      this.currentHand = hand;
      this.identity = identity;
   }

   @Override
   public void handle(Context context) {
      ServerPlayer player = context.getSender();
      if (player != null) {
         ItemStack stack = player.m_21120_(this.currentHand);
         if (!stack.m_41619_() && stack.m_41720_() instanceof ItemPortableTeleporter) {
            TeleporterFrequency found = FrequencyType.TELEPORTER.getFrequency(this.identity, player.m_20148_());
            if (found == null) {
               return;
            }

            Coord4D coords = found.getClosestCoords(new Coord4D(player));
            if (coords != null) {
               Level teleWorld = ServerLifecycleHooks.getCurrentServer().m_129880_(coords.dimension);
               TileEntityTeleporter teleporter = WorldUtils.getTileEntity(TileEntityTeleporter.class, teleWorld, coords.getPos());
               if (teleporter != null) {
                  if (!player.m_7500_()) {
                     FloatingLong energyCost = TileEntityTeleporter.calculateEnergyCost(player, teleWorld, coords);
                     IEnergyContainer energyContainer = StorageUtils.getEnergyContainer(stack, 0);
                     if (energyContainer == null || energyContainer.extract(energyCost, Action.SIMULATE, AutomationType.MANUAL).smallerThan(energyCost)) {
                        return;
                     }

                     energyContainer.extract(energyCost, Action.EXECUTE, AutomationType.MANUAL);
                  }

                  try {
                     teleporter.didTeleport.add(player.m_20148_());
                     teleporter.teleDelay = 5;
                     player.f_8906_.f_9737_ = 0;
                     player.m_6915_();
                     Mekanism.packetHandler().sendToAllTracking(new PacketPortalFX(player.m_20183_()), player.m_9236_(), coords.getPos());
                     if (player.m_20159_()) {
                        player.m_8127_();
                     }

                     double oldX = player.m_20185_();
                     double oldY = player.m_20186_();
                     double oldZ = player.m_20189_();
                     Level oldWorld = player.m_9236_();
                     BlockPos teleporterTargetPos = teleporter.getTeleporterTargetPos();
                     TileEntityTeleporter.teleportEntityTo(player, teleWorld, teleporterTargetPos);
                     TileEntityTeleporter.alignPlayer(player, teleporterTargetPos, teleporter);
                     if (player.m_9236_() != oldWorld || player.m_20275_(oldX, oldY, oldZ) >= 25.0) {
                        oldWorld.m_6263_(null, oldX, oldY, oldZ, SoundEvents.f_11852_, SoundSource.PLAYERS, 1.0F, 1.0F);
                     }

                     player.m_9236_()
                        .m_6263_(null, player.m_20185_(), player.m_20186_(), player.m_20189_(), SoundEvents.f_11852_, SoundSource.PLAYERS, 1.0F, 1.0F);
                     teleporter.sendTeleportParticles();
                  } catch (Exception var16) {
                  }
               }
            }
         }
      }
   }

   @Override
   public void encode(FriendlyByteBuf buffer) {
      buffer.m_130068_(this.currentHand);
      FrequencyType.TELEPORTER.getIdentitySerializer().write(buffer, this.identity);
   }

   public static PacketPortableTeleporterTeleport decode(FriendlyByteBuf buffer) {
      return new PacketPortableTeleporterTeleport(
         (InteractionHand)buffer.m_130066_(InteractionHand.class), FrequencyType.TELEPORTER.getIdentitySerializer().read(buffer)
      );
   }
}
