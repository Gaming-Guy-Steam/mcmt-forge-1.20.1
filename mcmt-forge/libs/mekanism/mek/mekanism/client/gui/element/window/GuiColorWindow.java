package mekanism.client.gui.element.window;

import com.mojang.blaze3d.vertex.VertexConsumer;
import java.util.function.Consumer;
import java.util.function.Supplier;
import mekanism.api.text.EnumColor;
import mekanism.api.text.ILangEntry;
import mekanism.client.gui.GuiUtils;
import mekanism.client.gui.IGuiWrapper;
import mekanism.client.gui.element.GuiElement;
import mekanism.client.gui.element.GuiElementHolder;
import mekanism.client.gui.element.button.TranslationButton;
import mekanism.client.gui.element.custom.GuiEntityPreview;
import mekanism.client.gui.element.text.BackgroundType;
import mekanism.client.gui.element.text.GuiTextField;
import mekanism.common.MekanismLang;
import mekanism.common.inventory.container.SelectedWindowData;
import mekanism.common.lib.Color;
import mekanism.common.util.MekanismUtils;
import mekanism.common.util.text.InputValidator;
import mekanism.common.util.text.TextUtils;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.LivingEntity;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.joml.Matrix4f;

public class GuiColorWindow extends GuiWindow {
   public static final ResourceLocation TRANSPARENCY_GRID = MekanismUtils.getResource(MekanismUtils.ResourceType.GUI, "transparency_grid.png");
   private static final ResourceLocation HUE_PICKER = MekanismUtils.getResource(MekanismUtils.ResourceType.GUI, "color_picker.png");
   private static final int S_TILES = 10;
   private static final int V_TILES = 10;
   private final GuiTextField textField;
   private final boolean handlesAlpha;
   @Nullable
   private final Consumer<Color> updatePreviewColor;
   @Nullable
   private final Runnable previewReset;
   private float hue;
   private float saturation = 0.5F;
   private float value = 0.5F;
   private float alpha = 1.0F;

   public GuiColorWindow(IGuiWrapper gui, int x, int y, boolean handlesAlpha, Consumer<Color> callback) {
      this(gui, x, y, handlesAlpha, callback, null, null, null);
   }

   public GuiColorWindow(
      IGuiWrapper gui,
      int x,
      int y,
      boolean handlesAlpha,
      Consumer<Color> callback,
      @Nullable Supplier<LivingEntity> armorPreview,
      @Nullable Consumer<Color> updatePreviewColor,
      @Nullable Runnable previewReset
   ) {
      super(gui, x, y, (handlesAlpha ? 184 : 158) + (armorPreview == null ? 0 : 83), handlesAlpha ? 152 : 140, SelectedWindowData.WindowType.COLOR);
      this.interactionStrategy = GuiWindow.InteractionStrategy.NONE;
      this.handlesAlpha = handlesAlpha;
      this.updatePreviewColor = updatePreviewColor;
      this.previewReset = previewReset;
      int extraWidth = this.handlesAlpha ? 26 : 0;
      int extraShadeWidth = this.handlesAlpha ? 20 : 0;
      int extraViewWidth = extraWidth - extraShadeWidth;
      this.addChild(new GuiElementHolder(gui, this.relativeX + 6, this.relativeY + 17, 41 + extraViewWidth, 82));
      this.addChild(new GuiColorWindow.GuiColorView(gui, this.relativeX + 7, this.relativeY + 18, 39 + extraViewWidth, 80));
      this.addChild(new GuiElementHolder(gui, this.relativeX + 50 + extraViewWidth, this.relativeY + 17, 102 + extraShadeWidth, 82));
      this.addChild(new GuiColorWindow.GuiShadePicker(gui, this.relativeX + 51 + extraViewWidth, this.relativeY + 18, 100 + extraShadeWidth, 80));
      this.addChild(new GuiElementHolder(gui, this.relativeX + 6, this.relativeY + 103, 146 + extraWidth, 10));
      this.addChild(new GuiColorWindow.GuiHuePicker(gui, this.relativeX + 7, this.relativeY + 104, 144 + extraWidth, 8));
      if (this.handlesAlpha) {
         this.addChild(new GuiElementHolder(gui, this.relativeX + 6, this.relativeY + 115, 146 + extraWidth, 10));
         this.addChild(new GuiColorWindow.GuiAlphaPicker(gui, this.relativeX + 7, this.relativeY + 116, 144 + extraWidth, 8));
      }

      int textOffset = this.handlesAlpha ? 6 : 0;
      this.textField = this.addChild(
         new GuiTextField(gui, this.relativeX + 30 + textOffset, this.relativeY + this.f_93619_ - 20, 63 + extraWidth - textOffset, 12)
      );
      this.textField
         .setInputValidator(InputValidator.DIGIT.or(c -> c == ','))
         .setPasteTransformer(text -> text.replace(" ", ""))
         .setBackground(BackgroundType.ELEMENT_HOLDER)
         .setMaxLength(this.handlesAlpha ? 15 : 11);
      this.addChild(
         new TranslationButton(gui, this.relativeX + 98 + extraWidth, this.relativeY + this.f_93619_ - 21, 54, 14, MekanismLang.BUTTON_CONFIRM, () -> {
            callback.accept(this.getColor());
            this.close();
         })
      );
      if (armorPreview != null) {
         this.addChild(new GuiEntityPreview(gui, this.relativeX + 155 + extraWidth, this.relativeY + 17, 80, this.f_93619_ - 24, 5, armorPreview));
      }

      this.setColor(Color.rgbi(128, 70, 70));
   }

