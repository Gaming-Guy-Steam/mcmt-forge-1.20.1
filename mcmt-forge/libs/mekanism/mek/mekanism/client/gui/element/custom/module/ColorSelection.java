package mekanism.client.gui.element.custom.module;

import java.util.Optional;
import java.util.function.Consumer;
import mekanism.api.gear.IModule;
import mekanism.api.gear.config.ModuleColorData;
import mekanism.client.gui.GuiModuleTweaker;
import mekanism.client.gui.GuiUtils;
import mekanism.client.gui.element.text.GuiTextField;
import mekanism.client.gui.element.window.GuiColorWindow;
import mekanism.common.MekanismLang;
import mekanism.common.content.gear.Module;
import mekanism.common.content.gear.ModuleConfigItem;
import mekanism.common.content.gear.ModuleHelper;
import mekanism.common.content.gear.shared.ModuleColorModulationUnit;
import mekanism.common.lib.Color;
import mekanism.common.registries.MekanismModules;
import mekanism.common.util.text.TextUtils;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.item.ArmorItem;
import net.minecraft.world.item.ItemStack;
import org.jetbrains.annotations.Nullable;

class ColorSelection extends MiniElement {
   private static final int OFFSET_Y = 1;
   private final int OFFSET_X;
   private final ModuleConfigItem<Integer> data;
   @Nullable
   private final GuiModuleTweaker.ArmorPreview armorPreview;
   private final boolean handlesAlpha;

   ColorSelection(
      GuiModuleScreen parent,
      ModuleConfigItem<Integer> data,
      int xPos,
      int yPos,
      int dataIndex,
      boolean handlesAlpha,
      @Nullable GuiModuleTweaker.ArmorPreview armorPreview
   ) {
      super(parent, xPos, yPos, dataIndex);
      this.data = data;
      this.handlesAlpha = handlesAlpha;
      this.armorPreview = armorPreview;
      this.OFFSET_X = this.parent.getScreenWidth() - 26;
   }

   private Color getColor() {
      return Color.argb(this.data.get());
   }

   @Override
   protected int getNeededHeight() {
      return 20;
   }

   @Override
   protected void renderBackground(GuiGraphics guiGraphics, int mouseX, int mouseY) {
      int xTarget = this.getRelativeX() + this.OFFSET_X;
      int yTarget = this.getRelativeY() + 1;
      GuiUtils.drawOutline(guiGraphics, xTarget, yTarget, 18, 18, GuiTextField.SCREEN_COLOR.getAsInt());
      guiGraphics.m_280218_(GuiColorWindow.TRANSPARENCY_GRID, xTarget + 1, yTarget + 1, 0, 0, 16, 16);
      GuiUtils.fill(guiGraphics, xTarget + 1, yTarget + 1, 16, 16, this.data.get());
   }

   @Override
   protected void renderForeground(GuiGraphics guiGraphics, int mouseX, int mouseY) {
      int textColor = this.parent.screenTextColor();
      this.parent.drawTextWithScale(guiGraphics, this.data.getDescription(), this.getRelativeX() + 3, this.getRelativeY(), textColor, 0.8F);
      String hex;
      if (this.handlesAlpha) {
         hex = TextUtils.hex(false, 4, this.data.get().intValue());
      } else {
         hex = TextUtils.hex(false, 3, this.getColor().rgb());
      }

      this.parent
         .drawTextExact(guiGraphics, MekanismLang.GENERIC_HEX.translate(new Object[]{hex}), this.getRelativeX() + 3, this.getRelativeY() + 11, textColor);
   }

   @Override
   protected void click(double mouseX, double mouseY) {
      if (this.mouseOver(mouseX, mouseY, this.OFFSET_X, 1, 18, 18)) {
         Consumer<Color> updatePreviewColor = null;
         Runnable previewReset = null;
         IModule<?> currentModule = this.parent.getCurrentModule();
         if (this.armorPreview != null && this.data.matches(MekanismModules.COLOR_MODULATION_UNIT, "color") && currentModule != null) {
            ItemStack stack = currentModule.getContainer().m_41777_();
            if (stack.m_41720_() instanceof ArmorItem armorItem) {
               Module<ModuleColorModulationUnit> colorModulation = ModuleHelper.get().load(stack, MekanismModules.COLOR_MODULATION_UNIT);
               if (colorModulation != null) {
                  Optional<ModuleConfigItem<Integer>> matchedData = colorModulation.getConfigItems()
                     .stream()
                     .filter(e -> e.getName().equals("color") && e.getData() instanceof ModuleColorData)
                     .map(e -> (ModuleConfigItem<Integer>)e)
                     .findFirst();
                  if (matchedData.isPresent()) {
                     this.armorPreview.get();
                     EquipmentSlot slot = armorItem.m_40402_();
                     this.armorPreview.updatePreview(slot, stack);
                     updatePreviewColor = c -> matchedData.get().set(c.argb());
                     previewReset = () -> this.armorPreview.resetToDefault(slot);
                  }
               }
            }
         }

         GuiColorWindow window = new GuiColorWindow(
            this.parent.gui(),
            this.parent.getGuiWidth() / 2 - 80,
            this.parent.getGuiHeight() / 2 - 60,
            this.handlesAlpha,
            color -> this.setData(this.data, color.argb()),
            this.armorPreview,
            updatePreviewColor,
            previewReset
         );
         window.setColor(this.getColor());
         this.parent.gui().addWindow(window);
      }
   }
}
