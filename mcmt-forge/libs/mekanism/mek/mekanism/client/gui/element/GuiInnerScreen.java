package mekanism.client.gui.element;

import java.util.List;
import java.util.function.Supplier;
import mekanism.client.gui.IGuiWrapper;
import mekanism.client.jei.MekanismJEIRecipeType;
import mekanism.client.jei.interfaces.IJEIRecipeArea;
import mekanism.common.util.MekanismUtils;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class GuiInnerScreen extends GuiScalableElement implements IJEIRecipeArea<GuiInnerScreen> {
   public static final ResourceLocation SCREEN = MekanismUtils.getResource(MekanismUtils.ResourceType.GUI, "inner_screen.png");
   public static int SCREEN_SIZE = 32;
   private Supplier<List<Component>> renderStrings;
   private Supplier<List<Component>> tooltipStrings;
   private MekanismJEIRecipeType<?>[] recipeCategories;
   private boolean centerY;
   private int spacing = 1;
   private int padding = 3;
   private float textScale = 1.0F;

   public GuiInnerScreen(IGuiWrapper gui, int x, int y, int width, int height) {
      super(SCREEN, gui, x, y, width, height, SCREEN_SIZE, SCREEN_SIZE);
   }

   public GuiInnerScreen(IGuiWrapper gui, int x, int y, int width, int height, Supplier<List<Component>> renderStrings) {
      this(gui, x, y, width, height);
      this.renderStrings = renderStrings;
      this.defaultFormat();
   }

   public GuiInnerScreen tooltip(Supplier<List<Component>> tooltipStrings) {
      this.tooltipStrings = tooltipStrings;
      this.f_93623_ = true;
      return this;
   }

   public GuiInnerScreen spacing(int spacing) {
      this.spacing = spacing;
      return this;
   }

   public GuiInnerScreen padding(int padding) {
      this.padding = padding;
      return this;
   }

   public GuiInnerScreen textScale(float textScale) {
      this.textScale = textScale;
      return this;
   }

   public GuiInnerScreen centerY() {
      this.centerY = true;
      return this;
   }

   public GuiInnerScreen clearFormat() {
      this.centerY = false;
      return this;
   }

   public GuiInnerScreen defaultFormat() {
      return this.padding(5).spacing(3).textScale(0.8F).centerY();
   }

   @Override
   public void renderForeground(GuiGraphics guiGraphics, int mouseX, int mouseY) {
      super.renderForeground(guiGraphics, mouseX, mouseY);
      if (this.renderStrings != null) {
         List<Component> list = this.renderStrings.get();
         float startY = this.relativeY + this.padding;
         if (this.centerY) {
            int listSize = list.size();
            int totalHeight = listSize * 8 + this.spacing * (listSize - 1);
            startY = this.relativeY + (this.m_93694_() - totalHeight) / 2.0F;
         }

         for (Component text : this.renderStrings.get()) {
            this.drawText(guiGraphics, text, this.relativeX + this.padding, startY);
            startY += 8 + this.spacing;
         }
      }
   }

   @Override
   public void renderToolTip(@NotNull GuiGraphics guiGraphics, int mouseX, int mouseY) {
      super.renderToolTip(guiGraphics, mouseX, mouseY);
      if (this.tooltipStrings != null) {
         List<Component> list = this.tooltipStrings.get();
         if (list != null && !list.isEmpty()) {
            this.displayTooltips(guiGraphics, mouseX, mouseY, list);
         }
      }
   }

   private void drawText(GuiGraphics guiGraphics, Component text, float x, float y) {
      this.drawScaledTextScaledBound(guiGraphics, text, x, y, this.screenTextColor(), this.m_5711_() - this.padding * 2, this.textScale);
   }

   @NotNull
   public GuiInnerScreen jeiCategories(@NotNull MekanismJEIRecipeType<?>... recipeCategories) {
      this.recipeCategories = recipeCategories;
      return this;
   }

   @Nullable
   @Override
   public MekanismJEIRecipeType<?>[] getRecipeCategories() {
      return this.recipeCategories;
   }

   @Override
   public boolean isMouseOverJEIArea(double mouseX, double mouseY) {
      return this.f_93624_
         && mouseX >= this.m_252754_()
         && mouseY >= this.m_252907_()
         && mouseX < this.m_252754_() + this.f_93618_
         && mouseY < this.m_252907_() + this.f_93619_;
   }
}
