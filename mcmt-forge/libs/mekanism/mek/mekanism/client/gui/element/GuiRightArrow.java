package mekanism.client.gui.element;

import mekanism.client.gui.IGuiWrapper;
import mekanism.client.jei.MekanismJEIRecipeType;
import mekanism.client.jei.interfaces.IJEIRecipeArea;
import mekanism.common.util.MekanismUtils;
import net.minecraft.resources.ResourceLocation;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class GuiRightArrow extends GuiTextureOnlyElement implements IJEIRecipeArea<GuiRightArrow> {
   private static final ResourceLocation ARROW = MekanismUtils.getResource(MekanismUtils.ResourceType.GUI, "right_arrow.png");
   private MekanismJEIRecipeType<?>[] recipeCategories;

   public GuiRightArrow(IGuiWrapper gui, int x, int y) {
      super(ARROW, gui, x, y, 22, 15);
   }

   @NotNull
   public GuiRightArrow jeiCategories(@NotNull MekanismJEIRecipeType<?>... recipeCategories) {
      this.recipeCategories = recipeCategories;
      return this;
   }

   @Nullable
   @Override
   public MekanismJEIRecipeType<?>[] getRecipeCategories() {
      return this.recipeCategories;
   }
}
