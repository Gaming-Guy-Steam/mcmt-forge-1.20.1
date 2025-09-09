package mekanism.client.gui.element.tab.window;

import java.util.function.Consumer;
import java.util.function.Supplier;
import mekanism.client.SpecialColors;
import mekanism.client.gui.IGuiWrapper;
import mekanism.client.gui.element.window.GuiCraftingWindow;
import mekanism.client.gui.element.window.GuiWindow;
import mekanism.client.render.MekanismRenderer;
import mekanism.common.MekanismLang;
import mekanism.common.inventory.container.QIOItemViewerContainer;
import mekanism.common.util.MekanismUtils;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.network.chat.Component;
import org.jetbrains.annotations.NotNull;

public class GuiCraftingWindowTab extends GuiWindowCreatorTab<Void, GuiCraftingWindowTab> {
   private final boolean[] openWindows = new boolean[3];
   private final QIOItemViewerContainer container;
   private byte currentWindows;

   public GuiCraftingWindowTab(IGuiWrapper gui, Supplier<GuiCraftingWindowTab> elementSupplier, QIOItemViewerContainer container) {
      super(MekanismUtils.getResource(MekanismUtils.ResourceType.GUI_BUTTON, "crafting.png"), gui, null, -26, 34, 26, 18, true, elementSupplier);
      this.container = container;
   }

   @Override
   public void renderToolTip(@NotNull GuiGraphics guiGraphics, int mouseX, int mouseY) {
      super.renderToolTip(guiGraphics, mouseX, mouseY);
      this.displayTooltips(guiGraphics, mouseX, mouseY, new Component[]{MekanismLang.CRAFTING_TAB.translate(new Object[]{this.currentWindows, (byte)3})});
   }

   @Override
   protected void colorTab(GuiGraphics guiGraphics) {
      MekanismRenderer.color(guiGraphics, SpecialColors.TAB_CRAFTING_WINDOW.get());
   }

   @Override
   protected Consumer<GuiWindow> getCloseListener() {
      return window -> {
         GuiCraftingWindowTab tab = this.getElementSupplier().get();
         if (window instanceof GuiCraftingWindow craftingWindow) {
            tab.openWindows[craftingWindow.getIndex()] = false;
         }

         tab.currentWindows--;
         if (tab.currentWindows < 3) {
            tab.f_93623_ = true;
         }
      };
   }

   @Override
   protected Consumer<GuiWindow> getReAttachListener() {
      return super.getReAttachListener().andThen(window -> {
         if (window instanceof GuiCraftingWindow craftingWindow) {
            GuiCraftingWindowTab tab = this.getElementSupplier().get();
            tab.openWindows[craftingWindow.getIndex()] = true;
         }
      });
   }

   @Override
   protected void disableTab() {
      this.currentWindows++;
      if (this.currentWindows >= 3) {
         super.disableTab();
      }
   }

   @Override
   protected GuiWindow createWindow() {
      byte index = 0;

      for (int i = 0; i < this.openWindows.length; i++) {
         if (!this.openWindows[i]) {
            index = (byte)i;
            break;
         }
      }

      this.openWindows[index] = true;
      return new GuiCraftingWindow(this.gui(), this.getGuiWidth() / 2 - 78, 15, this.container, index);
   }
}
