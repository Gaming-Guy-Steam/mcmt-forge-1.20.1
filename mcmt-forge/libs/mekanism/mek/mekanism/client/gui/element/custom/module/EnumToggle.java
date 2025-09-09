package mekanism.client.gui.element.custom.module;

import java.util.List;
import mekanism.api.gear.config.ModuleEnumData;
import mekanism.api.text.IHasTextComponent;
import mekanism.common.content.gear.ModuleConfigItem;
import mekanism.common.util.MekanismUtils;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;

class EnumToggle<TYPE extends Enum<TYPE> & IHasTextComponent> extends MiniElement {
   private static final ResourceLocation SLIDER = MekanismUtils.getResource(MekanismUtils.ResourceType.GUI, "slider.png");
   private static final float TEXT_SCALE = 0.7F;
   private static final int BAR_START = 10;
   private final int BAR_LENGTH;
   private final ModuleConfigItem<TYPE> data;
   private final int optionDistance;
   boolean dragging = false;

   EnumToggle(GuiModuleScreen parent, ModuleConfigItem<TYPE> data, int xPos, int yPos, int dataIndex) {
      super(parent, xPos, yPos, dataIndex);
      this.data = data;
      this.BAR_LENGTH = this.parent.getScreenWidth() - 24;
      this.optionDistance = this.BAR_LENGTH / (this.getData().getEnums().size() - 1);
   }

   @Override
   protected int getNeededHeight() {
      return 28;
   }

   private ModuleEnumData<TYPE> getData() {
      return (ModuleEnumData<TYPE>)this.data.getData();
   }

   @Override
   protected void renderBackground(GuiGraphics guiGraphics, int mouseX, int mouseY) {
      int center = this.optionDistance * this.data.get().ordinal();
      guiGraphics.m_280163_(SLIDER, this.getRelativeX() + 10 + center - 2, this.getRelativeY() + 11, 0.0F, 0.0F, 5, 6, 8, 8);
      guiGraphics.m_280163_(SLIDER, this.getRelativeX() + 10, this.getRelativeY() + 17, 0.0F, 6.0F, this.BAR_LENGTH, 2, 8, 8);
   }

   @Override
   protected void renderForeground(GuiGraphics guiGraphics, int mouseX, int mouseY) {
      int textColor = this.parent.screenTextColor();
      this.parent.drawTextWithScale(guiGraphics, this.data.getDescription(), this.getRelativeX() + 3, this.getRelativeY(), textColor, 0.8F);
      List<TYPE> options = this.getData().getEnums();
      int i = 0;

      for (int count = options.size(); i < count; i++) {
         int center = this.optionDistance * i;
         Component text = options.get(i).getTextComponent();
         int textWidth = this.parent.getStringWidth(text);
         float widthScaling = textWidth / 2.0F * 0.7F;
         float left = 10 + center - widthScaling;
         if (left < 0.0F) {
            left = 0.0F;
         } else {
            int max = this.parent.getScreenWidth() - 1;
            int end = this.xPos + (int)Math.ceil(left + textWidth * 0.7F);
            if (end > max) {
               left -= end - max;
            }
         }

         this.parent.drawTextWithScale(guiGraphics, text, this.getRelativeX() + left, this.getRelativeY() + 20, textColor, 0.7F);
      }
   }

   @Override
   protected void click(double mouseX, double mouseY) {
      if (!this.dragging) {
         int center = this.optionDistance * this.data.get().ordinal();
         if (this.mouseOver(mouseX, mouseY, 10 + center - 2, 11, 5, 6)) {
            this.dragging = true;
         } else if (this.mouseOver(mouseX, mouseY, 10, 10, this.BAR_LENGTH, 12)) {
            this.setData(this.getData().getEnums(), mouseX);
         }
      }
   }

   @Override
   protected void onDrag(double mouseX, double mouseY, double deltaX, double deltaY) {
      if (this.dragging) {
         this.setData(this.getData().getEnums(), mouseX);
      }
   }

   private void setData(List<TYPE> options, double mouseX) {
      int size = options.size() - 1;
      int cur = (int)Math.round((mouseX - this.getX() - 10.0) / this.BAR_LENGTH * size);
      cur = Mth.m_14045_(cur, 0, size);
      if (cur != this.data.get().ordinal()) {
         this.setData(this.data, options.get(cur));
      }
   }

   @Override
   protected void release(double mouseX, double mouseY) {
      this.dragging = false;
   }
}
