package mekanism.client.gui.robit;

import mekanism.client.SpecialColors;
import mekanism.client.gui.GuiMekanism;
import mekanism.client.gui.element.GuiSideHolder;
import mekanism.client.gui.element.button.MekanismImageButton;
import mekanism.client.gui.element.tab.GuiSecurityTab;
import mekanism.common.Mekanism;
import mekanism.common.MekanismLang;
import mekanism.common.entity.EntityRobit;
import mekanism.common.inventory.container.entity.IEntityContainer;
import mekanism.common.network.to_server.PacketGuiButtonPress;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.inventory.AbstractContainerMenu;

public abstract class GuiRobit<CONTAINER extends AbstractContainerMenu & IEntityContainer<EntityRobit>> extends GuiMekanism<CONTAINER> {
   protected final EntityRobit robit;

   protected GuiRobit(CONTAINER container, Inventory inv, Component title) {
      super(container, inv, title);
      this.robit = (EntityRobit)container.getEntity();
   }

   @Override
   protected void addGuiElements() {
      super.addGuiElements();
      this.addRenderableWidget(new GuiSecurityTab(this, this.robit, 120));
      this.addRenderableWidget(GuiSideHolder.create(this, this.getWidth(), 6, 106, false, false, SpecialColors.TAB_ROBIT_MENU));
      this.addRenderableWidget(
         new MekanismImageButton(
            this,
            this.getWidth() + 3,
            10,
            18,
            this.getButtonLocation("main"),
            () -> Mekanism.packetHandler().sendToServer(new PacketGuiButtonPress(PacketGuiButtonPress.ClickedEntityButton.ROBIT_MAIN, this.robit)),
            this.getOnHover(MekanismLang.ROBIT)
         )
      );
      this.addRenderableWidget(new MekanismImageButton(this, this.getWidth() + 3, 30, 18, this.getButtonLocation("crafting"), () -> {
         if (this.shouldOpenGui(GuiRobit.RobitGuiType.CRAFTING)) {
            Mekanism.packetHandler().sendToServer(new PacketGuiButtonPress(PacketGuiButtonPress.ClickedEntityButton.ROBIT_CRAFTING, this.robit));
         }
      }, this.getOnHover(MekanismLang.ROBIT_CRAFTING)));
      this.addRenderableWidget(new MekanismImageButton(this, this.getWidth() + 3, 50, 18, this.getButtonLocation("inventory"), () -> {
         if (this.shouldOpenGui(GuiRobit.RobitGuiType.INVENTORY)) {
            Mekanism.packetHandler().sendToServer(new PacketGuiButtonPress(PacketGuiButtonPress.ClickedEntityButton.ROBIT_INVENTORY, this.robit));
         }
      }, this.getOnHover(MekanismLang.ROBIT_INVENTORY)));
      this.addRenderableWidget(new MekanismImageButton(this, this.getWidth() + 3, 70, 18, this.getButtonLocation("smelting"), () -> {
         if (this.shouldOpenGui(GuiRobit.RobitGuiType.SMELTING)) {
            Mekanism.packetHandler().sendToServer(new PacketGuiButtonPress(PacketGuiButtonPress.ClickedEntityButton.ROBIT_SMELTING, this.robit));
         }
      }, this.getOnHover(MekanismLang.ROBIT_SMELTING)));
      this.addRenderableWidget(new MekanismImageButton(this, this.getWidth() + 3, 90, 18, this.getButtonLocation("repair"), () -> {
         if (this.shouldOpenGui(GuiRobit.RobitGuiType.REPAIR)) {
            Mekanism.packetHandler().sendToServer(new PacketGuiButtonPress(PacketGuiButtonPress.ClickedEntityButton.ROBIT_REPAIR, this.robit));
         }
      }, this.getOnHover(MekanismLang.ROBIT_REPAIR)));
   }

   protected abstract boolean shouldOpenGui(GuiRobit.RobitGuiType guiType);

   public static enum RobitGuiType {
      CRAFTING,
      INVENTORY,
      SMELTING,
      REPAIR;
   }
}
