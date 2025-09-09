package mekanism.common.content.tank;

import mekanism.common.lib.multiblock.MultiblockCache;
import mekanism.common.tile.interfaces.IFluidContainerManager;
import mekanism.common.util.NBTUtils;
import net.minecraft.nbt.CompoundTag;

public class TankCache extends MultiblockCache<TankMultiblockData> {
   private IFluidContainerManager.ContainerEditMode editMode = IFluidContainerManager.ContainerEditMode.BOTH;

   @Override
   public void merge(MultiblockCache<TankMultiblockData> mergeCache, MultiblockCache.RejectContents rejectContents) {
      super.merge(mergeCache, rejectContents);
      this.editMode = ((TankCache)mergeCache).editMode;
   }

   public void apply(TankMultiblockData data) {
      super.apply(data);
      data.editMode = this.editMode;
   }

   public void sync(TankMultiblockData data) {
      super.sync(data);
      this.editMode = data.editMode;
   }

   @Override
   public void load(CompoundTag nbtTags) {
      super.load(nbtTags);
      NBTUtils.setEnumIfPresent(nbtTags, "editMode", IFluidContainerManager.ContainerEditMode::byIndexStatic, mode -> this.editMode = mode);
   }

   @Override
   public void save(CompoundTag nbtTags) {
      super.save(nbtTags);
      NBTUtils.writeEnum(nbtTags, "editMode", this.editMode);
   }
}
