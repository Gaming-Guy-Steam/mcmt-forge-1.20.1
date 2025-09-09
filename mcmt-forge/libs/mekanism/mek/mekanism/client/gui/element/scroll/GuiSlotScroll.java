package mekanism.client.gui.element.scroll;

import com.mojang.blaze3d.vertex.PoseStack;
import java.math.RoundingMode;
import java.text.DecimalFormat;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.function.Supplier;
import mekanism.api.text.EnumColor;
import mekanism.client.gui.IGuiWrapper;
import mekanism.client.gui.element.GuiElement;
import mekanism.client.jei.interfaces.IJEIIngredientHelper;
import mekanism.common.MekanismLang;
import mekanism.common.inventory.ISlotClickHandler;
import mekanism.common.lib.inventory.HashedItem;
import mekanism.common.util.MekanismUtils;
import mekanism.common.util.text.TextUtils;
import net.minecraft.Util;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.renderer.Rect2i;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
import org.jetbrains.annotations.NotNull;

public class GuiSlotScroll extends GuiElement implements IJEIIngredientHelper {
   private static final ResourceLocation SLOTS = MekanismUtils.getResource(MekanismUtils.ResourceType.GUI_SLOT, "slots.png");
   private static final ResourceLocation SLOTS_DARK = MekanismUtils.getResource(MekanismUtils.ResourceType.GUI_SLOT, "slots_dark.png");
   private static final DecimalFormat COUNT_FORMAT = (DecimalFormat)Util.m_137469_(
      new DecimalFormat("#.#"), format -> format.setRoundingMode(RoundingMode.FLOOR)
   );
   private final GuiScrollBar scrollBar;
   private final int xSlots;
   private final int ySlots;
   private final Supplier<List<ISlotClickHandler.IScrollableSlot>> slotList;
   private final ISlotClickHandler clickHandler;

   public GuiSlotScroll(
      IGuiWrapper gui, int x, int y, int xSlots, int ySlots, Supplier<List<ISlotClickHandler.IScrollableSlot>> slotList, ISlotClickHandler clickHandler
   ) {
      super(gui, x, y, xSlots * 18 + 18, ySlots * 18);
      this.xSlots = xSlots;
      this.ySlots = ySlots;
      this.slotList = slotList;
      this.clickHandler = clickHandler;
      this.scrollBar = this.addChild(
         new GuiScrollBar(
            gui,
            this.relativeX + xSlots * 18 + 4,
            y,
            ySlots * 18,
            () -> this.getSlotList() == null ? 0 : (int)Math.ceil((double)this.getSlotList().size() / xSlots),
            () -> ySlots
         )
      );
   }

   @Override
   public void drawBackground(@NotNull GuiGraphics guiGraphics, int mouseX, int mouseY, float partialTicks) {
      super.drawBackground(guiGraphics, mouseX, mouseY, partialTicks);
      List<ISlotClickHandler.IScrollableSlot> list = this.getSlotList();
      ResourceLocation resource = list == null ? SLOTS_DARK : SLOTS;
      guiGraphics.m_280163_(resource, this.relativeX, this.relativeY, 0.0F, 0.0F, this.xSlots * 18, this.ySlots * 18, 288, 288);
      if (list != null) {
         int slotStart = this.scrollBar.getCurrentSelection() * this.xSlots;
         int max = this.xSlots * this.ySlots;

         for (int i = 0; i < max; i++) {
            int slot = slotStart + i;
            if (slot >= list.size()) {
               break;
            }

            int slotX = this.relativeX + i % this.xSlots * 18;
            int slotY = this.relativeY + i / this.xSlots * 18;
            this.renderSlot(guiGraphics, list.get(slot), slotX, slotY);
         }
      }
   }

   @Override
   public void renderForeground(GuiGraphics guiGraphics, int mouseX, int mouseY) {
      super.renderForeground(guiGraphics, mouseX, mouseY);
      int xAxis = mouseX - this.getGuiLeft();
      int yAxis = mouseY - this.getGuiTop();
      int slotX = (xAxis - this.relativeX) / 18;
      int slotY = (yAxis - this.relativeY) / 18;
      if (slotX >= 0 && slotY >= 0 && slotX < this.xSlots && slotY < this.ySlots) {
         int slotStartX = this.relativeX + slotX * 18 + 1;
         int slotStartY = this.relativeY + slotY * 18 + 1;
         if (xAxis >= slotStartX && xAxis < slotStartX + 16 && yAxis >= slotStartY && yAxis < slotStartY + 16 && this.checkWindows(mouseX, mouseY)) {
            guiGraphics.m_285944_(RenderType.m_286086_(), slotStartX, slotStartY, slotStartX + 16, slotStartY + 16, -2130706433);
         }
      }
   }

   @Override
   public void renderToolTip(@NotNull GuiGraphics guiGraphics, int mouseX, int mouseY) {
      super.renderToolTip(guiGraphics, mouseX, mouseY);
      ISlotClickHandler.IScrollableSlot slot = this.getSlot(mouseX, mouseY);
      if (slot != null) {
         this.renderSlotTooltip(guiGraphics, slot, mouseX, mouseY);
      }
   }

   @Override
   public boolean m_6050_(double mouseX, double mouseY, double delta) {
      return this.scrollBar.adjustScroll(delta) || super.m_6050_(mouseX, mouseY, delta);
   }

