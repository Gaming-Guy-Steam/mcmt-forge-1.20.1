package mekanism.client.gui.element.window;

import java.util.ArrayList;
import java.util.Collections;
import java.util.EnumMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import mekanism.api.RelativeSide;
import mekanism.api.text.EnumColor;
import mekanism.api.text.TextComponentUtil;
import mekanism.client.gui.GuiMekanism;
import mekanism.client.gui.IGuiWrapper;
import mekanism.client.gui.element.GuiElement;
import mekanism.client.gui.element.GuiInnerScreen;
import mekanism.client.gui.element.button.MekanismButton;
import mekanism.client.gui.element.button.MekanismImageButton;
import mekanism.client.gui.element.button.SideDataButton;
import mekanism.client.gui.element.tab.GuiConfigTypeTab;
import mekanism.common.Mekanism;
import mekanism.common.MekanismLang;
import mekanism.common.inventory.container.MekanismContainer;
import mekanism.common.inventory.container.SelectedWindowData;
import mekanism.common.lib.transmitter.TransmissionType;
import mekanism.common.network.to_server.PacketConfigurationUpdate;
import mekanism.common.network.to_server.PacketGuiInteract;
import mekanism.common.tile.base.TileEntityMekanism;
import mekanism.common.tile.component.config.ConfigInfo;
import mekanism.common.tile.component.config.DataType;
import mekanism.common.tile.interfaces.ISideConfiguration;
import mekanism.common.util.text.BooleanStateDisplay;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.network.chat.Component;
import org.jetbrains.annotations.Nullable;

public class GuiSideConfiguration<TILE extends TileEntityMekanism & ISideConfiguration> extends GuiWindow {
   private final List<GuiConfigTypeTab> configTabs = new ArrayList<>();
   private final Map<RelativeSide, SideDataButton> sideConfigButtons = new EnumMap<>(RelativeSide.class);
   private final MekanismButton ejectButton;
   private final TILE tile;
   private TransmissionType currentType;

   public GuiSideConfiguration(IGuiWrapper gui, int x, int y, TILE tile) {
      super(gui, x, y, 156, 135, SelectedWindowData.WindowType.SIDE_CONFIG);
      this.tile = tile;
      this.interactionStrategy = GuiWindow.InteractionStrategy.ALL;
      List<TransmissionType> transmissions = this.tile.getConfig().getTransmissions();
      this.setCurrentType(transmissions.get(0));
      this.addChild(
         new GuiInnerScreen(
               gui,
               this.relativeX + 41,
               this.relativeY + 25,
               74,
               12,
               () -> {
                  ConfigInfo config = this.getCurrentConfig();
                  return config != null && config.canEject()
                     ? Collections.singletonList(MekanismLang.EJECT.translate(new Object[]{BooleanStateDisplay.OnOff.of(config.isEjecting())}))
                     : Collections.singletonList(MekanismLang.NO_EJECT.translate(new Object[0]));
               }
            )
            .tooltip(
               () -> {
                  ConfigInfo config = this.getCurrentConfig();
                  return config != null && config.canEject()
                     ? Collections.emptyList()
                     : Collections.singletonList(MekanismLang.CANT_EJECT_TOOLTIP.translate(new Object[0]));
               }
            )
      );

      for (int i = 0; i < transmissions.size(); i++) {
         GuiConfigTypeTab tab = new GuiConfigTypeTab(
            gui, transmissions.get(i), this.relativeX + (i < 4 ? -26 : this.f_93618_), this.relativeY + 2 + 28 * (i % 4), this, i < 4
         );
         this.addChild(tab);
         this.configTabs.add(tab);
      }

      this.ejectButton = this.addChild(
         new MekanismImageButton(
            gui,
            this.relativeX + 136,
            this.relativeY + 6,
            14,
            this.getButtonLocation("auto_eject"),
            () -> Mekanism.packetHandler()
               .sendToServer(new PacketConfigurationUpdate(PacketConfigurationUpdate.ConfigurationPacket.EJECT, this.tile.m_58899_(), this.currentType)),
            this.getOnHover(MekanismLang.AUTO_EJECT)
         )
      );
      this.addChild(
         new MekanismImageButton(
            gui,
            this.relativeX + 136,
            this.relativeY + 95,
            14,
            this.getButtonLocation("clear_sides"),
            () -> Mekanism.packetHandler()
               .sendToServer(new PacketConfigurationUpdate(PacketConfigurationUpdate.ConfigurationPacket.CLEAR_ALL, this.tile.m_58899_(), this.currentType)),
            this.getOnHover(MekanismLang.SIDE_CONFIG_CLEAR)
         )
      );
      this.addSideDataButton(RelativeSide.BOTTOM, 68, 92);
      this.addSideDataButton(RelativeSide.TOP, 68, 46);
      this.addSideDataButton(RelativeSide.FRONT, 68, 69);
      this.addSideDataButton(RelativeSide.BACK, 45, 92);
      this.addSideDataButton(RelativeSide.LEFT, 45, 69);
      this.addSideDataButton(RelativeSide.RIGHT, 91, 69);
      this.updateTabs();
      ((MekanismContainer)((GuiMekanism)this.gui()).m_6262_()).startTracking(1, this.tile.getConfig());
      Mekanism.packetHandler().sendToServer(new PacketGuiInteract(PacketGuiInteract.GuiInteraction.CONTAINER_TRACK_SIDE_CONFIG, tile, 1));
   }

