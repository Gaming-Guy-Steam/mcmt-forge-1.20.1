package mekanism.client.gui.machine;

import java.util.List;
import mekanism.api.math.FloatingLong;
import mekanism.client.gui.GuiMekanismTile;
import mekanism.client.gui.element.GuiInnerScreen;
import mekanism.client.gui.element.bar.GuiVerticalPowerBar;
import mekanism.client.gui.element.tab.GuiEnergyTab;
import mekanism.client.gui.element.tab.GuiHeatTab;
import mekanism.client.gui.element.text.GuiTextField;
import mekanism.common.Mekanism;
import mekanism.common.MekanismLang;
import mekanism.common.inventory.container.tile.MekanismTileContainer;
import mekanism.common.network.to_server.PacketGuiSetEnergy;
import mekanism.common.tile.machine.TileEntityResistiveHeater;
import mekanism.common.util.MekanismUtils;
import mekanism.common.util.UnitDisplayUtils;
import mekanism.common.util.text.EnergyDisplay;
import mekanism.common.util.text.InputValidator;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Inventory;
import org.jetbrains.annotations.NotNull;

public class GuiResistiveHeater extends GuiMekanismTile<TileEntityResistiveHeater, MekanismTileContainer<TileEntityResistiveHeater>> {
   private GuiTextField energyUsageField;

   public GuiResistiveHeater(MekanismTileContainer<TileEntityResistiveHeater> container, Inventory inv, Component title) {
      super(container, inv, title);
      this.f_97731_ += 2;
      this.dynamicSlots = true;
   }

   @Override
   protected void addGuiElements() {
      super.addGuiElements();
      this.addRenderableWidget(
         new GuiInnerScreen(
               this,
               48,
               23,
               80,
               42,
               () -> List.of(
                  MekanismLang.TEMPERATURE
                     .translate(
                        new Object[]{MekanismUtils.getTemperatureDisplay(this.tile.getTotalTemperature(), UnitDisplayUtils.TemperatureUnit.KELVIN, true)}
                     ),
                  MekanismLang.RESISTIVE_HEATER_USAGE.translate(new Object[]{EnergyDisplay.of(this.tile.getEnergyContainer().getEnergyPerTick())})
               )
            )
            .clearFormat()
      );
      this.addRenderableWidget(new GuiVerticalPowerBar(this, this.tile.getEnergyContainer(), 164, 15));
      this.addRenderableWidget(new GuiEnergyTab(this, this.tile.getEnergyContainer(), this.tile::getEnergyUsed));
      this.addRenderableWidget(
         new GuiHeatTab(
            this,
            () -> {
               Component temp = MekanismUtils.getTemperatureDisplay(this.tile.getTotalTemperature(), UnitDisplayUtils.TemperatureUnit.KELVIN, true);
               Component transfer = MekanismUtils.getTemperatureDisplay(this.tile.getLastTransferLoss(), UnitDisplayUtils.TemperatureUnit.KELVIN, false);
               Component environment = MekanismUtils.getTemperatureDisplay(this.tile.getLastEnvironmentLoss(), UnitDisplayUtils.TemperatureUnit.KELVIN, false);
               return List.of(
                  MekanismLang.TEMPERATURE.translate(new Object[]{temp}),
                  MekanismLang.TRANSFERRED_RATE.translate(new Object[]{transfer}),
                  MekanismLang.DISSIPATED_RATE.translate(new Object[]{environment})
               );
            }
         )
      );
      this.energyUsageField = this.addRenderableWidget(new GuiTextField(this, 50, 51, 76, 12));
      this.energyUsageField.setMaxLength(7);
      this.energyUsageField.setInputValidator(InputValidator.DIGIT).configureDigitalInput(this::setEnergyUsage);
      this.energyUsageField.m_93692_(true);
   }

   @Override
   protected void drawForegroundText(@NotNull GuiGraphics guiGraphics, int mouseX, int mouseY) {
      this.renderTitleText(guiGraphics);
      this.drawString(guiGraphics, this.f_169604_, this.f_97730_, this.f_97731_, this.titleTextColor());
      super.drawForegroundText(guiGraphics, mouseX, mouseY);
   }

   private void setEnergyUsage() {
      if (!this.energyUsageField.getText().isEmpty()) {
         try {
            Mekanism.packetHandler()
               .sendToServer(
                  new PacketGuiSetEnergy(
                     PacketGuiSetEnergy.GuiEnergyValue.ENERGY_USAGE,
                     this.tile.m_58899_(),
                     MekanismUtils.convertToJoules(FloatingLong.parseFloatingLong(this.energyUsageField.getText()))
                  )
               );
         } catch (NumberFormatException var2) {
         }

         this.energyUsageField.setText("");
      }
   }
}
