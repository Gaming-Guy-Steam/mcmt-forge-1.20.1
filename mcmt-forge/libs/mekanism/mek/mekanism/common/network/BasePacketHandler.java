package mekanism.common.network;

import java.util.Map;
import java.util.Optional;
import java.util.function.BiConsumer;
import java.util.function.Function;
import java.util.function.IntFunction;
import mekanism.api.functions.TriConsumer;
import mekanism.common.Mekanism;
import mekanism.common.config.MekanismConfig;
import mekanism.common.lib.Version;
import mekanism.common.lib.math.Range3D;
import mekanism.common.lib.transmitter.DynamicBufferedNetwork;
import net.minecraft.core.BlockPos;
import net.minecraft.core.SectionPos;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.server.players.PlayerList;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.common.util.FakePlayer;
import net.minecraftforge.network.NetworkDirection;
import net.minecraftforge.network.PacketDistributor;
import net.minecraftforge.network.NetworkRegistry.ChannelBuilder;
import net.minecraftforge.network.simple.SimpleChannel;
import net.minecraftforge.server.ServerLifecycleHooks;
import org.jetbrains.annotations.Nullable;

public abstract class BasePacketHandler {
   private int index = 0;

   protected static SimpleChannel createChannel(ResourceLocation name, Version version) {
      String protocolVersion = version.toString();
      return ChannelBuilder.named(name)
         .clientAcceptedVersions(protocolVersion::equals)
         .serverAcceptedVersions(protocolVersion::equals)
         .networkProtocolVersion(() -> protocolVersion)
         .simpleChannel();
   }

   public static String readString(FriendlyByteBuf buffer) {
      return buffer.m_130136_(32767);
   }

   public static Vec3 readVector3d(FriendlyByteBuf buffer) {
      return new Vec3(buffer.readDouble(), buffer.readDouble(), buffer.readDouble());
   }

   public static void writeVector3d(FriendlyByteBuf buffer, Vec3 vector) {
      buffer.writeDouble(vector.m_7096_());
      buffer.writeDouble(vector.m_7098_());
      buffer.writeDouble(vector.m_7094_());
   }

   public static <TYPE> void writeOptional(FriendlyByteBuf buffer, @Nullable TYPE value, BiConsumer<FriendlyByteBuf, TYPE> writer) {
      if (value == null) {
         buffer.writeBoolean(false);
      } else {
         buffer.writeBoolean(true);
         writer.accept(buffer, value);
      }
   }

   @Nullable
   public static <TYPE> TYPE readOptional(FriendlyByteBuf buffer, Function<FriendlyByteBuf, TYPE> reader) {
      return buffer.readBoolean() ? reader.apply(buffer) : null;
   }

   public static <TYPE> void writeArray(FriendlyByteBuf buffer, TYPE[] array, BiConsumer<TYPE, FriendlyByteBuf> writer) {
      buffer.m_130130_(array.length);

      for (TYPE element : array) {
         writer.accept(element, buffer);
      }
   }

   public static <TYPE> TYPE[] readArray(FriendlyByteBuf buffer, IntFunction<TYPE[]> arrayFactory, Function<FriendlyByteBuf, TYPE> reader) {
      TYPE[] array = (TYPE[])arrayFactory.apply(buffer.m_130242_());

      for (int element = 0; element < array.length; element++) {
         array[element] = reader.apply(buffer);
      }

      return array;
   }

   public static <KEY, VALUE> void writeMap(FriendlyByteBuf buffer, Map<KEY, VALUE> map, TriConsumer<KEY, VALUE, FriendlyByteBuf> writer) {
      buffer.m_130130_(map.size());
      map.forEach((key, value) -> writer.accept((KEY)key, (VALUE)value, buffer));
   }

   public static <KEY, VALUE, MAP extends Map<KEY, VALUE>> MAP readMap(
      FriendlyByteBuf buffer, IntFunction<MAP> mapFactory, Function<FriendlyByteBuf, KEY> keyReader, Function<FriendlyByteBuf, VALUE> valueReader
   ) {
      int elements = buffer.m_130242_();
      MAP map = (MAP)mapFactory.apply(elements);

      for (int element = 0; element < elements; element++) {
         map.put(keyReader.apply(buffer), valueReader.apply(buffer));
      }

      return map;
   }

