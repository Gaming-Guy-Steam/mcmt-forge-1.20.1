package mekanism.client.gui.element.custom;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import mekanism.api.text.APILang;
import mekanism.api.text.EnumColor;
import mekanism.api.text.TextComponentUtil;
import mekanism.client.gui.IGuiWrapper;
import mekanism.client.gui.element.GuiElement;
import mekanism.client.gui.element.button.ColorButton;
import mekanism.client.gui.element.button.MekanismButton;
import mekanism.client.gui.element.button.TranslationButton;
import mekanism.client.gui.element.scroll.GuiTextScrollList;
import mekanism.client.gui.element.slot.GuiSlot;
import mekanism.client.gui.element.slot.SlotType;
import mekanism.client.gui.element.text.BackgroundType;
import mekanism.client.gui.element.text.GuiTextField;
import mekanism.client.gui.element.window.GuiConfirmationDialog;
import mekanism.common.Mekanism;
import mekanism.common.MekanismLang;
import mekanism.common.inventory.container.item.FrequencyItemContainer;
import mekanism.common.lib.frequency.Frequency;
import mekanism.common.lib.frequency.FrequencyType;
import mekanism.common.lib.frequency.IColorableFrequency;
import mekanism.common.lib.frequency.IFrequencyHandler;
import mekanism.common.network.to_server.PacketGuiSetFrequency;
import mekanism.common.network.to_server.PacketGuiSetFrequencyColor;
import mekanism.common.tile.base.TileEntityMekanism;
import mekanism.common.util.text.InputValidator;
import mekanism.common.util.text.OwnerDisplay;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.network.chat.MutableComponent;
import org.jetbrains.annotations.Nullable;

public class GuiFrequencySelector<FREQ extends Frequency> extends GuiElement {
   private final GuiFrequencySelector.IGuiFrequencySelector<FREQ> frequencySelector;
   private final MekanismButton publicButton;
   private final MekanismButton privateButton;
   private final MekanismButton setButton;
   private final MekanismButton deleteButton;
   private final GuiTextScrollList scrollList;
   private final GuiTextField frequencyField;
   private final int yStart;
   private List<FREQ> lastFrequencies = Collections.emptyList();
   private boolean publicFreq = true;
   private boolean init;

   public <SELECTOR extends IGuiWrapper & GuiFrequencySelector.IGuiFrequencySelector<FREQ>> GuiFrequencySelector(SELECTOR frequencySelector, int yStart) {
      super(frequencySelector, 27, yStart, 132, 121);
      this.frequencySelector = frequencySelector;
      this.yStart = yStart;
      boolean hasColor = frequencySelector instanceof GuiFrequencySelector.IGuiColorFrequencySelector;
      this.scrollList = this.addChild(new GuiTextScrollList(frequencySelector, 27, yStart + 22, 122, 42));
      this.publicButton = this.addChild(new TranslationButton(frequencySelector, 27, yStart, 60, 20, APILang.PUBLIC, () -> {
         this.publicFreq = true;
         this.scrollList.clearSelection();
         this.updateButtons();
      }));
      this.privateButton = this.addChild(new TranslationButton(frequencySelector, 89, yStart, 60, 20, APILang.PRIVATE, () -> {
         this.publicFreq = false;
         this.scrollList.clearSelection();
         this.updateButtons();
      }));
      int buttonWidth = hasColor ? 50 : 60;
      this.setButton = this.addChild(new TranslationButton(frequencySelector, 27, yStart + 113, buttonWidth, 18, MekanismLang.BUTTON_SET, () -> {
         int selection = this.scrollList.getSelection();
         if (selection != -1) {
            Frequency frequency = this.getFrequencies().get(selection);
            this.setFrequency(frequency.getName());
         }

         this.updateButtons();
      }));
      this.deleteButton = this.addChild(
         new TranslationButton(
            frequencySelector,
            29 + buttonWidth,
            yStart + 113,
            buttonWidth,
            18,
            MekanismLang.BUTTON_DELETE,
            () -> GuiConfirmationDialog.show(this.gui(), MekanismLang.FREQUENCY_DELETE_CONFIRM.translate(new Object[0]), () -> {
               int selection = this.scrollList.getSelection();
               if (selection != -1) {
                  Frequency frequency = this.getFrequencies().get(selection);
                  this.frequencySelector.sendRemoveFrequency(frequency.getIdentity());
                  this.scrollList.clearSelection();
               }

               this.updateButtons();
            }, GuiConfirmationDialog.DialogType.DANGER)
         )
      );
      if (hasColor) {
         this.addChild(new GuiSlot(SlotType.NORMAL, frequencySelector, 131, yStart + 113).setRenderAboveSlots());
         GuiFrequencySelector.IGuiColorFrequencySelector<?> colorFrequencySelector = (GuiFrequencySelector.IGuiColorFrequencySelector<?>)frequencySelector;
         this.addChild(new ColorButton(frequencySelector, 132, yStart + 114, 16, 16, () -> {
            IColorableFrequency frequency = (IColorableFrequency)colorFrequencySelector.getFrequency();
            return frequency == null ? null : frequency.getColor();
         }, () -> colorFrequencySelector.sendColorUpdate(true), () -> colorFrequencySelector.sendColorUpdate(false)));
      }

      this.frequencyField = this.addChild(new GuiTextField(frequencySelector, 50, yStart + 99, 98, 11));
      this.frequencyField.setMaxLength(16);
      this.frequencyField.setBackground(BackgroundType.INNER_SCREEN);
      this.frequencyField.setEnterHandler(this::setFrequency);
      this.frequencyField.setInputValidator(InputValidator.LETTER_OR_DIGIT.or(InputValidator.FREQUENCY_CHARS));
      this.frequencyField.addCheckmarkButton(this::setFrequency);
      this.publicButton.f_93623_ = false;
      this.setButton.f_93623_ = false;
      this.deleteButton.f_93623_ = false;
   }

