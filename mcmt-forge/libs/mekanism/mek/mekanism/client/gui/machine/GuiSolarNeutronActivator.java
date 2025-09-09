package mekanism.client.gui.machine;

import mekanism.api.recipes.cache.CachedRecipe;
import mekanism.client.gui.GuiConfigurableTile;
import mekanism.client.gui.element.gauge.GaugeType;
import mekanism.client.gui.element.gauge.GuiGasGauge;
import mekanism.client.gui.element.progress.GuiProgress;
import mekanism.client.gui.element.progress.ProgressType;
import mekanism.common.inventory.container.tile.MekanismTileContainer;
import mekanism.common.inventory.warning.WarningTracker;
import mekanism.common.tile.machine.TileEntitySolarNeutronActivator;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Inventory;
import org.jetbrains.annotations.NotNull;

public class GuiSolarNeutronActivator extends GuiConfigurableTile<TileEntitySolarNeutronActivator, MekanismTileContainer<TileEntitySolarNeutronActivator>> {
   public GuiSolarNeutronActivator(MekanismTileContainer<TileEntitySolarNeutronActivator> container, Inventory inv, Component title) {
      super(container, inv, title);
      this.f_97731_ += 2;
      this.f_97729_ = 4;
      this.dynamicSlots = true;
   }

   @Override
   protected void addGuiElements() {
      super.addGuiElements();
      this.addRenderableWidget(new GuiGasGauge(() -> this.tile.inputTank, () -> this.tile.getGasTanks(null), GaugeType.STANDARD, this, 25, 13))
         .warning(WarningTracker.WarningType.NO_MATCHING_RECIPE, this.tile.getWarningCheck(CachedRecipe.OperationTracker.RecipeError.NOT_ENOUGH_INPUT));
      this.addRenderableWidget(new GuiGasGauge(() -> this.tile.outputTank, () -> this.tile.getGasTanks(null), GaugeType.STANDARD, this, 133, 13))
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
      this.drawString(guiGraphics, this.f_169604_, this.f_97730_, this.f_97731_, this.titleTextColor());
      super.drawForegroundText(guiGraphics, mouseX, mouseY);
   }
}