   @Override
   public void close() {
      super.close();
      if (this.previewReset != null) {
         this.previewReset.run();
      }
   }

   public Color getColor() {
      Color color = Color.hsv(this.hue, this.saturation, this.value);
      if (this.handlesAlpha) {
         color = color.alpha(this.alpha);
      }

      return color;
   }

   public void setColor(Color color) {
      this.setFromColor(color);
      this.updateTextFromColor();
   }

   @Override
   public void renderForeground(GuiGraphics guiGraphics, int mouseX, int mouseY) {
      super.renderForeground(guiGraphics, mouseX, mouseY);
      this.drawTitleText(guiGraphics, MekanismLang.COLOR_PICKER.translate(new Object[0]), 6.0F);
      ILangEntry entry = this.handlesAlpha ? MekanismLang.RGBA : MekanismLang.RGB;
      int textOffset = this.handlesAlpha ? 6 : 0;
      this.drawTextScaledBound(
         guiGraphics, entry.translate(), this.relativeX + 7, this.relativeY + this.f_93619_ - 18.0F, this.titleTextColor(), 20 + textOffset
      );
   }

   private void drawTiledGradient(GuiGraphics guiGraphics, int x, int y, int width, int height) {
      int tileWidth = Math.round(width / 10.0F);
      int tileHeight = Math.round(height / 10.0F);

      for (int i = 0; i < 10; i++) {
         float minV = i / 10.0F;
         float maxV = (i + 1) / 10.0F;

         for (int j = 0; j < 10; j++) {
            float minS = j / 10.0F;
            float maxS = (j + 1) / 10.0F;
            Color tl = Color.hsv(this.hue, minS, maxV);
            Color tr = Color.hsv(this.hue, maxS, maxV);
            Color bl = Color.hsv(this.hue, minS, minV);
            Color br = Color.hsv(this.hue, maxS, minV);
            this.drawGradient(guiGraphics, x + j * tileWidth, y + (10 - i - 1) * tileHeight, tileWidth, tileHeight, tl, tr, bl, br);
         }
      }
   }

   private void drawGradient(GuiGraphics guiGraphics, int x, int y, int width, int height, Color tl, Color tr, Color bl, Color br) {
      VertexConsumer buffer = guiGraphics.m_280091_().m_6299_(RenderType.m_285907_());
      Matrix4f matrix4f = guiGraphics.m_280168_().m_85850_().m_252922_();
      buffer.m_252986_(matrix4f, x, y + height, 0.0F).m_6122_(bl.r(), bl.g(), bl.b(), bl.a()).m_5752_();
      buffer.m_252986_(matrix4f, x + width, y + height, 0.0F).m_6122_(br.r(), br.g(), br.b(), br.a()).m_5752_();
      buffer.m_252986_(matrix4f, x + width, y, 0.0F).m_6122_(tr.r(), tr.g(), tr.b(), tr.a()).m_5752_();
      buffer.m_252986_(matrix4f, x, y, 0.0F).m_6122_(tl.r(), tl.g(), tl.b(), tl.a()).m_5752_();
      guiGraphics.m_280262_();
   }

   private void updateTextFromColor() {
      Color color = this.getColor();
      String text = color.r() + "," + color.g() + "," + color.b();
      if (this.handlesAlpha) {
         text = text + "," + color.a();
      }

      this.textField.setText(text);
   }

   private void setFromColor(Color c) {
      double[] hsv = c.hsvArray();
      this.hue = (float)hsv[0];
      this.saturation = (float)hsv[1];
      this.value = (float)hsv[2];
      this.alpha = this.handlesAlpha ? c.af() : 255.0F;
      if (this.updatePreviewColor != null) {
         this.updatePreviewColor.accept(c);
      }
   }

   private void updateArmorPreview() {
      if (this.updatePreviewColor != null) {
         this.updatePreviewColor.accept(this.getColor());
      }
   }

