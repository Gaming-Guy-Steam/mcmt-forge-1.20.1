package mekanism.client.gui;

import java.util.ArrayList;
import java.util.List;
import mekanism.client.gui.element.GuiInnerScreen;
import mekanism.client.gui.element.bar.GuiBar;
import mekanism.client.gui.element.bar.GuiDynamicHorizontalRateBar;
import mekanism.client.gui.element.gauge.GaugeType;
import mekanism.client.gui.element.gauge.GuiGasGauge;
import mekanism.client.jei.MekanismJEIRecipeType;
import mekanism.common.MekanismLang;
import mekanism.common.content.sps.SPSMultiblockData;
import mekanism.common.inventory.container.tile.MekanismTileContainer;
import mekanism.common.lib.Color;
import mekanism.common.tile.multiblock.TileEntitySPSCasing;
import mekanism.common.util.text.EnergyDisplay;
import mekanism.common.util.text.TextUtils;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Inventory;
import org.jetbrains.annotations.NotNull;

public class GuiSPS extends GuiMekanismTile<TileEntitySPSCasing, MekanismTileContainer<TileEntitySPSCasing>> {
   public GuiSPS(MekanismTileContainer<TileEntitySPSCasing> container, Inventory inv, Component title) {
      super(container, inv, title);
      this.dynamicSlots = true;
      this.f_97727_ += 16;
      this.f_97731_ = this.f_97727_ - 92;
   }

   @Override
   protected void addGuiElements() {
      super.addGuiElements();
      this.addRenderableWidget(
         new GuiGasGauge(() -> this.tile.getMultiblock().inputTank, () -> this.tile.getMultiblock().getGasTanks(null), GaugeType.STANDARD, this, 7, 17)
      );
      this.addRenderableWidget(
         new GuiGasGauge(() -> this.tile.getMultiblock().outputTank, () -> this.tile.getMultiblock().getGasTanks(null), GaugeType.STANDARD, this, 151, 17)
      );
      this.addRenderableWidget(new GuiInnerScreen(this, 27, 17, 122, 60, () -> {
         List<Component> list = new ArrayList<>();
         SPSMultiblockData multiblock = this.tile.getMultiblock();
         boolean active = multiblock.lastProcessed > 0.0;
         list.add(MekanismLang.STATUS.translate(new Object[]{active ? MekanismLang.ACTIVE : MekanismLang.IDLE}));
         if (active) {
            list.add(MekanismLang.SPS_ENERGY_INPUT.translate(new Object[]{EnergyDisplay.of(multiblock.lastReceivedEnergy)}));
            list.add(MekanismLang.PROCESS_RATE_MB.translate(new Object[]{multiblock.getProcessRate()}));
         }

         return list;
      }).jeiCategories(MekanismJEIRecipeType.SPS));
      this.addRenderableWidget(new GuiDynamicHorizontalRateBar(this, new GuiBar.IBarInfoHandler() {
         @Override
         public Component getTooltip() {
            return MekanismLang.PROGRESS.translate(new Object[]{TextUtils.getPercent(GuiSPS.this.tile.getMultiblock().getScaledProgress())});
         }

         @Override
         public double getLevel() {
            return Math.min(1.0, GuiSPS.this.tile.getMultiblock().getScaledProgress());
         }
      }, 7, 79, 160, Color.ColorFunction.scale(Color.rgbi(60, 45, 74), Color.rgbi(100, 30, 170))));
   }

   @Override
   protected void drawForegroundText(@NotNull GuiGraphics guiGraphics, int mouseX, int mouseY) {
      this.renderTitleText(guiGraphics);
      this.drawString(guiGraphics, this.f_169604_, this.f_97730_, this.f_97731_, this.titleTextColor());
      super.drawForegroundText(guiGraphics, mouseX, mouseY);
   }
}
