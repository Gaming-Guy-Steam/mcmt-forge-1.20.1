package mekanism.client.gui.element.button;

import java.util.function.BooleanSupplier;
import mekanism.client.gui.IGuiWrapper;
import mekanism.client.gui.element.GuiElement;
import mekanism.common.util.MekanismUtils;
import net.minecraft.resources.ResourceLocation;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class ToggleButton extends MekanismImageButton {
   private static final ResourceLocation TOGGLE = MekanismUtils.getResource(MekanismUtils.ResourceType.GUI_BUTTON, "toggle.png");
   private static final ResourceLocation TOGGLE_FLIPPED = MekanismUtils.getResource(MekanismUtils.ResourceType.GUI_BUTTON, "toggle_flipped.png");
   private final ResourceLocation flipped;
   private final BooleanSupplier toggled;

   public ToggleButton(IGuiWrapper gui, int x, int y, BooleanSupplier toggled, @NotNull Runnable onPress, @Nullable GuiElement.IHoverable onHover) {
      this(gui, x, y, 18, toggled, onPress, onHover);
   }

   public ToggleButton(IGuiWrapper gui, int x, int y, int size, BooleanSupplier toggled, @NotNull Runnable onPress, @Nullable GuiElement.IHoverable onHover) {
      this(gui, x, y, size, 18, TOGGLE, TOGGLE_FLIPPED, toggled, onPress, onHover);
   }

   public ToggleButton(
      IGuiWrapper gui,
      int x,
      int y,
      int size,
      int textureSize,
      ResourceLocation toggle,
      ResourceLocation flipped,
      BooleanSupplier toggled,
      @NotNull Runnable onPress,
      @Nullable GuiElement.IHoverable onHover
   ) {
      super(gui, x, y, size, textureSize, toggle, onPress, onHover);
      this.toggled = toggled;
      this.flipped = flipped;
   }

   @Override
   protected ResourceLocation getResource() {
      return this.toggled.getAsBoolean() ? this.flipped : super.getResource();
   }
}
