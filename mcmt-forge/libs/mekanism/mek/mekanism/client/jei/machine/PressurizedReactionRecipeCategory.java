package mekanism.client.jei.machine;

import java.util.ArrayList;
import java.util.List;
import mekanism.api.chemical.ChemicalStack;
import mekanism.api.chemical.gas.GasStack;
import mekanism.api.recipes.PressurizedReactionRecipe;
import mekanism.client.gui.element.bar.GuiVerticalPowerBar;
import mekanism.client.gui.element.gauge.GaugeType;
import mekanism.client.gui.element.gauge.GuiFluidGauge;
import mekanism.client.gui.element.gauge.GuiGasGauge;
import mekanism.client.gui.element.gauge.GuiGauge;
import mekanism.client.gui.element.progress.ProgressType;
import mekanism.client.gui.element.slot.GuiSlot;
import mekanism.client.gui.element.slot.SlotType;
import mekanism.client.jei.BaseRecipeCategory;
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
import net.minecraft.world.item.ItemStack;
import org.jetbrains.annotations.NotNull;

public class PressurizedReactionRecipeCategory extends BaseRecipeCategory<PressurizedReactionRecipe> {
   private static final String OUTPUT_GAS = "outputGas";
   private final GuiGauge<?> inputGas;
   private final GuiGauge<?> inputFluid;
   private final GuiSlot inputItem = this.addSlot(SlotType.INPUT, 54, 35);
   private final GuiSlot outputItem = this.addSlot(SlotType.OUTPUT, 116, 35);
   private final GuiGauge<?> outputGas;

   public PressurizedReactionRecipeCategory(IGuiHelper helper, MekanismJEIRecipeType<PressurizedReactionRecipe> recipeType) {
      super(helper, recipeType, MekanismBlocks.PRESSURIZED_REACTION_CHAMBER, 3, 10, 170, 60);
      this.addSlot(SlotType.POWER, 141, 17).with(SlotOverlay.POWER);
      this.inputFluid = this.addElement(GuiFluidGauge.getDummy(GaugeType.STANDARD.with(DataType.INPUT), this, 5, 10));
      this.inputGas = this.addElement(GuiGasGauge.getDummy(GaugeType.STANDARD.with(DataType.INPUT), this, 28, 10));
      this.outputGas = this.addElement(GuiGasGauge.getDummy(GaugeType.SMALL.with(DataType.OUTPUT), this, 140, 40));
      this.addElement(new GuiVerticalPowerBar(this, FULL_BAR, 164, 15));
      this.addSimpleProgress(ProgressType.RIGHT, 77, 38);
   }

   protected void renderElements(PressurizedReactionRecipe recipe, IRecipeSlotsView recipeSlotsView, GuiGraphics guiGraphics, int x, int y) {
      super.renderElements(recipe, recipeSlotsView, guiGraphics, x, y);
      if (recipeSlotsView.findSlotByName("outputGas").isEmpty()) {
         this.outputGas.drawBarOverlay(guiGraphics);
      }
   }

   public void setRecipe(@NotNull IRecipeLayoutBuilder builder, PressurizedReactionRecipe recipe, @NotNull IFocusGroup focusGroup) {
      this.initItem(builder, RecipeIngredientRole.INPUT, this.inputItem, recipe.getInputSolid().getRepresentations());
      this.initFluid(builder, RecipeIngredientRole.INPUT, this.inputFluid, recipe.getInputFluid().getRepresentations());
      this.initChemical(builder, MekanismJEI.TYPE_GAS, RecipeIngredientRole.INPUT, this.inputGas, recipe.getInputGas().getRepresentations());
      List<ItemStack> itemOutputs = new ArrayList<>();
      List<GasStack> gasOutputs = new ArrayList<>();

      for (PressurizedReactionRecipe.PressurizedReactionRecipeOutput output : recipe.getOutputDefinition()) {
         itemOutputs.add(output.item());
         gasOutputs.add(output.gas());
      }

      if (!itemOutputs.stream().allMatch(ItemStack::m_41619_)) {
         this.initItem(builder, RecipeIngredientRole.OUTPUT, this.outputItem, itemOutputs);
      }

      if (!gasOutputs.stream().allMatch(ChemicalStack::isEmpty)) {
         this.initChemical(builder, MekanismJEI.TYPE_GAS, RecipeIngredientRole.OUTPUT, this.outputGas, gasOutputs).setSlotName("outputGas");
      }
   }
}
