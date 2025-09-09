package mekanism.client.jei;

import it.unimi.dsi.fastutil.ints.Int2ObjectLinkedOpenHashMap;
import it.unimi.dsi.fastutil.ints.Int2ObjectMap;
import it.unimi.dsi.fastutil.ints.Int2ObjectOpenHashMap;
import it.unimi.dsi.fastutil.ints.Int2ObjectMap.Entry;
import it.unimi.dsi.fastutil.ints.Int2ObjectSortedMap.FastSortedEntrySet;
import it.unimi.dsi.fastutil.objects.ObjectBidirectionalIterator;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;
import mekanism.client.gui.GuiMekanism;
import mekanism.client.gui.element.GuiElement;
import mekanism.client.gui.element.window.GuiWindow;
import mekanism.client.jei.interfaces.IJEIGhostTarget;
import mekanism.common.lib.collection.LRU;
import mezz.jei.api.gui.handlers.IGhostIngredientHandler;
import mezz.jei.api.gui.handlers.IGhostIngredientHandler.Target;
import mezz.jei.api.ingredients.ITypedIngredient;
import net.minecraft.client.gui.components.AbstractWidget;
import net.minecraft.client.gui.components.events.GuiEventListener;
import net.minecraft.client.renderer.Rect2i;

public class GhostIngredientHandler<GUI extends GuiMekanism<?>> implements IGhostIngredientHandler<GUI> {
   public <INGREDIENT> List<Target<INGREDIENT>> getTargetsTyped(GUI gui, ITypedIngredient<INGREDIENT> ingredient, boolean doStart) {
      boolean hasTargets = false;
      int depth = 0;
      Int2ObjectLinkedOpenHashMap<List<GhostIngredientHandler.TargetInfo<INGREDIENT>>> depthBasedTargets = new Int2ObjectLinkedOpenHashMap();
      Int2ObjectMap<List<Rect2i>> layerIntersections = new Int2ObjectOpenHashMap();
      List<GhostIngredientHandler.TargetInfo<INGREDIENT>> ghostTargets = this.getTargets(gui.m_6702_(), ingredient);
      if (!ghostTargets.isEmpty()) {
         depthBasedTargets.put(depth, ghostTargets);
         hasTargets = true;
      }

      LRU<GuiWindow>.LRUIterator iter = gui.getWindowsDescendingIterator();

      while (iter.hasNext()) {
         GuiWindow window = iter.next();
         depth++;
         if (hasTargets) {
            List<Rect2i> areas = new ArrayList<>();
            areas.add(new Rect2i(window.m_252754_(), window.m_252907_(), window.m_5711_(), window.m_93694_()));
            areas.addAll(GuiElementHandler.getAreasFor(window.m_252754_(), window.m_252907_(), window.m_5711_(), window.m_93694_(), window.children()));
            layerIntersections.put(depth, areas);
         }

         ghostTargets = this.getTargets(window.children(), ingredient);
         if (!ghostTargets.isEmpty()) {
            depthBasedTargets.put(depth, ghostTargets);
            hasTargets = true;
         }
      }

      if (!hasTargets) {
         return Collections.emptyList();
      } else {
         List<Target<INGREDIENT>> targets = new ArrayList<>();
         List<Rect2i> coveredArea = new ArrayList<>();
         FastSortedEntrySet<List<GhostIngredientHandler.TargetInfo<INGREDIENT>>> depthEntries = depthBasedTargets.int2ObjectEntrySet();
         ObjectBidirectionalIterator<Entry<List<GhostIngredientHandler.TargetInfo<INGREDIENT>>>> iterx = depthEntries.fastIterator((Entry)depthEntries.last());

         while (iterx.hasPrevious()) {
            Entry<List<GhostIngredientHandler.TargetInfo<INGREDIENT>>> entry = (Entry<List<GhostIngredientHandler.TargetInfo<INGREDIENT>>>)iterx.previous();

            for (int targetDepth = entry.getIntKey(); depth > targetDepth; depth--) {
               coveredArea.addAll((Collection<? extends Rect2i>)layerIntersections.get(depth));
            }

            for (GhostIngredientHandler.TargetInfo<INGREDIENT> ghostTarget : (List)entry.getValue()) {
               targets.addAll(ghostTarget.convertToTargets(coveredArea));
            }
         }

         return targets;
      }
   }

