package mekanism.client.jei.machine;

import mekanism.api.providers.IItemProvider;
import mekanism.api.recipes.ItemStackToFluidRecipe;
import mekanism.client.gui.element.gauge.GaugeType;
import mekanism.client.gui.element.gauge.GuiFluidGauge;
import mekanism.client.gui.element.gauge.GuiGauge;
import mekanism.client.gui.element.progress.GuiProgress;
import mekanism.client.gui.element.progress.ProgressType;
import mekanism.client.gui.element.slot.GuiSlot;
import mekanism.client.gui.element.slot.SlotType;
import mekanism.client.jei.BaseRecipeCategory;
import mekanism.client.jei.MekanismJEIRecipeType;
import mekanism.common.tile.component.config.DataType;
import mezz.jei.api.gui.builder.IRecipeLayoutBuilder;
import mezz.jei.api.gui.drawable.IDrawable;
import mezz.jei.api.helpers.IGuiHelper;
import mezz.jei.api.recipe.IFocusGroup;
import mezz.jei.api.recipe.RecipeIngredientRole;
import net.minecraft.network.chat.Component;
import org.jetbrains.annotations.NotNull;

public class ItemStackToFluidRecipeCategory extends BaseRecipeCategory<ItemStackToFluidRecipe> {
   protected final GuiProgress progressBar;
   private final GuiGauge<?> output = this.addElement(GuiFluidGauge.getDummy(GaugeType.STANDARD.with(DataType.OUTPUT), this, 131, 13));
   private final GuiSlot input = this.addSlot(SlotType.INPUT, 26, 36);

   public ItemStackToFluidRecipeCategory(
      IGuiHelper helper, MekanismJEIRecipeType<ItemStackToFluidRecipe> recipeType, IItemProvider provider, boolean isConversion
   ) {
      this(helper, recipeType, provider.getTextComponent(), createIcon(helper, provider), isConversion);
   }

   public ItemStackToFluidRecipeCategory(
      IGuiHelper helper, MekanismJEIRecipeType<ItemStackToFluidRecipe> recipeType, Component component, IDrawable icon, boolean isConversion
   ) {
      super(helper, recipeType, component, icon, 20, 12, 132, 62);
      this.progressBar = this.addElement(new GuiProgress(isConversion ? () -> 1.0 : this.getSimpleProgressTimer(), ProgressType.LARGE_RIGHT, this, 64, 40));
   }

   public void setRecipe(@NotNull IRecipeLayoutBuilder builder, ItemStackToFluidRecipe recipe, @NotNull IFocusGroup focusGroup) {
      this.initItem(builder, RecipeIngredientRole.INPUT, this.input, recipe.getInput().getRepresentations());
      this.initFluid(builder, RecipeIngredientRole.OUTPUT, this.output, recipe.getOutputDefinition());
   }
}
