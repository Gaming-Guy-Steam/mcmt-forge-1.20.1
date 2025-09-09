package mekanism.client.gui.machine;

import mekanism.api.recipes.cache.CachedRecipe;
import mekanism.client.gui.GuiConfigurableTile;
import mekanism.client.gui.element.bar.GuiVerticalPowerBar;
import mekanism.client.gui.element.gauge.GaugeType;
import mekanism.client.gui.element.gauge.GuiFluidGauge;
import mekanism.client.gui.element.gauge.GuiGasGauge;
import mekanism.client.gui.element.progress.GuiProgress;
import mekanism.client.gui.element.progress.ProgressType;
import mekanism.client.gui.element.tab.GuiEnergyTab;
import mekanism.common.inventory.container.tile.MekanismTileContainer;
import mekanism.common.inventory.warning.WarningTracker;
import mekanism.common.tile.machine.TileEntityPressurizedReactionChamber;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Inventory;
import org.jetbrains.annotations.NotNull;

public class GuiPRC extends GuiConfigurableTile<TileEntityPressurizedReactionChamber, MekanismTileContainer<TileEntityPressurizedReactionChamber>> {
   public GuiPRC(MekanismTileContainer<TileEntityPressurizedReactionChamber> container, Inventory inv, Component title) {
      super(container, inv, title);
      this.dynamicSlots = true;
   }

   @Override
   protected void addGuiElements() {
      super.addGuiElements();
      this.addRenderableWidget(new GuiEnergyTab(this, this.tile.getEnergyContainer(), this.tile::getActive));
      this.addRenderableWidget(
         new GuiFluidGauge(() -> this.tile.inputFluidTank, () -> this.tile.getFluidTanks(null), GaugeType.STANDARD, this, 5, 10)
            .warning(
               WarningTracker.WarningType.NO_MATCHING_RECIPE, this.tile.getWarningCheck(TileEntityPressurizedReactionChamber.NOT_ENOUGH_FLUID_INPUT_ERROR)
            )
      );
      this.addRenderableWidget(
         new GuiGasGauge(() -> this.tile.inputGasTank, () -> this.tile.getGasTanks(null), GaugeType.STANDARD, this, 28, 10)
            .warning(WarningTracker.WarningType.NO_MATCHING_RECIPE, this.tile.getWarningCheck(TileEntityPressurizedReactionChamber.NOT_ENOUGH_GAS_INPUT_ERROR))
      );
      this.addRenderableWidget(
         new GuiGasGauge(() -> this.tile.outputGasTank, () -> this.tile.getGasTanks(null), GaugeType.SMALL, this, 140, 40)
            .warning(
               WarningTracker.WarningType.NO_SPACE_IN_OUTPUT, this.tile.getWarningCheck(TileEntityPressurizedReactionChamber.NOT_ENOUGH_SPACE_GAS_OUTPUT_ERROR)
            )
      );
      this.addRenderableWidget(
         new GuiVerticalPowerBar(this, this.tile.getEnergyContainer(), 163, 16)
            .warning(WarningTracker.WarningType.NOT_ENOUGH_ENERGY, this.tile.getWarningCheck(CachedRecipe.OperationTracker.RecipeError.NOT_ENOUGH_ENERGY))
      );
      this.addRenderableWidget(new GuiProgress(this.tile::getScaledProgress, ProgressType.RIGHT, this, 77, 38).jeiCategory(this.tile))
         .warning(
            WarningTracker.WarningType.INPUT_DOESNT_PRODUCE_OUTPUT,
            this.tile.getWarningCheck(CachedRecipe.OperationTracker.RecipeError.INPUT_DOESNT_PRODUCE_OUTPUT)
         );
   }

   @Override
   protected void drawForegroundText(@NotNull GuiGraphics guiGraphics, int mouseX, int mouseY) {
      float widthThird = this.f_97726_ / 3.0F;
      this.drawTextScaledBound(guiGraphics, this.f_96539_, widthThird - 7.0F, this.f_97729_, this.titleTextColor(), 2.0F * widthThird);
      this.drawString(guiGraphics, this.f_169604_, this.f_97730_, this.f_97731_, this.titleTextColor());
      super.drawForegroundText(guiGraphics, mouseX, mouseY);
   }
}
