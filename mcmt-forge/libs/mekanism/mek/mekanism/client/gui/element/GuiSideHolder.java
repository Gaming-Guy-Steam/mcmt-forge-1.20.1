package mekanism.client.gui.element;

import mekanism.client.SpecialColors;
import mekanism.client.gui.GuiUtils;
import mekanism.client.gui.IGuiWrapper;
import mekanism.client.render.MekanismRenderer;
import mekanism.client.render.lib.ColorAtlas;
import mekanism.common.util.MekanismUtils;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.resources.ResourceLocation;
import org.jetbrains.annotations.NotNull;

public abstract class GuiSideHolder extends GuiTexturedElement {
   private static final ResourceLocation HOLDER_LEFT = MekanismUtils.getResource(MekanismUtils.ResourceType.GUI, "holder_left.png");
   private static final ResourceLocation HOLDER_RIGHT = MekanismUtils.getResource(MekanismUtils.ResourceType.GUI, "holder_right.png");
   private static final int TEXTURE_WIDTH = 26;
   private static final int TEXTURE_HEIGHT = 9;
   protected final boolean left;
   private final boolean slotHolder;

   public static GuiSideHolder armorHolder(IGuiWrapper gui) {
      return create(gui, -26, 62, 98, true, true, SpecialColors.TAB_ARMOR_SLOTS);
   }

   public static GuiSideHolder create(IGuiWrapper gui, int x, int y, int height, boolean left, boolean slotHolder, ColorAtlas.ColorRegistryObject tabColor) {
      return new GuiSideHolder(gui, x, y, height, left, slotHolder) {
         @Override
         protected void colorTab(GuiGraphics guiGraphics) {
            MekanismRenderer.color(guiGraphics, tabColor);
         }
      };
   }

   protected GuiSideHolder(IGuiWrapper gui, int x, int y, int height, boolean left, boolean slotHolder) {
      super(left ? HOLDER_LEFT : HOLDER_RIGHT, gui, x, y, 26, height);
      this.left = left;
      this.slotHolder = slotHolder;
      this.f_93623_ = false;
      if (!this.slotHolder) {
         this.setButtonBackground(GuiElement.ButtonBackground.DEFAULT);
      }
   }

   protected abstract void colorTab(GuiGraphics guiGraphics);

   @Override
   public void m_87963_(@NotNull GuiGraphics guiGraphics, int mouseX, int mouseY, float partialTicks) {
      super.m_87963_(guiGraphics, mouseX, mouseY, partialTicks);
      if (this.slotHolder) {
         this.draw(guiGraphics);
      }
   }

   @Override
   public void drawBackground(@NotNull GuiGraphics guiGraphics, int mouseX, int mouseY, float partialTicks) {
      super.drawBackground(guiGraphics, mouseX, mouseY, partialTicks);
      if (!this.slotHolder) {
         this.draw(guiGraphics);
      }
   }

   private void draw(@NotNull GuiGraphics guiGraphics) {
      this.colorTab(guiGraphics);
      GuiUtils.blitNineSlicedSized(guiGraphics, this.getResource(), this.relativeX, this.relativeY, this.f_93618_, this.f_93619_, 4, 26, 9, 0, 0, 26, 9);
      MekanismRenderer.resetColor(guiGraphics);
   }
}
