package mekanism.client.gui.element.window.filter;

import mekanism.api.text.ILangEntry;
import mekanism.client.gui.IGuiWrapper;
import mekanism.client.gui.element.GuiElementHolder;
import mekanism.client.gui.element.button.TranslationButton;
import mekanism.client.gui.element.window.GuiWindow;
import mekanism.common.MekanismLang;
import mekanism.common.inventory.container.SelectedWindowData;
import mekanism.common.tile.base.TileEntityMekanism;
import mekanism.common.tile.interfaces.ITileFilterHolder;
import net.minecraft.client.gui.GuiGraphics;
import org.jetbrains.annotations.Nullable;

public abstract class GuiFilterSelect<TILE extends TileEntityMekanism & ITileFilterHolder<?>> extends GuiWindow {
   private static final int FILTER_HEIGHT = 20;
   protected final TILE tile;

   protected GuiFilterSelect(IGuiWrapper gui, TILE tile, int filterCount) {
      super(gui, (gui.getWidth() - 152) / 2, 20, 152, 30 + filterCount * 20, SelectedWindowData.UNSPECIFIED);
      this.tile = tile;
      this.addChild(new GuiElementHolder(gui, 23, this.relativeY + 18, 130, 2 + filterCount * 20));
      int buttonY = this.relativeY + 19;
      buttonY = this.addFilterButton(buttonY, MekanismLang.BUTTON_ITEMSTACK_FILTER, this.getItemStackFilterCreator());
      buttonY = this.addFilterButton(buttonY, MekanismLang.BUTTON_TAG_FILTER, this.getTagFilterCreator());
      this.addFilterButton(buttonY, MekanismLang.BUTTON_MODID_FILTER, this.getModIDFilterCreator());
   }

   private int addFilterButton(int buttonY, ILangEntry translationHelper, @Nullable GuiFilterSelect.GuiFilterCreator<TILE> filterSupplier) {
      if (filterSupplier == null) {
         return buttonY;
      } else {
         this.addChild(new TranslationButton(this.gui(), 24, buttonY, 128, 20, translationHelper, () -> {
            this.gui().addWindow(filterSupplier.create(this.gui(), this.tile));
            this.close();
         }));
         return buttonY + 20;
      }
   }

   @Nullable
   protected GuiFilterSelect.GuiFilterCreator<TILE> getItemStackFilterCreator() {
      return null;
   }

   @Nullable
   protected GuiFilterSelect.GuiFilterCreator<TILE> getTagFilterCreator() {
      return null;
   }

   @Nullable
   protected GuiFilterSelect.GuiFilterCreator<TILE> getModIDFilterCreator() {
      return null;
   }

   @Override
   public void renderForeground(GuiGraphics guiGraphics, int mouseX, int mouseY) {
      super.renderForeground(guiGraphics, mouseX, mouseY);
      this.drawTitleText(guiGraphics, MekanismLang.CREATE_FILTER_TITLE.translate(new Object[0]), 6.0F);
   }

   @FunctionalInterface
   protected interface GuiFilterCreator<TILE extends TileEntityMekanism & ITileFilterHolder<?>> {
      GuiFilter<?, ?> create(IGuiWrapper gui, TILE tile);
   }
}
