package mekanism.client.jei.machine;

import mekanism.api.providers.IBlockProvider;
import mekanism.api.recipes.ItemStackToItemStackRecipe;
import mekanism.client.gui.element.GuiUpArrow;
import mekanism.client.gui.element.bar.GuiVerticalPowerBar;
import mekanism.client.gui.element.progress.ProgressType;
import mekanism.client.gui.element.slot.GuiSlot;
import mekanism.client.gui.element.slot.SlotType;
import mekanism.client.jei.BaseRecipeCategory;
import mekanism.client.jei.MekanismJEIRecipeType;
import mekanism.common.inventory.container.slot.SlotOverlay;
import mezz.jei.api.gui.builder.IRecipeLayoutBuilder;
import mezz.jei.api.helpers.IGuiHelper;
import mezz.jei.api.recipe.IFocusGroup;
import mezz.jei.api.recipe.RecipeIngredientRole;
import org.jetbrains.annotations.NotNull;

public class ItemStackToItemStackRecipeCategory extends BaseRecipeCategory<ItemStackToItemStackRecipe> {
   private final GuiSlot input;
   private final GuiSlot output;

   public ItemStackToItemStackRecipeCategory(IGuiHelper helper, MekanismJEIRecipeType<ItemStackToItemStackRecipe> recipeType, IBlockProvider mekanismBlock) {
      super(helper, recipeType, mekanismBlock, 28, 16, 144, 54);
      this.addElement(new GuiUpArrow(this, 68, 38));
      this.input = this.addSlot(SlotType.INPUT, 64, 17);
      this.output = this.addSlot(SlotType.OUTPUT, 116, 35);
      this.addSlot(SlotType.POWER, 64, 53).with(SlotOverlay.POWER);
      this.addElement(new GuiVerticalPowerBar(this, FULL_BAR, 164, 15));
      this.addSimpleProgress(ProgressType.BAR, 86, 38);
   }

   public void setRecipe(@NotNull IRecipeLayoutBuilder builder, ItemStackToItemStackRecipe recipe, @NotNull IFocusGroup focusGroup) {
      this.initItem(builder, RecipeIngredientRole.INPUT, this.input, recipe.getInput().getRepresentations());
      this.initItem(builder, RecipeIngredientRole.OUTPUT, this.output, recipe.getOutputDefinition());
   }
}
