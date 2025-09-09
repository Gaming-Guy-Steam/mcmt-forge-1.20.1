package mekanism.client.gui.machine;

import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import mekanism.api.MekanismAPI;
import mekanism.api.chemical.ChemicalType;
import mekanism.api.chemical.merged.BoxedChemicalStack;
import mekanism.api.chemical.merged.MergedChemicalTank;
import mekanism.api.chemical.slurry.Slurry;
import mekanism.api.recipes.ChemicalCrystallizerRecipe;
import mekanism.api.recipes.cache.CachedRecipe;
import mekanism.client.gui.GuiConfigurableTile;
import mekanism.client.gui.element.GuiInnerScreen;
import mekanism.client.gui.element.bar.GuiVerticalPowerBar;
import mekanism.client.gui.element.gauge.GaugeType;
import mekanism.client.gui.element.gauge.GuiMergedChemicalTankGauge;
import mekanism.client.gui.element.progress.GuiProgress;
import mekanism.client.gui.element.progress.ProgressType;
import mekanism.client.gui.element.slot.GuiSequencedSlotDisplay;
import mekanism.client.gui.element.slot.GuiSlot;
import mekanism.client.gui.element.slot.SlotType;
import mekanism.client.gui.element.tab.GuiEnergyTab;
import mekanism.common.MekanismLang;
import mekanism.common.inventory.container.tile.MekanismTileContainer;
import mekanism.common.inventory.warning.WarningTracker;
import mekanism.common.tags.MekanismTags;
import mekanism.common.tags.TagUtils;
import mekanism.common.tile.machine.TileEntityChemicalCrystallizer;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.network.chat.Component;
import net.minecraft.tags.TagKey;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.registries.ForgeRegistries;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class GuiChemicalCrystallizer extends GuiConfigurableTile<TileEntityChemicalCrystallizer, MekanismTileContainer<TileEntityChemicalCrystallizer>> {
   private final List<ItemStack> iterStacks = new ArrayList<>();
   private final GuiChemicalCrystallizer.IOreInfo oreInfo = new GuiChemicalCrystallizer.OreInfo();
   private GuiSequencedSlotDisplay slotDisplay;
   @NotNull
   private Slurry prevSlurry = MekanismAPI.EMPTY_SLURRY;

   public GuiChemicalCrystallizer(MekanismTileContainer<TileEntityChemicalCrystallizer> container, Inventory inv, Component title) {
      super(container, inv, title);
      this.dynamicSlots = true;
      this.f_97729_ = 4;
   }

   @Override
   protected void addGuiElements() {
      super.addGuiElements();
      this.addRenderableWidget(new GuiVerticalPowerBar(this, this.tile.getEnergyContainer(), 157, 23))
         .warning(WarningTracker.WarningType.NOT_ENOUGH_ENERGY, this.tile.getWarningCheck(CachedRecipe.OperationTracker.RecipeError.NOT_ENOUGH_ENERGY));
      this.addRenderableWidget(new GuiEnergyTab(this, this.tile.getEnergyContainer(), this.tile::getActive));
      this.addRenderableWidget(new GuiMergedChemicalTankGauge<>(() -> this.tile.inputTank, () -> this.tile, GaugeType.STANDARD, this, 7, 4))
         .warning(WarningTracker.WarningType.NO_MATCHING_RECIPE, this.tile.getWarningCheck(CachedRecipe.OperationTracker.RecipeError.NOT_ENOUGH_INPUT));
      this.addRenderableWidget(new GuiProgress(this.tile::getScaledProgress, ProgressType.LARGE_RIGHT, this, 53, 61).jeiCategory(this.tile))
         .warning(
            WarningTracker.WarningType.INPUT_DOESNT_PRODUCE_OUTPUT,
            this.tile.getWarningCheck(CachedRecipe.OperationTracker.RecipeError.INPUT_DOESNT_PRODUCE_OUTPUT)
         );
      this.slotDisplay = new GuiSequencedSlotDisplay(this, 129, 14, () -> this.iterStacks);
      this.updateSlotContents();
      this.addRenderableWidget(new GuiInnerScreen(this, 31, 13, 115, 42, () -> getScreenRenderStrings(this.oreInfo)));
      this.addRenderableWidget(new GuiSlot(SlotType.ORE, this, 128, 13).setRenderAboveSlots());
      this.addRenderableWidget(this.slotDisplay);
   }

   @Override
   protected void drawForegroundText(@NotNull GuiGraphics guiGraphics, int mouseX, int mouseY) {
      this.renderTitleText(guiGraphics);
      super.drawForegroundText(guiGraphics, mouseX, mouseY);
   }

   @Override
   public void m_181908_() {
      this.updateSlotContents();
      super.m_181908_();
   }

   private void updateSlotContents() {
      BoxedChemicalStack boxedChemical = this.oreInfo.getInputChemical();
      if (!boxedChemical.isEmpty() && boxedChemical.getChemicalType() == ChemicalType.SLURRY) {
         Slurry inputSlurry = (Slurry)boxedChemical.getChemicalStack().getType();
         if (this.prevSlurry != inputSlurry) {
            this.prevSlurry = inputSlurry;
            this.iterStacks.clear();
            if (!this.prevSlurry.isEmptyType() && !MekanismTags.Slurries.DIRTY_LOOKUP.contains(this.prevSlurry)) {
               TagKey<Item> oreTag = this.prevSlurry.getOreTag();
               if (oreTag != null) {
                  for (Item ore : TagUtils.tag(ForgeRegistries.ITEMS, oreTag)) {
                     this.iterStacks.add(new ItemStack(ore));
                  }
               }
            }

            this.slotDisplay.updateStackList();
         }
      } else if (!this.prevSlurry.isEmptyType()) {
         this.prevSlurry = MekanismAPI.EMPTY_SLURRY;
         this.iterStacks.clear();
         this.slotDisplay.updateStackList();
      }
   }

   public static List<Component> getScreenRenderStrings(GuiChemicalCrystallizer.IOreInfo oreInfo) {
      BoxedChemicalStack boxedChemical = oreInfo.getInputChemical();
      if (boxedChemical.isEmpty()) {
         return Collections.emptyList();
      } else {
         List<Component> ret = new ArrayList<>();
         ret.add(boxedChemical.getTextComponent());
         if (boxedChemical.getChemicalType() == ChemicalType.SLURRY && !oreInfo.getRenderStack().m_41619_()) {
            ret.add(MekanismLang.GENERIC_PARENTHESIS.translate(new Object[]{oreInfo.getRenderStack()}));
         } else {
            ChemicalCrystallizerRecipe recipe = oreInfo.getRecipe();
            if (recipe == null) {
               ret.add(MekanismLang.NO_RECIPE.translate(new Object[0]));
            } else {
               ret.add(MekanismLang.GENERIC_PARENTHESIS.translate(new Object[]{recipe.getOutput(boxedChemical)}));
            }
         }

         return ret;
      }
   }

   public interface IOreInfo {
      @NotNull
      BoxedChemicalStack getInputChemical();

      @Nullable
      ChemicalCrystallizerRecipe getRecipe();

      @NotNull
      ItemStack getRenderStack();
   }

   private class OreInfo implements GuiChemicalCrystallizer.IOreInfo {
      private WeakReference<ChemicalCrystallizerRecipe> cachedRecipe;

      @NotNull
      @Override
      public BoxedChemicalStack getInputChemical() {
         MergedChemicalTank.Current current = GuiChemicalCrystallizer.this.tile.inputTank.getCurrent();
         return current == MergedChemicalTank.Current.EMPTY
            ? BoxedChemicalStack.EMPTY
            : BoxedChemicalStack.box(GuiChemicalCrystallizer.this.tile.inputTank.getTankFromCurrent(current).getStack());
      }

      @Nullable
      @Override
      public ChemicalCrystallizerRecipe getRecipe() {
         BoxedChemicalStack input = this.getInputChemical();
         if (input.isEmpty()) {
            return null;
         } else {
            ChemicalCrystallizerRecipe recipe;
            if (this.cachedRecipe == null) {
               recipe = this.getRecipeAndCache();
            } else {
               recipe = this.cachedRecipe.get();
               if (recipe == null || !recipe.testType(input)) {
                  recipe = this.getRecipeAndCache();
               }
            }

            return recipe;
         }
      }

      @NotNull
      @Override
      public ItemStack getRenderStack() {
         return GuiChemicalCrystallizer.this.slotDisplay == null ? ItemStack.f_41583_ : GuiChemicalCrystallizer.this.slotDisplay.getRenderStack();
      }

      private ChemicalCrystallizerRecipe getRecipeAndCache() {
         ChemicalCrystallizerRecipe recipe = GuiChemicalCrystallizer.this.tile.getRecipe(0);
         if (recipe == null) {
            this.cachedRecipe = null;
         } else {
            this.cachedRecipe = new WeakReference<>(recipe);
         }

         return recipe;
      }
   }
}
