package mekanism.client.gui.element.window.filter;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;
import java.util.function.Predicate;
import mekanism.api.text.EnumColor;
import mekanism.api.text.ILangEntry;
import mekanism.client.gui.IGuiWrapper;
import mekanism.client.gui.element.GuiElement;
import mekanism.client.gui.element.GuiInnerScreen;
import mekanism.client.gui.element.button.MekanismImageButton;
import mekanism.client.gui.element.button.TranslationButton;
import mekanism.client.gui.element.slot.GuiSequencedSlotDisplay;
import mekanism.client.gui.element.slot.GuiSlot;
import mekanism.client.gui.element.slot.SlotType;
import mekanism.client.gui.element.text.GuiTextField;
import mekanism.client.gui.element.window.GuiWindow;
import mekanism.client.jei.interfaces.IJEIGhostTarget;
import mekanism.common.Mekanism;
import mekanism.common.MekanismLang;
import mekanism.common.content.filter.IFilter;
import mekanism.common.content.transporter.SorterFilter;
import mekanism.common.inventory.container.SelectedWindowData;
import mekanism.common.network.to_server.PacketEditFilter;
import mekanism.common.network.to_server.PacketNewFilter;
import mekanism.common.tile.base.TileEntityMekanism;
import mekanism.common.tile.interfaces.ITileFilterHolder;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.ItemStack;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public abstract class GuiFilter<FILTER extends IFilter<FILTER>, TILE extends TileEntityMekanism & ITileFilterHolder<? super FILTER>>
   extends GuiWindow
   implements GuiFilterHelper<TILE> {
   public static final Predicate<ItemStack> NOT_EMPTY = stack -> !stack.m_41619_();
   public static final Predicate<ItemStack> NOT_EMPTY_BLOCK = stack -> !stack.m_41619_() && stack.m_41720_() instanceof BlockItem;
   private final Component filterName;
   @Nullable
   protected final FILTER origFilter;
   protected final FILTER filter;
   protected final TILE tile;
   private final boolean isNew;
   protected Component status = MekanismLang.STATUS_OK.translateColored(EnumColor.DARK_GREEN, new Object[0]);
   protected GuiSequencedSlotDisplay slotDisplay;
   private int ticker;

   protected GuiFilter(IGuiWrapper gui, int x, int y, int width, int height, Component filterName, TILE tile, @Nullable FILTER origFilter) {
      super(gui, x, y, width, height, SelectedWindowData.UNSPECIFIED);
      this.tile = tile;
      this.origFilter = origFilter;
      this.filterName = filterName;
      if (origFilter == null) {
         this.isNew = true;
         this.filter = this.createNewFilter();
      } else {
         this.isNew = false;
         this.filter = origFilter.clone();
      }

      this.init();
      if (!this.isFocusOverlay()) {
         if (this.isNew && this.hasFilterSelect()) {
            this.addChild(
               new MekanismImageButton(
                  gui,
                  this.relativeX + 6,
                  this.relativeY + 6,
                  11,
                  14,
                  this.getButtonLocation("back"),
                  this::openFilterSelect,
                  this.getOnHover(MekanismLang.BACK)
               )
            );
         } else {
            super.addCloseButton();
         }
      }

      if (this.filter.hasFilter()) {
         this.slotDisplay.updateStackList();
      }
   }

   @Override
   protected int getTitlePadStart() {
      return this.isNew && this.hasFilterSelect() ? super.getTitlePadStart() + 3 : super.getTitlePadStart();
   }

   @Override
   protected void addCloseButton() {
   }

   protected int getSlotOffset() {
      return 18;
   }

   protected int getScreenHeight() {
      return 43;
   }

   protected int getScreenWidth() {
      return 116;
   }

   protected int getLeftButtonX() {
      return this.relativeX + this.f_93618_ / 2 - 61;
   }

   protected void init() {
      int screenTop = this.relativeY + 18;
      int screenBottom = screenTop + this.getScreenHeight();
      this.addChild(
         new GuiInnerScreen(this.gui(), this.relativeX + 29, screenTop, this.getScreenWidth(), this.getScreenHeight(), this::getScreenText).clearFormat()
      );
      this.addChild(
         new TranslationButton(
            this.gui(), this.getLeftButtonX(), screenBottom + 2, 60, 20, this.isNew ? MekanismLang.BUTTON_CANCEL : MekanismLang.BUTTON_DELETE, () -> {
               if (this.origFilter != null) {
                  Mekanism.packetHandler().sendToServer(new PacketEditFilter(this.tile.m_58899_(), this.origFilter, null));
               }

               this.close();
            }
         )
      );
      this.addChild(new TranslationButton(this.gui(), this.getLeftButtonX() + 62, screenBottom + 2, 60, 20, MekanismLang.BUTTON_SAVE, this::validateAndSave));
      GuiSlot slot = this.addChild(
         new GuiSlot(SlotType.NORMAL, this.gui(), this.relativeX + 7, this.relativeY + this.getSlotOffset())
            .setRenderHover(true)
            .setGhostHandler(this.getGhostHandler())
      );
      GuiElement.IClickable slotClickHandler = this.getSlotClickHandler();
      if (slotClickHandler != null) {
         slot.click(slotClickHandler);
      }

      this.slotDisplay = this.addChild(
         new GuiSequencedSlotDisplay(this.gui(), this.relativeX + 8, this.relativeY + this.getSlotOffset() + 1, this::getRenderStacks)
      );
   }

   @Nullable
   protected GuiElement.IClickable getSlotClickHandler() {
      return null;
   }

   @Nullable
   protected IJEIGhostTarget.IGhostIngredientConsumer getGhostHandler() {
      return null;
   }

   private void openFilterSelect() {
      this.gui().addWindow(this.getFilterSelect(this.gui(), this.tile));
      this.close();
   }

   protected List<Component> getScreenText() {
      List<Component> list = new ArrayList<>();
      list.add(MekanismLang.STATUS.translate(new Object[]{this.status}));
      return list;
   }

   protected void validateAndSave() {
      if (this.filter.hasFilter()) {
         this.saveFilter();
      } else {
         this.filterSaveFailed(this.getNoFilterSaveError());
      }
   }

   protected static <FILTER extends SorterFilter<FILTER>> void validateAndSaveSorterFilter(
      GuiFilter<FILTER, ?> guiFilter, GuiTextField minField, GuiTextField maxField
   ) {
      if (guiFilter.filter.hasFilter()) {
         if (!minField.getText().isEmpty() && !maxField.getText().isEmpty()) {
            int min = Integer.parseInt(minField.getText());
            int max = Integer.parseInt(maxField.getText());
            if (max >= min && max <= 64) {
               guiFilter.filter.min = min;
               guiFilter.filter.max = max;
               guiFilter.saveFilter();
            } else if (min > max) {
               guiFilter.filterSaveFailed(MekanismLang.SORTER_FILTER_MAX_LESS_THAN_MIN);
            } else {
               guiFilter.filterSaveFailed(MekanismLang.SORTER_FILTER_OVER_SIZED);
            }
         } else {
            guiFilter.filterSaveFailed(MekanismLang.SORTER_FILTER_SIZE_MISSING);
         }
      } else {
         guiFilter.filterSaveFailed(guiFilter.getNoFilterSaveError());
      }
   }

   protected void filterSaveFailed(ILangEntry reason, Object... args) {
      this.status = reason.translateColored(EnumColor.DARK_RED, args);
      this.ticker = 100;
   }

   protected void filterSaveSuccess() {
      this.status = MekanismLang.STATUS_OK.translateColored(EnumColor.DARK_GREEN, new Object[0]);
      this.ticker = 0;
   }

   protected void saveFilter() {
      if (this.isNew) {
         Mekanism.packetHandler().sendToServer(new PacketNewFilter(this.tile.m_58899_(), this.filter));
      } else {
         Mekanism.packetHandler().sendToServer(new PacketEditFilter(this.tile.m_58899_(), this.origFilter, this.filter));
      }

      this.close();
   }

   protected abstract ILangEntry getNoFilterSaveError();

   @NotNull
   protected abstract List<ItemStack> getRenderStacks();

   @Override
   public void renderForeground(GuiGraphics guiGraphics, int mouseX, int mouseY) {
      super.renderForeground(guiGraphics, mouseX, mouseY);
      this.drawTitleText(guiGraphics, (this.isNew ? MekanismLang.FILTER_NEW : MekanismLang.FILTER_EDIT).translate(new Object[]{this.filterName}), 6.0F);
   }

   @Override
   public void tick() {
      super.tick();
      if (this.ticker > 0) {
         this.ticker--;
      } else {
         this.status = MekanismLang.STATUS_OK.translateColored(EnumColor.DARK_GREEN, new Object[0]);
      }
   }

   protected abstract FILTER createNewFilter();

   public static GuiElement.IClickable getHandleClickSlot(IGuiWrapper gui, Predicate<ItemStack> stackValidator, Consumer<ItemStack> itemConsumer) {
      return (element, mouseX, mouseY) -> {
         if (Screen.m_96638_()) {
            itemConsumer.accept(ItemStack.f_41583_);
         } else {
            ItemStack stack = gui.getCarriedItem();
            if (!stackValidator.test(stack)) {
               return false;
            }

            itemConsumer.accept(stack.m_255036_(1));
         }

         return true;
      };
   }
}
