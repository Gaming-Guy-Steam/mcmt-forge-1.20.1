package mekanism.client.gui;

import java.util.Collections;
import mekanism.api.math.MathUtils;
import mekanism.client.gui.element.graph.GuiLongGraph;
import mekanism.client.gui.element.tab.GuiBoilerTab;
import mekanism.client.gui.element.tab.GuiHeatTab;
import mekanism.common.MekanismLang;
import mekanism.common.config.MekanismConfig;
import mekanism.common.content.boiler.BoilerMultiblockData;
import mekanism.common.inventory.container.tile.EmptyTileContainer;
import mekanism.common.tile.multiblock.TileEntityBoilerCasing;
import mekanism.common.util.HeatUtils;
import mekanism.common.util.MekanismUtils;
import mekanism.common.util.UnitDisplayUtils;
import mekanism.common.util.text.TextUtils;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Inventory;
import org.jetbrains.annotations.NotNull;

public class GuiBoilerStats extends GuiMekanismTile<TileEntityBoilerCasing, EmptyTileContainer<TileEntityBoilerCasing>> {
   private GuiLongGraph boilGraph;
   private GuiLongGraph maxGraph;

   public GuiBoilerStats(EmptyTileContainer<TileEntityBoilerCasing> container, Inventory inv, Component title) {
      super(container, inv, title);
   }

   @Override
   protected void addGuiElements() {
      super.addGuiElements();
      this.addRenderableWidget(new GuiBoilerTab(this, this.tile, GuiBoilerTab.BoilerTab.MAIN));
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
      this.boilGraph = this.addRenderableWidget(new GuiLongGraph(this, 7, 82, 162, 38, xva$0 -> rec$.translate(new Object[]{xva$0})));
      this.maxGraph = this.addRenderableWidget(new GuiLongGraph(this, 7, 121, 162, 38, xva$0 -> rec$.translate(new Object[]{xva$0})));
      this.maxGraph
         .enableFixedScale(
            MathUtils.clampToLong(
               MekanismConfig.general.superheatingHeatTransfer.get() * this.tile.getMultiblock().superheatingElements / HeatUtils.getWaterThermalEnthalpy()
            )
         );
   }

   @Override
   protected void drawForegroundText(@NotNull GuiGraphics guiGraphics, int mouseX, int mouseY) {
      this.drawCenteredText(guiGraphics, this.f_96539_, 0.0F, this.f_97726_, this.f_97729_, this.titleTextColor());
      BoilerMultiblockData multiblock = this.tile.getMultiblock();
      this.drawString(
         guiGraphics,
         MekanismLang.BOILER_MAX_WATER.translate(new Object[]{TextUtils.format((long)multiblock.waterTank.getCapacity())}),
         8,
         26,
         this.titleTextColor()
      );
      this.drawString(
         guiGraphics, MekanismLang.BOILER_MAX_STEAM.translate(new Object[]{TextUtils.format(multiblock.steamTank.getCapacity())}), 8, 35, this.titleTextColor()
      );
      this.drawString(guiGraphics, MekanismLang.BOILER_HEAT_TRANSFER.translate(new Object[0]), 8, 49, this.subheadingTextColor());
      this.drawString(guiGraphics, MekanismLang.BOILER_HEATERS.translate(new Object[]{multiblock.superheatingElements}), 14, 58, this.titleTextColor());
      this.drawString(
         guiGraphics, MekanismLang.BOILER_CAPACITY.translate(new Object[]{TextUtils.format(multiblock.getBoilCapacity())}), 8, 72, this.titleTextColor()
      );
      super.drawForegroundText(guiGraphics, mouseX, mouseY);
   }

   @Override
   public void m_181908_() {
      super.m_181908_();
      BoilerMultiblockData multiblock = this.tile.getMultiblock();
      this.boilGraph.addData(multiblock.lastBoilRate);
      this.maxGraph.addData(multiblock.lastMaxBoil);
   }
}
