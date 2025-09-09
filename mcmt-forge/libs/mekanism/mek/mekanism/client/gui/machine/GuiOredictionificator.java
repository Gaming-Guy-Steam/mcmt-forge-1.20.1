package mekanism.client.gui.machine;

import java.util.Collections;
import mekanism.client.gui.GuiConfigurableTile;
import mekanism.client.gui.element.GuiElementHolder;
import mekanism.client.gui.element.button.FilterButton;
import mekanism.client.gui.element.button.TranslationButton;
import mekanism.client.gui.element.progress.GuiProgress;
import mekanism.client.gui.element.progress.ProgressType;
import mekanism.client.gui.element.scroll.GuiScrollBar;
import mekanism.client.gui.element.window.filter.GuiOredictionificatorFilter;
import mekanism.common.Mekanism;
import mekanism.common.MekanismLang;
import mekanism.common.content.filter.FilterManager;
import mekanism.common.content.filter.IFilter;
import mekanism.common.content.oredictionificator.OredictionificatorItemFilter;
import mekanism.common.inventory.container.tile.MekanismTileContainer;
import mekanism.common.inventory.warning.WarningTracker;
import mekanism.common.network.to_server.PacketGuiInteract;
import mekanism.common.tile.machine.TileEntityOredictionificator;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Inventory;
import org.jetbrains.annotations.NotNull;

public class GuiOredictionificator extends GuiConfigurableTile<TileEntityOredictionificator, MekanismTileContainer<TileEntityOredictionificator>> {
   private static final int FILTER_COUNT = 3;
   private GuiScrollBar scrollBar;

   public GuiOredictionificator(MekanismTileContainer<TileEntityOredictionificator> container, Inventory inv, Component title) {
      super(container, inv, title);
      this.f_97727_ += 64;
      this.f_97731_ = this.f_97727_ - 94;
      this.dynamicSlots = true;
   }

   @Override
   protected void addGuiElements() {
      super.addGuiElements();
      this.addRenderableWidget(new GuiElementHolder(this, 9, 17, 144, 68));
      this.addRenderableWidget(new GuiElementHolder(this, 9, 85, 144, 22));
      FilterManager<OredictionificatorItemFilter> filterManager = this.tile.getFilterManager();
      this.scrollBar = this.addRenderableWidget(new GuiScrollBar(this, 153, 17, 90, filterManager::count, () -> 3));
      this.addRenderableWidget(new GuiProgress(() -> this.tile.didProcess, ProgressType.LARGE_RIGHT, this, 64, 119));
      this.addRenderableWidget(
         new TranslationButton(this, 10, 86, 142, 20, MekanismLang.BUTTON_NEW_FILTER, () -> this.addWindow(GuiOredictionificatorFilter.create(this, this.tile)))
      );

      for (int i = 0; i < 3; i++) {
         this.addRenderableWidget(
               new FilterButton(
                  this,
                  10,
                  18 + i * 22,
                  142,
                  22,
                  i,
                  this.scrollBar::getCurrentSelection,
                  filterManager,
                  this::onClick,
                  index -> Mekanism.packetHandler().sendToServer(new PacketGuiInteract(PacketGuiInteract.GuiInteraction.TOGGLE_FILTER_STATE, this.tile, index)),
                  filter -> filter instanceof OredictionificatorItemFilter oredictionificatorFilter
                     ? Collections.singletonList(oredictionificatorFilter.getResult())
                     : Collections.emptyList()
               )
            )
            .warning(WarningTracker.WarningType.INVALID_OREDICTIONIFICATOR_FILTER, filter -> filter != null && filter.isEnabled() && !filter.hasFilter());
      }

      this.trackWarning(WarningTracker.WarningType.INVALID_OREDICTIONIFICATOR_FILTER, () -> filterManager.anyEnabledMatch(filter -> !filter.hasFilter()));
   }

   protected void onClick(IFilter<?> filter, int index) {
      if (filter instanceof OredictionificatorItemFilter oredictionificatorFilter) {
         this.addWindow(GuiOredictionificatorFilter.edit(this, this.tile, oredictionificatorFilter));
      }
   }

   @Override
   protected void drawForegroundText(@NotNull GuiGraphics guiGraphics, int mouseX, int mouseY) {
      this.renderTitleText(guiGraphics);
      this.drawString(guiGraphics, this.f_169604_, this.f_97730_, this.f_97731_, this.titleTextColor());
      super.drawForegroundText(guiGraphics, mouseX, mouseY);
   }

   @Override
   public boolean m_6050_(double mouseX, double mouseY, double delta) {
      return super.m_6050_(mouseX, mouseY, delta) || this.scrollBar.adjustScroll(delta);
   }
}
