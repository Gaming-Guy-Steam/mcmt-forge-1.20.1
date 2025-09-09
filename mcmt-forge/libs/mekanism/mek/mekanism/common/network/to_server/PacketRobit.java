package mekanism.common.network.to_server;

import java.util.Collections;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.stream.Stream;
import mekanism.api.MekanismAPI;
import mekanism.api.robit.RobitSkin;
import mekanism.api.security.ISecurityUtils;
import mekanism.api.text.TextComponentUtil;
import mekanism.common.entity.EntityRobit;
import mekanism.common.entity.RobitPrideSkinData;
import mekanism.common.network.BasePacketHandler;
import mekanism.common.network.IMekanismPacket;
import mekanism.common.registries.MekanismRobitSkins;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.entity.player.Player;
import net.minecraftforge.network.NetworkEvent.Context;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class PacketRobit implements IMekanismPacket {
   private static final Map<String, List<ResourceKey<RobitSkin>>> EASTER_EGGS = Map.of(
      "sara", getPrideSkins(RobitPrideSkinData.TRANS, RobitPrideSkinData.LESBIAN)
   );
   private final PacketRobit.RobitPacketType activeType;
   private final int entityId;
   private final String name;
   private final ResourceKey<RobitSkin> skin;

   private static List<ResourceKey<RobitSkin>> getPrideSkins(RobitPrideSkinData... prideSkinData) {
      return Stream.of(prideSkinData).map(MekanismRobitSkins.PRIDE_SKINS::get).toList();
   }

   public PacketRobit(PacketRobit.RobitPacketType type, EntityRobit robit) {
      this(type, robit.m_19879_(), null, null);
   }

   public PacketRobit(EntityRobit robit, @NotNull String name) {
      this(PacketRobit.RobitPacketType.NAME, robit, name, null);
   }

   public PacketRobit(EntityRobit robit, @NotNull ResourceKey<RobitSkin> skin) {
      this(PacketRobit.RobitPacketType.SKIN, robit, null, skin);
   }

   private PacketRobit(PacketRobit.RobitPacketType type, EntityRobit robit, @Nullable String name, @Nullable ResourceKey<RobitSkin> skin) {
      this(type, robit.m_19879_(), name, skin);
   }

   private PacketRobit(PacketRobit.RobitPacketType type, int entityId, @Nullable String name, @Nullable ResourceKey<RobitSkin> skin) {
      this.activeType = type;
      this.entityId = entityId;
      this.name = name;
      this.skin = skin;
   }

   @Override
   public void handle(Context context) {
      Player player = context.getSender();
      if (player != null) {
         EntityRobit robit = (EntityRobit)player.m_9236_().m_6815_(this.entityId);
         if (robit != null && ISecurityUtils.INSTANCE.canAccess(player, robit)) {
            if (this.activeType == PacketRobit.RobitPacketType.GO_HOME) {
               robit.goHome();
            } else if (this.activeType == PacketRobit.RobitPacketType.FOLLOW) {
               robit.setFollowing(!robit.getFollowing());
            } else if (this.activeType == PacketRobit.RobitPacketType.DROP_PICKUP) {
               robit.setDropPickup(!robit.getDropPickup());
            } else if (this.activeType == PacketRobit.RobitPacketType.NAME) {
               robit.m_6593_(TextComponentUtil.getString(this.name));
               if (robit.getSkin() == MekanismRobitSkins.BASE) {
                  List<ResourceKey<RobitSkin>> skins = EASTER_EGGS.getOrDefault(this.name.toLowerCase(Locale.ROOT), Collections.emptyList());
                  if (!skins.isEmpty()) {
                     robit.setSkin(skins.get(robit.m_9236_().f_46441_.m_188503_(skins.size())), null);
                  }
               }
            } else if (this.activeType == PacketRobit.RobitPacketType.SKIN) {
               robit.setSkin(this.skin, player);
            }
         }
      }
   }

   @Override
   public void encode(FriendlyByteBuf buffer) {
      buffer.m_130068_(this.activeType);
      buffer.m_130130_(this.entityId);
      if (this.activeType == PacketRobit.RobitPacketType.NAME) {
         buffer.m_130070_(this.name);
      } else if (this.activeType == PacketRobit.RobitPacketType.SKIN) {
         buffer.m_236858_(this.skin);
      }
   }

   public static PacketRobit decode(FriendlyByteBuf buffer) {
      PacketRobit.RobitPacketType activeType = (PacketRobit.RobitPacketType)buffer.m_130066_(PacketRobit.RobitPacketType.class);
      int entityId = buffer.m_130242_();
      String name = null;
      ResourceKey<RobitSkin> skin = null;
      if (activeType == PacketRobit.RobitPacketType.NAME) {
         name = BasePacketHandler.readString(buffer).trim();
      } else if (activeType == PacketRobit.RobitPacketType.SKIN) {
         skin = buffer.m_236801_(MekanismAPI.ROBIT_SKIN_REGISTRY_NAME);
      }

      return new PacketRobit(activeType, entityId, name, skin);
   }

   public static enum RobitPacketType {
      GO_HOME,
      FOLLOW,
      DROP_PICKUP,
      NAME,
      SKIN;
   }
}
