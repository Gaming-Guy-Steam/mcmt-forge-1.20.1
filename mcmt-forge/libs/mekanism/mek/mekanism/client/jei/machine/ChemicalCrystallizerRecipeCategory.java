package mekanism.client.jei.machine;

import java.util.HashSet;
import java.util.Set;
import mekanism.api.chemical.ChemicalStack;
import mekanism.api.chemical.merged.BoxedChemicalStack;
import mekanism.api.chemical.slurry.Slurry;
import mekanism.api.chemical.slurry.SlurryStack;
import mekanism.api.recipes.ChemicalCrystallizerRecipe;
import mekanism.api.recipes.ingredients.ChemicalStackIngredient;
import mekanism.client.gui.element.GuiInnerScreen;
import mekanism.client.gui.element.gauge.GaugeType;
import mekanism.client.gui.element.gauge.GuiGasGauge;
import mekanism.client.gui.element.gauge.GuiGauge;
import mekanism.client.gui.element.progress.ProgressType;
import mekanism.client.gui.element.slot.GuiSlot;
import mekanism.client.gui.element.slot.SlotType;
import mekanism.client.gui.machine.GuiChemicalCrystallizer;
import mekanism.client.jei.BaseRecipeCategory;
import mekanism.client.jei.MekanismJEI;
import mekanism.client.jei.MekanismJEIRecipeType;
import mekanism.common.inventory.container.slot.SlotOverlay;
import mekanism.common.registries.MekanismBlocks;
import mekanism.common.tags.MekanismTags;
import mekanism.common.tags.TagUtils;
import mekanism.common.tile.component.config.DataType;
import mezz.jei.api.constants.VanillaTypes;
import mezz.jei.api.gui.builder.IRecipeLayoutBuilder;
import mezz.jei.api.gui.ingredient.IRecipeSlotView;
import mezz.jei.api.gui.ingredient.IRecipeSlotsView;
import mezz.jei.api.helpers.IGuiHelper;
import mezz.jei.api.ingredients.IIngredientType;
import mezz.jei.api.ingredients.ITypedIngredient;
import mezz.jei.api.recipe.IFocusGroup;
import mezz.jei.api.recipe.RecipeIngredientRole;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.tags.TagKey;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.tags.ITag;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class ChemicalCrystallizerRecipeCategory extends BaseRecipeCategory<ChemicalCrystallizerRecipe> {
   private static final String CHEMICAL_INPUT = "chemicalInput";
   private static final String DISPLAYED_ITEM = "displayedItem";
   private final ChemicalCrystallizerRecipeCategory.OreInfo oreInfo = new ChemicalCrystallizerRecipeCategory.OreInfo();
   private final GuiGauge<?> gauge = this.addElement(GuiGasGauge.getDummy(GaugeType.STANDARD.with(DataType.INPUT), this, 7, 4));
   private final GuiSlot output;
   private final GuiSlot slurryOreSlot;

   public ChemicalCrystallizerRecipeCategory(IGuiHelper helper, MekanismJEIRecipeType<ChemicalCrystallizerRecipe> recipeType) {
      super(helper, recipeType, MekanismBlocks.CHEMICAL_CRYSTALLIZER, 5, 3, 147, 79);
      this.addSlot(SlotType.INPUT, 8, 65).with(SlotOverlay.PLUS);
      this.output = this.addSlot(SlotType.OUTPUT, 129, 57);
      this.addSimpleProgress(ProgressType.LARGE_RIGHT, 53, 61);
      this.addElement(new GuiInnerScreen(this, 31, 13, 115, 42, () -> GuiChemicalCrystallizer.getScreenRenderStrings(this.oreInfo)));
      this.slurryOreSlot = this.addElement(new GuiSlot(SlotType.ORE, this, 128, 13).setRenderAboveSlots());
   }

   public void draw(ChemicalCrystallizerRecipe recipe, IRecipeSlotsView recipeSlotsView, GuiGraphics guiGraphics, double mouseX, double mouseY) {
      this.oreInfo.currentRecipe = recipe;
      this.oreInfo.ingredient = recipeSlotsView.findSlotByName("chemicalInput")
         .flatMap(IRecipeSlotView::getDisplayedIngredient)
         .<ChemicalStack<?>>map(ITypedIngredient::getIngredient)
         .filter(ingredient -> ingredient instanceof ChemicalStack)
         .orElse(null);
      this.oreInfo.itemIngredient = this.getDisplayedStack(recipeSlotsView, "displayedItem", VanillaTypes.ITEM_STACK, ItemStack.f_41583_);
      super.draw(recipe, recipeSlotsView, guiGraphics, mouseX, mouseY);
      this.oreInfo.currentRecipe = null;
      this.oreInfo.ingredient = null;
      this.oreInfo.itemIngredient = ItemStack.f_41583_;
   }

   public void setRecipe(@NotNull IRecipeLayoutBuilder builder, ChemicalCrystallizerRecipe recipe, @NotNull IFocusGroup focusGroup) {
      this.initItem(builder, RecipeIngredientRole.OUTPUT, this.output, recipe.getOutputDefinition());
      ChemicalStackIngredient<?, ?> input = recipe.getInput();
      if (input instanceof ChemicalStackIngredient.GasStackIngredient ingredient) {
         this.initChemical(builder, MekanismJEI.TYPE_GAS, ingredient);
      } else if (input instanceof ChemicalStackIngredient.InfusionStackIngredient ingredient) {
         this.initChemical(builder, MekanismJEI.TYPE_INFUSION, ingredient);
      } else if (input instanceof ChemicalStackIngredient.PigmentStackIngredient ingredient) {
         this.initChemical(builder, MekanismJEI.TYPE_PIGMENT, ingredient);
      } else if (input instanceof ChemicalStackIngredient.SlurryStackIngredient ingredient) {
         this.initChemical(builder, MekanismJEI.TYPE_SLURRY, ingredient);
         Set<ITag<Item>> tags = new HashSet<>();

         for (SlurryStack slurryStack : ingredient.getRepresentations()) {
            Slurry slurry = slurryStack.getType();
            if (!MekanismTags.Slurries.DIRTY_LOOKUP.contains(slurry)) {
               TagKey<Item> oreTag = slurry.getOreTag();
               if (oreTag != null) {
                  tags.add(TagUtils.tag(ForgeRegistries.ITEMS, oreTag));
               }
            }
         }

         if (tags.size() == 1) {
            tags.stream()
               .findFirst()
               .ifPresent(
                  tag -> this.initItem(builder, RecipeIngredientRole.RENDER_ONLY, this.slurryOreSlot, tag.stream().<ItemStack>map(ItemStack::new).toList())
                     .setSlotName("displayedItem")
               );
         }
      }
   }

   private <STACK extends ChemicalStack<?>> void initChemical(
      IRecipeLayoutBuilder builder, IIngredientType<STACK> type, ChemicalStackIngredient<?, STACK> ingredient
   ) {
      this.initChemical(builder, type, RecipeIngredientRole.INPUT, this.gauge, ingredient.getRepresentations()).setSlotName("chemicalInput");
   }

   private static class OreInfo implements GuiChemicalCrystallizer.IOreInfo {
      @Nullable
      private ChemicalCrystallizerRecipe currentRecipe;
      @Nullable
      private ChemicalStack<?> ingredient;
      private ItemStack itemIngredient = ItemStack.f_41583_;

      @NotNull
      @Override
      public BoxedChemicalStack getInputChemical() {
         return this.ingredient != null && !this.ingredient.isEmpty() ? BoxedChemicalStack.box(this.ingredient) : BoxedChemicalStack.EMPTY;
      }

      @Nullable
      @Override
      public ChemicalCrystallizerRecipe getRecipe() {
         return this.currentRecipe;
      }

      @NotNull
      @Override
      public ItemStack getRenderStack() {
         return this.itemIngredient;
      }
   }
}
