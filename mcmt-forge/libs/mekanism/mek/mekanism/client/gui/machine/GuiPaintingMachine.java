package mekanism.client.gui.machine;

import mekanism.api.recipes.cache.CachedRecipe;
import mekanism.client.gui.GuiConfigurableTile;
import mekanism.client.gui.element.bar.GuiVerticalPowerBar;
import mekanism.client.gui.element.gauge.GaugeType;
import mekanism.client.gui.element.gauge.GuiPigmentGauge;
import mekanism.client.gui.element.progress.GuiProgress;
import mekanism.client.gui.element.progress.ProgressType;
import mekanism.client.gui.element.tab.GuiEnergyTab;
import mekanism.common.inventory.container.tile.MekanismTileContainer;
import mekanism.common.inventory.warning.WarningTracker;
import mekanism.common.tile.machine.TileEntityPaintingMachine;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Inventory;
import org.jetbrains.annotations.NotNull;

public class GuiPaintingMachine extends GuiConfigurableTile<TileEntityPaintingMachine, MekanismTileContainer<TileEntityPaintingMachine>> {
   public GuiPaintingMachine(MekanismTileContainer<TileEntityPaintingMachine> container, Inventory inv, Component title) {
      super(container, inv, title);
      this.dynamicSlots = true;
      this.f_97729_ = 4;
      this.f_97731_ += 2;
   }

   @Override
   protected void addGuiElements() {
      super.addGuiElements();
      this.addRenderableWidget(new GuiVerticalPowerBar(this, this.tile.getEnergyContainer(), 164, 15))
         .warning(WarningTracker.WarningType.NOT_ENOUGH_ENERGY, this.tile.getWarningCheck(CachedRecipe.OperationTracker.RecipeError.NOT_ENOUGH_ENERGY));
      this.addRenderableWidget(new GuiEnergyTab(this, this.tile.getEnergyContainer(), this.tile::getActive));
      this.addRenderableWidget(new GuiPigmentGauge(() -> this.tile.pigmentTank, () -> this.tile.getPigmentTanks(null), GaugeType.STANDARD, this, 25, 13))
         .warning(
            WarningTracker.WarningType.NO_MATCHING_RECIPE, this.tile.getWarningCheck(CachedRecipe.OperationTracker.RecipeError.NOT_ENOUGH_SECONDARY_INPUT)
         );
      this.addRenderableWidget(
            new GuiProgress(this.tile::getScaledProgress, ProgressType.LARGE_RIGHT, this, 64, 39)
               .jeiCategory(this.tile)
               .colored(new GuiPaintingMachine.PigmentColorDetails())
         )
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

   private class PigmentColorDetails implements GuiProgress.ColorDetails {
      @Override
      public int getColorFrom() {
         if (GuiPaintingMachine.this.tile == null) {
            return -1;
         } else {
            int tint = GuiPaintingMachine.this.tile.pigmentTank.getType().getColorRepresentation();
            return (tint & 0xFF000000) == 0 ? 0xFF000000 | tint : tint;
         }
      }

      @Override
      public int getColorTo() {
         return -1;
      }
   }
}
