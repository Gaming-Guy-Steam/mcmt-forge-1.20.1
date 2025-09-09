package mekanism.common.tier;

import mekanism.api.tier.BaseTier;
import mekanism.api.tier.ITier;
import mekanism.common.config.value.CachedDoubleValue;
import mekanism.common.lib.Color;
import mekanism.common.util.EnumUtils;

public enum ConductorTier implements ITier {
   BASIC(BaseTier.BASIC, 5.0, 1.0, 10.0, Color.rgbad(0.2, 0.2, 0.2, 1.0)),
   ADVANCED(BaseTier.ADVANCED, 5.0, 1.0, 400.0, Color.rgbad(0.2, 0.2, 0.2, 1.0)),
   ELITE(BaseTier.ELITE, 5.0, 1.0, 8000.0, Color.rgbad(0.2, 0.2, 0.2, 1.0)),
   ULTIMATE(BaseTier.ULTIMATE, 5.0, 1.0, 100000.0, Color.rgbad(0.2, 0.2, 0.2, 1.0));

   private final Color baseColor;
   private final double baseConduction;
   private final double baseHeatCapacity;
   private final double baseConductionInsulation;
   private final BaseTier baseTier;
   private CachedDoubleValue conductionReference;
   private CachedDoubleValue capacityReference;
   private CachedDoubleValue insulationReference;

   private ConductorTier(BaseTier tier, double conduction, double heatCapacity, double conductionInsulation, Color color) {
      this.baseConduction = conduction;
      this.baseHeatCapacity = heatCapacity;
      this.baseConductionInsulation = conductionInsulation;
      this.baseColor = color;
      this.baseTier = tier;
   }

   public static ConductorTier get(BaseTier tier) {
      for (ConductorTier transmitter : EnumUtils.CONDUCTOR_TIERS) {
         if (transmitter.getBaseTier() == tier) {
            return transmitter;
         }
      }

      return BASIC;
   }

   @Override
   public BaseTier getBaseTier() {
      return this.baseTier;
   }

   public double getInverseConduction() {
      return this.conductionReference == null ? this.getBaseConduction() : this.conductionReference.getOrDefault();
   }

   public double getInverseConductionInsulation() {
      return this.insulationReference == null ? this.getBaseConductionInsulation() : this.insulationReference.getOrDefault();
   }

   public double getHeatCapacity() {
      return this.capacityReference == null ? this.getBaseHeatCapacity() : this.capacityReference.getOrDefault();
   }

   public Color getBaseColor() {
      return this.baseColor;
   }

   public double getBaseConduction() {
      return this.baseConduction;
   }

   public double getBaseHeatCapacity() {
      return this.baseHeatCapacity;
   }

   public double getBaseConductionInsulation() {
      return this.baseConductionInsulation;
   }

   public void setConfigReference(CachedDoubleValue conductionReference, CachedDoubleValue capacityReference, CachedDoubleValue insulationReference) {
      this.conductionReference = conductionReference;
      this.capacityReference = capacityReference;
      this.insulationReference = insulationReference;
   }
}
