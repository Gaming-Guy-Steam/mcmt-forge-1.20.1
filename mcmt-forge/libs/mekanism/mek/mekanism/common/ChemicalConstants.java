package mekanism.common;

import mekanism.common.base.IChemicalConstant;

public enum ChemicalConstants implements IChemicalConstant {
   HYDROGEN("hydrogen", -1, 0, 20.28F, 70.85F),
   OXYGEN("oxygen", -9641217, 0, 90.19F, 1141.0F),
   CHLORINE("chlorine", -3151872, 0, 207.15F, 1422.92F),
   SULFUR_DIOXIDE("sulfur_dioxide", -5661296, 0, 263.05F, 1400.0F),
   SULFUR_TRIOXIDE("sulfur_trioxide", -3249044, 0, 318.0F, 1920.0F),
   SULFURIC_ACID("sulfuric_acid", -8224725, 0, 300.0F, 1840.0F),
   HYDROGEN_CHLORIDE("hydrogen_chloride", -5705239, 0, 188.1F, 821.43F),
   ETHENE("ethene", -1389319, 0, 169.45F, 577.0F),
   SODIUM("sodium", -1442060, 0, 370.944F, 927.0F),
   SUPERHEATED_SODIUM("superheated_sodium", -3042199, 0, 2000.0F, 927.0F),
   LITHIUM("lithium", -1334272, 0, 453.65F, 512.0F),
   HYDROFLUORIC_ACID("hydrofluoric_acid", -3749955, 0, 189.6F, 1150.0F),
   URANIUM_OXIDE("uranium_oxide", -1968781, 0, 3138.15F, 10970.0F),
   URANIUM_HEXAFLUORIDE("uranium_hexafluoride", -8349344, 0, 337.2F, 5090.0F);

   private final String name;
   private final int color;
   private final int lightLevel;
   private final float temperature;
   private final float density;

   private ChemicalConstants(String name, int color, int lightLevel, float temperature, float density) {
      this.name = name;
      this.color = color;
      this.lightLevel = lightLevel;
      this.temperature = temperature;
      this.density = density;
   }

   @Override
   public String getName() {
      return this.name;
   }

   @Override
   public int getColor() {
      return this.color;
   }

   @Override
   public float getTemperature() {
      return this.temperature;
   }

   @Override
   public float getDensity() {
      return this.density;
   }

   @Override
   public int getLightLevel() {
      return this.lightLevel;
   }
}