   public boolean m_6348_(double mouseX, double mouseY, int button) {
      if (this.gui().currentlyQuickCrafting()) {
         return super.m_6348_(mouseX, mouseY, button);
      } else {
         super.m_6348_(mouseX, mouseY, button);
         this.clickHandler.onClick(() -> this.getSlot(mouseX, mouseY), button, Screen.m_96638_(), this.gui().getCarriedItem());
         return true;
      }
   }

   private ISlotClickHandler.IScrollableSlot getSlot(double mouseX, double mouseY) {
      List<ISlotClickHandler.IScrollableSlot> list = this.getSlotList();
      if (list == null) {
         return null;
      } else {
         int slotX = (int)((mouseX - this.m_252754_()) / 18.0);
         int slotY = (int)((mouseY - this.m_252907_()) / 18.0);
         int slotStartX = this.m_252754_() + slotX * 18 + 1;
         int slotStartY = this.m_252907_() + slotY * 18 + 1;
         if (mouseX < slotStartX || mouseX >= slotStartX + 16 || mouseY < slotStartY || mouseY >= slotStartY + 16) {
            return null;
         } else if (slotX >= 0 && slotY >= 0 && slotX < this.xSlots && slotY < this.ySlots) {
            int slot = (slotY + this.scrollBar.getCurrentSelection()) * this.xSlots + slotX;
            return slot >= list.size() ? null : list.get(slot);
         } else {
            return null;
         }
      }
   }

   private void renderSlot(GuiGraphics guiGraphics, ISlotClickHandler.IScrollableSlot slot, int slotX, int slotY) {
      if (!this.isSlotEmpty(slot)) {
         this.gui().renderItemWithOverlay(guiGraphics, slot.item().getInternalStack(), slotX + 1, slotY + 1, 1.0F, "");
         if (slot.count() > 1L) {
            this.renderSlotText(guiGraphics, this.getCountText(slot.count()), slotX + 1, slotY + 1);
         }
      }
   }

   private void renderSlotTooltip(GuiGraphics guiGraphics, ISlotClickHandler.IScrollableSlot slot, int slotX, int slotY) {
      if (!this.isSlotEmpty(slot)) {
         ItemStack stack = slot.item().getInternalStack();
         long count = slot.count();
         if (count < 10000L) {
            this.gui().renderItemTooltip(guiGraphics, stack, slotX, slotY);
         } else {
            this.gui()
               .renderItemTooltipWithExtra(
                  guiGraphics,
                  stack,
                  slotX,
                  slotY,
                  Collections.singletonList(
                     MekanismLang.QIO_STORED_COUNT.translateColored(EnumColor.GRAY, new Object[]{EnumColor.INDIGO, TextUtils.format(count)})
                  )
               );
         }
      }
   }

   private boolean isSlotEmpty(ISlotClickHandler.IScrollableSlot slot) {
      if (slot.count() == 0L) {
         return true;
      } else {
         HashedItem item = slot.item();
         return item == null || item.getInternalStack().m_41619_();
      }
   }

   private void renderSlotText(GuiGraphics guiGraphics, String text, int x, int y) {
      PoseStack pose = guiGraphics.m_280168_();
      pose.m_85836_();
      float scale = 0.6F;
      int width = this.getFont().m_92895_(text);
      scale = Math.min(1.0F, 16.0F / (width * scale)) * scale;
      float yAdd = 4.0F - scale * 8.0F / 2.0F;
      pose.m_252880_(x + 16 - width * scale, y + 9 + yAdd, 200.0F);
      pose.m_85841_(scale, scale, scale);
      guiGraphics.m_280488_(this.getFont(), text, 0, 0, 16777215);
      pose.m_85849_();
   }

   private String getCountText(long count) {
      if (count <= 1L) {
         return null;
      } else if (count < 10000L) {
         return Long.toString(count);
      } else if (count < 10000000L) {
         return COUNT_FORMAT.format(count / 1000.0) + "K";
      } else if (count < 10000000000L) {
         return COUNT_FORMAT.format(count / 1000000.0) + "M";
      } else {
         return count < 10000000000000L ? COUNT_FORMAT.format(count / 1.0E9) + "B" : ">10T";
      }
   }

   private List<ISlotClickHandler.IScrollableSlot> getSlotList() {
      return this.slotList.get();
   }

   @Override
   public Optional<?> getIngredient(double mouseX, double mouseY) {
      ISlotClickHandler.IScrollableSlot slot = this.getSlot(mouseX, mouseY);
      return slot == null ? Optional.empty() : Optional.of(slot.item().getInternalStack());
   }

   @Override
   public Rect2i getIngredientBounds(double mouseX, double mouseY) {
      List<ISlotClickHandler.IScrollableSlot> list = this.getSlotList();
      if (list != null) {
         int slotX = (int)((mouseX - this.m_252754_()) / 18.0);
         int slotY = (int)((mouseY - this.m_252907_()) / 18.0);
         int slotStartX = this.m_252754_() + slotX * 18 + 1;
         int slotStartY = this.m_252907_() + slotY * 18 + 1;
         if (mouseX >= slotStartX && mouseX < slotStartX + 16 && mouseY >= slotStartY && mouseY < slotStartY + 16) {
            return new Rect2i(slotStartX + 1, slotStartY + 1, 16, 16);
         }
      }

      return new Rect2i(this.m_252754_(), this.m_252907_(), this.f_93618_, this.f_93619_);
   }
}
