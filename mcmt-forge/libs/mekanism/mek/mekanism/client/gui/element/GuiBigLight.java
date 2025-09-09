package mekanism.client.gui.element;

import java.util.function.BooleanSupplier;
import mekanism.client.gui.IGuiWrapper;
import mekanism.common.util.MekanismUtils;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.resources.ResourceLocation;
import org.jetbrains.annotations.NotNull;

public class GuiBigLight extends GuiTexturedElement {
   private static final ResourceLocation LIGHTS = MekanismUtils.getResource(MekanismUtils.ResourceType.GUI, "big_lights.png");
   private final BooleanSupplier lightSupplier;

   public GuiBigLight(IGuiWrapper gui, int x, int y, BooleanSupplier lightSupplier) {
      super(LIGHTS, gui, x, y, 14, 14);
      this.lightSupplier = lightSupplier;
   }

   @Override
   public void drawBackground(@NotNull GuiGraphics guiGraphics, int mouseX, int mouseY, float partialTicks) {
      super.drawBackground(guiGraphics, mouseX, mouseY, partialTicks);
      this.renderBackgroundTexture(guiGraphics, GuiInnerScreen.SCREEN, GuiInnerScreen.SCREEN_SIZE, GuiInnerScreen.SCREEN_SIZE);
      guiGraphics.m_280163_(
         this.getResource(),
         this.relativeX + 1,
         this.relativeY + 1,
         this.lightSupplier.getAsBoolean() ? 0.0F : 12.0F,
         0.0F,
         this.f_93618_ - 2,
         this.f_93619_ - 2,
         24,
         12
      );
   }
}
