package mekanism.client.jei.machine;

import mekanism.api.recipes.SawmillRecipe;
import mekanism.client.SpecialColors;
import mekanism.client.gui.element.GuiUpArrow;
import mekanism.client.gui.element.bar.GuiVerticalPowerBar;
import mekanism.client.gui.element.progress.ProgressType;
import mekanism.client.gui.element.slot.GuiSlot;
import mekanism.client.gui.element.slot.SlotType;
import mekanism.client.jei.BaseRecipeCategory;
import mekanism.client.jei.MekanismJEIRecipeType;
import mekanism.common.inventory.container.slot.SlotOverlay;
import mekanism.common.registries.MekanismBlocks;
import mekanism.common.util.text.TextUtils;
import mezz.jei.api.gui.builder.IRecipeLayoutBuilder;
import mezz.jei.api.gui.ingredient.IRecipeSlotsView;
import mezz.jei.api.helpers.IGuiHelper;
import mezz.jei.api.recipe.IFocusGroup;
import mezz.jei.api.recipe.RecipeIngredientRole;
import net.minecraft.client.gui.GuiGraphics;
import org.jetbrains.annotations.NotNull;

public class SawmillRecipeCategory extends BaseRecipeCategory<SawmillRecipe> {
   private final GuiSlot input;
   private final GuiSlot output;

   public SawmillRecipeCategory(IGuiHelper helper, MekanismJEIRecipeType<SawmillRecipe> recipeType) {
      super(helper, recipeType, MekanismBlocks.PRECISION_SAWMILL, 28, 16, 144, 54);
      this.addElement(new GuiUpArrow(this, 60, 38));
      this.input = this.addSlot(SlotType.INPUT, 56, 17);
      this.addSlot(SlotType.POWER, 56, 53).with(SlotOverlay.POWER);
      this.output = this.addSlot(SlotType.OUTPUT_WIDE, 112, 31);
      this.addElement(new GuiVerticalPowerBar(this, FULL_BAR, 164, 15));
      this.addSimpleProgress(ProgressType.BAR, 78, 38);
   }

   public void setRecipe(@NotNull IRecipeLayoutBuilder builder, SawmillRecipe recipe, @NotNull IFocusGroup focusGroup) {
      this.initItem(builder, RecipeIngredientRole.INPUT, this.input, recipe.getInput().getRepresentations());
      this.initItem(builder, RecipeIngredientRole.OUTPUT, this.output.getRelativeX() + 4, this.output.getRelativeY() + 4, recipe.getMainOutputDefinition());
      this.initItem(
         builder, RecipeIngredientRole.OUTPUT, this.output.getRelativeX() + 20, this.output.getRelativeY() + 4, recipe.getSecondaryOutputDefinition()
      );
   }

   public void draw(SawmillRecipe recipe, IRecipeSlotsView recipeSlotView, GuiGraphics guiGraphics, double mouseX, double mouseY) {
      super.draw(recipe, recipeSlotView, guiGraphics, mouseX, mouseY);
      double secondaryChance = recipe.getSecondaryChance();
      if (secondaryChance > 0.0) {
         guiGraphics.m_280614_(this.getFont(), TextUtils.getPercent(secondaryChance), 104, 41, SpecialColors.TEXT_TITLE.argb(), false);
      }
   }
}
