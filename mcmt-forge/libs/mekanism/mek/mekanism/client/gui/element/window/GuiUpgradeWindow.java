package mekanism.client.gui.element.window;

import java.util.EnumMap;
import java.util.Map;
import mekanism.api.Upgrade;
import mekanism.client.gui.GuiMekanism;
import mekanism.client.gui.IGuiWrapper;
import mekanism.client.gui.element.GuiInnerScreen;
import mekanism.client.gui.element.button.DigitalButton;
import mekanism.client.gui.element.button.MekanismButton;
import mekanism.client.gui.element.custom.GuiSupportedUpgrades;
import mekanism.client.gui.element.progress.GuiProgress;
import mekanism.client.gui.element.progress.ProgressType;
import mekanism.client.gui.element.scroll.GuiUpgradeScrollList;
import mekanism.client.gui.element.slot.GuiVirtualSlot;
import mekanism.client.gui.element.slot.SlotType;
import mekanism.client.render.IFancyFontRenderer;
import mekanism.common.Mekanism;
import mekanism.common.MekanismLang;
import mekanism.common.inventory.container.MekanismContainer;
import mekanism.common.inventory.container.SelectedWindowData;
import mekanism.common.inventory.container.tile.MekanismTileContainer;
import mekanism.common.network.to_server.PacketGuiInteract;
import mekanism.common.tile.base.TileEntityMekanism;
import mekanism.common.util.UpgradeUtils;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;

public class GuiUpgradeWindow extends GuiWindow {
   private final Map<Upgrade, IFancyFontRenderer.WrappedTextRenderer> upgradeTypeData = new EnumMap<>(Upgrade.class);
   private final IFancyFontRenderer.WrappedTextRenderer noSelection = new IFancyFontRenderer.WrappedTextRenderer(
      this, MekanismLang.UPGRADE_NO_SELECTION.translate(new Object[0])
   );
   private final TileEntityMekanism tile;
   private final MekanismButton removeButton;
   private final GuiUpgradeScrollList scrollList;

   public GuiUpgradeWindow(IGuiWrapper gui, int x, int y, TileEntityMekanism tile) {
      super(gui, x, y, 156, 76 + 12 * GuiSupportedUpgrades.calculateNeededRows(), SelectedWindowData.WindowType.UPGRADE);
      this.tile = tile;
      this.interactionStrategy = GuiWindow.InteractionStrategy.ALL;
      this.scrollList = this.addChild(
         new GuiUpgradeScrollList(gui, this.relativeX + 6, this.relativeY + 18, 66, 50, tile.getComponent(), this::updateEnabledButtons)
      );
      this.addChild(new GuiSupportedUpgrades(gui, this.relativeX + 6, this.relativeY + 68, tile.getComponent().getSupportedTypes()));
      this.addChild(new GuiInnerScreen(gui, this.relativeX + 72, this.relativeY + 18, 59, 50));
      this.addChild(
         new GuiProgress(() -> this.tile.getComponent().getScaledUpgradeProgress(), ProgressType.INSTALLING, gui, this.relativeX + 134, this.relativeY + 37)
      );
      this.addChild(new GuiProgress(() -> 0.0, ProgressType.UNINSTALLING, gui, this.relativeX + 134, this.relativeY + 59));
      this.removeButton = this.addChild(
         new DigitalButton(
            gui,
            this.relativeX + 73,
            this.relativeY + 54,
            56,
            12,
            MekanismLang.UPGRADE_UNINSTALL,
            () -> {
               if (this.scrollList.hasSelection()) {
                  Mekanism.packetHandler()
                     .sendToServer(
                        new PacketGuiInteract(
                           Screen.m_96638_() ? PacketGuiInteract.GuiInteraction.REMOVE_ALL_UPGRADE : PacketGuiInteract.GuiInteraction.REMOVE_UPGRADE,
                           this.tile,
                           this.scrollList.getSelection().ordinal()
                        )
                     );
               }
            },
            this.getOnHover(MekanismLang.UPGRADE_UNINSTALL_TOOLTIP)
         )
      );
      MekanismTileContainer<?> container = (MekanismTileContainer<?>)((GuiMekanism)this.gui()).m_6262_();
      this.addChild(new GuiVirtualSlot(this, SlotType.NORMAL, gui, this.relativeX + 133, this.relativeY + 18, container.getUpgradeSlot()));
      this.addChild(new GuiVirtualSlot(this, SlotType.NORMAL, gui, this.relativeX + 133, this.relativeY + 73, container.getUpgradeOutputSlot()));
      this.updateEnabledButtons();
      container.startTracking(2, tile.getComponent());
      Mekanism.packetHandler().sendToServer(new PacketGuiInteract(PacketGuiInteract.GuiInteraction.CONTAINER_TRACK_UPGRADES, tile, 2));
   }

   @Override
   public void close() {
      super.close();
      Mekanism.packetHandler().sendToServer(new PacketGuiInteract(PacketGuiInteract.GuiInteraction.CONTAINER_STOP_TRACKING, this.tile, 2));
      ((MekanismContainer)((GuiMekanism)this.gui()).m_6262_()).stopTracking(2);
   }

   private void updateEnabledButtons() {
      this.removeButton.f_93623_ = this.scrollList.hasSelection();
   }

   @Override
   public void renderForeground(GuiGraphics guiGraphics, int mouseX, int mouseY) {
      super.renderForeground(guiGraphics, mouseX, mouseY);
      this.drawTitleText(guiGraphics, MekanismLang.UPGRADES.translate(new Object[0]), 5.0F);
      if (this.scrollList.hasSelection()) {
         Upgrade selectedType = this.scrollList.getSelection();
         int amount = this.tile.getComponent().getUpgrades(selectedType);
         int textY = this.relativeY + 20;
         IFancyFontRenderer.WrappedTextRenderer textRenderer = this.upgradeTypeData
            .computeIfAbsent(selectedType, type -> new IFancyFontRenderer.WrappedTextRenderer(this, MekanismLang.UPGRADE_TYPE.translate(new Object[]{type})));
         int lines = textRenderer.renderWithScale(guiGraphics, this.relativeX + 74, textY, this.screenTextColor(), 56.0F, 0.6F);
         textY += 6 * lines + 2;
         this.drawTextWithScale(
            guiGraphics,
            MekanismLang.UPGRADE_COUNT.translate(new Object[]{amount, selectedType.getMax()}),
            this.relativeX + 74,
            textY,
            this.screenTextColor(),
            0.6F
         );

         for (Component component : UpgradeUtils.getInfo(this.tile, selectedType)) {
            textY += 6;
            this.drawTextWithScale(guiGraphics, component, this.relativeX + 74, textY, this.screenTextColor(), 0.6F);
         }
      } else {
         this.noSelection.renderWithScale(guiGraphics, this.relativeX + 74, this.relativeY + 20, this.screenTextColor(), 56.0F, 0.8F);
      }
   }
}
