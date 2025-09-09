package mekanism.client.jei.machine;

import mekanism.api.recipes.FluidSlurryToSlurryRecipe;
import mekanism.client.gui.element.gauge.GaugeType;
import mekanism.client.gui.element.gauge.GuiFluidGauge;
import mekanism.client.gui.element.gauge.GuiGauge;
import mekanism.client.gui.element.gauge.GuiSlurryGauge;
import mekanism.client.gui.element.progress.ProgressType;
import mekanism.client.gui.element.slot.SlotType;
import mekanism.client.jei.BaseRecipeCategory;
import mekanism.client.jei.MekanismJEI;
import mekanism.client.jei.MekanismJEIRecipeType;
import mekanism.common.inventory.container.slot.SlotOverlay;
import mekanism.common.registries.MekanismBlocks;
import mekanism.common.tile.component.config.DataType;
import mezz.jei.api.gui.builder.IRecipeLayoutBuilder;
import mezz.jei.api.helpers.IGuiHelper;
import mezz.jei.api.recipe.IFocusGroup;
import mezz.jei.api.recipe.RecipeIngredientRole;
import org.jetbrains.annotations.NotNull;

public class FluidSlurryToSlurryRecipeCategory extends BaseRecipeCategory<FluidSlurryToSlurryRecipe> {
   private final GuiGauge<?> fluidInput = this.addElement(GuiFluidGauge.getDummy(GaugeType.STANDARD.with(DataType.INPUT), this, 7, 13));
   private final GuiGauge<?> slurryInput = this.addElement(GuiSlurryGauge.getDummy(GaugeType.STANDARD.with(DataType.INPUT), this, 28, 13));
   private final GuiGauge<?> output = this.addElement(GuiSlurryGauge.getDummy(GaugeType.STANDARD.with(DataType.OUTPUT), this, 131, 13));

   public FluidSlurryToSlurryRecipeCategory(IGuiHelper helper, MekanismJEIRecipeType<FluidSlurryToSlurryRecipe> recipeType) {
      super(helper, recipeType, MekanismBlocks.CHEMICAL_WASHER, 7, 13, 162, 60);
      this.addSlot(SlotType.POWER, 152, 14).with(SlotOverlay.POWER);
      this.addSlot(SlotType.OUTPUT, 152, 56).with(SlotOverlay.MINUS);
      this.addConstantProgress(ProgressType.LARGE_RIGHT, 64, 39);
   }

   public void setRecipe(@NotNull IRecipeLayoutBuilder builder, FluidSlurryToSlurryRecipe recipe, @NotNull IFocusGroup focusGroup) {
      this.initFluid(builder, RecipeIngredientRole.INPUT, this.fluidInput, recipe.getFluidInput().getRepresentations());
      this.initChemical(builder, MekanismJEI.TYPE_SLURRY, RecipeIngredientRole.INPUT, this.slurryInput, recipe.getChemicalInput().getRepresentations());
      this.initChemical(builder, MekanismJEI.TYPE_SLURRY, RecipeIngredientRole.OUTPUT, this.output, recipe.getOutputDefinition());
   }
}
