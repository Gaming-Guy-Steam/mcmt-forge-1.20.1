package mekanism.client.jei.machine;

import java.util.Collections;
import java.util.List;
import mekanism.api.math.FloatingLong;
import mekanism.api.recipes.ItemStackToEnergyRecipe;
import mekanism.api.text.TextComponentUtil;
import mekanism.client.gui.element.gauge.GaugeType;
import mekanism.client.gui.element.gauge.GuiEnergyGauge;
import mekanism.client.gui.element.progress.ProgressType;
import mekanism.client.gui.element.slot.GuiSlot;
import mekanism.client.gui.element.slot.SlotType;
import mekanism.client.jei.BaseRecipeCategory;
import mekanism.client.jei.MekanismJEIRecipeType;
import mekanism.common.MekanismLang;
import mekanism.common.tile.component.config.DataType;
import mekanism.common.util.MekanismUtils;
import mekanism.common.util.text.EnergyDisplay;
import mezz.jei.api.constants.VanillaTypes;
import mezz.jei.api.gui.builder.IRecipeLayoutBuilder;
import mezz.jei.api.gui.ingredient.IRecipeSlotsView;
import mezz.jei.api.helpers.IGuiHelper;
import mezz.jei.api.recipe.IFocusGroup;
import mezz.jei.api.recipe.RecipeIngredientRole;
import net.minecraft.ChatFormatting;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
import org.jetbrains.annotations.NotNull;

public class ItemStackToEnergyRecipeCategory extends BaseRecipeCategory<ItemStackToEnergyRecipe> {
   private static final ResourceLocation iconRL = MekanismUtils.getResource(MekanismUtils.ResourceType.GUI, "energy.png");
   private static final String INPUT = "input";
   private final GuiEnergyGauge gauge = this.addElement(GuiEnergyGauge.getDummy(GaugeType.STANDARD.with(DataType.OUTPUT), this, 133, 13));
   private final GuiSlot input = this.addSlot(SlotType.INPUT, 26, 36);

   public ItemStackToEnergyRecipeCategory(IGuiHelper helper, MekanismJEIRecipeType<ItemStackToEnergyRecipe> recipeType) {
      super(helper, recipeType, MekanismLang.CONVERSION_ENERGY.translate(new Object[0]), createIcon(helper, iconRL), 20, 12, 132, 62);
      this.addConstantProgress(ProgressType.LARGE_RIGHT, 64, 40);
   }

   public void setRecipe(@NotNull IRecipeLayoutBuilder builder, ItemStackToEnergyRecipe recipe, @NotNull IFocusGroup focusGroup) {
      this.initItem(builder, RecipeIngredientRole.INPUT, this.input, recipe.getInput().getRepresentations()).setSlotName("input");
   }

   protected void renderElements(ItemStackToEnergyRecipe recipe, IRecipeSlotsView recipeSlotView, GuiGraphics guiGraphics, int x, int y) {
      super.renderElements(recipe, recipeSlotView, guiGraphics, x, y);
      if (!this.getOutputEnergy(recipe, recipeSlotView).isZero()) {
         this.gauge.renderContents(guiGraphics);
      }
   }

   public List<Component> getTooltipStrings(ItemStackToEnergyRecipe recipe, IRecipeSlotsView recipeSlotsView, double mouseX, double mouseY) {
      if (this.gauge.m_5953_(mouseX, mouseY)) {
         FloatingLong energy = this.getOutputEnergy(recipe, recipeSlotsView);
         if (!energy.isZero()) {
            Component energyOutput = EnergyDisplay.of(energy).getTextComponent();
            if (!Minecraft.m_91087_().f_91066_.f_92125_ && !Screen.m_96638_()) {
               return Collections.singletonList(energyOutput);
            }

            return List.of(
               energyOutput, TextComponentUtil.build(ChatFormatting.DARK_GRAY, MekanismLang.JEI_RECIPE_ID.translate(new Object[]{recipe.m_6423_()}))
            );
         }
      }

      return Collections.emptyList();
   }

   private FloatingLong getOutputEnergy(ItemStackToEnergyRecipe recipe, IRecipeSlotsView recipeSlotsView) {
      ItemStack displayedIngredient = this.getDisplayedStack(recipeSlotsView, "input", VanillaTypes.ITEM_STACK, ItemStack.f_41583_);
      return displayedIngredient.m_41619_() ? FloatingLong.ZERO : recipe.getOutput(displayedIngredient);
   }
}