   private <INGREDIENT> List<GhostIngredientHandler.TargetInfo<INGREDIENT>> getTargets(
      List<? extends GuiEventListener> children, ITypedIngredient<INGREDIENT> ingredient
   ) {
      List<GhostIngredientHandler.TargetInfo<INGREDIENT>> ghostTargets = new ArrayList<>();

      for (GuiEventListener child : children) {
         if (child instanceof AbstractWidget widget && widget.f_93624_) {
            if (widget instanceof GuiElement element) {
               ghostTargets.addAll(this.getTargets(element.children(), ingredient));
            }

            if (widget instanceof IJEIGhostTarget ghostTarget) {
               IJEIGhostTarget.IGhostIngredientConsumer ghostHandler = ghostTarget.getGhostHandler();
               if (ghostHandler != null && ghostHandler.supportsIngredient(ingredient.getIngredient())) {
                  ghostTargets.add(
                     new GhostIngredientHandler.TargetInfo<>(
                        ghostTarget, ghostHandler, widget.m_252754_(), widget.m_252907_(), widget.m_5711_(), widget.m_93694_()
                     )
                  );
               }
            }
         }
      }

      return ghostTargets;
   }

   public void onComplete() {
   }

   private static void addVisibleAreas(List<Rect2i> visible, Rect2i area, List<Rect2i> coveredArea) {
      boolean intersected = false;
      int x = area.m_110085_();
      int x2 = x + area.m_110090_();
      int y = area.m_110086_();
      int y2 = y + area.m_110091_();
      int size = coveredArea.size();

      for (int i = 0; i < size; i++) {
         Rect2i covered = coveredArea.get(i);
         int cx = covered.m_110085_();
         int cx2 = cx + covered.m_110090_();
         int cy = covered.m_110086_();
         int cy2 = cy + covered.m_110091_();
         if (x < cx2 && x2 > cx && y < cy2 && y2 > cy) {
            intersected = true;
            if (x < cx || y < cy || x2 > cx2 || y2 > cy2) {
               List<Rect2i> uncoveredArea = getVisibleArea(area, covered);
               if (i + 1 == size) {
                  visible.addAll(uncoveredArea);
               } else {
                  List<Rect2i> coveredAreas = coveredArea.subList(i + 1, size);

                  for (Rect2i visibleArea : uncoveredArea) {
                     addVisibleAreas(visible, visibleArea, coveredAreas);
                  }
               }
            }
            break;
         }
      }

      if (!intersected) {
         visible.add(area);
      }
   }

