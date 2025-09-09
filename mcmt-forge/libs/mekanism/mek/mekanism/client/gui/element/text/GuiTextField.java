package mekanism.client.gui.element.text;

import com.mojang.blaze3d.vertex.PoseStack;
import java.util.function.Consumer;
import java.util.function.IntSupplier;
import java.util.function.UnaryOperator;
import mekanism.api.functions.CharPredicate;
import mekanism.api.functions.CharUnaryOperator;
import mekanism.client.SpecialColors;
import mekanism.client.gui.IGuiWrapper;
import mekanism.client.gui.element.GuiElement;
import mekanism.client.gui.element.button.MekanismImageButton;
import mekanism.common.lib.Color;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.EditBox;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class GuiTextField extends GuiElement {
   public static final int DEFAULT_BORDER_COLOR = -6250336;
   public static final int DEFAULT_BACKGROUND_COLOR = -16777216;
   public static final IntSupplier SCREEN_COLOR = SpecialColors.TEXT_SCREEN::argb;
   public static final IntSupplier DARK_SCREEN_COLOR = () -> Color.argb(SCREEN_COLOR.getAsInt()).darken(0.4).argb();
   private final EditBox textField;
   private Runnable enterHandler;
   private CharPredicate inputValidator;
   private CharUnaryOperator inputTransformer;
   private UnaryOperator<String> pasteTransformer;
   private Consumer<String> responder;
   private BackgroundType backgroundType = BackgroundType.DEFAULT;
   private IconType iconType;
   private int textOffsetX;
   private int textOffsetY;
   private float textScale = 1.0F;
   private MekanismImageButton checkmarkButton;

   public GuiTextField(IGuiWrapper gui, int x, int y, int width, int height) {
      super(gui, x, y, width, height);
      this.textField = new EditBox(this.getFont(), this.m_252754_(), this.m_252907_(), width, height, Component.m_237119_());
      this.textField.m_94182_(false);
      this.textField.m_94151_(s -> {
         if (this.responder != null) {
            this.responder.accept(s);
         }

         if (this.checkmarkButton != null) {
            this.checkmarkButton.f_93623_ = !this.textField.m_94155_().isEmpty();
         }
      });
      this.gui().addFocusListener(this);
      this.updateTextField();
   }

   @Override
   public void resize(int prevLeft, int prevTop, int left, int top) {
      super.resize(prevLeft, prevTop, left, top);
      this.textField.m_252865_(this.textField.m_252754_() - prevLeft + left);
      this.textField.m_253211_(this.textField.m_252907_() - prevTop + top);
   }

   public GuiTextField setScale(float textScale) {
      this.textScale = textScale;
      return this;
   }

   public GuiTextField setOffset(int offsetX, int offsetY) {
      this.textOffsetX = offsetX;
      this.textOffsetY = offsetY;
      this.updateTextField();
      return this;
   }

   public GuiTextField configureDigitalInput(Runnable enterHandler) {
      this.setBackground(BackgroundType.NONE);
      this.setIcon(IconType.DIGITAL);
      this.setTextColor(this.screenTextColor());
      this.setEnterHandler(enterHandler);
      this.addCheckmarkButton(ButtonType.DIGITAL, enterHandler);
      this.setScale(0.8F);
      return this;
   }

   public GuiTextField configureDigitalBorderInput(Runnable enterHandler) {
      this.setBackground(BackgroundType.DIGITAL);
      this.setTextColor(this.screenTextColor());
      this.setEnterHandler(enterHandler);
      this.addCheckmarkButton(ButtonType.DIGITAL, enterHandler);
      this.setScale(0.8F);
      return this;
   }

   public GuiTextField setEnterHandler(Runnable enterHandler) {
      this.enterHandler = enterHandler;
      return this;
   }

   public GuiTextField setInputValidator(CharPredicate inputValidator) {
      this.inputValidator = inputValidator;
      return this;
   }

   public GuiTextField setInputTransformer(CharUnaryOperator inputTransformer) {
      this.inputTransformer = inputTransformer;
      return this;
   }

   public GuiTextField setPasteTransformer(UnaryOperator<String> pasteTransformer) {
      this.pasteTransformer = pasteTransformer;
      return this;
   }

   public GuiTextField setBackground(BackgroundType backgroundType) {
      this.backgroundType = backgroundType;
      return this;
   }

   public GuiTextField setIcon(IconType iconType) {
      this.iconType = iconType;
      this.updateTextField();
      return this;
   }

   public GuiTextField addCheckmarkButton(Runnable callback) {
      return this.addCheckmarkButton(ButtonType.NORMAL, callback);
   }

   public GuiTextField addCheckmarkButton(ButtonType type, Runnable callback) {
      this.checkmarkButton = this.addChild(type.getButton(this, () -> {
         callback.run();
         this.m_93692_(true);
      }));
      this.checkmarkButton.f_93623_ = false;
      this.updateTextField();
      return this;
   }

   private void updateTextField() {
      int iconOffsetX = this.iconType == null ? 0 : this.iconType.getOffsetX();
      this.textField
         .m_93674_(Math.round((this.f_93618_ - (this.checkmarkButton == null ? 0 : this.textField.m_93694_() + 2) - iconOffsetX) * (1.0F / this.textScale)));
      this.textField.m_252865_(this.m_252754_() + this.textOffsetX + 2 + iconOffsetX);
      this.textField.m_253211_(this.m_252907_() + this.textOffsetY + 1 + (int)(this.f_93619_ / 2.0F - 4.0F));
   }

   public boolean isTextFieldFocused() {
      return this.textField.m_93696_();
   }

   @Override
   public void onWindowClose() {
      super.onWindowClose();
      this.gui().removeFocusListener(this);
   }

   @Override
   public void move(int changeX, int changeY) {
      super.move(changeX, changeY);
      this.updateTextField();
   }

   @Override
   public void tick() {
      super.tick();
      this.textField.m_94120_();
   }

   @Nullable
   @Override
   public GuiElement mouseClickedNested(double mouseX, double mouseY, int button) {
      boolean prevFocus = this.isTextFieldFocused();
      double scaledX = mouseX;
      if (this.textScale != 1.0F && mouseX > this.textField.m_252754_()) {
         scaledX = Math.min(mouseX, (double)this.textField.m_252754_()) + (mouseX - this.textField.m_252754_()) * (1.0F / this.textScale);
      }

      boolean ret = this.textField.m_6375_(scaledX, mouseY, button);
      if (!prevFocus && this.isTextFieldFocused()) {
         this.gui().focusChange(this);
      }

      return (GuiElement)(ret ? this : super.mouseClickedNested(mouseX, mouseY, button));
   }

   @Override
   public void drawBackground(@NotNull GuiGraphics guiGraphics, int mouseX, int mouseY, float partialTicks) {
      super.drawBackground(guiGraphics, mouseX, mouseY, partialTicks);
      this.backgroundType.render(this, guiGraphics);
      PoseStack pose = guiGraphics.m_280168_();
      pose.m_85836_();
      pose.m_252880_(-this.getGuiLeft(), -this.getGuiTop(), 0.0F);
      if (this.textScale == 1.0F) {
         this.textField.m_88315_(guiGraphics, mouseX, mouseY, partialTicks);
      } else {
         float reverse = 1.0F / this.textScale - 1.0F;
         float yAdd = 4.0F - this.textScale * 8.0F / 2.0F;
         pose.m_85841_(this.textScale, this.textScale, this.textScale);
         pose.m_252880_(this.textField.m_252754_() * reverse, this.textField.m_252907_() * reverse + yAdd * (1.0F / this.textScale), 0.0F);
         this.textField.m_88315_(guiGraphics, mouseX, mouseY, partialTicks);
      }

      pose.m_85849_();
      if (this.iconType != null) {
         guiGraphics.m_280163_(
            this.iconType.getIcon(),
            this.relativeX + 2,
            this.relativeY + this.f_93619_ / 2 - (int)Math.ceil(this.iconType.getHeight() / 2.0F),
            0.0F,
            0.0F,
            this.iconType.getWidth(),
            this.iconType.getHeight(),
            this.iconType.getWidth(),
            this.iconType.getHeight()
         );
      }
   }

   @Override
   public boolean hasPersistentData() {
      return true;
   }

   @Override
   public void syncFrom(GuiElement element) {
      super.syncFrom(element);
      this.textField.m_94144_(((GuiTextField)element).getText());
      this.m_93692_(element.m_93696_());
   }

   @Override
   public boolean m_7933_(int keyCode, int scanCode, int modifiers) {
      if (this.canWrite()) {
         if (keyCode == 256) {
            return false;
         } else if (keyCode != 257 && keyCode != 335) {
            if (keyCode == 258 && this.textField.f_94097_) {
               this.gui().incrementFocus(this);
               return true;
            } else {
               if (Screen.m_96630_(keyCode)) {
                  String text = Minecraft.m_91087_().f_91068_.m_90876_();
                  if (this.pasteTransformer != null) {
                     text = this.pasteTransformer.apply(text);
                  }

                  if (this.inputTransformer != null || this.inputValidator != null) {
                     boolean transformed = false;
                     char[] charArray = text.toCharArray();

                     for (int i = 0; i < charArray.length; i++) {
                        char c = charArray[i];
                        if (this.inputTransformer != null) {
                           c = this.inputTransformer.applyAsChar(c);
                           charArray[i] = c;
                           transformed = true;
                        }

                        if (this.inputValidator != null && !this.inputValidator.test(c)) {
                           return false;
                        }
                     }

                     if (transformed) {
                        text = String.copyValueOf(charArray);
                     }
                  }

                  this.textField.m_94164_(text);
               } else {
                  this.textField.m_7933_(keyCode, scanCode, modifiers);
               }

               return true;
            }
         } else {
            if (this.enterHandler != null) {
               this.enterHandler.run();
            }

            return true;
         }
      } else {
         return super.m_7933_(keyCode, scanCode, modifiers);
      }
   }

   @Override
   public boolean m_5534_(char c, int keyCode) {
      if (this.canWrite()) {
         if (this.inputTransformer != null) {
            c = this.inputTransformer.applyAsChar(c);
         }

         return this.inputValidator != null && !this.inputValidator.test(c) ? false : this.textField.m_5534_(c, keyCode);
      } else {
         return super.m_5534_(c, keyCode);
      }
   }

   public String getText() {
      return this.textField.m_94155_();
   }

   public void setVisible(boolean visible) {
      this.textField.m_94194_(visible);
   }

   public void setMaxLength(int length) {
      this.textField.m_94199_(length);
   }

   public void setTextColor(int color) {
      this.textField.m_94202_(color);
   }

   public void setTextColorUneditable(int color) {
      this.textField.m_94205_(color);
   }

   public void setEditable(boolean enabled) {
      this.textField.m_94186_(enabled);
   }

   public void setCanLoseFocus(boolean canLoseFocus) {
      this.textField.m_94190_(canLoseFocus);
   }

   public void m_93692_(boolean focused) {
      if (this.textField.f_94097_ || focused) {
         super.m_93692_(focused);
         this.textField.m_93692_(focused);
         if (focused) {
            this.gui().focusChange(this);
         }
      }
   }

   public boolean canWrite() {
      return this.textField.m_94204_();
   }

   public void setText(String text) {
      this.textField.m_94144_(text);
   }

   public void setResponder(Consumer<String> responder) {
      this.responder = responder;
   }
}
