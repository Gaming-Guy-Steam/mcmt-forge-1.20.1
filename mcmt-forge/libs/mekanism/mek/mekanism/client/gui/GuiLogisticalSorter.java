package mekanism.client.gui;

import java.util.List;
import mekanism.client.gui.element.button.ColorButton;
import mekanism.client.gui.element.button.MekanismImageButton;
import mekanism.client.gui.element.button.TranslationButton;
import mekanism.client.gui.element.slot.GuiSlot;
import mekanism.client.gui.element.slot.SlotType;
import mekanism.client.gui.element.window.filter.transporter.GuiSorterFilerSelect;
import mekanism.client.gui.element.window.filter.transporter.GuiSorterItemStackFilter;
import mekanism.client.gui.element.window.filter.transporter.GuiSorterModIDFilter;
import mekanism.client.gui.element.window.filter.transporter.GuiSorterTagFilter;
import mekanism.common.Mekanism;
import mekanism.common.MekanismLang;
import mekanism.common.base.TagCache;
import mekanism.common.content.filter.IFilter;
import mekanism.common.content.filter.IItemStackFilter;
import mekanism.common.content.filter.IModIDFilter;
import mekanism.common.content.filter.ITagFilter;
import mekanism.common.content.transporter.SorterFilter;
import mekanism.common.content.transporter.SorterItemStackFilter;
import mekanism.common.content.transporter.SorterModIDFilter;
import mekanism.common.content.transporter.SorterTagFilter;
import mekanism.common.inventory.container.tile.MekanismTileContainer;
import mekanism.common.network.to_server.PacketGuiInteract;
import mekanism.common.tile.TileEntityLogisticalSorter;
import mekanism.common.util.TransporterUtils;
import mekanism.common.util.text.BooleanStateDisplay;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.item.ItemStack;
import org.jetbrains.annotations.NotNull;

public class GuiLogisticalSorter extends GuiFilterHolder<SorterFilter<?>, TileEntityLogisticalSorter, MekanismTileContainer<TileEntityLogisticalSorter>> {
   public GuiLogisticalSorter(MekanismTileContainer<TileEntityLogisticalSorter> container, Inventory inv, Component title) {
      super(container, inv, title);
   }

   @Override
   protected void addGuiElements() {
      super.addGuiElements();
      this.addRenderableWidget(new GuiSlot(SlotType.NORMAL, this, 12, 136).setRenderAboveSlots());
      this.addRenderableWidget(
         new TranslationButton(this, 56, 136, 96, 20, MekanismLang.BUTTON_NEW_FILTER, () -> this.addWindow(new GuiSorterFilerSelect(this, this.tile)))
      );
      this.addRenderableWidget(
         new MekanismImageButton(
            this,
            12,
            58,
            14,
            this.getButtonLocation("single"),
            () -> Mekanism.packetHandler().sendToServer(new PacketGuiInteract(PacketGuiInteract.GuiInteraction.SINGLE_ITEM_BUTTON, this.tile)),
            this.getOnHover(MekanismLang.SORTER_SINGLE_ITEM_DESCRIPTION)
         )
      );
      this.addRenderableWidget(
         new MekanismImageButton(
            this,
            12,
            84,
            14,
            this.getButtonLocation("round_robin"),
            () -> Mekanism.packetHandler().sendToServer(new PacketGuiInteract(PacketGuiInteract.GuiInteraction.ROUND_ROBIN_BUTTON, this.tile)),
            this.getOnHover(MekanismLang.SORTER_ROUND_ROBIN_DESCRIPTION)
         )
      );
      this.addRenderableWidget(
         new MekanismImageButton(
            this,
            12,
            110,
            14,
            this.getButtonLocation("auto_eject"),
            () -> Mekanism.packetHandler().sendToServer(new PacketGuiInteract(PacketGuiInteract.GuiInteraction.AUTO_EJECT_BUTTON, this.tile)),
            this.getOnHover(MekanismLang.SORTER_AUTO_EJECT_DESCRIPTION)
         )
      );
      this.addRenderableWidget(
         new ColorButton(
            this,
            13,
            137,
            16,
            16,
            () -> this.tile.color,
            () -> Mekanism.packetHandler()
               .sendToServer(
                  new PacketGuiInteract(
                     PacketGuiInteract.GuiInteraction.CHANGE_COLOR,
                     this.tile,
                     m_96638_() ? -1 : TransporterUtils.getColorIndex(TransporterUtils.increment(this.tile.color))
                  )
               ),
            () -> Mekanism.packetHandler()
               .sendToServer(
                  new PacketGuiInteract(
                     PacketGuiInteract.GuiInteraction.CHANGE_COLOR, this.tile, TransporterUtils.getColorIndex(TransporterUtils.decrement(this.tile.color))
                  )
               )
         )
      );
   }

