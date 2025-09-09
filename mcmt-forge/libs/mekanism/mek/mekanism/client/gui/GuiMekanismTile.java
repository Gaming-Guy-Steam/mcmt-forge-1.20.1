package mekanism.client.gui;

import java.util.Set;
import mekanism.api.inventory.IInventorySlot;
import mekanism.api.text.EnumColor;
import mekanism.client.gui.element.tab.GuiRedstoneControlTab;
import mekanism.client.gui.element.tab.GuiSecurityTab;
import mekanism.client.gui.element.tab.window.GuiUpgradeWindowTab;
import mekanism.common.MekanismLang;
import mekanism.common.capabilities.Capabilities;
import mekanism.common.inventory.container.slot.InventoryContainerSlot;
import mekanism.common.inventory.container.tile.MekanismTileContainer;
import mekanism.common.item.ItemConfigurator;
import mekanism.common.lib.transmitter.TransmissionType;
import mekanism.common.tile.base.TileEntityMekanism;
import mekanism.common.tile.component.config.ConfigInfo;
import mekanism.common.tile.component.config.DataType;
import mekanism.common.tile.component.config.slot.InventorySlotInfo;
import mekanism.common.tile.interfaces.ISideConfiguration;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public abstract class GuiMekanismTile<TILE extends TileEntityMekanism, CONTAINER extends MekanismTileContainer<TILE>> extends GuiMekanism<CONTAINER> {
   protected final TILE tile;
   @Nullable
   private GuiUpgradeWindowTab upgradeWindowTab;

   protected GuiMekanismTile(CONTAINER container, Inventory inv, Component title) {
      super(container, inv, title);
      this.tile = container.getTileEntity();
   }

   public TILE getTileEntity() {
      return this.tile;
   }

   @Override
   protected void addGuiElements() {
      super.addGuiElements();
      this.addGenericTabs();
   }

   protected void addGenericTabs() {
      if (this.tile.supportsUpgrades()) {
         this.upgradeWindowTab = this.addRenderableWidget(new GuiUpgradeWindowTab(this, this.tile, () -> this.upgradeWindowTab));
      }

      if (this.tile.supportsRedstone()) {
         this.addRenderableWidget(new GuiRedstoneControlTab(this, this.tile));
      }

      if (this.tile.getCapability(Capabilities.SECURITY_OBJECT).isPresent()) {
         this.addSecurityTab();
      }
   }

   protected void addSecurityTab() {
      this.addRenderableWidget(new GuiSecurityTab(this, this.tile));
   }

   protected void m_280072_(@NotNull GuiGraphics guiGraphics, int mouseX, int mouseY) {
      super.m_280072_(guiGraphics, mouseX, mouseY);
      if (this.tile instanceof ISideConfiguration) {
         ItemStack stack = this.getCarriedItem();
         if (!stack.m_41619_() && stack.m_41720_() instanceof ItemConfigurator) {
            Slot slot = this.getSlotUnderMouse();
            if (slot != null) {
               DataType data = this.getFromSlot(slot);
               if (data != null) {
                  EnumColor color = data.getColor();
                  this.displayTooltips(
                     guiGraphics,
                     mouseX,
                     mouseY,
                     new Component[]{MekanismLang.GENERIC_WITH_PARENTHESIS.translateColored(color, new Object[]{data, color.getName()})}
                  );
               }
            }
         }
      }
   }

   private DataType getFromSlot(Slot slot) {
      if (slot.f_40219_ < this.tile.getSlots() && slot instanceof InventoryContainerSlot containerSlot) {
         ISideConfiguration config = (ISideConfiguration)this.tile;
         ConfigInfo info = config.getConfig().getConfig(TransmissionType.ITEM);
         if (info != null) {
            Set<DataType> supportedDataTypes = info.getSupportedDataTypes();
            IInventorySlot inventorySlot = containerSlot.getInventorySlot();

            for (DataType type : supportedDataTypes) {
               if (info.getSlotInfo(type) instanceof InventorySlotInfo inventorySlotInfo && inventorySlotInfo.hasSlot(inventorySlot)) {
                  return type;
               }
            }
         }
      }

      return null;
   }
}
