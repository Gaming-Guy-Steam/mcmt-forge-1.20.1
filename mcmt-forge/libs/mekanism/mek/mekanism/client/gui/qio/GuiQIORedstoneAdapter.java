package mekanism.client.gui.qio;

import java.util.ArrayList;
import java.util.List;
import mekanism.client.gui.GuiMekanismTile;
import mekanism.client.gui.element.GuiInnerScreen;
import mekanism.client.gui.element.button.MekanismImageButton;
import mekanism.client.gui.element.slot.GuiSlot;
import mekanism.client.gui.element.slot.SlotType;
import mekanism.client.gui.element.tab.GuiQIOFrequencyTab;
import mekanism.client.gui.element.text.GuiTextField;
import mekanism.client.jei.interfaces.IJEIGhostTarget;
import mekanism.common.Mekanism;
import mekanism.common.MekanismLang;
import mekanism.common.inventory.container.tile.MekanismTileContainer;
import mekanism.common.network.to_server.PacketGuiInteract;
import mekanism.common.registries.MekanismSounds;
import mekanism.common.tile.qio.TileEntityQIORedstoneAdapter;
import mekanism.common.util.text.InputValidator;
import mekanism.common.util.text.TextUtils;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.resources.sounds.SimpleSoundInstance;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.item.ItemStack;
import org.jetbrains.annotations.NotNull;

public class GuiQIORedstoneAdapter extends GuiMekanismTile<TileEntityQIORedstoneAdapter, MekanismTileContainer<TileEntityQIORedstoneAdapter>> {
   private GuiTextField text;

   public GuiQIORedstoneAdapter(MekanismTileContainer<TileEntityQIORedstoneAdapter> container, Inventory inv, Component title) {
      super(container, inv, title);
      this.dynamicSlots = true;
      this.f_97727_ += 26;
      this.f_97731_ = this.f_97727_ - 94;
   }

   @Override
   protected void addGuiElements() {
      super.addGuiElements();
      this.addRenderableWidget(new GuiQIOFrequencyTab(this, this.tile));
      this.addRenderableWidget(new GuiSlot(SlotType.NORMAL, this, 7, 30).setRenderHover(true)).click((element, mouseX, mouseY) -> {
         ItemStack stack = this.getCarriedItem();
         if (stack.m_41619_() == m_96638_()) {
            this.updateStack(stack);
            return true;
         } else {
            return false;
         }
      }, MekanismSounds.BEEP).setGhostHandler((IJEIGhostTarget.IGhostItemConsumer)ingredient -> {
         this.updateStack((ItemStack)ingredient);
         this.f_96541_.m_91106_().m_120367_(SimpleSoundInstance.m_119752_(MekanismSounds.BEEP.get(), 1.0F));
      });
      this.addRenderableWidget(
         new MekanismImageButton(
            this,
            9,
            80,
            14,
            this.getButtonLocation("fuzzy"),
            () -> Mekanism.packetHandler().sendToServer(new PacketGuiInteract(PacketGuiInteract.GuiInteraction.QIO_REDSTONE_ADAPTER_FUZZY, this.tile)),
            this.getOnHover(MekanismLang.FUZZY_MODE)
         )
      );
      this.addRenderableWidget(
         new GuiInnerScreen(this, 7, 16, this.f_97726_ - 15, 12, GuiQIOFilterHandler.getFrequencyText(this.tile))
            .tooltip(GuiQIOFilterHandler.getFrequencyTooltip(this.tile))
      );
      this.addRenderableWidget(new GuiInnerScreen(this, 27, 30, this.f_97726_ - 27 - 8, 64, () -> {
         List<Component> list = new ArrayList<>();
         ItemStack itemType = this.tile.getItemType();
         list.add((Component)(itemType.m_41619_() ? MekanismLang.QIO_ITEM_TYPE_UNDEFINED.translate(new Object[0]) : itemType.m_41786_()));
         list.add(MekanismLang.QIO_TRIGGER_COUNT.translate(new Object[]{TextUtils.format(this.tile.getCount())}));
         if (!itemType.m_41619_() && this.tile.getQIOFrequency() != null) {
            list.add(MekanismLang.QIO_STORED_COUNT.translate(new Object[]{TextUtils.format(this.tile.getStoredCount())}));
         }

         list.add(MekanismLang.QIO_FUZZY_MODE.translate(new Object[]{this.tile.getFuzzyMode()}));
         return list;
      }).clearFormat());
      this.text = this.addRenderableWidget(new GuiTextField(this, 29, 80, this.f_97726_ - 39, 12));
      this.text.setInputValidator(InputValidator.DIGIT).configureDigitalInput(this::setCount).setMaxLength(10);
      this.text.m_93692_(true);
   }

   private void updateStack(ItemStack stack) {
      Mekanism.packetHandler()
         .sendToServer(new PacketGuiInteract(PacketGuiInteract.GuiInteractionItem.QIO_REDSTONE_ADAPTER_STACK, this.tile, stack.m_255036_(1)));
   }

   private void setCount() {
      if (!this.text.getText().isEmpty()) {
         long count = Long.parseLong(this.text.getText());
         Mekanism.packetHandler()
            .sendToServer(new PacketGuiInteract(PacketGuiInteract.GuiInteraction.QIO_REDSTONE_ADAPTER_COUNT, this.tile, (int)Math.min(count, 2147483647L)));
         this.text.setText("");
      }
   }

   @Override
   protected void drawForegroundText(@NotNull GuiGraphics guiGraphics, int mouseX, int mouseY) {
      this.renderTitleText(guiGraphics);
      this.drawString(guiGraphics, this.f_169604_, this.f_97730_, this.f_97731_, this.titleTextColor());
      this.renderItem(guiGraphics, this.tile.getItemType(), 8, 31);
      super.drawForegroundText(guiGraphics, mouseX, mouseY);
   }
}
