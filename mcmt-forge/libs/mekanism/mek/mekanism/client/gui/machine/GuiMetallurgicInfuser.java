package mekanism.client.gui.machine;

import mekanism.api.recipes.cache.CachedRecipe;
import mekanism.client.gui.GuiConfigurableTile;
import mekanism.client.gui.element.GuiDumpButton;
import mekanism.client.gui.element.bar.GuiChemicalBar;
import mekanism.client.gui.element.bar.GuiVerticalPowerBar;
import mekanism.client.gui.element.progress.GuiProgress;
import mekanism.client.gui.element.progress.ProgressType;
import mekanism.client.gui.element.tab.GuiEnergyTab;
import mekanism.common.inventory.container.tile.MekanismTileContainer;
import mekanism.common.inventory.warning.WarningTracker;
import mekanism.common.tile.machine.TileEntityMetallurgicInfuser;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Inventory;
import org.jetbrains.annotations.NotNull;

public class GuiMetallurgicInfuser extends GuiConfigurableTile<TileEntityMetallurgicInfuser, MekanismTileContainer<TileEntityMetallurgicInfuser>> {
   public GuiMetallurgicInfuser(MekanismTileContainer<TileEntityMetallurgicInfuser> container, Inventory inv, Component title) {
      super(container, inv, title);
      this.dynamicSlots = true;
   }

   @Override
   protected void addGuiElements() {
      super.addGuiElements();
      this.addRenderableWidget(new GuiVerticalPowerBar(this, this.tile.getEnergyContainer(), 164, 15))
         .warning(WarningTracker.WarningType.NOT_ENOUGH_ENERGY, this.tile.getWarningCheck(CachedRecipe.OperationTracker.RecipeError.NOT_ENOUGH_ENERGY));
      this.addRenderableWidget(new GuiEnergyTab(this, this.tile.getEnergyContainer(), this.tile::getActive));
      this.addRenderableWidget(new GuiProgress(this.tile::getScaledProgress, ProgressType.RIGHT, this, 72, 47).jeiCategory(this.tile))
         .warning(
            WarningTracker.WarningType.INPUT_DOESNT_PRODUCE_OUTPUT,
            this.tile.getWarningCheck(CachedRecipe.OperationTracker.RecipeError.INPUT_DOESNT_PRODUCE_OUTPUT)
         );
      this.addRenderableWidget(
            new GuiChemicalBar<>(this, GuiChemicalBar.getProvider(this.tile.infusionTank, this.tile.getInfusionTanks(null)), 7, 15, 4, 52, false)
         )
         .warning(
            WarningTracker.WarningType.NO_MATCHING_RECIPE, this.tile.getWarningCheck(CachedRecipe.OperationTracker.RecipeError.NOT_ENOUGH_SECONDARY_INPUT)
         );
      this.addRenderableWidget(new GuiDumpButton<>(this, this.tile, 16, 59));
   }

   @Override
   protected void drawForegroundText(@NotNull GuiGraphics guiGraphics, int mouseX, int mouseY) {
      this.renderTitleText(guiGraphics);
      this.drawString(guiGraphics, this.f_169604_, this.f_97730_, this.f_97731_, this.titleTextColor());
      super.drawForegroundText(guiGraphics, mouseX, mouseY);
   }
}
