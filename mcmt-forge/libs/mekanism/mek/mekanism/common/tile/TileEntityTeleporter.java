package mekanism.common.tile;

import it.unimi.dsi.fastutil.longs.Long2ObjectArrayMap;
import it.unimi.dsi.fastutil.longs.Long2ObjectMap;
import it.unimi.dsi.fastutil.objects.Object2BooleanMap;
import it.unimi.dsi.fastutil.objects.Object2BooleanOpenHashMap;
import it.unimi.dsi.fastutil.objects.ObjectOpenHashSet;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;
import java.util.function.Function;
import mekanism.api.Action;
import mekanism.api.AutomationType;
import mekanism.api.Coord4D;
import mekanism.api.IContentsListener;
import mekanism.api.math.FloatingLong;
import mekanism.api.text.EnumColor;
import mekanism.common.Mekanism;
import mekanism.common.advancements.MekanismCriteriaTriggers;
import mekanism.common.capabilities.Capabilities;
import mekanism.common.capabilities.energy.MachineEnergyContainer;
import mekanism.common.capabilities.holder.energy.EnergyContainerHelper;
import mekanism.common.capabilities.holder.energy.IEnergyContainerHolder;
import mekanism.common.capabilities.holder.slot.IInventorySlotHolder;
import mekanism.common.capabilities.holder.slot.InventorySlotHelper;
import mekanism.common.capabilities.resolver.BasicCapabilityResolver;
import mekanism.common.config.MekanismConfig;
import mekanism.common.content.teleporter.TeleporterFrequency;
import mekanism.common.integration.computer.ComputerException;
import mekanism.common.integration.computer.SpecialComputerMethodWrapper;
import mekanism.common.integration.computer.annotation.ComputerMethod;
import mekanism.common.integration.computer.annotation.WrappingComputerMethod;
import mekanism.common.inventory.container.MekanismContainer;
import mekanism.common.inventory.container.sync.SyncableByte;
import mekanism.common.inventory.slot.EnergyInventorySlot;
import mekanism.common.lib.chunkloading.IChunkLoader;
import mekanism.common.lib.frequency.Frequency;
import mekanism.common.lib.frequency.FrequencyType;
import mekanism.common.network.to_client.PacketPortalFX;
import mekanism.common.registries.MekanismBlocks;
import mekanism.common.tile.base.SubstanceType;
import mekanism.common.tile.base.TileEntityMekanism;
import mekanism.common.tile.component.TileComponentChunkLoader;
import mekanism.common.util.EnumUtils;
import mekanism.common.util.MekanismUtils;
import mekanism.common.util.NBTUtils;
import mekanism.common.util.WorldUtils;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.Direction.Axis;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.protocol.game.ClientboundMoveVehiclePacket;
import net.minecraft.network.protocol.game.ClientboundSetPassengersPacket;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerChunkCache;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.chunk.ChunkAccess;
import net.minecraft.world.level.portal.PortalInfo;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.common.util.FakePlayer;
import net.minecraftforge.common.util.ITeleporter;
import net.minecraftforge.entity.PartEntity;
import net.minecraftforge.server.ServerLifecycleHooks;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class TileEntityTeleporter extends TileEntityMekanism implements IChunkLoader {
   private static final TileEntityTeleporter.TeleportInfo NO_FRAME = new TileEntityTeleporter.TeleportInfo((byte)2, null, Collections.emptyList());
   private static final TileEntityTeleporter.TeleportInfo NO_LINK = new TileEntityTeleporter.TeleportInfo((byte)3, null, Collections.emptyList());
   private static final TileEntityTeleporter.TeleportInfo NOT_ENOUGH_ENERGY = new TileEntityTeleporter.TeleportInfo((byte)4, null, Collections.emptyList());
   public final Set<UUID> didTeleport = new ObjectOpenHashSet();
   private AABB teleportBounds;
   public int teleDelay = 0;
   public boolean shouldRender;
   @Nullable
   private Direction frameDirection;
   private boolean frameRotated;
   private EnumColor color;
   public byte status = 0;
   private final TileComponentChunkLoader<TileEntityTeleporter> chunkLoaderComponent = new TileComponentChunkLoader<>(this);
   private MachineEnergyContainer<TileEntityTeleporter> energyContainer;
   @WrappingComputerMethod(
      wrapper = SpecialComputerMethodWrapper.ComputerIInventorySlotWrapper.class,
      methodNames = {"getEnergyItem"},
      docPlaceholder = "energy slot"
   )
   EnergyInventorySlot energySlot;

   public TileEntityTeleporter(BlockPos pos, BlockState state) {
      super(MekanismBlocks.TELEPORTER, pos, state);
      this.frequencyComponent.track(FrequencyType.TELEPORTER, true, true, false);
      this.addCapabilityResolver(BasicCapabilityResolver.constant(Capabilities.CONFIG_CARD, this));
      this.cacheCoord();
   }

   @NotNull
   @Override
   protected IEnergyContainerHolder getInitialEnergyContainers(IContentsListener listener) {
      EnergyContainerHelper builder = EnergyContainerHelper.forSide(this::getDirection);
      builder.addContainer(this.energyContainer = MachineEnergyContainer.input(this, listener));
      return builder.build();
   }

   @NotNull
   @Override
   protected IInventorySlotHolder getInitialInventory(IContentsListener listener) {
      InventorySlotHelper builder = InventorySlotHelper.forSide(this::getDirection);
      builder.addSlot(this.energySlot = EnergyInventorySlot.fillOrConvert(this.energyContainer, this::m_58904_, listener, 153, 7));
      return builder.build();
   }

   public static void alignPlayer(ServerPlayer player, BlockPos target, TileEntityTeleporter teleporter) {
      Direction side = null;
      if (teleporter.frameDirection != null && teleporter.frameDirection.m_122434_().m_122479_()) {
         side = teleporter.frameDirection;
      } else {
         for (Direction iterSide : EnumUtils.HORIZONTAL_DIRECTIONS) {
            if (player.m_9236_().m_46859_(target.m_121945_(iterSide))) {
               side = iterSide;
               break;
            }
         }
      }

      float yaw = player.m_146908_();
      if (side != null) {
         switch (side) {
            case NORTH:
               yaw = 180.0F;
               break;
            case SOUTH:
               yaw = 0.0F;
               break;
            case WEST:
               yaw = 90.0F;
               break;
            case EAST:
               yaw = 270.0F;
         }
      }

      player.f_8906_.m_9774_(player.m_20185_(), player.m_20186_(), player.m_20189_(), yaw, player.m_146909_());
   }

   @Override
   protected void onUpdateServer() {
      super.onUpdateServer();
      if (this.teleportBounds == null && this.frameDirection != null) {
         this.resetBounds();
      }

      TeleporterFrequency freq = this.getFrequency(FrequencyType.TELEPORTER);
      TileEntityTeleporter.TeleportInfo teleportInfo = this.canTeleport(freq);
      this.status = teleportInfo.status();
      if (this.status == 1 && this.teleDelay == 0 && MekanismUtils.canFunction(this)) {
         this.teleport(freq, teleportInfo);
      }

      if (this.teleDelay == 0 && this.teleportBounds != null && !this.didTeleport.isEmpty()) {
         this.cleanTeleportCache();
      }

      boolean prevShouldRender = this.shouldRender;
      this.shouldRender = this.status == 1 || this.status > 4;
      EnumColor prevColor = this.color;
      this.color = freq == null ? null : freq.getColor();
      if (this.shouldRender != prevShouldRender) {
         WorldUtils.notifyLoadedNeighborsOfTileChange(this.f_58857_, this.m_58899_());
         this.sendUpdatePacket();
      } else if (this.color != prevColor) {
         this.sendUpdatePacket();
      }

      this.teleDelay = Math.max(0, this.teleDelay - 1);
      this.energySlot.fillContainerOrConvert();
   }

   @Nullable
   private Coord4D getClosest(@Nullable TeleporterFrequency frequency) {
      return frequency == null ? null : frequency.getClosestCoords(this.getTileCoord());
   }

   private void cleanTeleportCache() {
      List<UUID> inTeleporter = this.f_58857_.m_45976_(Entity.class, this.teleportBounds).stream().<UUID>map(Entity::m_20148_).toList();
      if (inTeleporter.isEmpty()) {
         this.didTeleport.clear();
      } else {
         this.didTeleport.removeIf(id -> !inTeleporter.contains(id));
      }
   }

   private void resetBounds() {
      if (this.frameDirection == null) {
         this.teleportBounds = null;
      } else {
         this.teleportBounds = this.getTeleporterBoundingBox(this.frameDirection);
      }
   }

   private TileEntityTeleporter.TeleportInfo canTeleport(@Nullable TeleporterFrequency frequency) {
      Direction direction = this.getFrameDirection();
      if (direction == null) {
         this.frameDirection = null;
         return NO_FRAME;
      } else {
         if (this.frameDirection != direction) {
            this.frameDirection = direction;
            this.resetBounds();
         }

         Coord4D closestCoords = this.getClosest(frequency);
         if (closestCoords != null && this.f_58857_ != null) {
            boolean sameDimension = this.f_58857_.m_46472_() == closestCoords.dimension;
            Level targetWorld;
            if (sameDimension) {
               targetWorld = this.f_58857_;
            } else {
               MinecraftServer server = this.f_58857_.m_7654_();
               if (server == null) {
                  return NO_LINK;
               }

               targetWorld = server.m_129880_(closestCoords.dimension);
               if (targetWorld == null) {
                  return NO_LINK;
               }
            }

            List<Entity> toTeleport = this.getToTeleport(sameDimension);
            FloatingLong sum = FloatingLong.ZERO;

            for (Entity entity : toTeleport) {
               sum = sum.plusEqual(calculateEnergyCost(entity, targetWorld, closestCoords));
            }

            return this.energyContainer.extract(sum, Action.SIMULATE, AutomationType.INTERNAL).smallerThan(sum)
               ? NOT_ENOUGH_ENERGY
               : new TileEntityTeleporter.TeleportInfo((byte)1, closestCoords, toTeleport);
         } else {
            return NO_LINK;
         }
      }
   }

   public BlockPos getTeleporterTargetPos() {
      if (this.frameDirection == null) {
         return this.f_58858_.m_7494_();
      } else {
         return this.frameDirection == Direction.DOWN ? this.f_58858_.m_6625_(2) : this.f_58858_.m_121945_(this.frameDirection);
      }
   }

   public void sendTeleportParticles() {
      BlockPos teleporterTargetPos = this.getTeleporterTargetPos();
      Direction offsetDirection;
      if (this.frameDirection != null && !this.frameDirection.m_122434_().m_122478_()) {
         offsetDirection = this.frameDirection;
      } else {
         offsetDirection = Direction.UP;
      }

      Mekanism.packetHandler().sendToAllTracking(new PacketPortalFX(teleporterTargetPos, offsetDirection), this.f_58857_, teleporterTargetPos);
   }

   private void teleport(TeleporterFrequency frequency, TileEntityTeleporter.TeleportInfo teleportInfo) {
      if (teleportInfo.closest != null && this.f_58857_ != null && !teleportInfo.toTeleport.isEmpty()) {
         MinecraftServer currentServer = ServerLifecycleHooks.getCurrentServer();
         boolean sameDimension = this.f_58857_.m_46472_() == teleportInfo.closest.dimension;
         Level teleWorld = (Level)(sameDimension ? this.f_58857_ : currentServer.m_129880_(teleportInfo.closest.dimension));
         BlockPos closestPos = teleportInfo.closest.getPos();
         TileEntityTeleporter teleporter = WorldUtils.getTileEntity(TileEntityTeleporter.class, teleWorld, closestPos);
         if (teleporter != null) {
            Set<Coord4D> activeCoords = frequency.getActiveCoords();
            BlockPos teleporterTargetPos = teleporter.getTeleporterTargetPos();

            for (Entity entity : teleportInfo.toTeleport) {
               this.markTeleported(teleporter, entity, sameDimension);
               teleporter.teleDelay = 5;
               FloatingLong energyCost = calculateEnergyCost(entity, teleWorld, teleportInfo.closest);
               double oldX = entity.m_20185_();
               double oldY = entity.m_20186_();
               double oldZ = entity.m_20189_();
               Entity teleportedEntity = teleportEntityTo(entity, teleWorld, teleporterTargetPos);
               if (teleportedEntity instanceof ServerPlayer player) {
                  alignPlayer(player, teleporterTargetPos, teleporter);
                  MekanismCriteriaTriggers.TELEPORT.m_222618_(player);
               }

               for (Coord4D coords : activeCoords) {
                  Level world = (Level)(this.f_58857_.m_46472_() == coords.dimension ? this.f_58857_ : currentServer.m_129880_(coords.dimension));
                  TileEntityTeleporter tile = WorldUtils.getTileEntity(TileEntityTeleporter.class, world, coords.getPos());
                  if (tile != null) {
                     tile.sendTeleportParticles();
                  }
               }

               this.energyContainer.extract(energyCost, Action.EXECUTE, AutomationType.INTERNAL);
               if (teleportedEntity != null) {
                  if (this.f_58857_ != teleportedEntity.m_9236_() || teleportedEntity.m_20275_(oldX, oldY, oldZ) >= 25.0) {
                     this.f_58857_.m_6263_(null, oldX, oldY, oldZ, SoundEvents.f_11852_, entity.m_5720_(), 1.0F, 1.0F);
                  }

                  teleportedEntity.m_9236_()
                     .m_6263_(
                        null,
                        teleportedEntity.m_20185_(),
                        teleportedEntity.m_20186_(),
                        teleportedEntity.m_20189_(),
                        SoundEvents.f_11852_,
                        teleportedEntity.m_5720_(),
                        1.0F,
                        1.0F
                     );
               }
            }
         }
      }
   }

   private void markTeleported(TileEntityTeleporter teleporter, Entity entity, boolean sameDimension) {
      if (sameDimension || entity.m_6072_()) {
         teleporter.didTeleport.add(entity.m_20148_());

         for (Entity passenger : entity.m_20197_()) {
            this.markTeleported(teleporter, passenger, sameDimension);
         }
      }
   }

   @Nullable
   public static Entity teleportEntityTo(Entity entity, Level targetWorld, BlockPos target) {
      if (entity.m_9236_().m_46472_() == targetWorld.m_46472_()) {
         entity.m_6021_(target.m_123341_() + 0.5, target.m_123342_(), target.m_123343_() + 0.5);
         if (!entity.m_20197_().isEmpty()) {
            ((ServerChunkCache)entity.m_9236_().m_7726_()).m_8445_(entity, new ClientboundSetPassengersPacket(entity));
            Entity controller = entity.m_6688_();
            if (controller != entity && controller instanceof ServerPlayer player && !(controller instanceof FakePlayer) && player.f_8906_ != null) {
               player.f_8906_.m_9829_(new ClientboundMoveVehiclePacket(entity));
            }
         }

         return entity;
      } else {
         final Vec3 destination = new Vec3(target.m_123341_() + 0.5, target.m_123342_(), target.m_123343_() + 0.5);
         final List<Entity> passengers = entity.m_20197_();
         return entity.changeDimension((ServerLevel)targetWorld, new ITeleporter() {
            public Entity placeEntity(Entity entity, ServerLevel currentWorld, ServerLevel destWorld, float yaw, Function<Boolean, Entity> repositionEntity) {
               Entity repositionedEntity = repositionEntity.apply(false);
               if (repositionedEntity != null) {
                  for (Entity passenger : passengers) {
                     TileEntityTeleporter.teleportPassenger(destWorld, destination, repositionedEntity, passenger);
                  }
               }

               return repositionedEntity;
            }

            public PortalInfo getPortalInfo(Entity entity, ServerLevel destWorld, Function<ServerLevel, PortalInfo> defaultPortalInfo) {
               return new PortalInfo(destination, entity.m_20184_(), entity.m_146908_(), entity.m_146909_());
            }

            public boolean playTeleportSound(ServerPlayer player, ServerLevel sourceWorld, ServerLevel destWorld) {
               return false;
            }
         });
      }
   }

   private static void teleportPassenger(ServerLevel destWorld, Vec3 destination, Entity repositionedEntity, Entity passenger) {
      if (passenger.m_6072_()) {
         final List<Entity> passengers = passenger.m_20197_();
         passenger.changeDimension(destWorld, new ITeleporter() {
            public Entity placeEntity(Entity entity, ServerLevel currentWorld, ServerLevel destWorldx, float yaw, Function<Boolean, Entity> repositionEntity) {
               boolean invulnerable = entity.m_20147_();
               entity.m_20331_(true);
               Entity repositionedPassenger = repositionEntity.apply(false);
               if (repositionedPassenger != null) {
                  repositionedPassenger.m_7998_(repositionedEntity, true);

                  for (Entity passengerx : passengers) {
                     TileEntityTeleporter.teleportPassenger(destWorldx, destination, repositionedPassenger, passengerx);
                  }

                  repositionedPassenger.m_20331_(invulnerable);
               }

               entity.m_20331_(invulnerable);
               return repositionedPassenger;
            }

            public PortalInfo getPortalInfo(Entity entity, ServerLevel destWorldx, Function<ServerLevel, PortalInfo> defaultPortalInfo) {
               return new PortalInfo(destination, entity.m_20184_(), entity.m_146908_(), entity.m_146909_());
            }

            public boolean playTeleportSound(ServerPlayer player, ServerLevel sourceWorld, ServerLevel destWorldx) {
               return false;
            }
         });
      }
   }

   private List<Entity> getToTeleport(boolean sameDimension) {
      return this.f_58857_ != null && this.teleportBounds != null
         ? this.f_58857_
            .m_6443_(
               Entity.class,
               this.teleportBounds,
               entity -> !entity.m_5833_()
                  && !entity.m_20159_()
                  && !(entity instanceof PartEntity)
                  && (sameDimension || entity.m_6072_())
                  && !this.didTeleport.contains(entity.m_20148_())
            )
         : Collections.emptyList();
   }

   @Nullable
   public static FloatingLong calculateEnergyCost(Entity entity, Coord4D coords) {
      MinecraftServer currentServer = ServerLifecycleHooks.getCurrentServer();
      if (currentServer != null) {
         Level targetWorld = currentServer.m_129880_(coords.dimension);
         if (targetWorld != null) {
            return calculateEnergyCost(entity, targetWorld, coords);
         }
      }

      return null;
   }

   @NotNull
   public static FloatingLong calculateEnergyCost(Entity entity, Level targetWorld, Coord4D coords) {
      FloatingLong energyCost = MekanismConfig.usage.teleporterBase.get();
      boolean sameDimension = entity.m_9236_().m_46472_() == coords.dimension;
      if (sameDimension) {
         energyCost = energyCost.add(
            MekanismConfig.usage.teleporterDistance.get().multiply(Math.sqrt(entity.m_20275_(coords.getX(), coords.getY(), coords.getZ())))
         );
      } else {
         double currentScale = entity.m_9236_().m_6042_().f_63859_();
         double targetScale = targetWorld.m_6042_().f_63859_();
         double yDifference = entity.m_20186_() - coords.getY();
         double xDifference;
         double zDifference;
         if (currentScale <= targetScale) {
            double scale = currentScale / targetScale;
            xDifference = entity.m_20185_() * scale - coords.getX();
            zDifference = entity.m_20189_() * scale - coords.getZ();
         } else {
            double inverseScale = targetScale / currentScale;
            xDifference = entity.m_20185_() - coords.getX() * inverseScale;
            zDifference = entity.m_20189_() - coords.getZ() * inverseScale;
         }

         double distance = Mth.m_184648_(xDifference, yDifference, zDifference);
         energyCost = energyCost.add(MekanismConfig.usage.teleporterDimensionPenalty.get())
            .plusEqual(MekanismConfig.usage.teleporterDistance.get().multiply(distance));
      }

      Set<Entity> passengers = new HashSet<>();
      fillIndirectPassengers(entity, sameDimension, passengers);
      int passengerCount = passengers.size();
      return passengerCount > 0 ? energyCost.multiply((long)passengerCount) : energyCost;
   }

   private static void fillIndirectPassengers(Entity base, boolean sameDimension, Set<Entity> passengers) {
      for (Entity entity : base.m_20197_()) {
         if (sameDimension || entity.m_6072_()) {
            passengers.add(entity);
            fillIndirectPassengers(entity, sameDimension, passengers);
         }
      }
   }

   @Nullable
   private Direction getFrameDirection() {
      Long2ObjectMap<ChunkAccess> chunkMap = new Long2ObjectArrayMap(3);
      Object2BooleanMap<BlockPos> cachedIsFrame = new Object2BooleanOpenHashMap();

      for (Direction direction : EnumUtils.DIRECTIONS) {
         if (this.hasFrame(chunkMap, cachedIsFrame, direction, false)) {
            this.frameRotated = false;
            return direction;
         }

         if (this.hasFrame(chunkMap, cachedIsFrame, direction, true)) {
            this.frameRotated = true;
            return direction;
         }
      }

      return null;
   }

   private boolean hasFrame(Long2ObjectMap<ChunkAccess> chunkMap, Object2BooleanMap<BlockPos> cachedIsFrame, Direction direction, boolean rotated) {
      int alternatingX = 0;
      int alternatingY = 0;
      int alternatingZ = 0;
      if (rotated) {
         if (direction.m_122434_() == Axis.Z) {
            alternatingX = 1;
         } else {
            alternatingZ = 1;
         }
      } else if (direction.m_122434_() == Axis.Y) {
         alternatingX = 1;
      } else {
         alternatingY = 1;
      }

      int xComponent = direction.m_122429_();
      int yComponent = direction.m_122430_();
      int zComponent = direction.m_122431_();
      return this.isFramePair(chunkMap, cachedIsFrame, 0, alternatingX, 0, alternatingY, 0, alternatingZ)
         && this.isFrame(chunkMap, cachedIsFrame, 3 * xComponent, 3 * yComponent, 3 * zComponent)
         && this.isFramePair(chunkMap, cachedIsFrame, xComponent, alternatingX, yComponent, alternatingY, zComponent, alternatingZ)
         && this.isFramePair(chunkMap, cachedIsFrame, 2 * xComponent, alternatingX, 2 * yComponent, alternatingY, 2 * zComponent, alternatingZ)
         && this.isFramePair(chunkMap, cachedIsFrame, 3 * xComponent, alternatingX, 3 * yComponent, alternatingY, 3 * zComponent, alternatingZ);
   }

   private boolean isFramePair(
      Long2ObjectMap<ChunkAccess> chunkMap,
      Object2BooleanMap<BlockPos> cachedIsFrame,
      int xOffset,
      int alternatingX,
      int yOffset,
      int alternatingY,
      int zOffset,
      int alternatingZ
   ) {
      return this.isFrame(chunkMap, cachedIsFrame, xOffset - alternatingX, yOffset - alternatingY, zOffset - alternatingZ)
         && this.isFrame(chunkMap, cachedIsFrame, xOffset + alternatingX, yOffset + alternatingY, zOffset + alternatingZ);
   }

   private boolean isFrame(Long2ObjectMap<ChunkAccess> chunkMap, Object2BooleanMap<BlockPos> cachedIsFrame, int xOffset, int yOffset, int zOffset) {
      return cachedIsFrame.computeIfAbsent(this.f_58858_.m_7918_(xOffset, yOffset, zOffset), pos -> {
         Optional<BlockState> state = WorldUtils.getBlockState(this.f_58857_, chunkMap, pos);
         return state.filter(blockState -> blockState.m_60713_(MekanismBlocks.TELEPORTER_FRAME.getBlock())).isPresent();
      });
   }

   @Nullable
   public Direction frameDirection() {
      return this.frameDirection == null ? this.getFrameDirection() : this.frameDirection;
   }

   public boolean frameRotated() {
      return this.frameRotated;
   }

   @NotNull
   public AABB getRenderBoundingBox() {
      Direction frameDirection = this.getFrameDirection();
      return frameDirection == null ? new AABB(this.f_58858_, this.f_58858_.m_7918_(1, 1, 1)) : this.getTeleporterBoundingBox(frameDirection);
   }

   private AABB getTeleporterBoundingBox(@NotNull Direction frameDirection) {
      return switch (frameDirection) {
         case NORTH -> new AABB(this.f_58858_, this.f_58858_.m_7918_(1, 1, -2));
         case SOUTH -> new AABB(this.f_58858_.m_122019_(), this.f_58858_.m_7918_(1, 1, 3));
         case WEST -> new AABB(this.f_58858_, this.f_58858_.m_7918_(-2, 1, 1));
         case EAST -> new AABB(this.f_58858_.m_122029_(), this.f_58858_.m_7918_(3, 1, 1));
         case UP -> new AABB(this.f_58858_.m_7494_(), this.f_58858_.m_7918_(1, 3, 1));
         case DOWN -> new AABB(this.f_58858_, this.f_58858_.m_7918_(1, -2, 1));
         default -> throw new IncompatibleClassChangeError();
      };
   }

   @Override
   public TileComponentChunkLoader<TileEntityTeleporter> getChunkLoader() {
      return this.chunkLoaderComponent;
   }

   @Override
   public Set<ChunkPos> getChunkSet() {
      return Collections.singleton(new ChunkPos(this.m_58899_()));
   }

   @Override
   public int getRedstoneLevel() {
      return this.shouldRender ? 15 : 0;
   }

   @Override
   protected boolean makesComparatorDirty(@Nullable SubstanceType type) {
      return false;
   }

   @Override
   public int getCurrentRedstoneLevel() {
      return this.getRedstoneLevel();
   }

   public MachineEnergyContainer<TileEntityTeleporter> getEnergyContainer() {
      return this.energyContainer;
   }

   public EnumColor getColor() {
      return this.color;
   }

   @Override
   public void addContainerTrackers(MekanismContainer container) {
      super.addContainerTrackers(container);
      container.track(SyncableByte.create(() -> this.status, value -> this.status = value));
   }

   @NotNull
   @Override
   public CompoundTag getReducedUpdateTag() {
      CompoundTag updateTag = super.getReducedUpdateTag();
      updateTag.m_128379_("rendering", this.shouldRender);
      if (this.color != null) {
         NBTUtils.writeEnum(updateTag, "color", this.color);
      }

      return updateTag;
   }

   @Override
   public void handleUpdateTag(@NotNull CompoundTag tag) {
      super.handleUpdateTag(tag);
      NBTUtils.setBooleanIfPresent(tag, "rendering", value -> this.shouldRender = value);
      if (tag.m_128425_("color", 3)) {
         this.color = EnumColor.byIndexStatic(tag.m_128451_("color"));
      } else {
         this.color = null;
      }
   }

   @ComputerMethod(
      methodDescription = "Lists public frequencies"
   )
   Collection<TeleporterFrequency> getFrequencies() {
      return FrequencyType.TELEPORTER.getManagerWrapper().getPublicManager().getFrequencies();
   }

   @ComputerMethod
   boolean hasFrequency() {
      TeleporterFrequency frequency = this.getFrequency(FrequencyType.TELEPORTER);
      return frequency != null && frequency.isValid() && !frequency.isRemoved();
   }

   @ComputerMethod(
      methodDescription = "Requires a frequency to be selected"
   )
   TeleporterFrequency getFrequency() throws ComputerException {
      TeleporterFrequency frequency = this.getFrequency(FrequencyType.TELEPORTER);
      if (frequency != null && frequency.isValid() && !frequency.isRemoved()) {
         return frequency;
      } else {
         throw new ComputerException("No frequency is currently selected.");
      }
   }

   @ComputerMethod(
      requiresPublicSecurity = true,
      methodDescription = "Requires a public frequency to exist"
   )
   void setFrequency(String name) throws ComputerException {
      this.validateSecurityIsPublic();
      TeleporterFrequency frequency = FrequencyType.TELEPORTER.getManagerWrapper().getPublicManager().getFrequency(name);
      if (frequency == null) {
         throw new ComputerException("No public teleporter frequency with name '%s' found.", name);
      } else {
         this.setFrequency(FrequencyType.TELEPORTER, frequency.getIdentity(), this.getOwnerUUID());
      }
   }

   @ComputerMethod(
      requiresPublicSecurity = true,
      methodDescription = "Requires frequency to not already exist and for it to be public so that it can make it as the player who owns the block. Also sets the frequency after creation"
   )
   void createFrequency(String name) throws ComputerException {
      this.validateSecurityIsPublic();
      TeleporterFrequency frequency = FrequencyType.TELEPORTER.getManagerWrapper().getPublicManager().getFrequency(name);
      if (frequency != null) {
         throw new ComputerException("Unable to create public teleporter frequency with name '%s' as one already exists.", name);
      } else {
         this.setFrequency(FrequencyType.TELEPORTER, new Frequency.FrequencyIdentity(name, true), this.getOwnerUUID());
      }
   }

   @ComputerMethod(
      methodDescription = "Requires a frequency to be selected"
   )
   EnumColor getFrequencyColor() throws ComputerException {
      return this.getFrequency().getColor();
   }

   @ComputerMethod(
      requiresPublicSecurity = true,
      methodDescription = "Requires a frequency to be selected"
   )
   void setFrequencyColor(EnumColor color) throws ComputerException {
      this.validateSecurityIsPublic();
      this.getFrequency().setColor(color);
   }

   @ComputerMethod(
      requiresPublicSecurity = true,
      methodDescription = "Requires a frequency to be selected"
   )
   void incrementFrequencyColor() throws ComputerException {
      this.validateSecurityIsPublic();
      TeleporterFrequency frequency = this.getFrequency();
      frequency.setColor(frequency.getColor().getNext());
   }

   @ComputerMethod(
      requiresPublicSecurity = true,
      methodDescription = "Requires a frequency to be selected"
   )
   void decrementFrequencyColor() throws ComputerException {
      this.validateSecurityIsPublic();
      TeleporterFrequency frequency = this.getFrequency();
      frequency.setColor(frequency.getColor().getPrevious());
   }

   @ComputerMethod(
      methodDescription = "Requires a frequency to be selected"
   )
   Set<Coord4D> getActiveTeleporters() throws ComputerException {
      return this.getFrequency().getActiveCoords();
   }

   @ComputerMethod
   String getStatus() {
      if (this.hasFrequency()) {
         return switch (this.status) {
            case 1 -> "ready";
            case 2 -> "no frame";
            default -> "no link";
            case 4 -> "needs energy";
         };
      } else {
         return "no frequency";
      }
   }

   private record TeleportInfo(byte status, @Nullable Coord4D closest, List<Entity> toTeleport) {
   }
}
