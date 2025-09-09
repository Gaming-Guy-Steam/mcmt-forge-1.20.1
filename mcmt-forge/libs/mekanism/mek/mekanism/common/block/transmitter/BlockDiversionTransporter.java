package mekanism.common.block.transmitter;

import mekanism.common.block.interfaces.IHasTileEntity;
import mekanism.common.registration.impl.TileEntityTypeRegistryObject;
import mekanism.common.registries.MekanismTileEntityTypes;
import mekanism.common.tile.transmitter.TileEntityDiversionTransporter;
import net.minecraft.world.level.material.MapColor;

public class BlockDiversionTransporter extends BlockLargeTransmitter implements IHasTileEntity<TileEntityDiversionTransporter> {
   public BlockDiversionTransporter() {
      super(properties -> properties.m_284180_(MapColor.f_283750_));
   }

   @Override
   public TileEntityTypeRegistryObject<TileEntityDiversionTransporter> getTileType() {
      return MekanismTileEntityTypes.DIVERSION_TRANSPORTER;
   }
}
