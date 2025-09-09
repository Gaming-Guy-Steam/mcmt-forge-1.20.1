package mekanism.client.gui;

import java.util.List;
import mekanism.api.math.FloatingLong;
import mekanism.client.gui.element.bar.GuiBar;
import mekanism.client.gui.element.bar.GuiVerticalRateBar;
import mekanism.client.gui.element.gauge.GaugeType;
import mekanism.client.gui.element.gauge.GuiEnergyGauge;
import mekanism.client.gui.element.tab.GuiEnergyTab;
import mekanism.client.gui.element.tab.GuiMatrixTab;
import mekanism.common.MekanismLang;
import mekanism.common.content.matrix.MatrixMultiblockData;
import mekanism.common.inventory.container.tile.EmptyTileContainer;
import mekanism.common.tile.multiblock.TileEntityInductionCasing;
import mekanism.common.util.text.EnergyDisplay;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Inventory;
import org.jetbrains.annotations.NotNull;

public class GuiMatrixStats extends GuiMekanismTile<TileEntityInductionCasing, EmptyTileContainer<TileEntityInductionCasing>> {
   public GuiMatrixStats(EmptyTileContainer<TileEntityInductionCasing> container, Inventory inv, Component title) {
      super(container, inv, title);
   }

   @Override
   protected void addGuiElements() {
      super.addGuiElements();
      this.addRenderableWidget(new GuiMatrixTab(this, this.tile, GuiMatrixTab.MatrixTab.MAIN));
      this.addRenderableWidget(new GuiEnergyGauge(new GuiEnergyGauge.IEnergyInfoHandler() {
         @Override
         public FloatingLong getEnergy() {
            return GuiMatrixStats.this.tile.getMultiblock().getEnergy();
         }

         @Override
         public FloatingLong getMaxEnergy() {
            return GuiMatrixStats.this.tile.getMultiblock().getStorageCap();
         }
      }, GaugeType.STANDARD, this, 6, 13));
      this.addRenderableWidget(new GuiVerticalRateBar(this, new GuiBar.IBarInfoHandler() {
         @Override
         public Component getTooltip() {
            return MekanismLang.MATRIX_RECEIVING_RATE.translate(new Object[]{EnergyDisplay.of(GuiMatrixStats.this.tile.getMultiblock().getLastInput())});
         }

         @Override
         public double getLevel() {
            MatrixMultiblockData multiblock = GuiMatrixStats.this.tile.getMultiblock();
            return multiblock.isFormed() ? multiblock.getLastInput().divideToLevel(multiblock.getTransferCap()) : 0.0;
         }
      }, 30, 13));
      this.addRenderableWidget(new GuiVerticalRateBar(this, new GuiBar.IBarInfoHandler() {
         @Override
         public Component getTooltip() {
            return MekanismLang.MATRIX_OUTPUTTING_RATE.translate(new Object[]{EnergyDisplay.of(GuiMatrixStats.this.tile.getMultiblock().getLastOutput())});
         }

         @Override
         public double getLevel() {
            MatrixMultiblockData multiblock = GuiMatrixStats.this.tile.getMultiblock();
            return !multiblock.isFormed() ? 0.0 : multiblock.getLastOutput().divideToLevel(multiblock.getTransferCap());
         }
      }, 38, 13));
      this.addRenderableWidget(
         new GuiEnergyTab(
            this,
            () -> {
               MatrixMultiblockData multiblock = this.tile.getMultiblock();
               return List.of(
                  MekanismLang.STORING.translate(new Object[]{EnergyDisplay.of(multiblock.getEnergy(), multiblock.getStorageCap())}),
                  MekanismLang.MATRIX_INPUT_RATE.translate(new Object[]{EnergyDisplay.of(multiblock.getLastInput())}),
                  MekanismLang.MATRIX_OUTPUT_RATE.translate(new Object[]{EnergyDisplay.of(multiblock.getLastOutput())})
               );
            }
         )
      );
   }

   @Override
   protected void drawForegroundText(@NotNull GuiGraphics guiGraphics, int mouseX, int mouseY) {
      this.renderTitleText(guiGraphics);
      MatrixMultiblockData multiblock = this.tile.getMultiblock();
      this.drawString(guiGraphics, MekanismLang.INPUT.translate(new Object[0]), 53, 26, this.subheadingTextColor());
      this.drawString(guiGraphics, EnergyDisplay.of(multiblock.getLastInput(), multiblock.getTransferCap()).getTextComponent(), 59, 35, this.titleTextColor());
      this.drawString(guiGraphics, MekanismLang.OUTPUT.translate(new Object[0]), 53, 46, this.subheadingTextColor());
      this.drawString(guiGraphics, EnergyDisplay.of(multiblock.getLastOutput(), multiblock.getTransferCap()).getTextComponent(), 59, 55, this.titleTextColor());
      this.drawString(guiGraphics, MekanismLang.MATRIX_DIMENSIONS.translate(new Object[0]), 8, 82, this.subheadingTextColor());
      if (multiblock.isFormed()) {
         this.drawString(
            guiGraphics,
            MekanismLang.MATRIX_DIMENSION_REPRESENTATION.translate(new Object[]{multiblock.width(), multiblock.height(), multiblock.length()}),
            14,
            91,
            this.titleTextColor()
         );
      }

      this.drawString(guiGraphics, MekanismLang.MATRIX_CONSTITUENTS.translate(new Object[0]), 8, 102, this.subheadingTextColor());
      this.drawString(guiGraphics, MekanismLang.MATRIX_CELLS.translate(new Object[]{multiblock.getCellCount()}), 14, 111, this.titleTextColor());
      this.drawString(guiGraphics, MekanismLang.MATRIX_PROVIDERS.translate(new Object[]{multiblock.getProviderCount()}), 14, 120, this.titleTextColor());
      super.drawForegroundText(guiGraphics, mouseX, mouseY);
   }
}
