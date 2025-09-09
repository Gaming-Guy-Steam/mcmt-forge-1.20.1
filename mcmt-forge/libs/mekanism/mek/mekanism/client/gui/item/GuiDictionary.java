package mekanism.client.gui.item;

import java.util.Set;
import mekanism.api.text.ILangEntry;
import mekanism.client.gui.GuiMekanism;
import mekanism.client.gui.element.GuiDropdown;
import mekanism.client.gui.element.custom.GuiDictionaryTarget;
import mekanism.client.gui.element.scroll.GuiTextScrollList;
import mekanism.client.gui.element.slot.GuiSlot;
import mekanism.client.gui.element.slot.SlotType;
import mekanism.common.MekanismLang;
import mekanism.common.inventory.GuiComponents;
import mekanism.common.inventory.container.item.DictionaryContainer;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;
import org.jetbrains.annotations.NotNull;

public class GuiDictionary extends GuiMekanism<DictionaryContainer> {
   private GuiTextScrollList scrollList;
   private GuiDictionaryTarget target;
   private GuiDictionary.DictionaryTagType currentType = GuiDictionary.DictionaryTagType.ITEM;

   public GuiDictionary(DictionaryContainer container, Inventory inv, Component title) {
      super(container, inv, title);
      this.f_97727_ += 5;
      this.f_97731_ = this.f_97727_ - 96;
      this.f_97729_ = 5;
      this.dynamicSlots = true;
   }

   @Override
   protected void addGuiElements() {
      super.addGuiElements();
      this.addRenderableWidget(new GuiSlot(SlotType.NORMAL, this, 5, 5).setRenderHover(true));
      this.scrollList = this.addRenderableWidget(new GuiTextScrollList(this, 7, 29, 162, 42));
      this.addRenderableWidget(new GuiDropdown<>(this, 124, 73, 45, GuiDictionary.DictionaryTagType.class, () -> this.currentType, this::setCurrentType));
      this.target = this.addRenderableWidget(new GuiDictionaryTarget(this, 6, 6, this::updateScrollList));
   }

   private void setCurrentType(GuiDictionary.DictionaryTagType type) {
      this.currentType = type;
      this.scrollList.setText(this.target.getTags(this.currentType));
   }

   private void updateScrollList(Set<GuiDictionary.DictionaryTagType> supportedTypes) {
      if (!supportedTypes.contains(this.currentType) && !supportedTypes.isEmpty()) {
         this.currentType = supportedTypes.stream().findFirst().orElse(this.currentType);
      }

      this.scrollList.setText(this.target.getTags(this.currentType));
   }

   @Override
   protected void drawForegroundText(@NotNull GuiGraphics guiGraphics, int mouseX, int mouseY) {
      this.renderTitleText(guiGraphics);
      this.drawString(guiGraphics, this.f_169604_, this.f_97730_, this.f_97731_, this.titleTextColor());
      this.drawTextScaledBound(guiGraphics, MekanismLang.DICTIONARY_TAG_TYPE.translate(new Object[0]), 77.0F, this.f_97731_, this.titleTextColor(), 45.0F);
      super.drawForegroundText(guiGraphics, mouseX, mouseY);
   }

   @Override
   public boolean m_6375_(double mouseX, double mouseY, int button) {
      if (button == 0 && m_96638_() && !this.target.hasTarget()) {
         Slot slot = this.getSlotUnderMouse();
         if (slot != null) {
            ItemStack stack = slot.m_7993_();
            if (!stack.m_41619_()) {
               this.target.setTargetSlot(stack);
               return true;
            }
         }
      }

      return super.m_6375_(mouseX, mouseY, button);
   }

   public static enum DictionaryTagType implements GuiComponents.IDropdownEnum<GuiDictionary.DictionaryTagType> {
      ITEM(MekanismLang.DICTIONARY_ITEM, MekanismLang.DICTIONARY_ITEM_DESC),
      BLOCK(MekanismLang.DICTIONARY_BLOCK, MekanismLang.DICTIONARY_BLOCK_DESC),
      BLOCK_ENTITY_TYPE(MekanismLang.DICTIONARY_BLOCK_ENTITY_TYPE, MekanismLang.DICTIONARY_BLOCK_ENTITY_TYPE_DESC),
      FLUID(MekanismLang.DICTIONARY_FLUID, MekanismLang.DICTIONARY_FLUID_DESC),
      ENTITY_TYPE(MekanismLang.DICTIONARY_ENTITY_TYPE, MekanismLang.DICTIONARY_ENTITY_TYPE_DESC),
      ATTRIBUTE(MekanismLang.DICTIONARY_ATTRIBUTE, MekanismLang.DICTIONARY_ATTRIBUTE_DESC),
      POTION(MekanismLang.DICTIONARY_POTION, MekanismLang.DICTIONARY_POTION_DESC),
      MOB_EFFECT(MekanismLang.DICTIONARY_MOB_EFFECT, MekanismLang.DICTIONARY_MOB_EFFECT_DESC),
      ENCHANTMENT(MekanismLang.DICTIONARY_ENCHANTMENT, MekanismLang.DICTIONARY_ENCHANTMENT_DESC),
      GAS(MekanismLang.DICTIONARY_GAS, MekanismLang.DICTIONARY_GAS_DESC),
      INFUSE_TYPE(MekanismLang.DICTIONARY_INFUSE_TYPE, MekanismLang.DICTIONARY_INFUSE_TYPE_DESC),
      PIGMENT(MekanismLang.DICTIONARY_PIGMENT, MekanismLang.DICTIONARY_PIGMENT_DESC),
      SLURRY(MekanismLang.DICTIONARY_SLURRY, MekanismLang.DICTIONARY_SLURRY_DESC);

      private final ILangEntry name;
      private final ILangEntry tooltip;

      private DictionaryTagType(ILangEntry name, ILangEntry tooltip) {
         this.name = name;
         this.tooltip = tooltip;
      }

      @Override
      public Component getTooltip() {
         return this.tooltip.translate();
      }

      @Override
      public Component getShortName() {
         return this.name.translate();
      }
   }
}
