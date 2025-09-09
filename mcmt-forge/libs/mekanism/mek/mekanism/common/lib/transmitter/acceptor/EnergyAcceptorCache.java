package mekanism.common.lib.transmitter.acceptor;

import mekanism.api.annotations.NothingNullByDefault;
import mekanism.api.energy.IStrictEnergyHandler;
import mekanism.common.content.network.transmitter.Transmitter;
import mekanism.common.integration.energy.EnergyCompatUtils;
import mekanism.common.integration.energy.IEnergyCompat;
import mekanism.common.integration.energy.StrictEnergyCompat;
import mekanism.common.tile.transmitter.TileEntityTransmitter;
import mekanism.common.util.CapabilityUtils;
import net.minecraft.core.Direction;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraftforge.common.util.LazyOptional;
import org.jetbrains.annotations.Nullable;

@NothingNullByDefault
public class EnergyAcceptorCache extends AcceptorCache<IStrictEnergyHandler> {
   public EnergyAcceptorCache(Transmitter<IStrictEnergyHandler, ?, ?> transmitter, TileEntityTransmitter transmitterTile) {
      super(transmitter, transmitterTile);
   }

   public boolean hasStrictEnergyHandlerAndListen(@Nullable BlockEntity tile, Direction side) {
      if (tile != null && !tile.m_58901_() && tile.m_58898_()) {
         Direction opposite = side.m_122424_();

         for (IEnergyCompat energyCompat : EnergyCompatUtils.getCompats()) {
            if (energyCompat.isUsable()) {
               LazyOptional<?> acceptor = CapabilityUtils.getCapability(tile, energyCompat.getCapability(), opposite);
               if (acceptor.isPresent()) {
                  if (energyCompat instanceof StrictEnergyCompat) {
                     this.updateCachedAcceptorAndListen(side, tile, acceptor.cast());
                  } else {
                     LazyOptional<IStrictEnergyHandler> wrappedAcceptor = energyCompat.getLazyStrictEnergyHandler(tile, opposite);
                     if (wrappedAcceptor.isPresent()) {
                        this.updateCachedAcceptorAndListen(side, tile, wrappedAcceptor, acceptor, false);
                     }
                  }

                  return true;
               }
            }
         }
      }

      return false;
   }
}
