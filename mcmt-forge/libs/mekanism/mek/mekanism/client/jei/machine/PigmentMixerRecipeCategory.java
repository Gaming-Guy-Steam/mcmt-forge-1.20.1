package mekanism.client.jei.machine;

import mekanism.api.chemical.pigment.Pigment;
import mekanism.api.chemical.pigment.PigmentStack;
import mekanism.api.recipes.PigmentMixingRecipe;
import mekanism.client.gui.element.gauge.GaugeType;
import mekanism.client.gui.element.gauge.GuiChemicalGauge;
import mekanism.client.gui.element.gauge.GuiPigmentGauge;
import mekanism.client.jei.JEIColorDetails;
import mekanism.client.jei.MekanismJEI;
import mekanism.client.jei.MekanismJEIRecipeType;
import mekanism.common.registries.MekanismBlocks;
import mezz.jei.api.gui.ingredient.IRecipeSlotsView;
import mezz.jei.api.helpers.IGuiHelper;
import net.minecraft.client.gui.GuiGraphics;

public class PigmentMixerRecipeCategory extends ChemicalChemicalToChemicalRecipeCategory<Pigment, PigmentStack, PigmentMixingRecipe> {
   private final PigmentMixerRecipeCategory.PigmentColorDetails leftColorDetails;
   private final PigmentMixerRecipeCategory.PigmentColorDetails rightColorDetails;

   public PigmentMixerRecipeCategory(IGuiHelper helper, MekanismJEIRecipeType<PigmentMixingRecipe> recipeType) {
      super(helper, recipeType, MekanismBlocks.PIGMENT_MIXER, MekanismJEI.TYPE_PIGMENT, 3, 3, 170, 80);
      this.rightArrow.colored(this.leftColorDetails = new PigmentMixerRecipeCategory.PigmentColorDetails());
      this.leftArrow.colored(this.rightColorDetails = new PigmentMixerRecipeCategory.PigmentColorDetails());
   }

   @Override
   protected GuiChemicalGauge<Pigment, PigmentStack, ?> getGauge(GaugeType type, int x, int y) {
      return GuiPigmentGauge.getDummy(type, this, x, y);
   }

   public void draw(PigmentMixingRecipe recipe, IRecipeSlotsView recipeSlotsView, GuiGraphics guiGraphics, double mouseX, double mouseY) {
      this.leftColorDetails.ingredient = this.getDisplayedStack(recipeSlotsView, "leftInput", MekanismJEI.TYPE_PIGMENT, PigmentStack.EMPTY);
      this.rightColorDetails.ingredient = this.getDisplayedStack(recipeSlotsView, "rightInput", MekanismJEI.TYPE_PIGMENT, PigmentStack.EMPTY);
      this.leftColorDetails.outputIngredient = this.getDisplayedStack(recipeSlotsView, "output", MekanismJEI.TYPE_PIGMENT, PigmentStack.EMPTY);
      this.rightColorDetails.outputIngredient = this.leftColorDetails.outputIngredient;
      super.draw(recipe, recipeSlotsView, guiGraphics, mouseX, mouseY);
      this.leftColorDetails.reset();
      this.rightColorDetails.reset();
   }

   private static class PigmentColorDetails extends JEIColorDetails<Pigment, PigmentStack> {
      private PigmentStack outputIngredient = PigmentStack.EMPTY;

      private PigmentColorDetails() {
         super(PigmentStack.EMPTY);
      }

      @Override
      public void reset() {
         super.reset();
         this.outputIngredient = this.empty;
      }

      @Override
      public int getColorFrom() {
         return this.getColor(this.ingredient);
      }

      @Override
      public int getColorTo() {
         return this.getColor(this.outputIngredient);
      }
   }
}
