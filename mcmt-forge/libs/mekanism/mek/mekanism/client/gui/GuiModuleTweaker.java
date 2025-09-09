package mekanism.client.gui;

import it.unimi.dsi.fastutil.ints.IntArrayList;
import it.unimi.dsi.fastutil.ints.IntList;
import java.util.EnumMap;
import java.util.Map;
import java.util.function.ObjIntConsumer;
import java.util.function.Supplier;
import mekanism.api.gear.IModule;
import mekanism.client.gui.element.GuiElementHolder;
import mekanism.client.gui.element.button.TranslationButton;
import mekanism.client.gui.element.custom.module.GuiModuleScreen;
import mekanism.client.gui.element.scroll.GuiModuleScrollList;
import mekanism.client.gui.element.slot.GuiSlot;
import mekanism.client.gui.element.slot.SlotType;
import mekanism.client.gui.element.window.GuiMekaSuitHelmetOptions;
import mekanism.common.Mekanism;
import mekanism.common.MekanismLang;
import mekanism.common.content.gear.Module;
import mekanism.common.content.gear.ModuleConfigItem;
import mekanism.common.inventory.container.ModuleTweakerContainer;
import mekanism.common.inventory.container.slot.SlotOverlay;
import mekanism.common.network.to_server.PacketUpdateModuleSettings;
import mekanism.common.registries.MekanismItems;
import mekanism.common.registries.MekanismSounds;
import mekanism.common.util.EnumUtils;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.decoration.ArmorStand;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ArmorItem;
import net.minecraft.world.item.ItemStack;
import org.jetbrains.annotations.NotNull;

public class GuiModuleTweaker extends GuiMekanism<ModuleTweakerContainer> {
   private final GuiModuleTweaker.ArmorPreview armorPreview = new GuiModuleTweaker.ArmorPreview();
   private final ObjIntConsumer<ModuleConfigItem<?>> saveCallback;
   private GuiModuleScrollList scrollList;
   private GuiModuleScreen moduleScreen;
   private TranslationButton optionsButton;
   private int selected = -1;

   public GuiModuleTweaker(ModuleTweakerContainer container, Inventory inv, Component title) {
      super(container, inv, title);
      this.saveCallback = (configItem, dataIndex) -> {
         if (this.moduleScreen != null) {
            IModule<?> module = this.moduleScreen.getCurrentModule();
            if (module != null && this.selected != -1) {
               int slotIndex = ((Slot)((ModuleTweakerContainer)this.f_97732_).f_38839_.get(this.selected)).getSlotIndex();
               Mekanism.packetHandler().sendToServer(PacketUpdateModuleSettings.create(slotIndex, module.getData(), dataIndex, configItem.getData()));
            }
         }
      };
      this.f_97726_ = 248;
      this.f_97727_ += 20;
   }

   @Override
   protected void addGuiElements() {
      super.addGuiElements();
      this.moduleScreen = this.addRenderableWidget(new GuiModuleScreen(this, 138, 20, this.saveCallback, this.armorPreview));
      this.scrollList = this.addRenderableWidget(new GuiModuleScrollList(this, 30, 20, 108, 116, () -> this.getStack(this.selected), this::onModuleSelected));
      this.addRenderableWidget(new GuiElementHolder(this, 30, 136, 108, 18));
      this.optionsButton = this.addRenderableWidget(new TranslationButton(this, 31, 137, 106, 16, MekanismLang.BUTTON_OPTIONS, this::openOptions));
      this.optionsButton.f_93623_ = false;
      int size = ((ModuleTweakerContainer)this.f_97732_).f_38839_.size();

      for (int i = 0; i < size; i++) {
         Slot slot = (Slot)((ModuleTweakerContainer)this.f_97732_).f_38839_.get(i);
         int index = i;
         if (this.selected == -1 && this.isValidItem(index)) {
            this.select(index);
         }

         this.addRenderableWidget(
            new GuiSlot(SlotType.NORMAL, this, slot.f_40220_ - 1, slot.f_40221_ - 1)
               .click((e, x, y) -> this.select(index), MekanismSounds.BEEP)
               .overlayColor(this.isValidItem(index) ? null : () -> -869059789)
               .with(() -> index == this.selected ? SlotOverlay.SELECT : null)
         );
      }
   }

   private void onModuleSelected(Module<?> module) {
      this.moduleScreen.setModule(module);
   }

   private void openOptions() {
      this.addWindow(new GuiMekaSuitHelmetOptions(this, this.getWidth() / 2 - 70, this.getHeight() / 2 - 45));
   }

