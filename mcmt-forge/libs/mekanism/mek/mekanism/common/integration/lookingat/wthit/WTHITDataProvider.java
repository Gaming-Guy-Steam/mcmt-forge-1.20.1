package mekanism.common.integration.lookingat.wthit;

import mcp.mobius.waila.api.IDataProvider;
import mcp.mobius.waila.api.IDataWriter;
import mcp.mobius.waila.api.IPluginConfig;
import mcp.mobius.waila.api.IServerAccessor;
import mekanism.common.integration.lookingat.LookingAtUtils;
import mekanism.common.tile.TileEntityBoundingBlock;
import mekanism.common.util.WorldUtils;
import net.minecraft.world.level.block.entity.BlockEntity;

public class WTHITDataProvider implements IDataProvider<BlockEntity> {
   static final WTHITDataProvider INSTANCE = new WTHITDataProvider();

   public void appendData(IDataWriter dataWriter, IServerAccessor<BlockEntity> serverAccessor, IPluginConfig config) {
      BlockEntity tile = (BlockEntity)serverAccessor.getTarget();
      if (tile instanceof TileEntityBoundingBlock boundingBlock) {
         if (!boundingBlock.hasReceivedCoords() || tile.m_58899_().equals(boundingBlock.getMainPos())) {
            return;
         }

         tile = WorldUtils.getTileEntity(serverAccessor.getWorld(), boundingBlock.getMainPos());
         if (tile == null) {
            return;
         }
      }

      WTHITLookingAtHelper helper = new WTHITLookingAtHelper();
      LookingAtUtils.addInfo(helper, tile, true, true);
      dataWriter.add(WTHITLookingAtHelper.class, result -> result.add(helper));
   }
}
