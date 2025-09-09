package mekanism.common.tile.transmitter;

import mekanism.api.providers.IBlockProvider;
import mekanism.api.tier.BaseTier;
import mekanism.client.model.data.TransmitterModelData;
import mekanism.common.block.states.BlockStateHelper;
import mekanism.common.block.states.TransmitterType;
import mekanism.common.content.network.transmitter.LogisticalTransporter;
import mekanism.common.registries.MekanismBlocks;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.block.state.BlockState;
import org.jetbrains.annotations.NotNull;

public class TileEntityLogisticalTransporter extends TileEntityLogisticalTransporterBase {
   public TileEntityLogisticalTransporter(IBlockProvider blockProvider, BlockPos pos, BlockState state) {
      super(blockProvider, pos, state);
   }

   protected LogisticalTransporter createTransmitter(IBlockProvider blockProvider) {
      return new LogisticalTransporter(blockProvider, this);
   }

   public LogisticalTransporter getTransmitter() {
      return (LogisticalTransporter)super.getTransmitter();
   }

   @Override
   public TransmitterType getTransmitterType() {
      return TransmitterType.LOGISTICAL_TRANSPORTER;
   }

   @Override
   protected void updateModelData(TransmitterModelData modelData) {
      super.updateModelData(modelData);
      modelData.setHasColor(this.getTransmitter().getColor() != null);
   }

   @NotNull
   @Override
   protected BlockState upgradeResult(@NotNull BlockState current, @NotNull BaseTier tier) {
      return BlockStateHelper.copyStateData(current, switch (tier) {
         case BASIC -> MekanismBlocks.BASIC_LOGISTICAL_TRANSPORTER;
         case ADVANCED -> MekanismBlocks.ADVANCED_LOGISTICAL_TRANSPORTER;
         case ELITE -> MekanismBlocks.ELITE_LOGISTICAL_TRANSPORTER;
         case ULTIMATE -> MekanismBlocks.ULTIMATE_LOGISTICAL_TRANSPORTER;
         default -> null;
      });
   }
}
