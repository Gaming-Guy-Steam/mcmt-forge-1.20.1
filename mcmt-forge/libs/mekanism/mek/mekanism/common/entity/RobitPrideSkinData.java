package mekanism.common.entity;

import java.util.Locale;

public enum RobitPrideSkinData {
   PRIDE(-3071447, -620002, -139238, -16746176, -14401394, -9229950),
   LESBIAN(-2806528, -1415136, -356537, -142925, -1460010, -3645794, -5093753, -6094238),
   TRANS(-10760454, -677448, -1, -677448),
   ARO(-12736699, -5778566, -1, -5526613, -16777216),
   ACE(-16777216, -5526613, -1, -8509313),
   BI(-2812301, -2812301, -6598761, -14794341, -14794341),
   ENBY(-528841, -1, -7708761, -16777216),
   PAN(-1237108, -1237108, -206076, -206076, -12014109, -12014109),
   GAY(-16282256, -14233942, -6690622, -1, -8671773, -11515445, -12707208),
   AGENDER(-16777216, -6710887, -1, -6172809, -1, -6710887),
   GENDERFLUID(-170832, -1, -6737204, -16777216, -12490271);

   private final int[] color;

   private RobitPrideSkinData(int... color) {
      this.color = color;
   }

   public int[] getColor() {
      return this.color;
   }

   public String lowerCaseName() {
      return this.name().toLowerCase(Locale.ROOT);
   }
}
