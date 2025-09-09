package mekanism.client.jei;

import com.mojang.blaze3d.vertex.PoseStack;
import java.util.ArrayList;
import java.util.EnumMap;
import java.util.List;
import java.util.Map;
import mekanism.api.chemical.ChemicalStack;
import mekanism.api.providers.IItemProvider;
import mekanism.client.gui.IGuiWrapper;
import mekanism.client.gui.element.GuiElement;
import mekanism.client.gui.element.GuiTexturedElement;
import mekanism.client.gui.element.bar.GuiBar;
import mekanism.client.gui.element.gauge.GaugeOverlay;
import mekanism.client.gui.element.gauge.GuiGauge;
import mekanism.client.gui.element.progress.GuiProgress;
import mekanism.client.gui.element.progress.IProgressInfoHandler;
import mekanism.client.gui.element.progress.ProgressType;
import mekanism.client.gui.element.slot.GuiSlot;
import mekanism.client.gui.element.slot.SlotType;
import mekanism.common.MekanismLang;
import mekanism.common.util.text.TextUtils;
import mezz.jei.api.constants.VanillaTypes;
import mezz.jei.api.forge.ForgeTypes;
import mezz.jei.api.gui.ITickTimer;
import mezz.jei.api.gui.builder.IRecipeLayoutBuilder;
import mezz.jei.api.gui.builder.IRecipeSlotBuilder;
import mezz.jei.api.gui.drawable.IDrawable;
import mezz.jei.api.gui.ingredient.IRecipeSlotsView;
import mezz.jei.api.helpers.IGuiHelper;
import mezz.jei.api.ingredients.IIngredientType;
import mezz.jei.api.recipe.RecipeIngredientRole;
import mezz.jei.api.recipe.RecipeType;
import mezz.jei.api.recipe.category.IRecipeCategory;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.fluids.FluidStack;
import org.jetbrains.annotations.Nullable;

public abstract class BaseRecipeCategory<RECIPE> implements IRecipeCategory<RECIPE>, IGuiWrapper {
   private static final IProgressInfoHandler CONSTANT_PROGRESS = () -> 1.0;
   protected static final GuiBar.IBarInfoHandler FULL_BAR = () -> 1.0;
   private final List<GuiTexturedElement> guiElements = new ArrayList<>();
   private final Component component;
   private final IGuiHelper guiHelper;
   private final IDrawable background;
   private final RecipeType<RECIPE> recipeType;
   private final IDrawable icon;
   private final int xOffset;
   private final int yOffset;
   @Nullable
   private Map<GaugeOverlay, IDrawable> overlayLookup;
   @Nullable
   private ITickTimer timer;

   protected static IDrawable createIcon(IGuiHelper helper, ResourceLocation iconRL) {
      return helper.drawableBuilder(iconRL, 0, 0, 18, 18).setTextureSize(18, 18).build();
   }

   protected static IDrawable createIcon(IGuiHelper helper, IItemProvider provider) {
      return helper.createDrawableIngredient(VanillaTypes.ITEM_STACK, provider.getItemStack());
   }

   protected BaseRecipeCategory(
      IGuiHelper helper, MekanismJEIRecipeType<RECIPE> recipeType, IItemProvider provider, int xOffset, int yOffset, int width, int height
   ) {
      this(helper, recipeType, provider.getTextComponent(), createIcon(helper, provider), xOffset, yOffset, width, height);
   }

   protected BaseRecipeCategory(
      IGuiHelper helper, MekanismJEIRecipeType<RECIPE> recipeType, Component component, IDrawable icon, int xOffset, int yOffset, int width, int height
   ) {
      this.recipeType = MekanismJEI.recipeType(recipeType);
      this.component = component;
      this.guiHelper = helper;
      this.icon = icon;
      this.xOffset = xOffset;
      this.yOffset = yOffset;
      this.background = new NOOPDrawable(width, height);
   }

   protected <ELEMENT extends GuiTexturedElement> ELEMENT addElement(ELEMENT element) {
      this.guiElements.add(element);
      return element;
   }

   protected GuiSlot addSlot(SlotType type, int x, int y) {
      return this.addElement(new GuiSlot(type, this, x - 1, y - 1));
   }

   protected GuiProgress addSimpleProgress(ProgressType type, int x, int y) {
      return this.addElement(new GuiProgress(this.getSimpleProgressTimer(), type, this, x, y));
   }

   protected GuiProgress addConstantProgress(ProgressType type, int x, int y) {
      return this.addElement(new GuiProgress(CONSTANT_PROGRESS, type, this, x, y));
   }

   @Override
   public int getLeft() {
      return -this.xOffset;
   }

   @Override
   public int getTop() {
      return -this.yOffset;
   }

   @Override
   public int getWidth() {
      return this.background.getWidth();
   }

   @Override
   public int getHeight() {
      return this.background.getHeight();
   }

   public RecipeType<RECIPE> getRecipeType() {
      return this.recipeType;
   }

   public Component getTitle() {
      return this.component;
   }

   public void draw(RECIPE recipe, IRecipeSlotsView recipeSlotsView, GuiGraphics guiGraphics, double mouseX, double mouseY) {
      PoseStack pose = guiGraphics.m_280168_();
      pose.m_85836_();
      pose.m_252880_(-this.xOffset, -this.yOffset, 0.0F);
      this.renderElements(recipe, recipeSlotsView, guiGraphics, (int)mouseX, (int)mouseY);
      pose.m_85849_();
   }

