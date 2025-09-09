package mekanism.client.gui.machine;

import java.util.List;
import mekanism.client.gui.GuiMekanismTile;
import mekanism.client.gui.element.GuiInnerScreen;
import mekanism.client.gui.element.progress.GuiFlame;
import mekanism.client.gui.element.progress.IProgressInfoHandler;
import mekanism.client.gui.element.tab.GuiHeatTab;
import mekanism.common.MekanismLang;
import mekanism.common.inventory.container.tile.MekanismTileContainer;
import mekanism.common.tile.machine.TileEntityFuelwoodHeater;
import mekanism.common.util.MekanismUtils;
import mekanism.common.util.UnitDisplayUtils;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Inventory;
import org.jetbrains.annotations.NotNull;

public class GuiFuelwoodHeater extends GuiMekanismTile<TileEntityFuelwoodHeater, MekanismTileContainer<TileEntityFuelwoodHeater>> {
   public GuiFuelwoodHeater(MekanismTileContainer<TileEntityFuelwoodHeater> container, Inventory inv, Component title) {
      super(container, inv, title);
      this.dynamicSlots = true;
   }

   @Override
   protected void addGuiElements() {
      super.addGuiElements();
      this.addRenderableWidget(
         new GuiInnerScreen(
            this,
            48,
            23,
            80,
            28,
            () -> List.of(
               MekanismLang.TEMPERATURE
                  .translate(new Object[]{MekanismUtils.getTemperatureDisplay(this.tile.getTotalTemperature(), UnitDisplayUtils.TemperatureUnit.KELVIN, true)}),
               MekanismLang.FUEL.translate(new Object[]{this.tile.burnTime})
            )
         )
      );
      this.addRenderableWidget(new GuiFlame(new IProgressInfoHandler() {
         @Override
         public double getProgress() {
            return (double)GuiFuelwoodHeater.this.tile.burnTime / GuiFuelwoodHeater.this.tile.maxBurnTime;
         }

         @Override
         public boolean isActive() {
            return GuiFuelwoodHeater.this.tile.burnTime > 0;
         }
      }, this, 144, 31));
      this.addRenderableWidget(
         new GuiHeatTab(
            this,
            () -> {
               Component temp = MekanismUtils.getTemperatureDisplay(this.tile.getTotalTemperature(), UnitDisplayUtils.TemperatureUnit.KELVIN, true);
               Component transfer = MekanismUtils.getTemperatureDisplay(this.tile.getLastTransferLoss(), UnitDisplayUtils.TemperatureUnit.KELVIN, false);
               Component environment = MekanismUtils.getTemperatureDisplay(this.tile.getLastEnvironmentLoss(), UnitDisplayUtils.TemperatureUnit.KELVIN, false);
               return List.of(
                  MekanismLang.TEMPERATURE.translate(new Object[]{temp}),
                  MekanismLang.TRANSFERRED_RATE.translate(new Object[]{transfer}),
                  MekanismLang.DISSIPATED_RATE.translate(new Object[]{environment})
               );
            }
         )
      );
   }

   @Override
   protected void drawForegroundText(@NotNull GuiGraphics guiGraphics, int mouseX, int mouseY) {
      this.renderTitleText(guiGraphics);
      this.drawString(guiGraphics, this.f_169604_, this.f_97730_, this.f_97731_, this.titleTextColor());
      super.drawForegroundText(guiGraphics, mouseX, mouseY);
   }
}
