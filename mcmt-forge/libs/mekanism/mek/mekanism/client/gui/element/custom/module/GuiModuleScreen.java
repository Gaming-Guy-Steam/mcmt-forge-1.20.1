package mekanism.client.gui.element.custom.module;

import com.mojang.blaze3d.vertex.PoseStack;
import java.util.ArrayList;
import java.util.List;
import java.util.function.ObjIntConsumer;
import mekanism.api.gear.IModule;
import mekanism.api.gear.config.ModuleBooleanData;
import mekanism.api.gear.config.ModuleColorData;
import mekanism.api.gear.config.ModuleEnumData;
import mekanism.api.text.IHasTextComponent;
import mekanism.client.gui.GuiModuleTweaker;
import mekanism.client.gui.IGuiWrapper;
import mekanism.client.gui.element.GuiElement;
import mekanism.client.gui.element.GuiInnerScreen;
import mekanism.client.gui.element.scroll.GuiScrollList;
import mekanism.client.gui.element.scroll.GuiScrollableElement;
import mekanism.common.MekanismLang;
import mekanism.common.content.gear.Module;
import mekanism.common.content.gear.ModuleConfigItem;
import net.minecraft.client.gui.GuiGraphics;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class GuiModuleScreen extends GuiScrollableElement {
   private static final int ELEMENT_SPACER = 4;
   final ObjIntConsumer<ModuleConfigItem<?>> saveCallback;
   private final GuiModuleTweaker.ArmorPreview armorPreview;
   @Nullable
   private Module<?> currentModule;
   private List<MiniElement> miniElements = new ArrayList<>();
   private int maxElements;

   public GuiModuleScreen(IGuiWrapper gui, int x, int y, ObjIntConsumer<ModuleConfigItem<?>> saveCallback, GuiModuleTweaker.ArmorPreview armorPreview) {
      this(gui, x, y, 102, 134, saveCallback, armorPreview);
   }

   private GuiModuleScreen(
      IGuiWrapper gui, int x, int y, int width, int height, ObjIntConsumer<ModuleConfigItem<?>> saveCallback, GuiModuleTweaker.ArmorPreview armorPreview
   ) {
      super(GuiScrollList.SCROLL_LIST, gui, x, y, width, height, width - 6, 2, 4, 4, height - 4);
      this.saveCallback = saveCallback;
      this.armorPreview = armorPreview;
   }

   public void setModule(@Nullable Module<?> module) {
      List<MiniElement> newElements = new ArrayList<>();
      if (module != null) {
         int startY = getStartY(module);
         List<ModuleConfigItem<?>> configItems = module.getConfigItems();
         int i = 0;

         for (int configItemsCount = configItems.size(); i < configItemsCount; i++) {
            ModuleConfigItem<?> configItem = configItems.get(i);
            MiniElement element = null;
            if (configItem.getData() instanceof ModuleBooleanData && (!configItem.getName().equals("enabled") || !module.getData().isNoDisable())) {
               if (configItem instanceof ModuleConfigItem.DisableableModuleConfigItem item && !item.isConfigEnabled()) {
                  continue;
               }

               element = new BooleanToggle(this, (ModuleConfigItem<Boolean>)configItem, 2, startY, i);
            } else if (configItem.getData() instanceof ModuleEnumData) {
               EnumToggle<?> toggle = this.createEnumToggle(configItem, 2, startY, i);
               element = toggle;
               if (this.currentModule != null
                  && this.currentModule.getData() == module.getData()
                  && i < this.miniElements.size()
                  && this.miniElements.get(i) instanceof EnumToggle<?> enumToggle) {
                  toggle.dragging = enumToggle.dragging;
               }
            } else if (configItem.getData() instanceof ModuleColorData data) {
               element = new ColorSelection(this, (ModuleConfigItem<Integer>)configItem, 2, startY, i, data.handlesAlpha(), this.armorPreview);
            }

            if (element != null) {
               newElements.add(element);
               startY += element.getNeededHeight() + 4;
            }
         }

         this.maxElements = newElements.isEmpty() ? startY : startY - 4;
      } else {
         this.maxElements = 0;
      }

      this.currentModule = module;
      this.miniElements = newElements;
   }

   private <TYPE extends Enum<TYPE> & IHasTextComponent> EnumToggle<TYPE> createEnumToggle(ModuleConfigItem<?> data, int xPos, int yPos, int dataIndex) {
      return new EnumToggle<>(this, (ModuleConfigItem<TYPE>)data, xPos, yPos, dataIndex);
   }

   private static int getStartY(@Nullable IModule<?> module) {
      int startY = 5;
      if (module != null) {
         if (module.getData().isExclusive(-1)) {
            startY += 13;
         }

         if (module.getData().getMaxStackSize() > 1) {
            startY += 13;
         }
      }

      return startY;
   }

   @Override
   protected int getMaxElements() {
      return this.maxElements;
   }

   @Override
   protected int getFocusedElements() {
      return this.f_93619_ - 2;
   }

   @Override
   protected int getScrollElementScaler() {
      return 10;
   }

   int getScreenWidth() {
      return this.barXShift;
   }

   @Nullable
   public IModule<?> getCurrentModule() {
      return this.currentModule;
   }

   @Override
   public void syncFrom(GuiElement element) {
      GuiModuleScreen old = (GuiModuleScreen)element;
      this.setModule(old.currentModule);
      super.syncFrom(element);
   }

   @Override
   public boolean m_6050_(double mouseX, double mouseY, double delta) {
      return this.m_5953_(mouseX, mouseY) && this.adjustScroll(delta) || super.m_6050_(mouseX, mouseY, delta);
   }

   @Override
   public void onClick(double mouseX, double mouseY, int button) {
      super.onClick(mouseX, mouseY, button);
      mouseY += this.getCurrentSelection();

      for (MiniElement element : this.miniElements) {
         element.click(mouseX, mouseY);
      }
   }

   @Override
   public void m_7691_(double mouseX, double mouseY) {
      super.m_7691_(mouseX, mouseY);
      mouseY += this.getCurrentSelection();

      for (MiniElement element : this.miniElements) {
         element.release(mouseX, mouseY);
      }
   }

   @Override
   public void m_7212_(double mouseX, double mouseY, double deltaX, double deltaY) {
      super.m_7212_(mouseX, mouseY, deltaX, deltaY);
      mouseY += this.getCurrentSelection();

      for (MiniElement element : this.miniElements) {
         element.onDrag(mouseX, mouseY, deltaX, deltaY);
      }
   }

   @Override
   public void drawBackground(@NotNull GuiGraphics guiGraphics, int mx, int my, float partialTicks) {
      super.drawBackground(guiGraphics, mx, my, partialTicks);
      this.renderBackgroundTexture(guiGraphics, GuiInnerScreen.SCREEN, GuiInnerScreen.SCREEN_SIZE, GuiInnerScreen.SCREEN_SIZE);
      this.drawScrollBar(guiGraphics, 6, 6);
      this.scissorScreen(guiGraphics, mx, my, (g, mouseX, mouseY, module, shift) -> getStartY(module), MiniElement::renderBackground);
   }

   @Override
   public void renderForeground(GuiGraphics guiGraphics, int mx, int my) {
      super.renderForeground(guiGraphics, mx, my);
      this.scissorScreen(
         guiGraphics,
         mx,
         my,
         (g, mouseX, mouseY, module, shift) -> {
            int startY = 5;
            if (module != null) {
               if (module.getData().isExclusive(-1)) {
                  if (startY + 13 > shift) {
                     this.drawTextWithScale(
                        g, MekanismLang.MODULE_EXCLUSIVE.translate(new Object[0]), this.relativeX + 5, this.relativeY + startY, 6511572, 0.8F
                     );
                  }

                  startY += 13;
               }

               if (module.getData().getMaxStackSize() > 1) {
                  if (startY + 13 > shift) {
                     this.drawTextWithScale(
                        g,
                        MekanismLang.MODULE_INSTALLED.translate(new Object[]{module.getInstalledCount()}),
                        this.relativeX + 5,
                        this.relativeY + startY,
                        this.screenTextColor(),
                        0.8F
                     );
                  }

                  startY += 13;
               }
            }

            return startY;
         },
         MiniElement::renderForeground
      );
   }

   private void scissorScreen(
      GuiGraphics guiGraphics, int mouseX, int mouseY, GuiModuleScreen.ScissorRender renderer, GuiModuleScreen.ScissorMiniElementRender miniElementRender
   ) {
      guiGraphics.m_280588_(0, this.m_252907_() + 1, guiGraphics.m_280182_(), this.m_252907_() + this.f_93619_ - 1);
      PoseStack pose = guiGraphics.m_280168_();
      pose.m_85836_();
      int shift = this.getCurrentSelection();
      pose.m_252880_(0.0F, -shift, 0.0F);
      mouseY += shift;
      int startY = renderer.render(guiGraphics, mouseX, mouseY, this.currentModule, shift);

      for (MiniElement element : this.miniElements) {
         if (startY >= shift + this.f_93619_) {
            break;
         }

         if (startY + element.getNeededHeight() > shift) {
            miniElementRender.render(element, guiGraphics, mouseX, mouseY);
         }

         startY += element.getNeededHeight() + 4;
      }

      pose.m_85849_();
      guiGraphics.m_280618_();
   }

   @FunctionalInterface
   private interface ScissorMiniElementRender {
      void render(MiniElement element, GuiGraphics guiGraphics, int mouseX, int mouseY);
   }

   @FunctionalInterface
   private interface ScissorRender {
      int render(GuiGraphics guiGraphics, int mouseX, int mouseY, @Nullable IModule<?> module, int shift);
   }
}
