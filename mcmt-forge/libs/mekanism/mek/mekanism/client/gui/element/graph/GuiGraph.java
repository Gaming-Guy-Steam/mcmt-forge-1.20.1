package mekanism.client.gui.element.graph;

import com.mojang.blaze3d.platform.GlStateManager.DestFactor;
import com.mojang.blaze3d.platform.GlStateManager.SourceFactor;
import com.mojang.blaze3d.systems.RenderSystem;
import java.util.Collection;
import mekanism.client.gui.IGuiWrapper;
import mekanism.client.gui.element.GuiInnerScreen;
import mekanism.client.gui.element.GuiTexturedElement;
import mekanism.client.render.MekanismRenderer;
import mekanism.common.util.MekanismUtils;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import org.jetbrains.annotations.NotNull;

public abstract class GuiGraph<COLLECTION extends Collection<?>, HANDLER extends GuiGraph.GraphDataHandler> extends GuiTexturedElement {
   private static final int TEXTURE_WIDTH = 3;
   private static final int TEXTURE_HEIGHT = 2;
   protected final COLLECTION graphData;
   protected final HANDLER dataHandler;
   protected boolean fixedScale = false;

   protected GuiGraph(IGuiWrapper gui, int x, int y, int width, int height, COLLECTION graphData, HANDLER handler) {
      super(MekanismUtils.getResource(MekanismUtils.ResourceType.GUI, "graph.png"), gui, x, y, width, height);
      this.graphData = graphData;
      this.dataHandler = handler;
   }

   @Override
   public void drawBackground(@NotNull GuiGraphics guiGraphics, int mouseX, int mouseY, float partialTicks) {
      super.drawBackground(guiGraphics, mouseX, mouseY, partialTicks);
      this.renderBackgroundTexture(guiGraphics, GuiInnerScreen.SCREEN, GuiInnerScreen.SCREEN_SIZE, GuiInnerScreen.SCREEN_SIZE);
      ResourceLocation texture = this.getResource();
      int size = this.graphData.size();
      int x = this.relativeX + 1;
      int y = this.relativeY + 1;
      int height = this.f_93619_ - 2;

      for (int i = 0; i < size; i++) {
         int relativeHeight = this.getRelativeHeight(i, height);
         guiGraphics.m_280163_(texture, x + i, y + height - relativeHeight, 0.0F, 0.0F, 1, 1, 3, 2);
         RenderSystem.enableBlend();
         RenderSystem.blendFunc(SourceFactor.SRC_ALPHA, DestFactor.ONE_MINUS_SRC_ALPHA);
         guiGraphics.m_280246_(1.0F, 1.0F, 1.0F, 0.2F + 0.8F * i / size);
         guiGraphics.m_280163_(texture, x + i, y + height - relativeHeight, 1.0F, 0.0F, 1, relativeHeight, 3, 2);
         int hoverIndex = mouseX - this.m_252754_();
         if (hoverIndex == i && mouseY >= this.m_252907_() && mouseY < this.m_252907_() + height) {
            guiGraphics.m_280246_(1.0F, 1.0F, 1.0F, 0.5F);
            guiGraphics.m_280163_(texture, x + i, y, 2.0F, 0.0F, 1, height, 3, 2);
            MekanismRenderer.resetColor(guiGraphics);
            guiGraphics.m_280163_(texture, x + i, y + height - relativeHeight, 0.0F, 1.0F, 1, 1, 3, 2);
         } else {
            MekanismRenderer.resetColor(guiGraphics);
         }

         RenderSystem.disableBlend();
      }
   }

   protected abstract int getRelativeHeight(int index, int height);

   protected abstract Component getDataDisplay(int hoverIndex);

   @Override
   public void renderToolTip(@NotNull GuiGraphics guiGraphics, int mouseX, int mouseY) {
      super.renderToolTip(guiGraphics, mouseX, mouseY);
      int hoverIndex = mouseX - this.m_252754_();
      if (hoverIndex >= 0 && hoverIndex < this.graphData.size()) {
         this.displayTooltips(guiGraphics, mouseX, mouseY, new Component[]{this.getDataDisplay(hoverIndex)});
      }
   }

   public interface GraphDataHandler {
   }
}
