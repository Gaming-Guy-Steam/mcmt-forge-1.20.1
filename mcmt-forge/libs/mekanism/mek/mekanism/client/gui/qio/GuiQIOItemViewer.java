package mekanism.client.gui.qio;

import com.google.common.collect.Sets;
import java.util.Collection;
import java.util.List;
import java.util.Set;
import mekanism.api.text.EnumColor;
import mekanism.client.gui.GuiMekanism;
import mekanism.client.gui.element.GuiDigitalIconToggle;
import mekanism.client.gui.element.GuiDropdown;
import mekanism.client.gui.element.GuiInnerScreen;
import mekanism.client.gui.element.custom.GuiResizeControls;
import mekanism.client.gui.element.scroll.GuiSlotScroll;
import mekanism.client.gui.element.tab.window.GuiCraftingWindowTab;
import mekanism.client.gui.element.text.BackgroundType;
import mekanism.client.gui.element.text.GuiTextField;
import mekanism.client.gui.element.window.GuiCraftingWindow;
import mekanism.client.gui.element.window.GuiWindow;
import mekanism.common.MekanismLang;
import mekanism.common.config.MekanismConfig;
import mekanism.common.content.qio.SearchQueryParser;
import mekanism.common.inventory.ISlotClickHandler;
import mekanism.common.inventory.container.QIOItemViewerContainer;
import mekanism.common.lib.frequency.Frequency;
import mekanism.common.util.text.TextUtils;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Inventory;
import org.jetbrains.annotations.NotNull;

public abstract class GuiQIOItemViewer<CONTAINER extends QIOItemViewerContainer> extends GuiMekanism<CONTAINER> {
   private static final Set<Character> ALLOWED_SPECIAL_CHARS = Sets.newHashSet(new Character[]{'_', ' ', '-', '/', '.', '"', '\'', '|', '(', ')', ':'});
   protected final Inventory inv;
   private GuiTextField searchField;
   private GuiCraftingWindowTab craftingWindowTab;

   protected GuiQIOItemViewer(CONTAINER container, Inventory inv, Component title) {
      super(container, inv, title);
      this.inv = inv;
      this.f_97726_ = 16 + MekanismConfig.client.qioItemViewerSlotsX.get() * 18 + 18;
      this.f_97727_ = 43 + MekanismConfig.client.qioItemViewerSlotsY.get() * 18 + 96;
      this.f_97731_ = this.f_97727_ - 94;
      this.f_97729_ = 5;
      this.dynamicSlots = true;
   }

   @Override
   protected void addGuiElements() {
      super.addGuiElements();
      int slotsY = MekanismConfig.client.qioItemViewerSlotsY.get();
      this.addRenderableWidget(
         new GuiInnerScreen(
               this,
               7,
               15,
               this.f_97726_ - 16,
               12,
               () -> {
                  Frequency.FrequencyIdentity freq = this.getFrequency();
                  return freq == null
                     ? List.of(MekanismLang.NO_FREQUENCY.translate(new Object[0]))
                     : List.of(MekanismLang.FREQUENCY.translate(new Object[]{freq.key()}));
               }
            )
            .tooltip(
               () -> this.getFrequency() == null
                  ? List.of()
                  : List.of(
                     MekanismLang.QIO_ITEMS_DETAIL
                        .translateColored(
                           EnumColor.GRAY,
                           new Object[]{
                              EnumColor.INDIGO,
                              TextUtils.format(((QIOItemViewerContainer)this.f_97732_).getTotalItems()),
                              TextUtils.format(((QIOItemViewerContainer)this.f_97732_).getCountCapacity())
                           }
                        ),
                     MekanismLang.QIO_TYPES_DETAIL
                        .translateColored(
                           EnumColor.GRAY,
                           new Object[]{
                              EnumColor.INDIGO,
                              TextUtils.format((long)((QIOItemViewerContainer)this.f_97732_).getTotalTypes()),
                              TextUtils.format((long)((QIOItemViewerContainer)this.f_97732_).getTypeCapacity())
                           }
                        )
                  )
            )
      );
      this.searchField = this.addRenderableWidget(new GuiTextField(this, 50, 30, this.f_97726_ - 50 - 10, 10));
      this.searchField
         .setOffset(0, -1)
         .setInputValidator(this::isValidSearchChar)
         .setBackground(BackgroundType.ELEMENT_HOLDER)
         .setResponder(((QIOItemViewerContainer)this.f_97732_)::updateSearch);
      this.searchField.setMaxLength(50);
      this.searchField.setVisible(true);
      this.searchField.setTextColor(16777215);
      this.searchField.m_93692_(true);
      this.addRenderableWidget(
         new GuiSlotScroll(
            this,
            7,
            43,
            MekanismConfig.client.qioItemViewerSlotsX.get(),
            slotsY,
            ((QIOItemViewerContainer)this.f_97732_)::getQIOItemList,
            (ISlotClickHandler)this.f_97732_
         )
      );
      this.addRenderableWidget(
         new GuiDropdown<>(
            this,
            this.f_97726_ - 9 - 54,
            43 + slotsY * 18 + 1,
            41,
            QIOItemViewerContainer.ListSortType.class,
            ((QIOItemViewerContainer)this.f_97732_)::getSortType,
            ((QIOItemViewerContainer)this.f_97732_)::setSortType
         )
      );
      this.addRenderableWidget(
         new GuiDigitalIconToggle<>(
            this,
            this.f_97726_ - 9 - 12,
            43 + slotsY * 18 + 1,
            12,
            12,
            QIOItemViewerContainer.SortDirection.class,
            ((QIOItemViewerContainer)this.f_97732_)::getSortDirection,
            ((QIOItemViewerContainer)this.f_97732_)::setSortDirection
         )
      );
      this.addRenderableWidget(new GuiResizeControls(this, this.getMinecraft().m_91268_().m_85446_() / 2 - 20 - this.f_97736_, this::resize));
      this.craftingWindowTab = this.addRenderableWidget(new GuiCraftingWindowTab(this, () -> this.craftingWindowTab, (QIOItemViewerContainer)this.f_97732_));
   }

