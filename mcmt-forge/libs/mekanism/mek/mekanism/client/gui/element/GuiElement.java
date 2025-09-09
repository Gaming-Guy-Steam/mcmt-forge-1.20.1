package mekanism.client.gui.element;

import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.PoseStack;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Predicate;
import java.util.function.Supplier;
import mekanism.api.text.ILangEntry;
import mekanism.client.gui.GuiMekanism;
import mekanism.client.gui.GuiUtils;
import mekanism.client.gui.IGuiWrapper;
import mekanism.client.gui.element.window.GuiWindow;
import mekanism.client.render.IFancyFontRenderer;
import mekanism.client.render.MekanismRenderer;
import mekanism.common.util.MekanismUtils;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.AbstractWidget;
import net.minecraft.client.gui.narration.NarrationElementOutput;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.client.resources.sounds.SimpleSoundInstance;
import net.minecraft.client.sounds.SoundManager;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.util.Mth;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public abstract class GuiElement extends AbstractWidget implements IFancyFontRenderer {
   private static final int BUTTON_TEX_X = 200;
   private static final int BUTTON_TEX_Y = 60;
   private static final int BUTTON_INDIVIDUAL_TEX_Y = 20;
   public static final ResourceLocation WARNING_BACKGROUND_TEXTURE = MekanismUtils.getResource(MekanismUtils.ResourceType.GUI, "warning_background.png");
   public static final ResourceLocation WARNING_TEXTURE = MekanismUtils.getResource(MekanismUtils.ResourceType.GUI, "warning.png");
   public static final Minecraft minecraft = Minecraft.m_91087_();
   protected GuiElement.ButtonBackground buttonBackground = GuiElement.ButtonBackground.NONE;
   protected final List<GuiElement> children = new ArrayList<>();
   private final List<GuiElement> positionOnlyChildren = new ArrayList<>();
   private IGuiWrapper guiObj;
   @Nullable
   protected Supplier<SoundEvent> clickSound;
   protected int relativeX;
   protected int relativeY;
   public boolean isOverlay;

   public GuiElement(IGuiWrapper gui, int x, int y, int width, int height) {
      this(gui, x, y, width, height, Component.m_237119_());
   }

   public GuiElement(IGuiWrapper gui, int x, int y, int width, int height, Component text) {
      super(gui.getLeft() + x, gui.getTop() + y, width, height, text);
      this.relativeX = x;
      this.relativeY = y;
      this.guiObj = gui;
   }

   public void m_168797_(@NotNull NarrationElementOutput output) {
   }

   public int getRelativeX() {
      return this.relativeX;
   }

   public int getRelativeY() {
      return this.relativeY;
   }

   public void transferToNewGui(IGuiWrapper gui) {
      int prevLeft = this.getGuiLeft();
      int prevTop = this.getGuiTop();
      this.transferToNewGuiInternal(gui);
      this.resize(prevLeft, prevTop, this.getGuiLeft(), this.getGuiTop());
   }

   private void transferToNewGuiInternal(IGuiWrapper gui) {
      this.guiObj = gui;
      this.children.forEach(child -> child.transferToNewGuiInternal(gui));
      this.positionOnlyChildren.forEach(child -> child.transferToNewGuiInternal(gui));
   }

   protected <ELEMENT extends GuiElement> ELEMENT addChild(ELEMENT element) {
      this.children.add(element);
      if (this.isOverlay) {
         element.isOverlay = true;
      }

      return element;
   }

   protected <ELEMENT extends GuiElement> ELEMENT addPositionOnlyChild(ELEMENT element) {
      this.positionOnlyChildren.add(element);
      return element;
   }

   public final IGuiWrapper gui() {
      return this.guiObj;
   }

   public final int getGuiLeft() {
      return this.guiObj.getLeft();
   }

   public final int getGuiTop() {
      return this.guiObj.getTop();
   }

   public final int getGuiWidth() {
      return this.guiObj.getWidth();
   }

   public final int getGuiHeight() {
      return this.guiObj.getHeight();
   }

   public List<GuiElement> children() {
      return this.children;
   }

   public void tick() {
      this.children.forEach(GuiElement::tick);
   }

   public void resize(int prevLeft, int prevTop, int left, int top) {
      this.m_252865_(this.m_252754_() - prevLeft + left);
      this.m_253211_(this.m_252907_() - prevTop + top);
      this.children.forEach(child -> child.resize(prevLeft, prevTop, left, top));
      this.positionOnlyChildren.forEach(child -> child.resize(prevLeft, prevTop, left, top));
   }

   public boolean childrenContainsElement(Predicate<GuiElement> checker) {
      return this.children.stream().anyMatch(e -> e.containsElement(checker));
   }

   public boolean containsElement(Predicate<GuiElement> checker) {
      return checker.test(this) || this.childrenContainsElement(checker);
   }

   public void move(int changeX, int changeY) {
      this.m_252865_(this.m_252754_() + changeX);
      this.m_253211_(this.m_252907_() + changeY);
      this.relativeX += changeX;
      this.relativeY += changeY;
      this.children.forEach(child -> child.move(changeX, changeY));
      this.positionOnlyChildren.forEach(child -> child.move(changeX, changeY));
   }

   public void onWindowClose() {
      this.children.forEach(GuiElement::onWindowClose);
   }

   protected ResourceLocation getButtonLocation(String name) {
      return MekanismUtils.getResource(MekanismUtils.ResourceType.GUI_BUTTON, name + ".png");
   }

   protected GuiElement.IHoverable getOnHover(ILangEntry translationHelper) {
      return this.getOnHover(() -> translationHelper.translate());
   }

   protected GuiElement.IHoverable getOnHover(Supplier<Component> componentSupplier) {
      return (onHover, guiGraphics, mouseX, mouseY) -> this.displayTooltips(guiGraphics, mouseX, mouseY, componentSupplier.get());
   }

   public boolean hasPersistentData() {
      return this.children.stream().anyMatch(GuiElement::hasPersistentData);
   }

   public void syncFrom(GuiElement element) {
      int numChildren = this.children.size();
      if (numChildren > 0) {
         for (int i = 0; i < element.children.size(); i++) {
            GuiElement prevChild = element.children.get(i);
            if (prevChild.hasPersistentData() && i < numChildren) {
               GuiElement child = this.children.get(i);
               if (child.getClass() == prevChild.getClass()) {
                  child.syncFrom(prevChild);
               }
            }
         }
      }
   }

   public final void onRenderForeground(GuiGraphics guiGraphics, int mouseX, int mouseY, int zOffset, int totalOffset) {
      if (this.f_93624_) {
         PoseStack pose = guiGraphics.m_280168_();
         pose.m_252880_(0.0F, 0.0F, zOffset);
         GuiMekanism.maxZOffset = Math.max(totalOffset, GuiMekanism.maxZOffset);
         this.renderBackgroundOverlay(guiGraphics, mouseX, mouseY);
         this.children.forEach(child -> child.renderShifted(guiGraphics, mouseX, mouseY, 0.0F));
         this.children.forEach(child -> child.onDrawBackground(guiGraphics, mouseX, mouseY, 0.0F));
         this.renderForeground(guiGraphics, mouseX, mouseY);
         this.children.forEach(child -> {
            pose.m_85836_();
            child.onRenderForeground(guiGraphics, mouseX, mouseY, 50, totalOffset + 50);
            pose.m_85849_();
         });
      }
   }

   public void renderForeground(GuiGraphics guiGraphics, int mouseX, int mouseY) {
      this.drawButtonText(guiGraphics, mouseX, mouseY);
   }

   public void renderBackgroundOverlay(GuiGraphics guiGraphics, int mouseX, int mouseY) {
   }

   public void renderToolTip(@NotNull GuiGraphics guiGraphics, int mouseX, int mouseY) {
      this.children.stream().filter(child -> child.m_5953_(mouseX, mouseY)).forEach(child -> child.renderToolTip(guiGraphics, mouseX, mouseY));
   }

   public void displayTooltips(GuiGraphics guiGraphics, int mouseX, int mouseY, Component... components) {
      this.guiObj.displayTooltips(guiGraphics, mouseX, mouseY, components);
   }

   public void displayTooltips(GuiGraphics guiGraphics, int mouseX, int mouseY, List<Component> components) {
      this.guiObj.displayTooltips(guiGraphics, mouseX, mouseY, components);
   }

   @Nullable
   public GuiElement mouseClickedNested(double mouseX, double mouseY, int button) {
      for (int i = this.children.size() - 1; i >= 0; i--) {
         GuiElement child = this.children.get(i);
         GuiElement childResult = child.mouseClickedNested(mouseX, mouseY, button);
         if (childResult != null) {
            return childResult;
         }
      }

      if (this.f_93623_ && this.f_93624_ && this.m_7972_(button) && this.m_93680_(mouseX, mouseY)) {
         this.m_7435_(minecraft.m_91106_());
         this.onClick(mouseX, mouseY, button);
         return this;
      } else {
         return null;
      }
   }

   public final boolean m_6375_(double mouseX, double mouseY, int button) {
      return this.mouseClickedNested(mouseX, mouseY, button) != null;
   }

   public final void m_5716_(double mouseX, double mouseY) {
      this.onClick(mouseX, mouseY, 0);
   }

   public void onClick(double mouseX, double mouseY, int button) {
      super.m_5716_(mouseX, mouseY);
   }

   public boolean m_7933_(int keyCode, int scanCode, int modifiers) {
      return GuiUtils.checkChildren(this.children, child -> child.m_7933_(keyCode, scanCode, modifiers)) || super.m_7933_(keyCode, scanCode, modifiers);
   }

   public boolean m_5534_(char c, int keyCode) {
      return GuiUtils.checkChildren(this.children, child -> child.m_5534_(c, keyCode)) || super.m_5534_(c, keyCode);
   }

   public void m_7212_(double mouseX, double mouseY, double deltaX, double deltaY) {
      this.children.forEach(element -> element.m_7212_(mouseX, mouseY, deltaX, deltaY));
      super.m_7212_(mouseX, mouseY, deltaX, deltaY);
   }

   public void m_7691_(double mouseX, double mouseY) {
      this.children.forEach(element -> element.m_7691_(mouseX, mouseY));
      super.m_7691_(mouseX, mouseY);
   }

   public boolean m_6050_(double mouseX, double mouseY, double delta) {
      return GuiUtils.checkChildren(this.children, child -> child.m_6050_(mouseX, mouseY, delta)) || super.m_6050_(mouseX, mouseY, delta);
   }

   @Override
   public Font getFont() {
      return this.guiObj.getFont();
   }

   @Override
   public int getXSize() {
      return this.f_93618_;
   }

   public void setButtonBackground(GuiElement.ButtonBackground buttonBackground) {
      this.buttonBackground = buttonBackground;
   }

   protected boolean m_93680_(double mouseX, double mouseY) {
      return this.m_5953_(mouseX, mouseY);
   }

   protected int getButtonX() {
      return this.relativeX;
   }

   protected int getButtonY() {
      return this.relativeY;
   }

   protected int getButtonWidth() {
      return this.f_93618_;
   }

   protected int getButtonHeight() {
      return this.f_93619_;
   }

   protected boolean resetColorBeforeRender() {
      return true;
   }

   public boolean m_5953_(double mouseX, double mouseY) {
      return super.m_5953_(mouseX, mouseY) || GuiUtils.checkChildren(this.children, child -> child.m_5953_(mouseX, mouseY));
   }

   public final boolean isMouseOverCheckWindows(double mouseX, double mouseY) {
      boolean isHovering = this.m_5953_(mouseX, mouseY);
      return this.checkWindows(mouseX, mouseY, isHovering);
   }

   protected final boolean checkWindows(double mouseX, double mouseY) {
      return this.checkWindows(mouseX, mouseY, true);
   }

   protected final boolean checkWindows(double mouseX, double mouseY, boolean isHovering) {
      if (isHovering) {
         GuiWindow window = this.guiObj.getWindowHovering(mouseX, mouseY);
         if (window != null && !window.childrenContainsElement(e -> e == this)) {
            isHovering = false;
         }
      }

      return isHovering;
   }

   public void drawBackground(@NotNull GuiGraphics guiGraphics, int mouseX, int mouseY, float partialTicks) {
      if (this.buttonBackground != GuiElement.ButtonBackground.NONE) {
         this.drawButton(guiGraphics, mouseX, mouseY);
      }
   }

   public final void onDrawBackground(@NotNull GuiGraphics guiGraphics, int mouseX, int mouseY, float partialTicks) {
      if (this.f_93624_) {
         this.drawBackground(guiGraphics, mouseX, mouseY, partialTicks);
      }
   }

   public final void renderShifted(@NotNull GuiGraphics guiGraphics, int mouseX, int mouseY, float partialTicks) {
      super.m_88315_(guiGraphics, mouseX, mouseY, partialTicks);
   }

   public void m_88315_(@NotNull GuiGraphics guiGraphics, int mouseX, int mouseY, float partialTicks) {
      if (this.f_93624_) {
         PoseStack pose = guiGraphics.m_280168_();
         pose.m_85836_();
         pose.m_252880_(this.getGuiLeft(), this.getGuiTop(), 0.0F);
         this.renderShifted(guiGraphics, mouseX, mouseY, partialTicks);
         pose.m_85849_();
      }
   }

   public void m_87963_(@NotNull GuiGraphics guiGraphics, int mouseX, int mouseY, float partialTicks) {
   }

   public int getFGColor() {
      if (this.packedFGColor != -1) {
         return this.packedFGColor;
      } else {
         return this.f_93623_ ? this.activeButtonTextColor() : this.inactiveButtonTextColor();
      }
   }

   protected int getButtonTextColor(int mouseX, int mouseY) {
      return this.getFGColor();
   }

   protected void drawButtonText(GuiGraphics guiGraphics, int mouseX, int mouseY) {
      Component text = this.m_6035_();
      if (!text.getString().isEmpty()) {
         int color = this.getButtonTextColor(mouseX, mouseY) | Mth.m_14167_(this.f_93625_ * 255.0F) << 24;
         this.drawCenteredTextScaledBound(guiGraphics, text, this.f_93618_ - 4, this.f_93619_ / 2.0F - 4.0F, color);
      }
   }

   protected int getButtonTextureY(boolean hoveredOrFocused) {
      if (!this.f_93623_) {
         return 0;
      } else {
         return hoveredOrFocused ? 2 : 1;
      }
   }

   protected void drawButton(GuiGraphics guiGraphics, int mouseX, int mouseY) {
      if (this.resetColorBeforeRender()) {
         MekanismRenderer.resetColor(guiGraphics);
      }

      ResourceLocation texture = this.buttonBackground.getTexture();
      RenderSystem.enableBlend();
      RenderSystem.defaultBlendFunc();
      RenderSystem.enableDepthTest();
      int i = this.getButtonTextureY(this.isMouseOverCheckWindows(mouseX, mouseY));
      GuiUtils.blitNineSlicedSized(
         guiGraphics, texture, this.getButtonX(), this.getButtonY(), this.getButtonWidth(), this.getButtonHeight(), 20, 4, 200, 20, 0, i * 20, 200, 60
      );
   }

   protected void renderExtendedTexture(GuiGraphics guiGraphics, ResourceLocation resource, int sideWidth, int sideHeight) {
      GuiUtils.renderExtendedTexture(
         guiGraphics, resource, sideWidth, sideHeight, this.getButtonX(), this.getButtonY(), this.getButtonWidth(), this.getButtonHeight()
      );
   }

   protected void renderBackgroundTexture(GuiGraphics guiGraphics, ResourceLocation resource, int sideWidth, int sideHeight) {
      GuiUtils.renderBackgroundTexture(
         guiGraphics, resource, sideWidth, sideHeight, this.getButtonX(), this.getButtonY(), this.getButtonWidth(), this.getButtonHeight(), 256, 256
      );
   }

   public void m_7435_(@NotNull SoundManager soundHandler) {
      if (this.clickSound != null) {
         playClickSound(soundHandler, this.clickSound);
      }
   }

   protected static void playClickSound(Supplier<SoundEvent> sound) {
      playClickSound(minecraft.m_91106_(), sound);
   }

   private static void playClickSound(@NotNull SoundManager soundHandler, @NotNull Supplier<SoundEvent> sound) {
      soundHandler.m_120367_(SimpleSoundInstance.m_119752_(sound.get(), 1.0F));
   }

   protected void drawTiledSprite(
      GuiGraphics guiGraphics,
      int xPosition,
      int yPosition,
      int yOffset,
      int desiredWidth,
      int desiredHeight,
      TextureAtlasSprite sprite,
      GuiUtils.TilingDirection tilingDirection
   ) {
      GuiUtils.drawTiledSprite(guiGraphics, xPosition, yPosition, yOffset, desiredWidth, desiredHeight, sprite, 16, 16, 0, tilingDirection);
   }

   @Override
   public void drawCenteredTextScaledBound(GuiGraphics guiGraphics, Component text, float maxLength, float x, float y, int color) {
      IFancyFontRenderer.super.drawCenteredTextScaledBound(guiGraphics, text, maxLength, this.relativeX + x, this.relativeY + y, color);
   }

   public static enum ButtonBackground {
      DEFAULT(MekanismUtils.getResource(MekanismUtils.ResourceType.GUI, "button.png")),
      DIGITAL(MekanismUtils.getResource(MekanismUtils.ResourceType.GUI, "button_digital.png")),
      NONE(null);

      private final ResourceLocation texture;

      private ButtonBackground(ResourceLocation texture) {
         this.texture = texture;
      }

      public ResourceLocation getTexture() {
         return this.texture;
      }
   }

   @FunctionalInterface
   public interface IClickable {
      boolean onClick(GuiElement element, int mouseX, int mouseY);
   }

   @FunctionalInterface
   public interface IHoverable {
      void onHover(GuiElement element, GuiGraphics guiGraphics, int mouseX, int mouseY);
   }
}
