package mekanism.common.capabilities.energy;

import mekanism.api.IContentsListener;
import mekanism.api.annotations.NothingNullByDefault;
import mekanism.api.math.FloatingLong;
import mekanism.common.block.attribute.AttributeEnergy;
import mekanism.common.config.MekanismConfig;
import mekanism.common.tile.machine.TileEntityDigitalMiner;
import net.minecraft.world.level.Level;
import org.jetbrains.annotations.Nullable;

@NothingNullByDefault
public class MinerEnergyContainer extends MachineEnergyContainer<TileEntityDigitalMiner> {
   private FloatingLong minerEnergyPerTick = this.getBaseEnergyPerTick();

   public static MinerEnergyContainer input(TileEntityDigitalMiner tile, @Nullable IContentsListener listener) {
      AttributeEnergy electricBlock = validateBlock(tile);
      return new MinerEnergyContainer(electricBlock.getStorage(), electricBlock.getUsage(), tile, listener);
   }

   private MinerEnergyContainer(FloatingLong maxEnergy, FloatingLong energyPerTick, TileEntityDigitalMiner tile, @Nullable IContentsListener listener) {
      super(maxEnergy, energyPerTick, notExternal, alwaysTrue, tile, listener);
   }

   @Override
   public void setEnergyPerTick(FloatingLong energyPerTick) {
      super.setEnergyPerTick(energyPerTick);
      this.minerEnergyPerTick = energyPerTick;
   }

   @Override
   public FloatingLong getEnergyPerTick() {
      return this.minerEnergyPerTick;
   }

   @Override
   public void updateEnergyPerTick() {
      super.updateEnergyPerTick();
      this.updateMinerEnergyPerTick();
   }

   public void updateMinerEnergyPerTick() {
      this.minerEnergyPerTick = super.getEnergyPerTick();
      if (this.tile.getSilkTouch()) {
         this.minerEnergyPerTick = this.minerEnergyPerTick.multiply((long)MekanismConfig.general.minerSilkMultiplier.get());
      }

      double radiusRange = MekanismConfig.general.minerMaxRadius.get() - 10;
      Level level = this.tile.m_58904_();
      double heightRange;
      if (level == null) {
         heightRange = 195.0;
      } else {
         heightRange = level.m_141928_() - 1 - 60;
      }

      double radiusCost;
      if (radiusRange == 0.0) {
         radiusCost = 0.0;
      } else {
         radiusCost = Math.max((this.tile.getRadius() - 10) / radiusRange, 0.0);
      }

      double heightCost;
      if (heightRange == 0.0) {
         heightCost = 0.0;
      } else {
         heightCost = Math.max((this.tile.getMaxY() - this.tile.getMinY() - 60) / heightRange, 0.0);
      }

      this.minerEnergyPerTick = this.minerEnergyPerTick.multiply((1.0 + radiusCost) * (1.0 + heightCost));
   }
}
