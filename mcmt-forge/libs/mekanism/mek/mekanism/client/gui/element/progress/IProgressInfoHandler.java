package mekanism.client.gui.element.progress;

public interface IProgressInfoHandler {
   double getProgress();

   default boolean isActive() {
      return true;
   }

   public interface IBooleanProgressInfoHandler extends IProgressInfoHandler {
      boolean fillProgressBar();

      @Override
      default double getProgress() {
         return this.fillProgressBar() ? 1.0 : 0.0;
      }
   }
}
