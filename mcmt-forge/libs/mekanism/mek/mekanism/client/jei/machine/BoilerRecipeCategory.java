package mekanism.client.jei.machine;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import mekanism.api.MekanismAPI;
import mekanism.api.chemical.gas.Gas;
import mekanism.api.chemical.gas.GasStack;
import mekanism.api.chemical.gas.attribute.GasAttributes;
import mekanism.api.math.MathUtils;
import mekanism.api.recipes.ingredients.ChemicalStackIngredient;
import mekanism.api.recipes.ingredients.creator.IngredientCreatorAccess;
import mekanism.api.text.EnumColor;
import mekanism.client.gui.element.GuiInnerScreen;
import mekanism.client.gui.element.gauge.GaugeType;
import mekanism.client.gui.element.gauge.GuiFluidGauge;
import mekanism.client.gui.element.gauge.GuiGasGauge;
import mekanism.client.gui.element.gauge.GuiGauge;
import mekanism.client.jei.BaseRecipeCategory;
import mekanism.client.jei.MekanismJEI;
import mekanism.client.jei.MekanismJEIRecipeType;
import mekanism.client.jei.recipe.BoilerJEIRecipe;
import mekanism.common.MekanismLang;
import mekanism.common.config.MekanismConfig;
import mekanism.common.registries.MekanismGases;
import mekanism.common.util.HeatUtils;
import mekanism.common.util.MekanismUtils;
import mekanism.common.util.UnitDisplayUtils;
import mekanism.common.util.text.TextUtils;
import mezz.jei.api.gui.builder.IRecipeLayoutBuilder;
import mezz.jei.api.gui.ingredient.IRecipeSlotsView;
import mezz.jei.api.helpers.IGuiHelper;
import mezz.jei.api.recipe.IFocusGroup;
import mezz.jei.api.recipe.RecipeIngredientRole;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.FluidTags;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class BoilerRecipeCategory extends BaseRecipeCategory<BoilerJEIRecipe> {
   private static final ResourceLocation iconRL = MekanismUtils.getResource(MekanismUtils.ResourceType.GUI, "heat.png");
   private final GuiGauge<?> superHeatedCoolantTank;
   private final GuiGauge<?> waterTank;
   private final GuiGauge<?> steamTank;
   private final GuiGauge<?> cooledCoolantTank;
   @Nullable
   private BoilerJEIRecipe recipe;

   public BoilerRecipeCategory(IGuiHelper helper, MekanismJEIRecipeType<BoilerJEIRecipe> recipeType) {
      super(helper, recipeType, MekanismLang.BOILER.translate(new Object[0]), createIcon(helper, iconRL), 6, 13, 180, 60);
      this.addElement(
         new GuiInnerScreen(
            this,
            48,
            23,
            96,
            40,
            () -> {
               double temperature;
               int boilRate;
               if (this.recipe == null) {
                  temperature = 300.0;
                  boilRate = 0;
               } else {
                  temperature = this.recipe.temperature();
                  boilRate = MathUtils.clampToInt(this.recipe.steam().getAmount());
               }

               return List.of(
                  MekanismLang.TEMPERATURE
                     .translate(new Object[]{MekanismUtils.getTemperatureDisplay(temperature, UnitDisplayUtils.TemperatureUnit.KELVIN, true)}),
                  MekanismLang.BOIL_RATE.translate(new Object[]{TextUtils.format((long)boilRate)})
               );
            }
         )
      );
      this.superHeatedCoolantTank = (GuiGauge<?>)this.addElement(
         GuiGasGauge.getDummy(GaugeType.STANDARD, this, 6, 13)
            .setLabel(MekanismLang.BOILER_HEATED_COOLANT_TANK.translateColored(EnumColor.ORANGE, new Object[0]))
      );
      this.waterTank = this.addElement(
         GuiFluidGauge.getDummy(GaugeType.STANDARD, this, 26, 13).setLabel(MekanismLang.BOILER_WATER_TANK.translateColored(EnumColor.INDIGO, new Object[0]))
      );
      this.steamTank = (GuiGauge<?>)this.addElement(
         GuiGasGauge.getDummy(GaugeType.STANDARD, this, 148, 13).setLabel(MekanismLang.BOILER_STEAM_TANK.translateColored(EnumColor.GRAY, new Object[0]))
      );
      this.cooledCoolantTank = (GuiGauge<?>)this.addElement(
         GuiGasGauge.getDummy(GaugeType.STANDARD, this, 168, 13).setLabel(MekanismLang.BOILER_COOLANT_TANK.translateColored(EnumColor.AQUA, new Object[0]))
      );
   }

   public void draw(BoilerJEIRecipe recipe, IRecipeSlotsView recipeSlotView, GuiGraphics guiGraphics, double mouseX, double mouseY) {
      this.recipe = recipe;
      super.draw(recipe, recipeSlotView, guiGraphics, mouseX, mouseY);
      this.recipe = null;
   }

   protected void renderElements(BoilerJEIRecipe recipe, IRecipeSlotsView recipeSlotView, GuiGraphics guiGraphics, int x, int y) {
      super.renderElements(recipe, recipeSlotView, guiGraphics, x, y);
      if (recipe.superHeatedCoolant() == null) {
         this.superHeatedCoolantTank.drawBarOverlay(guiGraphics);
         this.cooledCoolantTank.drawBarOverlay(guiGraphics);
      }
   }

   public void setRecipe(@NotNull IRecipeLayoutBuilder builder, BoilerJEIRecipe recipe, @NotNull IFocusGroup focusGroup) {
      this.initFluid(builder, RecipeIngredientRole.INPUT, this.waterTank, recipe.water().getRepresentations());
      if (recipe.superHeatedCoolant() == null) {
         this.initChemical(builder, MekanismJEI.TYPE_GAS, RecipeIngredientRole.OUTPUT, this.steamTank, Collections.singletonList(recipe.steam()));
      } else {
         this.initChemical(
            builder, MekanismJEI.TYPE_GAS, RecipeIngredientRole.INPUT, this.superHeatedCoolantTank, recipe.superHeatedCoolant().getRepresentations()
         );
         this.initChemical(builder, MekanismJEI.TYPE_GAS, RecipeIngredientRole.OUTPUT, this.steamTank, Collections.singletonList(recipe.steam()));
         this.initChemical(
            builder, MekanismJEI.TYPE_GAS, RecipeIngredientRole.OUTPUT, this.cooledCoolantTank, Collections.singletonList(recipe.cooledCoolant())
         );
      }
   }

   public static List<BoilerJEIRecipe> getBoilerRecipes() {
      int waterAmount = 1;
      double waterToSteamEfficiency = HeatUtils.getWaterThermalEnthalpy() / HeatUtils.getSteamEnergyEfficiency();
      List<BoilerJEIRecipe> recipes = new ArrayList<>();
      double temperature = waterAmount * waterToSteamEfficiency / (50.0 * MekanismConfig.general.boilerWaterConductivity.get()) + HeatUtils.BASE_BOIL_TEMP;
      recipes.add(
         new BoilerJEIRecipe(
            null, IngredientCreatorAccess.fluid().from(FluidTags.f_13131_, waterAmount), MekanismGases.STEAM.getStack(waterAmount), GasStack.EMPTY, temperature
         )
      );

      for (Gas gas : MekanismAPI.gasRegistry()) {
         gas.ifAttributePresent(
            GasAttributes.HeatedCoolant.class,
            heatedCoolant -> {
               Gas cooledCoolant = heatedCoolant.getCooledGas();
               long coolantAmount = Math.round(waterAmount * waterToSteamEfficiency / heatedCoolant.getThermalEnthalpy());
               recipes.add(
                  new BoilerJEIRecipe(
                     (ChemicalStackIngredient.GasStackIngredient)IngredientCreatorAccess.gas().from(gas, coolantAmount),
                     IngredientCreatorAccess.fluid().from(FluidTags.f_13131_, waterAmount),
                     MekanismGases.STEAM.getStack(waterAmount),
                     cooledCoolant.getStack(coolantAmount),
                     HeatUtils.BASE_BOIL_TEMP
                  )
               );
            }
         );
      }

      return recipes;
   }
}
