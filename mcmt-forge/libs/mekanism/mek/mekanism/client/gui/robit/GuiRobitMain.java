package mekanism.client.gui.robit;

import java.util.function.Supplier;
import mekanism.client.SpecialColors;
import mekanism.client.gui.GuiMekanism;
import mekanism.client.gui.element.GuiElement;
import mekanism.client.gui.element.GuiInnerScreen;
import mekanism.client.gui.element.GuiSideHolder;
import mekanism.client.gui.element.bar.GuiHorizontalPowerBar;
import mekanism.client.gui.element.button.MekanismImageButton;
import mekanism.client.gui.element.tab.GuiSecurityTab;
import mekanism.client.gui.element.window.GuiRobitRename;
import mekanism.client.gui.element.window.GuiRobitSkinSelect;
import mekanism.client.gui.element.window.GuiWindow;
import mekanism.common.Mekanism;
import mekanism.common.MekanismLang;
import mekanism.common.entity.EntityRobit;
import mekanism.common.inventory.container.entity.robit.MainRobitContainer;
import mekanism.common.network.to_server.PacketGuiButtonPress;
import mekanism.common.network.to_server.PacketRobit;
import mekanism.common.util.text.EnergyDisplay;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Inventory;
import org.jetbrains.annotations.NotNull;

public class GuiRobitMain extends GuiMekanism<MainRobitContainer> {
   private final EntityRobit robit;
   private MekanismImageButton renameButton;
   private MekanismImageButton skinButton;

   public GuiRobitMain(MainRobitContainer container, Inventory inv, Component title) {
      super(container, inv, title);
      this.robit = container.getEntity();
      this.dynamicSlots = true;
      this.f_97728_ = 76;
   }

   private void openWindow(GuiWindow window, Supplier<? extends GuiElement> elementSupplier) {
      window.setListenerTab(elementSupplier);
      elementSupplier.get().f_93623_ = false;
      this.addWindow(window);
   }

