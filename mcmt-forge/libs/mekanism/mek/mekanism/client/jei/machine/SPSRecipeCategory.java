package mekanism.client.jei.machine;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import mekanism.api.recipes.ingredients.ChemicalStackIngredient;
import mekanism.api.recipes.ingredients.creator.IngredientCreatorAccess;
import mekanism.client.gui.element.GuiInnerScreen;
import mekanism.client.gui.element.bar.GuiDynamicHorizontalRateBar;
import mekanism.client.gui.element.gauge.GaugeType;
import mekanism.client.gui.element.gauge.GuiGasGauge;
import mekanism.client.gui.element.gauge.GuiGauge;
import mekanism.client.jei.BaseRecipeCategory;
import mekanism.client.jei.MekanismJEI;
import mekanism.client.jei.MekanismJEIRecipeType;
import mekanism.client.jei.recipe.SPSJEIRecipe;
import mekanism.common.MekanismLang;
import mekanism.common.config.MekanismConfig;
import mekanism.common.lib.Color;
import mekanism.common.registries.MekanismGases;
import mekanism.common.registries.MekanismItems;
import mekanism.common.util.text.EnergyDisplay;
import mezz.jei.api.gui.builder.IRecipeLayoutBuilder;
import mezz.jei.api.helpers.IGuiHelper;
import mezz.jei.api.recipe.IFocusGroup;
import mezz.jei.api.recipe.RecipeIngredientRole;
import net.minecraft.network.chat.Component;
import org.jetbrains.annotations.NotNull;

public class SPSRecipeCategory extends BaseRecipeCategory<SPSJEIRecipe> {
   private final GuiGauge<?> input;
   private final GuiGauge<?> output;

   public SPSRecipeCategory(IGuiHelper helper, MekanismJEIRecipeType<SPSJEIRecipe> recipeType) {
      super(helper, recipeType, MekanismLang.SPS.translate(new Object[0]), createIcon(helper, MekanismItems.ANTIMATTER_PELLET), 3, 12, 168, 74);
      this.addElement(
         new GuiInnerScreen(
            this,
            26,
            13,
            122,
            60,
            () -> {
               List<Component> list = new ArrayList<>();
               list.add(MekanismLang.STATUS.translate(new Object[]{MekanismLang.ACTIVE}));
               list.add(
                  MekanismLang.SPS_ENERGY_INPUT
                     .translate(
                        new Object[]{
                           EnergyDisplay.of(MekanismConfig.general.spsEnergyPerInput.get().multiply((long)MekanismConfig.general.spsInputPerAntimatter.get()))
                        }
                     )
               );
               list.add(MekanismLang.PROCESS_RATE_MB.translate(new Object[]{1.0}));
               return list;
            }
         )
      );
      this.input = this.addElement(GuiGasGauge.getDummy(GaugeType.STANDARD, this, 6, 13));
      this.output = this.addElement(GuiGasGauge.getDummy(GaugeType.STANDARD, this, 150, 13));
      this.addElement(
         new GuiDynamicHorizontalRateBar(
            this, this.getBarProgressTimer(), 6, 75, 160, Color.ColorFunction.scale(Color.rgbi(60, 45, 74), Color.rgbi(100, 30, 170))
         )
      );
   }

   public void setRecipe(@NotNull IRecipeLayoutBuilder builder, SPSJEIRecipe recipe, @NotNull IFocusGroup focusGroup) {
      this.initChemical(builder, MekanismJEI.TYPE_GAS, RecipeIngredientRole.INPUT, this.input, recipe.input().getRepresentations());
      this.initChemical(builder, MekanismJEI.TYPE_GAS, RecipeIngredientRole.OUTPUT, this.output, Collections.singletonList(recipe.output()));
   }

   public static List<SPSJEIRecipe> getSPSRecipes() {
      return Collections.singletonList(
         new SPSJEIRecipe(
            (ChemicalStackIngredient.GasStackIngredient)IngredientCreatorAccess.gas()
               .from(MekanismGases.POLONIUM, (long)MekanismConfig.general.spsInputPerAntimatter.get()),
            MekanismGases.ANTIMATTER.getStack(1L)
         )
      );
   }
}
