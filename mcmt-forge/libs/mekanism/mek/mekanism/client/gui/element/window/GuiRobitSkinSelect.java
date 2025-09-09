package mekanism.client.gui.element.window;

import mekanism.api.robit.RobitSkin;
import mekanism.client.gui.GuiMekanism;
import mekanism.client.gui.element.button.TranslationButton;
import mekanism.client.gui.element.scroll.GuiRobitSkinSelectScroll;
import mekanism.client.gui.robit.GuiRobitMain;
import mekanism.common.Mekanism;
import mekanism.common.MekanismLang;
import mekanism.common.entity.EntityRobit;
import mekanism.common.inventory.container.MekanismContainer;
import mekanism.common.inventory.container.SelectedWindowData;
import mekanism.common.inventory.container.entity.robit.MainRobitContainer;
import mekanism.common.network.to_server.PacketGuiInteract;
import mekanism.common.network.to_server.PacketRobit;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.resources.ResourceKey;

public class GuiRobitSkinSelect extends GuiWindow {
   private final GuiRobitSkinSelectScroll selection;
   private final EntityRobit robit;

   public GuiRobitSkinSelect(GuiRobitMain gui, int x, int y, EntityRobit robit) {
      super(gui, x, y, 168, 190, SelectedWindowData.WindowType.SKIN_SELECT);
      this.robit = robit;
      this.selection = this.addChild(
         new GuiRobitSkinSelectScroll(
            this.gui(), this.relativeX + 6, this.relativeY + 18, this.robit, () -> ((MainRobitContainer)gui.m_6262_()).getUnlockedSkins()
         )
      );
      this.addChild(new TranslationButton(gui, this.relativeX + this.f_93618_ / 2 - 61, this.relativeY + 165, 60, 20, MekanismLang.BUTTON_CANCEL, this::close));
      this.addChild(new TranslationButton(gui, this.relativeX + this.f_93618_ / 2 + 1, this.relativeY + 165, 60, 20, MekanismLang.BUTTON_CONFIRM, () -> {
         ResourceKey<RobitSkin> selectedSkin = this.selection.getSelectedSkin();
         if (selectedSkin != robit.getSkin()) {
            Mekanism.packetHandler().sendToServer(new PacketRobit(robit, selectedSkin));
         }

         this.close();
      }));
      ((MainRobitContainer)gui.m_6262_()).startTracking(3, (MekanismContainer.ISpecificContainerTracker)gui.m_6262_());
      Mekanism.packetHandler().sendToServer(new PacketGuiInteract(PacketGuiInteract.GuiInteractionEntity.CONTAINER_TRACK_SKIN_SELECT, this.robit, 3));
   }

   @Override
   public void close() {
      super.close();
      Mekanism.packetHandler().sendToServer(new PacketGuiInteract(PacketGuiInteract.GuiInteractionEntity.CONTAINER_STOP_TRACKING, this.robit, 3));
      ((MekanismContainer)((GuiMekanism)this.gui()).m_6262_()).stopTracking(3);
   }

   @Override
   public void renderForeground(GuiGraphics guiGraphics, int mouseX, int mouseY) {
      super.renderForeground(guiGraphics, mouseX, mouseY);
      this.drawTitleText(guiGraphics, MekanismLang.ROBIT_SKIN_SELECT.translate(new Object[0]), 7.0F);
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