   @Override
   protected void drawForegroundText(@NotNull GuiGraphics guiGraphics, int mouseX, int mouseY) {
      super.drawForegroundText(guiGraphics, mouseX, mouseY);
      this.renderTitleText(guiGraphics);
      this.drawTextWithScale(guiGraphics, MekanismLang.FILTERS.translate(new Object[0]), 14.0F, 22.0F, this.screenTextColor(), 0.8F);
      this.drawTextWithScale(
         guiGraphics, MekanismLang.FILTER_COUNT.translate(new Object[]{this.getFilterManager().count()}), 14.0F, 31.0F, this.screenTextColor(), 0.8F
      );
      this.drawTextWithScale(guiGraphics, MekanismLang.SORTER_SINGLE_ITEM.translate(new Object[0]), 14.0F, 48.0F, this.screenTextColor(), 0.8F);
      this.drawTextWithScale(
         guiGraphics, BooleanStateDisplay.OnOff.of(this.tile.getSingleItem()).getTextComponent(), 27.0F, 60.0F, this.screenTextColor(), 0.8F
      );
      this.drawTextWithScale(guiGraphics, MekanismLang.SORTER_ROUND_ROBIN.translate(new Object[0]), 14.0F, 74.0F, this.screenTextColor(), 0.8F);
      this.drawTextWithScale(
         guiGraphics, BooleanStateDisplay.OnOff.of(this.tile.getRoundRobin()).getTextComponent(), 27.0F, 86.0F, this.screenTextColor(), 0.8F
      );
      this.drawTextWithScale(guiGraphics, MekanismLang.SORTER_AUTO_EJECT.translate(new Object[0]), 14.0F, 100.0F, this.screenTextColor(), 0.8F);
      this.drawTextWithScale(
         guiGraphics, BooleanStateDisplay.OnOff.of(this.tile.getAutoEject()).getTextComponent(), 27.0F, 112.0F, this.screenTextColor(), 0.8F
      );
      this.drawTextWithScale(guiGraphics, MekanismLang.SORTER_DEFAULT.translate(new Object[0]), 14.0F, 126.0F, this.screenTextColor(), 0.8F);
   }

   @Override
   protected void onClick(IFilter<?> filter, int index) {
      if (filter instanceof IItemStackFilter) {
         this.addWindow(GuiSorterItemStackFilter.edit(this, this.tile, (SorterItemStackFilter)filter));
      } else if (filter instanceof ITagFilter) {
         this.addWindow(GuiSorterTagFilter.edit(this, this.tile, (SorterTagFilter)filter));
      } else if (filter instanceof IModIDFilter) {
         this.addWindow(GuiSorterModIDFilter.edit(this, this.tile, (SorterModIDFilter)filter));
      }
   }

   @Override
   protected List<ItemStack> getTagStacks(String tagName) {
      return TagCache.getItemTagStacks(tagName);
   }

   @Override
   protected List<ItemStack> getModIDStacks(String tagName) {
      return TagCache.getItemModIDStacks(tagName);
   }
}
