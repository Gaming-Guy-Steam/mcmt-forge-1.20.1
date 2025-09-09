package mekanism.common.content.network.transmitter;

import java.util.Collection;
import java.util.Collections;
import java.util.EnumSet;
import java.util.Set;
import java.util.UUID;
import mekanism.api.Chunk3D;
import mekanism.api.Coord4D;
import mekanism.api.text.EnumColor;
import mekanism.common.MekanismLang;
import mekanism.common.lib.transmitter.CompatibleTransmitterValidator;
import mekanism.common.lib.transmitter.ConnectionType;
import mekanism.common.lib.transmitter.DynamicNetwork;
import mekanism.common.lib.transmitter.TransmissionType;
import mekanism.common.lib.transmitter.TransmitterNetworkRegistry;
import mekanism.common.lib.transmitter.acceptor.AbstractAcceptorCache;
import mekanism.common.lib.transmitter.acceptor.AcceptorCache;
import mekanism.common.tile.interfaces.ITileWrapper;
import mekanism.common.tile.transmitter.TileEntityTransmitter;
import mekanism.common.util.EnumUtils;
import mekanism.common.util.NBTUtils;
import mekanism.common.util.WorldUtils;
import mekanism.common.util.text.BooleanStateDisplay;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraftforge.common.util.LazyOptional;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public abstract class Transmitter<ACCEPTOR, NETWORK extends DynamicNetwork<ACCEPTOR, NETWORK, TRANSMITTER>, TRANSMITTER extends Transmitter<ACCEPTOR, NETWORK, TRANSMITTER>>
   implements ITileWrapper {
   private ConnectionType[] connectionTypes = new ConnectionType[]{
      ConnectionType.NORMAL, ConnectionType.NORMAL, ConnectionType.NORMAL, ConnectionType.NORMAL, ConnectionType.NORMAL, ConnectionType.NORMAL
   };
   private final AbstractAcceptorCache<ACCEPTOR, ?> acceptorCache;
   public byte currentTransmitterConnections = 0;
   private final TileEntityTransmitter transmitterTile;
   private final Set<TransmissionType> supportedTransmissionTypes;
   protected boolean redstoneReactive;
   private boolean redstonePowered;
   private boolean redstoneSet;
   private NETWORK theNetwork = (NETWORK)null;
   private boolean orphaned = true;
   private boolean isUpgrading;

   public static boolean connectionMapContainsSide(byte connections, Direction side) {
      return connectionMapContainsSide(connections, side.ordinal());
   }

   private static boolean connectionMapContainsSide(byte connections, int sideOrdinal) {
      byte tester = (byte)(1 << sideOrdinal);
      return (connections & tester) > 0;
   }

   private static byte setConnectionBit(byte connections, boolean toSet, Direction side) {
      return (byte)(connections & ~((byte)(1 << side.ordinal())) | (byte)((toSet ? 1 : 0) << side.ordinal()));
   }

   private static ConnectionType getConnectionType(Direction side, byte allConnections, byte transmitterConnections, ConnectionType[] types) {
      int sideOrdinal = side.ordinal();
      if (!connectionMapContainsSide(allConnections, sideOrdinal)) {
         return ConnectionType.NONE;
      } else {
         return connectionMapContainsSide(transmitterConnections, sideOrdinal) ? ConnectionType.NORMAL : types[sideOrdinal];
      }
   }

   public Transmitter(TileEntityTransmitter transmitterTile, TransmissionType... transmissionTypes) {
      this.transmitterTile = transmitterTile;
      this.acceptorCache = this.createAcceptorCache();
      this.supportedTransmissionTypes = EnumSet.noneOf(TransmissionType.class);
      Collections.addAll(this.supportedTransmissionTypes, transmissionTypes);
   }

   protected AbstractAcceptorCache<ACCEPTOR, ?> createAcceptorCache() {
      return new AcceptorCache<>(this, this.getTransmitterTile());
   }

   public AbstractAcceptorCache<ACCEPTOR, ?> getAcceptorCache() {
      return this.acceptorCache;
   }

   public TileEntityTransmitter getTransmitterTile() {
      return this.transmitterTile;
   }

   public boolean isUpgrading() {
      return this.isUpgrading;
   }

   public ConnectionType[] getConnectionTypesRaw() {
      return this.connectionTypes;
   }

   public void setConnectionTypesRaw(@NotNull ConnectionType[] connectionTypes) {
      if (this.connectionTypes.length != connectionTypes.length) {
         throw new IllegalArgumentException("Mismatched connection types length");
      } else {
         this.connectionTypes = connectionTypes;
      }
   }

   public ConnectionType getConnectionTypeRaw(@NotNull Direction side) {
      return this.connectionTypes[side.ordinal()];
   }

   public void setConnectionTypeRaw(@NotNull Direction side, @NotNull ConnectionType type) {
      int index = side.ordinal();
      ConnectionType old = this.connectionTypes[index];
      if (old != type) {
         this.connectionTypes[index] = type;
         this.getTransmitterTile().sideChanged(side, old, type);
      }
   }

   @Override
   public BlockPos getTilePos() {
      return this.transmitterTile.getTilePos();
   }

   @Override
   public Level getTileWorld() {
      return this.transmitterTile.getTileWorld();
   }

   @Override
   public Coord4D getTileCoord() {
      return this.transmitterTile.getTileCoord();
   }

   @Override
   public Chunk3D getTileChunk() {
      return this.transmitterTile.getTileChunk();
   }

   public boolean isRemote() {
      return this.transmitterTile.isRemote();
   }

   protected TRANSMITTER getTransmitter() {
      return (TRANSMITTER)this;
   }

   public NETWORK getTransmitterNetwork() {
      return this.theNetwork;
   }

   public void setTransmitterNetwork(NETWORK network) {
      this.setTransmitterNetwork(network, true);
   }

   public boolean setTransmitterNetwork(NETWORK network, boolean requestNow) {
      if (this.theNetwork == network) {
         return false;
      } else {
         if (this.isRemote() && this.theNetwork != null) {
            this.theNetwork.removeTransmitter(this.getTransmitter());
         }

         this.theNetwork = network;
         this.orphaned = this.theNetwork == null;
         if (this.isRemote()) {
            if (this.theNetwork != null) {
               this.theNetwork.addTransmitter(this.getTransmitter());
            }
         } else {
            if (!requestNow) {
               return true;
            }

            this.requestsUpdate();
         }

         return false;
      }
   }

   public boolean hasTransmitterNetwork() {
      return !this.isOrphan() && this.getTransmitterNetwork() != null;
   }

   public abstract NETWORK createEmptyNetworkWithID(UUID networkID);

   public abstract NETWORK createNetworkByMerging(Collection<NETWORK> toMerge);

   public boolean isValid() {
      return !this.getTransmitterTile().m_58901_() && this.getTransmitterTile().isLoaded();
   }

   public CompatibleTransmitterValidator<ACCEPTOR, NETWORK, TRANSMITTER> getNewOrphanValidator() {
      return new CompatibleTransmitterValidator<>();
   }

   public boolean isOrphan() {
      return this.orphaned;
   }

   public void setOrphan(boolean nowOrphaned) {
      this.orphaned = nowOrphaned;
   }

   public Set<TransmissionType> getSupportedTransmissionTypes() {
      return this.supportedTransmissionTypes;
   }

   public boolean supportsTransmissionType(Transmitter<?, ?, ?> transmitter) {
      return transmitter.getSupportedTransmissionTypes().stream().anyMatch(this.supportedTransmissionTypes::contains);
   }

   public boolean supportsTransmissionType(TileEntityTransmitter transmitter) {
      return this.supportsTransmissionType(transmitter.getTransmitter());
   }

   @NotNull
   public LazyOptional<ACCEPTOR> getAcceptor(Direction side) {
      return this.acceptorCache.getCachedAcceptor(side);
   }

   public boolean handlesRedstone() {
      return true;
   }

   public final boolean isRedstoneActivated() {
      if (this.handlesRedstone() && this.redstoneReactive) {
         if (!this.redstoneSet) {
            this.setRedstoneState();
         }

         return this.redstonePowered;
      } else {
         return false;
      }
   }

   public byte getPossibleTransmitterConnections() {
      byte connections = 0;
      if (this.isRedstoneActivated()) {
         return connections;
      } else {
         for (Direction side : EnumUtils.DIRECTIONS) {
            TileEntityTransmitter tile = WorldUtils.getTileEntity(TileEntityTransmitter.class, this.getTileWorld(), this.getTilePos().m_121945_(side));
            if (tile != null && this.isValidTransmitter(tile, side)) {
               connections = (byte)(connections | 1 << side.ordinal());
            }
         }

         return connections;
      }
   }

   private boolean getPossibleAcceptorConnection(Direction side) {
      if (this.isRedstoneActivated()) {
         return false;
      } else {
         BlockEntity tile = WorldUtils.getTileEntity(this.getTileWorld(), this.getTilePos().m_121945_(side));
         if (this.canConnectMutual(side, tile) && this.isValidAcceptor(tile, side)) {
            return true;
         } else {
            this.acceptorCache.invalidateCachedAcceptor(side);
            return false;
         }
      }
   }

   private boolean getPossibleTransmitterConnection(Direction side) {
      if (this.isRedstoneActivated()) {
         return false;
      } else {
         TileEntityTransmitter tile = WorldUtils.getTileEntity(TileEntityTransmitter.class, this.getTileWorld(), this.getTilePos().m_121945_(side));
         return tile != null && this.isValidTransmitter(tile, side);
      }
   }

   public byte getPossibleAcceptorConnections() {
      byte connections = 0;
      if (this.isRedstoneActivated()) {
         return connections;
      } else {
         for (Direction side : EnumUtils.DIRECTIONS) {
            BlockPos offset = this.getTilePos().m_121945_(side);
            BlockEntity tile = WorldUtils.getTileEntity(this.getTileWorld(), offset);
            if (this.canConnectMutual(side, tile)) {
               if (!this.isRemote() && !WorldUtils.isBlockLoaded(this.getTileWorld(), offset)) {
                  this.getTransmitterTile().setForceUpdate();
                  continue;
               }

               if (this.isValidAcceptor(tile, side)) {
                  connections = (byte)(connections | 1 << side.ordinal());
                  continue;
               }
            }

            this.acceptorCache.invalidateCachedAcceptor(side);
         }

         return connections;
      }
   }

   public byte getAllCurrentConnections() {
      return (byte)(this.currentTransmitterConnections | this.acceptorCache.currentAcceptorConnections);
   }

   public boolean isValidTransmitter(TileEntityTransmitter transmitter, Direction side) {
      return this.isValidTransmitterBasic(transmitter, side);
   }

   public boolean isValidTransmitterBasic(TileEntityTransmitter transmitter, Direction side) {
      return this.supportsTransmissionType(transmitter) && this.canConnectMutual(side, transmitter);
   }

   public boolean canConnectToAcceptor(Direction side) {
      ConnectionType type = this.getConnectionTypeRaw(side);
      return type == ConnectionType.NORMAL || type == ConnectionType.PUSH;
   }

   public boolean isValidAcceptor(BlockEntity tile, Direction side) {
      return !(tile instanceof TileEntityTransmitter transmitter && this.supportsTransmissionType(transmitter));
   }

   public boolean canConnectMutual(Direction side, @Nullable BlockEntity cachedTile) {
      if (!this.canConnect(side)) {
         return false;
      } else {
         if (cachedTile == null) {
            cachedTile = WorldUtils.getTileEntity(this.getTileWorld(), this.getTilePos().m_121945_(side));
         }

         return !(cachedTile instanceof TileEntityTransmitter transmitter && !transmitter.getTransmitter().canConnect(side.m_122424_()));
      }
   }

   public boolean canConnectMutual(Direction side, @Nullable TRANSMITTER cachedTransmitter) {
      return !this.canConnect(side) ? false : cachedTransmitter == null || cachedTransmitter.canConnect(side.m_122424_());
   }

   public boolean canConnect(Direction side) {
      if (this.getConnectionTypeRaw(side) == ConnectionType.NONE) {
         return false;
      } else if (this.handlesRedstone() && this.redstoneReactive) {
         if (!this.redstoneSet) {
            this.setRedstoneState();
         }

         return !this.redstonePowered;
      } else {
         return true;
      }
   }

   public void requestsUpdate() {
      this.getTransmitterTile().sendUpdatePacket();
   }

   @NotNull
   public CompoundTag getReducedUpdateTag(CompoundTag updateTag) {
      updateTag.m_128344_("connections", this.currentTransmitterConnections);
      updateTag.m_128344_("acceptors", this.acceptorCache.currentAcceptorConnections);

      for (Direction direction : EnumUtils.DIRECTIONS) {
         NBTUtils.writeEnum(updateTag, "side" + direction.ordinal(), this.getConnectionTypeRaw(direction));
      }

      if (this.hasTransmitterNetwork()) {
         updateTag.m_128362_("network", this.getTransmitterNetwork().getUUID());
      }

      return updateTag;
   }

   public void handleUpdateTag(@NotNull CompoundTag tag) {
      NBTUtils.setByteIfPresent(tag, "connections", connections -> this.currentTransmitterConnections = connections);
      NBTUtils.setByteIfPresent(tag, "acceptors", acceptors -> this.acceptorCache.currentAcceptorConnections = acceptors);

      for (Direction direction : EnumUtils.DIRECTIONS) {
         NBTUtils.setEnumIfPresent(tag, "side" + direction.ordinal(), ConnectionType::byIndexStatic, type -> this.setConnectionTypeRaw(direction, type));
      }

      NBTUtils.setUUIDIfPresentElse(tag, "network", networkID -> {
         if (!this.hasTransmitterNetwork() || !this.getTransmitterNetwork().getUUID().equals(networkID)) {
            DynamicNetwork<?, ?, ?> clientNetwork = TransmitterNetworkRegistry.getInstance().getClientNetwork(networkID);
            if (clientNetwork == null) {
               NETWORK network = this.createEmptyNetworkWithID(networkID);
               network.register();
               this.setTransmitterNetwork(network);
               this.handleContentsUpdateTag(network, tag);
            } else {
               this.updateClientNetwork((NETWORK)clientNetwork);
            }
         }
      }, () -> this.setTransmitterNetwork(null));
   }

   protected void updateClientNetwork(@NotNull NETWORK network) {
      network.register();
      this.setTransmitterNetwork(network);
   }

   protected void handleContentsUpdateTag(@NotNull NETWORK network, @NotNull CompoundTag tag) {
   }

   public void read(@NotNull CompoundTag nbtTags) {
      this.redstoneReactive = nbtTags.m_128471_("redstone");

      for (Direction direction : EnumUtils.DIRECTIONS) {
         NBTUtils.setEnumIfPresent(
            nbtTags, "connection" + direction.ordinal(), ConnectionType::byIndexStatic, type -> this.setConnectionTypeRaw(direction, type)
         );
      }
   }

   @NotNull
   public CompoundTag write(@NotNull CompoundTag nbtTags) {
      nbtTags.m_128379_("redstone", this.redstoneReactive);

      for (Direction direction : EnumUtils.DIRECTIONS) {
         NBTUtils.writeEnum(nbtTags, "connection" + direction.ordinal(), this.getConnectionTypeRaw(direction));
      }

      return nbtTags;
   }

   private void recheckRedstone() {
      if (this.handlesRedstone()) {
         boolean previouslyPowered = this.redstonePowered;
         this.setRedstoneState();
         if (previouslyPowered != this.redstonePowered) {
            this.markDirtyTransmitters();
            this.getTransmitterTile().redstoneChanged(this.redstonePowered);
         }
      }
   }

   private void setRedstoneState() {
      this.redstonePowered = this.redstoneReactive && this.transmitterTile.m_58898_() && WorldUtils.isGettingPowered(this.getTileWorld(), this.getTilePos());
      this.redstoneSet = true;
   }

   public void refreshConnections() {
      if (!this.isRemote()) {
         this.recheckRedstone();
         byte possibleTransmitters = this.getPossibleTransmitterConnections();
         byte possibleAcceptors = this.getPossibleAcceptorConnections();
         byte newlyEnabledTransmitters = 0;
         boolean sendDesc = false;
         if ((possibleTransmitters | possibleAcceptors) != this.getAllCurrentConnections()) {
            sendDesc = true;
            if (possibleTransmitters != this.currentTransmitterConnections) {
               newlyEnabledTransmitters = (byte)(possibleTransmitters ^ this.currentTransmitterConnections);
               newlyEnabledTransmitters = (byte)(newlyEnabledTransmitters & ~this.currentTransmitterConnections);
            }
         }

         this.currentTransmitterConnections = possibleTransmitters;
         this.acceptorCache.currentAcceptorConnections = possibleAcceptors;
         if (newlyEnabledTransmitters != 0) {
            this.recheckConnections(newlyEnabledTransmitters);
         }

         if (sendDesc) {
            this.getTransmitterTile().sendUpdatePacket();
         }
      }
   }

   public void refreshConnections(Direction side) {
      if (!this.isRemote()) {
         boolean possibleTransmitter = this.getPossibleTransmitterConnection(side);
         boolean possibleAcceptor = this.getPossibleAcceptorConnection(side);
         boolean transmitterChanged = false;
         boolean sendDesc = false;
         if ((possibleTransmitter || possibleAcceptor) != connectionMapContainsSide(this.getAllCurrentConnections(), side)) {
            sendDesc = true;
            if (possibleTransmitter != connectionMapContainsSide(this.currentTransmitterConnections, side)) {
               transmitterChanged = possibleTransmitter;
            }
         }

         this.currentTransmitterConnections = setConnectionBit(this.currentTransmitterConnections, possibleTransmitter, side);
         this.acceptorCache.currentAcceptorConnections = setConnectionBit(this.acceptorCache.currentAcceptorConnections, possibleAcceptor, side);
         if (transmitterChanged) {
            this.recheckConnection(side);
         }

         if (sendDesc) {
            this.getTransmitterTile().sendUpdatePacket();
         }
      }
   }

   protected void recheckConnections(byte newlyEnabledTransmitters) {
      if (!this.hasTransmitterNetwork()) {
         for (Direction side : EnumUtils.DIRECTIONS) {
            if (connectionMapContainsSide(newlyEnabledTransmitters, side)) {
               TileEntityTransmitter tile = WorldUtils.getTileEntity(TileEntityTransmitter.class, this.getTileWorld(), this.getTilePos().m_121945_(side));
               if (tile != null) {
                  tile.getTransmitter().refreshConnections(side.m_122424_());
               }
            }
         }
      }
   }

   protected void recheckConnection(Direction side) {
   }

   public void onModeChange(Direction side) {
      this.markDirtyAcceptor(side);
      if (this.getPossibleTransmitterConnections() != this.currentTransmitterConnections) {
         this.markDirtyTransmitters();
      }

      this.getTransmitterTile().m_6596_();
   }

   public void onNeighborTileChange(Direction side) {
      this.refreshConnections(side);
   }

   public void onNeighborBlockChange(Direction side) {
      if (this.handlesRedstone() && this.redstoneReactive) {
         this.refreshConnections();
      } else {
         this.refreshConnections(side);
      }
   }

   protected void markDirtyTransmitters() {
      this.notifyTileChange();
      if (this.hasTransmitterNetwork()) {
         TransmitterNetworkRegistry.invalidateTransmitter(this.getTransmitter());
      }
   }

   public void markDirtyAcceptor(Direction side) {
      if (this.hasTransmitterNetwork()) {
         this.getTransmitterNetwork().acceptorChanged(this.getTransmitter(), side);
      }
   }

   public void remove() {
      this.acceptorCache.clear();
   }

   public ConnectionType getConnectionType(Direction side) {
      return getConnectionType(side, this.getAllCurrentConnections(), this.currentTransmitterConnections, this.connectionTypes);
   }

   public Set<Direction> getConnections(ConnectionType type) {
      Set<Direction> sides = null;

      for (Direction side : EnumUtils.DIRECTIONS) {
         if (this.getConnectionType(side) == type) {
            if (sides == null) {
               sides = EnumSet.noneOf(Direction.class);
            }

            sides.add(side);
         }
      }

      return sides == null ? Collections.emptySet() : sides;
   }

   public InteractionResult onConfigure(Player player, Direction side) {
      return InteractionResult.PASS;
   }

   public InteractionResult onRightClick(Player player, Direction side) {
      if (this.handlesRedstone()) {
         this.redstoneReactive = !this.redstoneReactive;
         this.refreshConnections();
         this.notifyTileChange();
         player.m_5661_(
            MekanismLang.REDSTONE_SENSITIVITY
               .translateColored(EnumColor.GRAY, new Object[]{EnumColor.INDIGO, BooleanStateDisplay.OnOff.of(this.redstoneReactive)}),
            true
         );
      }

      return InteractionResult.SUCCESS;
   }

   public void notifyTileChange() {
      WorldUtils.notifyLoadedNeighborsOfTileChange(this.getTileWorld(), this.getTilePos());
   }

   public abstract void takeShare();

   public void validateAndTakeShare() {
      this.takeShare();
   }

   public void startUpgrading() {
      this.isUpgrading = true;
      this.takeShare();
      this.setTransmitterNetwork(null);
   }
}
