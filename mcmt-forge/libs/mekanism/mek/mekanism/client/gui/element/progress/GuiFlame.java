package mekanism.client.gui.element.progress;

import mekanism.client.gui.IGuiWrapper;
import net.minecraft.client.gui.GuiGraphics;
import org.jetbrains.annotations.NotNull;

public class GuiFlame extends GuiProgress {
   public GuiFlame(IProgressInfoHandler handler, IGuiWrapper gui, int x, int y) {
      super(handler, ProgressType.FLAME, gui, x, y);
   }

   @Override
   public void drawBackground(@NotNull GuiGraphics guiGraphics, int mouseX, int mouseY, float partialTicks) {
      super.drawBackground(guiGraphics, mouseX, mouseY, partialTicks);
      guiGraphics.m_280163_(
         this.getResource(),
         this.relativeX,
         this.relativeY,
         0.0F,
         0.0F,
         this.f_93618_,
         this.f_93619_,
         this.type.getTextureWidth(),
         this.type.getTextureHeight()
      );
      if (this.handler.isActive()) {
         int displayInt = (int)(this.getProgress() * this.f_93619_);
         if (displayInt > 0) {
            guiGraphics.m_280163_(
               this.getResource(),
               this.relativeX,
               this.relativeY + this.f_93619_ - displayInt,
               this.f_93618_,
               this.f_93619_ - displayInt,
               this.f_93618_,
               displayInt,
               this.type.getTextureWidth(),
               this.type.getTextureHeight()
            );
         }
      }
   }
}
