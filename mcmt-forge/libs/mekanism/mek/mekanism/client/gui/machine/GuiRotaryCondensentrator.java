package mekanism.client.gui.machine;

import mekanism.api.recipes.cache.CachedRecipe;
import mekanism.client.gui.GuiConfigurableTile;
import mekanism.client.gui.element.GuiDownArrow;
import mekanism.client.gui.element.bar.GuiHorizontalPowerBar;
import mekanism.client.gui.element.button.ToggleButton;
import mekanism.client.gui.element.gauge.GaugeType;
import mekanism.client.gui.element.gauge.GuiFluidGauge;
import mekanism.client.gui.element.gauge.GuiGasGauge;
import mekanism.client.gui.element.progress.GuiProgress;
import mekanism.client.gui.element.progress.IProgressInfoHandler;
import mekanism.client.gui.element.progress.ProgressType;
import mekanism.client.gui.element.tab.GuiEnergyTab;
import mekanism.client.jei.MekanismJEIRecipeType;
import mekanism.common.Mekanism;
import mekanism.common.MekanismLang;
import mekanism.common.inventory.container.tile.MekanismTileContainer;
import mekanism.common.inventory.warning.WarningTracker;
import mekanism.common.network.to_server.PacketGuiInteract;
import mekanism.common.tile.machine.TileEntityRotaryCondensentrator;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Inventory;
import org.jetbrains.annotations.NotNull;

public class GuiRotaryCondensentrator extends GuiConfigurableTile<TileEntityRotaryCondensentrator, MekanismTileContainer<TileEntityRotaryCondensentrator>> {
   public GuiRotaryCondensentrator(MekanismTileContainer<TileEntityRotaryCondensentrator> container, Inventory inv, Component title) {
      super(container, inv, title);
      this.dynamicSlots = true;
      this.f_97729_ = 4;
   }

   @Override
   protected void addGuiElements() {
      super.addGuiElements();
      this.addRenderableWidget(new GuiDownArrow(this, 159, 44));
      this.addRenderableWidget(new GuiHorizontalPowerBar(this, this.tile.getEnergyContainer(), 115, 75))
         .warning(WarningTracker.WarningType.NOT_ENOUGH_ENERGY, this.tile.getWarningCheck(CachedRecipe.OperationTracker.RecipeError.NOT_ENOUGH_ENERGY))
         .warning(
            WarningTracker.WarningType.NOT_ENOUGH_ENERGY_REDUCED_RATE,
            this.tile.getWarningCheck(CachedRecipe.OperationTracker.RecipeError.NOT_ENOUGH_ENERGY_REDUCED_RATE)
         );
      this.addRenderableWidget(new GuiEnergyTab(this, this.tile.getEnergyContainer(), this.tile::getEnergyUsed));
      this.addRenderableWidget(new GuiFluidGauge(() -> this.tile.fluidTank, () -> this.tile.getFluidTanks(null), GaugeType.STANDARD, this, 133, 13))
         .warning(WarningTracker.WarningType.NO_MATCHING_RECIPE, this.tile.getWarningCheck(TileEntityRotaryCondensentrator.NOT_ENOUGH_FLUID_INPUT_ERROR))
         .warning(WarningTracker.WarningType.NO_SPACE_IN_OUTPUT, this.tile.getWarningCheck(TileEntityRotaryCondensentrator.NOT_ENOUGH_SPACE_FLUID_OUTPUT_ERROR));
      this.addRenderableWidget(new GuiGasGauge(() -> this.tile.gasTank, () -> this.tile.getGasTanks(null), GaugeType.STANDARD, this, 25, 13))
         .warning(WarningTracker.WarningType.NO_MATCHING_RECIPE, this.tile.getWarningCheck(TileEntityRotaryCondensentrator.NOT_ENOUGH_GAS_INPUT_ERROR))
         .warning(WarningTracker.WarningType.NO_SPACE_IN_OUTPUT, this.tile.getWarningCheck(TileEntityRotaryCondensentrator.NOT_ENOUGH_SPACE_GAS_OUTPUT_ERROR));
      this.addRenderableWidget(new GuiProgress(new IProgressInfoHandler.IBooleanProgressInfoHandler() {
            @Override
            public boolean fillProgressBar() {
               return GuiRotaryCondensentrator.this.tile.getActive();
            }

            @Override
            public boolean isActive() {
               return !GuiRotaryCondensentrator.this.tile.mode;
            }
         }, ProgressType.LARGE_RIGHT, this, 64, 39).jeiCategories(MekanismJEIRecipeType.CONDENSENTRATING))
         .warning(
            WarningTracker.WarningType.INPUT_DOESNT_PRODUCE_OUTPUT,
            this.tile.getWarningCheck(CachedRecipe.OperationTracker.RecipeError.INPUT_DOESNT_PRODUCE_OUTPUT)
         );
      this.addRenderableWidget(new GuiProgress(new IProgressInfoHandler.IBooleanProgressInfoHandler() {
            @Override
            public boolean fillProgressBar() {
               return GuiRotaryCondensentrator.this.tile.getActive();
            }

            @Override
            public boolean isActive() {
               return GuiRotaryCondensentrator.this.tile.mode;
            }
         }, ProgressType.LARGE_LEFT, this, 64, 39).jeiCategories(MekanismJEIRecipeType.DECONDENSENTRATING))
         .warning(
            WarningTracker.WarningType.INPUT_DOESNT_PRODUCE_OUTPUT,
            this.tile.getWarningCheck(CachedRecipe.OperationTracker.RecipeError.INPUT_DOESNT_PRODUCE_OUTPUT)
         );
      this.addRenderableWidget(
         new ToggleButton(
            this,
            4,
            4,
            () -> this.tile.mode,
            () -> Mekanism.packetHandler().sendToServer(new PacketGuiInteract(PacketGuiInteract.GuiInteraction.NEXT_MODE, this.tile)),
            this.getOnHover(MekanismLang.CONDENSENTRATOR_TOGGLE)
         )
      );
   }

   @Override
   protected void drawForegroundText(@NotNull GuiGraphics guiGraphics, int mouseX, int mouseY) {
      this.renderTitleText(guiGraphics);
      this.drawString(
         guiGraphics,
         (this.tile.mode ? MekanismLang.DECONDENSENTRATING : MekanismLang.CONDENSENTRATING).translate(new Object[0]),
         6,
         this.f_97727_ - 92,
         this.titleTextColor()
      );
      super.drawForegroundText(guiGraphics, mouseX, mouseY);
   }
}
