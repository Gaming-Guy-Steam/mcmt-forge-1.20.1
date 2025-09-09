package mekanism.client.gui.element;

import java.util.Collections;
import java.util.function.Consumer;
import java.util.function.Supplier;
import mekanism.api.math.MathUtils;
import mekanism.client.gui.IGuiWrapper;
import mekanism.common.inventory.GuiComponents;
import mekanism.common.registries.MekanismSounds;
import net.minecraft.client.gui.GuiGraphics;
import org.jetbrains.annotations.NotNull;

public class GuiDigitalIconToggle<TYPE extends Enum<TYPE> & GuiComponents.IToggleEnum<TYPE>> extends GuiInnerScreen {
   private final Supplier<TYPE> typeSupplier;
   private final Consumer<TYPE> typeSetter;
   private final TYPE[] options;

   public GuiDigitalIconToggle(
      IGuiWrapper gui, int x, int y, int width, int height, Class<TYPE> enumClass, Supplier<TYPE> typeSupplier, Consumer<TYPE> typeSetter
   ) {
      super(gui, x, y, width, height);
      this.typeSupplier = typeSupplier;
      this.typeSetter = typeSetter;
      this.options = enumClass.getEnumConstants();
      this.clickSound = MekanismSounds.BEEP;
      this.tooltip(() -> Collections.singletonList(this.typeSupplier.get().getTooltip()));
   }

   @Override
   public void drawBackground(@NotNull GuiGraphics guiGraphics, int mouseX, int mouseY, float partialTicks) {
      super.drawBackground(guiGraphics, mouseX, mouseY, partialTicks);
      TYPE type = this.typeSupplier.get();
      guiGraphics.m_280163_(type.getIcon(), this.relativeX + 3, this.relativeY + 3, 0.0F, 0.0F, this.f_93618_ - 6, this.f_93619_ - 6, 6, 6);
   }

   @Override
   public void onClick(double mouseX, double mouseY, int button) {
      TYPE nextType = MathUtils.getByIndexMod(this.options, this.typeSupplier.get().ordinal() + 1);
      this.typeSetter.accept(nextType);
   }
}
