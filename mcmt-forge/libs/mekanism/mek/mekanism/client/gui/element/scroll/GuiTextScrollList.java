package mekanism.client.gui.element.scroll;

import java.util.ArrayList;
import java.util.List;
import mekanism.api.text.TextComponentUtil;
import mekanism.client.gui.IGuiWrapper;
import mekanism.client.gui.element.GuiElement;
import mekanism.client.gui.element.GuiInnerScreen;
import net.minecraft.client.gui.GuiGraphics;
import org.jetbrains.annotations.Nullable;

public class GuiTextScrollList extends GuiScrollList {
   private List<String> textEntries = new ArrayList<>();
   private int selected = -1;

   public GuiTextScrollList(IGuiWrapper gui, int x, int y, int width, int height) {
      super(gui, x, y, width, height, 10, GuiInnerScreen.SCREEN, GuiInnerScreen.SCREEN_SIZE);
   }

   @Override
   protected int getMaxElements() {
      return this.textEntries.size();
   }

   @Override
   public boolean hasSelection() {
      return this.selected != -1;
   }

   @Override
   protected void setSelected(int index) {
      this.selected = index;
   }

   public int getSelection() {
      return this.selected;
   }

   @Override
   public void clearSelection() {
      this.selected = -1;
   }

   public void setText(@Nullable List<String> text) {
      if (text == null) {
         this.textEntries.clear();
      } else {
         if (this.selected > text.size() - 1) {
            this.clearSelection();
         }

         this.textEntries = text;
      }

      if (!this.needsScrollBars()) {
         this.scroll = 0.0;
      }
   }

   @Override
   public void renderForeground(GuiGraphics guiGraphics, int mouseX, int mouseY) {
      super.renderForeground(guiGraphics, mouseX, mouseY);
      if (!this.textEntries.isEmpty()) {
         int scrollIndex = this.getCurrentSelection();
         int focusedElements = this.getFocusedElements();
         int maxElements = this.getMaxElements();

         for (int i = 0; i < focusedElements; i++) {
            int index = scrollIndex + i;
            if (index < maxElements) {
               this.drawScaledTextScaledBound(
                  guiGraphics,
                  TextComponentUtil.getString(this.textEntries.get(index)),
                  this.relativeX + 2,
                  this.relativeY + 2 + this.elementHeight * i,
                  this.screenTextColor(),
                  this.barXShift - 2,
                  0.8F
               );
            }
         }
      }
   }

   @Override
   public void renderElements(GuiGraphics guiGraphics, int mouseX, int mouseY, float partialTicks) {
      int scrollIndex = this.getCurrentSelection();
      if (this.selected != -1 && this.selected >= scrollIndex && this.selected <= scrollIndex + this.getFocusedElements() - 1) {
         guiGraphics.m_280411_(
            this.getResource(),
            this.relativeX + 1,
            this.relativeY + 1 + (this.selected - scrollIndex) * this.elementHeight,
            this.barXShift - 2,
            this.elementHeight,
            4.0F,
            2.0F,
            2,
            2,
            6,
            6
         );
      }
   }

   @Override
   public void syncFrom(GuiElement element) {
      GuiTextScrollList old = (GuiTextScrollList)element;
      this.setText(old.textEntries);
      this.setSelected(old.getSelection());
      super.syncFrom(element);
   }
}
