package mekanism.client.gui.robit;

import mekanism.api.recipes.cache.CachedRecipe;
import mekanism.client.gui.element.progress.GuiProgress;
import mekanism.client.gui.element.progress.ProgressType;
import mekanism.client.jei.MekanismJEIRecipeType;
import mekanism.common.inventory.container.entity.robit.RobitContainer;
import mekanism.common.inventory.warning.WarningTracker;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Inventory;
import org.jetbrains.annotations.NotNull;

public class GuiRobitSmelting extends GuiRobit<RobitContainer> {
   public GuiRobitSmelting(RobitContainer container, Inventory inv, Component title) {
      super(container, inv, title);
      this.f_97731_++;
      this.dynamicSlots = true;
   }

   @Override
   protected void addGuiElements() {
      super.addGuiElements();
      this.addRenderableWidget(new GuiProgress(this.robit::getScaledProgress, ProgressType.BAR, this, 78, 38).jeiCategories(MekanismJEIRecipeType.SMELTING))
         .warning(
            WarningTracker.WarningType.INPUT_DOESNT_PRODUCE_OUTPUT,
            this.robit.getWarningCheck(CachedRecipe.OperationTracker.RecipeError.INPUT_DOESNT_PRODUCE_OUTPUT)
         );
      this.trackWarning(WarningTracker.WarningType.NOT_ENOUGH_ENERGY, this.robit.getWarningCheck(CachedRecipe.OperationTracker.RecipeError.NOT_ENOUGH_ENERGY));
   }

   @Override
   protected void drawForegroundText(@NotNull GuiGraphics guiGraphics, int mouseX, int mouseY) {
      this.drawString(guiGraphics, this.f_96539_, this.f_97728_, this.f_97729_, this.titleTextColor());
      this.drawString(guiGraphics, this.f_169604_, this.f_97730_, this.f_97731_, this.titleTextColor());
      super.drawForegroundText(guiGraphics, mouseX, mouseY);
   }

   @Override
   protected boolean shouldOpenGui(GuiRobit.RobitGuiType guiType) {
      return guiType != GuiRobit.RobitGuiType.SMELTING;
   }
}