   @Override
   protected void drawForegroundText(@NotNull GuiGraphics guiGraphics, int mouseX, int mouseY) {
      this.renderTitleText(guiGraphics);
      this.drawString(guiGraphics, this.f_169604_, this.f_97730_, this.f_97731_, this.titleTextColor());
      this.drawTextScaledBound(guiGraphics, MekanismLang.LIST_SEARCH.translate(new Object[0]), 7.0F, 31.0F, this.titleTextColor(), 41.0F);
      Component text = MekanismLang.LIST_SORT.translate(new Object[0]);
      this.drawString(guiGraphics, text, this.f_97726_ - 66 - this.getStringWidth(text), this.f_97727_ - 92, this.titleTextColor());
      super.drawForegroundText(guiGraphics, mouseX, mouseY);
   }

   @Override
   protected void m_267719_() {
      super.m_267719_();
      int maxY = QIOItemViewerContainer.getSlotsYMax();
      if (MekanismConfig.client.qioItemViewerSlotsY.get() > maxY) {
         MekanismConfig.client.qioItemViewerSlotsY.set(maxY);
         MekanismConfig.client.save();
         this.recreateViewer();
      }
   }

   private boolean isValidSearchChar(char c) {
      return ALLOWED_SPECIAL_CHARS.contains(c) || Character.isDigit(c) || Character.isAlphabetic(c);
   }

   public abstract Frequency.FrequencyIdentity getFrequency();

   private void resize(GuiResizeControls.ResizeType type) {
      int sizeX = MekanismConfig.client.qioItemViewerSlotsX.get();
      int sizeY = MekanismConfig.client.qioItemViewerSlotsY.get();
      boolean changed = false;
      if (type == GuiResizeControls.ResizeType.EXPAND_X && sizeX < 16) {
         MekanismConfig.client.qioItemViewerSlotsX.set(sizeX + 1);
         changed = true;
      } else if (type == GuiResizeControls.ResizeType.EXPAND_Y && sizeY < QIOItemViewerContainer.getSlotsYMax()) {
         MekanismConfig.client.qioItemViewerSlotsY.set(sizeY + 1);
         changed = true;
      } else if (type == GuiResizeControls.ResizeType.SHRINK_X && sizeX > 8) {
         MekanismConfig.client.qioItemViewerSlotsX.set(sizeX - 1);
         changed = true;
      } else if (type == GuiResizeControls.ResizeType.SHRINK_Y && sizeY > 2) {
         MekanismConfig.client.qioItemViewerSlotsY.set(sizeY - 1);
         changed = true;
      }

      if (changed) {
         MekanismConfig.client.save();
         this.recreateViewer();
      }
   }

   private void recreateViewer() {
      CONTAINER c = (CONTAINER)((QIOItemViewerContainer)this.f_97732_).recreate();
      GuiQIOItemViewer<CONTAINER> s = this.recreate(c);
      this.getMinecraft().f_91080_ = null;
      this.getMinecraft().f_91074_.f_36096_ = s.m_6262_();
      this.getMinecraft().m_91152_(s);
      s.searchField.setText(this.searchField.getText());
      c.updateSearch(this.searchField.getText());
      s.transferWindows(this.windows);
   }

   protected void transferWindows(Collection<GuiWindow> windows) {
      for (GuiWindow window : windows) {
         if (window instanceof GuiCraftingWindow craftingWindow) {
            this.craftingWindowTab.adoptWindows(new GuiWindow[]{craftingWindow});
            craftingWindow.updateContainer((QIOItemViewerContainer)this.f_97732_);
         }

         this.addWindow(window);
         window.transferToNewGui(this);
      }
   }

   public abstract GuiQIOItemViewer<CONTAINER> recreate(CONTAINER container);

   static {
      ALLOWED_SPECIAL_CHARS.addAll(SearchQueryParser.QueryType.getPrefixChars());
   }
}
