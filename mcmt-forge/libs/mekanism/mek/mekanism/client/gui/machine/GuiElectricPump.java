package mekanism.client.gui.machine;

import java.util.ArrayList;
import java.util.List;
import mekanism.client.gui.GuiMekanismTile;
import mekanism.client.gui.element.GuiDownArrow;
import mekanism.client.gui.element.GuiInnerScreen;
import mekanism.client.gui.element.bar.GuiVerticalPowerBar;
import mekanism.client.gui.element.gauge.GaugeType;
import mekanism.client.gui.element.gauge.GuiFluidGauge;
import mekanism.client.gui.element.tab.GuiEnergyTab;
import mekanism.common.MekanismLang;
import mekanism.common.capabilities.energy.MachineEnergyContainer;
import mekanism.common.inventory.container.tile.MekanismTileContainer;
import mekanism.common.inventory.warning.WarningTracker;
import mekanism.common.tile.machine.TileEntityElectricPump;
import mekanism.common.util.text.EnergyDisplay;
import mekanism.common.util.text.TextUtils;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Inventory;
import net.minecraftforge.fluids.FluidStack;
import org.jetbrains.annotations.NotNull;

public class GuiElectricPump extends GuiMekanismTile<TileEntityElectricPump, MekanismTileContainer<TileEntityElectricPump>> {
   public GuiElectricPump(MekanismTileContainer<TileEntityElectricPump> container, Inventory inv, Component title) {
      super(container, inv, title);
      this.f_97731_ += 2;
      this.dynamicSlots = true;
   }

   @Override
   protected void addGuiElements() {
      super.addGuiElements();
      this.addRenderableWidget(new GuiInnerScreen(this, 54, 23, 80, 41, () -> {
         List<Component> list = new ArrayList<>();
         list.add(EnergyDisplay.of(this.tile.getEnergyContainer()).getTextComponent());
         FluidStack fluidStack = this.tile.fluidTank.getFluid();
         if (fluidStack.isEmpty()) {
            FluidStack fallBack = this.tile.getActiveType();
            if (fallBack.isEmpty()) {
               list.add(MekanismLang.NO_FLUID.translate(new Object[0]));
            } else {
               list.add(fallBack.getDisplayName());
            }
         } else {
            list.add(MekanismLang.GENERIC_STORED_MB.translate(new Object[]{fluidStack, TextUtils.format((long)fluidStack.getAmount())}));
         }

         return list;
      }));
      this.addRenderableWidget(new GuiDownArrow(this, 32, 39));
      this.addRenderableWidget(new GuiVerticalPowerBar(this, this.tile.getEnergyContainer(), 164, 15))
         .warning(WarningTracker.WarningType.NOT_ENOUGH_ENERGY, () -> {
            MachineEnergyContainer<TileEntityElectricPump> energyContainer = this.tile.getEnergyContainer();
            return energyContainer.getEnergyPerTick().greaterThan(energyContainer.getEnergy());
         });
      this.addRenderableWidget(new GuiFluidGauge(() -> this.tile.fluidTank, () -> this.tile.getFluidTanks(null), GaugeType.STANDARD, this, 6, 13))
         .warning(WarningTracker.WarningType.NO_SPACE_IN_OUTPUT, () -> this.tile.fluidTank.getNeeded() < this.tile.estimateIncrementAmount());
      this.addRenderableWidget(new GuiEnergyTab(this, this.tile.getEnergyContainer(), this.tile::usedEnergy));
   }

   @Override
   protected void drawForegroundText(@NotNull GuiGraphics guiGraphics, int mouseX, int mouseY) {
      this.renderTitleText(guiGraphics);
      this.drawString(guiGraphics, this.f_169604_, this.f_97730_, this.f_97731_, this.titleTextColor());
      super.drawForegroundText(guiGraphics, mouseX, mouseY);
   }
}
