package mekanism.client.gui.element;

import com.mojang.blaze3d.vertex.PoseStack;
import java.util.function.Consumer;
import java.util.function.Supplier;
import mekanism.client.gui.GuiUtils;
import mekanism.client.gui.IGuiWrapper;
import mekanism.common.inventory.GuiComponents;
import mekanism.common.registries.MekanismSounds;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import org.jetbrains.annotations.NotNull;

public class GuiDropdown<TYPE extends Enum<TYPE> & GuiComponents.IDropdownEnum<TYPE>> extends GuiTexturedElement {
   private final Consumer<TYPE> handler;
   private final Supplier<TYPE> curType;
   private final TYPE[] options;
   private boolean isOpen;
   private boolean isHolding;

   public GuiDropdown(IGuiWrapper gui, int x, int y, int width, Class<TYPE> enumClass, Supplier<TYPE> curType, Consumer<TYPE> handler) {
      super(GuiInnerScreen.SCREEN, gui, x, y, width, 12);
      this.curType = curType;
      this.handler = handler;
      this.options = enumClass.getEnumConstants();
      this.f_93623_ = true;
      this.clickSound = MekanismSounds.BEEP;
   }

   @Override
   public void onClick(double mouseX, double mouseY, int button) {
      super.onClick(mouseX, mouseY, button);
      this.isHolding = true;
      this.setOpen(!this.isOpen || mouseY > this.m_252907_() + 11);
   }

   @Override
   public void m_7691_(double mouseX, double mouseY) {
      super.m_7691_(mouseX, mouseY);
      if (this.isHolding) {
         this.isHolding = false;
         if (this.isOpen && mouseY > this.m_252907_() + 11) {
            this.handler.accept(this.options[this.getHoveredIndex(mouseX, mouseY)]);
            this.setOpen(false);
         }
      }
   }

   @Override
   public void renderForeground(GuiGraphics guiGraphics, int mouseX, int mouseY) {
      super.renderForeground(guiGraphics, mouseX, mouseY);
      int maxWidth = this.f_93618_ - 11;
      TYPE current = this.curType.get();
      this.drawScaledTextScaledBound(guiGraphics, current.getShortName(), this.relativeX + 4, this.relativeY + 2, this.screenTextColor(), maxWidth, 0.8F);
      if (this.isOpen) {
         for (int i = 0; i < this.options.length; i++) {
            this.drawScaledTextScaledBound(
               guiGraphics, this.options[i].getShortName(), this.relativeX + 4, this.relativeY + 11 + 2 + 10 * i, this.screenTextColor(), maxWidth, 0.8F
            );
         }
      }
   }

   @Override
   public void drawBackground(@NotNull GuiGraphics guiGraphics, int mouseX, int mouseY, float partialTicks) {
      super.drawBackground(guiGraphics, mouseX, mouseY, partialTicks);
      PoseStack pose = guiGraphics.m_280168_();
      pose.m_85836_();
      pose.m_252880_(0.0F, 0.0F, 1.0F);
      this.renderBackgroundTexture(guiGraphics, this.getResource(), GuiInnerScreen.SCREEN_SIZE, GuiInnerScreen.SCREEN_SIZE);
      int index = this.getHoveredIndex(mouseX, mouseY);
      if (index != -1) {
         GuiUtils.drawOutline(guiGraphics, this.relativeX + 1, this.relativeY + 12 + index * 10, this.f_93618_ - 2, 10, this.screenTextColor());
      }

      TYPE current = this.curType.get();
      if (current.getIcon() != null) {
         guiGraphics.m_280163_(current.getIcon(), this.relativeX + this.f_93618_ - 9, this.relativeY + 3, 0.0F, 0.0F, 6, 6, 6, 6);
      }

      if (this.isOpen) {
         for (int i = 0; i < this.options.length; i++) {
            ResourceLocation icon = this.options[i].getIcon();
            if (icon != null) {
               guiGraphics.m_280163_(icon, this.relativeX + this.f_93618_ - 9, this.relativeY + 12 + 2 + 10 * i, 0.0F, 0.0F, 6, 6, 6, 6);
            }
         }
      }

      pose.m_85849_();
   }

   @Override
   public void renderToolTip(@NotNull GuiGraphics guiGraphics, int mouseX, int mouseY) {
      super.renderToolTip(guiGraphics, mouseX, mouseY);
      int index = this.getHoveredIndex(mouseX, mouseY);
      if (index != -1) {
         Component text = this.options[index].getTooltip();
         if (text != null) {
            this.displayTooltips(guiGraphics, mouseX, mouseY, new Component[]{this.options[index].getTooltip()});
         }
      }
   }

   private int getHoveredIndex(double mouseX, double mouseY) {
      return this.isOpen
            && mouseX >= this.m_252754_()
            && mouseX < this.m_252754_() + this.f_93618_
            && mouseY >= this.m_252907_() + 11
            && mouseY < this.m_252907_() + this.f_93619_
         ? Math.max(0, Math.min(this.options.length - 1, (int)((mouseY - this.m_252907_() - 11.0) / 10.0)))
         : -1;
   }

   private void setOpen(boolean open) {
      if (this.isOpen != open) {
         if (open) {
            this.f_93619_ = this.f_93619_ + this.options.length * 10 + 1;
         } else {
            this.f_93619_ = this.f_93619_ - (this.options.length * 10 + 1);
         }
      }

      this.isOpen = open;
   }
}