   @Override
   public void tick() {
      super.tick();
      if (!this.init) {
         this.init = true;
         FREQ frequency = this.frequencySelector.getFrequency();
         if (frequency != null) {
            this.publicFreq = frequency.isPublic();
         }
      }

      this.updateButtons();
   }

   private void updateButtons() {
      List<FREQ> frequencies = this.getFrequencies();
      if (this.lastFrequencies != frequencies) {
         this.lastFrequencies = frequencies;
         List<String> text = new ArrayList<>(frequencies.size());

         for (Frequency freq : frequencies) {
            if (this.publicFreq) {
               text.add(freq.getName() + " (" + freq.getClientOwner() + ")");
            } else {
               text.add(freq.getName());
            }
         }

         this.scrollList.setText(text);
      }

      if (this.publicFreq) {
         this.publicButton.f_93623_ = false;
         this.privateButton.f_93623_ = true;
      } else {
         this.publicButton.f_93623_ = true;
         this.privateButton.f_93623_ = false;
      }

      if (this.scrollList.hasSelection()) {
         FREQ selectedFrequency = frequencies.get(this.scrollList.getSelection());
         FREQ currentFrequency = this.frequencySelector.getFrequency();
         this.setButton.f_93623_ = currentFrequency == null || !currentFrequency.equals(selectedFrequency);
         this.deleteButton.f_93623_ = Minecraft.m_91087_().f_91074_ != null && selectedFrequency.ownerMatches(Minecraft.m_91087_().f_91074_.m_20148_());
      } else {
         this.setButton.f_93623_ = false;
         this.deleteButton.f_93623_ = false;
      }

      this.frequencySelector.buttonsUpdated();
   }

   private List<FREQ> getFrequencies() {
      return this.publicFreq ? this.frequencySelector.getPublicFrequencies() : this.frequencySelector.getPrivateFrequencies();
   }

   private void setFrequency() {
      this.setFrequency(this.frequencyField.getText());
      this.frequencyField.setText("");
      this.updateButtons();
   }

   private void setFrequency(String freq) {
      if (!freq.isEmpty()) {
         this.frequencySelector.sendSetFrequency(new Frequency.FrequencyIdentity(freq, this.publicFreq));
      }
   }

   @Override
   public void renderForeground(GuiGraphics guiGraphics, int mouseX, int mouseY) {
      super.renderForeground(guiGraphics, mouseX, mouseY);
      FREQ frequency = this.frequencySelector.getFrequency();
      if (frequency == null) {
         MutableComponent noneComponent = MekanismLang.NONE.translateColored(EnumColor.DARK_RED, new Object[0]);
         this.drawString(guiGraphics, MekanismLang.FREQUENCY.translate(new Object[]{noneComponent}), 27, this.yStart + 67, this.titleTextColor());
         this.drawString(guiGraphics, MekanismLang.OWNER.translate(new Object[]{noneComponent}), 27, this.yStart + 77, this.titleTextColor());
         this.drawString(guiGraphics, MekanismLang.SECURITY.translate(new Object[]{noneComponent}), 27, this.yStart + 87, this.titleTextColor());
      } else {
         MutableComponent name = TextComponentUtil.color(TextComponentUtil.getString(frequency.getName()), this.subheadingTextColor());
         this.drawTextScaledBound(
            guiGraphics, MekanismLang.FREQUENCY.translate(new Object[]{name}), 27.0F, this.yStart + 67, this.titleTextColor(), this.getGuiWidth() - 36
         );
         this.drawString(
            guiGraphics,
            OwnerDisplay.of(Minecraft.m_91087_().f_91074_, frequency.getOwner(), frequency.getClientOwner(), false).getTextComponent(),
            27,
            this.yStart + 77,
            this.titleTextColor()
         );
         this.drawString(guiGraphics, MekanismLang.SECURITY.translate(new Object[]{frequency.getSecurity()}), 27, this.yStart + 87, this.titleTextColor());
      }

      this.drawTextScaledBound(guiGraphics, MekanismLang.SET.translate(new Object[0]), 27.0F, this.yStart + 100, this.titleTextColor(), 20.0F);
   }

