package mekanism.common.integration.lookingat.wthit;

import mcp.mobius.waila.api.IDataProvider;
import mcp.mobius.waila.api.IDataWriter;
import mcp.mobius.waila.api.IPluginConfig;
import mcp.mobius.waila.api.IServerAccessor;
import mekanism.common.integration.lookingat.LookingAtUtils;
import net.minecraft.world.entity.Entity;

public class WTHITEntityDataProvider implements IDataProvider<Entity> {
   static final WTHITEntityDataProvider INSTANCE = new WTHITEntityDataProvider();

   public void appendData(IDataWriter dataWriter, IServerAccessor<Entity> serverAccessor, IPluginConfig config) {
      WTHITLookingAtHelper helper = new WTHITLookingAtHelper();
      LookingAtUtils.addInfo(helper, (Entity)serverAccessor.getTarget());
      dataWriter.add(WTHITLookingAtHelper.class, result -> result.add(helper));
   }
}
