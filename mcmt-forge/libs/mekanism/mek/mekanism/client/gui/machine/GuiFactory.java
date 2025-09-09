package mekanism.client.gui.machine;

import mekanism.api.recipes.cache.CachedRecipe;
import mekanism.client.gui.GuiConfigurableTile;
import mekanism.client.gui.element.GuiDumpButton;
import mekanism.client.gui.element.bar.GuiChemicalBar;
import mekanism.client.gui.element.bar.GuiVerticalPowerBar;
import mekanism.client.gui.element.progress.GuiProgress;
import mekanism.client.gui.element.progress.ProgressType;
import mekanism.client.gui.element.tab.GuiEnergyTab;
import mekanism.client.gui.element.tab.GuiSortingTab;
import mekanism.client.jei.MekanismJEIRecipeType;
import mekanism.common.inventory.container.tile.MekanismTileContainer;
import mekanism.common.inventory.warning.ISupportsWarning;
import mekanism.common.inventory.warning.WarningTracker;
import mekanism.common.tier.FactoryTier;
import mekanism.common.tile.factory.TileEntityFactory;
import mekanism.common.tile.factory.TileEntityItemStackGasToItemStackFactory;
import mekanism.common.tile.factory.TileEntityMetallurgicInfuserFactory;
import mekanism.common.tile.factory.TileEntitySawingFactory;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Inventory;
import org.jetbrains.annotations.NotNull;

public class GuiFactory extends GuiConfigurableTile<TileEntityFactory<?>, MekanismTileContainer<TileEntityFactory<?>>> {
   public GuiFactory(MekanismTileContainer<TileEntityFactory<?>> container, Inventory inv, Component title) {
      super(container, inv, title);
      if (this.tile.hasSecondaryResourceBar()) {
         this.f_97727_ += 11;
         this.f_97731_ = 85;
      } else if (this.tile instanceof TileEntitySawingFactory) {
         this.f_97727_ += 21;
         this.f_97731_ = 95;
      } else {
         this.f_97731_ = 75;
      }

      if (this.tile.tier == FactoryTier.ULTIMATE) {
         this.f_97726_ += 34;
         this.f_97730_ = 26;
      }

      this.f_97729_ = 4;
      this.dynamicSlots = true;
   }

   @Override
   protected void addGuiElements() {
      super.addGuiElements();
      this.addRenderableWidget(new GuiSortingTab(this, this.tile));
      this.addRenderableWidget(
            new GuiVerticalPowerBar(this, this.tile.getEnergyContainer(), this.f_97726_ - 12, 16, this.tile instanceof TileEntitySawingFactory ? 73 : 52)
         )
         .warning(WarningTracker.WarningType.NOT_ENOUGH_ENERGY, this.tile.getWarningCheck(CachedRecipe.OperationTracker.RecipeError.NOT_ENOUGH_ENERGY, 0));
      this.addRenderableWidget(new GuiEnergyTab(this, this.tile.getEnergyContainer(), this.tile::getLastUsage));
      if (this.tile.hasSecondaryResourceBar()) {
         ISupportsWarning<?> secondaryBar = null;
         if (this.tile instanceof TileEntityMetallurgicInfuserFactory factory) {
            secondaryBar = (ISupportsWarning<?>)this.addRenderableWidget(
               new GuiChemicalBar<>(
                  this,
                  GuiChemicalBar.getProvider(factory.getInfusionTank(), this.tile.getInfusionTanks(null)),
                  7,
                  76,
                  this.tile.tier == FactoryTier.ULTIMATE ? 172 : 138,
                  4,
                  true
               )
            );
            this.addRenderableWidget(new GuiDumpButton<>(this, factory, this.tile.tier == FactoryTier.ULTIMATE ? 182 : 148, 76));
         } else if (this.tile instanceof TileEntityItemStackGasToItemStackFactory factory) {
            secondaryBar = (ISupportsWarning<?>)this.addRenderableWidget(
               new GuiChemicalBar<>(
                  this,
                  GuiChemicalBar.getProvider(factory.getGasTank(), this.tile.getGasTanks(null)),
                  7,
                  76,
                  this.tile.tier == FactoryTier.ULTIMATE ? 172 : 138,
                  4,
                  true
               )
            );
            this.addRenderableWidget(new GuiDumpButton<>(this, factory, this.tile.tier == FactoryTier.ULTIMATE ? 182 : 148, 76));
         }

         if (secondaryBar != null) {
            secondaryBar.warning(
               WarningTracker.WarningType.NO_MATCHING_RECIPE,
               this.tile.getWarningCheck(CachedRecipe.OperationTracker.RecipeError.NOT_ENOUGH_SECONDARY_INPUT, 0)
            );
         }
      }

      int baseX = this.tile.tier == FactoryTier.BASIC ? 55 : (this.tile.tier == FactoryTier.ADVANCED ? 35 : (this.tile.tier == FactoryTier.ELITE ? 29 : 27));
      int baseXMult = this.tile.tier == FactoryTier.BASIC ? 38 : (this.tile.tier == FactoryTier.ADVANCED ? 26 : 19);

      for (int i = 0; i < this.tile.tier.processes; i++) {
         int cacheIndex = i;
         this.addProgress(new GuiProgress(() -> this.tile.getScaledProgress(1, cacheIndex), ProgressType.DOWN, this, 4 + baseX + i * baseXMult, 33))
            .warning(
               WarningTracker.WarningType.INPUT_DOESNT_PRODUCE_OUTPUT,
               this.tile.getWarningCheck(CachedRecipe.OperationTracker.RecipeError.INPUT_DOESNT_PRODUCE_OUTPUT, cacheIndex)
            );
      }
   }

   private GuiProgress addProgress(GuiProgress progressBar) {
      MekanismJEIRecipeType<?> jeiType = switch (this.tile.getFactoryType()) {
         case SMELTING -> MekanismJEIRecipeType.SMELTING;
         case ENRICHING -> MekanismJEIRecipeType.ENRICHING;
         case CRUSHING -> MekanismJEIRecipeType.CRUSHING;
         case COMPRESSING -> MekanismJEIRecipeType.COMPRESSING;
         case COMBINING -> MekanismJEIRecipeType.COMBINING;
         case PURIFYING -> MekanismJEIRecipeType.PURIFYING;
         case INJECTING -> MekanismJEIRecipeType.INJECTING;
         case INFUSING -> MekanismJEIRecipeType.METALLURGIC_INFUSING;
         case SAWING -> MekanismJEIRecipeType.SAWING;
      };
      return this.addRenderableWidget(progressBar.jeiCategories(jeiType));
   }

   @Override
   protected void drawForegroundText(@NotNull GuiGraphics guiGraphics, int mouseX, int mouseY) {
      this.renderTitleText(guiGraphics);
      this.drawString(guiGraphics, this.f_169604_, this.f_97730_, this.f_97731_, this.titleTextColor());
      super.drawForegroundText(guiGraphics, mouseX, mouseY);
   }
}
