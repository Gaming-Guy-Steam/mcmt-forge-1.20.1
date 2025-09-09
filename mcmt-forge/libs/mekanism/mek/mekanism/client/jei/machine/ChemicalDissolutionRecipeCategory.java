package mekanism.client.jei.machine;

import java.util.ArrayList;
import java.util.Collections;
import java.util.EnumMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import mekanism.api.chemical.ChemicalStack;
import mekanism.api.chemical.ChemicalType;
import mekanism.api.chemical.gas.GasStack;
import mekanism.api.chemical.merged.BoxedChemicalStack;
import mekanism.api.recipes.ChemicalDissolutionRecipe;
import mekanism.client.gui.element.bar.GuiHorizontalPowerBar;
import mekanism.client.gui.element.gauge.GaugeType;
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
import mekanism.common.util.ChemicalUtil;
import mezz.jei.api.gui.builder.IRecipeLayoutBuilder;
import mezz.jei.api.helpers.IGuiHelper;
import mezz.jei.api.ingredients.IIngredientType;
import mezz.jei.api.recipe.IFocusGroup;
import mezz.jei.api.recipe.RecipeIngredientRole;
import org.jetbrains.annotations.NotNull;

public class ChemicalDissolutionRecipeCategory extends BaseRecipeCategory<ChemicalDissolutionRecipe> {
   private final GuiGauge<?> inputGauge = this.addElement(GuiGasGauge.getDummy(GaugeType.STANDARD.with(DataType.INPUT), this, 7, 4));
   private final GuiGauge<?> outputGauge = this.addElement(GuiGasGauge.getDummy(GaugeType.STANDARD.with(DataType.OUTPUT), this, 131, 13));
   private final GuiSlot inputSlot = this.addSlot(SlotType.INPUT, 28, 36);

   public ChemicalDissolutionRecipeCategory(IGuiHelper helper, MekanismJEIRecipeType<ChemicalDissolutionRecipe> recipeType) {
      super(helper, recipeType, MekanismBlocks.CHEMICAL_DISSOLUTION_CHAMBER, 3, 3, 170, 79);
      this.addSlot(SlotType.EXTRA, 8, 65).with(SlotOverlay.MINUS);
      this.addSlot(SlotType.OUTPUT, 152, 55).with(SlotOverlay.PLUS);
      this.addSlot(SlotType.POWER, 152, 14).with(SlotOverlay.POWER);
      this.addSimpleProgress(ProgressType.LARGE_RIGHT, 64, 40);
      this.addElement(new GuiHorizontalPowerBar(this, FULL_BAR, 115, 75));
   }

   public void setRecipe(@NotNull IRecipeLayoutBuilder builder, ChemicalDissolutionRecipe recipe, @NotNull IFocusGroup focusGroup) {
      this.initItem(builder, RecipeIngredientRole.INPUT, this.inputSlot, recipe.getItemInput().getRepresentations());
      List<GasStack> gasInputs = recipe.getGasInput().getRepresentations();
      List<GasStack> scaledGases = gasInputs.stream().map(gas -> new GasStack(gas, gas.getAmount() * 100L)).toList();
      this.initChemical(builder, MekanismJEI.TYPE_GAS, RecipeIngredientRole.INPUT, this.inputGauge, scaledGases);
      List<BoxedChemicalStack> outputDefinition = recipe.getOutputDefinition();
      if (outputDefinition.size() == 1) {
         BoxedChemicalStack output = outputDefinition.get(0);
         this.initChemicalOutput(builder, MekanismJEI.getIngredientType(output.getChemicalType()), Collections.singletonList(output.getChemicalStack()));
      } else {
         Map<ChemicalType, List<ChemicalStack<?>>> outputs = new EnumMap<>(ChemicalType.class);

         for (BoxedChemicalStack output : outputDefinition) {
            outputs.computeIfAbsent(output.getChemicalType(), type -> new ArrayList<>());
         }

         for (BoxedChemicalStack output : outputDefinition) {
            ChemicalType chemicalType = output.getChemicalType();

            for (Entry<ChemicalType, List<ChemicalStack<?>>> entry : outputs.entrySet()) {
               if (entry.getKey() == chemicalType) {
                  entry.getValue().add(output.getChemicalStack());
               } else {
                  entry.getValue().add(ChemicalUtil.getEmptyStack(entry.getKey()));
               }
            }
         }

         for (Entry<ChemicalType, List<ChemicalStack<?>>> entryx : outputs.entrySet()) {
            this.initChemicalOutput(builder, MekanismJEI.getIngredientType(entryx.getKey()), entryx.getValue());
         }
      }
   }

   private <STACK extends ChemicalStack<?>> void initChemicalOutput(IRecipeLayoutBuilder builder, IIngredientType<STACK> type, List<ChemicalStack<?>> stacks) {
      this.initChemical(builder, type, RecipeIngredientRole.OUTPUT, this.outputGauge, stacks);
   }
}