   @Override
   protected void addGuiElements() {
      super.addGuiElements();
      this.addRenderableWidget(new GuiSecurityTab(this, this.robit, 120));
      this.addRenderableWidget(GuiSideHolder.create(this, this.getWidth(), 6, 106, false, false, SpecialColors.TAB_ROBIT_MENU));
      this.addRenderableWidget(new GuiInnerScreen(this, 27, 16, 122, 56));
      this.addRenderableWidget(new GuiHorizontalPowerBar(this, this.robit.getEnergyContainer(), 27, 74, 120));
      this.addRenderableWidget(new MekanismImageButton(this, 6, 16, 18, this.getButtonLocation("home"), () -> {
         Mekanism.packetHandler().sendToServer(new PacketRobit(PacketRobit.RobitPacketType.GO_HOME, this.robit));
         this.getMinecraft().m_91152_(null);
      }, this.getOnHover(MekanismLang.ROBIT_TELEPORT)));
      this.renameButton = this.addRenderableWidget(
         new MekanismImageButton(
            this,
            6,
            35,
            18,
            this.getButtonLocation("rename"),
            () -> this.openWindow(new GuiRobitRename(this, 27, 16, this.robit), () -> this.renameButton),
            this.getOnHover(MekanismLang.ROBIT_RENAME)
         )
      );
      this.skinButton = this.addRenderableWidget(
         new MekanismImageButton(
            this,
            6,
            54,
            18,
            this.getButtonLocation("skin"),
            () -> this.openWindow(new GuiRobitSkinSelect(this, 4, -12, this.robit), () -> this.skinButton),
            this.getOnHover(MekanismLang.ROBIT_SKIN_SELECT)
         )
      );
      this.addRenderableWidget(
         new MekanismImageButton(
            this,
            152,
            35,
            18,
            this.getButtonLocation("drop"),
            () -> Mekanism.packetHandler().sendToServer(new PacketRobit(PacketRobit.RobitPacketType.DROP_PICKUP, this.robit)),
            this.getOnHover(MekanismLang.ROBIT_TOGGLE_PICKUP)
         )
      );
      this.addRenderableWidget(
         new MekanismImageButton(
            this,
            152,
            54,
            18,
            this.getButtonLocation("follow"),
            () -> Mekanism.packetHandler().sendToServer(new PacketRobit(PacketRobit.RobitPacketType.FOLLOW, this.robit)),
            this.getOnHover(MekanismLang.ROBIT_TOGGLE_FOLLOW)
         )
      );
      this.addRenderableWidget(
         new MekanismImageButton(this, this.getWidth() + 3, 10, 18, this.getButtonLocation("main"), () -> {}, this.getOnHover(MekanismLang.ROBIT))
      );
      this.addRenderableWidget(
         new MekanismImageButton(
            this,
            this.getWidth() + 3,
            30,
            18,
            this.getButtonLocation("crafting"),
            () -> Mekanism.packetHandler().sendToServer(new PacketGuiButtonPress(PacketGuiButtonPress.ClickedEntityButton.ROBIT_CRAFTING, this.robit)),
            this.getOnHover(MekanismLang.ROBIT_CRAFTING)
         )
      );
      this.addRenderableWidget(
         new MekanismImageButton(
            this,
            this.getWidth() + 3,
            50,
            18,
            this.getButtonLocation("inventory"),
            () -> Mekanism.packetHandler().sendToServer(new PacketGuiButtonPress(PacketGuiButtonPress.ClickedEntityButton.ROBIT_INVENTORY, this.robit)),
            this.getOnHover(MekanismLang.ROBIT_INVENTORY)
         )
      );
      this.addRenderableWidget(
         new MekanismImageButton(
            this,
            this.getWidth() + 3,
            70,
            18,
            this.getButtonLocation("smelting"),
            () -> Mekanism.packetHandler().sendToServer(new PacketGuiButtonPress(PacketGuiButtonPress.ClickedEntityButton.ROBIT_SMELTING, this.robit)),
            this.getOnHover(MekanismLang.ROBIT_SMELTING)
         )
      );
      this.addRenderableWidget(
         new MekanismImageButton(
            this,
            this.getWidth() + 3,
            90,
            18,
            this.getButtonLocation("repair"),
            () -> Mekanism.packetHandler().sendToServer(new PacketGuiButtonPress(PacketGuiButtonPress.ClickedEntityButton.ROBIT_REPAIR, this.robit)),
            this.getOnHover(MekanismLang.ROBIT_REPAIR)
         )
      );
   }

   @Override
   protected void drawForegroundText(@NotNull GuiGraphics guiGraphics, int mouseX, int mouseY) {
      this.drawString(guiGraphics, this.f_96539_, this.f_97728_, this.f_97729_, this.titleTextColor());
      this.drawTextScaledBound(
         guiGraphics, MekanismLang.ROBIT_GREETING.translate(new Object[]{this.robit.m_7755_()}), 29.0F, 18.0F, this.screenTextColor(), 119.0F
      );
      this.drawTextScaledBound(
         guiGraphics,
         MekanismLang.ENERGY.translate(new Object[]{EnergyDisplay.of(this.robit.getEnergyContainer())}),
         29.0F,
         32.0F,
         this.screenTextColor(),
         119.0F
      );
      this.drawTextScaledBound(
         guiGraphics, MekanismLang.ROBIT_FOLLOWING.translate(new Object[]{this.robit.getFollowing()}), 29.0F, 41.0F, this.screenTextColor(), 119.0F
      );
      this.drawTextScaledBound(
         guiGraphics, MekanismLang.ROBIT_DROP_PICKUP.translate(new Object[]{this.robit.getDropPickup()}), 29.0F, 50.0F, this.screenTextColor(), 119.0F
      );
      CharSequence owner = (CharSequence)(this.robit.getOwnerName().length() > 14 ? this.robit.getOwnerName().subSequence(0, 14) : this.robit.getOwnerName());
      this.drawTextScaledBound(guiGraphics, MekanismLang.ROBIT_OWNER.translate(new Object[]{owner}), 29.0F, 59.0F, this.screenTextColor(), 119.0F);
      super.drawForegroundText(guiGraphics, mouseX, mouseY);
   }
}
