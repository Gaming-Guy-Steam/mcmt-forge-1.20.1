package mekanism.common.integration.lookingat.jade;

import mekanism.api.chemical.ChemicalStack;
import mekanism.api.math.FloatingLong;
import mekanism.common.integration.lookingat.LookingAtHelper;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.Component.Serializer;
import net.minecraftforge.fluids.FluidStack;

public class JadeLookingAtHelper implements LookingAtHelper {
   static final String CHEMICAL_STACK = "chemical";
   static final String TEXT = "text";
   private final ListTag data = new ListTag();

   @Override
   public void addText(Component text) {
      CompoundTag textData = new CompoundTag();
      textData.m_128359_("text", Serializer.m_130703_(text));
      this.data.add(textData);
   }

   @Override
   public void addEnergyElement(FloatingLong energy, FloatingLong maxEnergy) {
      CompoundTag energyData = new CompoundTag();
      energyData.m_128359_("energy", energy.toString());
      energyData.m_128359_("max", maxEnergy.toString());
      this.data.add(energyData);
   }

   @Override
   public void addFluidElement(FluidStack stored, int capacity) {
      CompoundTag fluidData = new CompoundTag();
      fluidData.m_128365_("fluid", stored.writeToNBT(new CompoundTag()));
      fluidData.m_128405_("max", capacity);
      this.data.add(fluidData);
   }

   @Override
   public void addChemicalElement(ChemicalStack<?> stored, long capacity) {
      CompoundTag chemicalData = new CompoundTag();
      chemicalData.m_128365_("chemical", stored.write(new CompoundTag()));
      chemicalData.m_128356_("max", capacity);
      this.data.add(chemicalData);
   }

   public void finalizeData(CompoundTag data) {
      if (!this.data.isEmpty()) {
         data.m_128365_("mekData", this.data);
      }
   }
}
