package mekanism.client.gui.element.button;

import java.util.List;
import java.util.function.BooleanSupplier;
import java.util.function.Function;
import java.util.function.IntConsumer;
import java.util.function.IntSupplier;
import java.util.function.ObjIntConsumer;
import java.util.function.Predicate;
import mekanism.api.text.EnumColor;
import mekanism.api.text.ILangEntry;
import mekanism.client.gui.IGuiWrapper;
import mekanism.client.gui.element.GuiElement;
import mekanism.client.gui.element.slot.GuiSequencedSlotDisplay;
import mekanism.client.gui.element.slot.GuiSlot;
import mekanism.client.gui.element.slot.SlotType;
import mekanism.common.MekanismLang;
import mekanism.common.content.filter.FilterManager;
import mekanism.common.content.filter.IFilter;
import mekanism.common.content.filter.IItemStackFilter;
import mekanism.common.content.filter.IModIDFilter;
import mekanism.common.content.filter.ITagFilter;
import mekanism.common.content.oredictionificator.OredictionificatorFilter;
import mekanism.common.content.transporter.SorterFilter;
import mekanism.common.inventory.warning.WarningTracker;
import mekanism.common.util.MekanismUtils;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class FilterButton extends MekanismButton {
   private static final ResourceLocation TEXTURE = MekanismUtils.getResource(MekanismUtils.ResourceType.GUI_BUTTON, "filter_holder.png");
   protected static final int TEXTURE_WIDTH = 96;
   protected static final int TEXTURE_HEIGHT = 58;
   protected final FilterManager<?> filterManager;
   private final GuiSequencedSlotDisplay slotDisplay;
   private final IntSupplier filterIndex;
   private final RadioButton toggleButton;
   private final GuiSlot slot;
   private final int index;
   private IFilter<?> prevFilter;

   @Nullable
   private static IFilter<?> getFilter(FilterManager<?> filterManager, int index) {
      return index >= 0 && index < filterManager.count() ? (IFilter)filterManager.getFilters().get(index) : null;
   }

   public FilterButton(
      IGuiWrapper gui,
      int x,
      int y,
      int width,
      int height,
      int index,
      IntSupplier filterIndex,
      FilterManager<?> filterManager,
      ObjIntConsumer<IFilter<?>> onPress,
      IntConsumer toggleButtonPress,
      Function<IFilter<?>, List<ItemStack>> renderStackSupplier
   ) {
      super(gui, x, y, width, height, Component.m_237119_(), () -> {
         int actualIndex = filterIndex.getAsInt() + index;
         onPress.accept(getFilter(filterManager, actualIndex), actualIndex);
      }, null);
      this.index = index;
      this.filterIndex = filterIndex;
      this.filterManager = filterManager;
      this.slot = this.addChild(new GuiSlot(SlotType.NORMAL, gui, this.relativeX + 2, this.relativeY + 2));
      this.slotDisplay = this.addChild(
         new GuiSequencedSlotDisplay(gui, this.relativeX + 3, this.relativeY + 3, () -> renderStackSupplier.apply(this.getFilter()))
      );
      BooleanSupplier enabledCheck = () -> {
         IFilter<?> filter = this.getFilter();
         return filter != null && filter.isEnabled();
      };
      this.toggleButton = this.addChild(
         new RadioButton(
            gui,
            this.relativeX + this.f_93618_ - 8 - this.getToggleXShift(),
            this.relativeY + this.f_93619_ - 8 - this.getToggleYShift(),
            enabledCheck,
            () -> toggleButtonPress.accept(this.getActualIndex()),
            (element, guiGraphics, mouseX, mouseY) -> {
               if (enabledCheck.getAsBoolean()) {
                  this.displayTooltips(
                     guiGraphics,
                     mouseX,
                     mouseY,
                     new Component[]{MekanismLang.FILTER_STATE.translate(new Object[]{EnumColor.BRIGHT_GREEN, MekanismLang.MODULE_ENABLED_LOWER})}
                  );
               } else {
                  this.displayTooltips(
                     guiGraphics,
                     mouseX,
                     mouseY,
                     new Component[]{MekanismLang.FILTER_STATE.translate(new Object[]{EnumColor.RED, MekanismLang.MODULE_DISABLED_LOWER})}
                  );
               }
            }
         )
      );
      this.setButtonBackground(GuiElement.ButtonBackground.NONE);
   }

   protected int getToggleXShift() {
      return 2;
   }

   protected int getToggleYShift() {
      return 2;
   }

   protected int getActualIndex() {
      return this.filterIndex.getAsInt() + this.index;
   }

   @Nullable
   protected IFilter<?> getFilter() {
      return getFilter(this.filterManager, this.getActualIndex());
   }

   public FilterButton warning(@NotNull WarningTracker.WarningType type, @NotNull Predicate<IFilter<?>> hasWarning) {
      this.slot.warning(type, () -> hasWarning.test(this.getFilter()));
      return this;
   }

   protected void setVisibility(boolean visible) {
      this.f_93624_ = visible;
      this.slot.f_93624_ = visible;
      this.slotDisplay.f_93624_ = visible;
      this.toggleButton.f_93624_ = visible;
   }

   @Override
   public void m_88315_(@NotNull GuiGraphics guiGraphics, int mouseX, int mouseY, float partialTicks) {
      this.setVisibility(this.getFilter() != null);
      super.m_88315_(guiGraphics, mouseX, mouseY, partialTicks);
   }

   @Override
   public void drawBackground(@NotNull GuiGraphics guiGraphics, int mouseX, int mouseY, float partialTicks) {
      super.drawBackground(guiGraphics, mouseX, mouseY, partialTicks);
      guiGraphics.m_280411_(
         TEXTURE,
         this.getButtonX(),
         this.getButtonY(),
         this.getButtonWidth(),
         this.getButtonHeight(),
         0.0F,
         this.isMouseOverCheckWindows(mouseX, mouseY) ? 0.0F : 29.0F,
         96,
         29,
         96,
         58
      );
   }

   @Override
   public void renderForeground(GuiGraphics guiGraphics, int mouseX, int mouseY) {
      super.renderForeground(guiGraphics, mouseX, mouseY);
      IFilter<?> filter = this.getFilter();
      if (filter != this.prevFilter) {
         this.slotDisplay.updateStackList();
         this.prevFilter = filter;
      }

      if (filter instanceof IItemStackFilter) {
         this.drawFilterType(guiGraphics, this.relativeX, this.relativeY, MekanismLang.ITEM_FILTER);
      } else if (filter instanceof ITagFilter) {
         this.drawFilterType(guiGraphics, this.relativeX, this.relativeY, MekanismLang.TAG_FILTER);
      } else if (filter instanceof IModIDFilter) {
         this.drawFilterType(guiGraphics, this.relativeX, this.relativeY, MekanismLang.MODID_FILTER);
      } else if (filter instanceof OredictionificatorFilter<?, ?, ?> oredictionificatorFilter) {
         this.drawFilterType(guiGraphics, this.relativeX, this.relativeY, MekanismLang.FILTER);
         this.drawTextScaledBound(
            guiGraphics, oredictionificatorFilter.getFilterText(), this.relativeX + 22, this.relativeY + 11, this.titleTextColor(), this.getMaxLength()
         );
      }

      if (filter instanceof SorterFilter<?> sorterFilter) {
         this.drawTextScaledBound(
            guiGraphics,
            (Component)(sorterFilter.color == null ? MekanismLang.NONE.translate(new Object[0]) : sorterFilter.color.getColoredName()),
            this.relativeX + 22,
            this.relativeY + 11,
            this.titleTextColor(),
            this.getMaxLength()
         );
      }
   }

   protected int getMaxLength() {
      return this.f_93618_ - 22 - 2;
   }

   private void drawFilterType(GuiGraphics guiGraphics, int x, int y, ILangEntry langEntry) {
      this.drawTextScaledBound(guiGraphics, langEntry.translate(), x + 22, y + 2, this.titleTextColor(), this.getMaxLength());
   }
}
