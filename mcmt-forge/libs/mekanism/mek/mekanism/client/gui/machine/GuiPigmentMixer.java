package mekanism.client.gui.machine;

import java.lang.ref.WeakReference;
import mekanism.api.chemical.pigment.PigmentStack;
import mekanism.api.recipes.PigmentMixingRecipe;
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
import mekanism.common.tile.machine.TileEntityPigmentMixer;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Inventory;
import org.jetbrains.annotations.NotNull;

public class GuiPigmentMixer extends GuiConfigurableTile<TileEntityPigmentMixer, MekanismTileContainer<TileEntityPigmentMixer>> {
   public GuiPigmentMixer(MekanismTileContainer<TileEntityPigmentMixer> container, Inventory inv, Component title) {
      super(container, inv, title);
      this.f_97731_ += 2;
      this.f_97728_ = 5;
      this.f_97729_ = 5;
      this.dynamicSlots = true;
   }

   @Override
   protected void addGuiElements() {
      super.addGuiElements();
      this.addRenderableWidget(new GuiHorizontalPowerBar(this, this.tile.getEnergyContainer(), 115, 75))
         .warning(WarningTracker.WarningType.NOT_ENOUGH_ENERGY, this.tile.getWarningCheck(CachedRecipe.OperationTracker.RecipeError.NOT_ENOUGH_ENERGY))
         .warning(
            WarningTracker.WarningType.NOT_ENOUGH_ENERGY_REDUCED_RATE,
            this.tile.getWarningCheck(CachedRecipe.OperationTracker.RecipeError.NOT_ENOUGH_ENERGY_REDUCED_RATE)
         );
      this.addRenderableWidget(new GuiEnergyTab(this, this.tile.getEnergyContainer(), this.tile::getEnergyUsed));
      this.addRenderableWidget(new GuiPigmentGauge(() -> this.tile.leftInputTank, () -> this.tile.getPigmentTanks(null), GaugeType.STANDARD, this, 25, 13))
         .warning(WarningTracker.WarningType.NO_MATCHING_RECIPE, this.tile.getWarningCheck(CachedRecipe.OperationTracker.RecipeError.NOT_ENOUGH_LEFT_INPUT));
      this.addRenderableWidget(new GuiPigmentGauge(() -> this.tile.outputTank, () -> this.tile.getPigmentTanks(null), GaugeType.STANDARD, this, 79, 4))
         .warning(WarningTracker.WarningType.NO_SPACE_IN_OUTPUT, this.tile.getWarningCheck(CachedRecipe.OperationTracker.RecipeError.NOT_ENOUGH_OUTPUT_SPACE));
      this.addRenderableWidget(new GuiPigmentGauge(() -> this.tile.rightInputTank, () -> this.tile.getPigmentTanks(null), GaugeType.STANDARD, this, 133, 13))
         .warning(WarningTracker.WarningType.NO_MATCHING_RECIPE, this.tile.getWarningCheck(CachedRecipe.OperationTracker.RecipeError.NOT_ENOUGH_RIGHT_INPUT));
      this.addRenderableWidget(
            new GuiProgress(this.tile::getActive, ProgressType.SMALL_RIGHT, this, 47, 39)
               .jeiCategory(this.tile)
               .colored(new GuiPigmentMixer.LeftColorDetails())
         )
         .warning(
            WarningTracker.WarningType.INPUT_DOESNT_PRODUCE_OUTPUT,
            this.tile.getWarningCheck(CachedRecipe.OperationTracker.RecipeError.INPUT_DOESNT_PRODUCE_OUTPUT)
         );
      this.addRenderableWidget(
            new GuiProgress(this.tile::getActive, ProgressType.SMALL_LEFT, this, 101, 39)
               .jeiCategory(this.tile)
               .colored(new GuiPigmentMixer.RightColorDetails())
         )
         .warning(
            WarningTracker.WarningType.INPUT_DOESNT_PRODUCE_OUTPUT,
            this.tile.getWarningCheck(CachedRecipe.OperationTracker.RecipeError.INPUT_DOESNT_PRODUCE_OUTPUT)
         );
   }

   @Override
   protected void drawForegroundText(@NotNull GuiGraphics guiGraphics, int mouseX, int mouseY) {
      this.drawString(guiGraphics, this.f_96539_, this.f_97728_, this.f_97729_, this.titleTextColor());
      this.drawString(guiGraphics, this.f_169604_, this.f_97730_, this.f_97731_, this.titleTextColor());
      super.drawForegroundText(guiGraphics, mouseX, mouseY);
   }

   private class LeftColorDetails extends GuiPigmentMixer.PigmentColorDetails {
      @Override
      public int getColorFrom() {
         return GuiPigmentMixer.this.tile == null ? -1 : this.getColor(GuiPigmentMixer.this.tile.leftInputTank.getType().getColorRepresentation());
      }
   }

   private abstract class PigmentColorDetails implements GuiProgress.ColorDetails {
      private WeakReference<PigmentMixingRecipe> cachedRecipe;

      @Override
      public abstract int getColorFrom();

      @Override
      public int getColorTo() {
         if (GuiPigmentMixer.this.tile == null) {
            return -1;
         } else if (!GuiPigmentMixer.this.tile.outputTank.isEmpty()) {
            return this.getColor(GuiPigmentMixer.this.tile.outputTank.getType().getColorRepresentation());
         } else {
            if (!GuiPigmentMixer.this.tile.leftInputTank.isEmpty() && !GuiPigmentMixer.this.tile.rightInputTank.isEmpty()) {
               PigmentStack leftInput = GuiPigmentMixer.this.tile.leftInputTank.getStack();
               PigmentStack rightInput = GuiPigmentMixer.this.tile.rightInputTank.getStack();
               PigmentMixingRecipe recipe;
               if (this.cachedRecipe == null) {
                  recipe = this.getRecipeAndCache();
               } else {
                  recipe = this.cachedRecipe.get();
                  if (recipe == null || !this.isValid(recipe, leftInput, rightInput)) {
                     recipe = this.getRecipeAndCache();
                  }
               }

               if (recipe != null) {
                  return this.getColor(recipe.getOutput(leftInput, rightInput).getChemicalColorRepresentation());
               }
            }

            return -1;
         }
      }

      private PigmentMixingRecipe getRecipeAndCache() {
         PigmentMixingRecipe recipe = GuiPigmentMixer.this.tile.getRecipe(0);
         if (recipe == null) {
            this.cachedRecipe = null;
         } else {
            this.cachedRecipe = new WeakReference<>(recipe);
         }

         return recipe;
      }

      private boolean isValid(PigmentMixingRecipe recipe, PigmentStack leftInput, PigmentStack rightInput) {
         return recipe.getLeftInput().testType(leftInput) && recipe.getRightInput().testType(rightInput)
            || recipe.getLeftInput().testType(rightInput) && recipe.getRightInput().testType(leftInput);
      }

      protected int getColor(int tint) {
         return (tint & 0xFF000000) == 0 ? 0xFF000000 | tint : tint;
      }
   }

   private class RightColorDetails extends GuiPigmentMixer.PigmentColorDetails {
      @Override
      public int getColorFrom() {
         return GuiPigmentMixer.this.tile == null ? -1 : this.getColor(GuiPigmentMixer.this.tile.rightInputTank.getType().getColorRepresentation());
      }
   }
}