   public interface IGuiColorFrequencySelector<FREQ extends Frequency & IColorableFrequency> extends GuiFrequencySelector.IGuiFrequencySelector<FREQ> {
      default void sendColorUpdate(boolean next) {
         FREQ freq = (FREQ)this.getFrequency();
         if (freq != null) {
            Mekanism.packetHandler().sendToServer(PacketGuiSetFrequencyColor.create(freq, next));
         }
      }
   }

   public interface IGuiFrequencySelector<FREQ extends Frequency> {
      FrequencyType<FREQ> getFrequencyType();

      void sendSetFrequency(Frequency.FrequencyIdentity identity);

      void sendRemoveFrequency(Frequency.FrequencyIdentity identity);

      @Nullable
      FREQ getFrequency();

      List<FREQ> getPublicFrequencies();

      List<FREQ> getPrivateFrequencies();

      default void buttonsUpdated() {
      }
   }

   public interface IItemGuiFrequencySelector<FREQ extends Frequency, CONTAINER extends FrequencyItemContainer<FREQ>>
      extends GuiFrequencySelector.IGuiFrequencySelector<FREQ> {
      CONTAINER getFrequencyContainer();

      @Override
      default void sendSetFrequency(Frequency.FrequencyIdentity identity) {
         Mekanism.packetHandler()
            .sendToServer(
               PacketGuiSetFrequency.create(
                  PacketGuiSetFrequency.FrequencyUpdate.SET_ITEM, this.getFrequencyType(), identity, this.getFrequencyContainer().getHand()
               )
            );
      }

      @Override
      default void sendRemoveFrequency(Frequency.FrequencyIdentity identity) {
         Mekanism.packetHandler()
            .sendToServer(
               PacketGuiSetFrequency.create(
                  PacketGuiSetFrequency.FrequencyUpdate.REMOVE_ITEM, this.getFrequencyType(), identity, this.getFrequencyContainer().getHand()
               )
            );
      }

      @Override
      default FREQ getFrequency() {
         return this.getFrequencyContainer().getFrequency();
      }

      @Override
      default List<FREQ> getPublicFrequencies() {
         return this.getFrequencyContainer().getPublicCache();
      }

      @Override
      default List<FREQ> getPrivateFrequencies() {
         return this.getFrequencyContainer().getPrivateCache();
      }
   }

   public interface ITileGuiFrequencySelector<FREQ extends Frequency, TILE extends TileEntityMekanism & IFrequencyHandler>
      extends GuiFrequencySelector.IGuiFrequencySelector<FREQ> {
      TILE getTileEntity();

      @Override
      default void sendSetFrequency(Frequency.FrequencyIdentity identity) {
         Mekanism.packetHandler()
            .sendToServer(
               PacketGuiSetFrequency.create(PacketGuiSetFrequency.FrequencyUpdate.SET_TILE, this.getFrequencyType(), identity, this.getTileEntity().m_58899_())
            );
      }

      @Override
      default void sendRemoveFrequency(Frequency.FrequencyIdentity identity) {
         Mekanism.packetHandler()
            .sendToServer(
               PacketGuiSetFrequency.create(
                  PacketGuiSetFrequency.FrequencyUpdate.REMOVE_TILE, this.getFrequencyType(), identity, this.getTileEntity().m_58899_()
               )
            );
      }

      @Override
      default FREQ getFrequency() {
         return this.getTileEntity().getFrequency(this.getFrequencyType());
      }

      @Override
      default List<FREQ> getPublicFrequencies() {
         return this.getTileEntity().getPublicCache(this.getFrequencyType());
      }

      @Override
      default List<FREQ> getPrivateFrequencies() {
         return this.getTileEntity().getPrivateCache(this.getFrequencyType());
      }
   }
}
