package mekanism.common.tile.component;

import com.mojang.datafixers.util.Pair;
import com.mojang.logging.LogUtils;
import it.unimi.dsi.fastutil.longs.LongIterator;
import it.unimi.dsi.fastutil.longs.LongOpenHashSet;
import it.unimi.dsi.fastutil.longs.LongSet;
import it.unimi.dsi.fastutil.longs.LongSets;
import java.util.Objects;
import java.util.Set;
import java.util.Map.Entry;
import mekanism.api.Upgrade;
import mekanism.common.config.MekanismConfig;
import mekanism.common.lib.chunkloading.IChunkLoader;
import mekanism.common.tile.base.TileEntityMekanism;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraftforge.common.world.ForgeChunkManager;
import net.minecraftforge.common.world.ForgeChunkManager.LoadingValidationCallback;
import net.minecraftforge.common.world.ForgeChunkManager.TicketHelper;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.slf4j.Logger;

public class TileComponentChunkLoader<T extends TileEntityMekanism & IChunkLoader> implements ITileComponent {
   private static final Logger LOGGER = LogUtils.getLogger();
   private final T tile;
   private final LongSet chunkSet = new LongOpenHashSet();
   private final boolean forceTicks;
   @Nullable
   private ServerLevel prevWorld;
   @Nullable
   private BlockPos prevPos;
   private boolean hasRegistered;

   public TileComponentChunkLoader(T tile) {
      this(tile, false);
   }

   public TileComponentChunkLoader(T tile, boolean forceTicks) {
      this.tile = tile;
      this.tile.addComponent(this);
      this.forceTicks = forceTicks;
   }

   public boolean canOperate() {
      return MekanismConfig.general.allowChunkloading.get() && this.tile.supportsUpgrades() && this.tile.getComponent().isUpgradeInstalled(Upgrade.ANCHOR);
   }

   private void releaseChunkTickets(@NotNull ServerLevel world, @NotNull BlockPos pos) {
      int tickets = this.chunkSet.size();
      LOGGER.debug("Attempting to remove {} chunk tickets. Pos: {} World: {}", new Object[]{tickets, pos, world.m_46472_().m_135782_()});
      if (tickets > 0) {
         LongIterator var4 = this.chunkSet.iterator();

         while (var4.hasNext()) {
            long chunkPos = (Long)var4.next();
            ForgeChunkManager.forceChunk(world, "mekanism", pos, (int)chunkPos, (int)(chunkPos >> 32), false, this.forceTicks);
         }

         this.chunkSet.clear();
         this.markDirty();
      }

      this.hasRegistered = false;
      this.prevWorld = null;
   }

   private void registerChunkTickets(@NotNull ServerLevel world) {
      this.prevPos = this.tile.m_58899_();
      this.prevWorld = world;
      Set<ChunkPos> chunks = this.tile.getChunkSet();
      int tickets = chunks.size();
      LOGGER.debug("Attempting to add {} chunk tickets. Pos: {} World: {}", new Object[]{tickets, this.prevPos, world.m_46472_().m_135782_()});
      if (tickets > 0) {
         for (ChunkPos chunkPos : chunks) {
            ForgeChunkManager.forceChunk(world, "mekanism", this.prevPos, chunkPos.f_45578_, chunkPos.f_45579_, true, this.forceTicks);
            this.chunkSet.add(chunkPos.m_45588_());
         }

         this.markDirty();
      }

      this.hasRegistered = true;
   }

   public void refreshChunkTickets() {
      if (!this.tile.isRemote()) {
         this.refreshChunkTickets(Objects.requireNonNull(this.tile.m_58904_()), this.tile.m_58899_(), true);
      }
   }

