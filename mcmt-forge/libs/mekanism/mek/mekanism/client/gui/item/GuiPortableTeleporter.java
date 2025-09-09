package mekanism.client.gui.item;

import mekanism.api.energy.IEnergyContainer;
import mekanism.client.ClientTickHandler;
import mekanism.client.gui.GuiMekanism;
import mekanism.client.gui.element.bar.GuiBar;
import mekanism.client.gui.element.bar.GuiVerticalPowerBar;
import mekanism.client.gui.element.button.MekanismButton;
import mekanism.client.gui.element.button.TranslationButton;
import mekanism.client.gui.element.custom.GuiFrequencySelector;
import mekanism.client.gui.element.custom.GuiTeleporterStatus;
import mekanism.common.MekanismLang;
import mekanism.common.content.teleporter.TeleporterFrequency;
import mekanism.common.inventory.container.item.PortableTeleporterContainer;
import mekanism.common.lib.frequency.FrequencyType;
import mekanism.common.util.StorageUtils;
import mekanism.common.util.text.EnergyDisplay;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Inventory;
import org.jetbrains.annotations.NotNull;

public class GuiPortableTeleporter
   extends GuiMekanism<PortableTeleporterContainer>
   implements GuiFrequencySelector.IItemGuiFrequencySelector<TeleporterFrequency, PortableTeleporterContainer>,
   GuiFrequencySelector.IGuiColorFrequencySelector<TeleporterFrequency> {
   private MekanismButton teleportButton;

   public GuiPortableTeleporter(PortableTeleporterContainer container, Inventory inv, Component title) {
      super(container, inv, title);
      this.f_97727_ = 172;
      this.f_97729_ = 4;
   }

   @Override
   protected void addGuiElements() {
      super.addGuiElements();
      this.addRenderableWidget(new GuiTeleporterStatus(this, () -> this.getFrequency() != null, ((PortableTeleporterContainer)this.f_97732_)::getStatus));
      this.addRenderableWidget(new GuiVerticalPowerBar(this, new GuiBar.IBarInfoHandler() {
         @Override
         public Component getTooltip() {
            IEnergyContainer container = StorageUtils.getEnergyContainer(((PortableTeleporterContainer)GuiPortableTeleporter.this.f_97732_).getStack(), 0);
            return container == null ? EnergyDisplay.ZERO.getTextComponent() : EnergyDisplay.of(container).getTextComponent();
         }

         @Override
         public double getLevel() {
            IEnergyContainer container = StorageUtils.getEnergyContainer(((PortableTeleporterContainer)GuiPortableTeleporter.this.f_97732_).getStack(), 0);
            return container == null ? 0.0 : container.getEnergy().divideToLevel(container.getMaxEnergy());
         }
      }, 158, 26));
      this.teleportButton = this.addRenderableWidget(new TranslationButton(this, 42, 147, 92, 20, MekanismLang.BUTTON_TELEPORT, () -> {
         TeleporterFrequency frequency = this.getFrequency();
         if (frequency != null && ((PortableTeleporterContainer)this.f_97732_).getStatus() == 1) {
            ClientTickHandler.portableTeleport(this.getMinecraft().f_91074_, ((PortableTeleporterContainer)this.f_97732_).getHand(), frequency.getIdentity());
            this.getMinecraft().f_91074_.m_6915_();
         } else {
            this.teleportButton.f_93623_ = false;
         }
      }));
      this.teleportButton.f_93623_ = false;
      this.addRenderableWidget(new GuiFrequencySelector<>(this, 14));
   }

   @Override
   public void buttonsUpdated() {
      this.teleportButton.f_93623_ = ((PortableTeleporterContainer)this.f_97732_).getStatus() == 1 && this.getFrequency() != null;
   }

   @Override
   protected void drawForegroundText(@NotNull GuiGraphics guiGraphics, int mouseX, int mouseY) {
      this.renderTitleText(guiGraphics);
      super.drawForegroundText(guiGraphics, mouseX, mouseY);
   }

   @Override
   public FrequencyType<TeleporterFrequency> getFrequencyType() {
      return FrequencyType.TELEPORTER;
   }

   public PortableTeleporterContainer getFrequencyContainer() {
      return (PortableTeleporterContainer)this.f_97732_;
   }
}
