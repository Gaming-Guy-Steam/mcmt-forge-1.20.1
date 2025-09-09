package mekanism.common;

import it.unimi.dsi.fastutil.objects.Object2IntMap;
import it.unimi.dsi.fastutil.objects.Object2IntMaps;
import it.unimi.dsi.fastutil.objects.Object2IntOpenHashMap;
import it.unimi.dsi.fastutil.objects.Object2ObjectArrayMap;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.Map;
import java.util.Queue;
import java.util.function.Predicate;
import mekanism.api.security.ISecurityUtils;
import mekanism.common.config.MekanismConfig;
import mekanism.common.content.qio.QIOGlobalItemLookup;
import mekanism.common.inventory.container.item.PortableQIODashboardContainer;
import mekanism.common.lib.frequency.FrequencyManager;
import mekanism.common.lib.multiblock.MultiblockManager;
import mekanism.common.lib.radiation.RadiationManager;
import mekanism.common.util.WorldUtils;
import mekanism.common.world.GenHandler;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.ExperienceOrb;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraftforge.event.TickEvent.LevelTickEvent;
import net.minecraftforge.event.TickEvent.Phase;
import net.minecraftforge.event.TickEvent.ServerTickEvent;
import net.minecraftforge.event.entity.EntityJoinLevelEvent;
import net.minecraftforge.event.level.BlockEvent.BreakEvent;
import net.minecraftforge.event.level.ChunkDataEvent.Load;
import net.minecraftforge.event.level.ChunkDataEvent.Save;
import net.minecraftforge.event.level.ChunkEvent.Unload;
import net.minecraftforge.eventbus.api.EventPriority;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import org.jetbrains.annotations.Nullable;

public class CommonWorldTickHandler {
   private static final long maximumDeltaTimeNanoSecs = 16000000L;
   private Map<ResourceLocation, Object2IntMap<ChunkPos>> chunkVersions;
   private Map<ResourceLocation, Queue<ChunkPos>> chunkRegenMap;
   public static boolean flushTagAndRecipeCaches;
   public static boolean monitoringCardboardBox;
   @Nullable
   public static Predicate<ItemStack> fallbackItemCollector;

   public void addRegenChunk(ResourceKey<Level> dimension, ChunkPos chunkCoord) {
      if (this.chunkRegenMap == null) {
         this.chunkRegenMap = new Object2ObjectArrayMap();
      }

      ResourceLocation dimensionName = dimension.m_135782_();
      if (!this.chunkRegenMap.containsKey(dimensionName)) {
         LinkedList<ChunkPos> list = new LinkedList<>();
         list.add(chunkCoord);
         this.chunkRegenMap.put(dimensionName, list);
      } else {
         Queue<ChunkPos> regenPositions = this.chunkRegenMap.get(dimensionName);
         if (!regenPositions.contains(chunkCoord)) {
            regenPositions.add(chunkCoord);
         }
      }
   }

   public void resetChunkData() {
      this.chunkRegenMap = null;
      this.chunkVersions = null;
   }

   @SubscribeEvent(
      priority = EventPriority.HIGHEST
   )
   public void onEntitySpawn(EntityJoinLevelEvent event) {
      if (monitoringCardboardBox) {
         Entity entity = event.getEntity();
         if (entity instanceof ItemEntity || entity instanceof ExperienceOrb) {
            entity.m_146870_();
            event.setCanceled(true);
         }
      } else if (fallbackItemCollector != null && event.getEntity() instanceof ItemEntity entity && fallbackItemCollector.test(entity.m_32055_())) {
         entity.m_146870_();
         event.setCanceled(true);
      }
   }

   @SubscribeEvent
   public void onBlockBreak(BreakEvent event) {
      BlockState state = event.getState();
      if (state != null && !state.m_60795_() && state.m_155947_()) {
         BlockEntity blockEntity = WorldUtils.getTileEntity(event.getLevel(), event.getPos());
         if (!ISecurityUtils.INSTANCE.canAccess(event.getPlayer(), blockEntity)) {
            event.setCanceled(true);
         }
      }
   }

   @SubscribeEvent(
      priority = EventPriority.HIGHEST
   )
   public synchronized void chunkSave(Save event) {
      LevelAccessor world = event.getLevel();
      if (!world.m_5776_() && world instanceof Level level) {
         int chunkVersion = MekanismConfig.world.userGenVersion.get();
         if (this.chunkVersions != null) {
            chunkVersion = this.chunkVersions
               .getOrDefault(level.m_46472_().m_135782_(), Object2IntMaps.emptyMap())
               .getOrDefault(event.getChunk().m_7697_(), chunkVersion);
         }

         event.getData().m_128405_("mekWorldGenVersion", chunkVersion);
      }
   }

