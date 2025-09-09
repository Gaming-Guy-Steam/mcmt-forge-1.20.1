package mekanism.client.jei.machine;

import java.util.ArrayList;
import java.util.List;
import mekanism.api.chemical.gas.GasStack;
import mekanism.api.providers.IBlockProvider;
import mekanism.api.recipes.ItemStackGasToItemStackRecipe;
import mekanism.client.gui.element.bar.GuiBar;
import mekanism.client.gui.element.bar.GuiEmptyBar;
import mekanism.client.gui.element.bar.GuiVerticalPowerBar;
import mekanism.client.gui.element.progress.ProgressType;
import mekanism.client.gui.element.slot.GuiSlot;
import mekanism.client.gui.element.slot.SlotType;
import mekanism.client.jei.BaseRecipeCategory;
import mekanism.client.jei.MekanismJEI;
import mekanism.client.jei.MekanismJEIRecipeType;
import mekanism.common.inventory.container.slot.SlotOverlay;
import mezz.jei.api.gui.builder.IRecipeLayoutBuilder;
import mezz.jei.api.helpers.IGuiHelper;
import mezz.jei.api.recipe.IFocusGroup;
import mezz.jei.api.recipe.RecipeIngredientRole;
import net.minecraft.world.item.ItemStack;
import org.jetbrains.annotations.NotNull;

public class ItemStackGasToItemStackRecipeCategory extends BaseRecipeCategory<ItemStackGasToItemStackRecipe> {
   private final GuiBar<?> gasInput;
   private final GuiSlot input = this.addSlot(SlotType.INPUT, 64, 17);
   private final GuiSlot extra = this.addSlot(SlotType.EXTRA, 64, 53);
   private final GuiSlot output = this.addSlot(SlotType.OUTPUT, 116, 35);

   public ItemStackGasToItemStackRecipeCategory(
      IGuiHelper helper, MekanismJEIRecipeType<ItemStackGasToItemStackRecipe> recipeType, IBlockProvider mekanismBlock
   ) {
      super(helper, recipeType, mekanismBlock, 28, 16, 144, 54);
      this.addSlot(SlotType.POWER, 39, 35).with(SlotOverlay.POWER);
      this.addElement(new GuiVerticalPowerBar(this, FULL_BAR, 164, 15));
      this.gasInput = this.addElement(new GuiEmptyBar(this, 68, 36, 6, 12));
      this.addSimpleProgress(ProgressType.BAR, 86, 38);
   }

   public void setRecipe(@NotNull IRecipeLayoutBuilder builder, ItemStackGasToItemStackRecipe recipe, @NotNull IFocusGroup focusGroup) {
      this.initItem(builder, RecipeIngredientRole.INPUT, this.input, recipe.getItemInput().getRepresentations());
      List<ItemStack> gasItemProviders = new ArrayList<>();
      List<GasStack> scaledGases = new ArrayList<>();

      for (GasStack gas : recipe.getChemicalInput().getRepresentations()) {
         gasItemProviders.addAll(MekanismJEI.GAS_STACK_HELPER.getStacksFor(gas.getType(), true));
         scaledGases.add(new GasStack(gas, gas.getAmount() * 200L));
      }

      this.initChemical(builder, MekanismJEI.TYPE_GAS, RecipeIngredientRole.INPUT, this.gasInput, scaledGases);
      this.initItem(builder, RecipeIngredientRole.OUTPUT, this.output, recipe.getOutputDefinition());
      this.initItem(builder, RecipeIngredientRole.CATALYST, this.extra, gasItemProviders);
   }
}
