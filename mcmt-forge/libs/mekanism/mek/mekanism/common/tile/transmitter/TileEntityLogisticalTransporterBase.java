package mekanism.common.tile.transmitter;

import java.util.Collections;
import java.util.EnumMap;
import java.util.List;
import java.util.Map;
import mekanism.api.annotations.NothingNullByDefault;
import mekanism.api.providers.IBlockProvider;
import mekanism.common.capabilities.item.CursedTransporterItemHandler;
import mekanism.common.capabilities.resolver.ICapabilityResolver;
import mekanism.common.content.network.transmitter.LogisticalTransporterBase;
import mekanism.common.content.transporter.TransporterStack;
import mekanism.common.lib.transmitter.ConnectionType;
import mekanism.common.util.TransporterUtils;
import mekanism.common.util.WorldUtils;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.capabilities.ForgeCapabilities;
import net.minecraftforge.common.util.LazyOptional;
import net.minecraftforge.items.IItemHandler;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public abstract class TileEntityLogisticalTransporterBase extends TileEntityTransmitter {
   protected TileEntityLogisticalTransporterBase(IBlockProvider blockProvider, BlockPos pos, BlockState state) {
      super(blockProvider, pos, state);
      this.addCapabilityResolver(new TileEntityLogisticalTransporterBase.TransporterCapabilityResolver());
   }

   protected abstract LogisticalTransporterBase createTransmitter(IBlockProvider blockProvider);

   public LogisticalTransporterBase getTransmitter() {
      return (LogisticalTransporterBase)super.getTransmitter();
   }

   public static void tickClient(Level level, BlockPos pos, BlockState state, TileEntityLogisticalTransporterBase transmitter) {
      transmitter.getTransmitter().onUpdateClient();
   }

   @Override
   public void onUpdateServer() {
      super.onUpdateServer();
      this.getTransmitter().onUpdateServer();
   }

   @Override
   public void blockRemoved() {
      super.blockRemoved();
      if (!this.isRemote()) {
         LogisticalTransporterBase transporter = this.getTransmitter();
         if (!transporter.isUpgrading()) {
            for (TransporterStack stack : transporter.getTransit()) {
               TransporterUtils.drop(transporter, stack);
            }
         }
      }
   }

   @Override
   public void sideChanged(@NotNull Direction side, @NotNull ConnectionType old, @NotNull ConnectionType type) {
      super.sideChanged(side, old, type);
      if ((type != ConnectionType.NONE || old == ConnectionType.PUSH) && (type != ConnectionType.PUSH || old == ConnectionType.NONE)) {
         if (old == ConnectionType.NONE && type != ConnectionType.PUSH || old == ConnectionType.PUSH && type != ConnectionType.NONE) {
            WorldUtils.notifyNeighborOfChange(this.f_58857_, side, this.f_58858_);
         }
      } else {
         this.invalidateCapability(ForgeCapabilities.ITEM_HANDLER, side);
         WorldUtils.notifyNeighborOfChange(this.f_58857_, side, this.f_58858_);
      }
   }

   @NothingNullByDefault
   private class TransporterCapabilityResolver implements ICapabilityResolver {
      private static final List<Capability<?>> SUPPORTED_CAPABILITY = Collections.singletonList(ForgeCapabilities.ITEM_HANDLER);
      private final Map<Direction, CursedTransporterItemHandler> cursedHandlers = new EnumMap<>(Direction.class);
      private final Map<Direction, LazyOptional<IItemHandler>> handlers = new EnumMap<>(Direction.class);

      @Override
      public List<Capability<?>> getSupportedCapabilities() {
         return SUPPORTED_CAPABILITY;
      }

      @Override
      public <T> LazyOptional<T> resolve(Capability<T> capability, @Nullable Direction side) {
         if (side == null) {
            return LazyOptional.empty();
         } else {
            LazyOptional<IItemHandler> cachedCapability = this.handlers.get(side);
            if (cachedCapability == null || !cachedCapability.isPresent()) {
               LogisticalTransporterBase transporter = TileEntityLogisticalTransporterBase.this.getTransmitter();
               if (!transporter.exposesInsertCap(side)) {
                  return LazyOptional.empty();
               }

               this.handlers
                  .put(
                     side,
                     cachedCapability = LazyOptional.of(
                        () -> this.cursedHandlers
                           .computeIfAbsent(
                              side,
                              s -> new CursedTransporterItemHandler(
                                 transporter,
                                 TileEntityLogisticalTransporterBase.this.f_58858_.m_121945_(s),
                                 () -> TileEntityLogisticalTransporterBase.this.f_58857_ == null
                                    ? -1L
                                    : TileEntityLogisticalTransporterBase.this.f_58857_.m_46467_()
                              )
                           )
                     )
                  );
            }

            return cachedCapability.cast();
         }
      }

      @Override
      public void invalidate(Capability<?> capability, @Nullable Direction side) {
         if (side != null) {
            this.invalidate(this.handlers.get(side));
         }
      }

      @Override
      public void invalidateAll() {
         this.handlers.values().forEach(this::invalidate);
      }

      protected void invalidate(@Nullable LazyOptional<?> cachedCapability) {
         if (cachedCapability != null && cachedCapability.isPresent()) {
            cachedCapability.invalidate();
         }
      }
   }
}
