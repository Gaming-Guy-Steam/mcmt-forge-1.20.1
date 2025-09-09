package mekanism.client.jei.machine;

import mekanism.api.chemical.pigment.Pigment;
import mekanism.api.chemical.pigment.PigmentStack;
import mekanism.api.recipes.PaintingRecipe;
import mekanism.client.gui.element.bar.GuiVerticalPowerBar;
import mekanism.client.gui.element.gauge.GaugeType;
import mekanism.client.gui.element.gauge.GuiGauge;
import mekanism.client.gui.element.gauge.GuiPigmentGauge;
import mekanism.client.gui.element.progress.ProgressType;
import mekanism.client.gui.element.slot.GuiSlot;
import mekanism.client.gui.element.slot.SlotType;
import mekanism.client.jei.BaseRecipeCategory;
import mekanism.client.jei.JEIColorDetails;
import mekanism.client.jei.MekanismJEI;
import mekanism.client.jei.MekanismJEIRecipeType;
import mekanism.common.inventory.container.slot.SlotOverlay;
import mekanism.common.registries.MekanismBlocks;
import mekanism.common.tile.component.config.DataType;
import mezz.jei.api.gui.builder.IRecipeLayoutBuilder;
import mezz.jei.api.gui.ingredient.IRecipeSlotsView;
import mezz.jei.api.helpers.IGuiHelper;
import mezz.jei.api.recipe.IFocusGroup;
import mezz.jei.api.recipe.RecipeIngredientRole;
import net.minecraft.client.gui.GuiGraphics;
import org.jetbrains.annotations.NotNull;

public class PaintingRecipeCategory extends BaseRecipeCategory<PaintingRecipe> {
   private static final String PIGMENT_INPUT = "pigmentInput";
   private final PaintingRecipeCategory.PigmentColorDetails colorDetails;
   private final GuiGauge<?> inputPigment;
   private final GuiSlot inputSlot = this.addSlot(SlotType.INPUT, 45, 35);
   private final GuiSlot output;

   public PaintingRecipeCategory(IGuiHelper helper, MekanismJEIRecipeType<PaintingRecipe> recipeType) {
      super(helper, recipeType, MekanismBlocks.PAINTING_MACHINE, 25, 13, 146, 60);
      this.addSlot(SlotType.POWER, 144, 35).with(SlotOverlay.POWER);
      this.output = this.addSlot(SlotType.OUTPUT, 116, 35);
      this.addElement(new GuiVerticalPowerBar(this, FULL_BAR, 164, 15));
      this.inputPigment = this.addElement(GuiPigmentGauge.getDummy(GaugeType.STANDARD.with(DataType.INPUT), this, 25, 13));
      this.addSimpleProgress(ProgressType.LARGE_RIGHT, 64, 39).colored(this.colorDetails = new PaintingRecipeCategory.PigmentColorDetails());
   }

   public void draw(PaintingRecipe recipe, IRecipeSlotsView recipeSlotsView, GuiGraphics guiGraphics, double mouseX, double mouseY) {
      this.colorDetails.ingredient = this.getDisplayedStack(recipeSlotsView, "pigmentInput", MekanismJEI.TYPE_PIGMENT, PigmentStack.EMPTY);
      super.draw(recipe, recipeSlotsView, guiGraphics, mouseX, mouseY);
      this.colorDetails.reset();
   }

   public void setRecipe(@NotNull IRecipeLayoutBuilder builder, PaintingRecipe recipe, @NotNull IFocusGroup focusGroup) {
      this.initItem(builder, RecipeIngredientRole.INPUT, this.inputSlot, recipe.getItemInput().getRepresentations());
      this.initChemical(builder, MekanismJEI.TYPE_PIGMENT, RecipeIngredientRole.INPUT, this.inputPigment, recipe.getChemicalInput().getRepresentations())
         .setSlotName("pigmentInput");
      this.initItem(builder, RecipeIngredientRole.OUTPUT, this.output, recipe.getOutputDefinition());
   }

   private static class PigmentColorDetails extends JEIColorDetails<Pigment, PigmentStack> {
      private PigmentColorDetails() {
         super(PigmentStack.EMPTY);
      }

      @Override
      public int getColorFrom() {
         return this.getColor(this.ingredient);
      }

      @Override
      public int getColorTo() {
         return -1;
      }
   }
}
