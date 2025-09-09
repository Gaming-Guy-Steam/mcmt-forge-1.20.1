package mekanism.client.gui.robit;

import mekanism.client.gui.element.text.BackgroundType;
import mekanism.client.gui.element.text.GuiTextField;
import mekanism.common.MekanismLang;
import mekanism.common.inventory.container.entity.robit.RepairRobitContainer;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.network.chat.Component;
import net.minecraft.network.protocol.game.ServerboundRenameItemPacket;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.ContainerListener;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;
import org.jetbrains.annotations.NotNull;

public class GuiRobitRepair extends GuiRobit<RepairRobitContainer> implements ContainerListener {
   private static final ResourceLocation ANVIL_RESOURCE = new ResourceLocation("textures/gui/container/anvil.png");
   private final Player player;
   private GuiTextField itemNameField;

   public GuiRobitRepair(RepairRobitContainer container, Inventory inv, Component title) {
      super(container, inv, title);
      this.player = inv.f_35978_;
      this.f_97731_++;
      this.f_97728_ = 60;
   }

   @Override
   protected void addGuiElements() {
      super.addGuiElements();
      this.itemNameField = this.addRenderableWidget(new GuiTextField(this, 60, 21, 103, 12));
      this.itemNameField.setCanLoseFocus(false);
      this.itemNameField.setTextColor(-1);
      this.itemNameField.setTextColorUneditable(-1);
      this.itemNameField.setBackground(BackgroundType.NONE);
      this.itemNameField.setMaxLength(50);
      this.itemNameField.setResponder(this::onNameChanged);
      this.m_264313_(this.itemNameField);
      this.itemNameField.setEditable(false);
      ((RepairRobitContainer)this.f_97732_).m_38943_(this);
      ((RepairRobitContainer)this.f_97732_).m_38893_(this);
   }

   private void onNameChanged(String newText) {
      if (!newText.isEmpty()) {
         Slot slot = ((RepairRobitContainer)this.f_97732_).m_38853_(0);
         if (slot.m_6657_() && !slot.m_7993_().m_41788_() && newText.equals(slot.m_7993_().m_41786_().getString())) {
            newText = "";
         }

         ((RepairRobitContainer)this.f_97732_).m_39020_(newText);
         this.getMinecraft().f_91074_.f_108617_.m_104955_(new ServerboundRenameItemPacket(newText));
      }
   }

   @Override
   public void m_7861_() {
      super.m_7861_();
      ((RepairRobitContainer)this.f_97732_).m_38943_(this);
   }

   @Override
   protected void drawForegroundText(@NotNull GuiGraphics guiGraphics, int mouseX, int mouseY) {
      this.drawString(guiGraphics, this.f_96539_, this.f_97728_, this.f_97729_, this.titleTextColor());
      int maximumCost = ((RepairRobitContainer)this.f_97732_).m_39028_();
      if (maximumCost > 0) {
         int k = 8453920;
         boolean flag = true;
         Component component = MekanismLang.REPAIR_COST.translate(new Object[]{maximumCost});
         if (maximumCost >= 40 && !this.getMinecraft().f_91074_.m_7500_()) {
            component = MekanismLang.REPAIR_EXPENSIVE.translate(new Object[0]);
            k = 16736352;
         } else {
            Slot slot = ((RepairRobitContainer)this.f_97732_).m_38853_(2);
            if (!slot.m_6657_()) {
               flag = false;
            } else if (!slot.m_8010_(this.player)) {
               k = 16736352;
            }
         }

         if (flag) {
            int width = this.f_97726_ - 8 - this.getStringWidth(component) - 2;
            guiGraphics.m_280509_(width - 2, 67, this.f_97726_ - 8, 79, 1325400064);
            guiGraphics.m_280430_(this.getFont(), component, width, 69, k);
         }
      }

      this.drawString(guiGraphics, this.f_169604_, this.f_97730_, this.f_97731_, this.titleTextColor());
      super.drawForegroundText(guiGraphics, mouseX, mouseY);
   }

   @Override
   protected boolean shouldOpenGui(GuiRobit.RobitGuiType guiType) {
      return guiType != GuiRobit.RobitGuiType.REPAIR;
   }

   @Override
   protected void m_7286_(@NotNull GuiGraphics guiGraphics, float partialTick, int mouseX, int mouseY) {
      guiGraphics.m_280218_(ANVIL_RESOURCE, this.f_97735_, this.f_97736_, 0, 0, this.f_97726_, this.f_97727_);
      guiGraphics.m_280218_(
         ANVIL_RESOURCE,
         this.f_97735_ + 59,
         this.f_97736_ + 20,
         0,
         this.f_97727_ + (((RepairRobitContainer)this.f_97732_).m_38853_(0).m_6657_() ? 0 : 16),
         110,
         16
      );
      if ((((RepairRobitContainer)this.f_97732_).m_38853_(0).m_6657_() || ((RepairRobitContainer)this.f_97732_).m_38853_(1).m_6657_())
         && !((RepairRobitContainer)this.f_97732_).m_38853_(2).m_6657_()) {
         guiGraphics.m_280218_(ANVIL_RESOURCE, this.f_97735_ + 99, this.f_97736_ + 45, this.f_97726_, 0, 28, 21);
      }
   }

   public void m_7934_(@NotNull AbstractContainerMenu container, int slotID, @NotNull ItemStack stack) {
      if (slotID == 0) {
         this.itemNameField.setText(stack.m_41619_() ? "" : stack.m_41786_().getString());
         this.itemNameField.setEditable(!stack.m_41619_());
         this.m_7522_(this.itemNameField);
      }
   }

   public void m_142153_(@NotNull AbstractContainerMenu container, int slotID, int value) {
   }
}
