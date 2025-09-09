package mekanism.client.gui;

import java.util.ArrayList;
import java.util.List;
import mekanism.client.gui.element.GuiElementHolder;
import mekanism.client.gui.element.GuiInnerScreen;
import mekanism.client.gui.element.button.FilterButton;
import mekanism.client.gui.element.button.MovableFilterButton;
import mekanism.client.gui.element.scroll.GuiScrollBar;
import mekanism.common.Mekanism;
import mekanism.common.content.filter.FilterManager;
import mekanism.common.content.filter.IFilter;
import mekanism.common.content.filter.IItemStackFilter;
import mekanism.common.content.filter.IModIDFilter;
import mekanism.common.content.filter.ITagFilter;
import mekanism.common.inventory.container.tile.MekanismTileContainer;
import mekanism.common.network.to_server.PacketGuiInteract;
import mekanism.common.tile.base.TileEntityMekanism;
import mekanism.common.tile.interfaces.ITileFilterHolder;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.item.ItemStack;
import org.jetbrains.annotations.NotNull;

public abstract class GuiFilterHolder<FILTER extends IFilter<?>, TILE extends TileEntityMekanism & ITileFilterHolder<FILTER>, CONTAINER extends MekanismTileContainer<TILE>>
   extends GuiMekanismTile<TILE, CONTAINER> {
   private static final int FILTER_COUNT = 4;
   private GuiScrollBar scrollBar;

   protected GuiFilterHolder(CONTAINER container, Inventory inv, Component title) {
      super(container, inv, title);
      this.f_97727_ += 86;
      this.f_97731_ = this.f_97727_ - 92;
      this.dynamicSlots = true;
   }

   @Override
   protected void addGuiElements() {
      super.addGuiElements();
      this.addRenderableWidget(new GuiInnerScreen(this, 9, 17, 46, 140));
      this.addRenderableWidget(new GuiElementHolder(this, 55, 17, 98, 118));
      this.addRenderableWidget(new GuiElementHolder(this, 55, 135, 98, 22));
      FilterManager<FILTER> filterManager = this.getFilterManager();
      this.scrollBar = this.addRenderableWidget(new GuiScrollBar(this, 153, 17, 140, filterManager::count, () -> 4));

      for (int i = 0; i < 4; i++) {
         this.addFilterButton(
            new MovableFilterButton(
               this,
               56,
               18 + i * 29,
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
                        list.addAll(this.getTagStacks(tagFilter.getTagName()));
                     } else if (filter instanceof IModIDFilter<?> modIDFilter) {
                        list.addAll(this.getModIDStacks(modIDFilter.getModID()));
                     }
                  }

                  return list;
               }
            )
         );
      }
   }

   protected FilterButton addFilterButton(FilterButton button) {
      return this.addRenderableWidget(button);
   }

   protected FilterManager<FILTER> getFilterManager() {
      return this.tile.getFilterManager();
   }

   protected abstract void onClick(IFilter<?> filter, int index);

   @Override
   public boolean m_6050_(double mouseX, double mouseY, double delta) {
      return super.m_6050_(mouseX, mouseY, delta) || this.scrollBar.adjustScroll(delta);
   }

   protected abstract List<ItemStack> getTagStacks(String tagName);

   protected abstract List<ItemStack> getModIDStacks(String tagName);

   @Override
   protected void drawForegroundText(@NotNull GuiGraphics guiGraphics, int mouseX, int mouseY) {
      super.drawForegroundText(guiGraphics, mouseX, mouseY);
      this.drawString(guiGraphics, this.f_169604_, this.f_97730_, this.f_97731_, this.titleTextColor());
   }
}
