package mekanism.client.jei.machine;

import mekanism.api.chemical.Chemical;
import mekanism.api.chemical.ChemicalStack;
import mekanism.api.providers.IItemProvider;
import mekanism.api.recipes.chemical.ItemStackToChemicalRecipe;
import mekanism.client.gui.element.gauge.GaugeType;
import mekanism.client.gui.element.gauge.GuiChemicalGauge;
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
import mezz.jei.api.ingredients.IIngredientType;
import mezz.jei.api.recipe.IFocusGroup;
import mezz.jei.api.recipe.RecipeIngredientRole;
import net.minecraft.network.chat.Component;
import org.jetbrains.annotations.NotNull;

public abstract class ItemStackToChemicalRecipeCategory<CHEMICAL extends Chemical<CHEMICAL>, STACK extends ChemicalStack<CHEMICAL>, RECIPE extends ItemStackToChemicalRecipe<CHEMICAL, STACK>>
   extends BaseRecipeCategory<RECIPE> {
   protected static final String CHEMICAL_INPUT = "chemicalInput";
   private final IIngredientType<STACK> ingredientType;
   protected final GuiProgress progressBar;
   private final GuiGauge<?> output;
   private final GuiSlot input;

   protected ItemStackToChemicalRecipeCategory(
      IGuiHelper helper, MekanismJEIRecipeType<RECIPE> recipeType, IItemProvider provider, IIngredientType<STACK> ingredientType, boolean isConversion
   ) {
      this(helper, recipeType, provider.getTextComponent(), createIcon(helper, provider), ingredientType, isConversion);
   }

   protected ItemStackToChemicalRecipeCategory(
      IGuiHelper helper,
      MekanismJEIRecipeType<RECIPE> recipeType,
      Component component,
      IDrawable icon,
      IIngredientType<STACK> ingredientType,
      boolean isConversion
   ) {
      super(helper, recipeType, component, icon, 20, 12, 132, 62);
      this.ingredientType = ingredientType;
      this.output = (GuiGauge<?>)this.addElement(this.getGauge(GaugeType.STANDARD.with(DataType.OUTPUT), 131, 13));
      this.input = this.addSlot(SlotType.INPUT, 26, 36);
      this.progressBar = this.addElement(new GuiProgress(isConversion ? () -> 1.0 : this.getSimpleProgressTimer(), ProgressType.LARGE_RIGHT, this, 64, 40));
   }

   protected abstract GuiChemicalGauge<CHEMICAL, STACK, ?> getGauge(GaugeType type, int x, int y);

   public void setRecipe(@NotNull IRecipeLayoutBuilder builder, RECIPE recipe, @NotNull IFocusGroup focusGroup) {
      this.initItem(builder, RecipeIngredientRole.INPUT, this.input, recipe.getInput().getRepresentations());
      this.initChemical(builder, this.ingredientType, RecipeIngredientRole.OUTPUT, this.output, recipe.getOutputDefinition()).setSlotName("chemicalInput");
   }
}
