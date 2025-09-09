package mekanism.client.jei.machine;

import mekanism.api.recipes.RotaryRecipe;
import mekanism.client.gui.element.GuiDownArrow;
import mekanism.client.gui.element.gauge.GaugeType;
import mekanism.client.gui.element.gauge.GuiFluidGauge;
import mekanism.client.gui.element.gauge.GuiGasGauge;
import mekanism.client.gui.element.gauge.GuiGauge;
import mekanism.client.gui.element.progress.ProgressType;
import mekanism.client.gui.element.slot.SlotType;
import mekanism.client.jei.BaseRecipeCategory;
import mekanism.client.jei.MekanismJEI;
import mekanism.client.jei.MekanismJEIRecipeType;
import mekanism.common.MekanismLang;
import mekanism.common.inventory.container.slot.SlotOverlay;
import mekanism.common.registries.MekanismBlocks;
import mezz.jei.api.gui.builder.IRecipeLayoutBuilder;
import mezz.jei.api.helpers.IGuiHelper;
import mezz.jei.api.recipe.IFocusGroup;
import mezz.jei.api.recipe.RecipeIngredientRole;
import org.jetbrains.annotations.NotNull;

public class RotaryCondensentratorRecipeCategory extends BaseRecipeCategory<RotaryRecipe> {
   private final boolean condensentrating;
   private final GuiGauge<?> gasGauge;
   private final GuiGauge<?> fluidGauge;

   public RotaryCondensentratorRecipeCategory(IGuiHelper helper, boolean condensentrating) {
      super(
         helper,
         condensentrating ? MekanismJEIRecipeType.CONDENSENTRATING : MekanismJEIRecipeType.DECONDENSENTRATING,
         (condensentrating ? MekanismLang.CONDENSENTRATING : MekanismLang.DECONDENSENTRATING).translate(new Object[0]),
         createIcon(helper, MekanismBlocks.ROTARY_CONDENSENTRATOR),
         3,
         12,
         170,
         64
      );
      this.condensentrating = condensentrating;
      this.addElement(new GuiDownArrow(this, 159, 44));
      this.gasGauge = this.addElement(GuiGasGauge.getDummy(GaugeType.STANDARD, this, 25, 13));
      this.fluidGauge = this.addElement(GuiFluidGauge.getDummy(GaugeType.STANDARD, this, 133, 13));
      this.addSlot(SlotType.INPUT, 5, 25).with(SlotOverlay.PLUS);
      this.addSlot(SlotType.OUTPUT, 5, 56).with(SlotOverlay.MINUS);
      this.addSlot(SlotType.INPUT, 155, 25);
      this.addSlot(SlotType.OUTPUT, 155, 56);
      this.addConstantProgress(this.condensentrating ? ProgressType.LARGE_RIGHT : ProgressType.LARGE_LEFT, 64, 39);
   }

   public void setRecipe(@NotNull IRecipeLayoutBuilder builder, RotaryRecipe recipe, @NotNull IFocusGroup focusGroup) {
      if (this.condensentrating) {
         if (recipe.hasGasToFluid()) {
            this.initChemical(builder, MekanismJEI.TYPE_GAS, RecipeIngredientRole.INPUT, this.gasGauge, recipe.getGasInput().getRepresentations());
            this.initFluid(builder, RecipeIngredientRole.OUTPUT, this.fluidGauge, recipe.getFluidOutputDefinition());
         }
      } else if (recipe.hasFluidToGas()) {
         this.initFluid(builder, RecipeIngredientRole.INPUT, this.fluidGauge, recipe.getFluidInput().getRepresentations());
         this.initChemical(builder, MekanismJEI.TYPE_GAS, RecipeIngredientRole.OUTPUT, this.gasGauge, recipe.getGasOutputDefinition());
      }
   }
}
