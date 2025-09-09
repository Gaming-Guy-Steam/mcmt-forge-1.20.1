package mekanism.client.gui;

import java.util.List;
import mekanism.client.gui.element.custom.GuiFrequencySelector;
import mekanism.client.gui.element.tab.GuiEnergyTab;
import mekanism.client.gui.element.tab.GuiHeatTab;
import mekanism.common.MekanismLang;
import mekanism.common.content.entangloporter.InventoryFrequency;
import mekanism.common.inventory.container.tile.MekanismTileContainer;
import mekanism.common.lib.frequency.FrequencyType;
import mekanism.common.tile.TileEntityQuantumEntangloporter;
import mekanism.common.util.MekanismUtils;
import mekanism.common.util.UnitDisplayUtils;
import mekanism.common.util.text.EnergyDisplay;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Inventory;
import org.jetbrains.annotations.NotNull;

public class GuiQuantumEntangloporter
   extends GuiConfigurableTile<TileEntityQuantumEntangloporter, MekanismTileContainer<TileEntityQuantumEntangloporter>>
   implements GuiFrequencySelector.ITileGuiFrequencySelector<InventoryFrequency, TileEntityQuantumEntangloporter> {
   public GuiQuantumEntangloporter(MekanismTileContainer<TileEntityQuantumEntangloporter> container, Inventory inv, Component title) {
      super(container, inv, title);
      this.f_97727_ += 74;
      this.f_97729_ = 4;
      this.f_97731_ = this.f_97727_ - 93;
      this.dynamicSlots = true;
   }

   @Override
   protected void addGuiElements() {
      super.addGuiElements();
      this.addRenderableWidget(new GuiFrequencySelector<>(this, 14));
      this.addRenderableWidget(new GuiEnergyTab(this, () -> {
         InventoryFrequency frequency = this.getFrequency();
         EnergyDisplay storing = frequency == null ? EnergyDisplay.ZERO : EnergyDisplay.of(frequency.storedEnergy);
         EnergyDisplay rate = EnergyDisplay.of(this.tile.getInputRate());
         return List.of(MekanismLang.STORING.translate(new Object[]{storing}), MekanismLang.MATRIX_INPUT_RATE.translate(new Object[]{rate}));
      }));
      this.addRenderableWidget(new GuiHeatTab(this, () -> {
         Component transfer = MekanismUtils.getTemperatureDisplay(this.tile.getLastTransferLoss(), UnitDisplayUtils.TemperatureUnit.KELVIN, false);
         Component environment = MekanismUtils.getTemperatureDisplay(this.tile.getLastEnvironmentLoss(), UnitDisplayUtils.TemperatureUnit.KELVIN, false);
         return List.of(MekanismLang.TRANSFERRED_RATE.translate(new Object[]{transfer}), MekanismLang.DISSIPATED_RATE.translate(new Object[]{environment}));
      }));
   }

   @Override
   protected void drawForegroundText(@NotNull GuiGraphics guiGraphics, int mouseX, int mouseY) {
      this.renderTitleText(guiGraphics);
      this.drawString(guiGraphics, this.f_169604_, this.f_97730_, this.f_97731_, this.titleTextColor());
      super.drawForegroundText(guiGraphics, mouseX, mouseY);
   }

   @Override
   public FrequencyType<InventoryFrequency> getFrequencyType() {
      return FrequencyType.INVENTORY;
   }
}
