package mekanism.client.jei;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import mekanism.client.gui.GuiMekanism;
import mekanism.client.gui.element.GuiElement;
import mekanism.client.gui.element.window.GuiWindow;
import mekanism.client.jei.interfaces.IJEIIngredientHelper;
import mekanism.client.jei.interfaces.IJEIRecipeArea;
import mezz.jei.api.gui.handlers.IGuiClickableArea;
import mezz.jei.api.gui.handlers.IGuiContainerHandler;
import mezz.jei.api.ingredients.ITypedIngredient;
import mezz.jei.api.runtime.IClickableIngredient;
import mezz.jei.api.runtime.IIngredientManager;
import net.minecraft.client.gui.components.AbstractWidget;
import net.minecraft.client.gui.components.events.GuiEventListener;
import net.minecraft.client.renderer.Rect2i;

public class GuiElementHandler implements IGuiContainerHandler<GuiMekanism<?>> {
   private final IIngredientManager ingredientManager;

   private static boolean areaSticksOut(int x, int y, int width, int height, int parentX, int parentY, int parentWidth, int parentHeight) {
      return x < parentX || y < parentY || x + width > parentX + parentWidth || y + height > parentY + parentHeight;
   }

   public static List<Rect2i> getAreasFor(int parentX, int parentY, int parentWidth, int parentHeight, Collection<? extends GuiEventListener> children) {
      List<Rect2i> areas = new ArrayList<>();

      for (GuiEventListener child : children) {
         if (child instanceof AbstractWidget widget && widget.f_93624_) {
            if (areaSticksOut(widget.m_252754_(), widget.m_252907_(), widget.m_5711_(), widget.m_93694_(), parentX, parentY, parentWidth, parentHeight)) {
               areas.add(new Rect2i(widget.m_252754_(), widget.m_252907_(), widget.m_5711_(), widget.m_93694_()));
            }

            if (widget instanceof GuiElement element) {
               for (Rect2i grandChildArea : getAreasFor(widget.m_252754_(), widget.m_252907_(), widget.m_5711_(), widget.m_93694_(), element.children())) {
                  if (areaSticksOut(
                     grandChildArea.m_110085_(),
                     grandChildArea.m_110086_(),
                     grandChildArea.m_110090_(),
                     grandChildArea.m_110091_(),
                     parentX,
                     parentY,
                     parentWidth,
                     parentHeight
                  )) {
                     areas.add(grandChildArea);
                  }
               }
            }
         }
      }

      return areas;
   }

   public GuiElementHandler(IIngredientManager ingredientManager) {
      this.ingredientManager = ingredientManager;
   }

   public List<Rect2i> getGuiExtraAreas(GuiMekanism<?> gui) {
      int parentX = gui.getLeft();
      int parentY = gui.getTop();
      int parentWidth = gui.getWidth();
      int parentHeight = gui.getHeight();
      List<Rect2i> extraAreas = getAreasFor(parentX, parentY, parentWidth, parentHeight, gui.m_6702_());
      extraAreas.addAll(getAreasFor(parentX, parentY, parentWidth, parentHeight, gui.getWindows()));
      return extraAreas;
   }

   public Optional<IClickableIngredient<?>> getClickableIngredientUnderMouse(GuiMekanism<?> gui, double mouseX, double mouseY) {
      GuiWindow guiWindow = gui.getWindowHovering(mouseX, mouseY);
      return this.getIngredientUnderMouse(guiWindow == null ? gui.m_6702_() : guiWindow.children(), mouseX, mouseY);
   }

   private Optional<IClickableIngredient<?>> getIngredientUnderMouse(List<? extends GuiEventListener> children, double mouseX, double mouseY) {
      for (GuiEventListener child : children) {
         if (child instanceof AbstractWidget widget) {
            if (!widget.f_93624_) {
               continue;
            }

            if (widget instanceof GuiElement element) {
               Optional<IClickableIngredient<?>> underGrandChild = this.getIngredientUnderMouse(element.children(), mouseX, mouseY);
               if (underGrandChild.isPresent()) {
                  return underGrandChild;
               }
            }
         }

         if (child instanceof IJEIIngredientHelper helper && child.m_5953_(mouseX, mouseY)) {
            record ClickableIngredient<T>(ITypedIngredient<T> getTypedIngredient, Rect2i getArea) implements IClickableIngredient<T> {
            }

            return helper.getIngredient(mouseX, mouseY).<ITypedIngredient>flatMap(this.ingredientManager::createTypedIngredient).map(typedIngredient -> {
               Rect2i bounds = helper.getIngredientBounds(mouseX, mouseY);
               return new ClickableIngredient(typedIngredient, bounds);
            });
         }
      }

      return Optional.empty();
   }

   public Collection<IGuiClickableArea> getGuiClickableAreas(GuiMekanism<?> gui, double mouseX, double mouseY) {
      mouseX += gui.getGuiLeft();
      mouseY += gui.getGuiTop();
      GuiWindow guiWindow = gui.getWindowHovering(mouseX, mouseY);
      return guiWindow == null ? this.getGuiClickableArea(gui.m_6702_(), mouseX, mouseY) : this.getGuiClickableArea(guiWindow.children(), mouseX, mouseY);
   }

   private Collection<IGuiClickableArea> getGuiClickableArea(List<? extends GuiEventListener> children, double mouseX, double mouseY) {
      for (GuiEventListener child : children) {
         if (child instanceof GuiElement element && element.f_93624_) {
            Collection<IGuiClickableArea> clickableGrandChildAreas = this.getGuiClickableArea(element.children(), mouseX, mouseY);
            if (!clickableGrandChildAreas.isEmpty()) {
               return clickableGrandChildAreas;
            }

            if (element instanceof IJEIRecipeArea<?> recipeArea && recipeArea.isJEIAreaActive()) {
               MekanismJEIRecipeType<?>[] categories = recipeArea.getRecipeCategories();
               if (categories != null && recipeArea.isMouseOverJEIArea(mouseX, mouseY)) {
                  IGuiClickableArea clickableArea = IGuiClickableArea.createBasic(
                     element.getRelativeX(), element.getRelativeY(), element.m_5711_(), element.m_93694_(), MekanismJEI.recipeType(categories)
                  );
                  return Collections.singleton(clickableArea);
               }
            }
         }
      }

      return Collections.emptyList();
   }
}
