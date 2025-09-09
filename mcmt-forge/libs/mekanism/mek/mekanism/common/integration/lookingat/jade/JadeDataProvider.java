package mekanism.common.integration.lookingat.jade;

import mekanism.common.integration.lookingat.LookingAtUtils;
import mekanism.common.tile.TileEntityBoundingBlock;
import mekanism.common.util.WorldUtils;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.block.entity.BlockEntity;
import snownee.jade.api.BlockAccessor;
import snownee.jade.api.IServerDataProvider;

public class JadeDataProvider implements IServerDataProvider<BlockAccessor> {
   static final JadeDataProvider INSTANCE = new JadeDataProvider();

   public ResourceLocation getUid() {
      return JadeConstants.BLOCK_DATA;
   }

   public void appendServerData(CompoundTag data, BlockAccessor blockAccessor) {
      BlockEntity tile = blockAccessor.getBlockEntity();
      if (tile instanceof TileEntityBoundingBlock boundingBlock) {
         if (!boundingBlock.hasReceivedCoords() || blockAccessor.getPosition().equals(boundingBlock.getMainPos())) {
            return;
         }

         tile = WorldUtils.getTileEntity(blockAccessor.getLevel(), boundingBlock.getMainPos());
         if (tile == null) {
            return;
         }
      }

      JadeLookingAtHelper helper = new JadeLookingAtHelper();
      LookingAtUtils.addInfo(helper, tile, true, true);
      helper.finalizeData(data);
   }
}
