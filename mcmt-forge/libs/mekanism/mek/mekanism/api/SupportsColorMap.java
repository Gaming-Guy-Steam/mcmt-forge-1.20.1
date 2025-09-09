package mekanism.api;

public interface SupportsColorMap {
   default float getColor(int index) {
      return this.getRgbCode()[index] / 255.0F;
   }

   int[] getRgbCode();

   default float[] getRgbCodeFloat() {
      return new float[]{this.getColor(0), this.getColor(1), this.getColor(2)};
   }

   void setColorFromAtlas(int[] var1);
}
