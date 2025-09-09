package mekanism.client.gui.element.graph;

import it.unimi.dsi.fastutil.longs.LongArrayList;
import it.unimi.dsi.fastutil.longs.LongList;
import it.unimi.dsi.fastutil.longs.LongListIterator;
import mekanism.api.math.MathUtils;
import mekanism.client.gui.IGuiWrapper;
import mekanism.client.gui.element.GuiElement;
import net.minecraft.network.chat.Component;

public class GuiLongGraph extends GuiGraph<LongList, GuiLongGraph.LongGraphDataHandler> {
   private long currentScale = 10L;

   public GuiLongGraph(IGuiWrapper gui, int x, int y, int width, int height, GuiLongGraph.LongGraphDataHandler handler) {
      super(gui, x, y, width, height, new LongArrayList(), handler);
   }

   public void enableFixedScale(long scale) {
      this.fixedScale = true;
      this.currentScale = scale;
   }

   public void setMinScale(long minScale) {
      this.currentScale = minScale;
   }

   public void addData(long data) {
      if (this.graphData.size() == this.f_93618_ - 2) {
         this.graphData.removeLong(0);
      }

      this.graphData.add(data);
      if (!this.fixedScale) {
         LongListIterator var3 = this.graphData.iterator();

         while (var3.hasNext()) {
            long i = (Long)var3.next();
            if (i > this.currentScale) {
               this.currentScale = i;
            }
         }
      }
   }

   @Override
   protected int getRelativeHeight(int index, int height) {
      long data = Math.min(this.currentScale, this.graphData.getLong(index));
      return MathUtils.clampToInt((double)(data * height) / this.currentScale);
   }

   @Override
   protected Component getDataDisplay(int hoverIndex) {
      return this.dataHandler.getDataDisplay(this.graphData.getLong(hoverIndex));
   }

   @Override
   public boolean hasPersistentData() {
      return true;
   }

   @Override
   public void syncFrom(GuiElement element) {
      super.syncFrom(element);
      LongListIterator var2 = ((GuiLongGraph)element).graphData.iterator();

      while (var2.hasNext()) {
         long data = (Long)var2.next();
         this.addData(data);
      }
   }

   public interface LongGraphDataHandler extends GuiGraph.GraphDataHandler {
      Component getDataDisplay(long data);
   }
}
