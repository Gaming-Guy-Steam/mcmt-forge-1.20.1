package mekanism.client.gui;

import java.util.Collections;
import java.util.List;
import java.util.function.BooleanSupplier;
import mekanism.api.recipes.cache.CachedRecipe;
import mekanism.client.gui.element.GuiDownArrow;
import mekanism.client.gui.element.GuiInnerScreen;
import mekanism.client.gui.element.bar.GuiBar;
import mekanism.client.gui.element.bar.GuiHorizontalRateBar;
import mekanism.client.gui.element.gauge.GaugeType;
import mekanism.client.gui.element.gauge.GuiFluidGauge;
import mekanism.client.gui.element.tab.GuiHeatTab;
import mekanism.client.gui.element.tab.GuiWarningTab;
import mekanism.common.MekanismLang;
import mekanism.common.content.evaporation.EvaporationMultiblockData;
import mekanism.common.inventory.container.tile.MekanismTileContainer;
import mekanism.common.inventory.warning.IWarningTracker;
import mekanism.common.inventory.warning.WarningTracker;
import mekanism.common.tile.multiblock.TileEntityThermalEvaporationController;
import mekanism.common.util.MekanismUtils;
import mekanism.common.util.UnitDisplayUtils;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Inventory;
import org.jetbrains.annotations.NotNull;

public class GuiThermalEvaporationController
   extends GuiMekanismTile<TileEntityThermalEvaporationController, MekanismTileContainer<TileEntityThermalEvaporationController>> {
   public GuiThermalEvaporationController(MekanismTileContainer<TileEntityThermalEvaporationController> container, Inventory inv, Component title) {
      super(container, inv, title);
      this.f_97731_ += 2;
      this.f_97729_ = 4;
      this.dynamicSlots = true;
   }

   @Override
   protected void addGuiElements() {
      super.addGuiElements();
      this.addRenderableWidget(
         new GuiInnerScreen(
               this,
               48,
               19,
               80,
               40,
               () -> {
                  EvaporationMultiblockData multiblock = this.tile.getMultiblock();
                  return List.of(
                     MekanismLang.MULTIBLOCK_FORMED.translate(new Object[0]),
                     MekanismLang.EVAPORATION_HEIGHT.translate(new Object[]{multiblock.height()}),
                     MekanismLang.TEMPERATURE
                        .translate(
                           new Object[]{MekanismUtils.getTemperatureDisplay(multiblock.getTemperature(), UnitDisplayUtils.TemperatureUnit.KELVIN, true)}
                        ),
                     MekanismLang.FLUID_PRODUCTION.translate(new Object[]{Math.round(multiblock.lastGain * 100.0) / 100.0})
                  );
               }
            )
            .spacing(1)
            .jeiCategory(this.tile)
      );
      this.addRenderableWidget(new GuiDownArrow(this, 32, 39));
      this.addRenderableWidget(new GuiDownArrow(this, 136, 39));
      this.addRenderableWidget(
            new GuiHorizontalRateBar(
               this,
               new GuiBar.IBarInfoHandler() {
                  @Override
                  public Component getTooltip() {
                     return MekanismUtils.getTemperatureDisplay(
                        GuiThermalEvaporationController.this.tile.getMultiblock().getTemperature(), UnitDisplayUtils.TemperatureUnit.KELVIN, true
                     );
                  }

                  @Override
                  public double getLevel() {
                     return Math.min(1.0, GuiThermalEvaporationController.this.tile.getMultiblock().getTemperature() / 3000.0);
                  }
               },
               48,
               63
            )
         )
         .warning(
            WarningTracker.WarningType.INPUT_DOESNT_PRODUCE_OUTPUT, this.getWarningCheck(CachedRecipe.OperationTracker.RecipeError.INPUT_DOESNT_PRODUCE_OUTPUT)
         );
      this.addRenderableWidget(
            new GuiFluidGauge(() -> this.tile.getMultiblock().inputTank, () -> this.tile.getMultiblock().getFluidTanks(null), GaugeType.STANDARD, this, 6, 13)
         )
         .warning(WarningTracker.WarningType.NO_MATCHING_RECIPE, this.getWarningCheck(CachedRecipe.OperationTracker.RecipeError.NOT_ENOUGH_INPUT));
      this.addRenderableWidget(
            new GuiFluidGauge(
               () -> this.tile.getMultiblock().outputTank, () -> this.tile.getMultiblock().getFluidTanks(null), GaugeType.STANDARD, this, 152, 13
            )
         )
         .warning(WarningTracker.WarningType.NO_SPACE_IN_OUTPUT, this.getWarningCheck(CachedRecipe.OperationTracker.RecipeError.NOT_ENOUGH_OUTPUT_SPACE));
      this.addRenderableWidget(
         new GuiHeatTab(
            this,
            () -> {
               Component environment = MekanismUtils.getTemperatureDisplay(
                  this.tile.getMultiblock().lastEnvironmentLoss, UnitDisplayUtils.TemperatureUnit.KELVIN, false
               );
               return Collections.singletonList(MekanismLang.DISSIPATED_RATE.translate(new Object[]{environment}));
            }
         )
      );
   }

   private BooleanSupplier getWarningCheck(CachedRecipe.OperationTracker.RecipeError error) {
      return () -> this.tile.getMultiblock().hasWarning(error);
   }

   @Override
   protected void addWarningTab(IWarningTracker warningTracker) {
      this.addRenderableWidget(new GuiWarningTab(this, warningTracker, 137));
   }

   @Override
   protected void drawForegroundText(@NotNull GuiGraphics guiGraphics, int mouseX, int mouseY) {
      this.renderTitleText(guiGraphics);
      this.drawString(guiGraphics, this.f_169604_, this.f_97730_, this.f_97731_, this.titleTextColor());
      super.drawForegroundText(guiGraphics, mouseX, mouseY);
   }
}
