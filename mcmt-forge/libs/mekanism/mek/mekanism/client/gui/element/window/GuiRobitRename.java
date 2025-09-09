package mekanism.client.gui.element.window;

import mekanism.client.gui.IGuiWrapper;
import mekanism.client.gui.element.button.TranslationButton;
import mekanism.client.gui.element.text.GuiTextField;
import mekanism.common.Mekanism;
import mekanism.common.MekanismLang;
import mekanism.common.entity.EntityRobit;
import mekanism.common.inventory.container.SelectedWindowData;
import mekanism.common.network.to_server.PacketRobit;
import net.minecraft.client.gui.GuiGraphics;

public class GuiRobitRename extends GuiWindow {
   private final GuiTextField nameChangeField;
   private final EntityRobit robit;

   public GuiRobitRename(IGuiWrapper gui, int x, int y, EntityRobit robit) {
      super(gui, x, y, 122, 58, SelectedWindowData.WindowType.RENAME);
      this.robit = robit;
      this.addChild(new TranslationButton(gui, this.relativeX + 31, this.relativeY + 32, 60, 20, MekanismLang.BUTTON_CONFIRM, this::changeName));
      this.nameChangeField = this.addChild(new GuiTextField(gui, this.relativeX + 21, this.relativeY + 17, 80, 12));
      this.nameChangeField.setMaxLength(12);
      this.nameChangeField.setCanLoseFocus(false);
      this.nameChangeField.m_93692_(true);
      this.nameChangeField.setEnterHandler(this::changeName);
   }

   private void changeName() {
      String name = this.nameChangeField.getText().trim();
      if (!name.isEmpty()) {
         Mekanism.packetHandler().sendToServer(new PacketRobit(this.robit, name));
         this.close();
      }
   }

   @Override
   public void renderForeground(GuiGraphics guiGraphics, int mouseX, int mouseY) {
      super.renderForeground(guiGraphics, mouseX, mouseY);
      this.drawTitleText(guiGraphics, MekanismLang.ROBIT_RENAME.translate(new Object[0]), 7.0F);
   }

   @Override
   public boolean m_5953_(double mouseX, double mouseY) {
      return true;
   }

   @Override
   protected boolean isFocusOverlay() {
      return true;
   }
}
