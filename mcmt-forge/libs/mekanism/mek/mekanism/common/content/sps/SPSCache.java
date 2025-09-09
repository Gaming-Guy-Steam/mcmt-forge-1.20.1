package mekanism.common.content.sps;

import mekanism.api.math.FloatingLong;
import mekanism.common.lib.multiblock.MultiblockCache;
import mekanism.common.util.NBTUtils;
import net.minecraft.nbt.CompoundTag;

public class SPSCache extends MultiblockCache<SPSMultiblockData> {
   private double progress;
   private int inputProcessed;
   private boolean couldOperate;
   private FloatingLong receivedEnergy = FloatingLong.ZERO;
   private double lastProcessed;

   @Override
   public void merge(MultiblockCache<SPSMultiblockData> mergeCache, MultiblockCache.RejectContents rejectContents) {
      super.merge(mergeCache, rejectContents);
      this.progress = this.progress + ((SPSCache)mergeCache).progress;
      this.inputProcessed = this.inputProcessed + ((SPSCache)mergeCache).inputProcessed;
      this.couldOperate = this.couldOperate | ((SPSCache)mergeCache).couldOperate;
      this.receivedEnergy = this.receivedEnergy.add(((SPSCache)mergeCache).receivedEnergy);
      this.lastProcessed = Math.max(this.lastProcessed, ((SPSCache)mergeCache).lastProcessed);
   }

   public void apply(SPSMultiblockData data) {
      super.apply(data);
      data.progress = this.progress;
      data.inputProcessed = this.inputProcessed;
      data.couldOperate = this.couldOperate;
      data.receivedEnergy = this.receivedEnergy;
      data.lastProcessed = this.lastProcessed;
   }

   public void sync(SPSMultiblockData data) {
      super.sync(data);
      this.progress = data.progress;
      this.inputProcessed = data.inputProcessed;
      this.couldOperate = data.couldOperate;
      this.receivedEnergy = data.receivedEnergy;
      this.lastProcessed = data.lastProcessed;
   }

   @Override
   public void load(CompoundTag nbtTags) {
      super.load(nbtTags);
      NBTUtils.setDoubleIfPresent(nbtTags, "progress", val -> this.progress = val);
      NBTUtils.setIntIfPresent(nbtTags, "processed", val -> this.inputProcessed = val);
      NBTUtils.setBooleanIfPresent(nbtTags, "couldOperate", val -> this.couldOperate = val);
      NBTUtils.setFloatingLongIfPresent(nbtTags, "energyUsage", val -> this.receivedEnergy = val);
      NBTUtils.setDoubleIfPresent(nbtTags, "lastProcessed", val -> this.lastProcessed = val);
   }

   @Override
   public void save(CompoundTag nbtTags) {
      super.save(nbtTags);
      nbtTags.m_128347_("progress", this.progress);
      nbtTags.m_128405_("processed", this.inputProcessed);
      nbtTags.m_128379_("couldOperate", this.couldOperate);
      nbtTags.m_128359_("energyUsage", this.receivedEnergy.toString());
      nbtTags.m_128347_("lastProcessed", this.lastProcessed);
   }
}
