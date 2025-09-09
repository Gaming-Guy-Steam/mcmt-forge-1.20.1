package mekanism.client.gui;

import java.math.BigDecimal;
import mekanism.api.math.FloatingLong;
import mekanism.client.gui.element.gauge.GaugeType;
import mekanism.client.gui.element.gauge.GuiEnergyGauge;
import mekanism.client.gui.element.tab.GuiAmplifierTab;
import mekanism.client.gui.element.text.GuiTextField;
import mekanism.common.Mekanism;
import mekanism.common.MekanismLang;
import mekanism.common.inventory.container.tile.MekanismTileContainer;
import mekanism.common.network.to_server.PacketGuiInteract;
import mekanism.common.network.to_server.PacketGuiSetEnergy;
import mekanism.common.tile.laser.TileEntityLaserAmplifier;
import mekanism.common.util.MekanismUtils;
import mekanism.common.util.text.EnergyDisplay;
import mekanism.common.util.text.InputValidator;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Inventory;
import org.jetbrains.annotations.NotNull;

public class GuiLaserAmplifier extends GuiMekanismTile<TileEntityLaserAmplifier, MekanismTileContainer<TileEntityLaserAmplifier>> {
   private GuiTextField minField;
   private GuiTextField maxField;
   private GuiTextField timerField;

   public GuiLaserAmplifier(MekanismTileContainer<TileEntityLaserAmplifier> container, Inventory inv, Component title) {
      super(container, inv, title);
      this.dynamicSlots = true;
   }

   @Override
   protected void addGuiElements() {
      super.addGuiElements();
      this.addRenderableWidget(new GuiEnergyGauge(this.tile.getEnergyContainer(), GaugeType.STANDARD, this, 6, 10));
      this.addRenderableWidget(new GuiAmplifierTab(this, this.tile));
      this.timerField = this.addRenderableWidget(new GuiTextField(this, 96, 28, 36, 11));
      this.timerField.setMaxLength(4);
      this.timerField.setEnterHandler(this::setTime);
      this.timerField.setInputValidator(InputValidator.DIGIT);
      this.minField = this.addRenderableWidget(new GuiTextField(this, 96, 43, 72, 11));
      this.minField.setMaxLength(10);
      this.minField.setEnterHandler(this::setMinThreshold);
      this.minField.setInputValidator(InputValidator.SCI_NOTATION);
      this.maxField = this.addRenderableWidget(new GuiTextField(this, 96, 58, 72, 11));
      this.maxField.setMaxLength(10);
      this.maxField.setEnterHandler(this::setMaxThreshold);
      this.maxField.setInputValidator(InputValidator.SCI_NOTATION);
   }

   @Override
   protected void drawForegroundText(@NotNull GuiGraphics guiGraphics, int mouseX, int mouseY) {
      this.renderTitleText(guiGraphics);
      this.drawString(guiGraphics, this.f_169604_, this.f_97730_, this.f_97731_, this.titleTextColor());
      if (this.tile.getDelay() > 0) {
         this.drawTextScaledBound(guiGraphics, MekanismLang.DELAY.translate(new Object[]{this.tile.getDelay()}), 26.0F, 30.0F, this.titleTextColor(), 68.0F);
      } else {
         this.drawTextScaledBound(guiGraphics, MekanismLang.NO_DELAY.translate(new Object[0]), 26.0F, 30.0F, this.titleTextColor(), 68.0F);
      }

      this.drawTextScaledBound(
         guiGraphics, MekanismLang.MIN.translate(new Object[]{EnergyDisplay.of(this.tile.getMinThreshold())}), 26.0F, 45.0F, this.titleTextColor(), 68.0F
      );
      this.drawTextScaledBound(
         guiGraphics, MekanismLang.MAX.translate(new Object[]{EnergyDisplay.of(this.tile.getMaxThreshold())}), 26.0F, 60.0F, this.titleTextColor(), 68.0F
      );
      super.drawForegroundText(guiGraphics, mouseX, mouseY);
   }

   private FloatingLong parseFloatingLong(GuiTextField textField) {
      String text = textField.getText();
      if (text.contains("E")) {
         text = new BigDecimal(text).toPlainString();
      }

      return FloatingLong.parseFloatingLong(text);
   }

   private void setMinThreshold() {
      if (!this.minField.getText().isEmpty()) {
         try {
            Mekanism.packetHandler()
               .sendToServer(
                  new PacketGuiSetEnergy(
                     PacketGuiSetEnergy.GuiEnergyValue.MIN_THRESHOLD,
                     this.tile.m_58899_(),
                     MekanismUtils.convertToJoules(this.parseFloatingLong(this.minField))
                  )
               );
         } catch (NumberFormatException var2) {
         }

         this.minField.setText("");
      }
   }

   private void setMaxThreshold() {
      if (!this.maxField.getText().isEmpty()) {
         try {
            Mekanism.packetHandler()
               .sendToServer(
                  new PacketGuiSetEnergy(
                     PacketGuiSetEnergy.GuiEnergyValue.MAX_THRESHOLD,
                     this.tile.m_58899_(),
                     MekanismUtils.convertToJoules(this.parseFloatingLong(this.maxField))
                  )
               );
         } catch (NumberFormatException var2) {
         }

         this.maxField.setText("");
      }
   }

   private void setTime() {
      if (!this.timerField.getText().isEmpty()) {
         try {
            Mekanism.packetHandler()
               .sendToServer(new PacketGuiInteract(PacketGuiInteract.GuiInteraction.SET_TIME, this.tile, Integer.parseInt(this.timerField.getText())));
         } catch (NumberFormatException var2) {
         }

         this.timerField.setText("");
      }
   }
}
