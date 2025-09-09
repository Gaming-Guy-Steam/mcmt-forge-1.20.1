package mekanism.common.content.miner;

import it.unimi.dsi.fastutil.longs.Long2ObjectMap;
import it.unimi.dsi.fastutil.longs.Long2ObjectOpenHashMap;
import it.unimi.dsi.fastutil.objects.Reference2BooleanMap;
import it.unimi.dsi.fastutil.objects.Reference2BooleanOpenHashMap;
import java.util.BitSet;
import mekanism.api.annotations.NothingNullByDefault;
import mekanism.api.math.MathUtils;
import mekanism.api.text.IHasTextComponent;
import mekanism.api.text.ILangEntry;
import mekanism.common.MekanismLang;
import mekanism.common.tags.MekanismTags;
import mekanism.common.tile.TileEntityBoundingBlock;
import mekanism.common.tile.machine.TileEntityDigitalMiner;
import mekanism.common.util.MekanismUtils;
import mekanism.common.util.WorldUtils;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.PathNavigationRegion;
import net.minecraft.world.level.block.BedBlock;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.DoorBlock;
import net.minecraft.world.level.block.DoublePlantBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.BedPart;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.block.state.properties.DoubleBlockHalf;

public class ThreadMinerSearch extends Thread {
   private final TileEntityDigitalMiner tile;
   private final Long2ObjectMap<BitSet> oresToMine = new Long2ObjectOpenHashMap();
   private PathNavigationRegion chunkCache;
   public ThreadMinerSearch.State state = ThreadMinerSearch.State.IDLE;
   public int found = 0;

   public ThreadMinerSearch(TileEntityDigitalMiner tile) {
      this.tile = tile;
   }

   public void setChunkCache(PathNavigationRegion cache) {
      this.chunkCache = cache;
   }

   @Override
   public void run() {
      this.state = ThreadMinerSearch.State.SEARCHING;
      if (!this.tile.getInverse() && !this.tile.getFilterManager().hasEnabledFilters()) {
         this.state = ThreadMinerSearch.State.FINISHED;
      } else {
         Reference2BooleanMap<Block> acceptedItems = new Reference2BooleanOpenHashMap();
         BlockPos pos = this.tile.getStartingPos();
         int diameter = this.tile.getDiameter();
         int size = this.tile.getTotalSize();
         BlockPos minerPos = this.tile.m_58899_();

         for (int i = 0; i < size; i++) {
            if (this.tile.m_58901_()) {
               return;
            }

            BlockPos testPos = TileEntityDigitalMiner.getOffsetForIndex(pos, diameter, i);
            if (!minerPos.equals(testPos) && WorldUtils.getTileEntity(TileEntityBoundingBlock.class, this.chunkCache, testPos) == null) {
               BlockState state = this.chunkCache.m_8055_(testPos);
               if (!state.m_60795_()
                  && !state.m_204336_(MekanismTags.Blocks.MINER_BLACKLIST)
                  && !this.shouldSkipState(state)
                  && !(state.m_60800_(this.chunkCache, testPos) < 0.0F)) {
                  Block info = state.m_60734_();
                  if (!MekanismUtils.isLiquidBlock(info)) {
                     boolean accepted = acceptedItems.computeIfAbsent(
                        info,
                        block -> this.tile.isReplaceTarget(block.m_5456_())
                           ? false
                           : this.tile.getInverse() != this.tile.getFilterManager().anyEnabledMatch(filter -> filter.canFilter(state))
                     );
                     if (accepted) {
                        long chunk = ChunkPos.m_151388_(testPos);
                        ((BitSet)this.oresToMine.computeIfAbsent(chunk, k -> new BitSet())).set(i);
                        this.found++;
                     }
                  }
               }
            }
         }

         this.state = ThreadMinerSearch.State.FINISHED;
         this.chunkCache = null;
         if (this.tile.searcher == this) {
            this.tile.updateFromSearch(this.oresToMine, this.found);
         }
      }
   }

   private boolean shouldSkipState(BlockState state) {
      if (state.m_60734_() instanceof BedBlock) {
         return state.m_61143_(BlockStateProperties.f_61391_) == BedPart.FOOT;
      } else {
         return !(state.m_60734_() instanceof DoorBlock) && !(state.m_60734_() instanceof DoublePlantBlock)
            ? false
            : state.m_61143_(BlockStateProperties.f_61401_) == DoubleBlockHalf.UPPER;
      }
   }

   @NothingNullByDefault
   public static enum State implements IHasTextComponent {
      IDLE(MekanismLang.MINER_IDLE),
      SEARCHING(MekanismLang.MINER_SEARCHING),
      PAUSED(MekanismLang.MINER_PAUSED),
      FINISHED(MekanismLang.MINER_READY);

      private static final ThreadMinerSearch.State[] MODES = values();
      private final ILangEntry langEntry;

      private State(ILangEntry langEntry) {
         this.langEntry = langEntry;
      }

      @Override
      public Component getTextComponent() {
         return this.langEntry.translate();
      }

      public static ThreadMinerSearch.State byIndexStatic(int index) {
         return MathUtils.getByIndexMod(MODES, index);
      }
   }
}