   private void refreshChunkTickets(@NotNull ServerLevel world, @NotNull BlockPos pos, boolean ticketsChanged) {
      boolean canOperate = this.canOperate();
      if (this.hasRegistered && this.prevWorld != null && this.prevPos != null) {
         if (this.prevWorld != world || !pos.equals(this.prevPos)) {
            this.releaseChunkTickets(this.prevWorld, this.prevPos);
            if (canOperate) {
               this.registerChunkTickets(world);
            }
         } else if (!canOperate) {
            this.releaseChunkTickets(world, pos);
         } else if (ticketsChanged) {
            if (this.chunkSet.isEmpty()) {
               this.registerChunkTickets(world);
            } else {
               LongSet chunks = this.getTileChunks();
               if (chunks.isEmpty()) {
                  this.releaseChunkTickets(world, pos);
               } else {
                  int removed = 0;
                  int added = 0;
                  LongIterator chunkIt = this.chunkSet.iterator();

                  while (chunkIt.hasNext()) {
                     long chunkPos = chunkIt.nextLong();
                     if (!chunks.contains(chunkPos)) {
                        ForgeChunkManager.forceChunk(world, "mekanism", pos, (int)chunkPos, (int)(chunkPos >> 32), false, this.forceTicks);
                        chunkIt.remove();
                        removed++;
                     }
                  }

                  LongIterator var12 = chunks.iterator();

                  while (var12.hasNext()) {
                     long chunkPos = (Long)var12.next();
                     if (this.chunkSet.add(chunkPos)) {
                        ForgeChunkManager.forceChunk(world, "mekanism", pos, (int)chunkPos, (int)(chunkPos >> 32), true, this.forceTicks);
                        added++;
                     }
                  }

                  if (removed != 0 || added != 0) {
                     this.markDirty();
                  }

                  LOGGER.debug(
                     "Removed {} no longer valid chunk tickets, and added {} newly valid chunk tickets. Pos: {} World: {}",
                     new Object[]{removed, added, pos, world.m_46472_().m_135782_()}
                  );
               }
            }
         }
      } else if (canOperate) {
         this.registerChunkTickets(world);
      }
   }

   public void tickServer() {
      Level world = this.tile.m_58904_();
      if (world != null) {
         this.refreshChunkTickets((ServerLevel)world, this.tile.m_58899_(), false);
      }
   }

   @Override
   public void read(CompoundTag nbtTags) {
      if (!this.chunkSet.isEmpty()) {
         if (this.tile.m_58898_() && !this.tile.isRemote() && this.hasRegistered && this.prevWorld != null && this.prevPos != null) {
            this.releaseChunkTickets(this.prevWorld, this.prevPos);
         } else {
            this.chunkSet.clear();
         }
      }

      for (long chunk : nbtTags.m_128467_("chunkSet")) {
         this.chunkSet.add(chunk);
      }
   }

   @Override
   public void write(CompoundTag nbtTags) {
      nbtTags.m_128388_("chunkSet", this.chunkSet.toLongArray());
   }

   @Override
   public void removed() {
      if (!this.tile.isRemote() && this.hasRegistered && this.prevWorld != null && this.prevPos != null) {
         this.releaseChunkTickets(this.prevWorld, this.prevPos);
      }
   }

   private void markDirty() {
      this.tile.markForSave();
   }

   private LongSet getTileChunks() {
      Set<ChunkPos> chunks = this.tile.getChunkSet();
      if (chunks.isEmpty()) {
         return LongSets.EMPTY_SET;
      } else {
         LongSet chunksAsLongs = new LongOpenHashSet(chunks.size());

         for (ChunkPos chunkPos : chunks) {
            chunksAsLongs.add(chunkPos.m_45588_());
         }

         return chunksAsLongs;
      }
   }

   public static class ChunkValidationCallback implements LoadingValidationCallback {
      public static final TileComponentChunkLoader.ChunkValidationCallback INSTANCE = new TileComponentChunkLoader.ChunkValidationCallback();

      private ChunkValidationCallback() {
      }

      public void validateTickets(@NotNull ServerLevel world, @NotNull TicketHelper ticketHelper) {
         ResourceLocation worldName = world.m_46472_().m_135782_();
         TileComponentChunkLoader.LOGGER
            .debug(
               "Validating tickets for: {}. Blocks: {}, Entities: {}",
               new Object[]{worldName, ticketHelper.getBlockTickets().size(), ticketHelper.getEntityTickets().size()}
            );

         for (Entry<BlockPos, Pair<LongSet, LongSet>> entry : ticketHelper.getBlockTickets().entrySet()) {
            BlockPos pos = entry.getKey();
            LongSet forcedChunks = (LongSet)entry.getValue().getFirst();
            LongSet tickingForcedChunks = (LongSet)entry.getValue().getSecond();
            TileComponentChunkLoader.LOGGER
               .debug(
                  "Validating tickets for: {}, BlockPos: {}, Forced chunks: {}, Ticking forced chunks: {}",
                  new Object[]{worldName, pos, forcedChunks.size(), ((LongSet)entry.getValue().getSecond()).size()}
               );
            this.validateTickets(world, worldName, pos, ticketHelper, forcedChunks, false);
            this.validateTickets(world, worldName, pos, ticketHelper, tickingForcedChunks, true);
         }
      }

