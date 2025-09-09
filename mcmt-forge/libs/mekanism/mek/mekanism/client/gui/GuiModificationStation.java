package mekanism.client.gui;

import mekanism.api.gear.IModule;
import mekanism.client.gui.element.bar.GuiVerticalPowerBar;
import mekanism.client.gui.element.button.TranslationButton;
import mekanism.client.gui.element.progress.GuiProgress;
import mekanism.client.gui.element.progress.ProgressType;
import mekanism.client.gui.element.scroll.GuiModuleScrollList;
import mekanism.client.gui.element.tab.GuiEnergyTab;
import mekanism.common.Mekanism;
import mekanism.common.MekanismLang;
import mekanism.common.inventory.container.tile.MekanismTileContainer;
import mekanism.common.network.to_server.PacketRemoveModule;
import mekanism.common.tile.TileEntityModificationStation;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Inventory;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class GuiModificationStation extends GuiMekanismTile<TileEntityModificationStation, MekanismTileContainer<TileEntityModificationStation>> {
   private IModule<?> selectedModule;
   private TranslationButton removeButton;

   public GuiModificationStation(MekanismTileContainer<TileEntityModificationStation> container, Inventory inv, Component title) {
      super(container, inv, title);
      this.dynamicSlots = true;
      this.f_97727_ += 64;
      this.f_97731_ = this.f_97727_ - 92;
   }

   @Override
   protected void addGuiElements() {
      super.addGuiElements();
      this.addRenderableWidget(new GuiVerticalPowerBar(this, this.tile.getEnergyContainer(), 154, 40));
      this.addRenderableWidget(new GuiEnergyTab(this, this.tile.getEnergyContainer(), this.tile::usedEnergy));
      this.addRenderableWidget(new GuiProgress(this.tile::getScaledProgress, ProgressType.LARGE_RIGHT, this, 65, 123));
      this.removeButton = this.addRenderableWidget(
         new TranslationButton(
            this,
            34,
            96,
            108,
            17,
            MekanismLang.BUTTON_REMOVE,
            () -> Mekanism.packetHandler().sendToServer(new PacketRemoveModule(this.tile.m_58899_(), this.selectedModule.getData()))
         )
      );
      this.removeButton.f_93623_ = this.selectedModule != null;
      this.addRenderableWidget(new GuiModuleScrollList(this, 34, 20, 108, 74, () -> this.tile.containerSlot.getStack().m_41777_(), this::onModuleSelected));
   }

   private void onModuleSelected(@Nullable IModule<?> module) {
      this.selectedModule = module;
      this.removeButton.f_93623_ = module != null;
   }

   @Override
   protected void drawForegroundText(@NotNull GuiGraphics guiGraphics, int mouseX, int mouseY) {
      this.renderTitleText(guiGraphics);
      this.drawString(guiGraphics, this.f_169604_, this.f_97730_, this.f_97731_, this.titleTextColor());
      super.drawForegroundText(guiGraphics, mouseX, mouseY);
   }
}
