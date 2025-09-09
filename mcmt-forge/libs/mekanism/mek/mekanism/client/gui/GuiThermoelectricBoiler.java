package mekanism.client.gui;

import java.util.Collections;
import java.util.List;
import mekanism.api.text.EnumColor;
import mekanism.client.gui.element.GuiInnerScreen;
import mekanism.client.gui.element.bar.GuiBar;
import mekanism.client.gui.element.bar.GuiVerticalRateBar;
import mekanism.client.gui.element.gauge.GaugeType;
import mekanism.client.gui.element.gauge.GuiFluidGauge;
import mekanism.client.gui.element.gauge.GuiGasGauge;
import mekanism.client.gui.element.tab.GuiBoilerTab;
import mekanism.client.gui.element.tab.GuiHeatTab;
import mekanism.client.jei.MekanismJEIRecipeType;
import mekanism.common.MekanismLang;
import mekanism.common.config.MekanismConfig;
import mekanism.common.content.boiler.BoilerMultiblockData;
import mekanism.common.inventory.container.tile.MekanismTileContainer;
import mekanism.common.tile.multiblock.TileEntityBoilerCasing;
import mekanism.common.util.HeatUtils;
import mekanism.common.util.MekanismUtils;
import mekanism.common.util.UnitDisplayUtils;
import mekanism.common.util.text.TextUtils;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Inventory;
import org.jetbrains.annotations.NotNull;

public class GuiThermoelectricBoiler extends GuiMekanismTile<TileEntityBoilerCasing, MekanismTileContainer<TileEntityBoilerCasing>> {
   public GuiThermoelectricBoiler(MekanismTileContainer<TileEntityBoilerCasing> container, Inventory inv, Component title) {
      super(container, inv, title);
      this.dynamicSlots = true;
      this.f_97726_ += 40;
      this.f_97731_ += 2;
      this.f_97729_ = 5;
   }

   @Override
   protected void addGuiElements() {
      super.addGuiElements();
      this.addRenderableWidget(
         new GuiInnerScreen(
               this,
               60,
               23,
               96,
               40,
               () -> {
                  BoilerMultiblockData multiblock = this.tile.getMultiblock();
                  return List.of(
                     MekanismLang.TEMPERATURE
                        .translate(
                           new Object[]{MekanismUtils.getTemperatureDisplay(multiblock.getTotalTemperature(), UnitDisplayUtils.TemperatureUnit.KELVIN, true)}
                        ),
                     MekanismLang.BOIL_RATE.translate(new Object[]{TextUtils.format((long)multiblock.lastBoilRate)}),
                     MekanismLang.MAX_BOIL_RATE.translate(new Object[]{TextUtils.format((long)multiblock.lastMaxBoil)})
                  );
               }
            )
            .jeiCategories(MekanismJEIRecipeType.BOILER)
      );
      this.addRenderableWidget(new GuiBoilerTab(this, this.tile, GuiBoilerTab.BoilerTab.STAT));
      this.addRenderableWidget(new GuiVerticalRateBar(this, new GuiBar.IBarInfoHandler() {
         @Override
         public Component getTooltip() {
            return MekanismLang.BOIL_RATE.translate(new Object[]{TextUtils.format((long)GuiThermoelectricBoiler.this.tile.getMultiblock().lastBoilRate)});
         }

         @Override
         public double getLevel() {
            BoilerMultiblockData multiblock = GuiThermoelectricBoiler.this.tile.getMultiblock();
            return Math.min(1.0, (double)multiblock.lastBoilRate / multiblock.lastMaxBoil);
         }
      }, 44, 13));
      this.addRenderableWidget(
         new GuiVerticalRateBar(
            this,
            new GuiBar.IBarInfoHandler() {
               @Override
               public Component getTooltip() {
                  return MekanismLang.MAX_BOIL_RATE
                     .translate(new Object[]{TextUtils.format((long)GuiThermoelectricBoiler.this.tile.getMultiblock().lastMaxBoil)});
               }

               @Override
               public double getLevel() {
                  BoilerMultiblockData multiblock = GuiThermoelectricBoiler.this.tile.getMultiblock();
                  return Math.min(
                     1.0,
                     multiblock.lastMaxBoil
                        * HeatUtils.getWaterThermalEnthalpy()
                        / (multiblock.superheatingElements * MekanismConfig.general.superheatingHeatTransfer.get())
                  );
               }
            },
            164,
            13
         )
      );
      this.addRenderableWidget(
         new GuiGasGauge(
               () -> this.tile.getMultiblock().superheatedCoolantTank, () -> this.tile.getMultiblock().getGasTanks(null), GaugeType.STANDARD, this, 6, 13
            )
            .setLabel(MekanismLang.BOILER_HEATED_COOLANT_TANK.translateColored(EnumColor.ORANGE, new Object[0]))
      );
      this.addRenderableWidget(
         new GuiFluidGauge(() -> this.tile.getMultiblock().waterTank, () -> this.tile.getMultiblock().getFluidTanks(null), GaugeType.STANDARD, this, 26, 13)
            .setLabel(MekanismLang.BOILER_WATER_TANK.translateColored(EnumColor.INDIGO, new Object[0]))
      );
      this.addRenderableWidget(
         new GuiGasGauge(() -> this.tile.getMultiblock().steamTank, () -> this.tile.getMultiblock().getGasTanks(null), GaugeType.STANDARD, this, 172, 13)
            .setLabel(MekanismLang.BOILER_STEAM_TANK.translateColored(EnumColor.GRAY, new Object[0]))
      );
      this.addRenderableWidget(
         new GuiGasGauge(
               () -> this.tile.getMultiblock().cooledCoolantTank, () -> this.tile.getMultiblock().getGasTanks(null), GaugeType.STANDARD, this, 192, 13
            )
            .setLabel(MekanismLang.BOILER_COOLANT_TANK.translateColored(EnumColor.AQUA, new Object[0]))
      );
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

   @Override
   protected void drawForegroundText(@NotNull GuiGraphics guiGraphics, int mouseX, int mouseY) {
      this.renderTitleText(guiGraphics);
      this.drawString(guiGraphics, this.f_169604_, this.f_97730_, this.f_97731_, this.titleTextColor());
      super.drawForegroundText(guiGraphics, mouseX, mouseY);
   }
}
