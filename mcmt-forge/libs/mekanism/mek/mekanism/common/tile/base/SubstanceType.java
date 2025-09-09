package mekanism.common.tile.base;

import java.util.List;
import java.util.function.Function;
import mekanism.api.DataHandlerUtils;
import net.minecraft.nbt.CompoundTag;
import net.minecraftforge.common.util.INBTSerializable;

public enum SubstanceType {
   ENERGY("EnergyContainers", tile -> tile.getEnergyContainers(null)),
   FLUID("FluidTanks", tile -> tile.getFluidTanks(null)),
   GAS("GasTanks", tile -> tile.getGasTanks(null)),
   INFUSION("InfusionTanks", tile -> tile.getInfusionTanks(null)),
   PIGMENT("PigmentTanks", tile -> tile.getPigmentTanks(null)),
   SLURRY("SlurryTanks", tile -> tile.getSlurryTanks(null)),
   HEAT("HeatCapacitors", tile -> tile.getHeatCapacitors(null));

   private final String containerTag;
   private final Function<TileEntityMekanism, List<? extends INBTSerializable<CompoundTag>>> containerSupplier;

   private SubstanceType(String containerTag, Function<TileEntityMekanism, List<? extends INBTSerializable<CompoundTag>>> containerSupplier) {
      this.containerTag = containerTag;
      this.containerSupplier = containerSupplier;
   }

   public void write(TileEntityMekanism tile, CompoundTag tag) {
      tag.m_128365_(this.containerTag, DataHandlerUtils.writeContainers(this.containerSupplier.apply(tile)));
   }

   public void read(TileEntityMekanism tile, CompoundTag tag) {
      DataHandlerUtils.readContainers(this.containerSupplier.apply(tile), tag.m_128437_(this.containerTag, 10));
   }

   public String getContainerTag() {
      return this.containerTag;
   }

   public List<? extends INBTSerializable<CompoundTag>> getContainers(TileEntityMekanism tile) {
      return this.containerSupplier.apply(tile);
   }

   public boolean canHandle(TileEntityMekanism tile) {
      return switch (this) {
         case ENERGY -> tile.canHandleEnergy();
         case FLUID -> tile.canHandleFluid();
         case GAS -> tile.canHandleGas();
         case INFUSION -> tile.canHandleInfusion();
         case PIGMENT -> tile.canHandlePigment();
         case SLURRY -> tile.canHandleSlurry();
         case HEAT -> tile.canHandleHeat();
      };
   }
}
