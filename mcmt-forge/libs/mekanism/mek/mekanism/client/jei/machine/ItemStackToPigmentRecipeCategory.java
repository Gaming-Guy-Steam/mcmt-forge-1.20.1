package mekanism.client.jei.machine;

import mekanism.api.chemical.pigment.Pigment;
import mekanism.api.chemical.pigment.PigmentStack;
import mekanism.api.providers.IItemProvider;
import mekanism.api.recipes.ItemStackToPigmentRecipe;
import mekanism.client.gui.element.gauge.GaugeType;
import mekanism.client.gui.element.gauge.GuiPigmentGauge;
import mekanism.client.jei.JEIColorDetails;
import mekanism.client.jei.MekanismJEI;
import mekanism.client.jei.MekanismJEIRecipeType;
import mezz.jei.api.gui.ingredient.IRecipeSlotsView;
import mezz.jei.api.helpers.IGuiHelper;
import net.minecraft.client.gui.GuiGraphics;

public class ItemStackToPigmentRecipeCategory extends ItemStackToChemicalRecipeCategory<Pigment, PigmentStack, ItemStackToPigmentRecipe> {
   private final ItemStackToPigmentRecipeCategory.PigmentColorDetails currentDetails;

   public ItemStackToPigmentRecipeCategory(IGuiHelper helper, MekanismJEIRecipeType<ItemStackToPigmentRecipe> recipeType, IItemProvider mekanismBlock) {
      super(helper, recipeType, mekanismBlock, MekanismJEI.TYPE_PIGMENT, false);
      this.progressBar.colored(this.currentDetails = new ItemStackToPigmentRecipeCategory.PigmentColorDetails());
   }

   protected GuiPigmentGauge getGauge(GaugeType type, int x, int y) {
      return GuiPigmentGauge.getDummy(type, this, x, y);
   }

   public void draw(ItemStackToPigmentRecipe recipe, IRecipeSlotsView recipeSlotsView, GuiGraphics guiGraphics, double mouseX, double mouseY) {
      this.currentDetails.ingredient = this.getDisplayedStack(recipeSlotsView, "chemicalInput", MekanismJEI.TYPE_PIGMENT, PigmentStack.EMPTY);
      super.draw(recipe, recipeSlotsView, guiGraphics, mouseX, mouseY);
      this.currentDetails.reset();
   }

   private static class PigmentColorDetails extends JEIColorDetails<Pigment, PigmentStack> {
      private PigmentColorDetails() {
         super(PigmentStack.EMPTY);
      }

      @Override
      public int getColorFrom() {
         return -1;
      }

      @Override
      public int getColorTo() {
         return this.getColor(this.ingredient);
      }
   }
}
