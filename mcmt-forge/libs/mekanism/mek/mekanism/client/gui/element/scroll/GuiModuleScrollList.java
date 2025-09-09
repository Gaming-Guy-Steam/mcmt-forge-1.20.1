package mekanism.client.gui.element.scroll;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;
import java.util.function.ObjIntConsumer;
import java.util.function.Supplier;
import mekanism.api.gear.IModule;
import mekanism.api.gear.IModuleHelper;
import mekanism.api.gear.ModuleData;
import mekanism.api.text.EnumColor;
import mekanism.api.text.TextComponentUtil;
import mekanism.client.gui.IGuiWrapper;
import mekanism.client.gui.element.GuiElement;
import mekanism.client.gui.element.GuiElementHolder;
import mekanism.common.MekanismLang;
import mekanism.common.content.gear.Module;
import mekanism.common.content.gear.ModuleHelper;
import mekanism.common.util.MekanismUtils;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class GuiModuleScrollList extends GuiScrollList {
   private static final ResourceLocation MODULE_SELECTION = MekanismUtils.getResource(MekanismUtils.ResourceType.GUI, "module_selection.png");
   private static final int TEXTURE_WIDTH = 100;
   private static final int TEXTURE_HEIGHT = 36;
   private final Consumer<Module<?>> callback;
   private final List<ModuleData<?>> currentList = new ArrayList<>();
   private final Supplier<ItemStack> itemSupplier;
   private ItemStack currentItem;
   @Nullable
   private ModuleData<?> selected;

   public GuiModuleScrollList(IGuiWrapper gui, int x, int y, int width, int height, Supplier<ItemStack> itemSupplier, Consumer<Module<?>> callback) {
      super(gui, x, y, width, height, 12, GuiElementHolder.HOLDER, 32);
      this.itemSupplier = itemSupplier;
      this.callback = callback;
      this.updateItemAndList(itemSupplier.get());
   }

   public void updateItemAndList(ItemStack stack) {
      this.currentItem = stack;
      this.currentList.clear();
      this.currentList.addAll(IModuleHelper.INSTANCE.loadAllTypes(this.currentItem));
   }

   private void recheckItem() {
      ItemStack stack = this.itemSupplier.get();
      if (!ItemStack.m_41728_(this.currentItem, stack)) {
         this.updateItemAndList(stack);
         ModuleData<?> prevSelect = this.getSelection();
         if (prevSelect != null) {
            if (this.currentList.contains(prevSelect)) {
               this.onSelectedChange();
            } else {
               this.clearSelection();
            }
         }
      }
   }

   @Override
   protected int getMaxElements() {
      return this.currentList.size();
   }

   @Override
   public boolean hasSelection() {
      return this.selected != null;
   }

   @Override
   protected void setSelected(int index) {
      if (index >= 0 && index < this.currentList.size()) {
         this.setSelected(this.currentList.get(index));
      }
   }

   private void setSelected(@Nullable ModuleData<?> newData) {
      if (this.selected != newData) {
         this.selected = newData;
         this.onSelectedChange();
      }
   }

   private void onSelectedChange() {
      if (this.selected == null) {
         this.callback.accept(null);
      } else {
         this.callback.accept(ModuleHelper.get().load(this.currentItem, this.selected));
      }
   }

   @Nullable
   public ModuleData<?> getSelection() {
      return this.selected;
   }

   @Override
   public void clearSelection() {
      this.setSelected(null);
   }

   @Override
   public void renderForeground(GuiGraphics guiGraphics, int mouseX, int mouseY) {
      super.renderForeground(guiGraphics, mouseX, mouseY);
      this.recheckItem();
      this.forEachModule(
         (module, multipliedElement) -> {
            IModule<?> instance = IModuleHelper.INSTANCE.load(this.currentItem, module);
            if (instance != null) {
               int color = module.isExclusive(-1) ? (instance.isEnabled() ? 6511572 : 3025513) : (instance.isEnabled() ? this.titleTextColor() : 6167837);
               this.drawScaledTextScaledBound(
                  guiGraphics, TextComponentUtil.build(module), this.relativeX + 13, this.relativeY + 3 + multipliedElement, color, 86.0F, 0.7F
               );
            }
         }
      );
   }

   @Override
   public void renderToolTip(@NotNull GuiGraphics guiGraphics, int mouseX, int mouseY) {
      super.renderToolTip(guiGraphics, mouseX, mouseY);
      if (mouseX >= this.m_252754_() + 1 && mouseX < this.m_252754_() + this.barXShift - 1) {
         this.forEachModule(
            (module, multipliedElement) -> {
               IModule<?> instance = IModuleHelper.INSTANCE.load(this.currentItem, module);
               if (instance != null
                  && mouseY >= this.m_252907_() + 1 + multipliedElement
                  && mouseY < this.m_252907_() + 1 + multipliedElement + this.elementHeight) {
                  Component t = MekanismLang.GENERIC_FRACTION
                     .translateColored(EnumColor.GRAY, new Object[]{instance.getInstalledCount(), module.getMaxStackSize()});
                  this.displayTooltips(guiGraphics, mouseX, mouseY, new Component[]{MekanismLang.MODULE_INSTALLED.translate(new Object[]{t})});
               }
            }
         );
      }
   }

   @Override
   public void renderElements(GuiGraphics guiGraphics, int mouseX, int mouseY, float partialTicks) {
      this.forEachModule(
         (module, multipliedElement) -> {
            int shiftedY = this.m_252907_() + 1 + multipliedElement;
            int j = 1;
            if (module == this.getSelection()) {
               j = 2;
            } else if (mouseX >= this.m_252754_() + 1
               && mouseX < this.m_252754_() + this.barXShift - 1
               && mouseY >= shiftedY
               && mouseY < shiftedY + this.elementHeight) {
               j = 0;
            }

            guiGraphics.m_280163_(
               MODULE_SELECTION, this.relativeX + 1, this.relativeY + 1 + multipliedElement, 0.0F, this.elementHeight * j, 100, this.elementHeight, 100, 36
            );
         }
      );
      this.forEachModule(
         (module, multipliedElement) -> this.gui()
            .renderItem(guiGraphics, module.getItemProvider().getItemStack(), this.relativeX + 3, this.relativeY + 3 + multipliedElement, 0.5F)
      );
   }

   private void forEachModule(ObjIntConsumer<ModuleData<?>> consumer) {
      for (int i = 0; i < this.getFocusedElements(); i++) {
         int index = this.getCurrentSelection() + i;
         if (index > this.currentList.size() - 1) {
            break;
         }

         consumer.accept(this.currentList.get(index), this.elementHeight * i);
      }
   }

   @Override
   public void syncFrom(GuiElement element) {
      super.syncFrom(element);
      GuiModuleScrollList old = (GuiModuleScrollList)element;
      if (ItemStack.m_41728_(this.currentItem, old.currentItem)) {
         this.selected = old.selected;
      } else if (old.selected != null) {
         if (this.currentList.contains(old.selected)) {
            this.setSelected(old.selected);
         } else {
            this.onSelectedChange();
         }
      }
   }
}
