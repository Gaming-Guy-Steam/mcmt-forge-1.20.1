package mekanism.client.gui.machine;

import mekanism.api.recipes.cache.CachedRecipe;
import mekanism.client.gui.GuiConfigurableTile;
import mekanism.client.gui.element.bar.GuiVerticalPowerBar;
import mekanism.client.gui.element.button.GuiGasMode;
import mekanism.client.gui.element.gauge.GaugeType;
import mekanism.client.gui.element.gauge.GuiFluidGauge;
import mekanism.client.gui.element.gauge.GuiGasGauge;
import mekanism.client.gui.element.progress.GuiProgress;
import mekanism.client.gui.element.progress.ProgressType;
import mekanism.client.gui.element.tab.GuiEnergyTab;
import mekanism.common.inventory.container.tile.MekanismTileContainer;
import mekanism.common.inventory.warning.WarningTracker;
import mekanism.common.tile.machine.TileEntityElectrolyticSeparator;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Inventory;
import org.jetbrains.annotations.NotNull;

public class GuiElectrolyticSeparator extends GuiConfigurableTile<TileEntityElectrolyticSeparator, MekanismTileContainer<TileEntityElectrolyticSeparator>> {
   public GuiElectrolyticSeparator(MekanismTileContainer<TileEntityElectrolyticSeparator> container, Inventory inv, Component title) {
      super(container, inv, title);
      this.dynamicSlots = true;
   }

   @Override
   protected void addGuiElements() {
      super.addGuiElements();
      this.addRenderableWidget(new GuiEnergyTab(this, this.tile.getEnergyContainer(), this.tile::getEnergyUsed));
      this.addRenderableWidget(new GuiFluidGauge(() -> this.tile.fluidTank, () -> this.tile.getFluidTanks(null), GaugeType.STANDARD, this, 5, 10))
         .warning(WarningTracker.WarningType.NO_MATCHING_RECIPE, this.tile.getWarningCheck(CachedRecipe.OperationTracker.RecipeError.NOT_ENOUGH_INPUT));
      this.addRenderableWidget(new GuiGasGauge(() -> this.tile.leftTank, () -> this.tile.getGasTanks(null), GaugeType.SMALL, this, 58, 18))
         .warning(WarningTracker.WarningType.NO_SPACE_IN_OUTPUT, this.tile.getWarningCheck(TileEntityElectrolyticSeparator.NOT_ENOUGH_SPACE_LEFT_OUTPUT_ERROR));
      this.addRenderableWidget(new GuiGasGauge(() -> this.tile.rightTank, () -> this.tile.getGasTanks(null), GaugeType.SMALL, this, 100, 18))
         .warning(WarningTracker.WarningType.NO_SPACE_IN_OUTPUT, this.tile.getWarningCheck(TileEntityElectrolyticSeparator.NOT_ENOUGH_SPACE_RIGHT_OUTPUT_ERROR));
      this.addRenderableWidget(new GuiVerticalPowerBar(this, this.tile.getEnergyContainer(), 164, 15))
         .warning(WarningTracker.WarningType.NOT_ENOUGH_ENERGY, this.tile.getWarningCheck(CachedRecipe.OperationTracker.RecipeError.NOT_ENOUGH_ENERGY))
         .warning(
            WarningTracker.WarningType.NOT_ENOUGH_ENERGY_REDUCED_RATE,
            this.tile.getWarningCheck(CachedRecipe.OperationTracker.RecipeError.NOT_ENOUGH_ENERGY_REDUCED_RATE)
         );
      this.addRenderableWidget(new GuiProgress(this.tile::getActive, ProgressType.BI, this, 80, 30).jeiCategory(this.tile))
         .warning(
            WarningTracker.WarningType.INPUT_DOESNT_PRODUCE_OUTPUT,
            this.tile.getWarningCheck(CachedRecipe.OperationTracker.RecipeError.INPUT_DOESNT_PRODUCE_OUTPUT)
         );
      this.addRenderableWidget(new GuiGasMode(this, 7, 72, false, () -> this.tile.dumpLeft, this.tile.m_58899_(), 0));
      this.addRenderableWidget(new GuiGasMode(this, 159, 72, true, () -> this.tile.dumpRight, this.tile.m_58899_(), 1));
   }

   @Override
   protected void drawForegroundText(@NotNull GuiGraphics guiGraphics, int mouseX, int mouseY) {
      this.renderTitleText(guiGraphics);
      super.drawForegroundText(guiGraphics, mouseX, mouseY);
   }
}
