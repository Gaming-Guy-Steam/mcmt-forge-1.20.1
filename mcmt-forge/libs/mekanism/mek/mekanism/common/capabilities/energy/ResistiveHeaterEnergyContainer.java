package mekanism.common.capabilities.energy;

import java.util.function.Predicate;
import mekanism.api.AutomationType;
import mekanism.api.IContentsListener;
import mekanism.api.annotations.NothingNullByDefault;
import mekanism.api.math.FloatingLong;
import mekanism.common.block.attribute.AttributeEnergy;
import mekanism.common.tile.machine.TileEntityResistiveHeater;
import mekanism.common.util.NBTUtils;
import net.minecraft.nbt.CompoundTag;
import org.jetbrains.annotations.Nullable;

@NothingNullByDefault
public class ResistiveHeaterEnergyContainer extends MachineEnergyContainer<TileEntityResistiveHeater> {
   public static ResistiveHeaterEnergyContainer input(TileEntityResistiveHeater tile, @Nullable IContentsListener listener) {
      AttributeEnergy electricBlock = validateBlock(tile);
      return new ResistiveHeaterEnergyContainer(electricBlock.getStorage(), electricBlock.getUsage(), notExternal, alwaysTrue, tile, listener);
   }

   private ResistiveHeaterEnergyContainer(
      FloatingLong maxEnergy,
      FloatingLong energyPerTick,
      Predicate<AutomationType> canExtract,
      Predicate<AutomationType> canInsert,
      TileEntityResistiveHeater tile,
      @Nullable IContentsListener listener
   ) {
      super(maxEnergy, energyPerTick, canExtract, canInsert, tile, listener);
   }

   @Override
   public boolean adjustableRates() {
      return true;
   }

   public void updateEnergyUsage(FloatingLong energyUsage) {
      this.currentEnergyPerTick = energyUsage;
      this.setMaxEnergy(energyUsage.multiply(400L));
   }

   @Override
   public CompoundTag serializeNBT() {
      CompoundTag nbt = super.serializeNBT();
      nbt.m_128359_("energyUsage", this.getEnergyPerTick().toString());
      return nbt;
   }

   @Override
   public void deserializeNBT(CompoundTag nbt) {
      super.deserializeNBT(nbt);
      NBTUtils.setFloatingLongIfPresent(nbt, "energyUsage", this::updateEnergyUsage);
   }
}