   private void addSideDataButton(RelativeSide side, int xPos, int yPos) {
      this.sideConfigButtons
         .put(
            side,
            this.addChild(
               new SideDataButton(
                  this.gui(), this.relativeX + xPos, this.relativeY + yPos, side, () -> this.tile.getConfig().getDataType(this.currentType, side), () -> {
                     DataType dataType = this.tile.getConfig().getDataType(this.currentType, side);
                     return dataType == null ? EnumColor.GRAY : dataType.getColor();
                  }, this.tile, () -> this.currentType, PacketConfigurationUpdate.ConfigurationPacket.SIDE_DATA, this.getOnHover(side)
               )
            )
         );
   }

   @Override
   public void close() {
      super.close();
      Mekanism.packetHandler().sendToServer(new PacketGuiInteract(PacketGuiInteract.GuiInteraction.CONTAINER_STOP_TRACKING, this.tile, 1));
      ((MekanismContainer)((GuiMekanism)this.gui()).m_6262_()).stopTracking(1);
   }

   private GuiElement.IHoverable getOnHover(RelativeSide side) {
      return (onHover, guiGraphics, mouseX, mouseY) -> {
         if (onHover instanceof SideDataButton button) {
            DataType dataType = button.getDataType();
            if (dataType != null) {
               this.displayTooltips(
                  guiGraphics,
                  mouseX,
                  mouseY,
                  new Component[]{TextComponentUtil.translate(side.getTranslationKey()), TextComponentUtil.build(dataType.getColor(), dataType)}
               );
            }
         }
      };
   }

   public void setCurrentType(TransmissionType type) {
      this.currentType = type;
   }

   @Nullable
   private ConfigInfo getCurrentConfig() {
      return this.tile.getConfig().getConfig(this.currentType);
   }

   public void updateTabs() {
      for (GuiConfigTypeTab tab : this.configTabs) {
         tab.f_93624_ = this.currentType != tab.getTransmissionType();
      }

      ConfigInfo config = this.getCurrentConfig();
      if (config == null) {
         this.ejectButton.f_93623_ = false;

         for (SideDataButton button : this.sideConfigButtons.values()) {
            button.f_93623_ = false;
         }
      } else {
         this.ejectButton.f_93623_ = config.canEject();

         for (Entry<RelativeSide, SideDataButton> entry : this.sideConfigButtons.entrySet()) {
            entry.getValue().f_93623_ = config.isSideEnabled(entry.getKey());
         }
      }
   }

   @Override
   public void renderForeground(GuiGraphics guiGraphics, int mouseX, int mouseY) {
      super.renderForeground(guiGraphics, mouseX, mouseY);
      this.drawTitleText(guiGraphics, MekanismLang.CONFIG_TYPE.translate(new Object[]{this.currentType}), 5.0F);
      this.drawCenteredText(guiGraphics, MekanismLang.SLOTS.translate(new Object[0]), this.relativeX + 80, this.relativeY + 120, this.subheadingTextColor());
   }

   @Override
   protected int getTitlePadEnd() {
      return super.getTitlePadEnd() + 15;
   }
}