      private void validateTickets(
         ServerLevel world, ResourceLocation worldName, BlockPos pos, TicketHelper ticketHelper, LongSet forcedChunks, boolean ticking
      ) {
         int ticketCount = forcedChunks.size();
         if (ticketCount > 0) {
            BlockEntity tile = world.m_7702_(pos);
            if (tile instanceof IChunkLoader) {
               TileComponentChunkLoader<?> chunkLoader = ((IChunkLoader)tile).getChunkLoader();
               if (chunkLoader.canOperate()) {
                  if (!forcedChunks.equals(chunkLoader.chunkSet)) {
                     TileComponentChunkLoader.LOGGER.debug("Mismatched chunkSet for chunk loader at position: {} in {}. Correcting.", pos, worldName);
                     chunkLoader.chunkSet.clear();
                     chunkLoader.chunkSet.addAll(forcedChunks);
                     chunkLoader.markDirty();
                  }

                  LongSet chunks = chunkLoader.getTileChunks();
                  if (chunks.isEmpty()) {
                     TileComponentChunkLoader.LOGGER
                        .warn(
                           "Removing {} chunk tickets as they are no longer valid as this loader does not expect to have any tickets even though it is can operate. Pos: {} World: {}",
                           new Object[]{ticketCount, pos, worldName}
                        );
                     this.releaseAllTickets(chunkLoader, pos, ticketHelper);
                  } else {
                     int removed = 0;
                     int added = 0;
                     LongIterator chunkIt = chunkLoader.chunkSet.iterator();

                     while (chunkIt.hasNext()) {
                        long chunkPos = chunkIt.nextLong();
                        if (!chunks.contains(chunkPos) || ticking != chunkLoader.forceTicks) {
                           ticketHelper.removeTicket(pos, chunkPos, ticking);
                           chunkIt.remove();
                           removed++;
                        }
                     }

                     LongIterator var17 = chunks.iterator();

                     while (var17.hasNext()) {
                        long chunkPos = (Long)var17.next();
                        if (chunkLoader.chunkSet.add(chunkPos) || ticking != chunkLoader.forceTicks) {
                           ForgeChunkManager.forceChunk(world, "mekanism", pos, (int)chunkPos, (int)(chunkPos >> 32), true, chunkLoader.forceTicks);
                           added++;
                        }
                     }

                     chunkLoader.hasRegistered = true;
                     chunkLoader.prevWorld = world;
                     chunkLoader.prevPos = pos;
                     if (removed == 0 && added == 0) {
                        TileComponentChunkLoader.LOGGER.debug("Tickets for position: {} in {}, successfully validated.", pos, worldName);
                     } else {
                        chunkLoader.markDirty();
                        TileComponentChunkLoader.LOGGER
                           .info(
                              "Removed {} no longer valid chunk tickets, and added {} newly valid chunk tickets. Pos: {} World: {}",
                              new Object[]{removed, added, pos, worldName}
                           );
                     }
                  }
               } else {
                  TileComponentChunkLoader.LOGGER
                     .info(
                        "Removing {} chunk tickets as they are no longer valid as this loader cannot operate. Pos: {} World: {}",
                        new Object[]{ticketCount, pos, worldName}
                     );
                  this.releaseAllTickets(chunkLoader, pos, ticketHelper);
               }
            } else {
               TileComponentChunkLoader.LOGGER
                  .warn("Block at {}, in {}, is not a valid chunk loader. Removing {} chunk tickets.", new Object[]{pos, worldName, ticketCount});
               ticketHelper.removeAllTickets(pos);
            }
         }
      }

      private void releaseAllTickets(TileComponentChunkLoader<?> chunkLoader, BlockPos pos, TicketHelper ticketHelper) {
         ticketHelper.removeAllTickets(pos);
         chunkLoader.chunkSet.clear();
         chunkLoader.hasRegistered = false;
         chunkLoader.prevWorld = null;
         chunkLoader.markDirty();
      }
   }
}