   @Override
   public boolean m_7933_(int key, int i, int j) {
      if (super.m_7933_(key, i, j)) {
         return true;
      } else if (this.selected != -1 && (this.isPreviousButton(key) || this.isNextButton(key))) {
         int curIndex = -1;
         IntList selectable = new IntArrayList();
         int index = 0;

         for (int slots = ((ModuleTweakerContainer)this.f_97732_).f_38839_.size(); index < slots; index++) {
            if (this.isValidItem(index)) {
               selectable.add(index);
               if (index == this.selected) {
                  curIndex = selectable.size() - 1;
               }
            }
         }

         if (this.isPreviousButton(key)) {
            index = curIndex == 0 ? selectable.size() - 1 : curIndex - 1;
         } else {
            index = curIndex + 1;
         }

         this.select(selectable.getInt(index % selectable.size()));
         return true;
      } else {
         return false;
      }
   }

   private boolean isPreviousButton(int key) {
      return key == 265 || key == 263;
   }

   private boolean isNextButton(int key) {
      return key == 264 || key == 262;
   }

   @Override
   public boolean m_6348_(double mouseX, double mouseY, int button) {
      this.moduleScreen.m_7691_(mouseX, mouseY);
      return super.m_6348_(mouseX, mouseY, button);
   }

   @Override
   protected void drawForegroundText(@NotNull GuiGraphics guiGraphics, int mouseX, int mouseY) {
      this.renderTitleText(guiGraphics);
      super.drawForegroundText(guiGraphics, mouseX, mouseY);
   }

   private boolean select(int index) {
      if (this.isValidItem(index)) {
         this.selected = index;
         ItemStack stack = this.getStack(index);
         this.armorPreview.tryUpdateFull(stack);
         this.scrollList.updateItemAndList(stack);
         this.scrollList.clearSelection();
         this.optionsButton.f_93623_ = stack.m_41720_() == MekanismItems.MEKASUIT_HELMET.get();
         return true;
      } else {
         return false;
      }
   }

   private boolean isValidItem(int index) {
      return ModuleTweakerContainer.isTweakableItem(this.getStack(index));
   }

   private ItemStack getStack(int index) {
      return index == -1 ? ItemStack.f_41583_ : ((Slot)((ModuleTweakerContainer)this.f_97732_).f_38839_.get(index)).m_7993_();
   }

   public static class ArmorPreview implements Supplier<LivingEntity> {
      private final Map<EquipmentSlot, Supplier<ItemStack>> lazyItems = new EnumMap<>(EquipmentSlot.class);
      private ArmorStand preview;

      protected ArmorPreview() {
         for (EquipmentSlot armorSlot : EnumUtils.ARMOR_SLOTS) {
            this.lazyItems.put(armorSlot, () -> {
               ItemStack stack = Minecraft.m_91087_().f_91074_.m_6844_(armorSlot);
               if (stack.m_41619_()) {
                  return (switch (armorSlot) {
                     case FEET -> MekanismItems.MEKASUIT_BOOTS;
                     case LEGS -> MekanismItems.MEKASUIT_PANTS;
                     case CHEST -> MekanismItems.MEKASUIT_BODYARMOR;
                     case HEAD -> MekanismItems.MEKASUIT_HELMET;
                     default -> throw new IllegalStateException("Unknown armor slot: " + armorSlot.m_20751_());
                  }).getItemStack();
               } else {
                  return stack;
               }
            });
         }
      }

      public void tryUpdateFull(ItemStack stack) {
         if (!stack.m_41619_() && stack.m_41720_() instanceof ArmorItem armorItem) {
            EquipmentSlot slot = armorItem.m_40402_();
            this.lazyItems.put(slot, () -> stack);
            this.updatePreview(slot, stack);
         }
      }

      public void updatePreview(EquipmentSlot slot, ItemStack stack) {
         if (this.preview != null) {
            this.preview.m_8061_(slot, stack);
         }
      }

      public void resetToDefault(EquipmentSlot slot) {
         if (this.preview != null && this.lazyItems.containsKey(slot)) {
            this.updatePreview(slot, this.lazyItems.get(slot).get());
         }
      }

      public LivingEntity get() {
         if (this.preview == null) {
            this.preview = new ArmorStand(EntityType.f_20529_, Minecraft.m_91087_().f_91073_);
            this.preview.m_31678_(true);
            this.lazyItems.forEach((slot, item) -> this.preview.m_8061_(slot, item.get()));
         }

         return this.preview;
      }
   }
}
