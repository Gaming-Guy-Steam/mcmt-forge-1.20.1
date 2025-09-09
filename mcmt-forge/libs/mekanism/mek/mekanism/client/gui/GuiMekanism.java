package mekanism.client.gui;

import com.mojang.blaze3d.vertex.PoseStack;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Optional;
import java.util.function.BooleanSupplier;
import java.util.function.Supplier;
import mekanism.api.text.ILangEntry;
import mekanism.client.gui.element.GuiElement;
import mekanism.client.gui.element.slot.GuiSlot;
import mekanism.client.gui.element.slot.GuiVirtualSlot;
import mekanism.client.gui.element.slot.SlotType;
import mekanism.client.gui.element.tab.GuiWarningTab;
import mekanism.client.gui.element.window.GuiWindow;
import mekanism.client.render.IFancyFontRenderer;
import mekanism.client.render.MekanismRenderer;
import mekanism.common.Mekanism;
import mekanism.common.inventory.container.MekanismContainer;
import mekanism.common.inventory.container.SelectedWindowData;
import mekanism.common.inventory.container.slot.ContainerSlotType;
import mekanism.common.inventory.container.slot.IVirtualSlot;
import mekanism.common.inventory.container.slot.InventoryContainerSlot;
import mekanism.common.inventory.container.slot.SlotOverlay;
import mekanism.common.inventory.container.tile.MekanismTileContainer;
import mekanism.common.inventory.warning.IWarningTracker;
import mekanism.common.inventory.warning.WarningTracker;
import mekanism.common.lib.collection.LRU;
import mekanism.common.tile.component.config.DataType;
import mekanism.common.tile.interfaces.ISideConfiguration;
import mekanism.common.util.MekanismUtils;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.events.GuiEventListener;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public abstract class GuiMekanism<CONTAINER extends AbstractContainerMenu>
   extends VirtualSlotContainerScreen<CONTAINER>
   implements IGuiWrapper,
   IFancyFontRenderer {
   public static final ResourceLocation BASE_BACKGROUND = MekanismUtils.getResource(MekanismUtils.ResourceType.GUI, "base.png");
   public static final ResourceLocation SHADOW = MekanismUtils.getResource(MekanismUtils.ResourceType.GUI, "shadow.png");
   public static final ResourceLocation BLUR = MekanismUtils.getResource(MekanismUtils.ResourceType.GUI, "blur.png");
   protected boolean dynamicSlots;
   protected final LRU<GuiWindow> windows = new LRU<>();
   protected final List<GuiElement> focusListeners = new ArrayList<>();
   public boolean switchingToJEI;
   @Nullable
   private IWarningTracker warningTracker;
   private boolean hasClicked = false;
   public static int maxZOffset;

   protected GuiMekanism(CONTAINER container, Inventory inv, Component title) {
      super(container, inv, title);
   }

   @NotNull
   @Override
   public BooleanSupplier trackWarning(@NotNull WarningTracker.WarningType type, @NotNull BooleanSupplier warningSupplier) {
      if (this.warningTracker == null) {
         this.warningTracker = new WarningTracker();
      }

      return this.warningTracker.trackWarning(type, warningSupplier);
   }

   public void m_7861_() {
      if (!this.switchingToJEI) {
         this.windows.forEach(GuiWindow::close);
         super.m_7861_();
      }
   }

   protected void m_7856_() {
      super.m_7856_();
      if (this.warningTracker != null) {
         this.warningTracker.clearTrackedWarnings();
      }

      this.addGuiElements();
      if (this.warningTracker != null) {
         this.addWarningTab(this.warningTracker);
      }
   }

   protected void addWarningTab(IWarningTracker warningTracker) {
      this.addRenderableWidget(new GuiWarningTab(this, warningTracker, 109));
   }

   protected void addGuiElements() {
      if (this.dynamicSlots) {
         this.addSlots();
      }
   }

   protected <T extends GuiElement> T addElement(T element) {
      this.f_169369_.add(element);
      this.m_6702_().add(element);
      return element;
   }

   protected <T extends GuiElement> T addRenderableWidget(T element) {
      return this.addElement(element);
   }

   public void m_181908_() {
      super.m_181908_();
      this.m_6702_().stream().filter(child -> child instanceof GuiElement).map(child -> (GuiElement)child).forEach(GuiElement::tick);
      this.windows.forEach(GuiElement::tick);
   }

   protected void renderTitleText(GuiGraphics guiGraphics) {
      this.drawTitleText(guiGraphics, this.f_96539_, this.f_97729_);
   }

   protected GuiElement.IHoverable getOnHover(ILangEntry translationHelper) {
      return this.getOnHover(() -> translationHelper.translate());
   }

   protected GuiElement.IHoverable getOnHover(Supplier<Component> componentSupplier) {
      return (onHover, guiGraphics, mouseX, mouseY) -> this.displayTooltips(guiGraphics, mouseX, mouseY, new Component[]{componentSupplier.get()});
   }

   protected ResourceLocation getButtonLocation(String name) {
      return MekanismUtils.getResource(MekanismUtils.ResourceType.GUI_BUTTON, name + ".png");
   }

   @Override
   public void addFocusListener(GuiElement element) {
      this.focusListeners.add(element);
   }

   @Override
   public void removeFocusListener(GuiElement element) {
      this.focusListeners.remove(element);
   }

   @Override
   public void focusChange(GuiElement changed) {
      this.focusListeners.stream().filter(e -> e != changed).forEach(e -> e.m_93692_(false));
   }

   @Override
   public void incrementFocus(GuiElement current) {
      int index = this.focusListeners.indexOf(current);
      if (index != -1) {
         GuiElement next = this.focusListeners.get((index + 1) % this.focusListeners.size());
         next.m_93692_(true);
         this.focusChange(next);
      }
   }

   protected boolean m_7467_(double mouseX, double mouseY, int guiLeftIn, int guiTopIn, int mouseButton) {
      return this.getWindowHovering(mouseX, mouseY) == null && super.m_7467_(mouseX, mouseY, guiLeftIn, guiTopIn, mouseButton);
   }

   protected void m_267719_() {
      this.switchingToJEI = false;
      super.m_267719_();
   }

   protected void m_232761_() {
      record PreviousElement(int index, GuiElement element) {
      }

      List<PreviousElement> prevElements = new ArrayList<>();

      for (int i = 0; i < this.m_6702_().size(); i++) {
         GuiEventListener widget = (GuiEventListener)this.m_6702_().get(i);
         if (widget instanceof GuiElement element && element.hasPersistentData()) {
            prevElements.add(new PreviousElement(i, element));
         }
      }

      this.focusListeners.removeIf(elementx -> !elementx.isOverlay);
      int prevLeft = this.f_97735_;
      int prevTop = this.f_97736_;
      super.m_232761_();
      this.windows.forEach(window -> window.resize(prevLeft, prevTop, this.f_97735_, this.f_97736_));
      prevElements.forEach(e -> {
         if (e.index() < this.m_6702_().size()) {
            GuiEventListener widget = (GuiEventListener)this.m_6702_().get(e.index());
            if (widget.getClass() == e.element().getClass()) {
               ((GuiElement)widget).syncFrom(e.element());
            }
         }
      });
   }

   protected void m_280003_(@NotNull GuiGraphics guiGraphics, int mouseX, int mouseY) {
      PoseStack pose = guiGraphics.m_280168_();
      pose.m_252880_(0.0F, 0.0F, 300.0F);
      this.m_6702_()
         .stream()
         .filter(c -> c instanceof GuiElement)
         .forEach(c -> ((GuiElement)c).onDrawBackground(guiGraphics, mouseX, mouseY, MekanismRenderer.getPartialTick()));
      this.drawForegroundText(guiGraphics, mouseX, mouseY);
      int zOffset = 200;
      maxZOffset = zOffset;

      for (GuiEventListener widget : this.m_6702_()) {
         if (widget instanceof GuiElement element) {
            pose.m_85836_();
            element.onRenderForeground(guiGraphics, mouseX, mouseY, zOffset, zOffset);
            pose.m_85849_();
         }
      }

      for (LRU<GuiWindow>.LRUIterator iter = this.getWindowsDescendingIterator(); iter.hasNext(); pose.m_85849_()) {
         GuiWindow overlay = iter.next();
         zOffset = maxZOffset + 150;
         pose.m_85836_();
         overlay.onRenderForeground(guiGraphics, mouseX, mouseY, zOffset, zOffset);
         if (iter.hasNext()) {
            overlay.renderBlur(guiGraphics);
         }
      }

      GuiElement tooltipElement = this.getWindowHovering(mouseX, mouseY);
      if (tooltipElement == null) {
         for (int i = this.m_6702_().size() - 1; i >= 0; i--) {
            GuiEventListener widgetx = (GuiEventListener)this.m_6702_().get(i);
            if (widgetx instanceof GuiElement element && element.m_5953_(mouseX, mouseY)) {
               tooltipElement = element;
               break;
            }
         }
      }

      pose.m_252880_(0.0F, 0.0F, maxZOffset);
      pose.m_252880_(-this.f_97735_, -this.f_97736_, 0.0F);
      if (tooltipElement != null) {
         tooltipElement.renderToolTip(guiGraphics, mouseX, mouseY);
      }

      this.m_280072_(guiGraphics, mouseX, mouseY);
      pose.m_252880_(this.f_97735_, this.f_97736_, 0.0F);
      pose.m_252880_(0.0F, 0.0F, 200.0F);
   }

   protected void drawForegroundText(@NotNull GuiGraphics guiGraphics, int mouseX, int mouseY) {
   }

   @NotNull
   public Optional<GuiEventListener> m_94729_(double mouseX, double mouseY) {
      GuiWindow window = this.getWindowHovering(mouseX, mouseY);
      return window == null ? super.m_94729_(mouseX, mouseY) : Optional.of(window);
   }

   public boolean m_6050_(double mouseX, double mouseY, double delta) {
      GuiWindow top = this.windows.isEmpty() ? null : (GuiWindow)this.windows.iterator().next();
      if (top != null) {
         boolean windowScroll = top.m_6050_(mouseX, mouseY, delta);
         if (windowScroll || !top.getInteractionStrategy().allowAll()) {
            return windowScroll;
         }
      }

      return super.m_6050_(mouseX, mouseY, delta);
   }

   public boolean m_6375_(double mouseX, double mouseY, int button) {
      this.hasClicked = true;
      GuiWindow top = this.windows.isEmpty() ? null : (GuiWindow)this.windows.iterator().next();

      for (GuiWindow overlay : this.windows) {
         GuiElement focusedElement = overlay.mouseClickedNested(mouseX, mouseY, button);
         if (focusedElement != null) {
            if (this.windows.contains(overlay)) {
               this.m_7522_(focusedElement);
               if (button == 0) {
                  this.m_7897_(true);
               }

               if (top != overlay) {
                  top.onFocusLost();
                  this.windows.moveUp(overlay);
                  overlay.onFocused();
               }
            }

            return true;
         }
      }

      for (int i = this.m_6702_().size() - 1; i >= 0; i--) {
         GuiEventListener listener = (GuiEventListener)this.m_6702_().get(i);
         GuiEventListener focusedChild = null;
         if (listener instanceof GuiElement element) {
            focusedChild = element.mouseClickedNested(mouseX, mouseY, button);
         } else if (listener.m_6375_(mouseX, mouseY, button)) {
            focusedChild = listener;
         }

         if (focusedChild != null) {
            this.m_7522_(focusedChild);
            if (button == 0) {
               this.m_7897_(true);
            }

            return true;
         }
      }

      return super.m_6375_(mouseX, mouseY, button);
   }

   public boolean m_6348_(double mouseX, double mouseY, int button) {
      if (this.hasClicked) {
         this.windows.forEach(w -> w.m_7691_(mouseX, mouseY));
         return super.m_6348_(mouseX, mouseY, button);
      } else {
         return false;
      }
   }

   public boolean m_7933_(int keyCode, int scanCode, int modifiers) {
      return this.windows.stream().anyMatch(window -> window.m_7933_(keyCode, scanCode, modifiers))
         || GuiUtils.checkChildren(this.m_6702_(), child -> child.m_7933_(keyCode, scanCode, modifiers))
         || super.m_7933_(keyCode, scanCode, modifiers);
   }

   public boolean m_5534_(char c, int keyCode) {
      return this.windows.stream().anyMatch(window -> window.m_5534_(c, keyCode))
         || GuiUtils.checkChildren(this.m_6702_(), child -> child.m_5534_(c, keyCode))
         || super.m_5534_(c, keyCode);
   }

   public boolean m_7979_(double mouseX, double mouseY, int button, double mouseXOld, double mouseYOld) {
      super.m_7979_(mouseX, mouseY, button, mouseXOld, mouseYOld);
      return this.m_7222_() != null && this.m_7282_() && button == 0 && this.m_7222_().m_7979_(mouseX, mouseY, button, mouseXOld, mouseYOld);
   }

   @Deprecated
   @Nullable
   @Override
   protected Slot m_97744_(double mouseX, double mouseY) {
      boolean checkedWindow = false;
      boolean overNoButtons = false;
      GuiWindow window = null;

      for (Slot slot : this.f_97732_.f_38839_) {
         if (slot.m_6659_()) {
            boolean virtual = slot instanceof IVirtualSlot;
            int xPos = slot.f_40220_;
            int yPos = slot.f_40221_;
            if (virtual) {
               IVirtualSlot virtualSlot = (IVirtualSlot)slot;
               if (!this.isVirtualSlotAvailable(virtualSlot)) {
                  continue;
               }

               xPos = virtualSlot.getActualX();
               yPos = virtualSlot.getActualY();
            }

            if (super.m_6774_(xPos, yPos, 16, 16, mouseX, mouseY)) {
               if (!checkedWindow) {
                  checkedWindow = true;
                  window = this.getWindowHovering(mouseX, mouseY);
                  overNoButtons = this.overNoButtons(window, mouseX, mouseY);
               }

               if (overNoButtons && slot.m_6659_()) {
                  if (window == null) {
                     return slot;
                  }

                  if (virtual && window.childrenContainsElement(element -> element instanceof GuiVirtualSlot v && v.isElementForSlot((IVirtualSlot)slot))) {
                     return slot;
                  }
               }
            }
         }
      }

      return null;
   }

   @Override
   protected boolean isMouseOverSlot(@NotNull Slot slot, double mouseX, double mouseY) {
      if (!(slot instanceof IVirtualSlot virtualSlot)) {
         return this.m_6774_(slot.f_40220_, slot.f_40221_, 16, 16, mouseX, mouseY);
      } else {
         if (this.isVirtualSlotAvailable(virtualSlot)) {
            int xPos = virtualSlot.getActualX();
            int yPos = virtualSlot.getActualY();
            if (super.m_6774_(xPos, yPos, 16, 16, mouseX, mouseY)) {
               GuiWindow window = this.getWindowHovering(mouseX, mouseY);
               if (window == null || window.childrenContainsElement(element -> element instanceof GuiVirtualSlot v && v.isElementForSlot(virtualSlot))) {
                  return this.overNoButtons(window, mouseX, mouseY);
               }
            }
         }

         return false;
      }
   }

   private boolean overNoButtons(@Nullable GuiWindow window, double mouseX, double mouseY) {
      return window == null
         ? this.m_6702_().stream().noneMatch(button -> button.m_5953_(mouseX, mouseY))
         : !window.childrenContainsElement(e -> e.m_5953_(mouseX, mouseY));
   }

   private boolean isVirtualSlotAvailable(IVirtualSlot virtualSlot) {
      return !(virtualSlot.getLinkedWindow() instanceof GuiWindow linkedWindow && !this.windows.contains(linkedWindow));
   }

   protected boolean m_6774_(int x, int y, int width, int height, double mouseX, double mouseY) {
      return super.m_6774_(x, y, width, height, mouseX, mouseY) && this.getWindowHovering(mouseX, mouseY) == null && this.overNoButtons(null, mouseX, mouseY);
   }

   protected void addSlots() {
      int size = this.f_97732_.f_38839_.size();

      for (int i = 0; i < size; i++) {
         Slot slot = (Slot)this.f_97732_.f_38839_.get(i);
         if (slot instanceof InventoryContainerSlot containerSlot) {
            ContainerSlotType slotType = containerSlot.getSlotType();
            DataType dataType = this.findDataType(containerSlot);
            SlotType type;
            if (dataType != null) {
               type = SlotType.get(dataType);
            } else if (slotType == ContainerSlotType.INPUT || slotType == ContainerSlotType.OUTPUT || slotType == ContainerSlotType.EXTRA) {
               type = SlotType.NORMAL;
            } else if (slotType == ContainerSlotType.POWER) {
               type = SlotType.POWER;
            } else {
               if (slotType != ContainerSlotType.NORMAL && slotType != ContainerSlotType.VALIDITY) {
                  continue;
               }

               type = SlotType.NORMAL;
            }

            GuiSlot guiSlot = new GuiSlot(type, this, slot.f_40220_ - 1, slot.f_40221_ - 1);
            containerSlot.addWarnings(guiSlot);
            SlotOverlay slotOverlay = containerSlot.getSlotOverlay();
            if (slotOverlay != null) {
               guiSlot.with(slotOverlay);
            }

            if (slotType == ContainerSlotType.VALIDITY) {
               int index = i;
               guiSlot.validity(() -> this.checkValidity(index));
            }

            this.addRenderableWidget(guiSlot);
         } else {
            this.addRenderableWidget(new GuiSlot(SlotType.NORMAL, this, slot.f_40220_ - 1, slot.f_40221_ - 1));
         }
      }
   }

   @Nullable
   protected DataType findDataType(InventoryContainerSlot slot) {
      return this.f_97732_ instanceof MekanismTileContainer<?> container && container.getTileEntity() instanceof ISideConfiguration sideConfig
         ? sideConfig.getActiveDataType(slot.getInventorySlot())
         : null;
   }

   protected ItemStack checkValidity(int slotIndex) {
      return ItemStack.f_41583_;
   }

   protected void m_7286_(@NotNull GuiGraphics guiGraphics, float partialTick, int mouseX, int mouseY) {
      MekanismRenderer.resetColor(guiGraphics);
      if (this.f_96543_ >= 8 && this.f_96544_ >= 8) {
         GuiUtils.renderBackgroundTexture(guiGraphics, BASE_BACKGROUND, 4, 4, this.f_97735_, this.f_97736_, this.f_97726_, this.f_97727_, 256, 256);
      } else {
         Mekanism.logger
            .warn(
               "Gui: {}, was too small to draw the background of. Unable to draw a background for a gui smaller than 8 by 8.", this.getClass().getSimpleName()
            );
      }
   }

   @Override
   public Font getFont() {
      return this.f_96547_ == null ? this.f_96541_.f_91062_ : this.f_96547_;
   }

   public void m_88315_(@NotNull GuiGraphics guiGraphics, int mouseX, int mouseY, float partialTicks) {
      PoseStack pose = guiGraphics.m_280168_();
      pose.m_85836_();
      pose.m_252880_(0.0F, 0.0F, -500.0F);
      this.m_280273_(guiGraphics);
      super.m_88315_(guiGraphics, mouseX, mouseY, partialTicks);
      pose.m_85849_();
   }

   @Override
   public boolean currentlyQuickCrafting() {
      return this.f_97738_ && !this.f_97737_.isEmpty();
   }

   @Override
   public void addWindow(GuiWindow window) {
      GuiWindow top = this.windows.isEmpty() ? null : (GuiWindow)this.windows.iterator().next();
      if (top != null) {
         top.onFocusLost();
      }

      this.windows.add(window);
      window.onFocused();
   }

   @Override
   public void removeWindow(GuiWindow window) {
      if (!this.windows.isEmpty()) {
         GuiWindow top = (GuiWindow)this.windows.iterator().next();
         this.windows.remove(window);
         if (window == top) {
            window.onFocusLost();
            GuiWindow newTop = this.windows.isEmpty() ? null : (GuiWindow)this.windows.iterator().next();
            if (newTop == null) {
               this.lastWindowRemoved();
            } else {
               newTop.onFocused();
            }

            this.m_7522_(newTop);
         }
      }
   }

   protected void lastWindowRemoved() {
      if (this.f_97732_ instanceof MekanismContainer container) {
         container.setSelectedWindow(null);
      }
   }

   @Override
   public void setSelectedWindow(SelectedWindowData selectedWindow) {
      if (this.f_97732_ instanceof MekanismContainer container) {
         container.setSelectedWindow(selectedWindow);
      }
   }

   @Nullable
   @Override
   public GuiWindow getWindowHovering(double mouseX, double mouseY) {
      return this.windows.stream().filter(w -> w.m_5953_(mouseX, mouseY)).findFirst().orElse(null);
   }

   public Collection<GuiWindow> getWindows() {
      return this.windows;
   }

   public LRU<GuiWindow>.LRUIterator getWindowsDescendingIterator() {
      return this.windows.descendingIterator();
   }
}
