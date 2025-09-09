package mekanism.client.gui.element.button;

import java.util.Objects;
import mekanism.client.gui.IGuiWrapper;
import mekanism.client.gui.element.GuiElement;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.navigation.CommonInputs;
import net.minecraft.network.chat.Component;
import net.minecraft.sounds.SoundEvents;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class MekanismButton extends GuiElement {
   @Nullable
   private final GuiElement.IHoverable onHover;
   @NotNull
   private final Runnable onLeftClick;
   @Nullable
   private final Runnable onRightClick;

   public MekanismButton(
      IGuiWrapper gui, int x, int y, int width, int height, Component text, @NotNull Runnable onLeftClick, @Nullable GuiElement.IHoverable onHover
   ) {
      this(gui, x, y, width, height, text, onLeftClick, onLeftClick, onHover);
   }

   public MekanismButton(
      IGuiWrapper gui,
      int x,
      int y,
      int width,
      int height,
      Component text,
      @NotNull Runnable onLeftClick,
      @Nullable Runnable onRightClick,
      @Nullable GuiElement.IHoverable onHover
   ) {
      super(gui, x, y, width, height, text);
      this.onHover = onHover;
      this.onLeftClick = Objects.requireNonNull(onLeftClick, "Buttons must have a left click behavior");
      this.onRightClick = onRightClick;
      this.clickSound = SoundEvents.f_12490_;
      this.setButtonBackground(GuiElement.ButtonBackground.DEFAULT);
   }

   @Override
   public void onClick(double mouseX, double mouseY, int button) {
      if (button == 0) {
         this.onLeftClick.run();
      } else if (button == 1 && this.onRightClick != null) {
         this.onRightClick.run();
      }
   }

   public boolean m_7972_(int button) {
      return button == 0 || button == 1 && this.onRightClick != null;
   }

   @Override
   public boolean m_7933_(int keyCode, int scanCode, int modifiers) {
      if (this.f_93623_ && this.f_93624_ && this.m_93696_() && CommonInputs.m_278691_(keyCode)) {
         this.m_7435_(minecraft.m_91106_());
         this.onLeftClick.run();
         return true;
      } else {
         return super.m_7933_(keyCode, scanCode, modifiers);
      }
   }

   @Override
   public void renderToolTip(@NotNull GuiGraphics guiGraphics, int mouseX, int mouseY) {
      super.renderToolTip(guiGraphics, mouseX, mouseY);
      if (this.onHover != null) {
         this.onHover.onHover(this, guiGraphics, mouseX, mouseY);
      }
   }
}