   private static List<Rect2i> getVisibleArea(Rect2i area, Rect2i coveredArea) {
      int x = area.m_110085_();
      int x2 = x + area.m_110090_();
      int y = area.m_110086_();
      int y2 = y + area.m_110091_();
      int cx = coveredArea.m_110085_();
      int cx2 = cx + coveredArea.m_110090_();
      int cy = coveredArea.m_110086_();
      int cy2 = cy + coveredArea.m_110091_();
      boolean intersectsTop = y >= cy && y <= cy2;
      boolean intersectsLeft = x >= cx && x <= cx2;
      boolean intersectsBottom = y2 >= cy && y2 <= cy2;
      boolean intersectsRight = x2 >= cx && x2 <= cx2;
      List<Rect2i> areas = new ArrayList<>();
      if (intersectsTop && intersectsBottom) {
         if (intersectsLeft) {
            areas.add(new Rect2i(cx2, y, x2 - cx2, area.m_110091_()));
         } else if (intersectsRight) {
            areas.add(new Rect2i(x, y, cx - x, area.m_110091_()));
         } else {
            areas.add(new Rect2i(x, y, cx - x, area.m_110091_()));
            areas.add(new Rect2i(cx2, y, x2 - cx2, area.m_110091_()));
         }
      } else if (intersectsLeft && intersectsRight) {
         if (intersectsTop) {
            areas.add(new Rect2i(x, cy2, area.m_110090_(), y2 - cy2));
         } else if (intersectsBottom) {
            areas.add(new Rect2i(x, y, area.m_110090_(), cy - y));
         } else {
            areas.add(new Rect2i(x, y, area.m_110090_(), cy - y));
            areas.add(new Rect2i(x, cy2, area.m_110090_(), y2 - cy2));
         }
      } else if (intersectsTop && intersectsLeft) {
         areas.add(new Rect2i(x, cy2, area.m_110090_(), y2 - cy2));
         areas.add(new Rect2i(cx2, y, x2 - cx2, cy2 - y));
      } else if (intersectsTop && intersectsRight) {
         areas.add(new Rect2i(x, y, cx - x, cy2 - y));
         areas.add(new Rect2i(x, cy2, area.m_110090_(), y2 - cy2));
      } else if (intersectsBottom && intersectsLeft) {
         areas.add(new Rect2i(x, y, area.m_110090_(), cy - y));
         areas.add(new Rect2i(cx2, cy, x2 - cx2, y2 - cy));
      } else if (intersectsBottom && intersectsRight) {
         areas.add(new Rect2i(x, y, area.m_110090_(), cy - y));
         areas.add(new Rect2i(x, cy, cx - x, y2 - cy));
      } else if (intersectsTop) {
         areas.add(new Rect2i(x, y, cx - x, cy2 - y));
         areas.add(new Rect2i(x, cy2, area.m_110090_(), y2 - cy2));
         areas.add(new Rect2i(cx2, y, x2 - cx2, cy2 - y));
      } else if (intersectsLeft) {
         areas.add(new Rect2i(x, y, area.m_110090_(), cy - y));
         areas.add(new Rect2i(x, cy2, area.m_110090_(), y2 - cy2));
         areas.add(new Rect2i(cx2, cy, x2 - cx2, coveredArea.m_110091_()));
      } else if (intersectsBottom) {
         areas.add(new Rect2i(x, y, area.m_110090_(), cy - y));
         areas.add(new Rect2i(x, cy, cx - x, y2 - cy));
         areas.add(new Rect2i(cx2, cy, x2 - cx2, y2 - cy));
      } else if (intersectsRight) {
         areas.add(new Rect2i(x, y, area.m_110090_(), cy - y));
         areas.add(new Rect2i(x, cy, cx - x, coveredArea.m_110091_()));
         areas.add(new Rect2i(x, cy2, area.m_110090_(), y2 - cy2));
      } else {
         areas.add(new Rect2i(x, y, area.m_110090_(), cy - y));
         areas.add(new Rect2i(x, cy, cx - x, coveredArea.m_110091_()));
         areas.add(new Rect2i(x, cy2, area.m_110090_(), y2 - cy2));
         areas.add(new Rect2i(cx2, cy, x2 - cx2, coveredArea.m_110091_()));
      }

      return areas;
   }

   private static class TargetInfo<INGREDIENT> {
      private final IJEIGhostTarget.IGhostIngredientConsumer ghostHandler;
      private final int x;
      private final int y;
      private final int width;
      private final int height;

      public TargetInfo(IJEIGhostTarget ghostTarget, IJEIGhostTarget.IGhostIngredientConsumer ghostHandler, int x, int y, int width, int height) {
         this.ghostHandler = ghostHandler;
         int borderSize = ghostTarget.borderSize();
         this.x = x + borderSize;
         this.y = y + borderSize;
         this.width = width - 2 * borderSize;
         this.height = height - 2 * borderSize;
      }

      public List<Target<INGREDIENT>> convertToTargets(List<Rect2i> coveredArea) {
         List<Rect2i> visibleAreas = new ArrayList<>();
         GhostIngredientHandler.addVisibleAreas(visibleAreas, new Rect2i(this.x, this.y, this.width, this.height), coveredArea);
         return visibleAreas.stream().map(visibleArea -> new Target<INGREDIENT>() {
            public Rect2i getArea() {
               return visibleArea;
            }

            public void accept(INGREDIENT ingredient) {
               TargetInfo.this.ghostHandler.accept(ingredient);
            }
         }).collect(Collectors.toList());
      }
   }
}
