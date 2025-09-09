package mekanism.client.gui.machine;

import mekanism.api.recipes.cache.CachedRecipe;
import mekanism.client.gui.GuiConfigurableTile;
import mekanism.client.gui.element.bar.GuiHorizontalPowerBar;
import mekanism.client.gui.element.gauge.GaugeType;
import mekanism.client.gui.element.gauge.GuiGasGauge;
import mekanism.client.gui.element.progress.GuiProgress;
import mekanism.client.gui.element.progress.ProgressType;
import mekanism.client.gui.element.tab.GuiEnergyTab;
import mekanism.common.inventory.container.tile.MekanismTileContainer;
import mekanism.common.inventory.warning.WarningTracker;
import mekanism.common.tile.machine.TileEntityChemicalInfuser;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Inventory;
import org.jetbrains.annotations.NotNull;

public class GuiChemicalInfuser extends GuiConfigurableTile<TileEntityChemicalInfuser, MekanismTileContainer<TileEntityChemicalInfuser>> {
   public GuiChemicalInfuser(MekanismTileContainer<TileEntityChemicalInfuser> container, Inventory inv, Component title) {
      super(container, inv, title);
      this.f_97731_ += 2;
      this.f_97728_ = 5;
      this.f_97729_ = 5;
      this.dynamicSlots = true;
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
      this.addRenderableWidget(new GuiEnergyTab(this, this.tile.getEnergyContainer(), this.tile::getEnergyUsed));
      this.addRenderableWidget(new GuiGasGauge(() -> this.tile.leftTank, () -> this.tile.getGasTanks(null), GaugeType.STANDARD, this, 25, 13))
         .warning(WarningTracker.WarningType.NO_MATCHING_RECIPE, this.tile.getWarningCheck(CachedRecipe.OperationTracker.RecipeError.NOT_ENOUGH_LEFT_INPUT));
      this.addRenderableWidget(new GuiGasGauge(() -> this.tile.centerTank, () -> this.tile.getGasTanks(null), GaugeType.STANDARD, this, 79, 4))
         .warning(WarningTracker.WarningType.NO_SPACE_IN_OUTPUT, this.tile.getWarningCheck(CachedRecipe.OperationTracker.RecipeError.NOT_ENOUGH_OUTPUT_SPACE));
      this.addRenderableWidget(new GuiGasGauge(() -> this.tile.rightTank, () -> this.tile.getGasTanks(null), GaugeType.STANDARD, this, 133, 13))
         .warning(WarningTracker.WarningType.NO_MATCHING_RECIPE, this.tile.getWarningCheck(CachedRecipe.OperationTracker.RecipeError.NOT_ENOUGH_RIGHT_INPUT));
      this.addRenderableWidget(new GuiProgress(this.tile::getActive, ProgressType.SMALL_RIGHT, this, 47, 39).jeiCategory(this.tile))
         .warning(
            WarningTracker.WarningType.INPUT_DOESNT_PRODUCE_OUTPUT,
            this.tile.getWarningCheck(CachedRecipe.OperationTracker.RecipeError.INPUT_DOESNT_PRODUCE_OUTPUT)
         );
      this.addRenderableWidget(new GuiProgress(this.tile::getActive, ProgressType.SMALL_LEFT, this, 101, 39).jeiCategory(this.tile))
         .warning(
            WarningTracker.WarningType.INPUT_DOESNT_PRODUCE_OUTPUT,
            this.tile.getWarningCheck(CachedRecipe.OperationTracker.RecipeError.INPUT_DOESNT_PRODUCE_OUTPUT)
         );
   }

   @Override
   protected void drawForegroundText(@NotNull GuiGraphics guiGraphics, int mouseX, int mouseY) {
      this.drawString(guiGraphics, this.f_96539_, this.f_97728_, this.f_97729_, this.titleTextColor());
      this.drawString(guiGraphics, this.f_169604_, this.f_97730_, this.f_97731_, this.titleTextColor());
      super.drawForegroundText(guiGraphics, mouseX, mouseY);
   }
}
