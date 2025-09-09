package mekanism.client.gui.element.tab.window;

import java.util.function.Consumer;
import java.util.function.Supplier;
import mekanism.client.gui.IGuiWrapper;
import mekanism.client.gui.element.GuiInsetElement;
import mekanism.client.gui.element.window.GuiWindow;
import net.minecraft.resources.ResourceLocation;
import org.jetbrains.annotations.NotNull;

public abstract class GuiWindowCreatorTab<DATA_SOURCE, ELEMENT extends GuiWindowCreatorTab<DATA_SOURCE, ELEMENT>> extends GuiInsetElement<DATA_SOURCE> {
   @NotNull
   private final Supplier<ELEMENT> elementSupplier;

   public GuiWindowCreatorTab(
      ResourceLocation overlay,
      IGuiWrapper gui,
      DATA_SOURCE dataSource,
      int x,
      int y,
      int height,
      int innerSize,
      boolean left,
      @NotNull Supplier<ELEMENT> elementSupplier
   ) {
      super(overlay, gui, dataSource, x, y, height, innerSize, left);
      this.elementSupplier = elementSupplier;
   }

   @Override
   public void onClick(double mouseX, double mouseY, int button) {
      GuiWindow window = this.createWindow();
      window.setTabListeners(this.getCloseListener(), this.getReAttachListener());
      this.disableTab();
      this.gui().addWindow(window);
   }

   @NotNull
   protected final Supplier<ELEMENT> getElementSupplier() {
      return this.elementSupplier;
   }

   public void adoptWindows(GuiWindow... windows) {
      for (GuiWindow window : windows) {
         window.setTabListeners(this.getCloseListener(), this.getReAttachListener());
      }
   }

   protected void disableTab() {
      this.f_93623_ = false;
   }

   protected Consumer<GuiWindow> getCloseListener() {
      return window -> this.elementSupplier.get().f_93623_ = true;
   }

   protected Consumer<GuiWindow> getReAttachListener() {
      return window -> this.elementSupplier.get().disableTab();
   }

   protected abstract GuiWindow createWindow();
}
