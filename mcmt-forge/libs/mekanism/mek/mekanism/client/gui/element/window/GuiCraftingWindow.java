package mekanism.client.gui.element.window;

import java.util.ArrayList;
import java.util.List;
import mekanism.client.gui.IGuiWrapper;
import mekanism.client.gui.element.GuiRightArrow;
import mekanism.client.gui.element.button.MekanismImageButton;
import mekanism.client.gui.element.slot.GuiVirtualSlot;
import mekanism.client.gui.element.slot.SlotType;
import mekanism.common.Mekanism;
import mekanism.common.MekanismLang;
import mekanism.common.inventory.container.QIOItemViewerContainer;
import mekanism.common.inventory.container.SelectedWindowData;
import mekanism.common.network.to_server.PacketQIOClearCraftingWindow;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.Screen;

public class GuiCraftingWindow extends GuiWindow {
   private final List<GuiVirtualSlot> slots;
   private final byte index;
   private QIOItemViewerContainer container;

   public GuiCraftingWindow(IGuiWrapper gui, int x, int y, QIOItemViewerContainer container, byte index) {
      super(gui, x, y, 118, 80, new SelectedWindowData(SelectedWindowData.WindowType.CRAFTING, index));
      this.index = index;
      this.container = container;
      this.interactionStrategy = GuiWindow.InteractionStrategy.ALL;
      this.slots = new ArrayList<>();

      for (int row = 0; row < 3; row++) {
         for (int column = 0; column < 3; column++) {
            this.slots
               .add(
                  this.addChild(
                     new GuiVirtualSlot(
                        this,
                        SlotType.NORMAL,
                        gui,
                        this.relativeX + 8 + column * 18,
                        this.relativeY + 18 + row * 18,
                        this.container.getCraftingWindowSlot(this.index, row * 3 + column)
                     )
                  )
               );
         }
      }

      this.addChild(new GuiRightArrow(gui, this.relativeX + 66, this.relativeY + 38).jeiCrafting());
      this.slots
         .add(
            this.addChild(
               new GuiVirtualSlot(this, SlotType.NORMAL, gui, this.relativeX + 92, this.relativeY + 36, this.container.getCraftingWindowSlot(this.index, 9))
            )
         );
      this.addChild(
         new MekanismImageButton(
            gui,
            this.relativeX + this.f_93618_ - 20,
            this.relativeY + this.f_93619_ - 20,
            14,
            this.getButtonLocation("clear_sides"),
            () -> Mekanism.packetHandler().sendToServer(new PacketQIOClearCraftingWindow(index, Screen.m_96638_())),
            this.getOnHover(MekanismLang.CRAFTING_WINDOW_CLEAR)
         )
      );
   }

   public void updateContainer(QIOItemViewerContainer container) {
      this.container = container;

      for (int i = 0; i < this.slots.size(); i++) {
         this.slots.get(i).updateVirtualSlot(this, this.container.getCraftingWindowSlot(this.index, i));
      }
   }

   public byte getIndex() {
      return this.index;
   }

   @Override
   public void renderForeground(GuiGraphics guiGraphics, int mouseX, int mouseY) {
      super.renderForeground(guiGraphics, mouseX, mouseY);
      this.drawTitleText(guiGraphics, MekanismLang.CRAFTING_WINDOW.translate(new Object[]{this.index + 1}), 6.0F);
   }
}
