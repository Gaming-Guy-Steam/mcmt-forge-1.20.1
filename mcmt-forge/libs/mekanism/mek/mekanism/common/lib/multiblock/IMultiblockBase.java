package mekanism.common.lib.multiblock;

import mekanism.common.tile.interfaces.ITileWrapper;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;

public interface IMultiblockBase extends ITileWrapper {
   default MultiblockData getMultiblockData(MultiblockManager<?> manager) {
      MultiblockData data = this.getStructure(manager).getMultiblockData();
      return data != null && data.isFormed() ? data : this.getDefaultData();
   }

   default void setMultiblockData(MultiblockManager<?> manager, MultiblockData multiblockData) {
      this.getStructure(manager).setMultiblockData(multiblockData);
   }

   MultiblockData getDefaultData();

   InteractionResult onActivate(Player player, InteractionHand hand, ItemStack stack);

   Structure getStructure(MultiblockManager<?> manager);

   boolean hasStructure(Structure structure);

   void setStructure(MultiblockManager<?> manager, Structure structure);

   default Structure resetStructure(MultiblockManager<?> manager) {
      Structure structure = new Structure(this);
      this.setStructure(manager, structure);
      return structure;
   }

   default void resetForFormed() {
   }
}
