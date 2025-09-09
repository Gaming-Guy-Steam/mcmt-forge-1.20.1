package mekanism.common.lib.transmitter.acceptor;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import mekanism.api.annotations.NothingNullByDefault;
import mekanism.api.chemical.gas.IGasHandler;
import mekanism.api.chemical.infuse.IInfusionHandler;
import mekanism.api.chemical.pigment.IPigmentHandler;
import mekanism.api.chemical.slurry.ISlurryHandler;
import mekanism.common.capabilities.Capabilities;
import mekanism.common.capabilities.chemical.BoxedChemicalHandler;
import mekanism.common.content.network.transmitter.BoxedPressurizedTube;
import mekanism.common.tile.transmitter.TileEntityTransmitter;
import mekanism.common.util.CapabilityUtils;
import net.minecraft.core.Direction;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraftforge.common.util.LazyOptional;
import org.jetbrains.annotations.Nullable;

@NothingNullByDefault
public class BoxedChemicalAcceptorCache extends AbstractAcceptorCache<BoxedChemicalHandler, BoxedChemicalAcceptorCache.BoxedChemicalAcceptorInfo> {
   public BoxedChemicalAcceptorCache(BoxedPressurizedTube transmitter, TileEntityTransmitter transmitterTile) {
      super(transmitter, transmitterTile);
   }

   private void updateCachedAcceptorAndListen(Direction side, BlockEntity acceptorTile, BoxedChemicalHandler acceptor) {
      boolean dirtyAcceptor = false;
      if (this.cachedAcceptors.containsKey(side)) {
         BoxedChemicalAcceptorCache.BoxedChemicalAcceptorInfo acceptorInfo = this.cachedAcceptors.get(side);
         if (acceptorTile != acceptorInfo.getTile()) {
            this.cachedAcceptors.put(side, new BoxedChemicalAcceptorCache.BoxedChemicalAcceptorInfo(acceptorTile, acceptor));
            dirtyAcceptor = true;
         } else if (!acceptor.sameHandlers(acceptorInfo.boxedHandler)) {
            acceptorInfo.updateAcceptor(acceptor);
            dirtyAcceptor = true;
         }
      } else {
         this.cachedAcceptors.put(side, new BoxedChemicalAcceptorCache.BoxedChemicalAcceptorInfo(acceptorTile, acceptor));
         dirtyAcceptor = true;
      }

      if (dirtyAcceptor) {
         this.transmitter.markDirtyAcceptor(side);
         acceptor.addRefreshListeners(this.getRefreshListener(side));
      }
   }

   public boolean isChemicalAcceptorAndListen(@Nullable BlockEntity tile, Direction side) {
      Direction opposite = side.m_122424_();
      LazyOptional<IGasHandler> gasAcceptor = CapabilityUtils.getCapability(tile, Capabilities.GAS_HANDLER, opposite);
      LazyOptional<IInfusionHandler> infusionAcceptor = CapabilityUtils.getCapability(tile, Capabilities.INFUSION_HANDLER, opposite);
      LazyOptional<IPigmentHandler> pigmentAcceptor = CapabilityUtils.getCapability(tile, Capabilities.PIGMENT_HANDLER, opposite);
      LazyOptional<ISlurryHandler> slurryAcceptor = CapabilityUtils.getCapability(tile, Capabilities.SLURRY_HANDLER, opposite);
      if (!gasAcceptor.isPresent() && !infusionAcceptor.isPresent() && !pigmentAcceptor.isPresent() && !slurryAcceptor.isPresent()) {
         return false;
      } else {
         BoxedChemicalHandler chemicalHandler = new BoxedChemicalHandler();
         if (gasAcceptor.isPresent()) {
            chemicalHandler.addGasHandler(gasAcceptor);
         }

         if (infusionAcceptor.isPresent()) {
            chemicalHandler.addInfusionHandler(infusionAcceptor);
         }

         if (pigmentAcceptor.isPresent()) {
            chemicalHandler.addPigmentHandler(pigmentAcceptor);
         }

         if (slurryAcceptor.isPresent()) {
            chemicalHandler.addSlurryHandler(slurryAcceptor);
         }

         this.updateCachedAcceptorAndListen(side, tile, chemicalHandler);
         return true;
      }
   }

   @Override
   public List<BoxedChemicalHandler> getConnectedAcceptors(Set<Direction> sides) {
      List<BoxedChemicalHandler> acceptors = new ArrayList<>(sides.size());

      for (Direction side : sides) {
         if (this.cachedAcceptors.containsKey(side)) {
            BoxedChemicalAcceptorCache.BoxedChemicalAcceptorInfo acceptorInfo = this.cachedAcceptors.get(side);
            if (!acceptorInfo.getTile().m_58901_()) {
               acceptors.add(acceptorInfo.boxedHandler);
            }
         }
      }

      return acceptors;
   }

   @Override
   public LazyOptional<BoxedChemicalHandler> getConnectedAcceptor(Direction side) {
      if (this.cachedAcceptors.containsKey(side)) {
         BoxedChemicalAcceptorCache.BoxedChemicalAcceptorInfo acceptorInfo = this.cachedAcceptors.get(side);
         if (!acceptorInfo.getTile().m_58901_()) {
            return acceptorInfo.getAsLazy();
         }
      }

      return LazyOptional.empty();
   }

   public static class BoxedChemicalAcceptorInfo extends AbstractAcceptorInfo {
      private BoxedChemicalHandler boxedHandler;
      @Nullable
      private LazyOptional<BoxedChemicalHandler> asLazy;

      private BoxedChemicalAcceptorInfo(BlockEntity tile, BoxedChemicalHandler boxedHandler) {
         super(tile);
         this.boxedHandler = boxedHandler;
      }

      public void updateAcceptor(BoxedChemicalHandler acceptor) {
         this.boxedHandler = acceptor;
         this.asLazy = null;
      }

      private LazyOptional<BoxedChemicalHandler> getAsLazy() {
         if (this.asLazy == null) {
            this.asLazy = LazyOptional.of(() -> this.boxedHandler);
         }

         return this.asLazy;
      }

      @Override
      public boolean equals(Object o) {
         if (this == o) {
            return true;
         } else if (o != null && this.getClass() == o.getClass()) {
            BoxedChemicalAcceptorCache.BoxedChemicalAcceptorInfo other = (BoxedChemicalAcceptorCache.BoxedChemicalAcceptorInfo)o;
            return this.boxedHandler.equals(other.boxedHandler);
         } else {
            return false;
         }
      }

      @Override
      public int hashCode() {
         return this.boxedHandler.hashCode();
      }
   }
}
