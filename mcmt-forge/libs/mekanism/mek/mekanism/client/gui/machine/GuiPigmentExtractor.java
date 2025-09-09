package mekanism.client.gui.machine;

import java.lang.ref.WeakReference;
import mekanism.api.inventory.IInventorySlot;
import mekanism.api.recipes.ItemStackToPigmentRecipe;
import mekanism.api.recipes.cache.CachedRecipe;
import mekanism.client.gui.GuiConfigurableTile;
import mekanism.client.gui.element.bar.GuiHorizontalPowerBar;
import mekanism.client.gui.element.gauge.GaugeType;
import mekanism.client.gui.element.gauge.GuiPigmentGauge;
import mekanism.client.gui.element.progress.GuiProgress;
import mekanism.client.gui.element.progress.ProgressType;
import mekanism.client.gui.element.tab.GuiEnergyTab;
import mekanism.common.inventory.container.tile.MekanismTileContainer;
import mekanism.common.inventory.warning.WarningTracker;
import mekanism.common.tile.machine.TileEntityPigmentExtractor;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.item.ItemStack;
import org.jetbrains.annotations.NotNull;

public class GuiPigmentExtractor extends GuiConfigurableTile<TileEntityPigmentExtractor, MekanismTileContainer<TileEntityPigmentExtractor>> {
   public GuiPigmentExtractor(MekanismTileContainer<TileEntityPigmentExtractor> container, Inventory inv, Component title) {
      super(container, inv, title);
      this.dynamicSlots = true;
   }

   @Override
   protected void addGuiElements() {
      super.addGuiElements();
      this.addRenderableWidget(new GuiHorizontalPowerBar(this, this.tile.getEnergyContainer(), 115, 75))
         .warning(WarningTracker.WarningType.NOT_ENOUGH_ENERGY, this.tile.getWarningCheck(CachedRecipe.OperationTracker.RecipeError.NOT_ENOUGH_ENERGY));
      this.addRenderableWidget(new GuiEnergyTab(this, this.tile.getEnergyContainer(), this.tile::getActive));
      this.addRenderableWidget(new GuiPigmentGauge(() -> this.tile.pigmentTank, () -> this.tile.getPigmentTanks(null), GaugeType.STANDARD, this, 131, 13))
         .warning(WarningTracker.WarningType.NO_SPACE_IN_OUTPUT, this.tile.getWarningCheck(CachedRecipe.OperationTracker.RecipeError.NOT_ENOUGH_OUTPUT_SPACE));
      this.addRenderableWidget(
            new GuiProgress(this.tile::getScaledProgress, ProgressType.LARGE_RIGHT, this, 64, 40)
               .jeiCategory(this.tile)
               .colored(new GuiPigmentExtractor.PigmentColorDetails())
         )
         .warning(
            WarningTracker.WarningType.INPUT_DOESNT_PRODUCE_OUTPUT,
            this.tile.getWarningCheck(CachedRecipe.OperationTracker.RecipeError.INPUT_DOESNT_PRODUCE_OUTPUT)
         );
   }

   @Override
   protected void drawForegroundText(@NotNull GuiGraphics guiGraphics, int mouseX, int mouseY) {
      this.renderTitleText(guiGraphics);
      this.drawString(guiGraphics, this.f_169604_, this.f_97730_, this.f_97731_, this.titleTextColor());
      super.drawForegroundText(guiGraphics, mouseX, mouseY);
   }

   private class PigmentColorDetails implements GuiProgress.ColorDetails {
      private WeakReference<ItemStackToPigmentRecipe> cachedRecipe;

      @Override
      public int getColorFrom() {
         return -1;
      }

      @Override
      public int getColorTo() {
         if (GuiPigmentExtractor.this.tile == null) {
            return -1;
         } else if (!GuiPigmentExtractor.this.tile.pigmentTank.isEmpty()) {
            return this.getColor(GuiPigmentExtractor.this.tile.pigmentTank.getType().getColorRepresentation());
         } else {
            IInventorySlot inputSlot = GuiPigmentExtractor.this.tile.getInputSlot();
            if (!inputSlot.isEmpty()) {
               ItemStack input = inputSlot.getStack();
               ItemStackToPigmentRecipe recipe;
               if (this.cachedRecipe == null) {
                  recipe = this.getRecipeAndCache();
               } else {
                  recipe = this.cachedRecipe.get();
                  if (recipe == null || !recipe.getInput().testType(input)) {
                     recipe = this.getRecipeAndCache();
                  }
               }

               if (recipe != null) {
                  return this.getColor(recipe.getOutput(input).getChemicalColorRepresentation());
               }
            }

            return -1;
         }
      }

      private ItemStackToPigmentRecipe getRecipeAndCache() {
         ItemStackToPigmentRecipe recipe = GuiPigmentExtractor.this.tile.getRecipe(0);
         if (recipe == null) {
            this.cachedRecipe = null;
         } else {
            this.cachedRecipe = new WeakReference<>(recipe);
         }

         return recipe;
      }

      private int getColor(int tint) {
         return (tint & 0xFF000000) == 0 ? 0xFF000000 | tint : tint;
      }
   }
}
