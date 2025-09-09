package mekanism.client.gui.machine;

import mekanism.api.recipes.cache.CachedRecipe;
import mekanism.client.SpecialColors;
import mekanism.client.gui.GuiConfigurableTile;
import mekanism.client.gui.element.GuiDownArrow;
import mekanism.client.gui.element.GuiSideHolder;
import mekanism.client.gui.element.bar.GuiHorizontalPowerBar;
import mekanism.client.gui.element.gauge.GaugeType;
import mekanism.client.gui.element.gauge.GuiFluidGauge;
import mekanism.client.gui.element.gauge.GuiSlurryGauge;
import mekanism.client.gui.element.progress.GuiProgress;
import mekanism.client.gui.element.progress.ProgressType;
import mekanism.client.gui.element.tab.GuiEnergyTab;
import mekanism.common.inventory.container.tile.MekanismTileContainer;
import mekanism.common.inventory.warning.WarningTracker;
import mekanism.common.tile.machine.TileEntityChemicalWasher;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Inventory;
import org.jetbrains.annotations.NotNull;

public class GuiChemicalWasher extends GuiConfigurableTile<TileEntityChemicalWasher, MekanismTileContainer<TileEntityChemicalWasher>> {
   public GuiChemicalWasher(MekanismTileContainer<TileEntityChemicalWasher> container, Inventory inv, Component title) {
      super(container, inv, title);
      this.dynamicSlots = true;
      this.f_97729_ = 4;
   }

   @Override
   protected void addGuiElements() {
      this.addRenderableWidget(GuiSideHolder.create(this, this.f_97726_, 66, 57, false, true, SpecialColors.TAB_CHEMICAL_WASHER));
      super.addGuiElements();
      this.addRenderableWidget(new GuiDownArrow(this, this.f_97726_ + 8, 91));
      this.addRenderableWidget(new GuiHorizontalPowerBar(this, this.tile.getEnergyContainer(), 115, 75))
         .warning(WarningTracker.WarningType.NOT_ENOUGH_ENERGY, this.tile.getWarningCheck(CachedRecipe.OperationTracker.RecipeError.NOT_ENOUGH_ENERGY))
         .warning(
            WarningTracker.WarningType.NOT_ENOUGH_ENERGY_REDUCED_RATE,
            this.tile.getWarningCheck(CachedRecipe.OperationTracker.RecipeError.NOT_ENOUGH_ENERGY_REDUCED_RATE)
         );
      this.addRenderableWidget(new GuiEnergyTab(this, this.tile.getEnergyContainer(), this.tile::getEnergyUsed));
      this.addRenderableWidget(new GuiFluidGauge(() -> this.tile.fluidTank, () -> this.tile.getFluidTanks(null), GaugeType.STANDARD, this, 7, 13))
         .warning(
            WarningTracker.WarningType.NO_MATCHING_RECIPE, this.tile.getWarningCheck(CachedRecipe.OperationTracker.RecipeError.NOT_ENOUGH_SECONDARY_INPUT)
         );
      this.addRenderableWidget(new GuiSlurryGauge(() -> this.tile.inputTank, () -> this.tile.getSlurryTanks(null), GaugeType.STANDARD, this, 28, 13))
         .warning(WarningTracker.WarningType.NO_MATCHING_RECIPE, this.tile.getWarningCheck(CachedRecipe.OperationTracker.RecipeError.NOT_ENOUGH_INPUT));
      this.addRenderableWidget(new GuiSlurryGauge(() -> this.tile.outputTank, () -> this.tile.getSlurryTanks(null), GaugeType.STANDARD, this, 131, 13))
         .warning(WarningTracker.WarningType.NO_SPACE_IN_OUTPUT, this.tile.getWarningCheck(CachedRecipe.OperationTracker.RecipeError.NOT_ENOUGH_OUTPUT_SPACE));
      this.addRenderableWidget(new GuiProgress(this.tile::getActive, ProgressType.LARGE_RIGHT, this, 64, 39).jeiCategory(this.tile))
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
