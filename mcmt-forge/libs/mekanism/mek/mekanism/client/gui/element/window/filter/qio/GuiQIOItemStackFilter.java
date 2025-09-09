package mekanism.client.gui.element.window.filter.qio;

import mekanism.client.gui.IGuiWrapper;
import mekanism.client.gui.element.button.MekanismImageButton;
import mekanism.client.gui.element.window.filter.GuiItemStackFilter;
import mekanism.common.MekanismLang;
import mekanism.common.content.qio.filter.QIOItemStackFilter;
import mekanism.common.tile.qio.TileEntityQIOFilterHandler;
import mekanism.common.util.text.BooleanStateDisplay;
import net.minecraft.client.gui.GuiGraphics;
import org.jetbrains.annotations.Nullable;

public class GuiQIOItemStackFilter extends GuiItemStackFilter<QIOItemStackFilter, TileEntityQIOFilterHandler> implements GuiQIOFilterHelper {
   public static GuiQIOItemStackFilter create(IGuiWrapper gui, TileEntityQIOFilterHandler tile) {
      return new GuiQIOItemStackFilter(gui, (gui.getWidth() - 185) / 2, 15, tile, null);
   }

   public static GuiQIOItemStackFilter edit(IGuiWrapper gui, TileEntityQIOFilterHandler tile, QIOItemStackFilter filter) {
      return new GuiQIOItemStackFilter(gui, (gui.getWidth() - 185) / 2, 15, tile, filter);
   }

   private GuiQIOItemStackFilter(IGuiWrapper gui, int x, int y, TileEntityQIOFilterHandler tile, @Nullable QIOItemStackFilter origFilter) {
      super(gui, x, y, 185, 90, tile, origFilter);
   }

   @Override
   protected int getLeftButtonX() {
      return this.relativeX + 29;
   }

   @Override
   protected void init() {
      super.init();
      this.addChild(
         new MekanismImageButton(
            this.gui(),
            this.relativeX + 148,
            this.relativeY + 18,
            11,
            14,
            this.getButtonLocation("fuzzy"),
            () -> this.filter.fuzzyMode = !this.filter.fuzzyMode,
            this.getOnHover(MekanismLang.FUZZY_MODE)
         )
      );
   }

   protected QIOItemStackFilter createNewFilter() {
      return new QIOItemStackFilter();
   }

   @Override
   public void renderForeground(GuiGraphics guiGraphics, int mouseX, int mouseY) {
      super.renderForeground(guiGraphics, mouseX, mouseY);
      this.drawString(
         guiGraphics, BooleanStateDisplay.OnOff.of(this.filter.fuzzyMode).getTextComponent(), this.relativeX + 161, this.relativeY + 20, this.titleTextColor()
      );
   }
}
