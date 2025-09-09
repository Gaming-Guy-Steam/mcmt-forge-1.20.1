package mekanism.client.gui.element.slot;

import java.util.function.BooleanSupplier;
import java.util.function.IntSupplier;
import java.util.function.Supplier;
import mekanism.api.text.EnumColor;
import mekanism.client.gui.IGuiWrapper;
import mekanism.client.gui.element.GuiElement;
import mekanism.client.gui.element.GuiTexturedElement;
import mekanism.client.jei.interfaces.IJEIGhostTarget;
import mekanism.client.render.MekanismRenderer;
import mekanism.common.inventory.container.slot.SlotOverlay;
import mekanism.common.inventory.warning.ISupportsWarning;
import mekanism.common.inventory.warning.WarningTracker;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.item.ItemStack;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class GuiSlot extends GuiTexturedElement implements IJEIGhostTarget, ISupportsWarning<GuiSlot> {
   private static final int INVALID_SLOT_COLOR = MekanismRenderer.getColorARGB(EnumColor.DARK_RED, 0.8F);
   public static final int DEFAULT_HOVER_COLOR = -2130706433;
   private final SlotType slotType;
   private Supplier<ItemStack> validityCheck;
   private Supplier<ItemStack> storedStackSupplier;
   private Supplier<SlotOverlay> overlaySupplier;
   @Nullable
   private BooleanSupplier warningSupplier;
   @Nullable
   private IntSupplier overlayColorSupplier;
   @Nullable
   private SlotOverlay overlay;
   @Nullable
   private GuiElement.IHoverable onHover;
   @Nullable
   private GuiElement.IClickable onClick;
   private boolean renderHover;
   private boolean renderAboveSlots;
   @Nullable
   private IJEIGhostTarget.IGhostIngredientConsumer ghostHandler;

   public GuiSlot(SlotType type, IGuiWrapper gui, int x, int y) {
      super(type.getTexture(), gui, x, y, type.getWidth(), type.getHeight());
      this.slotType = type;
      this.f_93623_ = false;
   }

   public GuiSlot validity(Supplier<ItemStack> validityCheck) {
      this.validityCheck = validityCheck;
      return this;
   }

   public GuiSlot warning(@NotNull WarningTracker.WarningType type, @NotNull BooleanSupplier warningSupplier) {
      this.warningSupplier = ISupportsWarning.compound(this.warningSupplier, this.gui().trackWarning(type, warningSupplier));
      return this;
   }

   public GuiSlot stored(Supplier<ItemStack> storedStackSupplier) {
      this.storedStackSupplier = storedStackSupplier;
      return this;
   }

   public GuiSlot hover(GuiElement.IHoverable onHover) {
      this.onHover = onHover;
      return this;
   }

   public GuiSlot click(GuiElement.IClickable onClick) {
      return this.click(onClick, SoundEvents.f_12490_);
   }

   public GuiSlot click(GuiElement.IClickable onClick, @Nullable Supplier<SoundEvent> clickSound) {
      this.clickSound = clickSound;
      this.onClick = onClick;
      return this;
   }

   public GuiSlot with(SlotOverlay overlay) {
      this.overlay = overlay;
      return this;
   }

   public GuiSlot overlayColor(IntSupplier colorSupplier) {
      this.overlayColorSupplier = colorSupplier;
      return this;
   }

   public GuiSlot with(Supplier<SlotOverlay> overlaySupplier) {
      this.overlaySupplier = overlaySupplier;
      return this;
   }

   public GuiSlot setRenderHover(boolean renderHover) {
      this.renderHover = renderHover;
      return this;
   }

   public GuiSlot setGhostHandler(@Nullable IJEIGhostTarget.IGhostIngredientConsumer ghostHandler) {
      this.ghostHandler = ghostHandler;
      return this;
   }

   public GuiSlot setRenderAboveSlots() {
      this.renderAboveSlots = true;
      return this;
   }

   @Override
   public void m_87963_(@NotNull GuiGraphics guiGraphics, int mouseX, int mouseY, float partialTicks) {
      if (!this.renderAboveSlots) {
         this.draw(guiGraphics);
      }
   }

   @Override
   public void drawBackground(@NotNull GuiGraphics guiGraphics, int mouseX, int mouseY, float partialTicks) {
      if (this.renderAboveSlots) {
         this.draw(guiGraphics);
      }
   }

   private void draw(@NotNull GuiGraphics guiGraphics) {
      ResourceLocation texture;
      if (this.warningSupplier != null && this.warningSupplier.getAsBoolean()) {
         texture = this.slotType.getWarningTexture();
      } else {
         texture = this.getResource();
      }

      guiGraphics.m_280163_(texture, this.relativeX, this.relativeY, 0.0F, 0.0F, this.f_93618_, this.f_93619_, this.f_93618_, this.f_93619_);
      if (this.overlaySupplier != null) {
         this.overlay = this.overlaySupplier.get();
      }

      if (this.overlay != null) {
         guiGraphics.m_280163_(
            this.overlay.getTexture(),
            this.relativeX,
            this.relativeY,
            0.0F,
            0.0F,
            this.overlay.getWidth(),
            this.overlay.getHeight(),
            this.overlay.getWidth(),
            this.overlay.getHeight()
         );
      }

      this.drawContents(guiGraphics);
   }

   protected void drawContents(@NotNull GuiGraphics guiGraphics) {
      if (this.validityCheck != null) {
         ItemStack invalid = this.validityCheck.get();
         if (!invalid.m_41619_()) {
            int xPos = this.relativeX + 1;
            int yPos = this.relativeY + 1;
            guiGraphics.m_280509_(xPos, yPos, xPos + 16, yPos + 16, INVALID_SLOT_COLOR);
            this.gui().renderItem(guiGraphics, invalid, xPos, yPos);
         }
      } else if (this.storedStackSupplier != null) {
         ItemStack stored = this.storedStackSupplier.get();
         if (!stored.m_41619_()) {
            this.gui().renderItem(guiGraphics, stored, this.relativeX + 1, this.relativeY + 1);
         }
      }
   }

   @Override
   public void renderForeground(GuiGraphics guiGraphics, int mouseX, int mouseY) {
      boolean hovered = this.checkWindows(mouseX, mouseY, this.m_274382_());
      if (this.renderHover && hovered) {
         int xPos = this.relativeX + 1;
         int yPos = this.relativeY + 1;
         guiGraphics.m_285944_(RenderType.m_286086_(), xPos, yPos, xPos + 16, yPos + 16, -2130706433);
      }

      if (this.overlayColorSupplier != null) {
         int xPos = this.relativeX + 1;
         int yPos = this.relativeY + 1;
         guiGraphics.m_285944_(RenderType.m_286086_(), xPos, yPos, xPos + 16, yPos + 16, this.overlayColorSupplier.getAsInt());
      }

      if (hovered) {
         this.renderToolTip(guiGraphics, mouseX - this.getGuiLeft(), mouseY - this.getGuiTop());
      }
   }

   @Override
   public void renderToolTip(@NotNull GuiGraphics guiGraphics, int mouseX, int mouseY) {
      super.renderToolTip(guiGraphics, mouseX, mouseY);
      if (this.onHover != null) {
         this.onHover.onHover(this, guiGraphics, mouseX, mouseY);
      }
   }

   @Nullable
   @Override
   public GuiElement mouseClickedNested(double mouseX, double mouseY, int button) {
      if (this.onClick != null
         && this.m_7972_(button)
         && mouseX >= this.m_252754_() + this.borderSize()
         && mouseY >= this.m_252907_() + this.borderSize()
         && mouseX < this.m_252754_() + this.f_93618_ - this.borderSize()
         && mouseY < this.m_252907_() + this.f_93619_ - this.borderSize()
         && this.onClick.onClick(this, (int)mouseX, (int)mouseY)) {
         this.m_7435_(minecraft.m_91106_());
         return this;
      } else {
         return super.mouseClickedNested(mouseX, mouseY, button);
      }
   }

   @Nullable
   @Override
   public IJEIGhostTarget.IGhostIngredientConsumer getGhostHandler() {
      return this.ghostHandler;
   }

   @Override
   public int borderSize() {
      return 1;
   }
}
