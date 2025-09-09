package mekanism.client.gui.element.graph;

import it.unimi.dsi.fastutil.doubles.DoubleArrayList;
import it.unimi.dsi.fastutil.doubles.DoubleList;
import it.unimi.dsi.fastutil.doubles.DoubleListIterator;
import mekanism.api.math.MathUtils;
import mekanism.client.gui.IGuiWrapper;
import mekanism.client.gui.element.GuiElement;
import net.minecraft.network.chat.Component;

public class GuiDoubleGraph extends GuiGraph<DoubleList, GuiDoubleGraph.DoubleGraphDataHandler> {
   private double currentScale = 10.0;

   public GuiDoubleGraph(IGuiWrapper gui, int x, int y, int width, int height, GuiDoubleGraph.DoubleGraphDataHandler handler) {
      super(gui, x, y, width, height, new DoubleArrayList(), handler);
   }

   public void enableFixedScale(double scale) {
      this.fixedScale = true;
      this.currentScale = scale;
   }

   public void setMinScale(double minScale) {
      this.currentScale = minScale;
   }

   public void addData(double data) {
      if (this.graphData.size() == this.f_93618_ - 2) {
         this.graphData.removeDouble(0);
      }

      this.graphData.add(data);
      if (!this.fixedScale) {
         DoubleListIterator var3 = this.graphData.iterator();

         while (var3.hasNext()) {
            double i = (Double)var3.next();
            if (i > this.currentScale) {
               this.currentScale = i;
            }
         }
      }
   }

   @Override
   protected int getRelativeHeight(int index, int height) {
      double data = Math.min(this.currentScale, this.graphData.getDouble(index));
      return MathUtils.clampToInt(data * height / this.currentScale);
   }

   @Override
   protected Component getDataDisplay(int hoverIndex) {
      return this.dataHandler.getDataDisplay(this.graphData.getDouble(hoverIndex));
   }

   @Override
   public boolean hasPersistentData() {
      return true;
   }

   @Override
   public void syncFrom(GuiElement element) {
      super.syncFrom(element);
      DoubleListIterator var2 = ((GuiDoubleGraph)element).graphData.iterator();

      while (var2.hasNext()) {
         double data = (Double)var2.next();
         this.addData(data);
      }
   }

   public interface DoubleGraphDataHandler extends GuiGraph.GraphDataHandler {
      Component getDataDisplay(double data);
   }
}