   protected void renderElements(RECIPE recipe, IRecipeSlotsView recipeSlotsView, GuiGraphics guiGraphics, int x, int y) {
      PoseStack pose = guiGraphics.m_280168_();
      this.guiElements.forEach(e -> e.renderShifted(guiGraphics, x, y, 0.0F));
      this.guiElements.forEach(e -> e.onDrawBackground(guiGraphics, x, y, 0.0F));
      int zOffset = 200;

      for (GuiTexturedElement element : this.guiElements) {
         pose.m_85836_();
         element.onRenderForeground(guiGraphics, x, y, zOffset, zOffset);
         pose.m_85849_();
      }
   }

   @Override
   public Font getFont() {
      return Minecraft.m_91087_().f_91062_;
   }

   public IDrawable getBackground() {
      return this.background;
   }

   public IDrawable getIcon() {
      return this.icon;
   }

   protected IProgressInfoHandler getSimpleProgressTimer() {
      if (this.timer == null) {
         this.timer = this.guiHelper.createTickTimer(20, 20, false);
      }

      return () -> this.timer.getValue() / 20.0;
   }

   protected GuiBar.IBarInfoHandler getBarProgressTimer() {
      if (this.timer == null) {
         this.timer = this.guiHelper.createTickTimer(20, 20, false);
      }

      return new GuiBar.IBarInfoHandler() {
         @Override
         public Component getTooltip() {
            return MekanismLang.PROGRESS.translate(new Object[]{TextUtils.getPercent(this.getLevel())});
         }

         @Override
         public double getLevel() {
            return BaseRecipeCategory.this.timer.getValue() / 20.0;
         }
      };
   }

   private IDrawable getOverlay(GuiGauge<?> gauge) {
      if (this.overlayLookup == null) {
         this.overlayLookup = new EnumMap<>(GaugeOverlay.class);
      }

      return this.overlayLookup.computeIfAbsent(gauge.getGaugeOverlay(), overlay -> this.createDrawable(this.guiHelper, overlay));
   }

   private IDrawable createDrawable(IGuiHelper helper, GaugeOverlay gaugeOverlay) {
      return helper.drawableBuilder(gaugeOverlay.getBarOverlay(), 0, 0, gaugeOverlay.getWidth(), gaugeOverlay.getHeight())
         .setTextureSize(gaugeOverlay.getWidth(), gaugeOverlay.getHeight())
         .build();
   }

   protected <STACK> STACK getDisplayedStack(IRecipeSlotsView recipeSlotsView, String slotName, IIngredientType<STACK> type, STACK empty) {
      return recipeSlotsView.findSlotByName(slotName).flatMap(view -> view.getDisplayedIngredient(type)).orElse(empty);
   }

   protected IRecipeSlotBuilder initItem(IRecipeLayoutBuilder builder, RecipeIngredientRole role, GuiSlot slot, List<ItemStack> stacks) {
      return this.initItem(builder, role, slot.getRelativeX(), slot.getRelativeY(), stacks);
   }

   protected IRecipeSlotBuilder initItem(IRecipeLayoutBuilder builder, RecipeIngredientRole role, int relativeX, int relativeY, List<ItemStack> stacks) {
      return (IRecipeSlotBuilder)builder.addSlot(role, relativeX + 1 - this.xOffset, relativeY + 1 - this.yOffset).addItemStacks(stacks);
   }

   protected IRecipeSlotBuilder initFluid(IRecipeLayoutBuilder builder, RecipeIngredientRole role, GuiGauge<?> gauge, List<FluidStack> stacks) {
      int width = gauge.m_5711_() - 2;
      int height = gauge.m_93694_() - 2;
      int max = stacks.stream().mapToInt(FluidStack::getAmount).filter(stackSize -> stackSize > 0).max().orElse(1000);
      return this.init(builder, ForgeTypes.FLUID_STACK, role, gauge, stacks).setFluidRenderer(max, false, width, height);
   }

   protected <STACK extends ChemicalStack<?>> IRecipeSlotBuilder initChemical(
      IRecipeLayoutBuilder builder, IIngredientType<STACK> type, RecipeIngredientRole role, GuiElement element, List<STACK> stacks
   ) {
      int width = element.m_5711_() - 2;
      int height = element.m_93694_() - 2;
      long max = stacks.stream().mapToLong(ChemicalStack::getAmount).filter(stackSize -> stackSize > 0L).max().orElse(1000L);
      return this.init(builder, type, role, element, stacks).setCustomRenderer(type, new ChemicalStackRenderer(max, width, height));
   }

   private <STACK> IRecipeSlotBuilder init(
      IRecipeLayoutBuilder builder, IIngredientType<STACK> type, RecipeIngredientRole role, GuiElement element, List<STACK> stacks
   ) {
      int x = element.getRelativeX() + 1 - this.xOffset;
      int y = element.getRelativeY() + 1 - this.yOffset;
      IRecipeSlotBuilder slotBuilder = (IRecipeSlotBuilder)builder.addSlot(role, x, y).addIngredients(type, stacks);
      if (element instanceof GuiGauge<?> gauge) {
         slotBuilder.setOverlay(this.getOverlay(gauge), 0, 0);
      }

      return slotBuilder;
   }
}
