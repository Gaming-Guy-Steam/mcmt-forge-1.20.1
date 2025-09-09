package mekanism.common.tile.multiblock;

import java.util.LinkedList;
import java.util.Queue;
import mekanism.api.providers.IBlockProvider;
import mekanism.common.Mekanism;
import mekanism.common.content.sps.SPSMultiblockData;
import mekanism.common.lib.multiblock.MultiblockManager;
import mekanism.common.particle.SPSOrbitEffect;
import mekanism.common.registries.MekanismBlocks;
import mekanism.common.tile.prefab.TileEntityMultiblock;
import mekanism.common.util.NBTUtils;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.level.block.state.BlockState;
import org.jetbrains.annotations.NotNull;

public class TileEntitySPSCasing extends TileEntityMultiblock<SPSMultiblockData> {
   public final Queue<SPSOrbitEffect> orbitEffects = new LinkedList<>();
   private boolean handleSound;
   private boolean prevActive;

   public TileEntitySPSCasing(BlockPos pos, BlockState state) {
      this(MekanismBlocks.SPS_CASING, pos, state);
   }

   public TileEntitySPSCasing(IBlockProvider provider, BlockPos pos, BlockState state) {
      super(provider, pos, state);
   }

   @Override
   protected void onUpdateClient() {
      super.onUpdateClient();
      if (this.isMaster()) {
         this.orbitEffects.removeIf(SPSOrbitEffect::tick);
      } else {
         this.orbitEffects.clear();
      }
   }

   protected boolean onUpdateServer(SPSMultiblockData multiblock) {
      boolean needsPacket = super.onUpdateServer(multiblock);
      boolean active = multiblock.isFormed() && multiblock.handlesSound(this) && multiblock.lastProcessed > 0.0;
      if (active != this.prevActive) {
         this.prevActive = active;
         needsPacket = true;
      }

      return needsPacket;
   }

   protected void structureChanged(SPSMultiblockData multiblock) {
      super.structureChanged(multiblock);
      if (multiblock.isFormed()) {
         for (SPSOrbitEffect orbitEffect : this.orbitEffects) {
            orbitEffect.updateMultiblock(multiblock);
         }
      }
   }

   public SPSMultiblockData createMultiblock() {
      return new SPSMultiblockData(this);
   }

   @Override
   public MultiblockManager<SPSMultiblockData> getManager() {
      return Mekanism.spsManager;
   }

   @Override
   protected boolean canPlaySound() {
      SPSMultiblockData multiblock = this.getMultiblock();
      return multiblock.isFormed() && this.handleSound;
   }

   @NotNull
   @Override
   public CompoundTag getReducedUpdateTag() {
      CompoundTag updateTag = super.getReducedUpdateTag();
      SPSMultiblockData multiblock = this.getMultiblock();
      updateTag.m_128379_("handleSound", multiblock.isFormed() && multiblock.handlesSound(this) && multiblock.lastProcessed > 0.0);
      return updateTag;
   }

   @Override
   public void handleUpdateTag(@NotNull CompoundTag tag) {
      super.handleUpdateTag(tag);
      NBTUtils.setBooleanIfPresent(tag, "handleSound", value -> this.handleSound = value);
   }
}
