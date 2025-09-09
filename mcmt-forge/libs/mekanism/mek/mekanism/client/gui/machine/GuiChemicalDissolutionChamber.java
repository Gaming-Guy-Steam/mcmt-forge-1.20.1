package mekanism.client.gui.machine;

import mekanism.api.recipes.cache.CachedRecipe;
import mekanism.client.gui.GuiConfigurableTile;
import mekanism.client.gui.element.bar.GuiHorizontalPowerBar;
import mekanism.client.gui.element.gauge.GaugeType;
import mekanism.client.gui.element.gauge.GuiGasGauge;
import mekanism.client.gui.element.gauge.GuiMergedChemicalTankGauge;
import mekanism.client.gui.element.progress.GuiProgress;
import mekanism.client.gui.element.progress.ProgressType;
import mekanism.client.gui.element.tab.GuiEnergyTab;
import mekanism.common.inventory.container.tile.MekanismTileContainer;
import mekanism.common.inventory.warning.WarningTracker;
import mekanism.common.tile.machine.TileEntityChemicalDissolutionChamber;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Inventory;
import org.jetbrains.annotations.NotNull;

public class GuiChemicalDissolutionChamber
   extends GuiConfigurableTile<TileEntityChemicalDissolutionChamber, MekanismTileContainer<TileEntityChemicalDissolutionChamber>> {
   public GuiChemicalDissolutionChamber(MekanismTileContainer<TileEntityChemicalDissolutionChamber> container, Inventory inv, Component title) {
      super(container, inv, title);
      this.dynamicSlots = true;
      this.f_97729_ = 4;
   }

   @Override
   protected void addGuiElements() {
      super.addGuiElements();
      this.addRenderableWidget(new GuiHorizontalPowerBar(this, this.tile.getEnergyContainer(), 115, 75))
         .warning(WarningTracker.WarningType.NOT_ENOUGH_ENERGY, this.tile.getWarningCheck(CachedRecipe.OperationTracker.RecipeError.NOT_ENOUGH_ENERGY))
         .warning(
            WarningTracker.WarningType.NOT_ENOUGH_ENERGY_REDUCED_RATE,
            this.tile.getWarningCheck(CachedRecipe.OperationTracker.RecipeError.NOT_ENOUGH_ENERGY_REDUCED_RATE)
         );
      this.addRenderableWidget(new GuiEnergyTab(this, this.tile.getEnergyContainer(), this.tile::getActive));
      this.addRenderableWidget(new GuiGasGauge(() -> this.tile.injectTank, () -> this.tile.getGasTanks(null), GaugeType.STANDARD, this, 7, 4))
         .warning(
            WarningTracker.WarningType.NO_MATCHING_RECIPE, this.tile.getWarningCheck(CachedRecipe.OperationTracker.RecipeError.NOT_ENOUGH_SECONDARY_INPUT)
         );
      this.addRenderableWidget(new GuiMergedChemicalTankGauge<>(() -> this.tile.outputTank, () -> this.tile, GaugeType.STANDARD, this, 131, 13))
         .warning(WarningTracker.WarningType.NO_SPACE_IN_OUTPUT, this.tile.getWarningCheck(CachedRecipe.OperationTracker.RecipeError.NOT_ENOUGH_OUTPUT_SPACE));
      this.addRenderableWidget(new GuiProgress(this.tile::getScaledProgress, ProgressType.LARGE_RIGHT, this, 64, 40).jeiCategory(this.tile))
         .warning(
            WarningTracker.WarningType.INPUT_DOESNT_PRODUCE_OUTPUT,
            this.tile.getWarningCheck(CachedRecipe.OperationTracker.RecipeError.INPUT_DOESNT_PRODUCE_OUTPUT)
         );
   }

   @Override
   protected void drawForegroundText(@NotNull GuiGraphics guiGraphics, int mouseX, int mouseY) {
      this.renderTitleText(guiGraphics);
      super.drawForegroundText(guiGraphics, mouseX, mouseY);
   }
}
