package mekanism.client.gui.element.window;

import java.util.Collections;
import mekanism.api.RelativeSide;
import mekanism.api.text.EnumColor;
import mekanism.api.text.TextComponentUtil;
import mekanism.client.gui.GuiMekanism;
import mekanism.client.gui.IGuiWrapper;
import mekanism.client.gui.element.GuiElement;
import mekanism.client.gui.element.GuiInnerScreen;
import mekanism.client.gui.element.button.ColorButton;
import mekanism.client.gui.element.button.MekanismImageButton;
import mekanism.client.gui.element.button.SideDataButton;
import mekanism.client.gui.element.slot.GuiSlot;
import mekanism.client.gui.element.slot.SlotType;
import mekanism.common.Mekanism;
import mekanism.common.MekanismLang;
import mekanism.common.inventory.container.MekanismContainer;
import mekanism.common.inventory.container.SelectedWindowData;
import mekanism.common.lib.transmitter.TransmissionType;
import mekanism.common.network.to_server.PacketConfigurationUpdate;
import mekanism.common.network.to_server.PacketGuiInteract;
import mekanism.common.tile.base.TileEntityMekanism;
import mekanism.common.tile.component.config.DataType;
import mekanism.common.tile.interfaces.ISideConfiguration;
import mekanism.common.util.text.BooleanStateDisplay;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;

public class GuiTransporterConfig<TILE extends TileEntityMekanism & ISideConfiguration> extends GuiWindow {
   private final TILE tile;

   public GuiTransporterConfig(IGuiWrapper gui, int x, int y, TILE tile) {
      super(gui, x, y, 156, 119, SelectedWindowData.WindowType.TRANSPORTER_CONFIG);
      this.tile = tile;
      this.interactionStrategy = GuiWindow.InteractionStrategy.ALL;
      this.addChild(
         new GuiInnerScreen(
            gui,
            this.relativeX + 41,
            this.relativeY + 15,
            74,
            12,
            () -> Collections.singletonList(
               MekanismLang.STRICT_INPUT_ENABLED.translate(new Object[]{BooleanStateDisplay.OnOff.of(tile.getEjector().hasStrictInput())})
            )
         )
      );
      this.addChild(new GuiSlot(SlotType.NORMAL, gui, this.relativeX + 111, this.relativeY + 48));
      this.addChild(
         new MekanismImageButton(
            gui,
            this.relativeX + 136,
            this.relativeY + 6,
            14,
            16,
            this.getButtonLocation("exclamation"),
            () -> Mekanism.packetHandler().sendToServer(new PacketConfigurationUpdate(this.tile.m_58899_())),
            this.getOnHover(MekanismLang.STRICT_INPUT)
         )
      );
      this.addChild(
         new ColorButton(
            gui,
            this.relativeX + 112,
            this.relativeY + 49,
            16,
            16,
            () -> this.tile.getEjector().getOutputColor(),
            () -> Mekanism.packetHandler().sendToServer(new PacketConfigurationUpdate(this.tile.m_58899_(), Screen.m_96638_() ? 2 : 0)),
            () -> Mekanism.packetHandler().sendToServer(new PacketConfigurationUpdate(this.tile.m_58899_(), 1))
         )
      );
      this.addSideDataButton(RelativeSide.BOTTOM, 41, 80);
      this.addSideDataButton(RelativeSide.TOP, 41, 34);
      this.addSideDataButton(RelativeSide.FRONT, 41, 57);
      this.addSideDataButton(RelativeSide.BACK, 18, 80);
      this.addSideDataButton(RelativeSide.LEFT, 18, 57);
      this.addSideDataButton(RelativeSide.RIGHT, 64, 57);
      ((MekanismContainer)((GuiMekanism)this.gui()).m_6262_()).startTracking(0, this.tile.getEjector());
      Mekanism.packetHandler().sendToServer(new PacketGuiInteract(PacketGuiInteract.GuiInteraction.CONTAINER_TRACK_EJECTOR, this.tile, 0));
   }

   private void addSideDataButton(RelativeSide side, int xPos, int yPos) {
      SideDataButton button = this.addChild(
         new SideDataButton(
            this.gui(),
            this.relativeX + xPos,
            this.relativeY + yPos,
            side,
            () -> this.tile.getConfig().getDataType(TransmissionType.ITEM, side),
            () -> this.tile.getEjector().getInputColor(side),
            this.tile,
            () -> null,
            PacketConfigurationUpdate.ConfigurationPacket.INPUT_COLOR,
            this.getOnHover(side)
         )
      );
      if (!this.tile.getEjector().isInputSideEnabled(side)) {
         button.f_93623_ = false;
      }
   }

   @Override
   public void close() {
      super.close();
      Mekanism.packetHandler().sendToServer(new PacketGuiInteract(PacketGuiInteract.GuiInteraction.CONTAINER_STOP_TRACKING, this.tile, 0));
      ((MekanismContainer)((GuiMekanism)this.gui()).m_6262_()).stopTracking(0);
   }

   private GuiElement.IHoverable getOnHover(RelativeSide side) {
      return (onHover, guiGraphics, mouseX, mouseY) -> {
         if (onHover instanceof SideDataButton button) {
            DataType dataType = button.getDataType();
            if (dataType != null) {
               EnumColor color = button.getColor();
               Component colorComponent = (Component)(color == null ? MekanismLang.NONE.translate(new Object[0]) : color.getColoredName());
               this.displayTooltips(guiGraphics, mouseX, mouseY, new Component[]{TextComponentUtil.translate(side.getTranslationKey()), colorComponent});
            }
         }
      };
   }

   @Override
   public void renderForeground(GuiGraphics guiGraphics, int mouseX, int mouseY) {
      super.renderForeground(guiGraphics, mouseX, mouseY);
      this.drawTitleText(guiGraphics, MekanismLang.TRANSPORTER_CONFIG.translate(new Object[0]), 5.0F);
      this.drawCenteredText(guiGraphics, MekanismLang.INPUT.translate(new Object[0]), this.relativeX + 51, this.relativeY + 105, this.subheadingTextColor());
      this.drawCenteredText(guiGraphics, MekanismLang.OUTPUT.translate(new Object[0]), this.relativeX + 121, this.relativeY + 68, this.subheadingTextColor());
   }

   @Override
   protected int getTitlePadEnd() {
      return super.getTitlePadEnd() + 15;
   }
}
