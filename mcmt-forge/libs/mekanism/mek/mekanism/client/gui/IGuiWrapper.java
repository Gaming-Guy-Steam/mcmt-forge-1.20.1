package mekanism.client.gui;

import java.util.ArrayList;
import java.util.List;
import java.util.function.BooleanSupplier;
import mekanism.client.gui.element.GuiElement;
import mekanism.client.gui.element.window.GuiWindow;
import mekanism.common.Mekanism;
import mekanism.common.inventory.container.SelectedWindowData;
import mekanism.common.inventory.warning.WarningTracker;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public interface IGuiWrapper {
   default void displayTooltips(GuiGraphics guiGraphics, int mouseX, int mouseY, Component... components) {
      this.displayTooltips(guiGraphics, mouseX, mouseY, List.of(components));
   }

   default void displayTooltips(GuiGraphics guiGraphics, int mouseX, int mouseY, List<Component> components) {
      guiGraphics.m_280666_(this.getFont(), components, mouseX, mouseY);
   }

   @NotNull
   default ItemStack getCarriedItem() {
      if (this instanceof AbstractContainerScreen<?> screen) {
         return screen.m_6262_().m_142621_();
      } else {
         Player player = Minecraft.m_91087_().f_91074_;
         return player == null ? ItemStack.f_41583_ : player.f_36096_.m_142621_();
      }
   }

   default int getLeft() {
      return this instanceof AbstractContainerScreen<?> screen ? screen.getGuiLeft() : 0;
   }

   default int getTop() {
      return this instanceof AbstractContainerScreen<?> screen ? screen.getGuiTop() : 0;
   }

   default int getWidth() {
      return this instanceof AbstractContainerScreen<?> screen ? screen.getXSize() : 0;
   }

   default int getHeight() {
      return this instanceof AbstractContainerScreen<?> screen ? screen.getYSize() : 0;
   }

   default void addWindow(GuiWindow window) {
      Mekanism.logger.error("Tried to call 'addWindow' but unsupported in {}", this.getClass().getName());
   }

   default void removeWindow(GuiWindow window) {
      Mekanism.logger.error("Tried to call 'removeWindow' but unsupported in {}", this.getClass().getName());
   }

   default boolean currentlyQuickCrafting() {
      return false;
   }

   @Nullable
   default GuiWindow getWindowHovering(double mouseX, double mouseY) {
      return null;
   }

   @NotNull
   default BooleanSupplier trackWarning(@NotNull WarningTracker.WarningType type, @NotNull BooleanSupplier warningSupplier) {
      Mekanism.logger.error("Tried to call 'trackWarning' but unsupported in {}", this.getClass().getName());
      return warningSupplier;
   }

   Font getFont();

   default void renderItem(GuiGraphics guiGraphics, @NotNull ItemStack stack, int xAxis, int yAxis) {
      this.renderItem(guiGraphics, stack, xAxis, yAxis, 1.0F);
   }

   default void renderItem(GuiGraphics guiGraphics, @NotNull ItemStack stack, int xAxis, int yAxis, float scale) {
      GuiUtils.renderItem(guiGraphics, stack, xAxis, yAxis, scale, this.getFont(), null, false);
   }

   default void renderItemTooltip(GuiGraphics guiGraphics, @NotNull ItemStack stack, int xAxis, int yAxis) {
      guiGraphics.m_280153_(this.getFont(), stack, xAxis, yAxis);
   }

   default void renderItemTooltipWithExtra(GuiGraphics guiGraphics, @NotNull ItemStack stack, int xAxis, int yAxis, List<Component> toAppend) {
      if (toAppend.isEmpty()) {
         this.renderItemTooltip(guiGraphics, stack, xAxis, yAxis);
      } else {
         List<Component> tooltip = new ArrayList<>(Screen.m_280152_(Minecraft.m_91087_(), stack));
         tooltip.addAll(toAppend);
         guiGraphics.renderTooltip(this.getFont(), tooltip, stack.m_150921_(), stack, xAxis, yAxis);
      }
   }

   default void renderItemWithOverlay(GuiGraphics guiGraphics, @NotNull ItemStack stack, int xAxis, int yAxis, float scale, @Nullable String text) {
      GuiUtils.renderItem(guiGraphics, stack, xAxis, yAxis, scale, this.getFont(), text, true);
   }

   default void setSelectedWindow(SelectedWindowData selectedWindow) {
      Mekanism.logger.error("Tried to call 'setSelectedWindow' but unsupported in {}", this.getClass().getName());
   }

   default void addFocusListener(GuiElement element) {
      Mekanism.logger.error("Tried to call 'addFocusListener' but unsupported in {}", this.getClass().getName());
   }

   default void removeFocusListener(GuiElement element) {
      Mekanism.logger.error("Tried to call 'removeFocusListener' but unsupported in {}", this.getClass().getName());
   }

   default void focusChange(GuiElement changed) {
      Mekanism.logger.error("Tried to call 'focusChange' but unsupported in {}", this.getClass().getName());
   }

   default void incrementFocus(GuiElement current) {
      Mekanism.logger.error("Tried to call 'incrementFocus' but unsupported in {}", this.getClass().getName());
   }
}
