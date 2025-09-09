package mekanism.client.gui;

import com.mojang.blaze3d.platform.InputConstants.Key;
import com.mojang.blaze3d.platform.InputConstants.Type;
import mekanism.common.inventory.container.slot.IVirtualSlot;
import net.minecraft.ChatFormatting;
import net.minecraft.Util;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.ClickType;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public abstract class VirtualSlotContainerScreen<T extends AbstractContainerMenu> extends AbstractContainerScreen<T> {
   public VirtualSlotContainerScreen(T container, Inventory inv, Component titleIn) {
      super(container, inv, titleIn);
   }

   protected abstract boolean isMouseOverSlot(@NotNull Slot slot, double mouseX, double mouseY);

   @Deprecated
   @Nullable
   protected Slot m_97744_(double mouseX, double mouseY) {
      for (Slot slot : this.f_97732_.f_38839_) {
         if (slot.m_6659_() && this.isMouseOverSlot(slot, mouseX, mouseY)) {
            return slot;
         }
      }

      return null;
   }

   @Deprecated
   protected final boolean m_97774_(@NotNull Slot slot, double mouseX, double mouseY) {
      boolean mouseOver = this.isMouseOverSlot(slot, mouseX, mouseY);
      if (mouseOver && slot instanceof IVirtualSlot) {
         if (this.f_97734_ == null && slot.m_6659_()) {
            this.f_97734_ = slot;
         }

         return false;
      } else {
         return mouseOver;
      }
   }

   @Deprecated
   protected final void m_280211_(GuiGraphics guiGraphics, @NotNull ItemStack stack, int x, int y, @Nullable String altText) {
      if (!stack.m_41619_()) {
         if (stack == this.f_97715_ && this.f_97707_ instanceof IVirtualSlot returningVirtualSlot) {
            float f = (float)(Util.m_137550_() - this.f_97714_) / 100.0F;
            if (f >= 1.0F) {
               this.f_97715_ = ItemStack.f_41583_;
               return;
            }

            int xOffset = returningVirtualSlot.getActualX() - this.f_97712_;
            int yOffset = returningVirtualSlot.getActualY() - this.f_97713_;
            x = this.f_97712_ + (int)(xOffset * f);
            y = this.f_97713_ + (int)(yOffset * f);
         }

         super.m_280211_(guiGraphics, stack, x, y, altText);
      }
   }

   @Deprecated
   protected final void m_280092_(@NotNull GuiGraphics graphics, @NotNull Slot slot) {
      if (!(slot instanceof IVirtualSlot virtualSlot)) {
         super.m_280092_(graphics, slot);
      } else {
         ItemStack currentStack = slot.m_7993_();
         boolean shouldDrawOverlay = false;
         boolean skipStackRendering = slot == this.f_97706_ && !this.f_97711_.m_41619_() && !this.f_97710_;
         ItemStack heldStack = this.f_97732_.m_142621_();
         String s = null;
         if (slot == this.f_97706_ && !this.f_97711_.m_41619_() && this.f_97710_ && !currentStack.m_41619_()) {
            currentStack = currentStack.m_255036_(currentStack.m_41613_() / 2);
         } else if (this.f_97738_ && this.f_97737_.contains(slot) && !heldStack.m_41619_()) {
            if (this.f_97737_.size() == 1) {
               return;
            }

            if (AbstractContainerMenu.m_38899_(slot, heldStack, true) && this.f_97732_.m_5622_(slot)) {
               int max = Math.min(heldStack.m_41741_(), slot.m_5866_(heldStack));
               int placed = AbstractContainerMenu.m_278794_(this.f_97737_, this.f_97717_, heldStack) + currentStack.m_41613_();
               if (placed > max) {
                  placed = max;
                  s = ChatFormatting.YELLOW.toString() + max;
               }

               currentStack = heldStack.m_255036_(placed);
               shouldDrawOverlay = true;
            } else {
               this.f_97737_.remove(slot);
               this.m_97818_();
            }
         }

         virtualSlot.updateRenderInfo(skipStackRendering ? ItemStack.f_41583_ : currentStack, shouldDrawOverlay, s);
      }
   }

   public boolean slotClicked(@NotNull Slot slot, int button) {
      Key mouseKey = Type.MOUSE.m_84895_(button);
      boolean pickBlockButton = this.f_96541_.f_91066_.f_92097_.isActiveAndMatches(mouseKey);
      long time = Util.m_137550_();
      this.f_97723_ = this.f_97709_ == slot && time - this.f_97721_ < 250L && this.f_97722_ == button;
      this.f_97719_ = false;
      if (button != 0 && button != 1 && !pickBlockButton) {
         this.m_97762_(button);
      } else if (slot.f_40219_ != -1) {
         if ((Boolean)this.f_96541_.f_91066_.m_231828_().m_231551_()) {
            if (slot.m_6657_()) {
               this.f_97706_ = slot;
               this.f_97711_ = ItemStack.f_41583_;
               this.f_97710_ = button == 1;
            } else {
               this.f_97706_ = null;
            }
         } else if (!this.f_97738_) {
            if (this.f_97732_.m_142621_().m_41619_()) {
               if (pickBlockButton) {
                  this.m_6597_(slot, slot.f_40219_, button, ClickType.CLONE);
               } else {
                  ClickType clicktype = ClickType.PICKUP;
                  if (Screen.m_96638_()) {
                     this.f_97724_ = slot.m_6657_() ? slot.m_7993_().m_41777_() : ItemStack.f_41583_;
                     clicktype = ClickType.QUICK_MOVE;
                  }

                  this.m_6597_(slot, slot.f_40219_, button, clicktype);
               }

               this.f_97719_ = true;
            } else {
               this.f_97738_ = true;
               this.f_97718_ = button;
               this.f_97737_.clear();
               if (button == 0) {
                  this.f_97717_ = 0;
               } else if (button == 1) {
                  this.f_97717_ = 1;
               } else if (pickBlockButton) {
                  this.f_97717_ = 2;
               }
            }
         }
      }

      this.f_97709_ = slot;
      this.f_97721_ = time;
      this.f_97722_ = button;
      return true;
   }
}
