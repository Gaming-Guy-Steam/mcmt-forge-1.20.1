package mekanism.client.jei.machine;

import java.util.List;
import mekanism.api.recipes.FluidToFluidRecipe;
import mekanism.client.gui.element.GuiDownArrow;
import mekanism.client.gui.element.GuiInnerScreen;
import mekanism.client.gui.element.bar.GuiHorizontalRateBar;
import mekanism.client.gui.element.gauge.GaugeType;
import mekanism.client.gui.element.gauge.GuiFluidGauge;
import mekanism.client.gui.element.gauge.GuiGauge;
import mekanism.client.gui.element.slot.SlotType;
import mekanism.client.jei.BaseRecipeCategory;
import mekanism.client.jei.MekanismJEIRecipeType;
import mekanism.common.MekanismLang;
import mekanism.common.registries.MekanismBlocks;
import mekanism.common.util.MekanismUtils;
import mekanism.common.util.UnitDisplayUtils;
import mezz.jei.api.gui.builder.IRecipeLayoutBuilder;
import mezz.jei.api.helpers.IGuiHelper;
import mezz.jei.api.recipe.IFocusGroup;
import mezz.jei.api.recipe.RecipeIngredientRole;
import org.jetbrains.annotations.NotNull;

public class FluidToFluidRecipeCategory extends BaseRecipeCategory<FluidToFluidRecipe> {
   private final GuiGauge<?> input;
   private final GuiGauge<?> output;

   public FluidToFluidRecipeCategory(IGuiHelper helper, MekanismJEIRecipeType<FluidToFluidRecipe> recipeType) {
      super(helper, recipeType, MekanismBlocks.THERMAL_EVAPORATION_CONTROLLER, 3, 12, 170, 62);
      this.addElement(
         new GuiInnerScreen(
               this,
               48,
               19,
               80,
               40,
               () -> List.of(
                  MekanismLang.MULTIBLOCK_FORMED.translate(new Object[0]),
                  MekanismLang.EVAPORATION_HEIGHT.translate(new Object[]{18}),
                  MekanismLang.TEMPERATURE.translate(new Object[]{MekanismUtils.getTemperatureDisplay(300.0, UnitDisplayUtils.TemperatureUnit.KELVIN, true)}),
                  MekanismLang.FLUID_PRODUCTION.translate(new Object[]{0.0})
               )
            )
            .spacing(1)
      );
      this.addElement(new GuiDownArrow(this, 32, 39));
      this.addElement(new GuiDownArrow(this, 136, 39));
      this.addElement(new GuiHorizontalRateBar(this, FULL_BAR, 48, 63));
      this.addSlot(SlotType.INPUT, 28, 20);
      this.addSlot(SlotType.OUTPUT, 28, 51);
      this.addSlot(SlotType.INPUT, 132, 20);
      this.addSlot(SlotType.OUTPUT, 132, 51);
      this.input = this.addElement(GuiFluidGauge.getDummy(GaugeType.STANDARD, this, 6, 13));
      this.output = this.addElement(GuiFluidGauge.getDummy(GaugeType.STANDARD, this, 152, 13));
   }

   public void setRecipe(@NotNull IRecipeLayoutBuilder builder, FluidToFluidRecipe recipe, @NotNull IFocusGroup focusGroup) {
      this.initFluid(builder, RecipeIngredientRole.INPUT, this.input, recipe.getInput().getRepresentations());
      this.initFluid(builder, RecipeIngredientRole.OUTPUT, this.output, recipe.getOutputDefinition());
   }
}
