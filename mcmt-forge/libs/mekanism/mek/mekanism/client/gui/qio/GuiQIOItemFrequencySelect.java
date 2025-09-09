package mekanism.client.gui.qio;

import mekanism.client.gui.GuiMekanism;
import mekanism.client.gui.element.button.MekanismImageButton;
import mekanism.client.gui.element.custom.GuiFrequencySelector;
import mekanism.common.Mekanism;
import mekanism.common.MekanismLang;
import mekanism.common.content.qio.QIOFrequency;
import mekanism.common.inventory.container.item.QIOFrequencySelectItemContainer;
import mekanism.common.lib.frequency.FrequencyType;
import mekanism.common.network.to_server.PacketGuiButtonPress;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Inventory;
import org.jetbrains.annotations.NotNull;

public class GuiQIOItemFrequencySelect
   extends GuiMekanism<QIOFrequencySelectItemContainer>
   implements GuiFrequencySelector.IGuiColorFrequencySelector<QIOFrequency>,
   GuiFrequencySelector.IItemGuiFrequencySelector<QIOFrequency, QIOFrequencySelectItemContainer> {
   public GuiQIOItemFrequencySelect(QIOFrequencySelectItemContainer container, Inventory inv, Component title) {
      super(container, inv, title);
      this.f_97727_ -= 11;
      this.f_97729_ = 5;
   }

   @Override
   protected void addGuiElements() {
      super.addGuiElements();
      this.addRenderableWidget(new GuiFrequencySelector<>(this, 17));
      this.addRenderableWidget(
         new MekanismImageButton(
            this,
            6,
            6,
            14,
            this.getButtonLocation("back"),
            () -> Mekanism.packetHandler()
               .sendToServer(
                  new PacketGuiButtonPress(PacketGuiButtonPress.ClickedItemButton.BACK_BUTTON, ((QIOFrequencySelectItemContainer)this.f_97732_).getHand())
               ),
            this.getOnHover(MekanismLang.BACK)
         )
      );
   }

   @Override
   protected void drawForegroundText(@NotNull GuiGraphics guiGraphics, int mouseX, int mouseY) {
      this.renderTitleText(guiGraphics);
      super.drawForegroundText(guiGraphics, mouseX, mouseY);
   }

   @Override
   public FrequencyType<QIOFrequency> getFrequencyType() {
      return FrequencyType.QIO;
   }

   public QIOFrequencySelectItemContainer getFrequencyContainer() {
      return (QIOFrequencySelectItemContainer)this.f_97732_;
   }

   @Override
   public void drawTitleText(GuiGraphics guiGraphics, Component text, float y) {
      int leftShift = 15;
      int xSize = this.getXSize() - leftShift;
      int maxLength = xSize - 12;
      float textWidth = this.getStringWidth(text);
      float scale = Math.min(1.0F, maxLength / textWidth);
      this.drawScaledCenteredText(guiGraphics, text, leftShift + xSize / 2.0F, y, this.titleTextColor(), scale);
   }
}
