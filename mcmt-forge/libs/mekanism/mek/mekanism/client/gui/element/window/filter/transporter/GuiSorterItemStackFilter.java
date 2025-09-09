package mekanism.client.gui.element.window.filter.transporter;

import mekanism.client.gui.IGuiWrapper;
import mekanism.client.gui.element.button.MekanismImageButton;
import mekanism.client.gui.element.text.GuiTextField;
import mekanism.client.gui.element.window.filter.GuiItemStackFilter;
import mekanism.common.MekanismLang;
import mekanism.common.content.transporter.SorterItemStackFilter;
import mekanism.common.tile.TileEntityLogisticalSorter;
import mekanism.common.util.text.BooleanStateDisplay;
import net.minecraft.client.gui.GuiGraphics;
import org.jetbrains.annotations.Nullable;

public class GuiSorterItemStackFilter extends GuiItemStackFilter<SorterItemStackFilter, TileEntityLogisticalSorter> implements GuiSorterFilterHelper {
   private GuiTextField minField;
   private GuiTextField maxField;

   public static GuiSorterItemStackFilter create(IGuiWrapper gui, TileEntityLogisticalSorter tile) {
      return new GuiSorterItemStackFilter(gui, (gui.getWidth() - 195) / 2, 30, tile, null);
   }

   public static GuiSorterItemStackFilter edit(IGuiWrapper gui, TileEntityLogisticalSorter tile, SorterItemStackFilter filter) {
      return new GuiSorterItemStackFilter(gui, (gui.getWidth() - 195) / 2, 30, tile, filter);
   }

   private GuiSorterItemStackFilter(IGuiWrapper gui, int x, int y, TileEntityLogisticalSorter tile, @Nullable SorterItemStackFilter origFilter) {
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
      this.addChild(
         new MekanismImageButton(
            this.gui(),
            this.relativeX + 148,
            this.relativeY + 68,
            11,
            14,
            this.getButtonLocation("fuzzy"),
            () -> this.filter.fuzzyMode = !this.filter.fuzzyMode,
            this.getOnHover(MekanismLang.FUZZY_MODE)
         )
      );
   }

   @Override
   protected void validateAndSave() {
      validateAndSaveSorterFilter(this, this.minField, this.maxField);
   }

   protected SorterItemStackFilter createNewFilter() {
      return new SorterItemStackFilter();
   }

   @Override
   public void renderForeground(GuiGraphics guiGraphics, int mouseX, int mouseY) {
      super.renderForeground(guiGraphics, mouseX, mouseY);
      this.renderSorterForeground(guiGraphics, this.filter, this.tile.getSingleItem());
      this.drawString(
         guiGraphics, BooleanStateDisplay.OnOff.of(this.filter.fuzzyMode).getTextComponent(), this.relativeX + 161, this.relativeY + 71, this.titleTextColor()
      );
   }
}
