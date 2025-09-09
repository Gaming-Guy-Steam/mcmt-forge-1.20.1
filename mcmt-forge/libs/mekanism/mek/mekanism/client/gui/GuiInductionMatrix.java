package mekanism.client.gui;

import java.util.List;
import mekanism.api.math.FloatingLong;
import mekanism.client.SpecialColors;
import mekanism.client.gui.element.GuiElementHolder;
import mekanism.client.gui.element.GuiInnerScreen;
import mekanism.client.gui.element.GuiSideHolder;
import mekanism.client.gui.element.gauge.GaugeType;
import mekanism.client.gui.element.gauge.GuiEnergyGauge;
import mekanism.client.gui.element.slot.GuiSlot;
import mekanism.client.gui.element.slot.SlotType;
import mekanism.client.gui.element.tab.GuiEnergyTab;
import mekanism.client.gui.element.tab.GuiMatrixTab;
import mekanism.common.MekanismLang;
import mekanism.common.content.matrix.MatrixMultiblockData;
import mekanism.common.inventory.container.tile.MekanismTileContainer;
import mekanism.common.tile.multiblock.TileEntityInductionCasing;
import mekanism.common.util.text.EnergyDisplay;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Inventory;
import org.jetbrains.annotations.NotNull;

public class GuiInductionMatrix extends GuiMekanismTile<TileEntityInductionCasing, MekanismTileContainer<TileEntityInductionCasing>> {
   public GuiInductionMatrix(MekanismTileContainer<TileEntityInductionCasing> container, Inventory inv, Component title) {
      super(container, inv, title);
      this.f_97731_ += 2;
      this.dynamicSlots = true;
   }

   @Override
   protected void addGuiElements() {
      this.addRenderableWidget(GuiSideHolder.create(this, -26, 36, 98, true, true, SpecialColors.TAB_ARMOR_SLOTS));
      this.addRenderableWidget(new GuiElementHolder(this, 141, 16, 26, 56));
      super.addGuiElements();
      this.addRenderableWidget(new GuiSlot(SlotType.INNER_HOLDER_SLOT, this, 145, 20));
      this.addRenderableWidget(new GuiSlot(SlotType.INNER_HOLDER_SLOT, this, 145, 50));
      this.addRenderableWidget(
         new GuiInnerScreen(
               this,
               49,
               21,
               84,
               46,
               () -> {
                  MatrixMultiblockData multiblock = this.tile.getMultiblock();
                  return List.of(
                     MekanismLang.ENERGY.translate(new Object[]{EnergyDisplay.of(multiblock.getEnergy())}),
                     MekanismLang.CAPACITY.translate(new Object[]{EnergyDisplay.of(multiblock.getStorageCap())}),
                     MekanismLang.MATRIX_INPUT_AMOUNT
                        .translate(new Object[]{MekanismLang.GENERIC_PER_TICK.translate(new Object[]{EnergyDisplay.of(multiblock.getLastInput())})}),
                     MekanismLang.MATRIX_OUTPUT_AMOUNT
                        .translate(new Object[]{MekanismLang.GENERIC_PER_TICK.translate(new Object[]{EnergyDisplay.of(multiblock.getLastOutput())})})
                  );
               }
            )
            .spacing(2)
      );
      this.addRenderableWidget(new GuiMatrixTab(this, this.tile, GuiMatrixTab.MatrixTab.STAT));
      this.addRenderableWidget(new GuiEnergyGauge(new GuiEnergyGauge.IEnergyInfoHandler() {
         @Override
         public FloatingLong getEnergy() {
            return GuiInductionMatrix.this.tile.getMultiblock().getEnergy();
         }

         @Override
         public FloatingLong getMaxEnergy() {
            return GuiInductionMatrix.this.tile.getMultiblock().getStorageCap();
         }
      }, GaugeType.MEDIUM, this, 7, 16, 34, 56));
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
      this.drawString(guiGraphics, this.f_169604_, this.f_97730_, this.f_97731_, this.titleTextColor());
      super.drawForegroundText(guiGraphics, mouseX, mouseY);
   }
}
