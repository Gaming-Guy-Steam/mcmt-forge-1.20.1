package mekanism.client.gui.qio;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Supplier;
import mekanism.api.text.EnumColor;
import mekanism.client.gui.GuiMekanismTile;
import mekanism.client.gui.element.GuiElementHolder;
import mekanism.client.gui.element.GuiInnerScreen;
import mekanism.client.gui.element.button.MovableFilterButton;
import mekanism.client.gui.element.button.TranslationButton;
import mekanism.client.gui.element.scroll.GuiScrollBar;
import mekanism.client.gui.element.tab.GuiQIOFrequencyTab;
import mekanism.client.gui.element.window.filter.qio.GuiQIOItemStackFilter;
import mekanism.client.gui.element.window.filter.qio.GuiQIOModIDFilter;
import mekanism.client.gui.element.window.filter.qio.GuiQIOTagFilter;
import mekanism.common.Mekanism;
import mekanism.common.MekanismLang;
import mekanism.common.base.TagCache;
import mekanism.common.content.filter.IFilter;
import mekanism.common.content.filter.IItemStackFilter;
import mekanism.common.content.filter.IModIDFilter;
import mekanism.common.content.filter.ITagFilter;
import mekanism.common.content.filter.SortableFilterManager;
import mekanism.common.content.qio.IQIOFrequencyHolder;
import mekanism.common.content.qio.QIOFrequency;
import mekanism.common.content.qio.filter.QIOFilter;
import mekanism.common.content.qio.filter.QIOItemStackFilter;
import mekanism.common.content.qio.filter.QIOModIDFilter;
import mekanism.common.content.qio.filter.QIOTagFilter;
import mekanism.common.inventory.container.tile.MekanismTileContainer;
import mekanism.common.network.to_server.PacketGuiInteract;
import mekanism.common.tile.qio.TileEntityQIOFilterHandler;
import mekanism.common.util.text.TextUtils;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.item.ItemStack;
import org.jetbrains.annotations.NotNull;

public class GuiQIOFilterHandler<TILE extends TileEntityQIOFilterHandler> extends GuiMekanismTile<TILE, MekanismTileContainer<TILE>> {
   private static final int FILTER_COUNT = 3;
   private GuiScrollBar scrollBar;

   static Supplier<List<Component>> getFrequencyText(IQIOFrequencyHolder holder) {
      return () -> {
         QIOFrequency freq = holder.getQIOFrequency();
         return freq == null
            ? List.of(MekanismLang.NO_FREQUENCY.translate(new Object[0]))
            : List.of(MekanismLang.FREQUENCY.translate(new Object[]{freq.getKey()}));
      };
   }

   static Supplier<List<Component>> getFrequencyTooltip(IQIOFrequencyHolder holder) {
      return () -> {
         QIOFrequency freq = holder.getQIOFrequency();
         return freq == null
            ? List.of()
            : List.of(
               MekanismLang.QIO_ITEMS_DETAIL
                  .translateColored(
                     EnumColor.GRAY,
                     new Object[]{EnumColor.INDIGO, TextUtils.format(freq.getTotalItemCount()), TextUtils.format(freq.getTotalItemCountCapacity())}
                  ),
               MekanismLang.QIO_TYPES_DETAIL
                  .translateColored(
                     EnumColor.GRAY,
                     new Object[]{
                        EnumColor.INDIGO, TextUtils.format((long)freq.getTotalItemTypes(true)), TextUtils.format((long)freq.getTotalItemTypeCapacity())
                     }
                  )
            );
      };
   }

   public GuiQIOFilterHandler(MekanismTileContainer<TILE> container, Inventory inv, Component title) {
      super(container, inv, title);
      this.dynamicSlots = true;
      this.f_97727_ += 74;
      this.f_97731_ = this.f_97727_ - 94;
   }