   public static void log(String logFormat, Object... params) {
      if (MekanismConfig.general.logPackets.get()) {
         Mekanism.logger.info(logFormat, params);
      }
   }

   protected abstract SimpleChannel getChannel();

   public abstract void initialize();

   protected <MSG extends IMekanismPacket> void registerClientToServer(Class<MSG> type, Function<FriendlyByteBuf, MSG> decoder) {
      this.registerMessage(type, decoder, NetworkDirection.PLAY_TO_SERVER);
   }

   protected <MSG extends IMekanismPacket> void registerServerToClient(Class<MSG> type, Function<FriendlyByteBuf, MSG> decoder) {
      this.registerMessage(type, decoder, NetworkDirection.PLAY_TO_CLIENT);
   }

   private <MSG extends IMekanismPacket> void registerMessage(Class<MSG> type, Function<FriendlyByteBuf, MSG> decoder, NetworkDirection networkDirection) {
      this.getChannel().registerMessage(this.index++, type, IMekanismPacket::encode, decoder, IMekanismPacket::handle, Optional.of(networkDirection));
   }

   public <MSG> void sendTo(MSG message, ServerPlayer player) {
      if (!(player instanceof FakePlayer)) {
         this.getChannel().send(PacketDistributor.PLAYER.with(() -> player), message);
      }
   }

   public <MSG> void sendToAll(MSG message) {
      this.getChannel().send(PacketDistributor.ALL.noArg(), message);
   }

   public <MSG> void sendToAllIfLoaded(MSG message) {
      if (ServerLifecycleHooks.getCurrentServer() != null) {
         this.sendToAll(message);
      }
   }

   public <MSG> void sendToDimension(MSG message, ResourceKey<Level> dimension) {
      this.getChannel().send(PacketDistributor.DIMENSION.with(() -> dimension), message);
   }

   public <MSG> void sendToServer(MSG message) {
      this.getChannel().sendToServer(message);
   }

   public <MSG> void sendToAllTracking(MSG message, Entity entity) {
      this.getChannel().send(PacketDistributor.TRACKING_ENTITY.with(() -> entity), message);
   }

   public <MSG> void sendToAllTrackingAndSelf(MSG message, Entity entity) {
      this.getChannel().send(PacketDistributor.TRACKING_ENTITY_AND_SELF.with(() -> entity), message);
   }

   public <MSG> void sendToAllTracking(MSG message, BlockEntity tile) {
      this.sendToAllTracking(message, tile.m_58904_(), tile.m_58899_());
   }

   public <MSG> void sendToAllTracking(MSG message, Level world, BlockPos pos) {
      if (world instanceof ServerLevel level) {
         level.m_7726_().f_8325_.m_183262_(new ChunkPos(pos), false).forEach(p -> this.sendTo(message, p));
      } else {
         this.getChannel()
            .send(
               PacketDistributor.TRACKING_CHUNK.with(() -> world.m_6325_(SectionPos.m_123171_(pos.m_123341_()), SectionPos.m_123171_(pos.m_123343_()))),
               message
            );
      }
   }

   public <MSG> void sendToReceivers(MSG message, DynamicBufferedNetwork<?, ?, ?, ?> network) {
      try {
         MinecraftServer server = ServerLifecycleHooks.getCurrentServer();
         if (server != null) {
            Range3D range = network.getPacketRange();
            PlayerList playerList = server.m_6846_();
            int radius = playerList.m_11312_() * 16;

            for (ServerPlayer player : playerList.m_11314_()) {
               if (range.dimension() == player.m_9236_().m_46472_()) {
                  BlockPos playerPosition = player.m_20183_();
                  int playerX = playerPosition.m_123341_();
                  int playerZ = playerPosition.m_123343_();
                  if (playerX + radius + 1.99999 > range.xMin()
                     && range.xMax() + 0.99999 > playerX - radius
                     && playerZ + radius + 1.99999 > range.zMin()
                     && range.zMax() + 0.99999 > playerZ - radius) {
                     this.sendTo(message, player);
                  }
               }
            }
         }
      } catch (Exception var12) {
      }
   }
}
