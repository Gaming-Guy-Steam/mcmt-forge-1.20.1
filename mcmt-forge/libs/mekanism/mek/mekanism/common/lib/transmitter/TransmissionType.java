package mekanism.common.lib.transmitter;

import mekanism.api.annotations.NothingNullByDefault;
import mekanism.api.text.IHasTranslationKey;
import mekanism.api.text.ILangEntry;
import mekanism.common.MekanismLang;
import mekanism.common.content.network.transmitter.Transmitter;
import mekanism.common.tile.transmitter.TileEntityTransmitter;

@NothingNullByDefault
public enum TransmissionType implements IHasTranslationKey {
   ENERGY("EnergyNetwork", "energy", MekanismLang.TRANSMISSION_TYPE_ENERGY),
   FLUID("FluidNetwork", "fluids", MekanismLang.TRANSMISSION_TYPE_FLUID),
   GAS("GasNetwork", "gases", MekanismLang.TRANSMISSION_TYPE_GAS),
   INFUSION("InfusionNetwork", "infuse_types", MekanismLang.TRANSMISSION_TYPE_INFUSION),
   PIGMENT("PigmentNetwork", "pigments", MekanismLang.TRANSMISSION_TYPE_PIGMENT),
   SLURRY("SlurryNetwork", "slurries", MekanismLang.TRANSMISSION_TYPE_SLURRY),
   ITEM("InventoryNetwork", "items", MekanismLang.TRANSMISSION_TYPE_ITEM),
   HEAT("HeatNetwork", "heat", MekanismLang.TRANSMISSION_TYPE_HEAT);

   private final String name;
   private final String transmission;
   private final ILangEntry langEntry;

   private TransmissionType(String name, String transmission, ILangEntry langEntry) {
      this.name = name;
      this.transmission = transmission;
      this.langEntry = langEntry;
   }

   public String getName() {
      return this.name;
   }

   public String getTransmission() {
      return this.transmission;
   }

   public ILangEntry getLangEntry() {
      return this.langEntry;
   }

   @Override
   public String getTranslationKey() {
      return this.langEntry.getTranslationKey();
   }

   public boolean isChemical() {
      return this == GAS || this == INFUSION || this == PIGMENT || this == SLURRY;
   }

   public boolean checkTransmissionType(Transmitter<?, ?, ?> transmitter) {
      return transmitter.getSupportedTransmissionTypes().contains(this);
   }

   public boolean checkTransmissionType(TileEntityTransmitter transmitter) {
      return this.checkTransmissionType(transmitter.getTransmitter());
   }
}