   @Override
   protected void addGuiElements() {
      super.addGuiElements();
      this.addRenderableWidget(new GuiQIOFrequencyTab(this, this.tile));
      this.addRenderableWidget(new GuiInnerScreen(this, 9, 16, this.f_97726_ - 18, 12, getFrequencyText(this.tile)).tooltip(getFrequencyTooltip(this.tile)));
      this.addRenderableWidget(new GuiElementHolder(this, 9, 30, 144, 68));
      this.addRenderableWidget(new GuiElementHolder(this, 9, 98, 144, 22));
      this.addRenderableWidget(
         new TranslationButton(this, 10, 99, 142, 20, MekanismLang.BUTTON_NEW_FILTER, () -> this.addWindow(new GuiQIOFilerSelect(this, this.tile)))
      );
      SortableFilterManager<QIOFilter<?>> filterManager = this.tile.getFilterManager();
      this.scrollBar = this.addRenderableWidget(new GuiScrollBar(this, 153, 30, 90, filterManager::count, () -> 3));

      for (int i = 0; i < 3; i++) {
         this.addRenderableWidget(
            new MovableFilterButton(
               this,
               10,
               31 + i * 22,
               142,
               22,
               i,
               this.scrollBar::getCurrentSelection,
               filterManager,
               index -> {
                  if (index > 0) {
                     PacketGuiInteract.GuiInteraction interaction = m_96638_()
                        ? PacketGuiInteract.GuiInteraction.MOVE_FILTER_TO_TOP
                        : PacketGuiInteract.GuiInteraction.MOVE_FILTER_UP;
                     Mekanism.packetHandler().sendToServer(new PacketGuiInteract(interaction, this.tile, index));
                  }
               },
               index -> {
                  if (index < filterManager.count() - 1) {
                     PacketGuiInteract.GuiInteraction interaction = m_96638_()
                        ? PacketGuiInteract.GuiInteraction.MOVE_FILTER_TO_BOTTOM
                        : PacketGuiInteract.GuiInteraction.MOVE_FILTER_DOWN;
                     Mekanism.packetHandler().sendToServer(new PacketGuiInteract(interaction, this.tile, index));
                  }
               },
               this::onClick,
               index -> Mekanism.packetHandler().sendToServer(new PacketGuiInteract(PacketGuiInteract.GuiInteraction.TOGGLE_FILTER_STATE, this.tile, index)),
               filter -> {
                  List<ItemStack> list = new ArrayList<>();
                  if (filter != null) {
                     if (filter instanceof IItemStackFilter<?> itemFilter) {
                        list.add(itemFilter.getItemStack());
                     } else if (filter instanceof ITagFilter<?> tagFilter) {
                        String name = tagFilter.getTagName();
                        if (name != null && !name.isEmpty()) {
                           list.addAll(TagCache.getItemTagStacks(tagFilter.getTagName()));
                        }
                     } else if (filter instanceof IModIDFilter<?> modIDFilter) {
                        list.addAll(TagCache.getItemModIDStacks(modIDFilter.getModID()));
                     }
                  }

                  return list;
               }
            )
         );
      }
   }

   protected void onClick(IFilter<?> filter, int index) {
      if (filter instanceof IItemStackFilter) {
         this.addWindow(GuiQIOItemStackFilter.edit(this, this.tile, (QIOItemStackFilter)filter));
      } else if (filter instanceof ITagFilter) {
         this.addWindow(GuiQIOTagFilter.edit(this, this.tile, (QIOTagFilter)filter));
      } else if (filter instanceof IModIDFilter) {
         this.addWindow(GuiQIOModIDFilter.edit(this, this.tile, (QIOModIDFilter)filter));
      }
   }

   @Override
   public boolean m_6050_(double mouseX, double mouseY, double delta) {
      return super.m_6050_(mouseX, mouseY, delta) || this.scrollBar.adjustScroll(delta);
   }

   @Override
   protected void drawForegroundText(@NotNull GuiGraphics guiGraphics, int mouseX, int mouseY) {
      this.renderTitleText(guiGraphics);
      this.drawString(guiGraphics, this.f_169604_, this.f_97730_, this.f_97731_, this.titleTextColor());
      super.drawForegroundText(guiGraphics, mouseX, mouseY);
   }
}
