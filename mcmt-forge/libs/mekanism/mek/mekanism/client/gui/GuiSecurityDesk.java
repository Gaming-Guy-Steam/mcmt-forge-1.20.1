package mekanism.client.gui;

import java.util.Collections;
import mekanism.api.security.SecurityMode;
import mekanism.api.text.EnumColor;
import mekanism.client.gui.element.GuiElementHolder;
import mekanism.client.gui.element.GuiSecurityLight;
import mekanism.client.gui.element.GuiTextureOnlyElement;
import mekanism.client.gui.element.button.MekanismButton;
import mekanism.client.gui.element.button.MekanismImageButton;
import mekanism.client.gui.element.button.TranslationButton;
import mekanism.client.gui.element.scroll.GuiTextScrollList;
import mekanism.client.gui.element.slot.GuiSlot;
import mekanism.client.gui.element.slot.SlotType;
import mekanism.client.gui.element.text.BackgroundType;
import mekanism.client.gui.element.text.GuiTextField;
import mekanism.common.Mekanism;
import mekanism.common.MekanismLang;
import mekanism.common.inventory.container.tile.MekanismTileContainer;
import mekanism.common.lib.security.SecurityFrequency;
import mekanism.common.network.to_server.PacketAddTrusted;
import mekanism.common.network.to_server.PacketGuiInteract;
import mekanism.common.tile.TileEntitySecurityDesk;
import mekanism.common.util.MekanismUtils;
import mekanism.common.util.text.BooleanStateDisplay;
import mekanism.common.util.text.InputValidator;
import mekanism.common.util.text.OwnerDisplay;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Inventory;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class GuiSecurityDesk extends GuiMekanismTile<TileEntitySecurityDesk, MekanismTileContainer<TileEntitySecurityDesk>> {
   private static final ResourceLocation PUBLIC = MekanismUtils.getResource(MekanismUtils.ResourceType.GUI, "public.png");
   private static final ResourceLocation PRIVATE = MekanismUtils.getResource(MekanismUtils.ResourceType.GUI, "private.png");
   private MekanismButton removeButton;
   private MekanismButton publicButton;
   private MekanismButton privateButton;
   private MekanismButton trustedButton;
   private MekanismButton overrideButton;
   private GuiTextScrollList scrollList;
   private GuiTextField trustedField;

   public GuiSecurityDesk(MekanismTileContainer<TileEntitySecurityDesk> container, Inventory inv, Component title) {
      super(container, inv, title);
      this.f_97727_ += 64;
      this.f_97731_ = this.f_97727_ - 94;
      this.f_97729_ = 4;
      this.dynamicSlots = true;
   }

   @Override
   protected void addGuiElements() {
      this.addRenderableWidget(new GuiElementHolder(this, 141, 13, 26, 37));
      this.addRenderableWidget(new GuiElementHolder(this, 141, 54, 26, 34));
      this.addRenderableWidget(new GuiElementHolder(this, 141, 92, 26, 37));
      super.addGuiElements();
      this.addRenderableWidget(new GuiSlot(SlotType.INNER_HOLDER_SLOT, this, 145, 17));
      this.addRenderableWidget(new GuiSlot(SlotType.INNER_HOLDER_SLOT, this, 145, 96));
      this.addRenderableWidget(new GuiSecurityLight(this, 144, 77, () -> {
         SecurityFrequency frequency = this.tile.getFreq();
         if (!this.isOwner(frequency)) {
            return 2;
         } else {
            return frequency.isOverridden() ? 0 : 1;
         }
      }));
      this.addRenderableWidget(new GuiTextureOnlyElement(PUBLIC, this, 145, 32, 18, 18));
      this.addRenderableWidget(new GuiTextureOnlyElement(PRIVATE, this, 145, 111, 18, 18));
      this.scrollList = this.addRenderableWidget(new GuiTextScrollList(this, 13, 13, 122, 42));
      this.removeButton = this.addRenderableWidget(new TranslationButton(this, 13, 81, 122, 20, MekanismLang.BUTTON_REMOVE, () -> {
         int selection = this.scrollList.getSelection();
         if (this.tile.getFreq() != null && selection != -1) {
            Mekanism.packetHandler().sendToServer(new PacketGuiInteract(PacketGuiInteract.GuiInteraction.REMOVE_TRUSTED, this.tile, selection));
            this.scrollList.clearSelection();
            this.updateButtons();
         }
      }));
      this.trustedField = this.addRenderableWidget(new GuiTextField(this, 35, 68, 99, 11));
      this.trustedField.setMaxLength(16);
      this.trustedField.setBackground(BackgroundType.INNER_SCREEN);
      this.trustedField.setEnterHandler(this::setTrusted);
      this.trustedField.setInputValidator(InputValidator.USERNAME);
      this.trustedField.addCheckmarkButton(this::setTrusted);
      this.publicButton = this.addRenderableWidget(
         new MekanismImageButton(
            this,
            13,
            113,
            40,
            16,
            40,
            16,
            this.getButtonLocation("public"),
            () -> {
               Mekanism.packetHandler()
                  .sendToServer(new PacketGuiInteract(PacketGuiInteract.GuiInteraction.SECURITY_DESK_MODE, this.tile, SecurityMode.PUBLIC.ordinal()));
               this.updateButtons();
            },
            this.getOnHover(MekanismLang.PUBLIC_MODE)
         )
      );
      this.privateButton = this.addRenderableWidget(
         new MekanismImageButton(
            this,
            54,
            113,
            40,
            16,
            40,
            16,
            this.getButtonLocation("private"),
            () -> {
               Mekanism.packetHandler()
                  .sendToServer(new PacketGuiInteract(PacketGuiInteract.GuiInteraction.SECURITY_DESK_MODE, this.tile, SecurityMode.PRIVATE.ordinal()));
               this.updateButtons();
            },
            this.getOnHover(MekanismLang.PRIVATE_MODE)
         )
      );
      this.trustedButton = this.addRenderableWidget(
         new MekanismImageButton(
            this,
            95,
            113,
            40,
            16,
            40,
            16,
            this.getButtonLocation("trusted"),
            () -> {
               Mekanism.packetHandler()
                  .sendToServer(new PacketGuiInteract(PacketGuiInteract.GuiInteraction.SECURITY_DESK_MODE, this.tile, SecurityMode.TRUSTED.ordinal()));
               this.updateButtons();
            },
            this.getOnHover(MekanismLang.TRUSTED_MODE)
         )
      );
      this.overrideButton = this.addRenderableWidget(
         new MekanismImageButton(
            this,
            146,
            59,
            16,
            16,
            this.getButtonLocation("exclamation"),
            () -> {
               Mekanism.packetHandler().sendToServer(new PacketGuiInteract(PacketGuiInteract.GuiInteraction.OVERRIDE_BUTTON, this.tile));
               this.updateButtons();
            },
            (onHover, guiGraphics, mouseX, mouseY) -> {
               SecurityFrequency frequency = this.tile.getFreq();
               if (frequency != null) {
                  this.displayTooltips(
                     guiGraphics,
                     mouseX,
                     mouseY,
                     new Component[]{MekanismLang.SECURITY_OVERRIDE.translate(new Object[]{BooleanStateDisplay.OnOff.of(frequency.isOverridden())})}
                  );
               }
            }
         )
      );
      this.updateButtons();
   }

   private boolean isOwner(@Nullable SecurityFrequency frequency) {
      return frequency != null && this.tile.ownerMatches(this.getMinecraft().f_91074_);
   }

   private void setTrusted() {
      if (this.isOwner(this.tile.getFreq())) {
         this.addTrusted(this.trustedField.getText());
         this.trustedField.setText("");
         this.updateButtons();
      }
   }

   private void addTrusted(String trusted) {
      if (PacketAddTrusted.validateNameLength(trusted.length())) {
         Mekanism.packetHandler().sendToServer(new PacketAddTrusted(this.tile.m_58899_(), trusted));
      }
   }

   private void updateButtons() {
      SecurityFrequency freq = this.tile.getFreq();
      if (this.tile.getOwnerUUID() != null) {
         this.scrollList.setText(freq == null ? Collections.emptyList() : freq.getTrustedUsernameCache());
         this.removeButton.f_93623_ = this.scrollList.hasSelection();
      }

      if (this.isOwner(freq)) {
         this.publicButton.f_93623_ = freq.getSecurityMode() != SecurityMode.PUBLIC;
         this.privateButton.f_93623_ = freq.getSecurityMode() != SecurityMode.PRIVATE;
         this.trustedButton.f_93623_ = freq.getSecurityMode() != SecurityMode.TRUSTED;
         this.overrideButton.f_93623_ = true;
      } else {
         this.publicButton.f_93623_ = false;
         this.privateButton.f_93623_ = false;
         this.trustedButton.f_93623_ = false;
         this.overrideButton.f_93623_ = false;
      }
   }

   @Override
   public void m_181908_() {
      super.m_181908_();
      this.updateButtons();
   }

   @Override
   public boolean m_6375_(double mouseX, double mouseY, int button) {
      this.updateButtons();
      return super.m_6375_(mouseX, mouseY, button);
   }

   @Override
   protected void drawForegroundText(@NotNull GuiGraphics guiGraphics, int mouseX, int mouseY) {
      this.renderTitleText(guiGraphics);
      Component ownerComponent = OwnerDisplay.of(this.tile.getOwnerUUID(), this.tile.getOwnerName()).getTextComponent();
      this.drawString(guiGraphics, ownerComponent, this.f_97726_ - 7 - this.getStringWidth(ownerComponent), this.f_97731_, this.titleTextColor());
      this.drawString(guiGraphics, this.f_169604_, this.f_97730_, this.f_97731_, this.titleTextColor());
      this.drawCenteredText(guiGraphics, MekanismLang.TRUSTED_PLAYERS.translate(new Object[0]), 74.0F, 57.0F, this.subheadingTextColor());
      SecurityFrequency frequency = this.tile.getFreq();
      if (frequency != null) {
         this.drawString(guiGraphics, MekanismLang.SECURITY.translate(new Object[]{frequency.getSecurityMode()}), 13, 103, this.titleTextColor());
      } else {
         this.drawString(guiGraphics, MekanismLang.SECURITY_OFFLINE.translateColored(EnumColor.RED, new Object[0]), 13, 103, this.titleTextColor());
      }

      this.drawTextScaledBound(guiGraphics, MekanismLang.SECURITY_ADD.translate(new Object[0]), 13.0F, 70.0F, this.titleTextColor(), 20.0F);
      super.drawForegroundText(guiGraphics, mouseX, mouseY);
   }
}
