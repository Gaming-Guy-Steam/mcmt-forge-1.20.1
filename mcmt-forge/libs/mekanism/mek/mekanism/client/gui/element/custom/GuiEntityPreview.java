package mekanism.client.gui.element.custom;

import java.util.function.Supplier;
import mekanism.client.gui.IGuiWrapper;
import mekanism.client.gui.element.GuiElement;
import mekanism.client.gui.element.GuiInnerScreen;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.inventory.InventoryScreen;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.LivingEntity;
import org.jetbrains.annotations.NotNull;

public class GuiEntityPreview extends GuiElement {
   private final Supplier<LivingEntity> preview;
   private final int scale;
   private final int border;
   private final int size;
   private boolean isDragging;
   private float rotation;

   public GuiEntityPreview(IGuiWrapper gui, int x, int y, int size, int border, Supplier<LivingEntity> preview) {
      this(gui, x, y, size, size, border, preview);
   }

   public GuiEntityPreview(IGuiWrapper gui, int x, int y, int width, int height, int border, Supplier<LivingEntity> preview) {
      super(gui, x, y, width, height);
      this.border = border;
      this.size = Math.min(this.f_93618_, this.f_93619_);
      this.scale = (this.size - 2 * this.border) / 2;
      this.preview = preview;
   }

   @Override
   public void drawBackground(@NotNull GuiGraphics guiGraphics, int mouseX, int mouseY, float partialTicks) {
      super.drawBackground(guiGraphics, mouseX, mouseY, partialTicks);
      this.renderBackgroundTexture(guiGraphics, GuiInnerScreen.SCREEN, GuiInnerScreen.SCREEN_SIZE, GuiInnerScreen.SCREEN_SIZE);
   }

   @Override
   public void renderForeground(GuiGraphics guiGraphics, int mouseX, int mouseY) {
      super.renderForeground(guiGraphics, mouseX, mouseY);
      InventoryScreen.renderEntityInInventoryFollowsAngle(
         guiGraphics,
         this.relativeX + this.f_93618_ / 2,
         this.relativeY + this.f_93619_ - 2 - this.border - (this.f_93619_ - this.size) / 2,
         this.scale,
         this.rotation,
         0.0F,
         this.preview.get()
      );
   }

   @Override
   public void onClick(double mouseX, double mouseY, int button) {
      this.isDragging = true;
   }

   @Override
   public void m_7691_(double mouseX, double mouseY) {
      super.m_7691_(mouseX, mouseY);
      this.isDragging = false;
   }

   @Override
   public void m_7212_(double mouseX, double mouseY, double deltaX, double deltaY) {
      super.m_7212_(mouseX, mouseY, deltaX, deltaY);
      if (this.isDragging) {
         this.rotation = Mth.m_14177_(this.rotation - (float)(deltaX / 10.0));
      }
   }

   @Override
   public boolean m_6050_(double mouseX, double mouseY, double delta) {
      if (this.m_5953_(mouseX, mouseY)) {
         this.rotation = Mth.m_14177_(this.rotation + (float)delta);
         return true;
      } else {
         return super.m_6050_(mouseX, mouseY, delta);
      }
   }
}