   @SubscribeEvent(
      priority = EventPriority.HIGHEST
   )
   public synchronized void onChunkDataLoad(Load event) {
      if (event.getLevel() instanceof Level level && !level.m_5776_()) {
         int version = event.getData().m_128451_("mekWorldGenVersion");
         if (version < MekanismConfig.world.userGenVersion.get()) {
            if (this.chunkVersions == null) {
               this.chunkVersions = new Object2ObjectArrayMap();
            }

            ChunkPos chunkCoord = event.getChunk().m_7697_();
            ResourceKey<Level> dimension = level.m_46472_();
            this.chunkVersions.computeIfAbsent(dimension.m_135782_(), dim -> new Object2IntOpenHashMap()).put(chunkCoord, version);
            if (MekanismConfig.world.enableRegeneration.get()) {
               this.addRegenChunk(dimension, chunkCoord);
            }
         }
      }
   }

   @SubscribeEvent
   public void chunkUnloadEvent(Unload event) {
      if (event.getLevel() instanceof Level level && !level.m_5776_() && this.chunkVersions != null) {
         this.chunkVersions.getOrDefault(level.m_46472_().m_135782_(), Object2IntMaps.emptyMap()).removeInt(event.getChunk().m_7697_());
      }
   }

   @SubscribeEvent
   public void worldUnloadEvent(net.minecraftforge.event.level.LevelEvent.Unload event) {
      LevelAccessor world = event.getLevel();
      if (!world.m_5776_() && world instanceof Level level && this.chunkVersions != null) {
         this.chunkVersions.remove(level.m_46472_().m_135782_());
      }
   }

   @SubscribeEvent
   public void worldLoadEvent(net.minecraftforge.event.level.LevelEvent.Load event) {
      if (!event.getLevel().m_5776_()) {
         FrequencyManager.load();
         MultiblockManager.createOrLoadAll();
         QIOGlobalItemLookup.INSTANCE.createOrLoad();
         RadiationManager.get().createOrLoad();
      }
   }

   @SubscribeEvent
   public void onTick(ServerTickEvent event) {
      if (event.side.isServer() && event.phase == Phase.END) {
         this.serverTick();
      }
   }

   @SubscribeEvent
   public void onTick(LevelTickEvent event) {
      if (event.side.isServer() && event.phase == Phase.END) {
         this.tickEnd((ServerLevel)event.level);
      }
   }

   private void serverTick() {
      FrequencyManager.tick();
      RadiationManager.get().tickServer();
   }

   private void tickEnd(ServerLevel world) {
      if (!world.f_46443_) {
         RadiationManager.get().tickServerWorld(world);
         if (flushTagAndRecipeCaches) {
            Iterator dimensionName = world.m_6907_().iterator();

            label67:
            while (true) {
               if (dimensionName.hasNext()) {
                  ServerPlayer player = (ServerPlayer)dimensionName.next();
                  if (!(player.f_36096_ instanceof PortableQIODashboardContainer qioDashboard)) {
                     continue;
                  }

                  byte index = 0;

                  while (true) {
                     if (index >= 3) {
                        continue label67;
                     }

                     qioDashboard.getCraftingWindow(index).invalidateRecipe();
                     index++;
                  }
               }

               flushTagAndRecipeCaches = false;
               break;
            }
         }

         if (this.chunkRegenMap == null || !MekanismConfig.world.enableRegeneration.get()) {
            return;
         }

         ResourceLocation dimensionName = world.m_46472_().m_135782_();
         if (this.chunkRegenMap.containsKey(dimensionName)) {
            Queue<ChunkPos> chunksToGen = this.chunkRegenMap.get(dimensionName);
            Object2IntMap<ChunkPos> dimensionChunkVersions = this.chunkVersions == null
               ? Object2IntMaps.emptyMap()
               : this.chunkVersions.getOrDefault(dimensionName, Object2IntMaps.emptyMap());
            long startTime = System.nanoTime();

            while (System.nanoTime() - startTime < 16000000L && !chunksToGen.isEmpty()) {
               ChunkPos nextChunk = chunksToGen.poll();
               if (nextChunk == null) {
                  break;
               }

               if (WorldUtils.isChunkLoaded(world, nextChunk)) {
                  if (GenHandler.generate(world, nextChunk)) {
                     Mekanism.logger.info("Regenerating ores and salt at chunk {}", nextChunk);
                  }

                  if (this.chunkVersions != null) {
                     dimensionChunkVersions.removeInt(nextChunk);
                  }
               }
            }

            if (chunksToGen.isEmpty()) {
               this.chunkRegenMap.remove(dimensionName);
            }
         }
      }
   }
}
