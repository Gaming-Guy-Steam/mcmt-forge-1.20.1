package mekanism.client.gui.element.window.filter.transporter;

import mekanism.client.gui.IGuiWrapper;
import mekanism.client.gui.element.text.GuiTextField;
import mekanism.client.gui.element.window.filter.GuiTagFilter;
import mekanism.common.content.transporter.SorterTagFilter;
import mekanism.common.tile.TileEntityLogisticalSorter;
import net.minecraft.client.gui.GuiGraphics;
import org.jetbrains.annotations.Nullable;

public class GuiSorterTagFilter extends GuiTagFilter<SorterTagFilter, TileEntityLogisticalSorter> implements GuiSorterFilterHelper {
   private GuiTextField minField;
   private GuiTextField maxField;

   public static GuiSorterTagFilter create(IGuiWrapper gui, TileEntityLogisticalSorter tile) {
      return new GuiSorterTagFilter(gui, (gui.getWidth() - 182) / 2, 30, tile, null);
   }

   public static GuiSorterTagFilter edit(IGuiWrapper gui, TileEntityLogisticalSorter tile, SorterTagFilter filter) {
      return new GuiSorterTagFilter(gui, (gui.getWidth() - 182) / 2, 30, tile, filter);
   }

   private GuiSorterTagFilter(IGuiWrapper gui, int x, int y, TileEntityLogisticalSorter tile, @Nullable SorterTagFilter origFilter) {
      super(gui, x, y, 195, 90, tile, origFilter);
   }

   @Override
   protected int getLeftButtonX() {
      return this.relativeX + 24;
   }

   @Override
   protected void init() {
      super.init();
      this.addSorterDefaults(this.gui(), this.filter, this.getSlotOffset(), x$0 -> this.addChild(x$0), this.tile::getSingleItem, (min, max) -> {
         this.minField = min;
         this.maxField = max;
      });
   }

   @Override
   protected void validateAndSave() {
      if (this.text.getText().isEmpty() || this.setText()) {
         validateAndSaveSorterFilter(this, this.minField, this.maxField);
      }
   }

   protected SorterTagFilter createNewFilter() {
      return new SorterTagFilter();
   }

   @Override
   public void renderForeground(GuiGraphics guiGraphics, int mouseX, int mouseY) {
      super.renderForeground(guiGraphics, mouseX, mouseY);
      this.renderSorterForeground(guiGraphics, this.filter, this.tile.getSingleItem());
   }
}