   private void updateColorFromText() {
      String[] split = this.textField.getText().split(",");
      if (split.length == (this.handlesAlpha ? 4 : 3)) {
         try {
            int r = Integer.parseInt(split[0]);
            int g = Integer.parseInt(split[1]);
            int b = Integer.parseInt(split[2]);
            int a = this.handlesAlpha ? Integer.parseInt(split[3]) : 255;
            if (!this.byteCheck(r) || !this.byteCheck(g) || !this.byteCheck(b) || !this.byteCheck(a)) {
               return;
            }

            this.setFromColor(Color.rgbai(r, g, b, a));
         } catch (NumberFormatException var6) {
         }
      }
   }

   private boolean byteCheck(int val) {
      return val >= 0 && val <= 255;
   }

   private void drawColorBar(GuiGraphics guiGraphics, int x, int y, int width, int height) {
      for (int i = 0; i < width; i++) {
         GuiUtils.fill(guiGraphics, x + i, y, 1, height, Color.hsv((float)i / width * 360.0F, 1.0, 1.0).argb());
      }
   }

   private void drawAlphaBar(GuiGraphics guiGraphics, int x, int y, int width, int height) {
      Color hsv = Color.hsv(this.hue, this.saturation, this.value);

      for (int i = 0; i < width; i++) {
         GuiUtils.fill(guiGraphics, x + i, y, 1, height, hsv.alpha((float)i / width).argb());
      }
   }

   private void drawTransparencyGrid(GuiGraphics guiGraphics, int x, int y, int width, int height) {
      if (this.handlesAlpha) {
         guiGraphics.m_280218_(TRANSPARENCY_GRID, x, y, 0, 0, width, height);
      }
   }

   @Override
   public boolean m_5534_(char c, int keyCode) {
      boolean ret = super.m_5534_(c, keyCode);
      if (this.textField.canWrite()) {
         this.updateColorFromText();
      }

      return ret;
   }

   @Override
   public boolean m_7933_(int keyCode, int scanCode, int modifiers) {
      boolean ret = super.m_7933_(keyCode, scanCode, modifiers);
      if (this.textField.canWrite() && (Screen.m_96630_(keyCode) || Screen.m_96628_(keyCode) || keyCode == 259 || keyCode == 261)) {
         this.updateColorFromText();
      }

      return ret;
   }

   public class GuiAlphaPicker extends GuiColorWindow.GuiPicker {
      public GuiAlphaPicker(IGuiWrapper gui, int x, int y, int width, int height) {
         super(gui, x, y, width, height);
      }

      @Override
      public void drawBackground(@NotNull GuiGraphics guiGraphics, int mouseX, int mouseY, float partialTicks) {
         super.drawBackground(guiGraphics, mouseX, mouseY, partialTicks);
         GuiColorWindow.this.drawTransparencyGrid(guiGraphics, this.relativeX, this.relativeY, this.f_93618_, this.f_93619_);
      }

      @Override
      public void renderBackgroundOverlay(GuiGraphics guiGraphics, int mouseX, int mouseY) {
         super.renderBackgroundOverlay(guiGraphics, mouseX, mouseY);
         GuiColorWindow.this.drawAlphaBar(guiGraphics, this.relativeX, this.relativeY, this.f_93618_, this.f_93619_);
         int posX = Math.round(GuiColorWindow.this.alpha * (this.f_93618_ - 3));
         guiGraphics.m_280163_(GuiColorWindow.HUE_PICKER, this.relativeX - 2 + posX, this.relativeY - 2, 0.0F, 0.0F, 7, 12, 12, 12);
         GuiColorWindow.this.drawTransparencyGrid(guiGraphics, this.relativeX + posX, this.relativeY, 3, 8);
         GuiUtils.fill(guiGraphics, this.relativeX + posX, this.relativeY, 3, 8, GuiColorWindow.this.getColor().argb());
      }

      @Override
      protected void set(double mouseX, double mouseY) {
         float val = (float)(mouseX - this.m_252754_()) / this.f_93618_;
         GuiColorWindow.this.alpha = Mth.m_14036_(val, 0.0F, 1.0F);
         GuiColorWindow.this.updateTextFromColor();
         GuiColorWindow.this.updateArmorPreview();
      }
   }

   public class GuiColorView extends GuiElement {
      public GuiColorView(IGuiWrapper gui, int x, int y, int width, int height) {
         super(gui, x, y, width, height);
      }

      @Override
      public void renderToolTip(@NotNull GuiGraphics guiGraphics, int mouseX, int mouseY) {
         super.renderToolTip(guiGraphics, mouseX, mouseY);
         String hex;
         if (GuiColorWindow.this.handlesAlpha) {
            hex = TextUtils.hex(false, 4, GuiColorWindow.this.getColor().argb());
         } else {
            hex = TextUtils.hex(false, 3, GuiColorWindow.this.getColor().rgb());
         }

         this.displayTooltips(guiGraphics, mouseX, mouseY, new Component[]{MekanismLang.GENERIC_HEX.translateColored(EnumColor.GRAY, new Object[]{hex})});
      }

