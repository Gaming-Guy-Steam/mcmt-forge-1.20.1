package mekanism.client.jei.machine;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import mekanism.api.chemical.gas.GasStack;
import mekanism.api.math.FloatingLong;
import mekanism.api.recipes.NucleosynthesizingRecipe;
import mekanism.client.gui.element.GuiInnerScreen;
import mekanism.client.gui.element.bar.GuiDynamicHorizontalRateBar;
import mekanism.client.gui.element.gauge.GaugeType;
import mekanism.client.gui.element.gauge.GuiEnergyGauge;
import mekanism.client.gui.element.gauge.GuiGasGauge;
import mekanism.client.gui.element.gauge.GuiGauge;
import mekanism.client.gui.element.slot.GuiSlot;
import mekanism.client.gui.element.slot.SlotType;
import mekanism.client.jei.BaseRecipeCategory;
import mekanism.client.jei.MekanismJEI;
import mekanism.client.jei.MekanismJEIRecipeType;
import mekanism.common.MekanismLang;
import mekanism.common.inventory.container.slot.SlotOverlay;
import mekanism.common.lib.Color;
import mekanism.common.registries.MekanismBlocks;
import mekanism.common.tile.component.config.DataType;
import mezz.jei.api.gui.builder.IRecipeLayoutBuilder;
import mezz.jei.api.gui.ingredient.IRecipeSlotsView;
import mezz.jei.api.helpers.IGuiHelper;
import mezz.jei.api.recipe.IFocusGroup;
import mezz.jei.api.recipe.RecipeIngredientRole;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.ItemStack;
import org.jetbrains.annotations.NotNull;

public class NucleosynthesizingRecipeCategory extends BaseRecipeCategory<NucleosynthesizingRecipe> {
   private final GuiDynamicHorizontalRateBar rateBar;
   private final GuiSlot input = this.addSlot(SlotType.INPUT, 26, 40);
   private final GuiSlot extra = this.addSlot(SlotType.EXTRA, 6, 69);
   private final GuiSlot output = this.addSlot(SlotType.OUTPUT, 152, 40);
   private final GuiGauge<?> gasInput;

   public NucleosynthesizingRecipeCategory(IGuiHelper helper, MekanismJEIRecipeType<NucleosynthesizingRecipe> recipeType) {
      super(helper, recipeType, MekanismBlocks.ANTIPROTONIC_NUCLEOSYNTHESIZER, 6, 18, 182, 80);
      this.addSlot(SlotType.POWER, 173, 69).with(SlotOverlay.POWER);
      this.addElement(new GuiInnerScreen(this, 45, 18, 104, 68));
      this.gasInput = this.addElement(GuiGasGauge.getDummy(GaugeType.SMALL_MED.with(DataType.INPUT), this, 5, 18));
      this.addElement(new GuiEnergyGauge(new GuiEnergyGauge.IEnergyInfoHandler() {
         @Override
         public FloatingLong getEnergy() {
            return FloatingLong.ONE;
         }

         @Override
         public FloatingLong getMaxEnergy() {
            return FloatingLong.ONE;
         }
      }, GaugeType.SMALL_MED, this, 172, 18));
      this.rateBar = this.addElement(
         new GuiDynamicHorizontalRateBar(
            this, this.getBarProgressTimer(), 5, 88, 183, Color.ColorFunction.scale(Color.rgbi(60, 45, 74), Color.rgbi(100, 30, 170))
         )
      );
   }

   public List<Component> getTooltipStrings(NucleosynthesizingRecipe recipe, IRecipeSlotsView recipeSlotsView, double mouseX, double mouseY) {
      return this.rateBar.m_5953_(mouseX, mouseY)
         ? Collections.singletonList(MekanismLang.TICKS_REQUIRED.translate(new Object[]{recipe.getDuration()}))
         : Collections.emptyList();
   }

   public void setRecipe(@NotNull IRecipeLayoutBuilder builder, NucleosynthesizingRecipe recipe, @NotNull IFocusGroup focusGroup) {
      this.initItem(builder, RecipeIngredientRole.INPUT, this.input, recipe.getItemInput().getRepresentations());
      List<GasStack> gasInputs = recipe.getChemicalInput().getRepresentations();
      this.initChemical(builder, MekanismJEI.TYPE_GAS, RecipeIngredientRole.INPUT, this.gasInput, gasInputs);
      this.initItem(builder, RecipeIngredientRole.OUTPUT, this.output, recipe.getOutputDefinition());
      List<ItemStack> gasItemProviders = new ArrayList<>();

      for (GasStack gas : gasInputs) {
         gasItemProviders.addAll(MekanismJEI.GAS_STACK_HELPER.getStacksFor(gas.getType(), true));
      }

      this.initItem(builder, RecipeIngredientRole.CATALYST, this.extra, gasItemProviders);
   }
}
