package mekanism.client.gui.element.button;

import java.util.List;
import java.util.function.Function;
import java.util.function.IntConsumer;
import java.util.function.IntSupplier;
import java.util.function.ObjIntConsumer;
import mekanism.api.text.EnumColor;
import mekanism.client.gui.GuiUtils;
import mekanism.client.gui.IGuiWrapper;
import mekanism.client.render.MekanismRenderer;
import mekanism.common.MekanismLang;
import mekanism.common.content.filter.FilterManager;
import mekanism.common.content.filter.IFilter;
import mekanism.common.content.filter.IItemStackFilter;
import mekanism.common.content.filter.IModIDFilter;
import mekanism.common.content.filter.ITagFilter;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.ItemStack;
import org.jetbrains.annotations.NotNull;

public class MovableFilterButton extends FilterButton {
   private final FilterSelectButton upButton;
   private final FilterSelectButton downButton;

   public MovableFilterButton(
      IGuiWrapper gui,
      int x,
      int y,
      int index,
      IntSupplier filterIndex,
      FilterManager<?> filterManager,
      IntConsumer upButtonPress,
      IntConsumer downButtonPress,
      ObjIntConsumer<IFilter<?>> onPress,
      IntConsumer toggleButtonPress,
      Function<IFilter<?>, List<ItemStack>> renderStackSupplier
   ) {
      this(gui, x, y, 96, 29, index, filterIndex, filterManager, upButtonPress, downButtonPress, onPress, toggleButtonPress, renderStackSupplier);
   }

   public MovableFilterButton(
      IGuiWrapper gui,
      int x,
      int y,
      int width,
      int height,
      int index,
      IntSupplier filterIndex,
      FilterManager<?> filterManager,
      IntConsumer upButtonPress,
      IntConsumer downButtonPress,
      ObjIntConsumer<IFilter<?>> onPress,
      IntConsumer toggleButtonPress,
      Function<IFilter<?>, List<ItemStack>> renderStackSupplier
   ) {
      super(gui, x, y, width, height, index, filterIndex, filterManager, onPress, toggleButtonPress, renderStackSupplier);
      int arrowX = this.relativeX + width - 12;
      this.upButton = this.addPositionOnlyChild(
         new FilterSelectButton(
            gui,
            arrowX,
            this.relativeY + 1,
            false,
            () -> upButtonPress.accept(this.getActualIndex()),
            (onHover, guiGraphics, mouseX, mouseY) -> this.displayTooltips(
               guiGraphics, mouseX, mouseY, new Component[]{MekanismLang.MOVE_UP.translate(new Object[0]), MekanismLang.MOVE_TO_TOP.translate(new Object[0])}
            )
         )
      );
      this.downButton = this.addPositionOnlyChild(
         new FilterSelectButton(
            gui,
            arrowX,
            this.relativeY + height - 8,
            true,
            () -> downButtonPress.accept(this.getActualIndex()),
            (onHover, guiGraphics, mouseX, mouseY) -> this.displayTooltips(
               guiGraphics,
               mouseX,
               mouseY,
               new Component[]{MekanismLang.MOVE_DOWN.translate(new Object[0]), MekanismLang.MOVE_TO_BOTTOM.translate(new Object[0])}
            )
         )
      );
   }

   @Override
   protected int getToggleXShift() {
      return 13;
   }

   @Override
   protected int getToggleYShift() {
      return 1;
   }

   @Override
   public void onClick(double mouseX, double mouseY, int button) {
      if (this.upButton.m_7972_(button) && this.upButton.m_5953_(mouseX, mouseY)) {
         this.upButton.onClick(mouseX, mouseY, button);
      } else if (this.downButton.m_7972_(button) && this.downButton.m_5953_(mouseX, mouseY)) {
         this.downButton.onClick(mouseX, mouseY, button);
      } else if (super.m_7972_(button)) {
         super.onClick(mouseX, mouseY, button);
      }
   }

   @Override
   public boolean m_7972_(int button) {
      return super.m_7972_(button) || this.upButton.m_7972_(button) || this.downButton.m_7972_(button);
   }

   @Override
   public void renderForeground(GuiGraphics guiGraphics, int mouseX, int mouseY) {
      int xAxis = mouseX - this.getGuiLeft();
      int yAxis = mouseY - this.getGuiTop();
      if (this.upButton.isMouseOverCheckWindows(mouseX, mouseY)) {
         this.upButton.renderToolTip(guiGraphics, xAxis, yAxis);
      } else if (this.downButton.isMouseOverCheckWindows(mouseX, mouseY)) {
         this.downButton.renderToolTip(guiGraphics, xAxis, yAxis);
      }

      super.renderForeground(guiGraphics, mouseX, mouseY);
   }

   @Override
   protected void setVisibility(boolean visible) {
      super.setVisibility(visible);
      if (visible) {
         this.updateButtonVisibility();
      } else {
         this.upButton.f_93624_ = false;
         this.downButton.f_93624_ = false;
      }
   }

   private void updateButtonVisibility() {
      int index = this.getActualIndex();
      IFilter<?> filter = this.getFilter();
      this.upButton.f_93624_ = filter != null && index > 0;
      this.downButton.f_93624_ = filter != null && index < this.filterManager.count() - 1;
   }

   @Override
   public void drawBackground(@NotNull GuiGraphics guiGraphics, int mouseX, int mouseY, float partialTicks) {
      super.drawBackground(guiGraphics, mouseX, mouseY, partialTicks);
      IFilter<?> filter = this.getFilter();
      EnumColor color;
      if (filter instanceof IItemStackFilter) {
         color = EnumColor.INDIGO;
      } else if (filter instanceof ITagFilter) {
         color = EnumColor.BRIGHT_GREEN;
      } else if (filter instanceof IModIDFilter) {
         color = EnumColor.RED;
      } else {
         color = null;
      }

      if (color != null) {
         GuiUtils.fill(
            guiGraphics, this.getButtonX(), this.getButtonY(), this.getButtonWidth(), this.getButtonHeight(), MekanismRenderer.getColorARGB(color, 0.5F)
         );
      }

      this.updateButtonVisibility();
      this.upButton.onDrawBackground(guiGraphics, mouseX, mouseY, partialTicks);
      this.downButton.onDrawBackground(guiGraphics, mouseX, mouseY, partialTicks);
   }

   @Override
   protected int getMaxLength() {
      return super.getMaxLength() - 12;
   }
}