      @Override
      public void drawBackground(@NotNull GuiGraphics guiGraphics, int mouseX, int mouseY, float partialTicks) {
         super.drawBackground(guiGraphics, mouseX, mouseY, partialTicks);
         GuiColorWindow.this.drawTransparencyGrid(guiGraphics, this.relativeX, this.relativeY, this.f_93618_, this.f_93619_);
         Color c = GuiColorWindow.this.getColor();
         GuiUtils.fill(guiGraphics, this.relativeX, this.relativeY, this.f_93618_, this.f_93619_, c.argb());
      }
   }

   public class GuiHuePicker extends GuiColorWindow.GuiPicker {
      public GuiHuePicker(IGuiWrapper gui, int x, int y, int width, int height) {
         super(gui, x, y, width, height);
      }

      @Override
      public void renderBackgroundOverlay(GuiGraphics guiGraphics, int mouseX, int mouseY) {
         super.renderBackgroundOverlay(guiGraphics, mouseX, mouseY);
         GuiColorWindow.this.drawColorBar(guiGraphics, this.relativeX, this.relativeY, this.f_93618_, this.f_93619_);
         int posX = Math.round(GuiColorWindow.this.hue / 360.0F * (this.f_93618_ - 3));
         guiGraphics.m_280163_(GuiColorWindow.HUE_PICKER, this.relativeX - 2 + posX, this.relativeY - 2, 0.0F, 0.0F, 7, 12, 12, 12);
         GuiUtils.fill(guiGraphics, this.relativeX + posX, this.relativeY, 3, 8, Color.hsv(GuiColorWindow.this.hue, 1.0, 1.0).argb());
      }

      @Override
      protected void set(double mouseX, double mouseY) {
         float val = (float)(mouseX - this.m_252754_()) / this.f_93618_;
         GuiColorWindow.this.hue = Mth.m_14036_(val, 0.0F, 1.0F) * 360.0F;
         GuiColorWindow.this.updateTextFromColor();
         GuiColorWindow.this.updateArmorPreview();
      }
   }

   private abstract static class GuiPicker extends GuiElement {
      private boolean isDragging;

      public GuiPicker(IGuiWrapper gui, int x, int y, int width, int height) {
         super(gui, x, y, width, height);
      }

      protected abstract void set(double mouseX, double mouseY);

      @Override
      public void onClick(double mouseX, double mouseY, int button) {
         this.set(mouseX, mouseY);
         this.isDragging = true;
      }

      @Override
      public void m_7212_(double mouseX, double mouseY, double deltaX, double deltaY) {
         super.m_7212_(mouseX, mouseY, deltaX, deltaY);
         if (this.isDragging) {
            this.set(mouseX, mouseY);
         }
      }

      @Override
      public void m_7691_(double mouseX, double mouseY) {
         super.m_7691_(mouseX, mouseY);
         this.isDragging = false;
      }
   }

   public class GuiShadePicker extends GuiColorWindow.GuiPicker {
      public GuiShadePicker(IGuiWrapper gui, int x, int y, int width, int height) {
         super(gui, x, y, width, height);
      }

      @Override
      public void renderBackgroundOverlay(GuiGraphics guiGraphics, int mouseX, int mouseY) {
         super.renderBackgroundOverlay(guiGraphics, mouseX, mouseY);
         GuiColorWindow.this.drawTiledGradient(guiGraphics, this.relativeX, this.relativeY, this.f_93618_, this.f_93619_);
         int posX = this.relativeX + Math.round(GuiColorWindow.this.saturation * this.f_93618_) - 2;
         int posY = this.relativeY + Math.round((1.0F - GuiColorWindow.this.value) * this.f_93619_) - 2;
         GuiUtils.drawOutline(guiGraphics, posX, posY, 5, 5, -1);
         GuiUtils.fill(guiGraphics, posX + 1, posY + 1, 3, 3, GuiColorWindow.this.getColor().alpha(1.0).argb());
      }

      @Override
      protected void set(double mouseX, double mouseY) {
         float newS = (float)(mouseX - this.m_252754_()) / this.f_93618_;
         GuiColorWindow.this.saturation = Mth.m_14036_(newS, 0.0F, 1.0F);
         float newV = (float)(mouseY - this.m_252907_()) / this.f_93619_;
         GuiColorWindow.this.value = 1.0F - Mth.m_14036_(newV, 0.0F, 1.0F);
         GuiColorWindow.this.updateTextFromColor();
         GuiColorWindow.this.updateArmorPreview();
      }
   }
}
